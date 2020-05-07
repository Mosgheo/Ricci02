package executors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class Master {
	private SharedContext sharedContext;
	private ExecutorService executors;
	private ForkJoinPool forkJoinPool;
	
	public Master(final SharedContext sharedContext) {
		this.sharedContext = sharedContext;
	}
	
	public void compute() {
		int i = 0;
		if(sharedContext.isStarted()) {
			while(i++ < sharedContext.getDepth()) {
				executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
				if(i == 1) {
					try {
						String initialContent = sharedContext.getInitialUrl().substring(30);
						sharedContext.addNode(initialContent);
						executors.execute(new LinkAnalysisTask(new URL(sharedContext.getInitialUrl()), this.sharedContext));
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				} else {
					for(String str : sharedContext.getMasterList()) {
						executors.execute(new LinkAnalysisTask(str, this.sharedContext));
					}
				}
				executors.shutdown();
				try {
					executors.awaitTermination(Long.MAX_VALUE,TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.sharedContext.setLabelText(sharedContext.getGraph().getNodeCount());
			System.out.println(" " + sharedContext.getGraph().getNodeCount());
		}
	}
	
	public void execute() {
		long c = System.currentTimeMillis();
		if(sharedContext.isStarted()) {
			try {
				forkJoinPool = new ForkJoinPool();
				forkJoinPool.invoke(new HttpRequestTask(this.sharedContext, new URL(sharedContext.getInitialUrl())));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		long d = System.currentTimeMillis();
		System.out.println(""+(d-c));
		
	}
	
}
