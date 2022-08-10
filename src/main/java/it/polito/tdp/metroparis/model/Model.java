package it.polito.tdp.metroparis.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private Graph<Fermata, DefaultEdge> grafo;
	
	public void creaGrafo() {
		this.grafo = new SimpleDirectedGraph<Fermata, DefaultEdge>(DefaultEdge.class);
		
		MetroDAO dao = new MetroDAO();
		
		List<Fermata> fermate = dao.getAllFermate();
		Map<Integer, Fermata> fermateIdMap= new HashMap<Integer, Fermata>();
		
		for (Fermata f : fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}
		
		Graphs.addAllVertices(this.grafo, fermate);
		
		// METODO 1: soluzione più semplice ma fa un numero di accessi al database pari al quadrato del numero di vertici
		// Tale metodo itera su ogni coppia di vertici. Tuttavia in certi casi conviene usare tale metodo perchè
		// anche se è il più lento è anche il più semplice e talvolta torna molto utile
/*		for (Fermata partenza : fermate) {
			for (Fermata arrivo : fermate) {
				if (dao.isFermateConnesse(partenza, arrivo)) { // per ogni coppia di fermate fa una query al db
					this.grafo.addEdge(partenza, arrivo);
				}
			}
		}
*/		
		
		// METODO 2: dato ciascun vertice del grafo, tale metodo trova i vertici ad esso adiacenti
		// Variante 2a: il DAO restituisce un elenco di ID numerici
		// La query è un pò più complicata ma viene eseguita solo 619 volte , anzichè 383161 volte come nel metodo 1
		// Nota: posso iterare su 'fermate' oppure su 'this.grafo.vertexSet()'
/*		for (Fermata partenza : fermate) {
			List<Integer> idConnesse = dao.getIdFermateConnesse(partenza);
			for (Integer id : idConnesse) {
				Fermata arrivo = null;
				for (Fermata f : fermate) {  // fermate = this.grafo.vertexSet() ma NON dao.getAllFermate (operazione pesante)
					if (f.getIdFermata() == id) {   // fermata che possiede questo "id"
						arrivo = f;   // stiamo dando per scontato che l'elemento nella lista esista
						break;
					}
				}
				this.grafo.addEdge(partenza, arrivo);
			}
		}
*/		
		
		// METODO 2: dato ciascun vertice del grafo, tale metodo trova i vertici ad esso adiacenti
		// Variante 2b: il DAO restituisce un elenco di oggetti 'Fermata'
/*		for (Fermata partenza : fermate) {  // ciclo esterno su tutti i vertici
			List<Fermata> arrivi = dao.getFermateConnesse(partenza);
			for (Fermata arrivo : arrivi) { // questa volta il ciclo interno non viene fatto su tutti i vertici ma solo 
											// su quelli adiacenti alla fermata di partenza
				this.grafo.addEdge(partenza, arrivo); // sto passando a questo metodo il vertice partenza che esiste già nel grafo, 
			}									// mentre, l'oggetto arrivo non appartiene al grafo perchè è un oggetto nuovo:
		}										// non è un problema perchè l'importante è che tale oggetto sia uguale ad un oggetto
*/												// che invece nel grafo esiste. 
												// il lavoro sporco lo facciamo nello scrivere la query
		
		
		// METODO 2: dato ciascun vertice del grafo, tale metodo trova i vertici ad esso adiacenti
		// Variante 2c: il DAO restituisce un elenco di ID numerici che converto in oggetti tramite 
		// una Map<Integer, Fermata> --> pattern "Identity Map"
/*		for (Fermata partenza : fermate) {
			List<Integer> idConnesse = dao.getIdFermateConnesse(partenza);
			for (int id : idConnesse) {
				Fermata arrivo = fermateIdMap.get(id);
				this.grafo.addEdge(partenza, arrivo);
			}
		}
*/		
		
		// METODO 3: faccio una sola query che mi restituisca le coppie di fermate da collegare
		// (variante preferibile: usare Identity Map) 
		// qui il lavoro viene svolto solo dal dao, il model aggiunge solo gli archi nel grafo
		List<CoppiaId> fermateDaCollegare = dao.getAllFermateConnesse();
		for (CoppiaId coppia : fermateDaCollegare) {
			this.grafo.addEdge(fermateIdMap.get(coppia.getIdPartenza()), fermateIdMap.get(coppia.getIdArrivo()));
		}
		
		
		System.out.println(this.grafo);
		System.out.println("Vertici = " + this.grafo.vertexSet().size());
		System.out.println("Archi = " + this.grafo.edgeSet().size());
		
		visitaGrafo(fermate.get(0));
		
	}
	
	public void visitaGrafo(Fermata partenza) { // da una fermata di partenza voglio trovare le fermate adiacenti ad essa
		// dobbiamo inizializzare l'iteratore per fare in modo che parta dal vertice di partenza
		GraphIterator<Fermata, DefaultEdge> visita = new DepthFirstIterator<>(this.grafo, partenza);
		
		while (visita.hasNext()) {  // quando l'iteratore è arrivato alla fine termina il ciclo
			Fermata f = visita.next();
			System.out.println(f);
		}
		
	}
	
}
