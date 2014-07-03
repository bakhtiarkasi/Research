package cse.unl.edu.Framework;

import java.awt.List;
import java.util.ArrayList;

public class Task {
	
	public String taskId;
	public String shortDescription;
	public String longDescription;
	
	public ArrayList nouns;
	public ArrayList verbs;
	public ArrayList spKeywords;
	
	//public ArrayList contextIds;
	public ArrayList files;
	public String filteredDescription;
	
	public Task()
	{
		nouns = new ArrayList();
		verbs = new ArrayList();
		//contextIds = new ArrayList();
		files = new ArrayList();
		spKeywords = new ArrayList();
	}
	
	
	public Task(String taskId, String longDescription)
	{
		this();
		this.taskId = taskId;
		this.longDescription = longDescription;
	}
	
	
}
