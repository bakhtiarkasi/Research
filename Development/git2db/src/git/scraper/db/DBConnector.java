package git.scraper.db;

import git.scraper.pojo.Action;
import git.scraper.pojo.Commit;
import git.scraper.pojo.FileData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.select.Evaluator.IsEmpty;

import git.scraper.pojo.Issue;
import git.scraper.pojo.Comment;
import rleano.util.PropUtil;
import rleano.util.Util;

public class DBConnector {

	private String url;
	private Connection conn = null;
	private ResultSet set = null;
	private PreparedStatement statement = null;

	private String prefix = null;

	public DBConnector(PropUtil props) {
		this.url = "jdbc:mysql://cse.unl.edu";
		this.url += "/" + props.get("database");
		this.url += "?user=" + props.get("username");
		this.url += "&password=" + props.get("password");
		this.prefix = props.get("table.prefix");
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

	public int insertCommit(Commit commit, String comment, String projectName) {
		ResultSet keys = null;
		try {
			String query = "INSERT INTO " + prefix + "_commit VALUES (0, ?, ?, ?, ?, ?, ?, ?, ?)";

			statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, commit.getHash());
			statement.setString(2, projectName);
			statement.setString(3, commit.getAuthorDate());

			statement.setString(4, commit.getAuthor());
			statement.setString(5, commit.getAuthorMail());
			statement.setString(6, commit.getCommitter());
			statement.setString(7, commit.getCommitterMail());
			statement.setString(8, comment);

			statement.executeUpdate();
			keys = statement.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				return id;
			}

		} catch (SQLException e) {
			System.err.println("ERROR Inserting commit " + commit.getHash());
			e.printStackTrace();
		} finally {
			try {
				keys.close();
			} catch (Exception e) {
			}
		}
		return -1;

	}

	public int insertFile(String file, String hash, Integer commit_id, String extension) {
		ResultSet keys = null;
		try {
			String query = "INSERT INTO " + prefix + "_file VALUES (0, ?, ?, ?)";

			statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, commit_id);
			statement.setString(2, file);
			statement.setString(3, extension);

			statement.executeUpdate();
			keys = statement.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				return id;
			}

		} catch (SQLException e) {
			System.err.println("ERROR Inserting file [" + file + "] for commit " + hash);
			e.printStackTrace();
		} finally {
			try {
				keys.close();
			} catch (Exception e) {
			}
		}
		return -1;

	}

	public int insertParent(int commit_id, String hash, String parent) {
		ResultSet keys = null;
		try {
			String query = "INSERT INTO " + prefix + "_parent VALUES (0, ?, ?)";

			statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, commit_id);
			statement.setString(2, parent);

			statement.executeUpdate();
			keys = statement.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				return id;
			}

		} catch (SQLException e) {
			System.err.println("ERROR Inserting parent [" + parent + "] for commit " + hash);
			e.printStackTrace();
		} finally {
			try {
				keys.close();
			} catch (Exception e) {
			}
		}
		return -1;

	}

	public List<FileData> getFiledata(String[] extensions, String initDate, String endDate, boolean desc) {
		ResultSet result = null;
		List<FileData> filedatas = new ArrayList<FileData>();
		String query = "";
		try {
			String select = "SELECT " + prefix + "_commit.id, hash, filename, author, committer, " + prefix + "_commit.date ";
			String from = "FROM " + prefix + "_file JOIN " + prefix + "_commit ON (" + prefix + "_file.commit_id = " + prefix + "_commit.id) ";
			String where = " WHERE ";
			if (extensions != null && extensions.length > 0) {
				where += getExtensionsClause(extensions) + " ";
				where += " AND ";
			}
			where += "STRCMP('" + initDate + "', " + prefix + "_commit.date) < 0 AND STRCMP('" + endDate + "', " + prefix + "_commit.date) > 0 ";
			String order = "ORDER BY " + prefix + "_commit.date ";
			if (desc == true) {
				order += " DESC";
			}

			query = select + from + where + order;

			statement = conn.prepareStatement(query);
//			statement = conn.prepareStatement(select + from);

			result = statement.executeQuery();

			while (result.next()) {
				FileData data = new FileData();
				data.setCommitId(result.getInt(1));
				data.setCommitHash(result.getString(2));
				data.setIssueId(0);
				data.setIssueKey("NONE");
				data.setFilename(result.getString(3));
				data.setAuthor(result.getString(4));
				data.setCommitter(result.getString(5));
				data.setDate(result.getString(6));
				filedatas.add(data);
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting FileData [" + query + "]");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return filedatas;
	}

	public List<FileData> getFiledata(int start, int step, String[] extensions, String initDate, String endDate, boolean desc) {
		ResultSet result = null;
		List<FileData> filedatas = new ArrayList<FileData>();
		String query = "";
		try {
			String select = "SELECT " + prefix + "_commit.id, hash, filename, author, committer, " + prefix + "_commit.date ";
			String from = "FROM " + prefix + "_file JOIN " + prefix + "_commit ON (" + prefix + "_file.commit_id = " + prefix + "_commit.id) ";
			String where = " WHERE ";
			if (extensions != null && extensions.length > 0) {
				where += getExtensionsClause(extensions) + " ";
				where += " AND ";
			}
			where += "STRCMP('" + initDate + "', " + prefix + "_commit.date) < 0 AND STRCMP('" + endDate + "', " + prefix + "_commit.date) > 0 ";
			String order = "ORDER BY " + prefix + "_commit.date ";
			if (desc == true) {
				order += " DESC ";
			}

			String limit = "LIMIT ?,? ";

			query = select + from + where + order + limit;

			statement = conn.prepareStatement(query);
			statement.setInt(1, start);
			statement.setInt(2, step);

			result = statement.executeQuery();

			while (result.next()) {
				FileData data = new FileData();
				data.setCommitId(result.getInt(1));
				data.setCommitHash(result.getString(2));
				data.setIssueId(0);
				data.setIssueKey("NONE");
				data.setFilename(result.getString(3));
				data.setAuthor(result.getString(4));
				data.setCommitter(result.getString(5));
				data.setDate(result.getString(6));
				filedatas.add(data);
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting FileData [" + query + "]");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return filedatas;
	}

	public List<String> getFilesByYearMonth(Integer year, String month, String[] extensions) {
		ResultSet result = null;
		List<String> files = new ArrayList<String>();
		String query = "";
		try {

			String select = "SELECT DISTINCT filename FROM " + prefix + "_file JOIN " + prefix + "_commit ON " + prefix + "_commit.id = " + prefix
					+ "_file.commit_id ";
			String where = "WHERE " + prefix + "_commit.date LIKE ? ";

			if (extensions != null && extensions.length > 0) {
				where += " AND ";
				where += getExtensionsClause(extensions);
			}
			String order = " ORDER BY filename";
			query = select + where + order;

			statement = conn.prepareStatement(query);
			statement.setString(1, year + "-" + month + "%");

			result = statement.executeQuery();

			while (result.next()) {
				files.add(result.getString(1));
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting filenames [" + query + "]");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return files;
	}

	private String getExtensionsClause(String[] extensions) {
		String result = "extension IN (";
		for (String extension : extensions) {
			result += "'" + extension + "', ";
		}
		return result.substring(0, result.length() - 2) + ")";
	}

	public List<Integer> getCommitIdByYearMonth(Integer year, String month) {
		ResultSet result = null;
		List<Integer> ids = new ArrayList<Integer>();
		try {
			String query = "SELECT " + prefix + "_commit.id FROM " + prefix + "_commit ";
			String where = "WHERE " + prefix + "_commit.date LIKE ?";

			statement = conn.prepareStatement(query + where);
			statement.setString(1, year + "-" + month + "%");

			result = statement.executeQuery();

			while (result.next()) {
				ids.add(result.getInt(1));
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting commit ids for date [" + year + "-" + month + "]");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return ids;
	}

	public List<String> getFilesByCommit(Integer id) {
		ResultSet result = null;
		List<String> files = new ArrayList<String>();
		try {
			String query = "SELECT filename FROM " + prefix + "_file JOIN " + prefix + "_commit ON " + prefix + "_commit.id = " + prefix
					+ "_file.commit_id ";
			String where = "WHERE " + prefix + "_commit.id = ? ORDER BY filename";

			statement = conn.prepareStatement(query + where);
			statement.setInt(1, id);

			result = statement.executeQuery();

			while (result.next()) {
				files.add(result.getString(1));
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting files for commit id [" + id + "]");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return files;
	}

	public FileData getPastAuthorsFrequency(FileData data) {
		ResultSet result = null;
		try {

			String select = "SELECT COUNT(DISTINCT author), COUNT(DISTINCT committer), COUNT(filename) ";
			String from = "FROM `" + prefix + "_file` JOIN `" + prefix + "_commit` ON (" + prefix + "_file.commit_id = " + prefix + "_commit.id) ";
			String where = "WHERE `filename` LIKE ? AND STRCMP(?, `date`) <> -1;";

			statement = conn.prepareStatement(select + from + where);

			statement.setString(1, data.getFilename());
			statement.setString(2, data.getDate());

			result = statement.executeQuery();

			while (result.next()) {
				data.setAuthors(result.getInt(1));
				data.setCommitters(result.getInt(2));
				data.setFrequency(result.getInt(3));
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting hashes");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return data;
	}

	public int insertFileData(FileData data) {
		ResultSet keys = null;
		try {
			//                                                            1  2  3  4  5  6  7  8  9 10 11 12 13 14 15  16   17 
			String query = "INSERT INTO `" + prefix + "_filedata` values (0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)";
			//                                                               1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14       15

			System.out.println("\t\t\t" + query);

			statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, data.getCommitId());
			statement.setString(2, data.getCommitHash());
			statement.setInt(3, data.getIssueId());
			statement.setString(4, data.getIssueKey());
			statement.setString(5, data.getFilename());
			statement.setString(6, data.getAuthor());
			statement.setString(7, data.getCommitter());
			statement.setString(8, data.getDate());
			statement.setInt(9, data.getAuthors());
			statement.setInt(10, data.getCommitters());
			statement.setInt(11, data.getLoc());
			statement.setInt(12, data.getCode());
			statement.setInt(13, data.getComment());
			statement.setFloat(14, data.getCommentRatio());
			statement.setInt(15, data.getFrequency());

			statement.executeUpdate();
			keys = statement.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				return id;
			}

		} catch (SQLException e) {
			System.err.println("Filedata: " + data.getCommitHash() + " -- " + data.getFilename());
			e.printStackTrace();
		} finally {
			try {
				keys.close();
			} catch (Exception e) {
			}
		}
		return -1;

	}

	public boolean checkFileData(FileData data) {
		ResultSet result = null;
		try {
			String query = "SELECT 1 FROM `" + prefix + "_filedata` WHERE `commit_hash` LIKE ? AND `filename` LIKE ?;";
			statement = conn.prepareStatement(query);

			statement.setString(1, data.getCommitHash());
			statement.setString(2, data.getFilename());

			result = statement.executeQuery();

			return result.next();

		} catch (SQLException e) {
			System.err.println("ERROR: checking ");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return false;
	}

	public int updateCentrality(String file, String date, float value) {
		String query = "";

		try {
			String update = "UPDATE " + prefix + "_filedata SET centrality = ? ";
			String where = "WHERE filename LIKE ? AND `date` LIKE ?";
			query = update + where;

			statement = conn.prepareStatement(query);
			statement.setFloat(1, value);
			statement.setString(2, file);
			statement.setString(3, date + "%");

			int rows = statement.executeUpdate();
			return rows;

		} catch (SQLException e) {
			System.err.println("ERROR: Updating centrality [" + query + "]");
			e.printStackTrace();
		} finally {
			try {

			} catch (Exception e) {
			}
		}

		return -1;

	}

	public String getCentralityQuery(String file, String date, float value) {
		String query = "";

		String update = "UPDATE " + prefix + "_filedata SET centrality = " + value;
		String where = "WHERE filename LIKE '" + file + "' AND date LIKE '" + date + "%'";
		query = update + where;

		return query;

	}

	public int updateComplexity(String file, String date, float complexity) {
		String query = "";

		try {
			String update = "UPDATE " + prefix + "_filedata SET complexity = ? ";
			String where = "WHERE filename LIKE ? AND `date` LIKE ?";
			query = update + where;

			statement = conn.prepareStatement(query);
			statement.setFloat(1, complexity);
			statement.setString(2, file);
			statement.setString(3, date + "%");

			int rows = statement.executeUpdate();
			return rows;

		} catch (SQLException e) {
			System.err.println("ERROR: Updating complexity [" + query + "]");
			e.printStackTrace();
		} finally {
			try {

			} catch (Exception e) {
			}
		}

		return -1;

	}
	
	public static void main(String[] args) {
		PropUtil props = new PropUtil("config.properties");
		DBConnector db = new DBConnector(props);
		db.createConnection();
		
		boolean test = db.testConnection();
		
		db.close();
		
		if (test == true) {
			System.out.println("OK");
		} else {
			System.out.println("Error");
		}
	}

	private boolean testConnection() {
		String query = "SELECT SYSDATE() FROM DUAL";
		try {

			statement = conn.prepareStatement(query);

			statement.executeQuery();

		} catch (Exception e) {
			System.err.println("ERROR: Testing connection [" + query + "]");
			e.printStackTrace();
			return false;
		} 
		
		return true;
	}
	
	// ---
	
	public int insertComment(Comment comment, int issueId) {
		ResultSet keys = null;
		try {
			statement = conn.prepareStatement("insert into " + prefix + "_comment values (0, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, issueId);
			statement.setString(2, comment.getAuthor());
			statement.setString(3, comment.getDate());
			statement.setString(4, comment.getComment());

			statement.executeUpdate();
			keys = statement.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				return id;
			}
		} catch (SQLException e) {
			System.err.println("Comment for Issue: " + issueId);
			e.printStackTrace();
		} finally {
			try {
				keys.close();
			} catch (Exception e) {
			}
		}
		return -1;

	}
	
	public int insertIssue(Issue issue) {
		ResultSet keys = null;
		try {
			statement = conn.prepareStatement("INSERT INTO " + prefix + "_issue VALUES (0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, issue.getId());
			statement.setString(2, issue.getTitle());
			statement.setString(3, issue.getDescription());
			statement.setString(4, issue.getReporterName());
			statement.setString(5, issue.getAssigneeName());
			statement.setString(6, issue.getComponent());
			statement.setString(7, issue.getDateIssued());
			statement.setString(8, issue.getDateSolved());
			statement.setString(9, issue.getStatus());
			statement.setString(10, issue.getPriority());
			statement.setString(11, issue.getResolution());
			statement.setString(12, issue.getReporter());
			statement.setString(13, issue.getAssignee());

			statement.executeUpdate();
			keys = statement.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				return id;
			}
		} catch (SQLException e) {
			System.err.println("Issue: " + issue.getId());
			e.printStackTrace();
		} finally {
			try {
				keys.close();
			} catch (Exception e) {
			}
		}
		return -1;
	}

	public int updateIssue(String hash, Integer issueId, String issueKey) {
		String query = "";

		try {
			String update = "UPDATE " + prefix + "_filedata SET issue_id = ?, issue_key = ? ";
			String where = "WHERE commit_hash LIKE ?";
			query = update + where;

			statement = conn.prepareStatement(query);
			statement.setInt(1, issueId);
			statement.setString(2, issueKey);
			statement.setString(3, hash + "%");

			int rows = statement.executeUpdate();
			return rows;

		} catch (SQLException e) {
			System.err.println("ERROR: Updating complexity [" + query + "]");
			e.printStackTrace();
		} finally {
			try {

			} catch (Exception e) {
			}
		}

		return -1;
	}

	public int createAction(String issueCode, Action action) {
		ResultSet keys = null;
		try {
			
			String query = "INSERT INTO " + prefix + "_actions VALUES (0, ?, ?, ?, ?, ?, ?, ?)";
			statement = conn.prepareStatement(query,
					Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, issueCode);
			statement.setString(2, action.getAuthor());
			statement.setString(3, action.getDate());
			statement.setString(4, action.getAction());
			statement.setString(5, action.getField());
			statement.setString(6, action.getOldValue());
			statement.setString(7, action.getNewValue());

			statement.executeUpdate();
			keys = statement.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				return id;
			}
		} catch (SQLException e) {
			System.err.println("ERROR: While creating action for [" + issueCode + ", " + action.getAction() + "]");
			e.printStackTrace();
		} finally {
			try {
				keys.close();
			} catch (Exception e) {
			}
		}
		return -1;
		
	}
	
	public int getIssueId(String key) {
		ResultSet result = null;
		int id = -1;
		try {
			String query = "SELECT " + prefix + "_issue.id FROM " + prefix + "_issue ";
			String where = "WHERE " + prefix + "_issue.key LIKE ?";

			statement = conn.prepareStatement(query + where);
			statement.setString(1, key);

			result = statement.executeQuery();

			while (result.next()) {
				id = result.getInt(1);
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting id for issue [" + key + "]");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return id;
	}
	
	
	public List<String> getAllIssueKeys() {
		ResultSet result = null;
		List<String> issues = new ArrayList();
		try {
			String query = "SELECT `key` FROM " + prefix + "_issue order by `key`";
			
			statement = conn.prepareStatement(query);
			result = statement.executeQuery();

			while (result.next()) {
				issues.add(result.getString(1));
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting getAllIssueKeys() method");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return issues;
	}
	
	
	
	public List<Issue> getIssueDescription(String issueIds) {
		ResultSet result = null;
		List<Issue> files = new ArrayList<Issue>();
		try {
			String query = "SELECT `key`, description " + prefix + "_issue ";
			String where = (issueIds != null && !issueIds.isEmpty()) ? " Where `key` in(" + issueIds + ")" : "";
			
			statement = conn.prepareStatement(query + where);
			result = statement.executeQuery();
			
			Issue issue;

			while (result.next()) {
				issue = new Issue();
				issue.setId(result.getString(1));
				issue.setDescription(result.getString(2));
				files.add(issue);
			}

		} catch (SQLException e) {
			System.err.println("ERROR: Getting getIssueDescription for commit id [" + issueIds + "]");
			e.printStackTrace();
		} finally {
			try {
				result.close();
			} catch (Exception e) {
			}
		}

		return files;
	}

	public void updateCommitLinkage() {
		String query = "";

		try {
						
			String update = "INSERT INTO " + prefix + "_issue2commit(`issue_id`,`commit_id`) ";
			update += "Select  c1.id, c2.id from " + prefix + "_issue c1 ";
			String where = "inner join  " + prefix + "_commit c2 ";
			where += "on c2.comment REGEXP CONCAT('[[:<:]]',c1.KEY,'[^0-9]*[[:>:]]') ";
			where += "order by c1.key";
			query = update + where;

			//System.out.println(query);
			
			statement = conn.prepareStatement(query);
			statement.executeUpdate();
			
		} catch (SQLException e) {
			System.err.println("ERROR: updateCommitLinkage [" + query + "]");
			e.printStackTrace();
		} finally {
			try {

			} catch (Exception e) {
			}
		}
		// TODO Auto-generated method stub
		
		
	}

}