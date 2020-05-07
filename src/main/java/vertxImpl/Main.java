package vertxImpl;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
	    int istances = Runtime.getRuntime().availableProcessors()+2;
		DeploymentOptions options = new DeploymentOptions().setWorker(true);
	    vertx.eventBus().registerDefaultCodec(DataHolder.class, new DataCodec());
		vertx.deployVerticle(new GuiVerticle(istances),options);
	}

}