package executors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import org.json.JSONArray;
import org.json.JSONObject;

public class HttpRequestTask extends RecursiveAction {
	
	private SharedContext sharedContext;
	private URL link;
	private String content;
	private int depth;
	
	public HttpRequestTask(final SharedContext sharedContext, final URL link) {
		this.sharedContext = sharedContext;
		this.link = link;
		this.content = link.toString().substring(30);
		this.depth = 0;
	}
	
	public HttpRequestTask(final SharedContext sharedContext, final String content, final int depth) {
		this.sharedContext = sharedContext;
		this.content = content;
		this.depth = depth;
	}

	@Override
	protected void compute() {
		List<RecursiveAction> tasks = new LinkedList<RecursiveAction>();
		if(this.depth++ < this.sharedContext.getDepth()) {
			if(depth == 1) {
				sharedContext.addNode(sharedContext.getInitialUrl().substring(30));
			}
			
			JSONObject jsonObject = this.getConnectionResponse();
			//For bad request 400
		    if(jsonObject==null || !jsonObject.has("parse")) {
		    	return;
		    }
		    
		    JSONArray jsonArray = jsonObject.getJSONObject("parse").getJSONArray("links");
		    for(int i = 0; i < jsonArray.length(); i++) {
		    	if(jsonArray.getJSONObject(i).getInt("ns") == 0) {
		    		String str = jsonArray.getJSONObject(i).getString("*");
		    		HttpRequestTask httpTask = new HttpRequestTask(this.sharedContext, str, depth);
		    		tasks.add(httpTask);
		    		httpTask.fork();
		    	}
		    }
		    
		    GraphRappresentationTask graphTask = new GraphRappresentationTask(sharedContext, jsonArray, content);
		    tasks.add(graphTask);
		    graphTask.fork();
		    
		    for(RecursiveAction task : tasks) {
		    	task.join();
		    }
		}
	}
	
	private void parseUrl() {
		try {
			String parsedUrl = sharedContext.getBasicUrl()+"w/api.php?action=parse&page="+
					URLEncoder.encode(this.content.replaceAll("\\s", "_"), "UTF-8")+"&format=json&section=0&prop=links";
			URL myURL = new URL(parsedUrl);
			this.link = myURL;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	private JSONObject getConnectionResponse() {
		JSONObject jsonObject = new JSONObject();
		try {
			this.parseUrl();
			HttpURLConnection connection = (HttpURLConnection)link.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			
			if(connection.getResponseCode() != 429) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
			    StringBuffer response = new StringBuffer();
			    while ((inputLine = reader.readLine()) != null) {
			    	response.append(inputLine);
			    }
			    reader.close();
			    jsonObject = new JSONObject(response.toString());
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

}
