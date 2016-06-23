package seed;

import java.util.concurrent.*;

public class Patent extends Thread {
	public static Integer lastPubDate;
	// Constructor
	QueueSender obj;
	ForkJoinPool pool;
	public Patent(QueueSender obj, ForkJoinPool pool){
		Patent.lastPubDate=obj.date;
		this.obj=obj;
		this.pool=pool;
		start();
	}

	public void run() {
		// use parallelism if available
		if(!pool.hasQueuedSubmissions() || pool.getQueuedSubmissionCount() < 10) {
			pool.execute(new PatentClaims(obj.name));
			pool.execute(new PatentAbstract(obj.oAbstract,obj.name));
			pool.execute(new PatentDescription(obj.description,obj.name));

		} else {
			(new PatentClaims(obj.name)).compute();
			(new PatentAbstract(obj.oAbstract,obj.name)).compute();
			(new PatentDescription(obj.description,obj.name)).compute();
		}

		System.out.println(obj.name);
	}

}
