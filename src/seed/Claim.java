package seed;

import java.sql.SQLException;
import java.util.List;

public class Claim {
	private String patentName;
	private List<Integer> values;
	private Integer claimNum;
	
	public Claim(String patentName, String claimText, Integer claimNum) throws SQLException {
		this.patentName = patentName;
		this.claimNum = claimNum;
		values = Patent.createMinHash(NLP.getShingles(claimText));
		if(claimNum==null || values.isEmpty()) throw new NullPointerException();
	}
	
	public List<Integer> getValues() {
		return values;
	}
	
	public Integer getClaimNum() {
		return claimNum;
	}
	
	public String getPatentName() {
		return patentName;
	}
	
}
