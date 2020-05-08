package reactive.control;

import java.util.Collection;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;


import io.reactivex.rxjava3.subjects.PublishSubject;
import reactive.model.linksFinder;

public final class SharedContext {

	private static final SharedContext SINGLETON = new SharedContext();
	private PublishSubject<Integer> stream = PublishSubject.create();
	private Graph graph;
	private static String BASICURL;
	private String initialUrl;
	
	public SharedContext() {
		graph = new SingleGraph("grafo");
		graph.setStrict(false);
	}
	
	public void Start(String base, Integer depth) {
		new linksFinder(getIstance(), base, depth).start();	
	}
	
	public boolean nodeExists (String title) {
		synchronized(graph) {
			return graph.getNode(title) != null;
		}
	}
	
	public boolean edgeExistsTo (String node1, String node2) {
		synchronized(graph) {
			return graph.getNode(node1).hasEdgeToward(node2);
		}
	}

	public void addNode (String title, String color) {
		synchronized(graph) {
			Node node = graph.addNode(title);
			node.addAttribute("ui.label", title);
			node.addAttribute("ui.style", "fill-color: "+ color);
			stream.onNext(graph.getNodeCount());
		}
	}
	
	public void addEdge (String title, String elem1, String elem2) {
		synchronized(graph) {
			graph.addEdge(title, elem1, elem2);
		}
	}	

	
	public Graph getGraph() {
		return graph;
	}
	
	public PublishSubject<Integer> getStream() {
		return stream;
	}
	
	// returns Singleton instance
	public static SharedContext getIstance() {
		return SharedContext.SINGLETON;
	}
	

	public void setBasicUrl() {
		SharedContext.BASICURL = this.initialUrl.toString().substring(0, 25);
	}
	
	public String getBasicUrl() {
		return SharedContext.BASICURL;
	}

	public String getInitialUrl() {
		return this.initialUrl;
	}
	
	public void setInitialUrl(final String url) {
		this.initialUrl = url;
		setBasicUrl();
	}

	//if i get an update and need to delete a node i also check all the children
	public void removeEdgeAndClean(String node, String father) {
		synchronized(graph) {
			graph.removeEdge(father, node);
			
			Node toRemove = graph.getNode(node);
			Collection<Edge> fathers = toRemove.getEnteringEdgeSet();
			
			if(fathers.isEmpty()) {
				for(Edge edge : toRemove.getLeavingEdgeSet()) {
					removeEdgeAndClean(edge.getTargetNode().getId(), node);
				}
				graph.removeNode(node);
				stream.onNext(graph.getNodeCount());
			}
		}
	}

	
	private static void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + "  ] " + msg);
	}
}
