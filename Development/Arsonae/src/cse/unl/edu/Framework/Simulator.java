package cse.unl.edu.Framework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cse.unl.edu.mylyn.Utils;

public class Simulator {

	int noOfDevelopers;
	int noOfTasks;
	int noOfFiles;
	int noOfAllFiles;

	float mergeConflictsRatio;
	float testFailuresRatio;
	float buildFailuresRatio;

	int mergeConflictReq;
	int minFilesReq;
	int maxFilesReq;
	int mcFilesNumber;

	String [] users = null;
	String [] selectedUsers = null;
	String [][] selectedTasks = null;
	String [] allFiles = null;
	HashSet<String> [] fileAssignments = null;

	public enum DateRange{Daily,Weekly,Monthly};
	DateRange dateRange;

	@SuppressWarnings("unchecked")
	Simulator(int noOfDevelopers, int noOfTasks, int noOfFiles, float mergeConflicts, float testFailures, float buildFailures, DateRange dateRange){
		this.noOfDevelopers = noOfDevelopers;
		this.noOfTasks = noOfTasks;
		this.noOfFiles = noOfFiles;
		this.mergeConflictsRatio = mergeConflicts;
		this.testFailuresRatio = testFailures;
		this.buildFailuresRatio = buildFailures;
		this.dateRange = dateRange;

		noOfAllFiles = noOfDevelopers * noOfTasks * noOfFiles;
		selectedUsers = new String[noOfDevelopers];
		selectedTasks = new String[noOfDevelopers][noOfTasks];

		fileAssignments = new HashSet[noOfDevelopers];
		for(int i =0; i<noOfDevelopers; i++)
			fileAssignments[i] = new HashSet<String>(noOfFiles);

		mergeConflictReq =  Math.round(mergeConflictsRatio * noOfAllFiles);
	}

	@Override
	public String toString() {
		return "Developers: " + noOfDevelopers + "\nNo. of tasks per developer: " + noOfTasks + "\nNo. of files per task: " + noOfFiles + "\nFrequency of conflicts:- MF: " + mergeConflictsRatio +" Build failures: "+ buildFailuresRatio +" Test failures: " + testFailuresRatio + "\nDate Range: " +dateRange.toString() +"\n";
	}

	private void startProcess() 
	{
		users = DatabaseManager.getUsers();

		ArrayList<Integer> exclude = new ArrayList<Integer>(users.length);
		int number = 0;

		for(int i = 0; i<noOfDevelopers; i++)
		{
			number = Utils.getRandomNumber(0, users.length - 1, exclude);
			exclude.add(number);

			selectedUsers[i] = users[number];
		}
		for(int i = 0; i<noOfDevelopers; i++)
			for(int j = 0; j<noOfTasks; j++)
			{
				selectedTasks[i][j] = "U" + (i+1) + "T" + (j+1);
				System.out.println(selectedTasks[i][j]);
			}

		allFiles = DatabaseManager.getFiles(noOfAllFiles);


		assignFiles(); 

	}


	private void assignFiles() 
	{
		int num = noOfDevelopers - 1; 
		/*if no of developers = 4 possible conflicts = 4-1 = 3. So if conflicts are divided evenly, then its ok to add the
		 * quotient else add 1 to the remainder.*/
		minFilesReq = mergeConflictReq % num == 0 ? mergeConflictReq / num : ((mergeConflictReq / num) + 1) ;
		maxFilesReq = mergeConflictReq;

		//finding the number of files in merge conflicts.
		mcFilesNumber = Utils.getRandomNumber(minFilesReq, maxFilesReq, null);

		System.out.println("mergeConflictReq " + mergeConflictReq + " maxFilesReq "+ maxFilesReq + " mcFilesNumber " + mcFilesNumber);
		
		
		int noOfTimesNC = mergeConflictReq / mcFilesNumber;
		int noOfTimeRepeatAnyNC = mergeConflictReq % mcFilesNumber;
		
		/*
		
		int cleanFilesNumber = noOfAllFiles - mcFilesNumber;

		int k = -1;
		ArrayList<Integer> exclude = new ArrayList<Integer>(cleanFilesNumber);
		int number = 0;
		
		//assigning clean files to users randomly
		for(int i = 0; i < cleanFilesNumber; i++)
		{
			k++;
			k = k % noOfDevelopers;

			number = Utils.getRandomNumber(0, noOfAllFiles - 1, exclude);
			exclude.add(number);

			fileAssignments[k].add(allFiles[number]);
		}
		
		int nonCleanFilesNo = noOfAllFiles - cleanFilesNumber;
		String nonCleanFiles [] = new String[nonCleanFilesNo];
		
		for(int j=0; j < nonCleanFilesNo; j++)
		{
			
			number = Utils.getRandomNumber(0, noOfAllFiles - 1, exclude);
			exclude.add(number);
			
			nonCleanFiles[j] = allFiles[number];
			              
		}
		
		int h = -1;
		//assign non clean files to the users
		for(int i = 0; i < noOfAllFiles; i++)
		{
			k++;
			k = k % noOfDevelopers;
			
			h++;
			h = h % noOfDevelopers;
			
			//fileAssignments[k].add(nonCleanFiles[h]);
		}
		
		for(int i = 0; i < noOfDevelopers; i++)
		for(String val : fileAssignments[i])
		{
			System.out.println(val + " " + nonCleanFilesNo);
			
		}

			*/
	}

	public static void main(String[] args) {

		int noOfDevelopers = 2;
		int noOfTasks = 2;
		int noOfFiles = 2;

		float mergeConflicts = 0.7f;
		float testFailures = 0.25f;
		float buildFailures = 0.05f;

		DateRange dateRange = DateRange.Weekly;
		Simulator obj;

		if (args.length < 1) {
			System.out.println("Usage : java ... Simulator -developers<noOfDevelopers> -tasks<noOfTasks> -files<noOfFiles> -frequency<MF:TF:BF> -DateRange<D|W|M>");
			System.exit(1);
		}

		for(int i=0; i < args.length; i++)
		{
			if(args[i].equalsIgnoreCase("-developers"))
				noOfDevelopers = Integer.parseInt(args[i+1]);

			else if(args[i].equalsIgnoreCase("-tasks"))
				noOfTasks = Integer.parseInt(args[i+1]);

			else if(args[i].equalsIgnoreCase("-files"))
				noOfFiles = Integer.parseInt(args[i+1]);

			else if(args[i].equalsIgnoreCase("-frequency"))
			{
				String frequency = args[i+1];

				mergeConflicts = Float.parseFloat(frequency.split(":")[0]);
				testFailures = Float.parseFloat(frequency.split(":")[1]);
				buildFailures = Float.parseFloat(frequency.split(":")[2]);
			}

			else if(args[i].equalsIgnoreCase("-DateRange"))
			{
				String charRep = args[i+1];

				if(charRep.equalsIgnoreCase("M"))
					dateRange = DateRange.Monthly;
				else if(charRep.equalsIgnoreCase("W"))
					dateRange = DateRange.Weekly;
				else
					dateRange = DateRange.Daily;
			}
			i++;
		}
		obj = new Simulator(noOfDevelopers, noOfTasks, noOfFiles, mergeConflicts, testFailures, buildFailures, dateRange);
		System.out.print(obj);
		obj.startProcess();
	}

}
