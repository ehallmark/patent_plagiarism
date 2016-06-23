package seed;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

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
	
	private ArrayBlockingQueue<QueueSender> queue;
	private boolean kill = false;
	public static int FETCH_SIZE = 5;
	//private ForkJoinPool fork = new ForkJoinPool();

	Main() throws IOException, SQLException {
		this(-1);
	}

	Main(int limit) throws IOException, SQLException {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		queue = new ArrayBlockingQueue<QueueSender>(5000);
		Thread thr = new Thread() {
			@Override
			public void run() {
				try{Thread.sleep(1000);}catch(InterruptedException ie) {ie.printStackTrace();}
				QueueSender res = null;
				int timeToCommit = 0;
				long timeInit = System.currentTimeMillis();
				try {
					while (!kill) {
						if ((res = queue.poll()) == null) {
							Thread.sleep(50);
							continue;
						}
						try {
							new Patent(res);
							timeToCommit++;
							if(timeToCommit > 1000) {
								System.out.println("Finished 1000 Patents in: "+new Double(System.currentTimeMillis()-timeInit)/(1000)+ " seconds");
								timeInit = System.currentTimeMillis();
								// Update last date
								Database.updateLastPatentDate();
								Database.safeCommit();
								timeToCommit=0;
								System.gc(); System.gc();
							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							continue;
						}

					}


				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		};
		thr.start();
		ResultSet results = Database.selectPatents(limit);
		try {
			while (results.next()) {
				try {
					while (!queue.offer(new QueueSender(results.getString(1),results.getInt(2), results.getString(3), results.getString(4)))) {
						// Queue is full
						try {
							// sleep awhile to let other thread compute
							while (queue.size() > 250)
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
				
				Database.updateLastPatentDate();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				Database.close();
			}
		}

	}

}
