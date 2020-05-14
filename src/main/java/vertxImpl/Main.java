package vertxImpl;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {
	private final static int DEFAULT_N = 6;
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
	    int istances = Runtime.getRuntime().availableProcessors()+1 < DEFAULT_N ? Runtime.getRuntime().availableProcessors()+1 : DEFAULT_N;
	    vertx.eventBus().registerDefaultCodec(DataHolder.class, new DataCodec());
		vertx.deployVerticle(new GuiVerticle(istances),new DeploymentOptions());
	}
}