package seed;

import java.sql.SQLException;
import java.util.List;

import seed.Database.SimilarityType;

public class Claim extends Patent {
	public static Integer lastUid;
	private String patentName;
	private List<Integer> values;
	private Integer claimNum;
	
	public Claim(String patentName, String claimText, Integer claimNum, Integer lastUid) throws SQLException {
		super();
		this.patentName = patentName;
		this.claimNum = claimNum;
		Claim.lastUid = lastUid;
		values = NLP.createMinHash(claimText,SimilarityType.CLAIM, Main.LEN_SHINGLES);
		if(claimNum==null || values.isEmpty()) throw new NullPointerException("No value length");
	}
	
	// Constructor
	public Claim(String manual, SimilarityType type) throws SQLException {
		switch(type) {
			case ABSTRACT: {
				abstractValues = NLP.createMinHash(manual,SimilarityType.ABSTRACT, Main.LEN_SHINGLES);
			} break;
			case DESCRIPTION: {
				descriptionValues = NLP.createMinHash(manual,SimilarityType.DESCRIPTION, Main.LEN_SHINGLES);
			} break;
			case CLAIM: {
				values = NLP.createMinHash(manual,SimilarityType.CLAIM, Main.LEN_SHINGLES);
			} break;
			default: {
				throw new NullPointerException("No Type Given");
			}
		}
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
