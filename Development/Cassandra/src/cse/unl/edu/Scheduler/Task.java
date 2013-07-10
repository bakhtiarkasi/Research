package cse.unl.edu.Scheduler;

import java.util.HashSet;
import java.util.Set;


public class Task {

	final int taskId;
	

	//final int wsId;
	final int preferedSequence;
	final int recomendedOrder;
	final String taskDescription;
	final boolean processed;
	//final boolean clean;

	private Set<String> files;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	//public Task(int pkTaskid, int wsId, String description, int preferenceNo,
		//	boolean clean) {
	
	public Task(int pkTaskid, String description, int preferenceNo, int recomendedOrder,  boolean processed) {
		this.taskId = pkTaskid;
		//this.wsId = wsId;
		this.taskDescription = description;
		//this.preferedSequence = preferenceNo == 0 ? wsId : preferenceNo;
		this.preferedSequence = preferenceNo;
		this.processed = processed;
		this.recomendedOrder = recomendedOrder;
		//this.clean = clean;
		

		files = new HashSet();
	}

	/*public boolean isClean() {
		return this.clean;
	}*/

	public int getFileCount() {
		return getFiles().size();
	}

	public void addFiletoTask(String file) {
		this.files.add(file);

	}

	@Override
	public String toString() {
		String returnStatement = "Task: " + getTaskDescription() + /*"\nWSID: "
				+ getWsId() +*/ "\nDB Id: " + this.taskId + "\nTotal Files: "
				+ getFileCount() + /*"\nClean : " + this.clean
				+*/ "\nPrefered No : " + preferedSequence;

		return returnStatement;
	}

	public String getTaskDescription() {
		return taskDescription;
	}
	
	public int getTaskId() {
		return taskId;
	}

	/*public int getWsId() {
		return wsId;
	}*/

	public Set<String> getFiles() {
		return files;
	}

}
