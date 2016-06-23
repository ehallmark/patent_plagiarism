package seed;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import seed.Database.SimilarityType;


public class Patent extends RecursiveAction {
	public static Integer lastPubDate;
	private QueueSender obj;
	
	// Constructor
	public Patent(QueueSender obj)  {
		// Fork process
		this.obj=obj;
		System.out.println(obj.name);
		Patent.lastPubDate=obj.date;
	}

	public void compute() {
		try {
			Database.updateAbstractMinHash(NLP.createMinHash(obj.oAbstract, SimilarityType.ABSTRACT, Main.LEN_SHINGLES), obj.name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Database.updateDescriptionMinHash(NLP.createMinHash(obj.description,SimilarityType.DESCRIPTION, Main.LEN_SHINGLES), obj.name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
