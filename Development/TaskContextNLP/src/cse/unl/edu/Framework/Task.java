package cse.unl.edu.Framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Task {

	public String taskId;
	public String shortDescription;
	public String longDescription;

	public ArrayList nouns;
	public ArrayList verbs;
	public ArrayList spKeywords;

	// public ArrayList contextIds;
	public List<File> files;
	List<String> allfiles;
	public String filteredDescription;
	public String comments;

	public Task() {
		nouns = new ArrayList();
		verbs = new ArrayList();
		// contextIds = new ArrayList();
		// files = new ArrayList();
		spKeywords = new ArrayList();
	}

	public Task(String taskId, String longDescription) {
		this();
		this.taskId = taskId;
		this.longDescription = longDescription;
	}
	
	public List<String> getFileNamesList()
	{
		if(allfiles != null)
			return allfiles;
			
		allfiles = new ArrayList();
		for(File fil : this.files)
		{
			allfiles.add(fil.fileName);
		}
		return allfiles;
	}
	
	public List<String> getCommitsForFileName(String fileName)
	{
		for(File fil : this.files)
		{
			if(fil.fileName.equals(fileName))
				return fil.commits;
		}
		return null;
	}

	public void loadFiles(String filesCSV) {
		String[] filesCommits = filesCSV.split(",");
		this.files = new ArrayList();
		String fileCom;
		File obj;
		String contents [];
		for(int i = 0 ; i< filesCommits.length; i++)
		{
			fileCom = filesCommits[i];
			contents = fileCom.split(":");
			
			if(contents.length != 2)
				System.out.println("Error " + fileCom + " task id : " + this.taskId);
			
			obj = new File(contents[0], contents[1]);
			if(this.files.contains(obj))
			{
				int index = files.indexOf(obj);
				files.get(index).commits.add(contents[1]);
			}
			else
			{
				files.add(obj);
			}
		}
	}

	public class File {
		public String fileName;
		public List<String> commits;

		public File(String fileName, String commit) {
			this.fileName = fileName;
			this.commits = new ArrayList();
			this.commits.add(commit);
		}

		@Override
		public boolean equals(Object obj) {
			final File other = (File) obj;
			return this.fileName.equals(other.fileName);
		}

		@Override
		public int hashCode() {
			int hash = fileName.hashCode();
			return hash;
		}

		@Override
		public String toString() {
			return this.fileName + " " + this.commits.toString();
		}
	}
	
	public static void main(String args[])
	{
		Task t = new Task();
		
		Task.File f1 = t.new File("org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/MylarJavaPlugin.java", "6698");
		Task.File f2 = t.new File("org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/MylarJavaPlugin.java", "7474");
		Task.File f3 = t.new File("org.eclipse.mylyn.java.ui/src/org/sample.java", "6698");
 		
		t.loadFiles("org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/MylarJavaPlugin.java:6698,org.eclipse.mylyn.java.ui/plugin.xml:6700,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/MylarJavaPlugin.java:6700,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/ui/wizards/MylarPreferenceWizard.java:6700,org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/ui/wizards/MylarPreferenceWizardPage.java:6700");
		
		
		DBConnector db = new DBConnector();
		db.createConnection();
		System.out.println(db.getCommitGraphForFile("org.eclipse.mylyn.java.ui/src/org/eclipse/mylyn/java/MylarJavaPlugin.java", "6698"));
		
	}

}
