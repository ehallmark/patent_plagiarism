package seed;

import java.io.IOException;
import java.sql.SQLException;

public class SeedClaims {
	private static Integer stoppingPoint;
	public static void main(String[] args) {
		try {
			if (args.length > 1) {
				try {
					Main.FETCH_SIZE = Integer.parseInt(args[1]);
				} catch (Exception e) { }
			}
			Patent.setup();
			Database.setupSeedConn();
			Database.setupMainConn();
			stoppingPoint = Database.lastIngestableClaimUid();
			System.out.println("Last UID Avaiable: "+stoppingPoint);
			while(Claim.lastUid==null || stoppingPoint > Claim.lastUid) {
				new Main(Main.SEED_CLAIMS);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try { Database.close(); } catch(Exception e) {e.printStackTrace();}
		}
	}
}