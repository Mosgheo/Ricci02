package executors;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public final class SharedContext {

	private static final SharedContext SINGLETON = new SharedContext();
	private static String BASICURL;
	
	private List<String> masterLinks;
	private int depth; 
	private boolean start;
	private String initialUrl;
	private Graph graph;
	private Master master;
	private JLabel totalNodes;
	private boolean labelNodesIsSetted;
	private int nNodes;
	
	public SharedContext() {
		nNodes = 0;
		labelNodesIsSetted = false;
		masterLinks = new ArrayList<>();
		start = false;
		graph = new SingleGraph("grafo");
	}
	
	public boolean nodeExists (String title) {
		return graph.getNode(title) != null;
	}
	
	public boolean edgeExists (String title) {
		return graph.getEdge(title) != null;
	}
	
	public void addNode (String title) {
		synchronized (graph) {
			try {
				//System.out.println(title);
				graph.addNode(title);
				graph.getNode(title).addAttribute("ui.label", graph.getNode(title).getId());
				setLabelText(++nNodes);
			} catch(Exception e) {}
		}
	}
	
	public void addEdge (String title, String elem1, String elem2) {
		synchronized (graph) {
			try {
				graph.addEdge(title, elem1, elem2);
			} catch (Exception e) {}
		}
	}
	
	public Graph getGraph() {
		return this.graph;
	}
	
	// returns Singleton instance
	public static SharedContext getIstance() {
		return SharedContext.SINGLETON;
	}
	
	public int getDepth() {
		return this.depth;
	}
	
	public void setDepth(final int depth) {
		this.depth = depth;
	}
	
	public void setEnd(final boolean val) {
		this.start = false;
	}
	
	public List<String> getMasterList() {
		return this.masterLinks;
	}
	
	public boolean setMasterList(final String val) {
		synchronized (masterLinks) {
			try {
				if(!masterLinks.contains(val)) {
					//System.out.println(val);
					this.masterLinks.add(val);
					return true;
				}
			} catch(Exception e) {}
		}
		return false;
	}
	
	public String getInitialUrl() {
		return this.initialUrl;
	}
	
	public void setInitialUrl(final String url) {
		this.initialUrl = url;
	}
	
	public void running() {
		synchronized(this) {
			try {
			    start = true;
			    master = new Master(this);
			    this.notify();
			} catch(Exception e) {}
		}
	}
	
	public void execute() {
		this.master.execute();
		//this.master.compute();
	}
	
	public boolean isStarted() {
		return this.start;
	}
	
	public void setBasicUrl() {
		SharedContext.BASICURL = this.initialUrl.toString().substring(0, 25);
	}
	
	public String getBasicUrl() {
		return SharedContext.BASICURL;
	}
	
	public void setLabelCount(final JLabel jLabel) {
		this.totalNodes = jLabel;
	}
	
	public JLabel getLabelCount() {
		return this.totalNodes;
	}
	
	public boolean getIsLabelCountSetted() {
		return this.labelNodesIsSetted;
	}
	
	public void setLabelCount() {
		this.labelNodesIsSetted = true;
	}
	
	public void setLabelText(final int val) {
		this.totalNodes.setText("Total Nodes: " + val);
		
	}
	
	public static void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + "  ] " + msg);
	}
}
