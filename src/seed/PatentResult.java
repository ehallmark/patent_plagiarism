package seed;

public class PatentResult {
	private String name;
	private Integer similarity;


	public PatentResult(String name, Integer similarity) {
		this.name = name;
		this.similarity = similarity;
	}

	public String getSimilarity() {
		if(similarity!=null) return "%" + (int) (similarity / (Main.NUM_HASH_FUNCTIONS / 100));
		else return "N/A";
	}
	
	public String getExternalUrl() {
		return "<form style='display:inline;' target='new'><button style='margin-left:5px; margin-left:5px;' title='Show Patent "+name+" in Google' formaction=\"http://www.google.com/patents/US" + name + "\" >Show</button></form>";
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return "<b><a style='margin-left:5px; margin-left:5px;' title='Find Patents Similar to "+name+"' href='find_by_patent?patent=" + name + "' >"+name+"</a></b>";
	}
}
