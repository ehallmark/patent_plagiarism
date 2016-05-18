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

}
