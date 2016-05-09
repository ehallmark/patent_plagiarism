package seed;

import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringJoiner;
import java.util.Vector;

public class Database {
	private static String inUrl = "jdbc:postgresql://data.gttgrp.com/patentdb?user=readonly&password=&tcpKeepAlive=true";
	private static String outUrl = "jdbc:postgresql://localhost/patentdb?user=postgres&password=&tcpKeepAlive=true";
	private static String compdbUrl = "jdbc:postgresql://data.gttgrp.com/compdb_development?user=postgres&password=&tcpKeepAlive=true";
	private static final String lastIngest = "UPDATE last_min_hash_ingest SET last_uid=? WHERE table_name = 'patent_grant'";
	private final static String selectReelFrames = "select t.name, array_agg(r.reel::text||':'||r.frame::text) as reel_frames from technologies as t inner join deals_technologies as dt on (t.id = dt.technology_id) inner join deals on (dt.deal_id=deals.id) inner join recordings as r on (deals.id=r.deal_id) group by t.name order by t.name";
	private final static String selectTechnologiesByReelFrame = "select array_to_string(array_agg(substring(regexp_replace(lower(coalesce(invention_title,'')), '[^a-z]', '', 'g') || regexp_replace(lower(coalesce(abstract,'')), '[^a-z]', '', 'g') from 0 for 100000)),'') as abstract from patent_assignment_property_document as p inner join patent_grant as p2 on (p.doc_number=p2.pub_doc_number) where assignment_reel_frame = any(?) limit 1";
	private static Connection seedConn;
	private static Connection mainConn;
	private static Connection compdbConn;
	// private static final String orderByDate = " ORDER BY pub_date::int";
	private static final String selectPatents = " SELECT pub_doc_number, regexp_replace(lower(coalesce(invention_title,'')), '[^a-z]', '', 'g') as invention_title, regexp_replace(lower(coalesce(abstract,'')), '[^a-z]', '', 'g') as abstract, regexp_replace(lower(substring(coalesce(description,'') from 0 for least(char_length(coalesce(description,'')),10000))), '[^a-z]', '', 'g') as description, pub_date::int FROM patent_grant WHERE pub_date::int >= ? ";
	private static final String selectAlreadyIngested = " SELECT pub_doc_number from patent_min_hash ";
	private static final String selectLastIngestDate = " SELECT last_uid FROM last_min_hash_ingest WHERE table_name = 'patent_grant' limit 1";

	
	public static void setupCompDBConn() throws SQLException {
		compdbConn = DriverManager.getConnection(compdbUrl);

	}
	
	public static void setupMainConn() throws SQLException {
		mainConn = DriverManager.getConnection(outUrl);
	}

	public static void setupSeedConn() throws SQLException {
		seedConn = DriverManager.getConnection(inUrl);
		seedConn.setAutoCommit(false);
	}

	public static void insertPatent(List<Patent> patents) throws SQLException {
		StringJoiner insertStatement = new StringJoiner(" ");
		StringJoiner columns = new StringJoiner(",", "(", ")");
		StringJoiner valJoiner = new StringJoiner(",", " ", " ");
		insertStatement.add("INSERT INTO patent_min_hash");
		columns.add("pub_doc_number");
		for (int i = 1; i <= Main.NUM_HASH_FUNCTIONS; i++) {
			columns.add("m" + i);
		}
		insertStatement.add(columns.toString());
		insertStatement.add("VALUES");
		// Add patent values as array
		patents.forEach(p -> {
			StringJoiner vals = new StringJoiner(",", "(", ")");
			vals.add("'" + p.getName() + "'");
			p.getValues().forEach(val -> {
				vals.add(val.toString());
			});
			valJoiner.add(vals.toString());
		});
		insertStatement.add(valJoiner.toString());

		PreparedStatement ps = mainConn.prepareStatement(insertStatement.toString());

		ps.executeUpdate();
		ps.close();
	}

	public static ArrayList<PatentResult> similarPatents(String patent,
			int limit, boolean fast) throws SQLException {
		// Get the patent's hash values
		final String selectPatent = "SELECT * FROM patent_min_hash WHERE pub_doc_number = ?";
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
				where.add(and.toString());
			}
		} else {
			return null;
		}

		similarSelect.add(join.toString());
		similarSelect.add("as similarity FROM patent_min_hash WHERE pub_doc_number!=?");
		if(fast)similarSelect.add(where.toString());
		similarSelect.add("ORDER BY similarity DESC LIMIT ?");

		PreparedStatement ps2 = mainConn.prepareStatement(similarSelect
				.toString());
		ps2.setString(1, patent);
		ps2.setInt(2, limit);

		ArrayList<PatentResult> patents = new ArrayList<PatentResult>();
		results = ps2.executeQuery();
		while (results.next()) {
			patents.add(new PatentResult(results.getString(1), results
					.getInt(2)));
		}
		return patents;

	}

	public static ArrayList<PatentResult> similarPatents(
			Vector<Integer> minHashValues, int limit, boolean fast) throws SQLException {
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
		    where.add(and.toString());
		}

		similarSelect.add(join.toString());
		similarSelect.add("as similarity FROM patent_min_hash");
		if(fast)similarSelect.add(where.toString());
		similarSelect.add("ORDER BY similarity DESC LIMIT ?");

		PreparedStatement ps2 = mainConn.prepareStatement(similarSelect
				.toString());
		ps2.setInt(1, limit);

		ArrayList<PatentResult> patents = new ArrayList<PatentResult>();
		ResultSet results = ps2.executeQuery();
		while (results.next()) {
			patents.add(new PatentResult(results.getString(1), results
					.getInt(2)));
		}

		return patents;
	}

	// We need the seed connection
	public static ResultSet selectPatents() throws SQLException, IOException {
		PreparedStatement ps = mainConn.prepareStatement(selectLastIngestDate);
		ResultSet res = ps.executeQuery();
		PreparedStatement ps2 = seedConn.prepareStatement(selectPatents);
		if(res.next()) {
			ps2.setInt(1, res.getInt(1));
		} else {
			ps2.setInt(1, 0);
		}
		ps2.setFetchSize(Main.FETCH_SIZE);
		ResultSet results = ps2.executeQuery();
		return results;
	}

	public static void setAutoCommit(boolean commit) throws SQLException {
		mainConn.setAutoCommit(commit);
	}

	public static void commit() throws SQLException {
		mainConn.commit();
	}
	
	public static void updateLastDate() throws SQLException {
		// update last ingest
		LocalDateTime date = LocalDateTime.now();
		int lastDate = date.getYear()*10000+date.getMonthValue()*100+date.getDayOfMonth();
		PreparedStatement ps = mainConn.prepareStatement(lastIngest);
		ps.setInt(1, lastDate);
		ps.executeUpdate();
		ps.close();
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

	public static void insertTechnology(Technology t) throws SQLException {
		StringJoiner insertStatement = new StringJoiner(" ");
		StringJoiner columns = new StringJoiner(",", "(", ")");
		insertStatement.add("INSERT INTO technology_min_hash");
		columns.add("name");
		for (int i = 1; i <= (Main.NUM_HASH_FUNCTIONS*10); i++) {
			columns.add("m" + i);
		}
		insertStatement.add(columns.toString());
		insertStatement.add("VALUES");
		// Add patent values as array
		StringJoiner vals = new StringJoiner(",", "(", ")");
		vals.add("'" + t.getName() + "'");
		t.getValues().forEach(val -> {
			vals.add(val.toString());
		});
		insertStatement.add(vals.toString());

		PreparedStatement ps = mainConn.prepareStatement(insertStatement.toString());
		System.out.println(ps);
		ps.executeUpdate();
		ps.close();
	}

	public static List<Technology> selectTechnologies() throws SQLException {
		// First we get a list of reel frames for each technology from CompDB
		List<Technology> technologies = new ArrayList<Technology>();
		PreparedStatement pre = compdbConn.prepareStatement(selectReelFrames);
		ResultSet res = pre.executeQuery();
		Hashtable<String,Array> reel_frames = new Hashtable<String,Array>();
		while (res.next()) {
			reel_frames.put(res.getString(1),res.getArray(2));
		}
		pre.close();
		// Then we get the relevant text for each technology
		reel_frames.forEach((k,v)->{
			try{
				PreparedStatement ps = seedConn.prepareStatement(selectTechnologiesByReelFrame);
				ps.setArray(1, v);
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					technologies.add(new Technology(k,rs.getString(1)));
				}
				rs.close();
				ps.close();
			}catch(IOException e) {
				e.printStackTrace();
			} catch(SQLException e) {
				e.printStackTrace();
			}
			System.out.println(k);
		});
		return technologies;
	}



}
