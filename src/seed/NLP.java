package seed;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
	
	public static Set<Integer> createShingles(String result, SimilarityType type) {
		Integer shingleLength;
		switch(type) {
			case ABSTRACT: {
				shingleLength=Main.LEN_SHINGLES_ABSTRACT;
			} break;
			case DESCRIPTION: {
				shingleLength=Main.LEN_SHINGLES_DESCRIPTION;
			} break;
			case CLAIM: {
				shingleLength=Main.LEN_SHINGLES_CLAIM;
			} break;
			default: {
				shingleLength = null;
			} break;
		};
		Set<Integer> shingles = new HashSet<Integer>();
		for(int i = 0; i < result.length()-shingleLength; i++) {
			shingles.add(result.substring(i, i+shingleLength).hashCode());
		}
		return shingles;
	}
	
	public static List<Integer> createMinHash(String result, SimilarityType type) throws SQLException {
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
		
		List<Integer> MinHashVector = new ArrayList<Integer>();
		Set<Integer> shingles = createShingles(result, type);
		hashFunctions.subList(0, numHashFunctions).forEach(hash -> {
			int min = Integer.MAX_VALUE;
			for (int shingle : shingles) {
				int h = hash.getHashCode(shingle);
				if (h < min) min = h;
			};
		// Get the minimum value
			MinHashVector.add(min);
		});
		System.gc();
		return MinHashVector;
	}
}
