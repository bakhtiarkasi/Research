package cse.unl.edu.Scheduler;

import java.util.HashSet;
import java.util.Set;


public class Developer {
	
	final int pkDeveloperId;
	//final int wsId;
	final String name;
	final int autoId;

	Set<Integer> assignedTasks;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Developer(int pkDeveloperId,int autoId, String name) {
		
	//public Developer(int pkDeveloperId,int wsId,int autoId, String name) {
		this.pkDeveloperId = pkDeveloperId;
		//this.wsId = wsId;
		this.name = name;
		this.autoId = autoId;

		assignedTasks = new HashSet();
	}

	public void addTaskForUser(int taskId) {
		
		assignedTasks.add(taskId);
	}


	@Override
	public String toString() {
		String returnStatement = "Developer: " + getName() 
				+ "\nDB Id: " + pkDeveloperId
				//+ "\nWS Id: " + wsId
				+ "\nAuto Id: " + autoId
				+ "\nAssigned Tasks: ";

		for (Integer taskId : assignedTasks) {
			returnStatement += taskId;
			returnStatement += "\t";

		}
		return returnStatement;
	}

	public String getName() {
		return name;
	}
	
	public int getNoOfTasks() {
		return assignedTasks.size();
	}

	public int getDBId() {
		return this.pkDeveloperId;
	}

	/*public int getWsId() {
		return wsId;
	}*/

	public Set<Integer> getAssignedTasks() {
		return assignedTasks;
	}

	
}
