package cse.unl.edu.Scheduler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import cse.unl.edu.Framework.DatabaseManager;
import cse.unl.edu.Scheduler.Conflict.Direction;
import cse.unl.edu.Scheduler.Conflict.Type;
import cse.unl.edu.util.Utils;

public class TaskScheduler {

	final int sessionId;
	private Set<Task> tasks;
	private Set<Developer> developers;
	private Set<Conflict> conflictsSet;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TaskScheduler(int sessionId) {
		this.sessionId = sessionId;
		this.tasks = new HashSet();
		this.developers = new HashSet();
		this.conflictsSet = new HashSet();
	}

	public int getSessionId() {
		return sessionId;
	}

	public Set<Task> getTasks() {
		return tasks;
	}

	public Set<Developer> getDevelopers() {
		return developers;
	}

	public Set<Conflict> getConflictsSet() {
		return conflictsSet;
	}

	public void addNewTask(Task task) {
		this.tasks.add(task);
	}

	public void addNewDeveloper(Developer dev) {
		this.developers.add(dev);
	}

	public void addNewConflict(Conflict conf) {
		this.conflictsSet.add(conf);
	}

	public Task getTask(int pkTaskId, int ws_id) {
		if (pkTaskId != -1)
			for (Task task : tasks) {
				if (task.getTaskId() == pkTaskId)
					return task;
			}

		else if (ws_id != -1)
			for (Task task : tasks) {
				if (task.preferedSequence == ws_id)
					return task;
			}

		return null;
	}

	public Developer getDeveloper(int pkDevId, int wsId, int autoId, String name) {
		if (pkDevId != -1)
			for (Developer dev : developers) {
				if (dev.getDBId() == pkDevId)
					return dev;
			}

		else if (wsId != -1)
			for (Developer dev : developers) {
				if (dev.getDBId() == wsId)
					return dev;
			}
		else if (autoId != -1)
			for (Developer dev : developers) {
				if (dev.autoId == autoId)
					return dev;
			}

		else if (name != null)
			for (Developer dev : developers) {
				if (dev.getName().equals(name))
					return dev;
			}

		return null;
	}

	public Set<Conflict> getDirectConflictSet() {
		Set<Conflict> mergeConflictSet = new HashSet();

		for (Conflict conf : conflictsSet) {
			if (conf.getConflictType() == Conflict.Type.D)
				mergeConflictSet.add(conf);
		}
		return mergeConflictSet;
	}

	public Set<Conflict> getInDirectConflictSet() {
		Set<Conflict> btConflictSet = new HashSet();

		for (Conflict conf : conflictsSet) {
			if (conf.getConflictType() == Conflict.Type.I)
				btConflictSet.add(conf);
		}
		return btConflictSet;
	}

	private int getDevIdforTask(int taskId) {
		// TODO Auto-generated method stub

		for (Developer dev : developers) {
			if (dev.getAssignedTasks().contains(taskId))
				return dev.autoId;
		}
		return -1;
	}

	private boolean isTaskProcess(int dbId) {
		Developer dev = this.getDeveloper(dbId, -1, -1, null);

		for (Integer task : dev.getAssignedTasks()) {
			return this.getTask(task, -1).processed;
		}

		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {

			boolean optimise = Boolean.parseBoolean(args[1]);
			String fileName = "";

			int sessionId = Integer.parseInt(args[0]);
			// for (int sessionId = 226; sessionId <= 265; sessionId++) {
			TaskScheduler obj = new TaskScheduler(sessionId);
			fileName = "";

			DatabaseManager.populateConflictsforSession(sessionId, obj);

			String script;
			String filePath = "/Users/bkasi/Documents/Research/Development/Z3Scripts/";

			script = obj.getPythonScript(optimise);

			if (optimise)
				fileName = "Opt" + sessionId;
			else
				fileName += sessionId;

			System.out.println(Utils
					.writePyhtonFile(fileName, script, filePath));
			// System.out.println("Finished sucessful");
			// }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getPythonScript(boolean optimise) {

		StringBuilder scrpt = new StringBuilder();
		final int noOfDevs = this.developers.size();

		// Appending the methods sections

		// def printResult(m):
		scrpt.append("def printResult(m):\n\tresult = 'Results:\\n'\n");
		Developer dev = null;
		for (int i = 1; i <= noOfDevs; i++) {
			dev = this.getDeveloper(-1, -1, i, null);
			scrpt.append("\tresult += \'D" + i + " ->" + dev.getDBId()
					+ "[\'\n");
			scrpt.append("\tresult += getResultforDev(D" + i + ",m)\n");
			scrpt.append("\tresult +=  \']\\n\'\n");
		}
		scrpt.append("\treturn result\n\n");

		// def getGoal():
		scrpt.append("def getGoal():\n");
		scrpt.append("\treturn simplify(");
		for (int i = 1; i <= noOfDevs; i++) {
			scrpt.append(" getGoalFor(D" + i + ") ");
			if (i < noOfDevs)
				scrpt.append("+");
		}
		scrpt.append(")\n\n");

		// def getDistance(m):
		scrpt.append("def getDistance(m):\n");
		scrpt.append("\ttotal = 0;\n");
		for (int i = 1; i <= noOfDevs; i++) {
			if (i == 1)
				scrpt.append("\ttotal = distance(D" + i + ", m)\n");

			else if (noOfDevs > 1)
				scrpt.append("\ttotal = total + distance(D" + i + ", m)\n");
		}
		scrpt.append("\treturn total\n\n");
		scrpt.append("###end method sections\n\n");

		// Global variables section
		scrpt.append("#initialize the solver\n");

		if (optimise)
			scrpt.append("s = getSolverforOpt()\n\n");
		else
			scrpt.append("s = getSolver()\n\n");

		// scrpt.append("s = Then(With(\'simplify\', blast_distinct=True, arith_lhs=True), \n \'normalize-bounds\',  \'lia2pb\',  \'bit-blast\', \'smt\').solver()\n\n");

		// Developer vectors
		scrpt.append("##Developers Vectors\n");
		// Developer are identified on auto id.
		for (int i = 1; i <= noOfDevs; i++) {
			dev = this.getDeveloper(-1, -1, i, null);

			// Creating vector for this developer
			scrpt.append("D" + i + " = IntVector(\'D" + i + "\', "
					+ dev.getNoOfTasks() + ") \n");
		}

		// Putting hard constraints
		scrpt.append("\n##Adding hard constraints\n");
		scrpt.append("valueConst = [");
		int noOftask;
		for (int i = 1; i <= noOfDevs; i++) {
			dev = this.getDeveloper(-1, -1, i, null);

			boolean processed = isTaskProcess(dev.getDBId());
			noOftask = dev.getNoOfTasks();
			if (!processed) {
				for (int j = 0; j < noOftask; j++) {
					scrpt.append("1 <= D" + i + "[" + j + "], D" + i + "[" + j
							+ "] <= " + noOftask + ",");

				}
			}
			else
			{
				for(Integer id : dev.getAssignedTasks())
				{
					Task task = this.getTask(id, -1);
					int prefTaskId = task.preferedSequence -1;
					int recOrderTaskId = task.recomendedOrder;
					scrpt.append(recOrderTaskId + " <= D" + i + "[" + prefTaskId + "], D" + i + "[" + prefTaskId
							+ "] <= " + recOrderTaskId + ",");
				}
			}
			if (i == noOfDevs)
				scrpt.deleteCharAt(scrpt.lastIndexOf(","));
			else
				scrpt.append("\\\n\t\t");
		}
		scrpt.append("]\n\n");

		scrpt.append("distConst = [");
		for (int i = 1; i <= noOfDevs; i++) {
			scrpt.append("Distinct(D" + i + ")");

			if (i != noOfDevs)
				scrpt.append(", ");
		}
		scrpt.append("]\n\n");

		scrpt.append("##Add constraints to solver s\n");
		scrpt.append("s.add(valueConst)\n");
		scrpt.append("s.add(distConst)\n");

		// Adding soft constraints
		scrpt.append("\n##Adding soft constraints\n");
		scrpt.append("##Adding boolean vector for DC\n");
		Set<Conflict> directConfs = this.getDirectConflictSet();
		Set<Conflict> inDirectConfs = this.getInDirectConflictSet();
		scrpt.append("DC = BoolVector(\'DC\', " + directConfs.size() + ")\n");
		scrpt.append("IC = BoolVector(\'IC\', " + inDirectConfs.size() + ")\n");
		scrpt.append("\n##Adding soft constraints as assertions\n");

		int index = 0;
		int dev1 = 0;
		int dev2 = 0;
		int dev1PrefId = 0;
		int dev2PrefId = 0;
		scrpt.append("##Adding Direct Conflicts\n");
		for (Conflict dc : directConfs) {
			dev1 = this.getDevIdforTask(dc.getTask1Id());
			dev2 = this.getDevIdforTask(dc.getTask2Id());

			dev1PrefId = this.getTask(dc.getTask1Id(), -1).preferedSequence - 1;
			dev2PrefId = this.getTask(dc.getTask2Id(), -1).preferedSequence - 1;

			scrpt.append("s.add(Implies(DC[" + (index++) + "], D" + dev1 + "["
					+ dev1PrefId + "] != D" + dev2 + "[" + dev2PrefId + "]))\n");
		}

		index = 0;
		dev1 = 0;
		dev2 = 0;
		dev1PrefId = 0;
		dev2PrefId = 0;
		scrpt.append("##Adding INDirect Conflicts\n");
		for (Conflict ic : inDirectConfs) {
			dev1 = this.getDevIdforTask(ic.getTask1Id());
			dev2 = this.getDevIdforTask(ic.getTask2Id());

			dev1PrefId = this.getTask(ic.getTask1Id(), -1).preferedSequence - 1;
			dev2PrefId = this.getTask(ic.getTask2Id(), -1).preferedSequence - 1;

			scrpt.append("s.add(Implies(IC[" + (index++) + "], D" + dev1 + "["
					+ dev1PrefId + "] != D" + dev2 + "[" + dev2PrefId + "]))\n");
		}

		/*
		 * index = 0; dev1 = 0; dev2 = 0; dev1PrefId = 0; dev2PrefId = 0; String
		 * confDirection = ">"; scrpt.append("\n##Adding InDirect Conflicts\n");
		 * 
		 * Set<Conflict> treeSet = new TreeSet<Conflict>(new
		 * conflictTypeComparator()); treeSet.addAll(inDirectConfs);
		 * 
		 * for (Conflict idc : treeSet) { dev1 =
		 * this.getDevIdforTask(idc.getTask1Id()); dev2 =
		 * this.getDevIdforTask(idc.getTask2Id());
		 * 
		 * dev1PrefId = this.getTask(-1, idc.getTask1Id()).preferedSequence - 1;
		 * dev2PrefId = this.getTask(-1, idc.getTask2Id()).preferedSequence - 1;
		 * 
		 * //if(idc.getConflictDirection() == Direction.Left) // confDirection =
		 * "<";
		 * 
		 * //else if(idc.getConflictDirection() == Direction.Right)
		 * //confDirection = ">";
		 * 
		 * confDirection = ">";
		 * 
		 * scrpt.append("s.add(Implies(IC[" + (index++) + "], D" + dev1 + "[" +
		 * dev1PrefId + "] " + confDirection + " D" + dev2 + "[" + dev2PrefId +
		 * "]))\n"); }
		 */
		scrpt.append("\n##End Java insert\n");

		return scrpt.toString();

	}

	class conflictTypeComparator implements Comparator<Conflict> {

		@Override
		public int compare(Conflict arg0, Conflict arg1) {

			// return TC first and then BC
			if (arg0.getConflictType() == Type.I)
				return -1;
			else
				return 1;
		}
	}

}
