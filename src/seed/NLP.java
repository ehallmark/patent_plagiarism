package seed;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	private static Set<Integer> createShingles(String result, int shingleLength) {
		Set<Integer> shingles = new HashSet<Integer>();
		for(int i = 0; i < result.length()-shingleLength; i++) {
			shingles.add(result.substring(i, i+shingleLength).hashCode());
		}
		return shingles;
	}

	// Never returns null
	public static List<Integer> createMinHash(String result, SimilarityType type, int shingleLength)  {
		if(result==null)return null;

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
		Integer[] mins = new Integer[numHashFunctions];
		for(int i = 0; i < numHashFunctions; i++) {
			mins[i] = Integer.MAX_VALUE;
		}

		createShingles(result, shingleLength).forEach(shingle->{
			int i = 0;
			for(HashFunction hash : hashFunctions.subList(0, numHashFunctions)) {
				// Get the minimum value
				if(mins[i] > hash.getHashCode(shingle)) mins[i] = hash.getHashCode(shingle);
				i++;
			}
		});

		return Arrays.asList(mins);
	}


}
