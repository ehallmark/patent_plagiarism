package seed;

import java.sql.SQLException;
import java.util.List;

import seed.Database.SimilarityType;

public class Patent {
	private String name;
	private List<Integer> abstractValues;
	private List<Integer> descriptionValues;

	// Constructor
	public Patent(String inName, String inAbstract, String inDescription) throws SQLException {
		name = inName;
		abstractValues = NLP.createMinHash(inAbstract,SimilarityType.ABSTRACT);
		descriptionValues = NLP.createMinHash(inDescription,SimilarityType.DESCRIPTION);
		if (abstractValues.isEmpty() && descriptionValues.isEmpty())
			throw new NullPointerException();
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
