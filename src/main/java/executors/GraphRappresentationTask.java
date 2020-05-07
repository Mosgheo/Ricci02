package executors;

import java.util.concurrent.RecursiveAction;

import org.json.JSONArray;

public class GraphRappresentationTask extends RecursiveAction{

	private SharedContext sharedContext;
	private String content;
	private JSONArray links;
	
	public GraphRappresentationTask(final SharedContext sharedContext, final JSONArray links, final String content) {
		this.links = links;
		this.content = content;
		this.sharedContext = sharedContext;
	}
	
	
	@Override
	protected void compute() {
		sharedContext.addNode(content);
	    for(int i = 0; i < links.length(); i++) {
	    	if(links.getJSONObject(i).getInt("ns") == 0) {
	    		String str = links.getJSONObject(i).getString("*");
	    		if(!sharedContext.nodeExists(str)) {
	    			this.sharedContext.addNode(str);
	    		}
	    		if(!this.sharedContext.edgeExists(content+str) && !this.sharedContext.edgeExists(str+content)) {
	    			this.sharedContext.addEdge(content+str, content, str);
	    		}
	    	}
	    }
	}

}
