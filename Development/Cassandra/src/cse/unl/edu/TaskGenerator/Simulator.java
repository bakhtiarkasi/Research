package cse.unl.edu.TaskGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cse.unl.edu.Framework.Assignment;
import cse.unl.edu.Framework.DatabaseManager;
import cse.unl.edu.Framework.Assignment.Developer;
import cse.unl.edu.Framework.Assignment.Task;
import cse.unl.edu.util.Pair;
import cse.unl.edu.util.Utils;

import org.apache.log4j.Logger;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

public class Simulator {

	int noOfDevelopers;
	int noOfTasks;
	int noOfFiles;
	int noOfAllFiles;
	int noOfAllTasks;

	float mergeConflictsRatio;
	float testFailuresRatio;
	float buildFailuresRatio;

	int mergeConflictReq;
	int minFilesReq;
	int maxFilesReq;
	int mcFilesNumber;
	int allPossibleConflicts;

	String[] users = null;
	String[] selectedUsers = null;
	String[][] selectedTasks = null;
	HashSet<String> allFiles = null;
	HashSet<String>[] fileAssignments = null;

	static Logger log4j = Logger.getLogger("cse.unl.edu.Framework.Simulator");

	public enum DateRange {
		Daily, Weekly, Monthly
	};

	DateRange dateRange;

	Assignment assignment;

	@SuppressWarnings("unchecked")
	public Simulator(int noOfDevelopers, int noOfTasks, int noOfFiles,
			float mergeConflicts, float testFailures, float buildFailures,
			DateRange dateRange) {
		this.noOfDevelopers = noOfDevelopers;
		this.noOfTasks = noOfTasks;
		this.noOfFiles = noOfFiles;
		this.mergeConflictsRatio = mergeConflicts;
		this.testFailuresRatio = testFailures;
		this.buildFailuresRatio = buildFailures;
		this.dateRange = dateRange;
		this.noOfAllTasks = 0;
		this.allPossibleConflicts = 0;

		// noOfAllFiles = noOfDevelopers * noOfTasks * noOfFiles;
		// selectedUsers = new String[noOfDevelopers];
		// selectedTasks = new String[noOfDevelopers][noOfTasks];

		// fileAssignments = new HashSet[noOfDevelopers];
		// for(int i =0; i<noOfDevelopers; i++)
		// fileAssignments[i] = new HashSet<String>(noOfFiles);

		// mergeConflictReq = Math.round(mergeConflictsRatio * noOfAllFiles);

		assignment = new Assignment(noOfDevelopers);
		selectDevelopers();

		log4j.debug("Initialized Constructor");
	}

	@Override
	public String toString() {
		return "Developers: " + noOfDevelopers
				+ "\nAvg # of tasks per developer: " + noOfTasks
				+ "\nAvg # of files per task: " + noOfFiles
				+ "\nFrequency of conflicts:- MF: " + mergeConflictsRatio
				+ " Build failures: " + buildFailuresRatio + " Test failures: "
				+ testFailuresRatio + "\nDate Range: " + dateRange.toString()
				+ "\n";
	}

	public int getAllPossibleConflicts() {
		return this.allPossibleConflicts;
	}

	public float getMergeConfRatio() {
		return this.mergeConflictsRatio;
	}

	public float getBuildConfRatio() {
		return this.buildFailuresRatio;
	}

	public float getTestConfRatio() {
		return this.testFailuresRatio;
	}

	public boolean startProcess() {

		log4j.debug("In Start Process");

		int wsId = 1;

		for (int i = 1; i <= noOfDevelopers; i++) {
			int noTask = Utils.getNextGaussian(1, noOfTasks, null);

			noOfAllTasks += noTask;

			for (int j = 1; j <= noTask; j++) {
				String taskName = "D" + i + "T" + j;
				Task t = assignment.new Task(j, wsId, taskName);
				assignment.assignTasktoDeveloper(i, t);
				wsId++;
			}
		}

		noOfAllFiles = noOfAllTasks * noOfFiles;
		allFiles = DatabaseManager.getFiles(noOfAllFiles);

		if (noOfAllFiles > allFiles.size()) {
			log4j.error("Files limit exceeded - Exiting");
			return false;

		}

		// assignFiles();

		makeAssignments();

		log4j.debug("Start Process Completed");

		return true;

	}

	private void makeAssignments() {

		log4j.debug("In Make Assignment");

		int noOfPossibleConflicts = getAllPossibleEdes();

		this.allPossibleConflicts = noOfPossibleConflicts;

		float totalConflict = mergeConflictsRatio + buildFailuresRatio
				+ testFailuresRatio;
		int unCleanTasks = Math.round(totalConflict * noOfPossibleConflicts);
		int mergeConfTasks = Math.round(mergeConflictsRatio
				* noOfPossibleConflicts);
		int buildConfTasks = Math.round(buildFailuresRatio
				* noOfPossibleConflicts);
		int testConfTasks = Math.round(testFailuresRatio
				* noOfPossibleConflicts);

		// recalculate ratios:
		mergeConflictsRatio = ((float) mergeConfTasks / noOfPossibleConflicts);
		buildFailuresRatio = ((float) buildConfTasks / noOfPossibleConflicts);
		testFailuresRatio = ((float) testConfTasks / noOfPossibleConflicts);

		int conflicts = mergeConfTasks + buildConfTasks + testConfTasks;

		log4j.debug("MC Files orig " + mergeConfTasks + " all tasks "
				+ noOfPossibleConflicts);
		log4j.debug("BC Files orig " + buildConfTasks + " all tasks "
				+ noOfPossibleConflicts);
		log4j.debug("TC Files orig " + testConfTasks + " all tasks "
				+ noOfPossibleConflicts);

		log4j.debug("Recomputed MergeConflictsRatio " + mergeConflictsRatio);
		log4j.debug("Recomputed BuildFailuresRatio " + buildFailuresRatio);
		log4j.debug("Recomputed TestFailuresRatio " + testFailuresRatio);

		for (int i = 1; i <= noOfDevelopers; i++) {
			Developer d1 = assignment.getDeveloperById(i);
			log4j.debug("Developer " + d1.getId() + " #tasks: "
					+ d1.getNoOfTasks());
		}

		List<Integer> exclude = new ArrayList();
		int number = 0;

		for (int i = 0; i < mergeConfTasks; i++) {
			// randomly select file

			if (exclude.size() >= allFiles.size() - 1)
				exclude.clear();

			number = Utils.getRandomNumber(0, allFiles.size() - 1, exclude);
			exclude.add(number);

			// select first developer randomly
			ArrayList<Integer> excludeDevs = new ArrayList<Integer>(1);
			int dev1Id = Utils.getRandomNumber(1, noOfDevelopers, null);
			int dev2Id = dev1Id;

			while (dev2Id == dev1Id) {
				dev2Id = Utils.getRandomNumber(1, noOfDevelopers, null);
			}

			// select task for developer randomly
			Developer dev1 = assignment.getDeveloperById(dev1Id);
			int task1Id = Utils.getRandomNumber(1, dev1.getNoOfTasks(), null);

			Task task1 = dev1.getTaskById(task1Id);
			task1.addFiletoTask((String) allFiles.toArray()[number]);

			// select task for developer randomly
			Developer dev2 = assignment.getDeveloperById(dev2Id);
			int task2Id = Utils.getRandomNumber(1, dev2.getNoOfTasks(), null);

			Task task2 = dev2.getTaskById(task2Id);
			task2.addFiletoTask((String) allFiles.toArray()[number]);

			if (task1.getMergeConflicts().contains(task2.getWsId())) {
				i--;
				continue;
			}

			// add merge conflict
			task1.addMergeConflictWithTask(task2);

		}
		log4j.debug("Merge Conflicts done");

		for (int i = 0; i < buildConfTasks; i++) {

			if (exclude.size() >= allFiles.size() - 2)
				exclude.clear();

			// randomly select 2 files
			int file1 = Utils.getRandomNumber(0, allFiles.size() - 1, exclude);
			exclude.add(file1);

			int file2 = Utils.getRandomNumber(0, allFiles.size() - 1, exclude);
			exclude.add(file2);

			// select first developer randomly
			int dev1Id = Utils.getRandomNumber(1, noOfDevelopers, null);
			int dev2Id = dev1Id;

			while (dev2Id == dev1Id) {
				dev2Id = Utils.getRandomNumber(1, noOfDevelopers, null);
			}

			// select task for developer randomly
			Developer dev1 = assignment.getDeveloperById(dev1Id);
			int task1Id = Utils.getRandomNumber(1, dev1.getNoOfTasks(), null);

			Task task1 = dev1.getTaskById(task1Id);
			task1.addFiletoTask((String) allFiles.toArray()[file1]);

			// select task for developer randomly
			Developer dev2 = assignment.getDeveloperById(dev2Id);
			int task2Id = Utils.getRandomNumber(1, dev2.getNoOfTasks(), null);

			Task task2 = dev2.getTaskById(task2Id);
			task2.addFiletoTask((String) allFiles.toArray()[file2]);

			if (!task1.addBuildConflictWithTask(task2)) {
				i--;
				continue;
			}

		}
		log4j.debug("Build Conflicts done");

		for (int i = 0; i < testConfTasks; i++) {

			if (exclude.size() >= allFiles.size() - 2)
				exclude.clear();

			// randomly select 2 files
			int file1 = Utils.getRandomNumber(0, allFiles.size() - 1, exclude);
			exclude.add(file1);

			int file2 = Utils.getRandomNumber(0, allFiles.size() - 1, exclude);
			exclude.add(file2);

			// select first developer randomly
			int dev1Id = Utils.getRandomNumber(1, noOfDevelopers, null);
			int dev2Id = dev1Id;

			while (dev2Id == dev1Id) {
				dev2Id = Utils.getRandomNumber(1, noOfDevelopers, null);
			}

			// select task for developer randomly
			Developer dev1 = assignment.getDeveloperById(dev1Id);
			int task1Id = Utils.getRandomNumber(1, dev1.getNoOfTasks(), null);

			Task task1 = dev1.getTaskById(task1Id);
			task1.addFiletoTask((String) allFiles.toArray()[file1]);

			// select task for developer randomly
			Developer dev2 = assignment.getDeveloperById(dev2Id);
			int task2Id = Utils.getRandomNumber(1, dev2.getNoOfTasks(), null);

			Task task2 = dev2.getTaskById(task2Id);
			task2.addFiletoTask((String) allFiles.toArray()[file2]);

			if (!task1.addTestConflictWithTask(task2)) {
				i--;
				continue;
			}
		}

		log4j.debug("Test Conflicts done");

		// HashSet<String> tempFiles = (HashSet<String>) allFiles.clone();
		//
		// for (Integer value : exclude) {
		// Object fileName = tempFiles.toArray()[value];
		// allFiles.remove(fileName);
		// }

		assignRemainingFiles();

		log4j.debug("Make Assigment Completed");

	}

	private int getAllPossibleEdes() {
		log4j.debug("In Get All Possible Conflicts");

		int vertices = noOfAllTasks;

		// edges in complete graph;

		int edges = (vertices * (vertices - 1)) / 2;

		// removing unconnected partite's

		for (int devId = 1; devId <= noOfDevelopers; devId++) {
			Developer dev = assignment.getDeveloperById(devId);
			int devEdges = (dev.getNoOfTasks() * (dev.getNoOfTasks() - 1)) / 2;
			edges -= devEdges;
		}

		log4j.debug("Get All Possible Conflicts Completed");
		return edges;
	}

	private void assignRemainingFiles() {

		log4j.debug("In assign remaining files");

		List<Integer> exclude = new ArrayList();
		int number = 0;

		for (int devId = 1; devId <= noOfDevelopers; devId++) {
			Developer dev = assignment.getDeveloperById(devId);
			for (int taskId = 1; taskId <= dev.getNoOfTasks(); taskId++) {
				Task task = dev.getTaskById(taskId);
				int randomNoFiles = Utils.getNextGaussian(1, noOfFiles, null)
						- task.getFileCount();
				randomNoFiles = randomNoFiles < 0 ? 0 : randomNoFiles;

				for (int i = 0; i < randomNoFiles; i++) {

					number = Utils.getRandomNumber(0, allFiles.size() - 1,
							exclude);
					exclude.add(number);

					if (exclude.size() >= allFiles.size()) {
						exclude.clear();
						log4j.debug("Files Size exceeded");
					}

					task.addFiletoTask((String) allFiles.toArray()[number]);
				}
			}
		}

		log4j.debug("Assign remaing files Completed");

	}

	public String returnOutput() {
		String output = "";

		for (int devId = 1; devId <= noOfDevelopers; devId++) {
			Developer dev = assignment.getDeveloperById(devId);
			output += dev.toString();

			output += "\n\n--------------------------------------------------------------------------------";

			for (int taskId = 1; taskId <= dev.getNoOfTasks(); taskId++) {
				Task task = dev.getTaskById(taskId);
				output += "\n\r" + task.toString();
				output += "\n...............................................................................";
			}

			output += "\n\n================================================================================\n";
		}
		return output;
	}

	public int saveResultstoDB(String sessDescription) throws SQLException {
		int sessionId = 0;

		Connection conn = null;
		try {

			conn = DatabaseManager.getSimulatorDBConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(conn.TRANSACTION_READ_UNCOMMITTED);

			sessionId = DatabaseManager.createNewSession(conn, "TaskGenerator",
					sessDescription,
					new java.sql.Date(System.currentTimeMillis()),
					new java.sql.Date(System.currentTimeMillis()), 0, 0,
					this.noOfDevelopers, this.noOfTasks, this.noOfFiles,
					this.mergeConflictsRatio, this.buildFailuresRatio,
					this.testFailuresRatio, this.dateRange.toString());

			System.out.println("id is " + sessionId);

			int firstTask = 0;
			int lastTask = 0;
			HashSet<Integer> userTaskIds = new HashSet<Integer>();

			conn.commit();

			for (int i = 1; i <= noOfDevelopers; i++) {
				userTaskIds.clear();
				Developer dev = assignment.getDeveloperById(i);
				for (int j = 1; j <= dev.getNoOfTasks(); j++) {

					Task t = dev.getTaskById(j);
					int taskId = DatabaseManager.insertTask(conn, sessionId,
							t.getWsId(), t.getTaskDescription(),
							new java.sql.Date(System.currentTimeMillis()),
							t.isClean());

					if (i == 1 && j == 1)
						firstTask = taskId;
					lastTask = taskId;

					userTaskIds.add(taskId);

					DatabaseManager.assignFilestoTask(conn, taskId, t.getFiles());
					conn.commit();
				}
				DatabaseManager.assignTaskstoDev(conn, dev.getName(), userTaskIds);
				conn.commit();

			}

			String mergeConflicts = getMergeConflictString();
			String buildConflicts[] = getBuildConflictString();
			String testConflicts[] = getTestConflictString();

			if (!mergeConflicts.equals(""))
				DatabaseManager.insertTaskConflicts(conn, sessionId, "MC",
						mergeConflicts);
			if (!buildConflicts[0].equals(""))
				DatabaseManager.insertTaskConflicts(conn, sessionId, "BC",
						buildConflicts[0]);
			if (!buildConflicts[1].equals(""))
				DatabaseManager.insertTaskConflicts(conn, sessionId, "BC",
						buildConflicts[1]);
			if (!testConflicts[0].equals(""))
				DatabaseManager.insertTaskConflicts(conn, sessionId, "TC",
						testConflicts[0]);
			if (!testConflicts[1].equals(""))
				DatabaseManager.insertTaskConflicts(conn, sessionId, "TC",
						testConflicts[1]);

			conn.commit();

			DatabaseManager.updateSession(conn, sessionId, new java.sql.Date(
					System.currentTimeMillis()), firstTask, lastTask);
			conn.commit();

		} catch (SQLException e) {

			if (conn != null) {
				System.out.println("Connection rollback...");
			}
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}

		}

		return sessionId;
	}

	private String getMergeConflictString() {
		HashSet<Pair<Integer, Integer>> mergeConflicts = new HashSet<Pair<Integer, Integer>>();

		for (int i = 1; i <= noOfDevelopers; i++) {
			Developer dev = assignment.getDeveloperById(i);
			for (int j = 1; j <= dev.getNoOfTasks(); j++) {
				Task t = dev.getTaskById(j);
				for (Integer id : t.getMergeConflicts()) {
					if (t.getWsId() < id)
						mergeConflicts.add(new Pair<Integer, Integer>(t.getWsId(),
								id));
				}
			}
		}

		String conflicts = "";
		for (Pair<Integer, Integer> pair : mergeConflicts) {
			conflicts += (pair.first + ":" + pair.second);
			conflicts += ",";
		}

		if(conflicts.length() > 0)
			conflicts = conflicts.substring(0, conflicts.length() - 1);
		
		return conflicts;

	}

	private String[] getBuildConflictString() {
		String[] conflicts;
		conflicts = new String[2];

		HashSet<Pair<Integer, Integer>> buildConflictsRght = new HashSet<Pair<Integer, Integer>>();
		HashSet<Pair<Integer, Integer>> buildConflictsLft = new HashSet<Pair<Integer, Integer>>();

		for (int i = 1; i <= noOfDevelopers; i++) {
			Developer dev = assignment.getDeveloperById(i);
			for (int j = 1; j <= dev.getNoOfTasks(); j++) {
				Task t = dev.getTaskById(j);
				for (Pair<Integer, String> pair : t.getBuildConflicts()) {
					if (t.getWsId() < pair.first) {
						if (pair.second == ">")
							buildConflictsRght.add(new Pair<Integer, Integer>(
									t.getWsId(), pair.first));
						else
							buildConflictsLft.add(new Pair<Integer, Integer>(
									t.getWsId(), pair.first));
					}
				}
			}
		}

		conflicts[0] = "";
		for (Pair<Integer, Integer> pair : buildConflictsRght) {
			conflicts[0] += (pair.first + ":" + pair.second + "|>");
			conflicts[0] += ",";
		}
		if (conflicts[0].length() > 0)
			conflicts[0] = conflicts[0].substring(0, conflicts[0].length() - 1);

		conflicts[1] = "";
		for (Pair<Integer, Integer> pair : buildConflictsLft) {
			conflicts[1] += (pair.first + ":" + pair.second + "|<");
			conflicts[1] += ",";
		}
		if (conflicts[1].length() > 0)
			conflicts[1] = conflicts[1].substring(0, conflicts[1].length() - 1);

		return conflicts;
	}

	private String[] getTestConflictString() {
		String[] conflicts;
		conflicts = new String[2];

		HashSet<Pair<Integer, Integer>> testConflictsRght = new HashSet<Pair<Integer, Integer>>();
		HashSet<Pair<Integer, Integer>> testConflictsLft = new HashSet<Pair<Integer, Integer>>();

		for (int i = 1; i <= noOfDevelopers; i++) {
			Developer dev = assignment.getDeveloperById(i);
			for (int j = 1; j <= dev.getNoOfTasks(); j++) {
				Task t = dev.getTaskById(j);
				for (Pair<Integer, String> pair : t.getTestConflicts()) {
					if (t.getWsId() < pair.first) {
						if (pair.second == ">")
							testConflictsRght.add(new Pair<Integer, Integer>(
									t.getWsId(), pair.first));
						else
							testConflictsLft.add(new Pair<Integer, Integer>(
									t.getWsId(), pair.first));
					}
				}
			}
		}

		conflicts[0] = "";
		for (Pair<Integer, Integer> pair : testConflictsRght) {
			conflicts[0] += (pair.first + ":" + pair.second + "|>");
			conflicts[0] += ",";
		}
		if (conflicts[0].length() > 0)
			conflicts[0] = conflicts[0].substring(0, conflicts[0].length() - 1);

		conflicts[1] = "";
		for (Pair<Integer, Integer> pair : testConflictsLft) {
			conflicts[1] += (pair.first + ":" + pair.second + "|<");
			conflicts[1] += ",";
		}
		if (conflicts[1].length() > 0)
			conflicts[1] = conflicts[1].substring(0, conflicts[1].length() - 1);

		return conflicts;
	}

	public static void main(String[] args) {

		try {

			int noOfDevelopers = 2;
			int noOfTasks = 2;
			int noOfFiles = 2;

			float mergeConflicts = 0.7f;
			float testFailures = 0.25f;
			float buildFailures = 0.05f;

			DateRange dateRange = DateRange.Weekly;
			Simulator obj;

			if (args.length < 1) {
				System.out
						.println("Usage : java ... Simulator -developers<noOfDevelopers> -tasks<noOfTasks> -files<noOfFiles> -frequency<MF:TF:BF> -DateRange<D|W|M>");
				System.exit(1);
			}

			for (int i = 0; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("-developers"))
					noOfDevelopers = Integer.parseInt(args[i + 1]);

				else if (args[i].equalsIgnoreCase("-tasks"))
					noOfTasks = Integer.parseInt(args[i + 1]);

				else if (args[i].equalsIgnoreCase("-files"))
					noOfFiles = Integer.parseInt(args[i + 1]);

				else if (args[i].equalsIgnoreCase("-frequency")) {
					String frequency = args[i + 1];

					mergeConflicts = Float.parseFloat(frequency.split(":")[0]);
					testFailures = Float.parseFloat(frequency.split(":")[1]);
					buildFailures = Float.parseFloat(frequency.split(":")[2]);
				}

				else if (args[i].equalsIgnoreCase("-DateRange")) {
					String charRep = args[i + 1];

					if (charRep.equalsIgnoreCase("M"))
						dateRange = DateRange.Monthly;
					else if (charRep.equalsIgnoreCase("W"))
						dateRange = DateRange.Weekly;
					else
						dateRange = DateRange.Daily;
				}
				i++;
			}

			noOfTasks = Utils.getNextGaussian(1, noOfTasks, null);
			noOfFiles = Utils.getNextGaussian(1, noOfFiles, null);

			obj = new Simulator(noOfDevelopers, noOfTasks, noOfFiles,
					mergeConflicts, testFailures, buildFailures, dateRange);
			System.out.print(obj);
			if (obj.startProcess()) {
				System.out.println("Saving into DB");
				obj.saveResultstoDB(noOfDevelopers + " Devs Scenario");

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void selectDevelopers() {

		log4j.debug("Select developers started");
		users = DatabaseManager.getUsers();

		// ArrayList<Integer> exclude = new ArrayList<Integer>(users.length);
		List<Integer> exclude = new ArrayList();
		int number = 0;

		for (int i = 0; i < noOfDevelopers; i++) {
			number = Utils.getRandomNumber(0, users.length - 1, exclude);
			exclude.add(number);

			assignment.addDeveloper(i + 1, users[number]);
		}

		log4j.debug("Select developers completed");

	}

}
