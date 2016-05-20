package seed;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import seed.Database.SimilarityType;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;



public class NLP {
	private static final int MAX_SENTENCE_LEN = 500;
	public static final int SEED = 342689376;
	private static final Random rand = new Random(SEED);
	private static List<HashFunction> hashFunctions = new ArrayList<HashFunction>();
	static {
		for (int i = 0; i <  Math.max(Main.NUM_HASH_FUNCTIONS_DESCRIPTION, Math.max(Main.NUM_HASH_FUNCTIONS_ABSTRACT, Main.NUM_HASH_FUNCTIONS_CLAIM)); i++) {
			hashFunctions.add(new HashFunction(rand.nextInt()));
		};		
	}

	public static Set<Integer> getShingles(String result, SimilarityType type) throws SQLException {
		// option #1: By sentence.
		Set<Integer> shingles = new HashSet<Integer>();
		try{
			DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(result));
			for(List<HasWord> sentence : dp) {
				shingles.addAll(process(sentence.subList(0, Math.min(sentence.size(),MAX_SENTENCE_LEN)),type));
			};
		} catch(NullPointerException npe) {
			
		} 
		return shingles;
	}
	
	public static Set<Integer> process(List<HasWord> words, SimilarityType type) {
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
		
		List<String> toKeep = new ArrayList<String>();
		words.forEach(word->{
			if(word!=null && word.word()!=null) {
				String w = word.word().toLowerCase().replaceAll("[^a-z]", "");
				if(w.length() > 0) {
					toKeep.add(w);
				}
			}
		});

		Set<Integer> toReturn = new HashSet<Integer>();
		String sentence = String.join(" ", toKeep);
		for(int i = 0; i < sentence.length()-shingleLength; i++) {
			toReturn.add(sentence.substring(i, i+shingleLength).hashCode());
		}
		return toReturn;
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
		Set<Integer> shingles = getShingles(result, type);
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
