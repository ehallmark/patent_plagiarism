package seed;

public class PatentResult {
	private String name;
	private double similarity;
	public PatentResult(String name, double similarity) {
		this.name = name;
		this.similarity = similarity;
	}
	
	public String getSimilarity() {
		return "%"+(int)(similarity/(Main.NUM_HASH_FUNCTIONS/100));
	}
	
	public String getName() {
		return name;
	}
	
	public String getUrl() {
		return "<a href='http://www.google.com/patents/US"+name+"' >"+name+"</a>";
	}
}
