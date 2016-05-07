package seed;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {
	public static final int LEN_SHINGLES = 8;
	public static final int NUM_BANDS = 50;
	public static final int LEN_BANDS = 2;
	public static final int NUM_HASH_FUNCTIONS = NUM_BANDS*LEN_BANDS;
	public static final int SEED = 342689376;
	public static final double SIGNIFICANCE_RATIO = 0.15;	
	private ArrayBlockingQueue<String[]> queue;
	private static final Random rand = new Random(SEED);
	private static List<HashFunction> hashFunctions = new Vector<HashFunction>();
	private volatile boolean kill = false;

	
	public static void setup() 	{
		for(int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
			hashFunctions.add(new HashFunction(rand.nextInt()));
		};
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
		Thread thr = new Thread() {
			@Override
			public void run() {
				String[] res = null;
				try {
					int count = 0;
					while(!kill) {
						if((res = queue.poll())==null) {Thread.sleep(1); continue;}
						Patent p;
						try {
							p = new Patent(res[0],res[1],res[2],res[3]);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							continue;
						}
						
						// Calculate min hash and lsh 
						Vector<Integer> MinHashVector = createMinHash(p);
						
						try {
							Database.insertPatent(p, MinHashVector);
							if(count%1000==1)Database.commit();

						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						count++;
						//Thread.yield();
					}
						
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}


		};
		thr.start();
		ResultSet results = Database.selectPatents();
		try {
			while(results.next()) {
				try {
					final String[] r = new String[]{results.getString(1),results.getString(2), results.getString(3),results.getString(4)};
					while(!queue.offer(r)) { 
						// Queue is full
						try {
							System.out.println("Offer rejected");
							// sleep awhile to let other thread compute
							if(queue.size() > 200) Thread.sleep(500);
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
		hashFunctions.forEach(hash->{
			int min = Integer.MAX_VALUE;
			for(int shingle: shingles) {
				int h = hash.getHashCode(shingle);
				if(h<min) min = h;
			};
			// Get the minimum value
			MinHashVector.add(min);
		});
		System.gc();
		return MinHashVector;
	}
	
	
	public static void main(String[] args) {
		try {
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
