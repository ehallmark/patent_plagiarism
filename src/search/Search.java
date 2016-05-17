package search;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringJoiner;

import seed.Database;
import seed.Patent;
import seed.PatentResult;
import spark.Request;

public class Search {	
	private static final int DEFAULT_LIMIT = 10;
	
	private static StringJoiner freshTemplate() {
		return new StringJoiner("","<div style='width:100%; padding: 2% 10%;'><h2>Similar Patent Finder</h2><hr />","</div>");
	}
	
	public static void server() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		get("/", (req, res) -> {
			res.type("text/html");
			StringJoiner template = freshTemplate();
			String html =  "<h3>By Patent</h3>"
				+ "<form action='/find_by_patent' method='get'>"
					+ "<label style='margin-right:15px;'>Patent Number:</label><input id='patent' name='patent' /><br/>"
					+ "<label style='margin-right:15px;'>Limit:</label><input name='limit' id='limit' value='"+DEFAULT_LIMIT+"' /><br/>"
					+ "<button>Search</button>"

				+ "</form><br/>"
				+ "<h3>By Raw Text</h3>"
				+ "<form action='/find_by_text' method='post'>"
					+ "<label style='margin-right:15px;'>Limit:</label><input name='limit' id='limit' value='"+DEFAULT_LIMIT+"' /><br/>"
					+ "<textarea rows='10' cols='50' id='text' name='text' ></textarea><br/><br />"
					+ "<button>Search</button>"
				+ "</form>";
			template.add(html);
			return template.toString();
		});
		
		get("/find_by_patent", (req, res) -> {
			res.type("text/html");
			StringJoiner template = freshTemplate();
			String patent = req.queryParams("patent");
			Integer limit = getLimit(req);
			// set cookie
			res.cookie("limit", limit.toString());
			
			if(patent==null) return "Please provide a patent number!";
			else {patent=patent.toUpperCase().trim().replaceAll("US","").replaceAll("[^0-9A-Z]", "");}
			PatentResult pr = new PatentResult(patent,0);
			String title = "<h3>Showing "+limit+" most similar patents to "+ pr.getUrl()+pr.getExternalUrl()+"</h3>";
			String subtitle = "<h4>By Similarity Of <button>Abstract</button><button>Description</button><button>Claims</button></h4>";
			template.add(title+subtitle+resultsToHTML(Database.similarPatents(patent, limit)));
			return template.toString();
		});
		
		post("/find_by_text", (req, res) -> {
			StringJoiner template = freshTemplate();
			String text = req.queryParams("text");
			if(text==null) return "Please provide some text!";
	
			Integer limit = getLimit(req);
			// set cookie
			res.cookie("limit", limit.toString());
			
			// Create min hash for this text
			Patent p;
			try {
				p = new Patent("",text,text);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to perform search. Try providing more text!";
			}
	
			String title = "<h3>Showing "+limit+" most similar patents</h3>";
			String subtitle = "<h4>By Similarity Of <button>Abstract</button><button>Description</button><button>Claims</button></h4>";
			template.add(title+subtitle+resultsToHTML(Database.similarPatents(p.getAbstractValues(),limit)));
			return template.toString();
		});
	}
	
	
	private static Integer getLimit(Request req) {
		try{ return Integer.parseInt(req.queryParams("limit")); } catch(Exception e){ 
			// check cookies for limit
			try { return Integer.parseInt(req.cookie("limit")); } catch(Exception e2) {
				return DEFAULT_LIMIT; 
			}
		}
	}

	public static String resultsToHTML(ArrayList<PatentResult> results) {
		StringJoiner outerWrapper = new StringJoiner("","<div style='width:70%; left:0px; top:0px; height: auto;'>","</div>");
		StringJoiner sj = new StringJoiner("","<table><thead><tr><th>Patent Number</th><th>Similarity</th><th></th></tr></thead><tbody>","</tbody></table>");

		if(results == null) {
			sj.add("Patent not found!");
		}  else {
			results.forEach(r->{
				StringJoiner row = new StringJoiner("</td><td>","<tr><td>","</td></tr>");
				row.add(r.getUrl());
				row.add(r.getSimilarity());
				row.add(r.getExternalUrl());
				sj.add(row.toString());
			});	
		}
		outerWrapper.add(sj.toString());		
		return outerWrapper.toString();
	}
	
	public static void main(String[] args) {
		try {
			// Get patents
			synchronized(Database.class) {
				Database.setupMainConn();
			}
			// Start server
			Patent.setup();

			server();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
}
