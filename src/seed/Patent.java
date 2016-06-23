package seed;

import java.util.concurrent.*;

public class Patent   {
	public static Integer lastPubDate;
	QueueSender obj;
	ForkJoinPool pool;
	// Constructor
	public Patent(QueueSender obj, ForkJoinPool pool) {
		Patent.lastPubDate=obj.date;
		this.obj=obj;
		this.pool=pool;
		pool.execute(new PatentAbstract(obj.oAbstract,obj.name));
		pool.execute(new PatentDescription(obj.description,obj.name));
		pool.execute(new PatentClaims(obj.name));
		System.out.println(obj.name);
	}

}
