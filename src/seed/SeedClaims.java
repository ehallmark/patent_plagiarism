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
			Database.setupSeedConn();
			Database.setupMainConn();
			stoppingPoint = Database.lastIngestableClaimUid();
			System.out.println("Last UID Avaiable: "+stoppingPoint);
			Database.close();
			while(Claim.lastUid==null || stoppingPoint > Claim.lastUid) {
				Database.setupSeedConn();
				Database.setupMainConn();
				new Main(Main.SEED_CLAIMS);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
