package seed;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {
	public static final int LEN_SHINGLES = 8;
	public static final int NUM_BANDS = 100;
	public static final int LEN_BANDS = 2;
	public static final int NUM_HASH_FUNCTIONS = NUM_BANDS * LEN_BANDS;
	public static final double SIGNIFICANCE_RATIO = 0.15;
	private ArrayBlockingQueue<QueueSender> queue;
	private volatile boolean kill = false;
	public static int FETCH_SIZE = 20;
	public static final int SEED_PATENTS = 1;
	public static final int SEED_CLAIMS = 2;

	Main(int seedType) throws IOException, SQLException {
		// Get all the patents (chunked N at a time for "efficiency")
		if(seedType<0||seedType>2) {
			System.out.println("Please specify a seed type!");
			return;
		}
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		queue = new ArrayBlockingQueue<QueueSender>(5000);
		Database.setupSeedConn();
		Database.setupMainConn();
		Thread thr = new Thread() {
			@Override
			public void run() {
				QueueSender res = null;
				try {
					while (!kill) {
						if ((res = queue.poll()) == null) {
							Thread.sleep(50);
							continue;
						}
						if(seedType==Main.SEED_PATENTS)  {
							try {
								Patent p = new Patent(res.arg1, res.arg2, res.arg3);
								Database.insertPatent(p);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								continue;
							}
						} else if(seedType==Main.SEED_CLAIMS) {
							try {
								Claim c = new Claim(res.arg1, res.arg2, Integer.parseInt(res.arg3));
								Database.insertClaim(c);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								continue;
							}
						}

						System.gc();
					}


				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		};
		thr.start();
		ResultSet results;
		if(seedType==Main.SEED_CLAIMS) results = Database.selectClaims();
		else if(seedType==SEED_PATENTS) results = Database.selectPatents();
		else return;
		try {
			while (results.next()) {
				try { 
					final QueueSender r = new QueueSender(results.getString(1),
							results.getString(2), results.getString(3));
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
			while(!queue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			kill = true;
		}
		try {
			thr.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				
				Database.updateLastDate(seedType);
				Database.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	public static void main(String[] args) {
		try {
			if (args.length > 1)
				try {
					FETCH_SIZE = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}
			Patent.setup();
			new Main(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
