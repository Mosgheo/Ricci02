package vertxImpl;

import java.util.LinkedList;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class GuiVerticle extends AbstractVerticle {
	private Gui view;
	private EventBus eb;
	private Graph graph;
	private static final int DEPT = 0;
	private long initialMillis;
	private long finalMillis;
	private int nodesCount;
	private int instancies;
	private int maxDept;
	private List<NodeTuple> nodes;
	
	public GuiVerticle(final int instancies) {
		this.instancies = instancies;
	}
	@Override
	public void start(final Future<Void> startFuture) {
		try {
			super.start(startFuture);
		} catch (Exception e) {
		}
		nodes = new LinkedList<NodeTuple>();
		nodesCount = 0;
		eb = vertx.eventBus();
		graph = new SingleGraph("grafo");
		view = new Gui(1024, 768, graph,eb);
		view.display();
		/**
		 * When a GUI worker verticle is deployed, it launches the view and
		 * the first verticle.
		 */
		eb.consumer("init", message -> {
				String[] f = message.body().toString().split(":");
				LinkedList<String> words = new LinkedList<String>();
				words.add(f[0]);
				maxDept = Integer.parseInt(f[1]);
				initialMillis = System.currentTimeMillis();
				vertx.deployVerticle(new MyVerticle(instancies,DEPT, maxDept, words));
		});
		/**
		 * Every time a word is found and a node has to be added, it's sent through a
		 * "updateView" message.
		 */
		eb.consumer("updateView", message -> {
			DataHolder c = (DataHolder) message.body();
			NodeTuple newWords = c.getData();
			nodes.add(newWords);
			updateView(newWords);
			//view.updateLabel();
		});
		
		/**
		 * When a verticle ends its computation because the targeted depth is reached,
		 * it sends a stop message, when every computing verticle sends the stop message,
		 * vertx shuts them down.
		 */
		eb.consumer("stop", message -> {
			//System.out.println(vertx.deploymentIDs().size());
			//System.out.println("RECEIVED  STOP NUMBER "+nodesCount+" ; WAITING FOR OTHERS");
			finalMillis = System.currentTimeMillis();
			long diff = finalMillis - initialMillis;
			nodesCount++;
			if (nodesCount == Math.pow(instancies, maxDept-1)) {
				System.out.println("EVERYONE STOPPED, TIME:" + diff);
				view.updateLabel();
				vertx.close();
				//Closes the program and undeploys every verticle instanciated.
				//vertx.deployVerticle(new UpdateVerticle(graph,nodes.get(0)),new DeploymentOptions().setWorker(true));
			}

		});

	}

	@Override
	public void stop(final Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
		System.out.println("MyGUIVerticle stopped!");
	}
	
	/**
	 * Just updates the view graph every time MyVerticle finds a new word.
	 */
	private void updateView(final NodeTuple node) {
		String value = node.getValue();
		String father = node.getFather();
			try {
				if (!nodeExists(value)) {
					graph.addNode(value);
					graph.getNode(value).addAttribute("ui.label", graph.getNode(value).getId());
					// System.out.println("NNOODDII: "+graph.getNodeCount());
					graph.addEdge(father + value, father, value);
				} else {
					/*
					 * If a node already is in the graph, an edge between the already displayed node and the just found node's father is created
					 */
					graph.addEdge(father + value, father, value);
				}
			} catch (Exception e) {
			}
		}
	/**
	 * Checks if a certain word is already in the graph. 
	 */
	private boolean nodeExists(final String title) {
		return graph.getNode(title) != null;
	}
	
}
