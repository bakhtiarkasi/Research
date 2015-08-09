package jira.scraper;

import git.scraper.db.DBConnector;
import git.scraper.pojo.Comment;
import git.scraper.pojo.Issue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//import org.eclipse.jgit.api.Git;
//import org.eclipse.jgit.revwalk.RevCommit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import rleano.util.FileUtil;
import rleano.util.PropUtil;
import rleano.util.Util;

public class JiraScraper {

	private String project;
	private String gitFolder;
	private PropUtil props;
	private Map<String, Integer> hashes;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:MM");

	public JiraScraper() {
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
			Elements elements = document.getElementsByTag("item");

			JiraScraper.print("Filename: " + filename);
			JiraScraper.print("Size: " + elements.size());
			DBConnector db = new DBConnector(props);
			db.createConnection();
			int counter = 0;
			for (Element element : elements) {
				Issue issue = getIssue(element);
				int id = db.insertIssue(issue);
				for (Comment comment : issue.getComments()) {
					db.insertComment(comment, id);
				}

				// get hashes(s)
				// "git log --oneline --format=%h --grep="
				/*
				 * String command = "git log --oneline --format=%h --grep " +
				 * issue.getId() + "[^0-9] --grep=" + issue.getId() + "$";
				 * String[] lines = Util.cmdExec(command.split(" "), gitFolder +
				 * this.project + "/").split("\\r?\\n"); int counter2 = 0; for
				 * (String line : lines) { if (line.trim().isEmpty() == false) {
				 * this.hashes.put(line.trim(), id); } counter2++; }
				 */

				counter++;
				if (counter % 25 == 0) {
					JiraScraper.print("Issues ... " + counter);
				}
			}
			db.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
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
		String created = getDate(getText(eIssue, "created"));
		// Only if it has been resolved
		String resolved = getDate(getText(eIssue, "resolved"));

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
		Element eComments = eIssue.select("comments").first();
		if (eComments != null) {
			Elements comments = eComments.children();
			for (Element element : comments) {
				String author = element.attr("author");
				String date = getDate(element.attr("created"));
				String text = element.text();
				Comment comment = new Comment(author, text, date);
				// JiraScraper.print("Comment: " + author + " - " + date +
				// " :: " + text);
				issue.addComment(comment);
			}
		}

		return issue;
	}

	private void getCommits(String filename) {
		try {
			DBConnector db = new DBConnector(props);
			String contents = FileUtil.readFile(filename);
			Document document = Jsoup.parse(contents, "", Parser.xmlParser());
			Elements elements = document.getElementsByTag("item");

			JiraScraper.print("Filename: " + filename);
			JiraScraper.print("Size: " + elements.size());

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
					JiraScraper.print("Issues ... " + counter);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getUsername(Element element, String key) {
		if (element.select(key).isEmpty())
			return "";
		return element.select(key).first().attr("username");
	}

	private String getDate(String date) {
		if (date.trim().isEmpty() == true) {
			return "";
		}
		DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzzz",
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

		// hbase files name
		// final String projectName = "hbase_";

		// derby project name
		final String projectName = "derby_";
		JiraScraper scraper = new JiraScraper();

		/*
		 * 
		 * FileFilter filter = new FileFilter() {
		 * 
		 * @Override public boolean accept(File pathname) { return
		 * pathname.isFile() && pathname.getName().startsWith(projectName); } };
		 * 
		 * JiraScraper scraper = new JiraScraper(); File folder = new
		 * File("files/issues"); File[] files = folder.listFiles(filter);
		 * 
		 * Arrays.sort(files, new Comparator<File>() {
		 * 
		 * @Override public int compare(File o1, File o2) { try { DateFormat df
		 * = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		 * 
		 * Date x1 = df.parse(o1.getName().replaceAll(".xml",
		 * "").replaceAll(projectName, ""));
		 * 
		 * Date x2 = df.parse(o2.getName().replaceAll(".xml",
		 * "").replaceAll(projectName, ""));
		 * 
		 * return x1.compareTo(x2); } catch (ParseException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } return 0;
		 * 
		 * } });
		 * 
		 * 
		 * for (File file : files) { //if(file.getName().contains("24.xml")) {
		 * //this is important only //scraper.getIssues(file.getAbsolutePath());
		 * 
		 * //scraper.getCommits(file.getAbsolutePath());
		 * 
		 * 
		 * //System.out.println(file.getAbsolutePath()); //break; } }
		 */

		// scraper.updateFileCreationDates();
		// scraper.updateCommitLinkage();
		// scraper.getCommitsStatus();

		scraper.reportDeletedFiles();

	}

	private void updateFileCreationDates() {

		try {

			DBConnector db = new DBConnector(props);
			db.createConnection();
			List<String> file = db.getSourceFiles();
			db.close();

			StringBuilder builder = new StringBuilder();

			Map<String, String> results = new HashMap();
			List<String> nonFound = new ArrayList();

			String[] command = null;
			String path = "";
			boolean found = false;
			int count = 0;

			int foundP = 0;
			int notfound = 0;

			for (String fil : file) {

				String line = "";

				command = new String[] { "git", "log", "--diff-filter=A",
						"--format=%aD@%H", "--", fil };
				path = gitFolder + this.project;

				Process process = Runtime.getRuntime().exec(command, null,
						new File(path));

				BufferedReader br = new BufferedReader(new InputStreamReader(
						process.getInputStream()));

				String tmp = "";
				while (isAlive(process)) {

					while (br.ready() && (tmp = br.readLine()) != null) {
						line = tmp;
					}

				}
				while (br.ready() && (tmp = br.readLine()) != null) {
					line = tmp;
				}

				builder = new StringBuilder();
				for (String s : command) {
					builder.append(s);
					builder.append(" ");
				}

				if (line.isEmpty()) {
					found = false;
					System.out.println("Not Found " + fil);
					System.out.println("cd " + path + " \n "
							+ builder.toString());
					notfound++;
				} else {
					line += "@" + path;
					results.put(fil, line);
					found = true;
					foundP++;
				}
			}
			// db.createConnection();
			// db.updateFileCreationDates(results);
			// db.close();
			System.out.println(foundP + "/" + notfound);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void reportDeletedFiles() {
		try {

			DBConnector db = new DBConnector(props);
			db.createConnection();
			List<String> file = db.getSourceFiles();
			db.close();

			StringBuilder builder = new StringBuilder();

			Map<String, String> results = new HashMap();
			List<String> nonFound = new ArrayList();

			String[] command = null;
			String path = "";
			boolean found = false;
			int count = 0;

			int foundP = 0;
			int notfound = 0;

			for (String fil : file) {

				String fileName = fil.split(":")[0];
				path = gitFolder + this.project;

				if (this.project.equals("Mylyn")) {
					String projectName = "";
					if (fil.split(":").length == 2) {
						projectName = fil.split(":")[1];
						if (!projectName.isEmpty())
							path += "/" + projectName;
					}
				}

				String line = "";

				command = new String[] { "git", "log", "--diff-filter=D",
						"--summary", "--", fileName, "|", "grep", "delete" };

				Process process = Runtime.getRuntime().exec(command, null,
						new File(path));

				BufferedReader br = new BufferedReader(new InputStreamReader(
						process.getInputStream()));

				String tmp = "";
				while (isAlive(process)) {

					while (br.ready() && (tmp = br.readLine()) != null) {
						line = tmp;
					}

				}
				while (br.ready() && (tmp = br.readLine()) != null) {
					line = tmp;
				}

				builder = new StringBuilder();
				for (String s : command) {
					builder.append(s);
					builder.append(" ");
				}

				if (line.isEmpty()) {
					found = false;
					// System.out.println("Not Found " + fileName);
					// System.out.println("cd " + path + " \n "
					// + builder.toString());
					if (notfound % 500 == 0)
						System.out.println("Done with " + notfound);

					notfound++;
				} else {
					line += "@" + path;
					found = true;
					foundP++;
				}
			}
			// db.createConnection();
			// db.updateFileCreationDates(results);
			// db.close();
			System.out.println(foundP + "/" + notfound);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isAlive(Process p) {
		try {
			p.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}

	private void updateCommitLinkage() {
		try {
			DBConnector db = new DBConnector(props);
			db.createConnection();

			db.updateCommitLinkage();

			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
