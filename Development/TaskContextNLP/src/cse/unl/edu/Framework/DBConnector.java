package cse.unl.edu.Framework;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import cse.unl.edu.Lucene.LuceneIndexer;
import cse.unl.edu.util.Utils;

public class DBConnector {

	private String url;
	private Connection conn = null;
	private ResultSet set = null;
	private PreparedStatement statement = null;

	private String prefix = null;

	public DBConnector() {
		this.url = "jdbc:mysql://cse.unl.edu";
		this.url += "/" + "bkasi";
		this.url += "?user=" + "bkasi";
		this.url += "&password=" + "dj}3yv";
		this.prefix = "mylyn";
	}

	public void createConnection() {
		if (this.conn == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				this.conn = DriverManager.getConnection(this.url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void close() {
		try {
			if (set != null) {
				set.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
			conn = null;
		} catch (Exception e) {
			conn = null;
		}
	}

	public List<Task> getIssueDescription(String issueIds) {
		ResultSet result = null;
		List<Task> tasks = new ArrayList<Task>();
		try {
			// String query = "SELECT `key`, description from " + prefix +
			// "_issue ";

			String query = "Select `key`, if(description > '', description,title) description,  group_concat(fil.filename, ':', com.id) from "
					+ prefix
					+ "_issue iis "
					+ "inner join "
					+ prefix
					+ "_issue2commit i2c on iis.id = i2c.issue_id "
					+ "inner join "
					+ prefix
					+ "_commit com on i2c.commit_id = com.id "
					+ "inner join "
					+ prefix + "_file fil on fil.commit_id = com.id ";

			String where = (issueIds != null && !issueIds.isEmpty()) ? " Where `key` in ("
					+ issueIds + ")"
					: "";

			String groupby = " group by `key`,description";

			statement = conn
					.prepareStatement("SET group_concat_max_len := 90000000");
			statement.execute();

			statement = conn.prepareStatement(query + where + groupby);
			System.out.println(query + where + groupby);

			result = statement.executeQuery();

			Task task;

			while (result.next()) {
				task = new Task(result.getString(1), result.getString(2));
				// task.comments = result.getString(3);
				task.loadFiles(result.getString(3));

				tasks.add(task);
			}

			query = "Select `key`, group_concat(distinct(cmnt.comment), ' ') comments from "
					+ prefix
					+ "_issue iis left outer join "
					+ prefix
					+ "_comment cmnt on cmnt.issue_id = iis.id  ";
			where = (issueIds != null && !issueIds.isEmpty()) ? " Where `key` in ("
					+ issueIds + ")"
					: "";
			groupby = " group by `key`";

			statement = conn.prepareStatement(query + where + groupby);
			System.out.println(query + where + groupby);

			result = statement.executeQuery();
			String taskId = "", comments = "";

			while (result.next()) {
				taskId = result.getString(1);
				comments = result.getString(2);

				for (Task tas : tasks) {
					if (tas.taskId.equals(taskId)) {
						tas.comments = comments;
						break;
					}
				}

			}

		} catch (SQLException e) {
			System.err
					.println("ERROR: Getting getIssueDescription for commit id ["
							+ issueIds + "]");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return tasks;
	}

	public List<String> getCommitGraphForFile(String file, String commitIds) {
		// TODO Auto-generated method stub

		/*
		 * Select filename from mylyn_file f inner join ( Select mc1.id,
		 * mc1.date from mylyn_commit mc1 inner join mylyn_file mf on
		 * mf.commit_id = mc1.id where mf.filename =
		 * 'org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/MylarJavaPlugin.java'
		 * and mc1.date < (Select max(date) from mylyn_commit where id in
		 * (6037)) and mc1.id not in (6037) order by mc1.date desc limit 1) q on
		 * f.commit_id = q.id;
		 */

		ResultSet result = null;
		List<String> tasks = new ArrayList();
		try {
			// String query = "SELECT `key`, description from " + prefix +
			// "_issue ";

			String query = "Select filename from " + prefix + "_file f  "
					+ "inner join ( Select mc1.id from " + prefix
					+ "_commit mc1 " + "inner join " + prefix
					+ "_file mf on mf.commit_id = mc1.id "
					+ "where mf.filename = '" + file + "' "
					+ "and mc1.date < (Select max(date) from " + prefix
					+ "_commit where id in (" + commitIds + ")) "
					+ "and mc1.id not in (" + commitIds
					+ ") order by mc1.date desc limit 1) q "
					+ "on f.commit_id = q.id;";

			statement = conn.prepareStatement(query);
			// System.out.println(query);

			result = statement.executeQuery();

			Task task;

			while (result.next()) {
				tasks.add(result.getString(1));
			}

		} catch (SQLException e) {
			System.err
					.println("ERROR: Getting getCommitGraphForFile for file id ["
							+ file + "] ");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return tasks;
	}

	public List<String> getSortedAllFilesList() {
		// TODO Auto-generated method stub
		ResultSet result = null;
		List<String> allFiles = new ArrayList();
		try {

			String query = "Select distinct binary filename from " + prefix
					+ "_file f  " + " order by filename ";

			statement = conn.prepareStatement(query);

			result = statement.executeQuery();

			while (result.next()) {
				allFiles.add(result.getString(1));
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting getSortedAllFilesList");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return allFiles;
	}

	public String[][] getAdjancyData() {

		ResultSet result = null;
		String[][] array = null;
		try {

			String query = "Select f1.filename, f2.filename from " + prefix
					+ "_file f1 inner join " + prefix + "_file f2 "
					+ " on f1.commit_id = f2.commit_id "
					+ " order by f1.filename ";

			statement = conn.prepareStatement(query);

			result = statement.executeQuery();

			result.last();
			int totalRows = result.getRow();
			result.beforeFirst();
			array = new String[totalRows][2];

			int i = 0;
			while (result.next()) {
				array[i][0] = result.getString(1);
				array[i][1] = result.getString(2);
				i++;
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting getSortedAllFilesList");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return array;
	}

	public Map getCommonAdjacentFiles(String fileNames, String taskId) {
		Map results = new HashMap<String, List<String>>();
		ResultSet result = null;
		ArrayList array = null;
		List<String> intersectionFiles = null;
		String query = "", query1 = "";
		try {

			query = "select max(c1.date) from " + prefix + "_commit c1 "
					+ "inner join " + prefix
					+ "_issue2commit i1 on i1.commit_id = c1.id "
					+ "inner join " + prefix
					+ "_issue i2 on i2.id = i1.issue_id " + "where i2.key = "
					+ taskId;

			// System.out.println(query);

			statement = conn.prepareStatement(query);
			result = statement.executeQuery();

			Date date = null;

			while (result.next()) {
				date = result.getDate(1);
			}

			query1 = "Select f1.filename, f2.filename, count(f1.commit_id) from "
					+ prefix
					+ "_file f1 inner join "
					+ prefix
					+ "_file f2 "
					+ " on f1.commit_id = f2.commit_id "
					+ " inner join "
					+ prefix
					+ "_commit c1 on c1.id = f1.commit_id and c1.date between DATE_SUB('"
					+ date
					+ "', INTERVAL 6 MONTH) and '"
					+ date
					+ "' inner join "
					+ prefix
					+ "_commit c2 on c2.id = f2.commit_id and c2.date between DATE_SUB('"
					+ date
					+ "', INTERVAL 6 MONTH) and '"
					+ date
					+ "' where f1.filename in("
					+ fileNames
					+ ")"
					+ " group by f1.filename, f2.filename ";
					//+ " order by 3 desc limit 200 ";

			LuceneIndexer.LOGGER.info(query1);

			statement = conn.prepareStatement(query1);
			result = statement.executeQuery();
			String fileName;

			intersectionFiles = new ArrayList();

			while (result.next()) {

				fileName = result.getString(1);

				
				 if (results.get(fileName) == null) results.put(fileName, new
						 ArrayList());
				  
				  ((ArrayList) results.get(fileName)).add(result.getString(2));
				 
				//intersectionFiles.add(result.getString(2));
			}

			if (results.isEmpty())
				return null;

			for (Object keys : results.keySet()) {

				// intersectionFiles.addAll((Collection<? extends String>)
				// results.get(keys));

				/*
				 * if (intersectionFiles == null) { intersectionFiles =
				 * (ArrayList) results.get(keys); continue; } intersectionFiles
				 * = (ArrayList) Utils.intersection( intersectionFiles,
				 * (ArrayList) results.get(keys));
				 */
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting getSortedAllFilesList");
			System.err.println(query + " : \n" + query1);
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return results;
	}
}