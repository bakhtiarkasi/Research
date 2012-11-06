package cse.unl.edu.Scheduler;

import java.util.HashSet;
import java.util.Set;


public class Task {

	final int taskId;
	

	final int wsId;
	final int preferedSequence;
	final String taskDescription;
	final boolean clean;

	private Set<String> files;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Task(int pkTaskid, int wsId, String description, int preferenceNo,
			boolean clean) {
		this.taskId = pkTaskid;
		this.wsId = wsId;
		this.taskDescription = description;
		this.preferedSequence = preferenceNo == 0 ? wsId : preferenceNo;
		this.clean = clean;

		files = new HashSet();
	}

	public boolean isClean() {
		return this.clean;
	}

	public int getFileCount() {
		return getFiles().size();
	}

	public void addFiletoTask(String file) {
		this.files.add(file);

	}

	@Override
	public String toString() {
		String returnStatement = "Task: " + getTaskDescription() + "\nWSID: "
				+ getWsId() + "\nDB Id: " + this.taskId + "\nTotal Files: "
				+ getFileCount() + "\nClean : " + this.clean
				+ "\nPrefered No : " + preferedSequence;

		return returnStatement;
	}

	public String getTaskDescription() {
		return taskDescription;
	}
	
	public int getTaskId() {
		return taskId;
	}

	public int getWsId() {
		return wsId;
	}

	public Set<String> getFiles() {
		return files;
	}

}
