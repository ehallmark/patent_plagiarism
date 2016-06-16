package seed;

import seed.Database.SimilarityType;

import java.sql.SQLException;
import java.util.List;

public class Claim {
	public static Integer lastUid;
	private String patentName;
	private List<Integer> values;
	private Integer claimNum;
	
	public Claim(String patentName, String claimText, Integer claimNum, Integer lastUid) throws SQLException {
		this.patentName = patentName;
		this.claimNum = claimNum;
		Claim.lastUid = lastUid;
		values = NLP.createMinHash(claimText,SimilarityType.CLAIM, Main.LEN_SHINGLES);
		if(claimNum==null || values.isEmpty()) throw new NullPointerException("No value length");
	}
	
	// Constructor
	public Claim(String manual, SimilarityType type) throws SQLException {
		values = NLP.createMinHash(manual,type, Main.LEN_SHINGLES);
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
