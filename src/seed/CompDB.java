package seed;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class CompDB {

	private static final Random rand = new Random(Main.SEED);
	private static List<HashFunction> hashFunctions = new Vector<HashFunction>();
	public static int FETCH_SIZE = 20;

	public static void setup() {
		for (int i = 0; i < Main.NUM_HASH_FUNCTIONS; i++) {
			hashFunctions.add(new HashFunction(rand.nextInt()));
		}
		;
	}

	CompDB() throws IOException, SQLException {
		// Get all the patents (chunked N at a time for "efficiency")
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		setup();
		Database.setupSeedConn();
		Database.setupCompDBConn();
		Database.setupMainConn();

		Database.ingestTechnologies();

		Database.close();

	}

	public static List<Vector<Integer>> createMinHash(Technology p) {
		List<Vector<Integer>> toReturn = new ArrayList<Vector<Integer>>();
		p.getShingles().forEach(shingles->{
			Vector<Integer> MinHashVector = new Vector<Integer>();
			hashFunctions.forEach(hash -> {
				int min = Integer.MAX_VALUE;
				for (int shingle : shingles) {
					int h = hash.getHashCode(shingle);
					if(h<min) {
						min=h;
					}
				}
				;
				// Get the minimum value
				MinHashVector.add(min);	
			});
			toReturn.add(MinHashVector);
			System.gc();
		});

		return toReturn;
	}

	public static void main(String[] args) {
		try {
			if (args.length > 1)
				try {
					FETCH_SIZE = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}
			new CompDB();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
