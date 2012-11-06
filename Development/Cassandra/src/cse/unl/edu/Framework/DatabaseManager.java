package cse.unl.edu.Framework;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import cse.unl.edu.Scheduler.Conflict;
import cse.unl.edu.Scheduler.Developer;
import cse.unl.edu.Scheduler.Task;
import cse.unl.edu.Scheduler.TaskScheduler;

public class DatabaseManager {

	private static final String dbHost = "localhost";
	private static final String dbName = "arsonae";
	private static final String dbUsername = "root";
	private static final String dbPassword = "";

	private static Connection conn = null;
	private Statement stmt = null;

	public static void addEvent() {
		try {
			if (conn == null || conn.isClosed())
				conn = DatabaseManager.getConnection();

			conn.createStatement().executeUpdate("Use " + dbName);
			CallableStatement proc = conn
					.prepareCall("{ call getAllEvents() }");
			ResultSet rs = proc.executeQuery();

			proc.close();
			conn.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String sourceURL = "jdbc:mysql://" + DatabaseManager.dbHost;
			return DriverManager.getConnection(sourceURL,
					DatabaseManager.dbUsername, DatabaseManager.dbPassword);

		} catch (IllegalAccessException iae) {
		} catch (InstantiationException ie) {
		} catch (ClassNotFoundException cnfe) {
			System.out.println(cnfe.getMessage());
		} catch (SQLException sqle) {
		}
		return null;
	}

	/*****************************************************
	 * SIMULATOR
	 * METHODS***************************************************************
	 * 
	 *************************************************************************************************************************************/

	public static Connection getSimulatorDBConnection() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String sourceURL = "jdbc:mysql://localhost:3306/mytask";
			conn = DriverManager.getConnection(sourceURL,
					DatabaseManager.dbUsername, DatabaseManager.dbPassword);

		} catch (IllegalAccessException iae) {
			System.out.println(iae.getMessage());
		} catch (InstantiationException ie) {
			System.out.println(ie.getMessage());
		} catch (ClassNotFoundException cnfe) {
			System.out.println(cnfe.getMessage());
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		}
		return conn;
	}

	public static String[] getUsers() {
		String[] users = null;
		try {
			if (conn == null || conn.isClosed())
				conn = DatabaseManager.getSimulatorDBConnection();

			conn.createStatement().executeUpdate("Use mytask");
			PreparedStatement proc = conn
					.prepareStatement("Select distinct author from alltasks");
			ResultSet rs = proc.executeQuery();
			rs.last();
			users = new String[rs.getRow()];
			int index = 0;
			rs.first();
			do {
				users[index++] = rs.getString(1);
				rs.next();
			} while (!rs.isAfterLast());

			proc.close();
			conn.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return users;
	}

	public static HashSet<String> getFiles(int count) {
		HashSet<String> files = null;
		try {
			if (conn == null || conn.isClosed())
				conn = DatabaseManager.getSimulatorDBConnection();

			conn.createStatement().executeUpdate("Use mytask");
			PreparedStatement proc = conn
					.prepareStatement("SELECT distinct SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1) as filenames from event LIMIT "
							+ count);
			ResultSet rs = proc.executeQuery();
			rs.last();

			files = new HashSet<String>(rs.getRow());

			int index = 0;
			rs.first();
			do {
				files.add(rs.getString(1));
				rs.next();
			} while (!rs.isAfterLast());

			proc.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return files;
	}

	public static int createNewSession(Connection conn2, String source,
			String description, Date startDate, Date endDate, int taskStartId,
			int taskEndId, int p_Devs, int p_avgTask, int p_avgFile,
			float p_MF, float p_BF, float p_TF, String p_DR)
			throws SQLException {
		int sessionId = 0;

		CallableStatement cs = conn2
				.prepareCall("{ call cass.SP_InsertIntoSession(?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		cs.setString(1, source);
		cs.setString(2, description);
		cs.setDate(3, startDate);
		cs.setDate(4, endDate);
		cs.setInt(5, taskStartId);
		cs.setInt(6, taskEndId);
		cs.setInt(7, p_Devs);
		cs.setInt(8, p_avgTask);
		cs.setInt(9, p_avgFile);
		cs.setFloat(10, p_MF);
		cs.setFloat(11, p_BF);
		cs.setFloat(12, p_TF);
		cs.setString(13, p_DR);
		boolean hadResults = cs.execute();

		if (hadResults) {
			ResultSet rs = cs.getResultSet();
			if (rs.next())
				sessionId = rs.getInt("lastId");

			rs.close();
		}

		cs.close();

		return sessionId;
	}

	public static int insertTask(Connection conn2, int sessionId, int wsId,
			String taskDescription, Date date, boolean clean)
			throws SQLException {

		int taskId = 0;

		CallableStatement cs = conn2
				.prepareCall("{ call cass.SP_InsertIntoTasks(?,?,?,?,?)}");

		cs.setInt("pFkSessionID", sessionId);
		cs.setInt("pWS_ID", wsId);
		cs.setString("pDescription", taskDescription);
		cs.setDate("pDate", date);
		cs.setBoolean("pIsClean", clean);

		boolean hadResults = cs.execute();

		if (hadResults) {
			ResultSet rs = cs.getResultSet();
			if (rs.next())
				taskId = rs.getInt("lastId");

			rs.close();
		}

		cs.close();

		return taskId;
	}

	public static void assignFilestoTask(Connection conn2, int taskId,
			HashSet<String> files) throws SQLException {

		CallableStatement cs = conn2
				.prepareCall("{ call cass.SP_InsertIntoTasks2File(?,?)}");

		String fileNames = "";

		for (String fileName : files) {

			fileNames += fileName;
			fileNames += ",";
		}

		fileNames = fileNames.substring(0, fileNames.length() - 1);

		cs.setInt("pFkTaskID", taskId);
		cs.setString("pFileNames", fileNames);

		cs.executeUpdate();

	}

	public static void assignTaskstoDev(Connection conn2, String name,
			HashSet<Integer> userTaskIds) throws SQLException {

		CallableStatement cs = conn2
				.prepareCall("{ call cass.SP_InsertIntoTask2User(?,?)}");

		String taskIds = "";

		for (int taskId : userTaskIds) {

			taskIds += taskId;
			taskIds += ",";
		}

		taskIds = taskIds.substring(0, taskIds.length() - 1);

		cs.setString("pFkUserName", name);
		cs.setString("pTaskIds", taskIds);

		cs.executeUpdate();

	}

	public static void updateSession(Connection conn2, int sessionId,
			Date date, int firstTask, int lastTask) throws SQLException {

		CallableStatement cs = conn2
				.prepareCall("{ call cass.SP_UpdateSessionByID(?,?,?,?)}");

		cs.setInt("pPkSessionID", sessionId);
		cs.setDate("pEndTime", date);
		cs.setInt("pTaskStartId", firstTask);
		cs.setInt("pTaskEndId", lastTask);

		cs.executeUpdate();

	}

	public static void insertTaskConflicts(Connection conn2, int sessionId,
			String type, String conflicts) throws SQLException {

		CallableStatement cs = conn2
				.prepareCall("{ call cass.SP_InsertIntoConflicts(?,?,?)}");

		cs.setInt("pSessionId", sessionId);
		cs.setString("pType", type);
		cs.setString("pConflictIDs", conflicts);

		cs.executeUpdate();

	}

	public static String callTestCase(int sessionId, int allTasksCount,
			int allFilesCount, int devsCount, int totalMergeConflicts,
			int totalBuildConflicts, int totalTestConflicts, int totalConflicts)
			throws SQLException {

		String status = "";

		try {
			if (conn == null || conn.isClosed())
				conn = DatabaseManager.getSimulatorDBConnection();

			CallableStatement cs = conn
					.prepareCall("{ call cass.SP_UnitTestResults(?,?,?,?,?,?,?,?)}");

			// cs.setInt("pSessionId", sessionId);
			// cs.setString("pType", type);
			// cs.setString("pConflictIDs", conflicts);

			cs.setInt("pSessionId", sessionId);
			cs.setInt("pAllTasksCount", allTasksCount);
			cs.setInt("pAllFilesCount", allFilesCount);
			cs.setInt("pAllDevsCount", devsCount);
			cs.setInt("pTotalMergeConflicts", totalMergeConflicts);
			cs.setInt("pTotalBuildConflicts", totalBuildConflicts);
			cs.setInt("pTotalTestConflicts", totalTestConflicts);
			cs.setInt("pTotalConflicts", totalConflicts);

			ResultSet rs = cs.executeQuery();
			do {
				rs = cs.getResultSet();

			} while (cs.getMoreResults(Statement.KEEP_CURRENT_RESULT));

			if (rs.next()) {
				rs.last();
				status = rs.getString("Status");

			}

			cs.close();

		} catch (SQLException e) {

			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}

		}
		return status;

	}

	public static void populateConflictsforSession(int sessionId,
			TaskScheduler obj) throws SQLException {

		try {
			if (conn == null || conn.isClosed())
				conn = DatabaseManager.getSimulatorDBConnection();

			CallableStatement cs = conn
					.prepareCall("{ call cass.SP_GetDataforScheduler(?)}");

			cs.setInt("pfkSessionId", sessionId);

			ResultSet rs;
			cs.execute();

			// getting the first result set
			rs = cs.getResultSet();
			int pkUserId = 0;
			int pktaskid = 0;
			Task task = null;
			Developer dev = null;
			int devAutoId = 1;

			while (rs.next()) {

				task = new Task(rs.getInt("pktaskid"), rs.getInt("ws_Id"),
						rs.getString("Description"), rs.getInt("Preference"),
						rs.getBoolean("isclean"));

				pkUserId = rs.getInt("pkUserId");
				dev = obj.getDeveloper(pkUserId, -1,-1, null);

				if (dev == null) {
					dev = new Developer(pkUserId, rs.getInt("UID"),devAutoId,
							rs.getString("Name"));
					devAutoId++;
					obj.addNewDeveloper(dev);
				}

				dev.addTaskForUser(task.getWsId());
				obj.addNewTask(task);

			}

			// close the first result set and point rs to the second
			// Update the files for each task over here
			cs.getMoreResults();
			rs = cs.getResultSet();
			while (rs.next()) {
				pktaskid = rs.getInt("pktaskid");
				task = obj.getTask(pktaskid, -1);
				task.addFiletoTask(rs.getString("Name"));
			}

			// close the second result set and point rs to the third
			cs.getMoreResults();
			rs = cs.getResultSet();
			Conflict conf = null;
			while (rs.next()) {

				conf = new Conflict(sessionId, rs.getInt("task1"),
						rs.getInt("task2"), rs.getString("Type"),
						rs.getString("Direction"));
				obj.addNewConflict(conf);

			}

			rs.close();
			cs.close();

		} catch (SQLException e) {

			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}

		}

	}

}
