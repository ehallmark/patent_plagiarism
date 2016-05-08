package seed;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {
	public static final int LEN_SHINGLES = 8;
	public static final int NUM_BANDS = 50;
	public static final int LEN_BANDS = 2;
	public static final int NUM_HASH_FUNCTIONS = NUM_BANDS * LEN_BANDS;
	public static final int SEED = 342689376;
	public static final double SIGNIFICANCE_RATIO = 0.15;
	private ArrayBlockingQueue<String[]> queue;
	private static final Random rand = new Random(SEED);
	private static List<HashFunction> hashFunctions = new Vector<HashFunction>();
	private volatile boolean kill = false;
	public static int FETCH_SIZE = 10;

	public static void setup() {
		for (int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
			hashFunctions.add(new HashFunction(rand.nextInt()));
		}
		;
	}

	Main() throws IOException, SQLException {
		// Get all the patents (chunked N at a time for "efficiency")
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		setup();
		queue = new ArrayBlockingQueue<String[]>(5000);
		Database.setupSeedConn();
		Database.setupMainConn();
		Database.setAutoCommit(false);
		Thread thr = new Thread() {
			@Override
			public void run() {
				String[] res = null;
				try {
					int count = 0;
					final int chunkSize = FETCH_SIZE;
					int current = 0;
					List<Patent> patents = new ArrayList<Patent>();
					while (!kill) {
						if ((res = queue.poll()) == null) {
							Thread.sleep(50);
							continue;
						}
						Patent p;
						try {
							p = new Patent(res[0], res[1], res[2], res[3]);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							continue;
						}

						// Calculate min hash and lsh
						p.setValues(createMinHash(p));
						patents.add(p);
						System.out.print(p.getName() + ' ');
						if (current >= chunkSize) {
							try {
								Database.insertPatent(patents);

							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							patents.clear();
							current = 0;
							System.gc();
							System.out.println();
						} else {
							current++;
						}
						if (count % 2500 == 1) {
							try {
								Database.commit();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							count = 1;
						}

						count++;
						// Thread.yield();
					}
					if(!patents.isEmpty()) {
						try {
							Database.insertPatent(patents);
							Database.commit();

						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		};
		thr.start();
		ResultSet results = Database.selectPatents();
		try {
			while (results.next()) {
				try {
					final String[] r = new String[] { results.getString(1),
							results.getString(2), results.getString(3),
							results.getString(4) };
					while (!queue.offer(r)) {
						// Queue is full
						try {
							System.out.println("Offer rejected");
							// sleep awhile to let other thread compute
							while (queue.size() > 200)
								Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

						}
					}

				} catch (SQLException sql) {
					sql.printStackTrace();
				}
			}
		} finally {
			kill = true;
		}
		try {
			thr.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				Database.updateLastDate();
				Database.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static Vector<Integer> createMinHash(Patent p) {
		Vector<Integer> MinHashVector = new Vector<Integer>();
		Set<Integer> shingles = p.getShingles();
		hashFunctions.forEach(hash -> {
			int min = Integer.MAX_VALUE;
			for (int shingle : shingles) {
				int h = hash.getHashCode(shingle);
				if (h < min)
					min = h;
			}
			;
			// Get the minimum value
				MinHashVector.add(min);
			});
		System.gc();
		return MinHashVector;
	}

	public static void main(String[] args) {
		try {
			if (args.length > 1)
				try {
					FETCH_SIZE = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}
			new Main();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
