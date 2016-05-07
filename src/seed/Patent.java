package seed;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Patent {
	private String name;
	private Set<Integer> shingles;
	private List<Integer> values;

	// Constructor
	public Patent(String inName, String title, String inAbstract,
			String inDescription) throws Exception {
		name = inName;
		shingles = collectShinglesFrom(inAbstract);
		shingles.addAll(collectShinglesFrom(title));
		shingles.addAll(collectShinglesFrom(inDescription));
		if (shingles.size() < 5)
			throw new Exception();
	}

	public List<Integer> getValues() {
		return values;
	}

	public void setValues(List<Integer> values) {
		this.values = values;
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

	public Set<Integer> getShingles() {
		return shingles;
	}

	public String getName() {
		return name;
	}

}
