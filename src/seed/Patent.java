package seed;

import java.util.concurrent.*;

public class Patent {
	public static Integer lastPubDate;
	// Constructor
	public Patent(QueueSender obj, ForkJoinPool pool){
		Patent.lastPubDate=obj.date;

		// use parallelism if available
		if(!pool.hasQueuedSubmissions()) {
			pool.execute(new PatentClaims(obj.name));
		} else {
			(new PatentClaims(obj.name)).compute();
		}
		if(!pool.hasQueuedSubmissions()) {
			pool.execute(new PatentAbstract(obj.oAbstract,obj.name));
		} else {
			(new PatentAbstract(obj.oAbstract,obj.name)).compute();
		}
		if(!pool.hasQueuedSubmissions()) {
			pool.execute(new PatentDescription(obj.description,obj.name));
		} else {
			(new PatentDescription(obj.description,obj.name)).compute();
		}
		System.out.println(obj.name);
	}

}
