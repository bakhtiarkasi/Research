package cse.unl.edu.Simulation;

import java.util.ArrayList;
import java.util.List;

public class Task {

	
	public int taskId;
	public String developerName;
	public int percentage = 0;

	public List<File> filesList;
	public ArrayList<Integer[]> combinations;
	
	public Task()
	{
		filesList = new ArrayList();
	}
	
	@Override
    public Task clone() {
		Task task = new Task();
		task.taskId = this.taskId;
		task.developerName = this.developerName;
		task.percentage = this.percentage;

		task.filesList = new ArrayList(this.filesList);
		
        return task;
    }
}
