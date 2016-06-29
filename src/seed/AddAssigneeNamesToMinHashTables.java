package seed;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

public class AddAssigneeNamesToMinHashTables {
	public AddAssigneeNamesToMinHashTables() throws Exception {
		Database.setupMainConn();
		Database.setupSeedConn();
		ResultSet results = Database.selectAssigneeData();
		Set<String> pubDocNumbers = new HashSet<String>();
		int i = 0;
		while(results.next()) {
			String patent = results.getString(1);
			if(!pubDocNumbers.contains(patent)) {
				System.out.println(patent);
				Database.updateAssigneeNames(patent,results.getString(2));
				i= (i+1)%1000;
				if(i==0) {
					Database.safeCommit();
				}
			}
			pubDocNumbers.add(patent);
		}
		Database.safeCommit();
		Database.close();
	}
	
	public static void main(String[] args) {
		try {
			new AddAssigneeNamesToMinHashTables();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
