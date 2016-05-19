package seed;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Database {
	private static String inUrl = "jdbc:postgresql://data.gttgrp.com/patentdb?user=readonly&password=&tcpKeepAlive=true";
	private static String outUrl = "jdbc:postgresql://localhost/patentdb?user=postgres&password=&tcpKeepAlive=true";
	private static final String lastPatentIngest = "UPDATE last_min_hash_ingest SET last_uid=? WHERE table_name = 'patent_grant'";
	private static final String lastClaimIngest = "UPDATE last_min_hash_ingest SET last_uid=? WHERE table_name = 'patent_grant_claim'";

	private static Connection seedConn;
	private static Connection mainConn;

	private static final String selectAlreadyIngested = " SELECT pub_doc_number from patent_abstract_min_hash ";
	private static final String selectLastPatentIngestDate = " SELECT last_uid FROM last_min_hash_ingest WHERE table_name = 'patent_grant' limit 1";
	private static final String selectLastClaimIngestDate = " SELECT last_uid FROM last_min_hash_ingest WHERE table_name = 'patent_grant_claim' limit 1";
	private static final String selectPatents = "SELECT pub_doc_number, regexp_replace(coalesce(abstract,''), '[^a-zA-Z .,:;]', '', 'g') as abstract, substring(regexp_replace(coalesce(description, ''), '[^a-zA-Z .,:;]', '', 'g') from 0 for 100000) as description FROM patent_grant where pub_date::int > ?";
	private static final String selectClaims = "SELECT pub_doc_number, regexp_replace(coalesce(claim_text,''), '[^a-zA-Z .,:;]', '', 'g') as claims, number, uid  FROM patent_grant_claim WHERE uid > ? order by uid limit 1000";

	
	public static void setupMainConn() throws SQLException {
		mainConn = DriverManager.getConnection(outUrl);
	}

	public static void setupSeedConn() throws SQLException {
		seedConn = DriverManager.getConnection(inUrl);
		seedConn.setAutoCommit(false);
	}
	
	public enum SimilarityType {
		ABSTRACT, DESCRIPTION, CLAIM
	};

	public static void insertPatent(Patent p) throws SQLException {
		StringJoiner columns = new StringJoiner(",", "(", ")");
		
		columns.add("pub_doc_number");
		for (int i = 1; i <= Main.NUM_HASH_FUNCTIONS; i++) {
			columns.add("m" + i);
		}
		// update abstract and description table separately
	
		
		// Add patent values as array
		if(p.getAbstractValues()!=null&&!p.getAbstractValues().isEmpty()) {
			StringJoiner insertAbstract = new StringJoiner(" ");

			insertAbstract.add("INSERT INTO patent_abstract_min_hash");
			insertAbstract.add(columns.toString());
			insertAbstract.add("VALUES");
			StringJoiner abstractVals = new StringJoiner(",", "(", ")");
			abstractVals.add("'" + p.getName() + "'");
			p.getAbstractValues().forEach(val -> {
				abstractVals.add(val.toString());
			});
			insertAbstract.add(abstractVals.toString());
			PreparedStatement ps = mainConn.prepareStatement(insertAbstract.toString());
			ps.executeUpdate();
			ps.close();
		}
			
		if(p.getDescriptionValues()!=null&&!p.getDescriptionValues().isEmpty()) {
			StringJoiner insertDescription = new StringJoiner(" ");
			insertDescription.add("INSERT INTO patent_description_min_hash");
			insertDescription.add(columns.toString());
			insertDescription.add("VALUES");
			StringJoiner descriptionVals = new StringJoiner(",", "(", ")");
			descriptionVals.add("'" + p.getName() + "'");
			p.getDescriptionValues().forEach(val -> {
				descriptionVals.add(val.toString());
			});
			insertDescription.add(descriptionVals.toString());
			PreparedStatement ps = mainConn.prepareStatement(insertDescription.toString());
			ps.executeUpdate();
			ps.close();
		}
		
	}
	
	public static void insertClaim(Claim claim) throws SQLException {
		// Add patent values as array
		if(claim.getValues()!=null&&!claim.getValues().isEmpty()&&claim.getClaimNum()!=null) {
			StringJoiner insert = new StringJoiner(" ");
			StringJoiner columns = new StringJoiner(",", "(", ")");
			
			columns.add("pub_doc_number");
			columns.add("claim_number");
			for (int i = 1; i <= Main.NUM_HASH_FUNCTIONS; i++) {
				columns.add("m" + i);
			}
			// update abstract and description table separately

			insert.add("INSERT INTO patent_claim_min_hash");
			insert.add(columns.toString());
			insert.add("VALUES");
			StringJoiner vals = new StringJoiner(",", "(", ")");
			vals.add("'" + claim.getPatentName() + "'");
			vals.add(claim.getClaimNum().toString());
			claim.getValues().forEach(val -> {
				vals.add(val.toString());
			});
			insert.add(vals.toString());
			PreparedStatement ps = mainConn.prepareStatement(insert.toString());
			ps.executeUpdate();
			ps.close();
		}
		
	}


	public static ArrayList<PatentResult> similarPatents(String patent,SimilarityType type,
			int limit) throws SQLException {
		String SQLTable;
		boolean isClaim = false;
		if(type.equals(SimilarityType.ABSTRACT)) {
			SQLTable = "patent_abstract_min_hash";
		} else if(type.equals(SimilarityType.DESCRIPTION)) {
			SQLTable = "patent_description_min_hash";
		} else if(type.equals(SimilarityType.CLAIM)){ 
			isClaim = true;
			SQLTable = "patent_claim_min_hash";
		} else {
			return null;
		}
		
		// Get the patent's hash values
		String selectPatent;
		if(isClaim) {
			StringJoiner select = new StringJoiner(",","SELECT "," FROM "+SQLTable+" WHERE pub_doc_number = ?");
			for (int i = 1; i <= Main.NUM_HASH_FUNCTIONS; i++) {
				select.add("LEAST(m" + i + ")");
			}
			selectPatent = select.toString();
		} else {
			selectPatent = "SELECT * FROM "+SQLTable+" WHERE pub_doc_number = ?";
		}
		PreparedStatement ps = mainConn.prepareStatement(selectPatent);
		ps.setString(1, patent);

		// Construct query based on number of bands and length of bands
		StringJoiner similarSelect = new StringJoiner(" ");
		similarSelect.add("SELECT pub_doc_number,");
		StringJoiner join = new StringJoiner("+", "(", ")");
		StringJoiner where = new StringJoiner(" or ","AND (",")");
		ResultSet results = ps.executeQuery();
		StringJoiner and;
		int n;
		if (results.next()) {
			for (int i = 0; i < Main.NUM_BANDS; i++) {
				and = new StringJoiner(" and ","(",")");
				for (int j = 0; j < Main.LEN_BANDS; j++) {
					n = i * Main.LEN_BANDS + j + 1;
					String inner = "(m" + n + "=" + results.getInt(n) + ")";
					join.add(inner + "::int");
					and.add(inner);
				} 
				// First 10 have indices on the columns
				if(i<10)where.add(and.toString());
			}
		} else {
			return null;
		}

		similarSelect.add(join.toString()).add("as similarity");
		if(isClaim)similarSelect.add(",claim_number");
		similarSelect.add("FROM "+SQLTable+" WHERE pub_doc_number!=?")
		.add(where.toString())
		.add("ORDER BY similarity DESC LIMIT ?");

		PreparedStatement ps2 = mainConn.prepareStatement(similarSelect.toString());
		ps2.setString(1, patent);
		ps2.setInt(2, limit);

		ArrayList<PatentResult> patents = new ArrayList<PatentResult>();
		results = ps2.executeQuery();
		if(!isClaim) {
			while (results.next()) {
				patents.add(new PatentResult(results.getString(1), results.getInt(2)));
			}
		} else { // Dealing with claims
			while (results.next()) {
				patents.add(new ClaimResult(results.getString(1), results.getInt(2), results.getInt(3)));
			}
		}
		return patents;

	}

	public static ArrayList<PatentResult> similarPatents(List<Integer> minHashValues, SimilarityType type, int limit) throws SQLException {
		// Construct query based on number of bands and length of bands
		StringJoiner similarSelect = new StringJoiner(" ");
		similarSelect.add("SELECT pub_doc_number,");
		StringJoiner join = new StringJoiner("+", "(", ")");
		StringJoiner where = new StringJoiner(" or ","WHERE (",")");
		StringJoiner and;
		int n;
		for (int i = 0; i < Main.NUM_BANDS; i++) {
			and = new StringJoiner(" and ","(",")");
			for (int j = 0; j < Main.LEN_BANDS; j++) {
				n = i * Main.LEN_BANDS + j;
				String inner = "(m" + (n + 1) + "=" + minHashValues.get(n)
						+ ")";
				join.add(inner + "::int");
				and.add(inner);
			}
		    if(i<10)where.add(and.toString());
		}

		similarSelect.add(join.toString());
		String SQLTable;
		boolean isClaim = false;
		if(type.equals(SimilarityType.ABSTRACT)) {
			SQLTable = "patent_abstract_min_hash";
		} else if(type.equals(SimilarityType.DESCRIPTION)) {
			SQLTable = "patent_description_min_hash";
		} else if(type.equals(SimilarityType.CLAIM)){ 
			SQLTable = "patent_claim_min_hash";
			isClaim = true;
		} else {
			return null;
		}
			
		similarSelect.add("as similarity FROM "+SQLTable)
		.add(where.toString())
		.add("ORDER BY similarity DESC LIMIT ?");

		PreparedStatement ps2 = mainConn.prepareStatement(similarSelect.toString());
		ps2.setInt(1, limit);

		ArrayList<PatentResult> patents = new ArrayList<PatentResult>();
		ResultSet results = ps2.executeQuery();
		if(!isClaim) {
			while (results.next()) {
				patents.add(new PatentResult(results.getString(1), results.getInt(2)));
			}
		} else { // Dealing with claims
			while (results.next()) {
				patents.add(new ClaimResult(results.getString(1), results.getInt(2), results.getInt(3)));
			}
		}
		return patents;
	}

	// We need the seed connection
	public static ResultSet selectPatents() throws SQLException, IOException {
		PreparedStatement ps = mainConn.prepareStatement(selectLastPatentIngestDate);
		ResultSet res = ps.executeQuery();
		PreparedStatement ps2 = seedConn.prepareStatement(selectPatents);
		if(res.next()) {
			ps2.setInt(1, res.getInt(1));
		} else {
			ps2.setInt(1, 0);
		}
		ps2.setFetchSize(Main.FETCH_SIZE);
		System.out.println(ps2);

		ResultSet results = ps2.executeQuery();
		return results;
	}
	
	public static ResultSet selectClaims() throws SQLException, IOException {
		PreparedStatement ps = mainConn.prepareStatement(selectLastClaimIngestDate);
		ResultSet res = ps.executeQuery();
		PreparedStatement ps2 = seedConn.prepareStatement(selectClaims);
		if(res.next()) {
			ps2.setInt(1, res.getInt(1));
		} else {
			ps2.setInt(1, 0);
		}
		ps2.setFetchSize(Main.FETCH_SIZE);
		System.out.println(ps2);

		ResultSet results = ps2.executeQuery();
		return results;
	}

	public static void setAutoCommit(boolean commit) throws SQLException {
		mainConn.setAutoCommit(commit);
	}

	public static void commit() throws SQLException {
		mainConn.commit();
	}
	
	public static void updateLastPatentDate() throws SQLException {
		PreparedStatement ps;
		// update last date
		LocalDateTime date = LocalDateTime.now();
		int lastDate = date.getYear()*10000+date.getMonthValue()*100+date.getDayOfMonth();
		ps = mainConn.prepareStatement(lastPatentIngest);
		ps.setInt(1, lastDate);
		ps.executeUpdate();
		ps.close();
	}
	
	public static void updateLastClaimDate() throws SQLException {
		// update last ingest
		if(Claim.lastUid!=null) {
			PreparedStatement ps;
			// get the last UID and then add it to the last ingest table
			ps = mainConn.prepareStatement(lastClaimIngest);
			ps.setInt(1, Claim.lastUid);
			ps.executeUpdate();
			ps.close();
		}
		
	}
	
	public static Integer lastIngestableClaimUid() throws SQLException {
		PreparedStatement ps = seedConn.prepareStatement("SELECT uid FROM patent_grant_claim ORDER BY uid DESC LIMIT 1");
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			return rs.getInt(1);
		} else {
			return null;
		}
	}

	public static void close() throws SQLException {
		if (mainConn != null) {
			if (!mainConn.getAutoCommit()) {
				mainConn.commit();
			}
			mainConn.close();
		}
		if (seedConn != null)
			seedConn.close();
	}

	public static List<String> getAllPatentStrings() throws SQLException {
		PreparedStatement pre = mainConn
				.prepareStatement(selectAlreadyIngested);
		List<String> values = new ArrayList<String>();
		ResultSet res = pre.executeQuery();
		while (res.next()) {
			values.add(res.getString(1));
		}
		return values;
	}

	public static Integer claimCount() throws SQLException {
		ResultSet rs = mainConn.prepareStatement("SELECT count(*) FROM patent_claim_min_hash").executeQuery(); 
		if(rs.next()) {
			return rs.getInt(1);
		}
		return null;	}

	public static Integer patentCount() throws SQLException {
		ResultSet rs = mainConn.prepareStatement("SELECT count(*) FROM patent_abstract_min_hash").executeQuery(); 
		if(rs.next()) {
			return rs.getInt(1);
		}
		return null;
	}


}
