package seed;

public class PatentResult {
	private String name;
	private Integer similarity;

	public PatentResult(String name, int similarity) {
		this.name = name;
		this.similarity = similarity;
	}

	public String getSimilarity() {
		if(similarity!=null) return "%" + (int) (similarity / (Main.NUM_HASH_FUNCTIONS / 100));
		else return "N/A";
	}
	
	public String getExternalUrl() {
		return "<a style='margin-left:5px; margin-left:5px;' target='_blank' href='http://www.google.com/patents/US" + name + "' >link</a>";
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return "<b><a style='margin-left:5px; margin-left:5px;' href='find_by_patent?patent=" + name + "' >"+name+"</a></b>";
	}
}
