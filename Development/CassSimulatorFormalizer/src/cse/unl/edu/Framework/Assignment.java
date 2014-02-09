package cse.unl.edu.Framework;

import java.util.HashMap;
import java.util.HashSet;


import cse.unl.edu.Framework.Assignment.Task;
import cse.unl.edu.util.Pair;
import cse.unl.edu.util.Utils;

public class Assignment {

	HashSet<Developer> developers;

	public Assignment(int noOfDevs) {
		developers = new HashSet<Developer>(noOfDevs);

	}

	public void addDeveloper(int id, String name) {
		developers.add(new Developer(id, name));
	}

	public void assignTasktoDeveloper(int devId, Task task) {
		Developer devel = getDeveloperById(devId);
		devel.addTaskForUser(task);
	}

	public Developer getDeveloperById(int devId) {

		for (Developer d : developers)
			if (d.getId() == devId)
				return d;

		return null;
	}

	public class Developer {
		int developerId;
		private String name;

		HashMap<Integer, Task> assignedTasks;

		public int getNoOfTasks() {
			return assignedTasks.size();
		}

		public int getId() {
			return this.developerId;
		}

		public Developer(int id, String name) {
			this.developerId = id;
			this.setName(name);

			assignedTasks = new HashMap<Integer, Assignment.Task>();
		}

		public void addTaskForUser(Task task) {
			int id = assignedTasks.size() + 1;
			task.setAssigned(true);
			assignedTasks.put(id, task);
		}

		public Task getTaskById(int taskId) {

			return assignedTasks.get(taskId);
		}

		@Override
		public String toString() {
			String returnStatement = "Developer: " + getName() + "\nId: "
					+ developerId + "\nAssigned Tasks: ";

			for (Task tasks : assignedTasks.values()) {
				returnStatement += tasks.getWsId();
				returnStatement += "\t";

			}
			return returnStatement;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	public class Task {
		final int taskId;
		private int wsId;
		private String taskDescription;

		private HashSet<String> files;
		private HashSet<Integer> mergeConflicts;
		private HashSet<Pair<Integer, String>> buildConflicts;
		private HashSet<Pair<Integer, String>> testConflicts;

		boolean assigned;
		boolean clean;

		public Task(int id, int wsId,  String description) {
			this.taskId = id;
			this.setTaskDescription(description);
			this.setWsId(wsId);
			assigned = false;
			clean = true;
			setFiles(new HashSet<String>());
			setMergeConflicts(new HashSet<Integer>());
			setBuildConflicts(new HashSet<Pair<Integer, String>>());
			setTestConflicts(new HashSet<Pair<Integer, String>>());
		}

		public boolean isClean() {
			return getMergeConflicts().size() > 0 || getBuildConflicts().size() > 0
					|| getTestConflicts().size() > 0;
		}

		public boolean isAssigned() {
			return assigned;
		}

		public void setAssigned(boolean isAssigned) {
			this.assigned = isAssigned;
		}

		public int getFileCount() {
			return getFiles().size();
		}

		public void addFiletoTask(String file) {
			this.getFiles().add(file);

		}

		public void addMergeConflictWithTask(Task task2) {

			this.getMergeConflicts().add(task2.getWsId());
			task2.getMergeConflicts().add(this.getWsId());
			this.clean = false;
			task2.clean = false;
		}

		// this task should be done before task2. "<"
		// conversely task2 is done after this task ">"
		public boolean addBuildConflictWithTask(Task task2) {

			Pair<Integer, String> pair = new Pair<Integer, String>(task2.getWsId(), "<");
			
			Pair<Integer, String> pair2 = new Pair<Integer, String>(task2.getWsId(), ">");
			
			if(this.getBuildConflicts().contains(pair))
				return false;
			
			else if(this.getBuildConflicts().contains(pair2))
				return false;
			
			this.getBuildConflicts().add(pair);

			pair = new Pair<Integer, String>(this.getWsId(), ">");
			task2.getBuildConflicts().add(pair);
			this.clean = false;
			task2.clean = false;
			
			return true;

		}

		// this task should be done before task2. "<"
		// conversely task2 is done after this task ">"
		public boolean addTestConflictWithTask(Task task2) {

			Pair<Integer, String> pair = new Pair<Integer, String>(task2.getWsId(), "<");
			
			Pair<Integer, String> pair2 = new Pair<Integer, String>(task2.getWsId(), ">");
			
			if(this.getTestConflicts().contains(pair))
				return false;
			
			else if(this.getTestConflicts().contains(pair2))
				return false;
			
			this.getTestConflicts().add(pair);

			pair = new Pair<Integer, String>(this.getWsId(), ">");
			task2.getTestConflicts().add(pair);
			this.clean = false;
			task2.clean = false;
			
			return true;

		}

		@Override
		public String toString() {
			String returnStatement = "Task: "
					+ getTaskDescription()
					+"\nWSID: "
					+ getWsId()
					+ "\nAssigned: "
					+ assigned
					+ "\nTotal Files: "
					+ getFileCount()
					+ "\nTotal Conflicts: "
				    + (getMergeConflicts().size() + getBuildConflicts().size() + getTestConflicts()
							.size()) + "\nClean: " + clean
					+ "\nMerge Conflicts: ";

			for (Integer task : getMergeConflicts()) {
				returnStatement += task;
				returnStatement += " | ";
			}
			returnStatement += "\nBuild Conflicts: ";

			for (Pair<Integer, String> pair : getBuildConflicts()) {
				returnStatement += pair.second;
				returnStatement += " ";
				returnStatement += pair.first;
				returnStatement += " | ";

			}
			returnStatement += "\nTest Conflicts: ";
			for (Pair<Integer, String> pair : getTestConflicts()) {
				returnStatement += pair.second;
				returnStatement += " ";
				returnStatement += pair.first;
				returnStatement += " | ";

			}
			return returnStatement;
		}

		public String getTaskDescription() {
			return taskDescription;
		}

		public void setTaskDescription(String taskDescription) {
			this.taskDescription = taskDescription;
		}

		public int getWsId() {
			return wsId;
		}

		public void setWsId(int wsId) {
			this.wsId = wsId;
		}

		public HashSet<String> getFiles() {
			return files;
		}

		public void setFiles(HashSet<String> files) {
			this.files = files;
		}

		public HashSet<Pair<Integer, String>> getBuildConflicts() {
			return buildConflicts;
		}

		public void setBuildConflicts(HashSet<Pair<Integer, String>> buildConflicts) {
			this.buildConflicts = buildConflicts;
		}

		public HashSet<Pair<Integer, String>> getTestConflicts() {
			return testConflicts;
		}

		public void setTestConflicts(HashSet<Pair<Integer, String>> testConflicts) {
			this.testConflicts = testConflicts;
		}

		public HashSet<Integer> getMergeConflicts() {
			return mergeConflicts;
		}

		public void setMergeConflicts(HashSet<Integer> mergeConflicts) {
			this.mergeConflicts = mergeConflicts;
		}

	}

}