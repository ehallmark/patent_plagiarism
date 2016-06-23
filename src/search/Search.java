package search;

import seed.Claim;
import seed.Database;
import seed.Database.SimilarityType;
import seed.PatentResult;
import spark.Request;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static spark.Spark.get;
import static spark.Spark.post;



public class Search {	
	private static final int DEFAULT_LIMIT = 10;
	private static final String HTML =  "<h3>By Patent</h3>"
			+ "<form action='/find_by_patent' method='get'>"
			+ "<label style='margin: 5px 10px;'>Patent:</label><input id='patent' name='patent' /><br/>"
			+ "<label style='margin: 5px 10px;'>Limit:</label><input name='limit' id='limit' value='"+DEFAULT_LIMIT+"' /><br/><br/>"
			+ "<label style='margin: 5px 10px;'>Include Assignees:</label><input type='checkbox' name='withAssignees' id='withAssignees' value='true' /><br/><br/>"
			+ "<button>Search</button>"

		+ "</form>"
		+ "<h3>By Text</h3>"
		+ "<form action='/find_by_text' method='post'>"
			+ "<label style='margin: 5px 10px; vertical-text-align: top; text-align:top; vertical-align:top;'>Text:</label><textarea rows='10' cols='50' id='text' name='text' ></textarea><br/>"
			+ "<label style='margin: 5px 10px;'>Limit:</label><input name='limit' id='limit' value='"+DEFAULT_LIMIT+"' /><br/><br/>"
			+ "<label style='margin: 5px 10px;'>Include Assignees:</label><input type='checkbox' name='withAssignees' id='withAssignees' value='true' /><br/><br/>"
			+ "<button>Search</button>"
		+ "</form>";
	
	private static StringJoiner freshTemplate() {
		return new StringJoiner("","<div style='width:80%; padding: 2% 10%;'><h2><a style='color:black; text-decoration:none;' href='/'>Similar Patent Finder</a></h2><hr/>","<br /><hr/>"+HTML+"</div>");
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
			SimilarityType type = getSimilarityType(req);
			res.cookie("by", type.toString().toLowerCase());
			template.add(resultsToHTML(new ArrayList<PatentResult>(),type,req));
			return template.toString();
		});
		
		get("/find_by_patent", (req, res) -> {
			res.type("text/html");
			StringJoiner template = freshTemplate();
			String patent = req.queryParams("patent");
			Integer limit = getLimit(req);
			boolean withAssignees = getWithAssignees(req);
			// set cookie
			res.cookie("limit", limit.toString());
			res.cookie("withAssignees", String.valueOf(withAssignees));

			if(patent==null || patent.trim().length()==0) return template.add("<b>Please provide a patent number!</b>").toString();
			else {patent=patent.toUpperCase().trim().replaceAll("US","").replaceAll("[^0-9A-Z]", "");}
						
			SimilarityType type = getSimilarityType(req);
			res.cookie("by", type.toString().toLowerCase()); 
			PatentResult pr = new PatentResult(patent,withAssignees); 
			String title = "<h4>Results for Patent "+pr.getUrl()+pr.getExternalUrl()+"</h4>";
			template.add(title+resultsToHTML(Database.similarPatents(patent, type, limit, withAssignees),type, req));
			return template.toString();
		});
		
		post("/find_by_text", (req, res) -> {
			StringJoiner template = freshTemplate();
			String text = req.queryParams("text");
			if(text==null) return  template.add("<b>Please provide some text!</b>").toString();
			else {text=Database.cleanWords(text);}			
			Integer limit = getLimit(req);
			boolean withAssignees = getWithAssignees(req);
			// set cookie
			res.cookie("limit", limit.toString());
			res.cookie("withAssignees", String.valueOf(withAssignees));
			SimilarityType type = getSimilarityType(req);
			if(type.equals(SimilarityType.CITATION)) return  template.add("<b>Cannot find citations for text input!</b>").toString();

			res.cookie("by", type.toString().toLowerCase()); 
			
			// Create min hash for this text
			Claim p;
			try {
				p = new Claim(text,type);
			} catch (Exception e) {
				e.printStackTrace();
				return template.add("<b>Unable to perform search. Try providing more text!</b>").toString();
			}
			
			template.add(resultsToHTML(Database.similarPatents(p.getValues(),type,limit,withAssignees),type, req));
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
			else if(type.equalsIgnoreCase("citation")) return SimilarityType.CITATION;

		} else {
			// Check cookies
			type = req.cookie("by");
			if(type!=null) {
				if(type.equalsIgnoreCase("abstract")) return SimilarityType.ABSTRACT;
				else if(type.equalsIgnoreCase("description")) return SimilarityType.DESCRIPTION;
				else if(type.equalsIgnoreCase("claim")) return SimilarityType.CLAIM;
				else if(type.equalsIgnoreCase("citation")) return SimilarityType.CITATION;

			}
		}
		// DEFAULT
		return SimilarityType.ABSTRACT;
	}
	
	public static boolean getWithAssignees(Request req) {
		String assignees = req.queryParams("withAssignees");
		if(assignees!=null && assignees.equalsIgnoreCase("true")) return true;
		String assigneesCookie = req.cookie("withAssignees");
		if(assigneesCookie!=null && assigneesCookie.equalsIgnoreCase("true")) return true;
		return false;
	}

	public static String resultsToHTML(List<PatentResult> results, SimilarityType type, Request req) {
		StringJoiner outerWrapper = new StringJoiner("","<div style='width:70%; left:0px; top:0px; height: auto;'>","</div>");
		StringJoiner sj = new StringJoiner("","<table><tbody>","</tbody></table>");
		String prefix = "<form action='"+req.uri()+"' id='form1' style='display:inline;' method='"+req.requestMethod()+"'>";
		String suffix = "</form>";
		StringJoiner mainContents = new StringJoiner("");
		// subtitle
		StringJoiner form1 = new StringJoiner("",prefix,suffix);
		StringJoiner form2 = new StringJoiner("",prefix,suffix);
		StringJoiner form3 = new StringJoiner("",prefix,suffix);
		StringJoiner form4 = new StringJoiner("",prefix,suffix);

		req.queryParams().forEach(q->{
			if(!q.equalsIgnoreCase("by"))mainContents.add("<input type='hidden' name='"+q+"' value='"+req.queryParams(q).replaceAll("[^0-9A-Za-z :;,.]", "")+"' />");				
		});
		
		form1.add(mainContents.toString()).add("<input type='hidden' name='by' value='abstract' />");	
		form2.add(mainContents.toString()).add("<input type='hidden' name='by' value='description' />");	
		form3.add(mainContents.toString()).add("<input type='hidden' name='by' value='claim' />");
		form4.add(mainContents.toString()).add("<input type='hidden' name='by' value='citation' />");


		String absBtn = "<button>Abstract</button>";
		String descBtn = "<button>Description</button>";
		String claimBtn = "<button>Claims</button>";
		String citationBtn = "<button>Citations</button>";

		if(type.equals(SimilarityType.ABSTRACT)) {
			absBtn = "<button style='background-color: blue; color: white;' disabled>Abstract</button>";
		} else if (type.equals(SimilarityType.DESCRIPTION)) {
			descBtn = "<button style='background-color: blue; color: white;' disabled>Description</button>";
		} else if (type.equals(SimilarityType.CLAIM)) {
			claimBtn = "<button style='background-color: blue; color: white;' disabled>Claims</button>";
		} else if (type.equals(SimilarityType.CITATION)) {
			citationBtn = "<button style='background-color: blue; color: white;' disabled>Citations</button>";
		}

		StringJoiner subtitle = new StringJoiner("","<h4>By ","</h4>");
		form1.add(absBtn); form2.add(descBtn); form3.add(claimBtn); form4.add(citationBtn); subtitle.add(form1.toString()+form2.toString()+form3.toString()+form4.toString());
		
		// actual results
		if(results == null) {
			outerWrapper.add("Patent not found!");
		}  else if (results.isEmpty()) {
			outerWrapper.add("No results found!");

		} else {
			results.forEach(r->{
				StringJoiner row = new StringJoiner("</td><td>","<tr><td>","</td></tr>");
				row.add(r.getUrl());
				if(!type.equals(SimilarityType.CITATION))row.add(r.getSimilarity());
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
			server();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
}
