package seed;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Technology {
	private String name;
	private List<Set<Integer>> shingles;
	private List<Vector<Integer>> values;

	// Constructor
	public Technology(String inName, Array abstracts) throws IOException, SQLException {
		this.name = inName;
		shingles = new ArrayList<Set<Integer>>();
		String[] texts = (String[])abstracts.getArray();
		System.out.println("Texts: "+texts.length);
		for(int i = 0; i < Math.min(100, texts.length); i++) {
			shingles.add(collectShinglesFrom(texts[i]));	
		}
	}

	private Set<Integer> collectShinglesFrom(String inText) throws IOException {
		Set<Integer> s = new HashSet<Integer>();
		StringReader ss = new StringReader(inText);
		int c;
		while ((c = ss.read()) != -1) {
			ss.mark(Main.LEN_SHINGLES);
			char[] hashVal = new char[Main.LEN_SHINGLES];
			hashVal[0] = (char) c;
			for (int i = 1; i < Main.LEN_SHINGLES; i++) {
				if ((c = ss.read()) != -1) {
					hashVal[i] = (char) c;
				} else {
					break;
				}
			}
			if (c == -1)
				break;
			s.add(new String(hashVal).hashCode()); // Convert to hash to save
													// space
			ss.reset();
		}

		ss.close();
		return s;
	}

	public List<Set<Integer>> getShingles() {
		return shingles;
	}

	public String getName() {
		return name;
	}

	public void setValues(List<Vector<Integer>> minHash) {
		values = minHash;
	}
	
	public List<Vector<Integer>> getValues() {
		return values;
	}

}
