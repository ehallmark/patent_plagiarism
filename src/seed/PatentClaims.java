package seed;

import seed.Database.SimilarityType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class PatentClaims extends RecursiveAction  {
	// Constructor
	private String name;
	public PatentClaims(String name) {
		this.name=name;
	}

	public void compute() {


		Integer[] claimCache = new Integer[Main.NUM_HASH_FUNCTIONS_CLAIM];
		for (int i = 0; i < Main.NUM_HASH_FUNCTIONS_CLAIM; i++) {
			claimCache[i] = Integer.MAX_VALUE;
		}
		try {
			ResultSet rs = Database.selectClaims(name);
			if (rs.next()) {
				String[] claims = (String[]) rs.getArray(1).getArray();
				Integer[] numbers = (Integer[]) rs.getArray(2).getArray();
				ExecutorService pool = Executors.newFixedThreadPool(Math.min(5,claims.length));
				for (int i = claims.length; i < claims.length; i++) {
					final int num = i;
					pool.execute(new Thread() {
						public void run() {
							try {
								List<Integer> curr = NLP.createMinHash(claims[num], SimilarityType.CLAIM, Main.LEN_SHINGLES);
								if (curr == null) return;
								Database.updateClaimMinHash(curr, name, numbers[num]);
								for (int j = 0; j < curr.size(); j++) {
									synchronized (claimCache[j]) {
										if (claimCache[j] > curr.get(j)) claimCache[j] = curr.get(j);
									}
								}
							}
							catch(Exception e) {	}
						}

					});

				}
				pool.shutdown();
				try {
					pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
					Database.updateCachedClaimMinHash(claimCache, name);

				} catch(Exception e) {
					e.printStackTrace();
				}


			}

		} catch (SQLException sql) {
			sql.printStackTrace();

		}

	}
}
