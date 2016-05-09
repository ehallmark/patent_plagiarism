package seed;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.Set;
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

		List<Technology> technologies = Database.selectTechnologies();
		technologies.forEach(t->{
			try {
				t.setValues(createMinHash(t));
				System.gc();
				Database.insertTechnology(t);
				System.out.println("COMMIT!!!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		Database.close();

	}

	public static Vector<Integer> createMinHash(Technology p) {
		Vector<Integer> MinHashVector = new Vector<Integer>();
		List<Set<Integer>> allShingles = p.getShingles();
		allShingles.forEach(shingles->{
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
