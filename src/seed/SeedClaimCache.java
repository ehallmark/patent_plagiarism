package seed;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SeedClaimCache {
	public static void main(String[] args) {
		try {
			if (args.length > 1) {
				try {
					Main.FETCH_SIZE = Integer.parseInt(args[1]);
				} catch (Exception e) { }
			}
			Database.setupCacheConn();;
			Database.setupMainConn();
			start();
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
	
	public static void start() throws IOException, SQLException {
		ResultSet results = Database.claimMinHash();
		while(results.next()) {
			System.out.println(results.getString(1));
			try{
				Database.insertCachedClaim(results);
			} catch(SQLException sql) {
				sql.printStackTrace();
			}
		}
	}
}