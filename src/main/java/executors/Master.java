package executors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;

public class Master {
	private SharedContext sharedContext;
	private ForkJoinPool forkJoinPool = new ForkJoinPool();
	
	/**
	 * Creates a master Thread to invoke tasks
	 * 
	 * @param sharedContext
	 */
	public Master(final SharedContext sharedContext) {
		this.sharedContext = sharedContext;
	}
	
	//Method called to running the tasks
	public void execute() {
		long c = System.currentTimeMillis();
		if(sharedContext.isStarted()) {
			try {
				forkJoinPool.invoke(new HttpRequestTask(this.sharedContext, new URL(sharedContext.getInitialUrl())));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		long d = System.currentTimeMillis();
		System.out.println(""+(d-c));
	}
	
}
