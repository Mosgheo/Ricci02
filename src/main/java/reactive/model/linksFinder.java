package reactive.model;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import reactive.control.SharedContext;

public class linksFinder {

	private final SharedContext context;
	private final String base;
	private final int depth;

	public linksFinder(final SharedContext context, String base, int depth) {
		this.context = context;
		this.base = base;
		this.depth = depth;
	}
	
	/**
	 * Start the research for the first node
	 *  
	 */
	public void start() {
		//create the first observable which will generate a random color for all the first sons
		Observable<Voice> source = Observable.create(emitter -> {	     
			log("starting...");
			
			if(depth > 0){
				URL converted = new URL(base);
				httpClient client = new httpClient(converted, context);
				if(client.connect()) {
					String baseTitle = base.split("/")[4];
					//TODO not hardcoded
					context.addNode(baseTitle,"rgb(0,0,0);");
					
					List <String> titles = client.getResult();
					for (String title : titles) {
						emitter.onNext(new Voice(1, title, baseTitle, generateColor()));
					}
					checkNodeForUpdates(new Voice(0,baseTitle,null,"rgb(0,0,0);"));
				}
			}	
		 });
		
		log("subscribing.");

		//generate a new Observable for each voice found until we reach desired depth
		source
		.subscribeOn(Schedulers.io())
		.subscribe((s) -> {
			handleNode(s);
		}, Throwable::printStackTrace);
	}
	
	/**
	 * As a node is found decide if add the new node to the graph, an edge only or just check for updates on it
	 *  
	 * @param node: the received node
	 */
	private synchronized void handleNode(Voice node) {
		//if the node exists add node and edge else add only edge
		if(!context.nodeExists(node.getTitle())) {	

			context.addNode(node.getTitle(), node.getColor());
			context.addEdge(node.getFather() + node.getTitle(),node.getFather(),node.getTitle());			
			
		} else if(!context.edgeExistsTo(node.getFather(), node.getTitle())) {	
			//if the edge between the two exists this instruction will be ignored
			context.addEdge(node.getFather() + node.getTitle(),node.getFather(),node.getTitle());										
		}
		
		//we keep going in deep anyway since the depth may differ
		createAndSubscribe(node);	
		//Check for updates on the node
		checkNodeForUpdates(node);
	}	
	
	/**
	 * Recursively look for connected nodes
	 *   
	 * @param node: the received node
	 */
	private void createAndSubscribe(Voice node)
	{
		//if reached desired depth don't go further down
		if(node.getDepth() < depth) {	
			
			Observable<Voice> newNode = Observable.create(emitter -> {	  
				URL converted = null;
				
				try {
					converted = new URL("https://it.wikipedia.org/wiki/" + node.getTitle());
				} catch (MalformedURLException e) {
					// TODO add further url check
					e.printStackTrace();
					return;
				}
				
				httpClient client = new httpClient(converted, context);
				
				if(client.connect() && client.getResult() != null) {
					
					List <String> titles = client.getResult();
					
					for (String title : titles) {	
						
						emitter.onNext(new Voice(node.getDepth()+1, title, node.getTitle(), node.getColor()));
					}								
				}
			});		
			
			newNode
			.subscribeOn(Schedulers.io())
			.subscribe((s) -> {				
				handleNode(s);
			});	
		}
	}

	//OPTIONAL PART, KEEP CHECKING FOREVER FOR UPDATES ON THIS NODE
	private void checkNodeForUpdates(Voice node) {		
		if(node.getDepth() < depth) {			
			final URL converted;
			
			try {
				converted = new URL("https://it.wikipedia.org/wiki/" + node.getTitle());
			} catch (MalformedURLException e) {
				// TODO add further url check
				e.printStackTrace();
				return;
			}

			Flowable
			.interval(15, TimeUnit.SECONDS)
			.subscribeOn(Schedulers.computation())
			.subscribe((s) -> {
				//in case the node has been removed
				if(!context.nodeExists(node.getTitle())) {
					return;
				}
				
				httpClient client = new httpClient(converted, context);
				
				if(client.connect() && client.getResult() != null) {

					ArrayList<String> children = context.getChildren(node.getTitle());
					List <String> titles = client.getResult();
					
					for (String title : titles) {	
						if(!context.nodeExists(title)) {	
							log("FOUND UPDATE FROM: "+node.getTitle()+ " --- " + title);
							handleNode(new Voice(node.getDepth() + 1, title, node.getTitle(), node.getColor()));
						}
					}
					
					for (String child : children) {
						if(!titles.contains(child)) {
							log("REMOVING NODE FROM: " + node.getTitle() + " --- " + child);
							context.removeEdgeAndClean(child, node.getTitle());	
							children.remove(child);
						}
					}			
				}
			});
		}
		
	}

	private String generateColor() {
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		Color randomColor = new Color(r, g, b);
		return "rgb(" + randomColor.getRed() + "," + randomColor.getGreen() + "," + randomColor.getBlue() + ");";
	}
	
	private static void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + "  ] " + msg);
	}
}
