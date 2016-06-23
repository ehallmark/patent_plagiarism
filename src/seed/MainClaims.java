package seed;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class MainClaims {
	public static final int LEN_SHINGLES = 6;
	
	public static final int NUM_BANDS_DESCRIPTION = 20;
	public static final int LEN_BANDS_DESCRIPTION = 4;
	public static final int NUM_HASH_FUNCTIONS_DESCRIPTION = 400;

	public static final int NUM_BANDS_ABSTRACT = 5;
	public static final int LEN_BANDS_ABSTRACT = 2;
	public static final int NUM_HASH_FUNCTIONS_ABSTRACT = 200;

	public static final int NUM_BANDS_CLAIM = 10;
	public static final int LEN_BANDS_CLAIM = 4;
	public static final int NUM_HASH_FUNCTIONS_CLAIM = 100;
	public static int lastClaimUid;
	public static int FETCH_SIZE = 5;
	//private ForkJoinPool fork = new ForkJoinPool();

	MainClaims() throws IOException, SQLException {
		this(-1);
	}

	MainClaims(int limit) throws IOException, SQLException {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		ForkJoinPool pool = new ForkJoinPool();
		int timeToCommit = 0;
		long timeInit = System.currentTimeMillis();
		lastClaimUid = Database.selectLastClaimUid();
		while(Claim.lastUid<lastClaimUid) {
			ResultSet results = Database.selectClaims(limit);
			try {
				while (results.next()) {
					try {
						pool.execute(new Claim(results.getString(1),results.getString(2), results.getInt(3), results.getInt(4)));
						timeToCommit++;
						if(timeToCommit > 10000) {
							while(pool.hasQueuedSubmissions()) {
								try {
									pool.awaitQuiescence(500, TimeUnit.MILLISECONDS);
								} catch (Exception e) {
									
								}
							}
	
							System.out.println("Finished 10000 Claims in: "+new Double(System.currentTimeMillis()-timeInit)/(1000)+ " seconds");
							timeInit = System.currentTimeMillis();
							// Update last date
							Database.updateLastClaimDate();
							Database.safeCommit();
							timeToCommit=0;
							System.gc(); System.gc();
						}
	
					} catch (SQLException sql) {
						sql.printStackTrace();
					}
				}
			} finally {
	
				try {
					
					Database.updateLastClaimDate();
					Database.safeCommit();

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
			
		Database.close();

	}

}
