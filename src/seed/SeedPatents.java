package seed;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SeedPatents {
	public static void main(String[] args) {
		try {
			if (args.length > 1) {
				try {
					Main.FETCH_SIZE = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}
			}
			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			final int currentDate = Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			Database.setupSeedConn();
			Database.setupMainConn();
			Main.setupLists();
			Patent.lastPubDate=Database.selectLastDate();
			Database.close();
			while(Patent.lastPubDate < currentDate) {
				Database.setupSeedConn();
				Database.setupMainConn();
				new Main(Patent.lastPubDate);
				Patent.lastPubDate++;
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
