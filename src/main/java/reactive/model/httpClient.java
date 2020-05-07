package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import control.SharedContext;


public class httpClient {
	
	private URL url;
	private boolean isConnected;
	private List<String> result = new ArrayList<String>();
	private final SharedContext context;
	
	public httpClient(final URL url, SharedContext context) {
		this.url = url;
		this.context = context;
		this.isConnected = false;
	}
	
	public boolean connect() {	
		
		try {
			URL parsedUrl = parseUrl();
			
			JSONObject jsonObject = this.getConnectionResponse(parsedUrl);
			
		    if(jsonObject == null || !jsonObject.has("parse")) {
		    	return false;
		    }
		    
		    JSONArray jsonArray = jsonObject.getJSONObject("parse").getJSONArray("links");
			
		    for(int i = 0; i < jsonArray.length(); i++) {
		    	if(jsonArray.getJSONObject(i).getInt("ns") == 0) {
		    		result.add(jsonArray.getJSONObject(i).getString("*"));
	    		}
	    	}
		    
			isConnected = true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return isConnected;
	}
	
	private URL parseUrl() {
		
		String basicUrl = context.getBasicUrl();
		String content = url.toString().substring(30);
		URL myURL = null;
		
		try {
			String parsedUrl = basicUrl+"w/api.php?action=parse&page="+
					URLEncoder.encode(content.replaceAll("\\s", "_"), "UTF-8")+"&format=json&section=0&prop=links";
			myURL = new URL(parsedUrl);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return myURL;		
	}
	
	private JSONObject getConnectionResponse(URL link) {
		JSONObject jsonObject = new JSONObject();
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
			    jsonObject = new JSONObject(response.toString());
			}
		} catch (Exception e) {
			System.out.println("TIMEOUT");
			return null;
		}
		return jsonObject;
		
	}
	
	public List<String> getResult(){
		return isConnected ? result : null;
	}
}
