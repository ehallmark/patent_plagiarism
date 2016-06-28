package seed;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import seed.Database.SimilarityType;


public class Patent extends RecursiveAction {
	private static final long serialVersionUID = 1L;
	public static Integer lastPubDate;
	private QueueSender obj;
	
	// Constructor
	public Patent(QueueSender obj)  {
		// Fork process
		this.obj=obj;
		//Patent.lastPubDate=obj.date;
	}

	public void compute() {
		List<RecursiveAction> tasks = new ArrayList<>();

		if (!Main.SEED_CLAIMS_ONLY) {
			RecursiveAction abstractAction = new RecursiveAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void compute() {
					try {
						Database.updateAbstractMinHash(NLP.createMinHash(obj.oAbstract, SimilarityType.ABSTRACT, Main.LEN_SHINGLES), obj.name);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			abstractAction.fork();
			tasks.add(abstractAction);

			RecursiveAction descriptionAction = new RecursiveAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void compute() {
					try {
						Database.updateDescriptionMinHash(NLP.createMinHash(obj.description, SimilarityType.DESCRIPTION, Main.LEN_SHINGLES), obj.name);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			descriptionAction.fork();
			tasks.add(descriptionAction);

		}

		if(!Main.allCachedClaims.contains(obj.name)) {
			RecursiveAction action = new RecursiveAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void compute() {
					Integer[] claimCache = new Integer[Main.NUM_HASH_FUNCTIONS_CLAIM];
					for (int i = 0; i < Main.NUM_HASH_FUNCTIONS_CLAIM; i++) {
						claimCache[i] = Integer.MAX_VALUE;
					}

					try {
						ResultSet rs = Database.selectClaims(obj.name);
						if (rs.next()) {
							List<List<Integer>> minHashValues = new ArrayList<>();
							List<Integer> numberList = new ArrayList<>();
							String[] claims = (String[]) rs.getArray(1).getArray();
							Integer[] numbers = (Integer[]) rs.getArray(2).getArray();
							List<RecursiveAction> actions = new ArrayList<>();
							for (int i = 0; i < claims.length; i++) {
								final int n = i;
								RecursiveAction task = new RecursiveAction() {
									/**
									 * 
									 */
									private static final long serialVersionUID = 1L;

									@Override
									protected void compute() {
										if (numbers[n] == null || claims[n] == null) return;
										List<Integer> curr = NLP.createMinHash(claims[n], SimilarityType.CLAIM, Main.LEN_SHINGLES);
										if (curr == null) return;
										minHashValues.add(curr);
										numberList.add(numbers[n]);
										for (int j = 0; j < curr.size(); j++) {
											synchronized(claimCache[j]) {if (claimCache[j] > curr.get(j)) claimCache[j] = curr.get(j);}
										}
									}
								};
								task.fork();
								actions.add(task);

							}

							actions.forEach(task->task.join());

							RecursiveAction cachedClaim = new RecursiveAction() {
								/**
								 * 
								 */
								private static final long serialVersionUID = 1L;

								@Override
								protected void compute() {
									try {
										Database.updateCachedClaimMinHash(claimCache, obj.name);
									} catch(Exception e) {
										e.printStackTrace();
									}
								}
							};
							cachedClaim.fork();

							Database.updateClaimMinHashes(minHashValues, numberList, obj.name);

							cachedClaim.join();
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			action.fork();
			tasks.add(action);

		}

		tasks.forEach(task->task.join());
		System.out.println(obj.name);

	}
}
