package seed;

import seed.Database.SimilarityType;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
public class Patent {
	public static Integer lastPubDate;

	// Constructor
	public Patent(QueueSender obj)  {
		System.out.println(obj.name);
		Patent.lastPubDate=obj.date;

		try {
			Database.updateAbstractMinHash(NLP.createMinHash(obj.oAbstract, SimilarityType.ABSTRACT, Main.LEN_SHINGLES), obj.name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Database.updateDescriptionMinHash(NLP.createMinHash(obj.description,SimilarityType.DESCRIPTION, Main.LEN_SHINGLES), obj.name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Integer[] claimCache = new Integer[Main.NUM_HASH_FUNCTIONS_CLAIM];
		for(int i = 0; i < Main.NUM_HASH_FUNCTIONS_CLAIM; i++) {
			claimCache[i] = Integer.MAX_VALUE;
		}
		try {
			ResultSet rs = Database.selectClaims(obj.name);
			if(rs.next()) {
				String[] claims = (String[])rs.getArray(1).getArray();
				Integer[] numbers = (Integer[])rs.getArray(2).getArray();
				for(int i = 0; i < claims.length; i++) {
					List<Integer> curr = NLP.createMinHash(claims[i], SimilarityType.CLAIM, Main.LEN_SHINGLES);
					try {
						for(int j = 0; j<curr.size(); j++) {
							if(claimCache[j] > curr.get(j)) claimCache[j] = curr.get(j);
						}
					} catch (Exception e) {
					}
					Database.updateClaimMinHash(curr, obj.name, numbers[i]);

				}

				// update claim cache
				try {
					Database.updateCachedClaimMinHash(claimCache, obj.name);
				} catch(Exception e) {
					e.printStackTrace();
				}

			}

		} catch (SQLException sql) {
			sql.printStackTrace();

		}

	}
}
