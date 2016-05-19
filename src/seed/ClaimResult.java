package seed;

public class ClaimResult extends PatentResult {
	private Integer claimNumber;
	
	public ClaimResult(String name, Integer similarity, Integer claimNumber) {
		super(name, similarity);
		this.claimNumber = claimNumber;
	}
	
	@Override
	public String getName() {
		return name+" Claim "+claimNumber;
	}

	@Override
	public String getUrl() {
		return super.getUrl()+" Claim "+claimNumber;
	}
	
	@Override
	public String getExternalUrl() {
		return "<form style='display:inline;' target='_blank'><button style='margin-left:5px; margin-left:5px;' title='Show Patent "+name+" Claim "+claimNumber+" in Google' formaction=\"http://www.google.com/patents/US" + name + "#CLM-"+String.format("%05d", claimNumber)+"\" >Show</button></form>";
	}
}
