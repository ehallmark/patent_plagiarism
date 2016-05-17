package seed;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;



public class NLP {
	private static final int MAX_SENTENCE_LEN = 500;

	public static Set<Integer> getShingles(String result) throws SQLException {
		// option #1: By sentence.
		Set<Integer> shingles = new HashSet<Integer>();
		try{
			DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(result));
			for(List<HasWord> sentence : dp) {
				shingles.addAll(process(sentence.subList(0, Math.min(sentence.size(),MAX_SENTENCE_LEN))));
			};
		} catch(NullPointerException npe) {
			
		} 
		return shingles;
	}
	
	public static Set<Integer> process(List<HasWord> words) {
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
		for(int i = 0; i < sentence.length()-Main.LEN_SHINGLES; i++) {
			toReturn.add(sentence.substring(i, i+Main.LEN_SHINGLES).hashCode());
		}
		return toReturn;
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
	    Map<K, V> result = new LinkedHashMap<>();
	    Stream<Map.Entry<K, V>> st = map.entrySet().stream();
	    st.sorted( Map.Entry.comparingByValue() )
	        .forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );
	    return result;
	}
	
}
