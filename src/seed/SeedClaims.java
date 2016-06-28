package seed;
import java.io.IOException;
import java.sql.SQLException;

public class SeedClaims {
	public static void main(String[] args) {
		try {
			if (args.length > 1)
				try {
					Main.FETCH_SIZE = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}
			Main.SEED_CLAIMS_ONLY=true;
			Database.setupSeedConn();
			Database.setupMainConn();
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
