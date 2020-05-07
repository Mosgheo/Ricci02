package vertxImpl;

import java.util.LinkedList;

import io.vertx.core.AbstractVerticle;
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
	private int count;
	private int instancies;
	private int maxDept;
	
	public GuiVerticle(final int instancies) {
		this.instancies = instancies;
	}
	@Override
	public void start(final Future<Void> startFuture) {
		try {
			super.start(startFuture);
		} catch (Exception e) {
		}
		count = 0;
		eb = vertx.eventBus();
		graph = new SingleGraph("grafo");
		view = new Gui(1024, 768, eb, graph);
		view.display();
		eb.consumer("init", message -> {
				String[] f = message.body().toString().split(":");
				LinkedList<String> words = new LinkedList<String>();
				words.add(f[0]);
				maxDept = Integer.parseInt(f[1]);
				initialMillis = System.currentTimeMillis();
				vertx.deployVerticle(new MyVerticle(instancies,DEPT, maxDept, words));
		});
		eb.consumer("updateView", message -> {
			// System.out.println("RECEIVED");
			// view.updateView((List<String>)message.body());
			DataHolder c = (DataHolder) message.body();
			NodeTuple newWords = c.getData();
			updateView(newWords);
		});
		eb.consumer("stop", message -> {
			System.out.println("RECEIVED A STOP; WAITING FOR OTHERS");
			finalMillis = System.currentTimeMillis();
			long diff = finalMillis - initialMillis;
			count++;
			if (count == Math.pow(instancies, maxDept-1)) {
				System.out.println("EVERYONE STOPPED, TIME:" + diff);
				//vertx.deploymentIDs().forEach(vertx::undeploy);
				view.updateLabel();
				vertx.close();
			}

		});
	}

	@Override
	public void stop(final Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
		System.out.println("MyGUIVerticle stopped!");
	}

	private void updateView(final NodeTuple node) {
		String value = node.getValue();
		String father = node.getFather();
		synchronized (graph) {
			try {
				if (!nodeExists(value)) {
					graph.addNode(value);
					graph.getNode(value).addAttribute("ui.label", graph.getNode(value).getId());
					// System.out.println("NNOODDII: "+graph.getNodeCount());
					graph.addEdge(father + value, father, value);
				} else {
					graph.addEdge(father + value, father, value);
				}
			} catch (Exception e) {
			}
		}
	}

	private boolean nodeExists(String title) {
		return graph.getNode(title) != null;
	}
}
