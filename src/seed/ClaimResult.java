package seed;

import seed.Database.SimilarityType;

public class ClaimResult extends PatentResult {
	private Integer claimNumber;
	
	public ClaimResult(String name, Integer similarity, SimilarityType type, Integer claimNumber, String assignee) {
		super(name, similarity, type, assignee);
		this.claimNumber = claimNumber;
	}
	
	@Override
	public String getName() {
		if(assignee!=null) return name+" Claim "+claimNumber+" ("+assignee+") ";
		else return name+" Claim "+claimNumber;
	}
	
	@Override
	public String getExternalUrl() {
		return "<form style='display:inline;' target='_blank'><button style='margin-left:5px; margin-left:5px;' title='Show Patent "+name+" Claim "+claimNumber+" in Google' formaction=\"http://www.google.com/patents/US" + name + "\" >Show</button></form>";
	}
}
