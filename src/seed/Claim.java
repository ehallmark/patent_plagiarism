package seed;

import java.sql.SQLException;
import java.util.concurrent.RecursiveAction;

import seed.Database.SimilarityType;

public class Claim extends RecursiveAction {
	private static final long serialVersionUID = 1L;
	public static Integer lastUid =1;
	private String patentName;
	private Integer claimNum;
	private String claimText;
	
	public Claim(String patentName, String claimText, Integer claimNum, Integer lastUid) throws SQLException {
		this.patentName = patentName;
		this.claimNum = claimNum;
		this.claimText = claimText;
		Claim.lastUid = lastUid;
		System.out.println(lastUid);

	}
	

	@Override
	protected void compute() {
		try {
			Database.updateClaimMinHash(NLP.createMinHash(claimText,SimilarityType.CLAIM, Main.LEN_SHINGLES), patentName, claimNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
