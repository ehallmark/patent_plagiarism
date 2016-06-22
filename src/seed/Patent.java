package seed;

import seed.Database.SimilarityType;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class Patent {
	public static Integer lastPubDate;

	// Constructor
	public Patent(QueueSender obj)  {
		// Fork process
		RecursiveAction action = new RecursiveAction() {
			public void compute() {
				Integer[] claimCache = new Integer[Main.NUM_HASH_FUNCTIONS_CLAIM];
				for (int i = 0; i < Main.NUM_HASH_FUNCTIONS_CLAIM; i++) {
					claimCache[i] = Integer.MAX_VALUE;
				}
				try {
					ResultSet rs = Database.selectClaims(obj.name);
					if (rs.next()) {
						String[] claims = (String[]) rs.getArray(1).getArray();
						Integer[] numbers = (Integer[]) rs.getArray(2).getArray();
						RecursiveTask<Integer[]> innerAction = new RecursiveTask<Integer[]>() {
							public Integer[] compute() {
								Integer[] claimCache2 = new Integer[Main.NUM_HASH_FUNCTIONS_CLAIM];
								for (int i = 0; i < claims.length/2; i++) {
									try {
										List<Integer> curr = NLP.createMinHash(claims[i], SimilarityType.CLAIM, Main.LEN_SHINGLES);
										if (curr == null) continue;
										Database.updateClaimMinHash(curr, obj.name, numbers[i]);
										for (int j = 0; j < curr.size(); j++) {
											if (claimCache2[j] > curr.get(j)) claimCache2[j] = curr.get(j);
										}
									} catch (Exception e) {
									}

								}
								return claimCache2;
							}
						};
						innerAction.fork();

						for (int i = claims.length/2; i < claims.length; i++) {
							try {
								List<Integer> curr = NLP.createMinHash(claims[i], SimilarityType.CLAIM, Main.LEN_SHINGLES);
								if (curr == null) continue;
								Database.updateClaimMinHash(curr, obj.name, numbers[i]);
								for (int j = 0; j < curr.size(); j++) {
									if (claimCache[j] > curr.get(j)) claimCache[j] = curr.get(j);
								}
							} catch (Exception e) {
							}

						}
						try {
							Integer[] received = innerAction.get();
							if(received!=null) {
								for (int i = 0; i < claimCache.length; i++) {
									claimCache[i] = Math.min(claimCache[i], received[i]);
								}
							}
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
		action.fork();


		System.out.println(obj.name);
		Patent.lastPubDate=obj.date;

		RecursiveAction addAbstract = new RecursiveAction() {
			public void compute() {
				try {
					Database.updateAbstractMinHash(NLP.createMinHash(obj.oAbstract, SimilarityType.ABSTRACT, Main.LEN_SHINGLES), obj.name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		addAbstract.fork();

		try {
			Database.updateDescriptionMinHash(NLP.createMinHash(obj.description,SimilarityType.DESCRIPTION, Main.LEN_SHINGLES), obj.name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		addAbstract.join();

	}
}
