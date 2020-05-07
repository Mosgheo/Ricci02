package executors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class LinkAnalysisTask implements Runnable {
	
	private URL link;
	private SharedContext sharedContext;
	private String content;
	
	public LinkAnalysisTask(final URL content, final SharedContext sharedContext) {
		this.sharedContext = sharedContext;
		this.content = content.toString().substring(30);
	}
	
	public LinkAnalysisTask(final String content, final SharedContext sharedContext) {
		this.sharedContext = sharedContext;
		this.content = content;
	}

	@Override
	public void run() {
		try {
			this.parseUrl();
			HttpURLConnection connection = (HttpURLConnection)link.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			
			if(connection.getResponseCode() == 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
			    StringBuffer response = new StringBuffer();
			    while ((inputLine = reader.readLine()) != null) {
			    	response.append(inputLine);
			    }
			    reader.close();
			    JSONObject jsonObject = new JSONObject(response.toString());
			    //System.out.println(response.toString());
			    if(!jsonObject.has("parse")) {
			    	return;
			    }
			    JSONArray jsonArray = jsonObject.getJSONObject("parse").getJSONArray("links");
			    sharedContext.addNode(content);
		    	
			    for(int i = 0; i < jsonArray.length(); i++) {
			    	if(jsonArray.getJSONObject(i).getInt("ns") == 0) {
			    		String str = jsonArray.getJSONObject(i).getString("*");
			    		//SharedContext.log(str);
			    		if(!this.sharedContext.getMasterList().contains(str)) {
				    		this.sharedContext.setMasterList(str);
							this.sharedContext.addNode(str);
			    		}
			    		if(!this.sharedContext.edgeExists(content+str) && !this.sharedContext.edgeExists(str+content)) {
			    			this.sharedContext.addEdge(content+str, content, str);
			    		}
			    	}
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseUrl() {
		String parsedUrl = sharedContext.getBasicUrl()+"w/api.php?action=parse&page="+this.content.replaceAll("\\s", "_")+"&format=json&section=0&prop=links";
		try {
			URL myURL = new URL(parsedUrl);
			this.link = myURL;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
