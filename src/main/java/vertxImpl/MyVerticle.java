package vertxImpl;

import java.util.LinkedList;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

public class MyVerticle extends AbstractVerticle {
	private static final String WIKI = "hhttps://it.wikipedia.org/w/api.php";
	private static final int ROOT = 0;
	private WebClient client;
	private List<NodeTuple> words;
	private EventBus eb;
	private int dept;
	private int maxDept;
	private List<String> assigned;
	private LinkedList<String> toAssign;
	private JsonArray links;
	private int instancies;

	public MyVerticle(int instancies, int dept, int maxDept, final List<String> voices) {
		this.dept = dept;
		this.maxDept = maxDept;
		this.assigned = voices;
		this.instancies = instancies;
	}

	@Override
	public void start(Future<Void> startFuture) {
		try {
			super.start(startFuture);
		} catch (Exception e) {
		}
		// System.out.println("I've been deployed!");
		this.words = new LinkedList<NodeTuple>();
		this.toAssign = new LinkedList<String>();
		eb = vertx.eventBus();
		if (dept == ROOT) {
			NodeTuple tmp = new NodeTuple("root", assigned.get(ROOT));
			eb.send("updateView", new DataHolder(tmp));
		}
		client = WebClient.create(vertx, new WebClientOptions().setSsl(true).setTrustAll(true).setDefaultPort(443)
				.setKeepAlive(true).setDefaultHost("www.wikipedia.org")

		);

		// eb.send("updateView", new DataHolder(new NodeTuple(ROOT,assigned.get(0)));
		startFuture = compute(assigned).onComplete(t -> {
			dept++;
			if (dept == maxDept) {
				eb.send("stop", "");
			} else {
				// System.out.println("Passing onto a new level");
				// System.out.println(toAssign.size());
				int tmp = toAssign.size() / instancies;
				int oldTmp = 0;
				List<String> verticleWords;
				/*
				 * List<String>c1 = toAssign.subList(oldTmp, oldTmp+=tmp);
				 * //System.out.println(c1.size()); List<String>c2 = toAssign.subList(oldTmp,
				 * oldTmp+=tmp); //System.out.println(c2.size()); List<String>c3 =
				 * toAssign.subList(oldTmp, oldTmp+=tmp); //System.out.println(c3.size());
				 * List<String>c4 = toAssign.subList(oldTmp, oldTmp+=tmp);
				 * //System.out.println(c4.size()); List<String>c5 = toAssign.subList(oldTmp,
				 * oldTmp+=tmp); //System.out.println(c4.size()); List<String>c6 =
				 * toAssign.subList(oldTmp, toAssign.size()); //System.out.println(c4.size());
				 * vertx.deployVerticle(new MyVerticle(instancies,dept, maxDept,c1));
				 * vertx.deployVerticle(new MyVerticle(instancies,dept, maxDept,c2));
				 * vertx.deployVerticle(new MyVerticle(instancies,dept, maxDept,c3));
				 * vertx.deployVerticle(new MyVerticle(instancies,dept, maxDept,c4));
				 * vertx.deployVerticle(new MyVerticle(instancies,dept, maxDept,c5));
				 * vertx.deployVerticle(new MyVerticle(instancies,dept, maxDept,c6));
				 */
				if(tmp>0) {
					for (int i = 0; i < instancies; i++) {
						if (i == instancies - 1) {
							verticleWords = toAssign.subList(oldTmp, toAssign.size());
							vertx.deployVerticle(new MyVerticle(instancies, dept, maxDept, verticleWords));
						} else {
							verticleWords = toAssign.subList(oldTmp, oldTmp += tmp);
							vertx.deployVerticle(new MyVerticle(instancies, dept, maxDept, verticleWords));
						}
					}
				}else {
					vertx.deployVerticle(new MyVerticle(instancies, dept, maxDept, toAssign));
				}
			}
		});
	}

	@Override
	public void stop(final Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
		System.out.println("MyVerticle at dept " + dept + " stopped!");
	}

	private Future<Void> compute(List<String> toBeSearched) {
		LinkedList<String> c = new LinkedList<String>();
		c.addAll(toBeSearched);
		Promise<Void> promise = Promise.promise();
		links = new JsonArray();
		if (dept < maxDept) {
			String father;
			if (c.size() > 0) {

				father = c.get(0);
				client.get(WIKI).addQueryParam("action", "parse").addQueryParam("page", father.replaceAll("\\s", "_"))
						.addQueryParam("format", "json").addQueryParam("section", "0").addQueryParam("prop", "links")
						.as(BodyCodec.jsonObject()).send(ar -> {
							if (ar.succeeded()) {
								/**
								 * ERRORE: Per ogni father crea un nuovo verticle passando alla depth
								 * successiva.
								 */
								// System.out.println("Trovata una");
								/* Promise tmp = promises.get(j); */
								sendWords(ar.result(), father).onComplete(t -> {
									c.remove(0);
									compute(c).onComplete(t2 -> {
										promise.complete();
									});
								});
							} else {
								// System.out.println("ERROR 404");
								// System.out.println(ar.cause());
								// System.out.println(father);
								c.remove(0);
								compute(c).onComplete(t2 -> {
									promise.complete();
								});
							}
						});
			} else {
				promise.complete();
			}
			// promise = Promise.promise();
			/*
			 * if(i ==toBeSearched.size()-1 ) { Future[] itemsArray = new
			 * Future[toBeSearched.size()]; itemsArray = promises.toArray(itemsArray);
			 * CompositeFutureImpl.all(itemsArray).onComplete(ar ->{ promise.complete(); });
			 * }
			 */

		}
		// System.out.println("HELLO");
		return promise.future();
	}

	private Future<Void> sendWords(HttpResponse<JsonObject> response, String father) {
		Promise<Void> promise = Promise.promise();
		JsonObject body = response.body();
		if (body == null) {
			System.out.println("BODY EMPTY");
			promise.complete();
		}

		if (body.getJsonObject("parse") == null || body.getJsonObject("parse").getJsonArray("links") == null) {
			System.out.println("LINK EMPTY, FATHER:" + father);
			promise.complete();
		} else {
			links = body.getJsonObject("parse").getJsonArray("links");
			for (int i = 0; i < links.size(); i++) {
				final int j = i;
				if (links.getJsonObject(i).getInteger("ns") == 0) {
					NodeTuple node = new NodeTuple(father, links.getJsonObject(i).getString("*"));
					updateView(node).onComplete(t -> {
						// if(!words.contains(node)) {
						toAssign.add(node.getValue());
						// words.add(node);
						// }
					});
				}
				if ((j == 0 && links.size() == 1) || j + 1 == links.size() - 1) {
					promise.complete();
				}

			}
		}
		return promise.future();
	}

	private Future<Void> updateView(NodeTuple word) {
		Promise<Void> promise = Promise.promise();
		eb.send("updateView", new DataHolder(word));
		promise.complete();
		return promise.future();
	}

}
