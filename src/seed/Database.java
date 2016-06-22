package seed;

import org.omg.CORBA.OBJECT_NOT_EXIST;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Database {
	private static String inUrl = "jdbc:postgresql://localhost/patentdb?user=postgres&password=&tcpKeepAlive=true";
	private static String outUrl = "jdbc:postgresql://localhost/patentdb?user=postgres&password=&tcpKeepAlive=true";
	private static final String lastPatentIngest = "UPDATE last_min_hash_ingest SET last_uid=? WHERE table_name = 'patent_grant'";

	private static Connection seedConn;
	private static Connection mainConn;
	private static final String selectLastPatentIngestDate = " SELECT last_uid FROM last_min_hash_ingest WHERE table_name = 'patent_grant' limit 1";
	private static final String selectPatents = "SELECT pub_doc_number, pub_date, words(abstract) as abstract, words(description) as description FROM patent_grant WHERE pub_date > ? ORDER BY pub_date";
	private static final String selectClaims = "SELECT array_agg(words(claim_text)) as claims, array_agg(number) as numbers FROM patent_grant_claim WHERE pub_doc_number = ?";

	
	public static void setupMainConn() throws SQLException {
		mainConn = DriverManager.getConnection(outUrl);
		mainConn.setAutoCommit(false);
	}

	public static void setupSeedConn() throws SQLException {
		seedConn = DriverManager.getConnection(inUrl);
		seedConn.setAutoCommit(false);
	}

	public static void safeCommit() throws SQLException {
		mainConn.commit();
	}
	
	public enum SimilarityType {
		ABSTRACT, DESCRIPTION, CLAIM
	}

	public static ResultSet selectPatents(int limit)throws SQLException {
		PreparedStatement ps = mainConn.prepareStatement(selectLastPatentIngestDate);
		ResultSet res = ps.executeQuery();
		String select = selectPatents;
		if(limit > 0) {
			select += " LIMIT "+limit;
		}
		PreparedStatement ps2 = seedConn.prepareStatement(select);
		if(res.next()) {
			ps2.setInt(1, res.getInt(1));
		} else {
			ps2.setInt(1, 20010000);
		}
		ps2.setFetchSize(Main.FETCH_SIZE);
		System.out.println(ps2);

		ps.close();
		return ps2.executeQuery();
	}
	
	public static String cleanWords(String unClean) throws SQLException {
		PreparedStatement ps = mainConn.prepareStatement("SELECT words(?) AS words");
		ps.setString(1,unClean);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			return rs.getString(1);
		} else {
			return "";
		}
	}


	public static void updateAbstractMinHash(List<Integer> minHash, String name) throws SQLException {
		if(minHash==null)return;
		// Add patent values as array
		StringJoiner columns = new StringJoiner(",", "(", ")");

		columns.add("pub_doc_number");
		for (int i = 1; i <= Main.NUM_HASH_FUNCTIONS_ABSTRACT; i++) {
			columns.add("m" + i);
		}

		StringJoiner insertAbstract = new StringJoiner(" ");

		insertAbstract.add("INSERT INTO patent_abstract_min_hash");
		insertAbstract.add(columns.toString());
		insertAbstract.add("VALUES");
		StringJoiner abstractVals = new StringJoiner(",", "(", ")");
		abstractVals.add("'" + name + "'");
		minHash.forEach(val -> {
			abstractVals.add(val.toString());
		});
		insertAbstract.add(abstractVals.toString());
		PreparedStatement ps = mainConn.prepareStatement(insertAbstract.toString());
		ps.executeUpdate();
		ps.close();
	}

	public static void updateDescriptionMinHash(List<Integer> minHash, String name) throws SQLException {
		if(minHash==null)return;

		StringJoiner columns = new StringJoiner(",", "(", ")");

		columns.add("pub_doc_number");
		for (int i = 1; i <= Main.NUM_HASH_FUNCTIONS_DESCRIPTION; i++) {
			columns.add("m" + i);
		}

		StringJoiner insertDescription = new StringJoiner(" ");
		insertDescription.add("INSERT INTO patent_description_min_hash");
		insertDescription.add(columns.toString());
		insertDescription.add("VALUES");
		StringJoiner descriptionVals = new StringJoiner(",", "(", ")");
		descriptionVals.add("'" + name + "'");
		minHash.forEach(val -> {
			descriptionVals.add(val.toString());
		});
		insertDescription.add(descriptionVals.toString());
		PreparedStatement ps = mainConn.prepareStatement(insertDescription.toString());
		ps.executeUpdate();
		ps.close();

	}

	public static void updateClaimMinHash(List<Integer> minHash, String name, Integer num) throws SQLException {
		if(minHash==null)return;

		StringJoiner insert = new StringJoiner(" ");
		StringJoiner columns = new StringJoiner(",", "(", ")");

		columns.add("pub_doc_number");
		columns.add("claim_number");
		for (int i = 1; i <= Main.NUM_HASH_FUNCTIONS_CLAIM; i++) {
			columns.add("m" + i);
		}
		// update abstract and description table separately

		insert.add("INSERT INTO patent_claim_min_hash");
		insert.add(columns.toString());
		insert.add("VALUES");
		StringJoiner vals = new StringJoiner(",", "(", ")");
		vals.add("'" + name + "'");
		vals.add(num.toString());
		minHash.forEach(val -> {
			vals.add(val.toString());
		});
		insert.add(vals.toString());
		PreparedStatement ps = mainConn.prepareStatement(insert.toString());
		ps.executeUpdate();
	}

	public static void updateCachedClaimMinHash(Integer[] minHash, String name) throws SQLException {
		if(minHash==null)return;

		StringJoiner columns = new StringJoiner(",", "(", ")");
		columns.add("pub_doc_number");
		for (int i = 1; i <= Main.NUM_HASH_FUNCTIONS_CLAIM; i++) {
			columns.add("m" + i);
		}

		StringJoiner insertPatent = new StringJoiner(" ");
		insertPatent.add("INSERT INTO patent_claim_cache_min_hash");
		insertPatent.add(columns.toString());
		insertPatent.add("VALUES");
		StringJoiner patentVals = new StringJoiner(",", "(", ")");
		patentVals.add("'" + name + "'");
		for(int i = 0; i < Main.NUM_HASH_FUNCTIONS_CLAIM; i++) {
			patentVals.add(String.valueOf(minHash[i]));
		}
		insertPatent.add(patentVals.toString());
		PreparedStatement ps = mainConn.prepareStatement(insertPatent.toString());
		ps.executeUpdate();
		ps.close();
	}

	public static ArrayList<PatentResult> similarPatents(String patent,SimilarityType type,
			int limit) throws SQLException {
		String SQLSeedTable;
		String SQLTable;

		boolean isClaim = false;
		Integer numBands;
		Integer bandLength;
		Integer numBandsForLSH;
		switch(type) {
			case ABSTRACT: {
				SQLSeedTable = "patent_abstract_min_hash";
				SQLTable = "patent_abstract_min_hash";

				numBandsForLSH = Main.NUM_BANDS_ABSTRACT;
				bandLength = Main.LEN_BANDS_ABSTRACT;
				numBands = Main.NUM_HASH_FUNCTIONS_ABSTRACT/Main.LEN_BANDS_ABSTRACT;
			} break;
			case DESCRIPTION: {
				SQLSeedTable = "patent_description_min_hash";
				SQLTable = "patent_description_min_hash";
				numBandsForLSH = Main.NUM_BANDS_DESCRIPTION;
				bandLength = Main.LEN_BANDS_DESCRIPTION;
				numBands = Main.NUM_HASH_FUNCTIONS_DESCRIPTION/Main.LEN_BANDS_DESCRIPTION;
			} break;
			case CLAIM: {
				SQLSeedTable = "patent_claim_cache_min_hash";
				SQLTable = "patent_claim_min_hash";
				numBandsForLSH = Main.NUM_BANDS_CLAIM;
				bandLength = Main.LEN_BANDS_CLAIM;
				numBands = Main.NUM_HASH_FUNCTIONS_CLAIM/Main.LEN_BANDS_CLAIM;
				isClaim = true;		
			} break;
			default: {
				return null;
			}
		}
		
		// Get the patent's hash values
		final String selectPatent = "SELECT * FROM "+SQLSeedTable+" WHERE pub_doc_number = ?";
		
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
			for (int i = 0; i < numBands; i++) {
				and = new StringJoiner(" and ","(",")");
				for (int j = 0; j < bandLength; j++) {
					n = i * bandLength + j + 1;
					String inner = "(m" + n + "=" + results.getInt(n) + ")";
					join.add(inner + "::int");
					and.add(inner);
				} 
				// First 15 columns have indices 
			    if(i<numBandsForLSH)where.add(and.toString());
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
				patents.add(new PatentResult(results.getString(1), results.getInt(2), type));
			}
		} else { // Dealing with claims
			while (results.next()) {
				patents.add(new ClaimResult(results.getString(1), results.getInt(2), type, results.getInt(3)));
			}
		}

		return patents;

	}

	public static ArrayList<PatentResult> similarPatents(List<Integer> minHashValues, SimilarityType type, int limit) throws SQLException {
		String SQLTable;
		boolean isClaim = false;
		Integer numBands;
		Integer bandLength;
		Integer numBandsForLSH;
		switch(type) {
			case ABSTRACT: {
				SQLTable = "patent_abstract_min_hash";
				numBandsForLSH = Main.NUM_BANDS_ABSTRACT;
				bandLength = Main.LEN_BANDS_ABSTRACT;
				numBands = Main.NUM_HASH_FUNCTIONS_ABSTRACT/Main.LEN_BANDS_ABSTRACT;
			} break;
			case DESCRIPTION: {
				SQLTable = "patent_description_min_hash";
				numBandsForLSH = Main.NUM_BANDS_DESCRIPTION;
				bandLength = Main.LEN_BANDS_DESCRIPTION;
				numBands = Main.NUM_HASH_FUNCTIONS_DESCRIPTION/Main.LEN_BANDS_DESCRIPTION;
			} break;
			case CLAIM: {
				SQLTable = "patent_claim_min_hash";
				numBandsForLSH = Main.NUM_BANDS_CLAIM;
				bandLength = Main.LEN_BANDS_CLAIM;
				numBands = Main.NUM_HASH_FUNCTIONS_CLAIM/Main.LEN_BANDS_CLAIM;
				isClaim = true;		
			} break;
			default: {
				return null;
			}
		}
		
		// Construct query based on number of bands and length of bands
		StringJoiner similarSelect = new StringJoiner(" ");
		similarSelect.add("SELECT pub_doc_number,");
		StringJoiner join = new StringJoiner("+", "(", ")");
		StringJoiner where = new StringJoiner(" or ","WHERE (",")");
		StringJoiner and;
		int n;
		for (int i = 0; i < numBands; i++) {
			and = new StringJoiner(" and ","(",")");
			for (int j = 0; j < bandLength; j++) {
				n = i * bandLength + j;
				String inner = "(m" + (n + 1) + "=" + minHashValues.get(n)
						+ ")";
				join.add(inner + "::int");
				and.add(inner);
			}
		    if(i<numBandsForLSH)where.add(and.toString());
		}
	
		similarSelect.add(join.toString());
			
		similarSelect.add("as similarity");
		if(isClaim)similarSelect.add(",claim_number");
		similarSelect.add("FROM "+SQLTable)
		.add(where.toString())
		.add("ORDER BY similarity DESC LIMIT ?");

		PreparedStatement ps2 = mainConn.prepareStatement(similarSelect.toString());
		ps2.setInt(1, limit);

		ArrayList<PatentResult> patents = new ArrayList<PatentResult>();
		ResultSet results=null;
		try{
		 results = ps2.executeQuery();
		} catch(Exception e) { e.printStackTrace(); }
		if(!isClaim) {
			while (results.next()) {
				patents.add(new PatentResult(results.getString(1), results.getInt(2), type));
			}
		} else { // Dealing with claims
			while (results.next()) {
				patents.add(new ClaimResult(results.getString(1), results.getInt(2), type, results.getInt(3)));
			}
		}
		return patents;
	}

	
	public static ResultSet selectClaims(String patent) throws SQLException {
		PreparedStatement ps = seedConn.prepareStatement(selectClaims);
		ps.setString(1, patent);

		//ps.setFetchSize(Main.FETCH_SIZE);
		//System.out.println(ps);

		return ps.executeQuery();
	}

	
	public static void updateLastPatentDate() throws SQLException {
		if(Patent.lastPubDate!=null) {
			PreparedStatement ps;
			// get the last UID and then add it to the last ingest table
			ps = mainConn.prepareStatement(lastPatentIngest);
			ps.setInt(1, Patent.lastPubDate);
			ps.executeUpdate();
			ps.close();
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



}
