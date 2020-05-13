package executors;

import javax.swing.JLabel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

//Class with all shared variables
public final class SharedContext {

	private static final SharedContext SINGLETON = new SharedContext();
	private static String BASICURL;
	
	private int depth; 
	private boolean start;
	private String initialUrl;
	private Graph graph;
	private Master master;
	private JLabel totalNodes;
	private int nNodes;
	

	/**
	 * Creates an instance of SharedContext
	 * 
	 */
	public SharedContext() {
		nNodes = 0;
		start = false;
		graph = new SingleGraph("grafo");
		graph.setStrict(true);
	}
	
	//Check if a node already exist
	public boolean nodeExists(final String title) {
		return graph.getNode(title) != null;
	}
	
	//Check if an edge already exist
	public boolean edgeExistsTo(final String node1, final String node2) {
		return graph.getNode(node1).hasEdgeToward(node2);
	}
	
	//Method to create and add a node and increment total nodes
	public void addNode(final String title) {
		synchronized (graph) {
			try {
				if(!nodeExists(title)) {
					graph.addNode(title).addAttribute("ui.label", graph.getNode(title).getId());
					setLabelText(++nNodes);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//Method to create and add an edge
	public void addEdge(final String title, final String elem1, final String elem2) {
		synchronized (graph) {
			try {
				if(!edgeExistsTo(elem1, elem2) && !edgeExistsTo(elem2, elem1)) {
					graph.addEdge(title, elem1, elem2);
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//Returns the current graph
	public Graph getGraph() {
		return this.graph;
	}
	
	//Returns Singleton instance
	public static SharedContext getIstance() {
		return SharedContext.SINGLETON;
	}
	
	//Return the Maximum depth
	public int getDepth() {
		return this.depth;
	}
	
	//Set depth at the start of the program
	public void setDepth(final int depth) {
		this.depth = depth;
	}
	
	//Get the initial URL
	public String getInitialUrl() {
		return this.initialUrl;
	}
	
	//Set initial URL
	public void setInitialUrl(final String url) {
		this.initialUrl = url;
	}
	
	//Called when i click on button RUN
	public void running() {
		synchronized(this) {
			try {
			    start = true;
			    master = new Master(this);
			    this.notify();
			} catch(Exception e) {}
		}
	}
	
	//Invoke Master thread to call tasks
	public void execute() {
		this.master.execute();
	}
	
	//Check if the program is started
	public boolean isStarted() {
		return this.start;
	}
	
	//Set the initial part of the URL
	public void setBasicUrl() {
		SharedContext.BASICURL = this.initialUrl.toString().substring(0, 25);
	}
	
	//Return the initial part of the UR
	public String getBasicUrl() {
		return SharedContext.BASICURL;
	}
	
	//Initialize instance of the label count
	public void setLabelCount(final JLabel jLabel) {
		this.totalNodes = jLabel;
	}
	
	//Set text of the label
	public void setLabelText(final int val) {
		this.totalNodes.setText("Total Nodes: " + val);
		
	}
	
	public static void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + "  ] " + msg);
	}
}
