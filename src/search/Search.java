package search;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.Vector;

import seed.Database;
import seed.Main;
import seed.Patent;
import seed.PatentResult;
import seed.TechnologyResult;

public class Search {	
	private static StringJoiner freshTemplate() {
		return new StringJoiner("","<div style='width:100%; padding: 2% 10%;'><h2>Similar Patent Finder</h2>","</div>");
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
				+ "<form action='/find_by_patent' method='post'>"
					+ "<button>Find Similar Patents</button><br/><br />"
					+ "<label style='margin-right:15px;'>Patent Number:</label><input id='patent' name='patent' /><br/>"
					+ "<label style='margin-right:15px;'>Limit:</label><input name='limit' id='limit' value='100' /><br/>"
					+ "<label style='margin-right:15px;'>Technology Estimate?</label><input name='technology' id='technology' type='checkbox' value='yes' /><br/>"
				+ "</form><br/>"
				+ "<h3>By Raw Text</h3>"
				+ "<form action='/find_by_text' method='post'>"
					+ "<button>Find Similar Patents</button><br/><br/>"
					+ "<label style='margin-right:15px;'>Limit:</label><input name='limit' id='limit' value='100' /><br/>"
					+ "<textarea rows='10' cols='50' id='text' name='text' ></textarea>"
				+ "</form>";
			template.add(html);
			return template.toString();
		});
		
		post("/find_by_patent", (req, res) -> {
			StringJoiner template = freshTemplate();
			ArrayList<PatentResult> results = null;
			String patent = req.queryParams("patent");
			Integer limit;
			try{ limit = Integer.parseInt(req.queryParams("limit")); } catch(Exception e){ limit = 100; }
			if(patent==null) return "Please provide a patent number!";
			else {patent=patent.toUpperCase().trim().replaceAll("[^0-9A-Z]", "");}
			
			boolean withTechnologies;
			if(req.queryParams("technology")==null) withTechnologies = false;
			else withTechnologies = true;
		
			try {
				synchronized(Database.class) {
					results = Database.similarPatents(patent,limit,true);
				}
				if(results == null) {
					return "Patent not found!";
				}
			} catch (SQLException sql) {
				sql.printStackTrace();
				return("Database error!");
			}
			
			res.type("text/html");
			StringJoiner sj = new StringJoiner("</li><li>","<div style='width:40%; float:left; margin:0px; padding:0px; height:auto; display:inline;'><ul><li>","</li></ul></div>");
			results.forEach(r->{
				sj.add("Patent: "+r.getUrl()+" Similarity: "+r.getSimilarity()+'\n');
			});	
			StringJoiner outerWrapper = new StringJoiner("","<div style='width:70%; left:0px; top:0px; height: auto;'>","</div>");
			outerWrapper.add(sj.toString());
			// Technologies
			if(withTechnologies) {
				StringJoiner sj2 = new StringJoiner("</li><li>","<div style='width:40%; margin:0px; padding:0px; float:right; height:auto; display:inline;'><ul><li>","</li></ul></div>");

				List<TechnologyResult> technologies = new ArrayList<TechnologyResult>();
				try{
					synchronized(Database.class) {
						technologies = Database.similarTechnologies(patent);
					}
				} catch(SQLException sql) {
					sql.printStackTrace();
					return("Unable to find technologies");
				}
				
				technologies.forEach(t->{
					sj2.add("Technology: "+t.getName()+" Similarity: "+t.getSimilarity()+'\n');
				});
				outerWrapper.add(sj2.toString());
				
			}
			String title = "<h3>Showing patents similar to: "+(new PatentResult(patent,0)).getUrl()+"</h3><hr/>";
			template.add(title+outerWrapper.toString());
			return template.toString();
		});
		
		post("/find_by_text", (req, res) -> {
			StringJoiner template = freshTemplate();
			ArrayList<PatentResult> results = null;
			String text = req.queryParams("text");
			Integer limit;
			try{ limit = Integer.parseInt(req.queryParams("limit")); } catch(Exception e){ limit = 100; }
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
					results = Database.similarPatents(MinHashVector,limit,true);
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
			}
			// Start server
			server();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
}
