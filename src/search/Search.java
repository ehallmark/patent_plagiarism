package search;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringJoiner;

import seed.Database;
import seed.Database.SimilarityType;
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
						
			SimilarityType type = getSimilarityType(req);
			res.cookie("by", type.toString().toLowerCase());
		

			PatentResult pr = new PatentResult(patent,0);
			String title = "<h3>Showing "+limit+" most similar patents to "+ pr.getUrl()+pr.getExternalUrl()+"</h3>";
			
			template.add(title+resultsToHTML(Database.similarPatents(patent, type, limit),type, req));
			
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
			
			SimilarityType type = getSimilarityType(req);
			// set cookie
			res.cookie("by", type.toString().toLowerCase());
			
	
			String title = "<h3>Showing "+limit+" most similar patents</h3>";
			template.add(title+resultsToHTML(Database.similarPatents(p.getAbstractValues(),type,limit),type, req));
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
	
	private static SimilarityType getSimilarityType(Request req) {
		String type = req.queryParams("by");
		if(type!=null) {
			if(type.equalsIgnoreCase("abstract")) return SimilarityType.ABSTRACT;
			else if(type.equalsIgnoreCase("description")) return SimilarityType.DESCRIPTION;
			else if(type.equalsIgnoreCase("claim")) return SimilarityType.CLAIM;
		} else {
			// Check cookies
			type = req.cookie("by");
			if(type!=null) {
				if(type.equalsIgnoreCase("abstract")) return SimilarityType.ABSTRACT;
				else if(type.equalsIgnoreCase("description")) return SimilarityType.DESCRIPTION;
				else if(type.equalsIgnoreCase("claim")) return SimilarityType.CLAIM;
			}
		}
		// DEFAULT
		return SimilarityType.ABSTRACT;
	}

	public static String resultsToHTML(ArrayList<PatentResult> results, SimilarityType type, Request req) {
		StringJoiner outerWrapper = new StringJoiner("","<div style='width:70%; left:0px; top:0px; height: auto;'>","</div>");
		StringJoiner sj = new StringJoiner("","<table><thead><tr><th>Patent Number</th><th>Similarity</th><th></th></tr></thead><tbody>","</tbody></table>");
		String prefix = "<form action='"+req.uri()+"' id='form1' style='display:inline;' method='"+req.requestMethod()+"'>";
		String suffix = "</form>";
		StringJoiner mainContents = new StringJoiner("");
		// subtitle
		StringJoiner form1 = new StringJoiner("",prefix,suffix);
		StringJoiner form2 = new StringJoiner("",prefix,suffix);
		StringJoiner form3 = new StringJoiner("",prefix,suffix);
		req.queryParams().forEach(q->{
			if(!q.equalsIgnoreCase("by"))mainContents.add("<input type='hidden' name='"+q+"' value='"+req.queryParams(q).replaceAll("[^0-9A-Za-z :;,.]", "")+"' />");				
		});
		
		form1.add(mainContents.toString()).add("<input type='hidden' name='by' value='abstract' />");	
		form2.add(mainContents.toString()).add("<input type='hidden' name='by' value='description' />");	
		form3.add(mainContents.toString()).add("<input type='hidden' name='by' value='claim' />");	

		String absBtn = "<button>Abstract</button>";
		String descBtn = "<button>Description</button>";
		String claimBtn = "<button>Claims</button>";
		if(type.equals(SimilarityType.ABSTRACT)) {
			absBtn = "<button disabled>Abstract</button>";
		} else if (type.equals(SimilarityType.DESCRIPTION)) {
			descBtn = "<button disabled>Description</button>";
		} else if (type.equals(SimilarityType.CLAIM)) {
			claimBtn = "<button disabled>Claims</button>";
		}			
		StringJoiner subtitle = new StringJoiner("","<h4>By Similarity Of ","</h4>");
		form1.add(absBtn); form2.add(descBtn); form3.add(claimBtn); subtitle.add(form1.toString()+form2.toString()+form3.toString());
		
		// actual results
		if(results == null) {
			outerWrapper.add("Patent not found!");
		}  else {
			results.forEach(r->{
				StringJoiner row = new StringJoiner("</td><td>","<tr><td>","</td></tr>");
				row.add(r.getUrl());
				row.add(r.getSimilarity());
				row.add(r.getExternalUrl());
				sj.add(row.toString());
			});	
			outerWrapper.add(sj.toString());	

		}
		

		return subtitle.toString()+outerWrapper.toString();
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
