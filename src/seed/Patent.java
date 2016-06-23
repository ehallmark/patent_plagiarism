package seed;

import java.util.concurrent.*;

public class Patent extends Thread {
	public static Integer lastPubDate;
	// Constructor
	public Patent(QueueSender obj, ForkJoinPool pool){
		Patent.lastPubDate=obj.date;
		// use parallelism if available
		if(!pool.hasQueuedSubmissions() || pool.getQueuedSubmissionCount() < 5) {
			//pool.execute(new PatentClaims(obj.name));
			pool.execute(new PatentAbstract(obj.oAbstract,obj.name));
			pool.execute(new PatentDescription(obj.description,obj.name));

		} else {
			//(new PatentClaims(obj.name)).compute();
			(new PatentAbstract(obj.oAbstract,obj.name)).compute();
			(new PatentDescription(obj.description,obj.name)).compute();
		}

		System.out.println(obj.name);
	}

}
