package seed;

import java.io.IOException;
import java.sql.SQLException;

public class SeedPatents {
	public static void main(String[] args) {
		try {
			if (args.length > 1)
				try {
					Main.FETCH_SIZE = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}
			Database.setupSeedConn();
			Database.setupMainConn();
			new Main(Main.SEED_PATENTS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try { Database.close(); } catch(Exception e) {e.printStackTrace();}
		}
	}
}
