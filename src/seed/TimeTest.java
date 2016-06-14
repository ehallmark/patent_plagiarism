package seed;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by ehallmark on 6/8/16.
 */
public class TimeTest {
    public static void main(String[] args) {
        long initialTime = System.currentTimeMillis();
        try {
            if (args.length > 1) {
                try {
                    Main.FETCH_SIZE = Integer.parseInt(args[1]);
                } catch (Exception e) { }
            }
            Database.setupSeedConn();
            Database.setupMainConn();
            for(int i = 0; i < 1000000; i++) {
                new Main(Main.SEED_CLAIMS, 1000000);
            }
            new Main(Main.SEED_PATENTS, 80000);
            long finalTime = System.currentTimeMillis();

            long deltaTime = finalTime - initialTime;

            double numDays = ((double)deltaTime)/(1000 * 60 * 60 * 24);
            System.out.println("Test took "+numDays+" days to complete...");
            System.out.println("Would take approximately "+(numDays*100)+" days to complete seed...");

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
