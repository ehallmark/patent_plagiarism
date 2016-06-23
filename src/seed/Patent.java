package seed;

import seed.Database.SimilarityType;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

public class Patent extends RecursiveAction  {
	public static Integer lastPubDate;
	QueueSender obj;
	// Constructor
	public Patent(QueueSender obj) {
		Patent.lastPubDate=obj.date;
		this.obj=obj;
	}

	public void compute() {

		// Fork process
		Thread claimsThread = new Thread() {
			public void run() {
				Integer[] claimCache = new Integer[Main.NUM_HASH_FUNCTIONS_CLAIM];
				for (int i = 0; i < Main.NUM_HASH_FUNCTIONS_CLAIM; i++) {
					claimCache[i] = Integer.MAX_VALUE;
				}
				try {
					ResultSet rs = Database.selectClaims(obj.name);
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
										Database.updateClaimMinHash(curr, obj.name, numbers[num]);
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
							Database.updateCachedClaimMinHash(claimCache, obj.name);

						} catch(Exception e) {
							e.printStackTrace();
						}


					}

				} catch (SQLException sql) {
					sql.printStackTrace();

				}
			}
		};


		Thread abstractThread = new Thread() {
			public void run() {
				try {
					Database.updateAbstractMinHash(NLP.createMinHash(obj.oAbstract, SimilarityType.ABSTRACT, Main.LEN_SHINGLES), obj.name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		claimsThread.start();
		abstractThread.start();

		try {
			Database.updateDescriptionMinHash(NLP.createMinHash(obj.description,SimilarityType.DESCRIPTION, Main.LEN_SHINGLES), obj.name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			abstractThread.join();
		} catch (Exception e) {

		}
		try {
			claimsThread.join();
		} catch (Exception e) {

		}
	}
}
