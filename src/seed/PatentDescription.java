package seed;

import seed.Database.SimilarityType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class PatentDescription extends RecursiveAction  {
	private String description;
	private String name;

	// Constructor
	public PatentDescription(String description, String name) {
		this.description=description;
		this.name=name;
	}

	public void compute() {

		try {
			Database.updateDescriptionMinHash(NLP.createMinHash(description,SimilarityType.DESCRIPTION, Main.LEN_SHINGLES), name);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
