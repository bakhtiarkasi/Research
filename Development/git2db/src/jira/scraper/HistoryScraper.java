package jira.scraper;

import git.scraper.db.DBConnector;
import git.scraper.pojo.Action;
import git.scraper.pojo.Issue;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import rleano.util.FileUtil;
import rleano.util.PropUtil;

public class HistoryScraper {
	
	DateFormat jdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.ENGLISH);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	PropUtil props = new PropUtil("config.properties");
	
	static public void main(String[] args) {
		
		HistoryScraper scraper = new HistoryScraper();
		File folder = new File("files/issues");
		File[] files = folder.listFiles();
//		boolean ok = false;
		for (File file : files) {
//			if (file.getName().equals("hbase_2013-01-03.xml")){
//				ok = true;
//			}
//			if (ok == true) {
				scraper.getIssues("files/issues/" + file.getName());
//			}
		}
		
	}
	
	public void getIssues(String filename) {
		try {

			String contents = FileUtil.readFile(filename);
			Document document = Jsoup.parse(contents, "", Parser.xmlParser());
			Elements elements = document.getElementsByTag("item");

			JiraScraper.print("Filename: " + filename);
			JiraScraper.print("Size: " + elements.size());
			
			
			int counter = 0;
			for (Element element : elements) {
				Issue issue = getIssue(element);
				this.getHistory(issue.getId());
				counter++;
				if (counter % 25 == 0) {
					JiraScraper.print("Issues ... " + counter);
				}
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	public void getHistory(String issueCode) {
		try {
		DBConnector db = new DBConnector(props);
		
		// issueCode = "HBASE-5155"
		String pre = "https://issues.apache.org/jira/browse/";
		String pos = "?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel";
		
		String address = pre + issueCode + pos;
		URL url = new URL(address);
		
//		String contents = FileUtil.readFile("files/issue.html");
//		Document doc = Jsoup.parse(contents, "", Parser.htmlParser());
		Document doc = Jsoup.parse(url, 6000);
		
		
		Elements first = doc.select("div.action-details:contains(created issue)");
		Element created = first.first();
		Action action = this.createdAction(created);
		db.createConnection();
		db.createAction(issueCode, action);
		db.close();
		
		Elements updates = doc.select("td.activity-name:contains(Status)");
		handleActions(issueCode, db, updates);
		
		updates = doc.select("td.activity-name:contains(Assignee)");
		handleActions(issueCode, db, updates);
		
		updates = doc.select("td.activity-name:contains(Resolution)");
		handleActions(issueCode, db, updates);
		} catch (Exception e) {
			FileUtil.writeLine("errors.log", "ERROR on issue [" + issueCode + "]\n", false);
			e.printStackTrace();
		}
	}

	private void handleActions(String issueCode, DBConnector db, Elements updates) {
		Action action;
		action = null;
		for (Element update : updates) {
			Element details = update.parent().parent().parent().parent().previousElementSibling();
			Elements author = details.select("a.user-avatar");
			Elements date = details.select("time.livestamp");
			Element oldValue = update.nextElementSibling();
			Element newValue = oldValue.nextElementSibling();
			
			action = new Action();
			action.setAction(update.text());
			action.setAuthor(author.first().text());
			action.setDate(getDate(date.first().attr("datetime")));
			action.setField(update.text());
			action.setOldValue(oldValue.text());
			action.setNewValue(newValue.text());
			db.createConnection();
			db.createAction(issueCode, action);
			db.close();
		}
	
	}
	
	
	private Action createdAction(Element created) {
		Elements author = created.select("a.user-avatar");
		Elements date = created.select("time.livestamp");
		Action action = new Action();
		action.setAction("Created");
		action.setAuthor(author.first().text());
		action.setDate(getDate(date.first().attr("datetime")));
		return action;
	}

	private String getDate(String date) {
		if (date.trim().isEmpty() == true) {
			return "";
		}
	    Date result = new Date();
		try {
			result = jdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	    return sdf.format(result);
	}
	
	private Issue getIssue(Element eIssue) {
		Issue issue = new Issue();

		String key = getText(eIssue, "key");
		String title = getText(eIssue, "title");
		String description = getText(eIssue, "description");
		String reporterName = getText(eIssue, "reporter");
		String reporter = getUsername(eIssue, "reporter");
		String assigneeName = getText(eIssue, "assignee");
		String assignee = getUsername(eIssue, "assignee");
		String priority = getText(eIssue, "priority");
		String status = getText(eIssue, "status");
		String resolution = getText(eIssue, "resolution");
		String component = getText(eIssue, "component");
		String created = getDateXML(getText(eIssue, "created"));
		// Only if it has been resolved
		String resolved = getDateXML(getText(eIssue, "resolved"));

		issue.setId(key);
		issue.setTitle(title);
		issue.setDescription(description);
		issue.setReporterName(reporterName);
		issue.setAssigneeName(assigneeName);
		issue.setReporter(reporter);
		issue.setAssignee(assignee);
		issue.setComponent(component);
		issue.setDateIssued(created);
		issue.setDateSolved(resolved);
		issue.setStatus(status);
		issue.setPriority(priority);
		issue.setResolution(resolution);

		return issue;
	}
	
	public String getUsername(Element element, String key) {
		if (element.select(key).isEmpty())
			return "";
		return element.select(key).first().attr("username");
	}
	
	private String getDateXML(String date) {
		if (date.trim().isEmpty() == true) {
			return "";
		}
	    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzzz", Locale.ENGLISH);
	    Date result = new Date();
		try {
			result = df.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	    return sdf.format(result);
	}
	
	public String getText(Element element, String key) {
		if (element.select(key).isEmpty())
			return "";
		return element.select(key).first().text();
	}
}
