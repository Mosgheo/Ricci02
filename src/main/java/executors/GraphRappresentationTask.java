package executors;

import java.util.concurrent.RecursiveAction;

import org.json.JSONArray;

public class GraphRappresentationTask extends RecursiveAction{

	private SharedContext sharedContext;
	private String content;
	private JSONArray links;
	
	/**
	 * Creates a task to draw a part of nodes and edges
	 * 
	 * @param sharedContext
	 * @param links
	 * @param content
	 */
	public GraphRappresentationTask(final SharedContext sharedContext, final JSONArray links, final String content) {
		this.links = links;
		this.content = content;
		this.sharedContext = sharedContext;
	}
	
	
	//Called after HTTP request to draw nodes and edges
	@Override
	protected void compute() {
		sharedContext.addNode(content);
	    for(int i = 0; i < links.length(); i++) {
	    	if(links.getJSONObject(i).getInt("ns") == 0) {
	    		String str = links.getJSONObject(i).getString("*");
	    		if(!this.sharedContext.nodeExists(str)) {
	    			this.sharedContext.addNode(str);
	    		}
	    		if(!this.sharedContext.edgeExistsTo(content, str) && !sharedContext.edgeExistsTo(str, content)) {
	    			this.sharedContext.addEdge(content+str, content, str);
	    		}
	    	}
	    }
	}

}
