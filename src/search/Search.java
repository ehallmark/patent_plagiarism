package search;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.Vector;

import seed.Database;
import seed.Main;
import seed.Patent;
import seed.PatentResult;

public class Search {
	public static HashSet<String> patents;
	
	private static StringJoiner freshTemplate() {
		return new StringJoiner("","<div style='width:100%; padding: 2% 10%;'><h2>Similar Patent Finder</h2>","</div>");
	}
	
	public static void server() {
		get("/", (req, res) -> {
			res.type("text/html");
			StringJoiner template = freshTemplate();
			String html =  "<h3>By Patent</h3>"
				+ "<form action='/find_by_patent' method='post'>"
					+ "<input id='patent' name='patent' />"
					+ "<button>Find Similar Patents</button>"
				+ "</form><br/>"
				+ "<h3>By Raw Text</h3>"
				+ "<form action='/find_by_text' method='post'>"
					+ "<button>Find Similar Patents</button><br/>"
					+ "<textarea rows='10' cols='50' id='text' name='text' ></textarea>"
				+ "</form>";
			template.add(html);
			return template.toString();
		});
		
		post("/find_by_patent", (req, res) -> {
			StringJoiner template = freshTemplate();
			ArrayList<PatentResult> results = null;
			String patent = req.queryParams("patent");
			if(patent==null) return "Please provide a patent number!";
			else {patent=patent.trim();}
			if(!patents.contains(patent)) return "Patent not found!";
			try {
				synchronized(Database.class) {
					results = Database.similarPatents(patent);
				}
				if(results == null) {
					return "Unable to find similar patents!";
				}
			} catch (SQLException sql) {
				sql.printStackTrace();
				return("Database error!");
			}
			res.type("text/html");
			StringJoiner sj = new StringJoiner("</li><li>","<ul><li>","</li></ul>");
			results.forEach(r->{
				sj.add("Patent: "+r.getUrl()+" Similarity: "+r.getSimilarity()+'\n');
			});	
			String title = "<h3>Showing patents similar to: "+(new PatentResult(patent,0)).getUrl()+"</h3><hr/>";
			template.add(title+sj.toString());
			return template.toString();
		});
		
		post("/find_by_text", (req, res) -> {
			StringJoiner template = freshTemplate();
			ArrayList<PatentResult> results = null;
			String text = req.queryParams("text");
			if(text==null) return "Please provide some text!";
			// Create min hash for this text
			Vector<Integer> MinHashVector;
			try {
				Patent p = new Patent("","",text.toLowerCase(),"");
				MinHashVector = Main.createMinHash(p);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to perform search. Try providing more text!";
			}
			
			try {
				synchronized(Database.class) {
					results = Database.similarPatents(MinHashVector);
				}
			} catch (SQLException sql) {
				sql.printStackTrace();
				return("Database error!");
			}
			res.type("text/html");
			StringJoiner sj = new StringJoiner("</li><li>","<ul><li>","</li></ul>");
			results.forEach(r->{
				sj.add("Patent: "+r.getUrl()+" Similarity: "+r.getSimilarity()+'\n');
			});	
			String title = "<h3>Showing patents similar to Custom Text</h3><hr/>";
			template.add(title+sj.toString());
			return template.toString();
		});
	}
	
	
	public static void main(String[] args) {
		try {
			// Get patents
			synchronized(Database.class) {
				Database.setupMainConn();
				Main.setup();
				patents = new HashSet<String>(Database.getAllPatentStrings());
			}
			System.out.println("We have this many patents: "+patents.size());
			// Start server
			server();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
}
