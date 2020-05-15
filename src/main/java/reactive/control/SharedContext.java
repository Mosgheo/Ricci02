package reactive.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;


import io.reactivex.rxjava3.subjects.PublishSubject;
import reactive.model.linksFinder;

public final class SharedContext {

	private static final SharedContext SINGLETON = new SharedContext();
	//stream used to update N. of nodes in the view
	private PublishSubject<Integer> stream = PublishSubject.create();
	private Graph graph;
	//variables for the wikipedia api
	private static String BASICURL;
	private String initialUrl;

	
	public SharedContext() {
		//initialize graph
		graph = new SingleGraph("grafo");
	}
	
	/**
	 * Start the main program
	 * 
	 * @param base: first word to search
	 * @param depth: desirred depth to reach
	 */
	public void Start(String base, Integer depth) {
		new linksFinder(getIstance(), base, depth).start();	
	}
	
	/**
	 * Check if a given node exists
	 * 
	 * @param title: name of the node to search
	 * @return boolean
	 */
	public boolean nodeExists (String title) {
		synchronized(graph) {
			return graph.getNode(title) != null;
		}
	}
	
	/**
	 * Check if a given edge exists
	 * 
	 * @param node1: name of the father node
	 * @param node2: name of the child node
	 * @return boolean
	 */
	public boolean edgeExistsTo (String node1, String node2) {
		synchronized(graph) {
			return graph.getNode(node1).hasEdgeToward(node2);
		}
		
	}
	
	/**
	 * Adds a node to the graph
	 * 
	 * @param title: name of the node
	 * @param color: color of the node
	 */
	public void addNode (String title, String color) {
		synchronized(graph) {
			Node node = graph.addNode(title);
			node.addAttribute("ui.label", title);
			node.addAttribute("ui.style", "fill-color: "+ color);
			stream.onNext(graph.getNodeCount());
		}
	}
	
	/**
	 * Adds an edge to the graph
	 * 
	 * @param title: name of the edge
	 * @param elem1: name of the father node
	 * @param elem2: name of the child node
	 */
	public void addEdge (String title, String elem1, String elem2) {
		synchronized(graph) {
			graph.addEdge(title, elem1, elem2);
		}
	}	

	/**
	 * Get the children of a given node
	 * 
	 * @param node: name of the node
	 * @return a list containing the children of the given node
	 */
	public ArrayList<String> getChildren(String node) {
		synchronized(graph) {
			ArrayList<String> children = new ArrayList<String>();
			
			for(Edge edge : graph.getNode(node).getEachEdge()) {
				//!= null needed otherwise the library throws exceptions
				if(edge != null && edge.getSourceNode().getId().equals(node)) {
					children.add(edge.getTargetNode().getId());
				}
			}
			
			return children;
		}
	}
	
	/**
	 * Deletes an edge and eventually any node or its children which remains detached from the graph
	 * 
	 * @param node: name of the node
	 * @param father: node father
	 */
	public void removeEdgeAndClean(String node, String father) {
		synchronized(graph) {
			if(!father.equals(node) && nodeExists(node) && nodeExists(father)) {
				graph.removeEdge(father, node);				
	
				Node toRemove = graph.getNode(node);
				Collection<Edge> edges =  new HashSet<Edge> (toRemove.getEdgeSet());
				
				Collection<Edge> fathers = new HashSet<Edge>();
				for(Edge edge : edges) {
					if(edge != null && edge.getTargetNode().equals(toRemove)) {
						fathers.add(edge);
					}
				}
				
				
				if(fathers.isEmpty()) {				
					for(Edge edge : edges) {
						if(edge != null && edge.getSourceNode().equals(toRemove)) {
							removeEdgeAndClean(edge.getTargetNode().getId(), node);
						}
					}
					graph.removeNode(node);
					stream.onNext(graph.getNodeCount());
				}
			}
		}
	}
	
	//GETTER
	
	public Graph getGraph() {
		return graph;
	}
	
	public PublishSubject<Integer> getStream() {
		return stream;
	}
	
	public String getBasicUrl() {
		return SharedContext.BASICURL;
	}
	
	public String getInitialUrl() {
		return this.initialUrl;
	}

	public static SharedContext getIstance() {
		return SharedContext.SINGLETON;
	}
	
	//SETTER
	
	public void setBasicUrl() {
		SharedContext.BASICURL = this.initialUrl.toString().substring(0, 25);
	}

	public void setInitialUrl(final String url) {
		this.initialUrl = url;
		setBasicUrl();
	}
}
