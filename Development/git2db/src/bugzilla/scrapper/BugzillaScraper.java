package bugzilla.scrapper;

import git.scraper.db.DBConnector;
import git.scraper.pojo.Comment;
import git.scraper.pojo.Issue;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import rleano.util.FileUtil;
import rleano.util.PropUtil;
import rleano.util.Util;

public class BugzillaScraper {

	private String project;
	private String gitFolder;
	private PropUtil props;
	private Map<String, Integer> hashes;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:MM");

	public BugzillaScraper() {
		this.props = new PropUtil("config.properties");
		// this.commits = new HashMap<String, RevCommit>();
		this.hashes = new HashMap<String, Integer>();
		// this.initRepo(project);
		this.project = this.props.get("project.name");
		this.gitFolder = this.props.get("git.home")
				+ this.props.get("project.location");
	}

	public void getIssues(String filename) {
		try {

			String contents = FileUtil.readFile(filename);
			Document document = Jsoup.parse(contents, "", Parser.xmlParser());
			Elements elements = document.getElementsByTag("bug");

			BugzillaScraper.print("Filename: " + filename);
			BugzillaScraper.print("Size: " + elements.size());
			DBConnector db = new DBConnector(props);
			db.createConnection();
			int counter = 0;
			for (Element element : elements) {
				Issue issue = getIssue(element);

				int id = db.insertIssue(issue);
				for (Comment comment : issue.getComments()) {
					db.insertComment(comment, id);
				}

				/*
				 * 
				 * String command = "git log --oneline --format=%h --grep " +
				 * issue.getId() + "[^0-9] --grep=" + issue.getId() + "$";
				 * String[] lines = Util.cmdExec(command.split(" "), gitFolder +
				 * this.project + "/").split("\\r?\\n"); int counter2 = 0; for
				 * (String line : lines) { if (line.trim().isEmpty() == false) {
				 * System.out.println(issue.getId() + " : " + line.trim());
				 * //this.hashes.put(line.trim(), id); } counter2++; }
				 */

				counter++;
				if (counter % 25 == 0) {
					BugzillaScraper.print("Issues ... " + counter);
				}

			}
			db.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Issue getIssue(Element eIssue) {
		Issue issue = new Issue();

		String key = getText(eIssue, "bug_id");
		String title = getText(eIssue, "short_desc");
		// String description = getText(eIssue, "description");
		String reporterName = getText(eIssue, "reporter");
		String reporter = getUsername(eIssue, "reporter");
		String assigneeName = getText(eIssue, "assigned_to");
		String assignee = getUsername(eIssue, "assigned_to");
		String priority = getText(eIssue, "priority");
		String status = getText(eIssue, "bug_status");
		String resolution = getText(eIssue, "resolution");
		String component = getText(eIssue, "component");
		String created = getDate(getText(eIssue, "creation_ts"));
		// Only if it has been resolved
		String resolved = getDate(getText(eIssue, "delta_ts"));
		String description = "";

		Elements eComments = eIssue.select("long_desc");
		for (Element element : eComments) {
			String count = getText(element, "comment_count");
			if (count.equals("0")) {
				description = getText(element, "thetext");
			} else {
				String author = getUsername(element, "who");
				String date = getDate(getText(element, "bug_when"));
				String text = getText(element, "thetext");
				Comment comment = new Comment(author, text, date);
				// BugzillaScraper.print("Comment: " + author + " - " + date +
				// " :: " + text);
				issue.addComment(comment);
			}
		}

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

		// Get comments

		return issue;
	}

	private void getCommits(String filename) {
		try {
			DBConnector db = new DBConnector(props);
			String contents = FileUtil.readFile(filename);
			Document document = Jsoup.parse(contents, "", Parser.xmlParser());
			Elements elements = document.getElementsByTag("bug");

			BugzillaScraper.print("Filename: " + filename);
			BugzillaScraper.print("Size: " + elements.size());

			int counter = 0;
			for (Element element : elements) {
				Issue issue = getIssue(element);

				// get hashes(s)
				// "git log --oneline --format=%h --grep="
				String command = "git log --oneline --format=%h --grep "
						+ issue.getId() + "[^0-9] --grep=" + issue.getId()
						+ "$";
				
				
				
				String[] lines = Util.cmdExec(command.split(" "),
						gitFolder + this.project + "/").split("\\r?\\n");
				int counter2 = 0;

				db.createConnection();
				int id = db.getIssueId(issue.getId());
				for (String line : lines) {
					if (line.trim().isEmpty() == false) {
						db.updateIssue(line, id, issue.getId());
					}
					counter2++;
				}
				db.close();

				counter++;
				if (counter % 25 == 0) {
					BugzillaScraper.print("Issues ... " + counter);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getUsername(Element element, String key) {
		if (element.select(key).isEmpty())
			return "";
		return element.select(key).first().attr("name");
	}

	private String getDate(String date) {
		if (date.trim().isEmpty() == true) {
			return "";
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss zzzz",
				Locale.ENGLISH);
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

	public static void print(String message) {
		System.out.println(message);
		FileUtil.writeLine("scraper.log", message, false);
	}

	public static void main(String[] args) throws Exception {
		BugzillaScraper scraper = new BugzillaScraper();
		File folder = new File("MylnIssues");
		File[] files = folder.listFiles();

		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				Integer x1 = Integer.parseInt(o1.getName().replaceAll(".xml",
						""));
				Integer x2 = Integer.parseInt(o2.getName().replaceAll(".xml",
						""));

				return x1.compareTo(x2);

			}
		});

		for (File file : files) {
			// if(file.getName().contains("12.xml"))
			{
				scraper.getIssues(file.getAbsolutePath());

				// System.out.println(file.getAbsolutePath());
			}
		}

		scraper.updateCommitLinkage();
		//scraper.getCommitsStatus();
		
	}

	private void updateCommitLinkage() {
		try{
			DBConnector db = new DBConnector(props);
			db.createConnection();
			
			db.updateCommitLinkage();
			
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getCommitsStatus() {
		try {

			DBConnector db = new DBConnector(props);
			db.createConnection();
			int counter = 0;
			List<String> issues = db.getAllIssueKeys();

			String projects[] = new String[8];

			projects[0] = "org.eclipse.mylyn";
			projects[1] = "org.eclipse.mylyn.builds";
			projects[2] = "org.eclipse.mylyn.commons";
			projects[3] = "org.eclipse.mylyn.context";
			projects[4] = "org.eclipse.mylyn.docs";
			projects[5] = "org.eclipse.mylyn.reviews";
			projects[6] = "org.eclipse.mylyn.tasks";
			projects[7] = "org.eclipse.mylyn.versions";
			
			Map<String, Integer> issueCount = new HashMap();;

			String command = "";
			for (int i = 0; i < issues.size(); i++) {
				command = "git log --oneline --format=%h --grep "
						+ issues.get(i) + "[^0-9] --grep=" + issues.get(i)
						+ "$";
				System.out.println(command);
				for (int j = 0; j < projects.length; j++) {
					String[] lines = Util.cmdExec(command.split(" "),
							gitFolder + this.project + "/" + projects[j] + "/").split("\\r?\\n");

					for (String line : lines) {
						if (!line.trim().isEmpty()) {
							issueCount.put(issues.get(i), issueCount.get(issues.get(i)) == null ? 1 :  issueCount.get(issues.get(i))+1);
						}
					}
				}
				
				if(i% 100 == 0)
				{
					System.out.println("done with " + i);
				}
			}

			db.close();
			
			for(Entry<String, Integer> entry : issueCount.entrySet())
			{
				System.out.println(entry.getKey() + " " + entry.getValue());
				
			}
			
			System.out.println("All key size " + issueCount.keySet().size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
