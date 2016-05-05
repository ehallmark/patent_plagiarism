package seed;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.Vector;

public class Database {
	private static String inUrl = "jdbc:postgresql://data.gttgrp.com/patentdb?user=readonly&password=";
	private static String outUrl = "jdbc:postgresql://localhost/patentdb?user=postgres&password=";
	private static Connection seedConn;
	private static Connection mainConn;

	private static final String selectPatents = " SELECT pub_doc_number, lower(invention_title) as invention_title, lower(abstract) as abstract, lower(description) as description FROM patent_grant WHERE pub_date::date > '1996-01-01'::date and abstract IS NOT NULL and description IS NOT NULL and invention_title IS NOT NULL ";
	private static final String selectPatentsWhere = selectPatents+" and pub_doc_number != any(?)";
	private static final String selectAlreadyIngested = " SELECT pub_doc_number from patent_min_hash ";

	public static void setupMainConn() throws SQLException {
		mainConn = DriverManager.getConnection(outUrl);
	}
	
	public static void setupSeedConn() throws SQLException {
		seedConn = DriverManager.getConnection(inUrl);
		seedConn.setAutoCommit(false);
	}

	
	public static void insertPatent(Patent p, Vector<Integer> values) throws SQLException {
		StringJoiner insertStatement = new StringJoiner(" ");
		StringJoiner columns = new StringJoiner(",","(",")");
		StringJoiner vals = new StringJoiner(",","(",")");
		insertStatement.add("INSERT INTO patent_min_hash");
		columns.add("pub_doc_number");
		for(int i = 1; i <= Main.NUM_HASH_FUNCTIONS; i++) {
			columns.add("m"+i);
		}
		vals.add("'"+p.getName()+"'");
		insertStatement.add(columns.toString());
		insertStatement.add("VALUES");
		values.forEach(val->{
			vals.add(val.toString());
		});
		insertStatement.add(vals.toString());

		PreparedStatement ps = mainConn.prepareStatement(insertStatement.toString());
		
		System.out.println(ps.toString());
		ps.executeUpdate();

	}
	
	public static ArrayList<PatentResult> similarPatents(String patent) throws SQLException {
		// Get the patent's hash values
		final String selectPatent = "SELECT * FROM patent_min_hash WHERE pub_doc_number = ?";
		PreparedStatement ps = mainConn.prepareStatement(selectPatent);
		ps.setString(1, patent);
		
		// Construct query based on number of bands and length of bands
		StringJoiner similarSelect = new StringJoiner(" ");
		similarSelect.add("SELECT pub_doc_number,"); 
		StringJoiner join = new StringJoiner("+","(",")");

		ResultSet results = ps.executeQuery();
		if(results.next()) {
			for(int i = 1; i <= Main.NUM_HASH_FUNCTIONS; i++) {
				join.add("((m"+i+"="+results.getInt(i)+")::int)");
			}
		} else {
			return null;
		}
		
		similarSelect.add(join.toString());
		similarSelect.add("as similarity FROM patent_min_hash WHERE pub_doc_number!=? ORDER BY similarity DESC LIMIT 100");

		PreparedStatement ps2 = mainConn.prepareStatement(similarSelect.toString());
		ps2.setString(1, patent);
		
		System.out.println(ps2.toString());
		
		ArrayList<PatentResult> patents = new ArrayList<PatentResult>();
		results = ps2.executeQuery();
		while(results.next()) {
			patents.add(new PatentResult(results.getString(1),results.getInt(2)));
		} 
		return patents;

	}
	
	public static ArrayList<PatentResult> similarPatents(Vector<Integer> minHashValues) throws SQLException {
		// Construct query based on number of bands and length of bands
		StringJoiner similarSelect = new StringJoiner(" ");
		similarSelect.add("SELECT pub_doc_number,"); 
		StringJoiner join = new StringJoiner("+","(",")");

		for(int i = 1; i <= Main.NUM_HASH_FUNCTIONS; i++) {
			join.add("((m"+i+"="+minHashValues.get(i-1)+")::int)");
		}

		similarSelect.add(join.toString());
		similarSelect.add("as similarity FROM patent_min_hash ORDER BY similarity DESC LIMIT 100");

		PreparedStatement ps2 = mainConn.prepareStatement(similarSelect.toString());
		
		System.out.println(ps2.toString());
		
		ArrayList<PatentResult> patents = new ArrayList<PatentResult>();
		ResultSet results = ps2.executeQuery();
		while(results.next()) {
			patents.add(new PatentResult(results.getString(1),results.getInt(2)));
		} 
		return patents;
	}
	
	// We need the seed connection
	public static ResultSet selectPatents(boolean update) throws SQLException, IOException {
		PreparedStatement ps = null;
		if(!update) {
			List<String> values = getAllPatentStrings();
			if(values.size()>0) {
				ps = seedConn.prepareStatement(selectPatentsWhere);
				ps.setArray(1, seedConn.createArrayOf("text",values.toArray()));
			} 

		} 
		// Default
		if(ps==null)ps = seedConn.prepareStatement(selectPatents);
		
		ps.setFetchSize(50);
		System.out.println(ps.toString());
		ResultSet results = ps.executeQuery();
		return results;
	}
	
	
	public static void close() throws SQLException {
		if(mainConn!=null)mainConn.close();
		if(seedConn!=null)seedConn.close();
	}

	public static List<String> getAllPatentStrings() throws SQLException {
		PreparedStatement pre = mainConn.prepareStatement(selectAlreadyIngested);
		List<String> values = new ArrayList<String>();
		ResultSet res = pre.executeQuery();
		while(res.next()) {
			values.add(res.getString(1));
		}
		return values;
	}
	
}
