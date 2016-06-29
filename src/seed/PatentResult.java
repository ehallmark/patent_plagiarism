package seed;

import seed.Database.SimilarityType;

public class PatentResult {
	protected String name;
	private Integer similarity;
	private Integer num_functions;
	protected String assignee;


	public PatentResult(String name, Integer similarity, SimilarityType type, String assignee) {
		this.name = name;
		this.similarity = similarity;
		this.assignee = assignee;
		switch(type) {
			case ABSTRACT: {
				num_functions = Main.NUM_HASH_FUNCTIONS_ABSTRACT;
			} break;
			case DESCRIPTION: {
				num_functions = Main.NUM_HASH_FUNCTIONS_DESCRIPTION;
			} break;
			case CLAIM: {
				num_functions = Main.NUM_HASH_FUNCTIONS_CLAIM;
			} break;
			default: {
				num_functions = null;
			} break;
		}
	}
	
	public PatentResult(String name, String assignee) {
		this.name = name;
		this.assignee = assignee;
	}

	public String getSimilarity() {
		if(similarity!=null) return "%" + similarity * 100 / num_functions;
		else return "N/A";
	}
	
	public String getExternalUrl() {
		return "<form style='display:inline;' target='_blank'><button style='margin-left:5px; margin-left:5px;' title='Show Patent "+name+" in Google' formaction=\"http://www.google.com/patents/US" + name + "\" >Show</button></form>";
	}

	public String getName() {
		if(assignee!=null) return name+" ("+assignee+") ";
		else return name;
	}

	public String getUrl() {
		return "<b><a style='margin-left:5px; margin-left:5px;' title='Find Patents Similar to "+name+"' href='find_by_patent?patent=" + name + "' >"+getName()+"</a></b>";
	}
	
	
}
