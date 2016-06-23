package seed;

import seed.Database.SimilarityType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class PatentAbstract extends RecursiveAction  {
	private String oAbstract;
	private String name;

	// Constructor
	public PatentAbstract(String iAbstract, String name) {
		this.name =name;
		this.oAbstract=iAbstract;
	}

	public void compute() {

		try {
			Database.updateAbstractMinHash(NLP.createMinHash(oAbstract, SimilarityType.ABSTRACT, Main.LEN_SHINGLES), name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
