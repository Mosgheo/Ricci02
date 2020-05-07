package executors;

public class Main {

	public static void main(String[] args) {
		SharedContext sharedContext = SharedContext.getIstance();
		new Gui(1024, 768, sharedContext);
		try {
		    synchronized(sharedContext) {
		        while(!sharedContext.isStarted()) {
		            sharedContext.wait();
		        }
		        sharedContext.execute();
		    }
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
