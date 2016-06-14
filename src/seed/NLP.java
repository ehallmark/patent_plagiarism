package seed;

import java.sql.SQLException;
import java.util.*;

import seed.Database.SimilarityType;



public class NLP {
	public static final int SEED = 342689376;
	private static final Random rand = new Random(SEED);
	private static List<HashFunction> hashFunctions = new ArrayList<HashFunction>();
	static {
		for (int i = 0; i <  Math.max(Main.NUM_HASH_FUNCTIONS_DESCRIPTION, Math.max(Main.NUM_HASH_FUNCTIONS_ABSTRACT, Main.NUM_HASH_FUNCTIONS_CLAIM)); i++) {
			hashFunctions.add(new HashFunction(rand.nextInt()));
		};		
	}
	
	private static Set<Integer> createShingles(String result, int shingleLength) {
		Set<Integer> shingles = new HashSet();
		for(int i = 0; i < result.length()-shingleLength; i++) {
			shingles.add(result.substring(i, i+shingleLength).hashCode());
		}
		return shingles;
	}
	
	public static List<Integer> createMinHash(String result, SimilarityType type, int shingleLength) throws SQLException {
		Integer numHashFunctions;
		switch(type) {
			case ABSTRACT: {
				numHashFunctions=Main.NUM_HASH_FUNCTIONS_ABSTRACT;
			} break;
			case DESCRIPTION: {
				numHashFunctions=Main.NUM_HASH_FUNCTIONS_DESCRIPTION;
			} break;
			case CLAIM: {
				numHashFunctions=Main.NUM_HASH_FUNCTIONS_CLAIM;
			} break;
			default: {
				numHashFunctions = null;
			} break;
		};
		
		List<Integer> MinHashVector = new LinkedList<Integer>();
		Set<Integer> shingles = createShingles(result, shingleLength);
		hashFunctions.subList(0, numHashFunctions).forEach(hash -> {
			// Get the minimum value
			MinHashVector.add(Collections.min(shingles));
		});
		return MinHashVector;
	}


}
