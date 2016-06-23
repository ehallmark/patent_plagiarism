package seed;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import seed.Database.SimilarityType;

public class Patent {
	public static Integer lastPubDate;

	// Constructor
	public Patent(QueueSender obj)  {
		// Fork process
		ForkJoinPool pool = new ForkJoinPool();
		
		pool.execute(new RecursiveAction() {
			public void compute() {
				try {
					Database.updateAbstractMinHash(NLP.createMinHash(obj.oAbstract, SimilarityType.ABSTRACT, Main.LEN_SHINGLES), obj.name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		pool.execute(new RecursiveAction() {
			public void compute() {
				try {
					Database.updateDescriptionMinHash(NLP.createMinHash(obj.description,SimilarityType.DESCRIPTION, Main.LEN_SHINGLES), obj.name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println(obj.name);
		Patent.lastPubDate=obj.date;
	}
}
