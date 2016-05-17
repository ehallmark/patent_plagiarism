package seed;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class Patent {
	private String name;
	private List<Integer> abstractValues;
	private List<Integer> descriptionValues;
	public static final int SEED = 342689376;
	private static final Random rand = new Random(SEED);
	private static List<HashFunction> hashFunctions = new Vector<HashFunction>();
	
	public static void setup() {
		for (int i = 0; i < Main.NUM_HASH_FUNCTIONS; i++) {
			hashFunctions.add(new HashFunction(rand.nextInt()));
		};		
	}
	

	// Constructor
	public Patent(String inName, String inAbstract, String inDescription) throws SQLException {
		name = inName;
		
		abstractValues = createMinHash(NLP.getShingles(inAbstract));
		descriptionValues = createMinHash(NLP.getShingles(inDescription));
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
	
	public static Vector<Integer> createMinHash(Set<Integer> shingles) {
		Vector<Integer> MinHashVector = new Vector<Integer>();
		hashFunctions.forEach(hash -> {
			int min = Integer.MAX_VALUE;
			for (int shingle : shingles) {
				int h = hash.getHashCode(shingle);
				if (h < min)
					min = h;
			}
			;
			// Get the minimum value
				MinHashVector.add(min);
			});
		System.gc();
		return MinHashVector;
	}

}
