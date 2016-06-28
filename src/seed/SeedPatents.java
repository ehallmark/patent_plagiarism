package seed;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SeedPatents {
	public static void main(String[] args) {
		try {
			if (args.length > 1)
				try {
					Main.FETCH_SIZE = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}
			final int currentDate = Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			Database.setupSeedConn();
			Database.setupMainConn();
			Main.setupLists();
			Database.close();
			while((Patent.lastPubDate==null) || Patent.lastPubDate < currentDate) {
				Database.setupSeedConn();
				Database.setupMainConn();
				new Main(50);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
