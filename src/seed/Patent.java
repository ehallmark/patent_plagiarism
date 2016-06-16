package seed;

import seed.Database.SimilarityType;

import java.sql.SQLException;
import java.util.List;

public class Patent {
	private String name;
	protected List<Integer> abstractValues;
	protected List<Integer> descriptionValues;

	// Constructor
	public Patent(String inName, String inAbstract, String inDescription) throws SQLException {
		name = inName;
		abstractValues = NLP.createMinHash(inAbstract,SimilarityType.ABSTRACT, Main.LEN_SHINGLES);
		descriptionValues = NLP.createMinHash(inDescription,SimilarityType.DESCRIPTION, Main.LEN_SHINGLES);
		if (abstractValues.isEmpty() && descriptionValues.isEmpty())
			throw new NullPointerException("No Value Length");
	}
	
	public Patent() {
		
	}

	public List<Integer> getAbstractValues() {
		return abstractValues;
	}
	
	public List<Integer> getDescriptionValues() {
		return descriptionValues;
	}
	

	public String getName() {
		return name;
	}

}
