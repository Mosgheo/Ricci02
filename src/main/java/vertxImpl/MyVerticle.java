package vertxImpl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

public class MyVerticle extends AbstractVerticle {
	private static final String WIKI = "hhttps://it.wikipedia.org/w/api.php";
	private static final int STATUS_OK = 200;
	private static final int FIRST = 0;
	private WebClient client;
	private EventBus eb;
	private int depth;
	private int maxDepth;
	private List<String> assigned;
	private LinkedList<String> toAssign;
	private JsonArray links;
	private int instancies;
	private LocalMap<String, String> sharedData;
	private int waitTime;

	public MyVerticle(int instancies, int depth, int maxDepth, final List<String> voices) {
		this.depth = depth;
		this.maxDepth = maxDepth;
		this.assigned = voices;
		this.instancies = instancies;
	}

	@Override
	public void start(Future<Void> startFuture) {
		try {
			super.start(startFuture);
		} catch (Exception e) {
		}
		waitTime = 50;
		this.toAssign = new LinkedList<String>();
		eb = vertx.eventBus();
		final SharedData sd = vertx.sharedData();
		sharedData = sd.getLocalMap("emptyLinks");

		/**
		 * The first verticle updates the view with the FIRST node, the one passed as an
		 * argument by user.
		 */
		if (depth == FIRST) {
			NodeTuple tmp = new NodeTuple("FIRST", assigned.get(FIRST), depth);
			eb.send("updateView", new DataHolder(tmp));
		}

		/**
		 * Creates a vertx webClient that'll handle http requests.
		 */
		client = WebClient.create(vertx, new WebClientOptions().setSsl(true).setTrustAll(true).setDefaultPort(443)
				.setKeepAlive(true).setDefaultHost("www.wikipedia.org"));

		/**
		 * Computing is executed in two phases: 1- Gets links from wikipedia for every
		 * link assigned to this verticle and sends updates to GUIVerticle 2- creates a
		 * variable instancies of verticles that'll iterate these two phases.
		 */
		startFuture = compute(assigned).onComplete(t -> {
			depth++;
			if (depth == maxDepth) {
				vertx.undeploy(this.deploymentID());
				eb.send("stop", "");
			} else {
				int tmp = toAssign.size() / instancies;
				int oldTmp = 0;
				List<String> verticleWords;
				if (tmp > 0) {
					for (int i = 0; i < instancies; i++) {
						/*
						 * In case the number of words isn't perfectly splittable between instancies,
						 * the last instance gets the remaining words to be searched -> toAssing.Size()
						 * % instancies.
						 */
						if (i == instancies - 1) {
							verticleWords = toAssign.subList(oldTmp, toAssign.size());
							vertx.deployVerticle(new MyVerticle(instancies, depth, maxDepth, verticleWords));
						} else {
							verticleWords = toAssign.subList(oldTmp, oldTmp += tmp);
							vertx.deployVerticle(new MyVerticle(instancies, depth, maxDepth, verticleWords));
						}
					}
				} else {
					/*
					 * if the number of words to be searched is less than the number of instancies,
					 * a single verticle'll be deployed and every word'll be assigned to it.
					 */
					vertx.deployVerticle(new MyVerticle(instancies, depth, maxDepth, toAssign));
				}
			}
		});
	}

	@Override
	public void stop(final Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
		// System.out.println("MyVerticle at dept " + depth + " stopped!");
	}

	/**
	 * compute is a recursive method, for every word assigned to this verticle it
	 * makes a http request and invokes itself upon the next words. Once every word
	 * has been "computed", the promise'll complete.
	 * 
	 * @param toBeSearched list of words to be searched by this verticle
	 * @return a Future that'll be completed when an http request has been made for
	 *         every word in toBeSearched.
	 */
	private Future<Void> compute(final List<String> toBeSearched) {
		LinkedList<String> c = new LinkedList<String>();
		c.addAll(toBeSearched);
		Promise<Void> promise = Promise.promise();
		links = new JsonArray();
		String father;
		if (c.size() > 0) {
					father = c.get(FIRST);
					if (!sharedData.containsKey(father)) {
								client.get(WIKI).addQueryParam("action", "parse")
										.addQueryParam("page", father.replaceAll("\\s", "_")).addQueryParam("format", "json")
										.addQueryParam("section", "0").addQueryParam("prop", "links").as(BodyCodec.jsonObject())
										.send(ar -> {
											if (ar.succeeded() && ar.result().statusCode() == STATUS_OK) {
												sendWords(ar.result(), father).onComplete(t -> {
													c.remove(FIRST);
													compute(c).onComplete(t2 -> {
														promise.complete();
													});
												});
											} else {
												waitTime+=50;
												System.out.println(ar.cause() + father);
												eb.send("error", "");
												//c.remove(FIRST);
												vertx.setTimer(waitTime, t ->{
													compute(c).onComplete(t2 -> {
														eb.send("error-", "");
														promise.complete();
													});
												});

											}
										});
					}else {
						promise.complete();
					}
		} else {
			promise.complete();
		}
		return promise.future();
	}

	/**
	 * Used to update view with new words, every time a http request has completed,
	 * the result is passed to this function that'll send every word contained in
	 * the result to the GuiVerticle through the event bus.
	 * 
	 * @param response the response of the just completed http request
	 * @param father   the father of the list of words contained in response.
	 * @return A future that'll be completed when every word has been sent trough
	 *         the event bus
	 */
	private Future<Void> sendWords(final HttpResponse<JsonObject> response, final String father) {
		Promise<Void> promise = Promise.promise();
		JsonObject body = response.body();
		waitTime=50;
		if (body.getJsonObject("parse") == null || body.getJsonObject("parse").getJsonArray("links").size() == 0) {
			System.out.println("LINK EMPTY, FATHER:" + father);
			sharedData.put(father,"");
			promise.complete();
		} else {
			links = body.getJsonObject("parse").getJsonArray("links");
			for (int i = 0; i < links.size(); i++) {
				if (links.getJsonObject(i).getInteger("ns") == 0) {
					NodeTuple node = new NodeTuple(father, links.getJsonObject(i).getString("*"), depth);
					updateView(node).onComplete(t -> {
							toAssign.add(node.getValue());
					});
				}
				if (i == links.size() - 1) {
					promise.complete();
				}

			}
		}
		return promise.future();
	}

	/**
	 * Sends a single word to the GuiVerticle through eventbus.
	 * 
	 * @param word the word to be sent
	 * @return a future that'll be completed when the word has been sent.
	 */
	private Future<Void> updateView(final NodeTuple word) {
		Promise<Void> promise = Promise.promise();
		eb.send("updateView", new DataHolder(word));
		promise.complete();
		return promise.future();
	}

}
