package seed;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class Main {
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
	
	public static int FETCH_SIZE = 5;
	public static Set<String> allCachedClaims;
	public static boolean SEED_CLAIMS_ONLY = false;



	public static void setupLists() throws SQLException {
		allCachedClaims=Database.selectPatentNumbers("patent_claim_cache_min_hash");
		System.out.print(allCachedClaims.size()); System.out.println(" lists of claims already ingested...");
	}

	Main(int date) throws IOException, SQLException {
		ForkJoinPool pool = new ForkJoinPool();
		int timeToCommit = 0;
		long timeInit = System.currentTimeMillis();

		ResultSet results = Database.selectPatents(date);
		try {
			while (results.next()) {
				try {
					if(Main.SEED_CLAIMS_ONLY) pool.execute(new Patent(new QueueSender(results.getString(1),results.getInt(2), null, null)));
					else pool.execute(new Patent(new QueueSender(results.getString(1),results.getInt(2), results.getString(3), results.getString(4))));
					timeToCommit++;
					if(timeToCommit > 1000) {
						while(pool.hasQueuedSubmissions()) {
							try {
								TimeUnit.SECONDS.sleep(1);
							}catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						System.out.println("Finished 1000 Patents in: "+new Double(System.currentTimeMillis()-timeInit)/(1000)+ " seconds");
						timeInit = System.currentTimeMillis();
						// Update last date
						Database.updateLastPatentDate();
						Database.safeCommit();
						timeToCommit=0;
						System.gc(); System.gc();
					}

				} catch (SQLException sql) {
					sql.printStackTrace();
				}
			}
		} finally {
			pool.shutdown();
			try {
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
			
			try {
				
				Database.updateLastPatentDate();
				Database.safeCommit();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				Database.close();
			}
		}

	}

}
