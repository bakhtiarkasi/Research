package cse.unl.edu.test;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.*;


import cse.unl.edu.Framework.DatabaseManager;
import cse.unl.edu.TaskGenerator.Simulator;
import cse.unl.edu.TaskGenerator.Simulator.DateRange;
import cse.unl.edu.util.Pair;
import cse.unl.edu.util.Utils;
import static org.junit.Assert.*;

public class SimulatorTest {

	HashSet<SimulatorSet> dataTestSet;
	HashMap<Integer, String> tasksDataSet;
	HashMap<String, String> devsDataSet;
	
	static Logger log4j = Logger.getLogger("cse.unl.edu.test.SimulatorTest");

	@Before
	public void setUp() {

		dataTestSet = new HashSet<SimulatorTest.SimulatorSet>();

		SimulatorSet st = null;
		Random rand = new Random();
		float upperBound = 0.0f;
		float lowerBound = 0.0f;

		for (int i = 0; i < 10; i++) {

			st = new SimulatorSet();
			st.noOfDevelopers = Utils.getRandomNumber(6, 22, null);
			st.noOfTasks = Utils.getNextGaussian(1, 8, null);
			st.noOfFiles = Utils.getNextGaussian(1, 4, null);

			
			upperBound = 0.02f;
			lowerBound = 0.001f;
			
			st.mergeConflictsRatio = (float) rand.nextDouble()
					* (upperBound - lowerBound) + lowerBound;
			
			upperBound = 0.09f;
			lowerBound = 0.001f;
			
			st.testFailuresRatio = (float) rand.nextDouble()
					* (upperBound - lowerBound) + lowerBound;
			
			upperBound = 0.08f;
			lowerBound = 0.001f;
			
			//st.mergeConflictsRatio = (float) (Math.abs(rand.nextGaussian()) * 0.279942786 + 0.20);
			//st.buildFailuresRatio = (float) (Math.abs(rand.nextGaussian()) * 0.170699083 + 0.10);
			//st.testFailuresRatio = (float) (Math.abs(rand.nextGaussian()) * 0.244865801 + 0.14);

			st.buildFailuresRatio = (float) rand.nextDouble()
					* (upperBound - lowerBound) + lowerBound;

			

			
			
			st.buildFailuresRatio = st.buildFailuresRatio <= 0.0005 ? 0.0f : st.buildFailuresRatio;
			st.mergeConflictsRatio = st.mergeConflictsRatio <= 0.0005 ? 0.0f : st.mergeConflictsRatio;
			st.testFailuresRatio = st.testFailuresRatio <= 0.0005 ? 0.0f : st.testFailuresRatio;

			// st.noOfDevelopers = 5;
			 //st.noOfTasks = 12;
			 //st.noOfFiles = 3;
			 //st.mergeConflictsRatio = 0.18480387f;
			 //st.buildFailuresRatio = 0.051852923f;
			 //st.testFailuresRatio = 0.39068133f;*/
			
			dataTestSet.add(st);
			
		}

	}

	@After
	public void tearDown() {

		dataTestSet = null;
	}

	@Test
	public void testAllTaskAssigned() {
		System.out.println("Test if all task are being assigned...");

		try {

			for (SimulatorSet st : dataTestSet) {

				Simulator obj = new Simulator(st.noOfDevelopers, st.noOfTasks,
						st.noOfFiles, st.mergeConflictsRatio,
						st.testFailuresRatio, st.buildFailuresRatio,
						st.dateRange);
				System.out.println("\n" + obj);

				if (obj.startProcess() == false)
					continue;

				String output = obj.returnOutput();

				//System.out.println(output);

				populateDataSets(output);

				// check if the task assigned to developers are valid
				int allTasksCount = 0;
				for (String devInfo : devsDataSet.values()) {
					for (String devsLine : devInfo.split("\n"))
						if (devsLine.startsWith("Assigned Tasks: ")) {
							String taskIds = devsLine.split("Assigned Tasks: ")[1]
									.trim();

							for (String taskId : taskIds.split("\t")) {
								taskId = taskId.trim();
								assertTrue(tasksDataSet.containsKey(Integer.parseInt(taskId)));
								allTasksCount++;
							}

						}

				}

				// check if there are no extra tasks left
			    log4j.debug("Check if all tasks are assigned");
				assertTrue(allTasksCount == tasksDataSet.size());

				int allFilesCount = 0;
				int totalConflicts = 0;
				int totalMergeConflicts = 0;
				int totalBuildConflicts = 0;
				int totalTestConflicts = 0;
				
				HashSet<Pair<Integer,Integer>> mergeConflicts = new HashSet<Pair<Integer,Integer>>();
				HashSet<Pair<Integer,Integer>> testConflicts = new HashSet<Pair<Integer,Integer>>();
				HashSet<Pair<Integer,Integer>> buildConflicts = new HashSet<Pair<Integer,Integer>>();

				for (Integer key : tasksDataSet.keySet()) {
					
					String taskInfo = tasksDataSet.get(key);

					int allConflicts = 0;
					int calculatedConflicts = 0;
					Boolean isClean = false;
					
					int mergeConflictCount = 0;
					int buildConflictCount = 0;
					int testConflictCount = 0;
					

					for (String taskLine : taskInfo.split("\n")) {

						if (taskLine.startsWith("Assigned: ")) {
							String assigned = taskLine.split("Assigned: ")[1]
									.trim();

							assertTrue(Boolean.parseBoolean(assigned));
						}

						else if (taskLine.startsWith("Total Conflicts: ")) {
							String assigned = taskLine
									.split("Total Conflicts: ")[1].trim();

							allConflicts = Integer.parseInt(assigned);
						}

						else if (taskLine.startsWith("Clean: ")) {
							String assigned = taskLine.split("Clean: ")[1]
									.trim();

							isClean = Boolean.parseBoolean(assigned);
						}

						else if (taskLine.startsWith("Total Files: ")) {
							String fileCount = taskLine.split("Total Files: ")[1]
									.trim();

							// check if all tasks have files associated with
							// them
							assertTrue(Integer.parseInt(fileCount) > 0);
							allFilesCount += Integer.parseInt(fileCount);
						}

						else if (taskLine.startsWith("Merge Conflicts: ")) {
							String fileCount = taskLine.replaceFirst(
									"Merge Conflicts: ", " ").trim();
								

									for (String str: fileCount.split("\\|"))
									{
										str = str.replace('<', ' ');
										str = str.replace('>', ' ');
										
										str = str.trim();
										if(str.length() > 0)
										{
											mergeConflictCount++;
											int confId = Integer.parseInt(str);
											if(confId > key)
												mergeConflicts.add(new Pair<Integer,Integer>(key,confId));
										}
									}
						}

						else if (taskLine.startsWith("Build Conflicts: ")) {
							String fileCount = taskLine.replaceFirst(
									"Build Conflicts: ", " ").trim();

							for (String str: fileCount.split("\\|"))
							{
								str = str.replace('<', ' ');
								str = str.replace('>', ' ');
								
								str = str.trim();
								if(str.length() > 0)
								{
									buildConflictCount++;
									int confId = Integer.parseInt(str);
									if(confId > key)
										buildConflicts.add(new Pair<Integer,Integer>(key,confId));
								}
							}
						}

						else if (taskLine.startsWith("Test Conflicts: ")) {
							String fileCount = taskLine.replaceFirst(
									"Test Conflicts: ", " ").trim();

							for (String str: fileCount.split("\\|"))
							{
								str = str.replace('<', ' ');
								str = str.replace('>', ' ');
								
								str = str.trim();
								if(str.length() > 0)
								{
									testConflictCount++;
									int confId = Integer.parseInt(str);
									if(confId > key)
										testConflicts.add(new Pair<Integer,Integer>(key,confId));
								}
							}
						}
					}

					// Check if a task is still marked clean when it has
					// conflicts
					log4j.debug("Check if a task is still marked clean when it has conflicts");
					
					if (isClean)
						assertTrue(allConflicts == 0);
					else
						assertTrue(allConflicts > 0);
					
					
					calculatedConflicts = mergeConflictCount + buildConflictCount + testConflictCount;
					

					// Check if all conflicts counts different than individual
					// conflicts
					log4j.debug("Check if all conflicts counts different than individual");
					System.out.println(taskInfo);
					assertTrue(allConflicts == calculatedConflicts);
					totalConflicts += allConflicts;
					

				}
				
				//finding total conflicts
				totalMergeConflicts += mergeConflicts.size();
				totalBuildConflicts += buildConflicts.size();
				totalTestConflicts += testConflicts.size();
				

				// check if total conflicts are same as individual task
				// conflicts
				log4j.debug("check if total conflicts are same as individual task conflicts");
				assertTrue(totalConflicts/2 == (totalMergeConflicts
						+ totalBuildConflicts + totalTestConflicts));

				// check if merge conflict percentage is same as provided
				log4j.debug("check if merge conflict percentage is same as provided");
				
				
				log4j.debug("MC Files after " + totalMergeConflicts + " all tasks " + obj.getAllPossibleConflicts() );
				log4j.debug( Math
						.abs((obj.getMergeConfRatio() - ((float) totalMergeConflicts)
								/ (float) obj.getAllPossibleConflicts())));
				
				assertTrue(Math
						.abs((obj.getMergeConfRatio() - ((float) totalMergeConflicts)
								/ (float) obj.getAllPossibleConflicts())) < 0.01);

				
				
				// check if build conflict percentage is same as provided
				log4j.debug("check if build conflict percentage is same as provided");
				log4j.debug("BC Files after " + totalBuildConflicts + " all tasks " + obj.getAllPossibleConflicts() );
				
				
				log4j.debug(Math
						.abs((obj.getBuildConfRatio() - ((float) totalBuildConflicts)
								/ (float) obj.getAllPossibleConflicts())));
				assertTrue(Math
						.abs((obj.getBuildConfRatio() - ((float) totalBuildConflicts)
								/ (float) obj.getAllPossibleConflicts())) < 0.01);

				// check if test conflict percentage is same as provided
				log4j.debug("check if test conflict percentage is same as provided");
				log4j.debug("TC Files after " + totalTestConflicts + " all tasks " + obj.getAllPossibleConflicts() );
				
				log4j.debug(Math
						.abs((obj.getTestConfRatio() - ((float) totalTestConflicts)
								/ (float) obj.getAllPossibleConflicts())));
				assertTrue(Math
						.abs((obj.getTestConfRatio() - ((float) totalTestConflicts)
								/ (float) obj.getAllPossibleConflicts())) < 0.01);

				System.out.println("ALL Test Sucessfull");
				
				System.out.println("Saving into DB");
				int sessionId = 2;
				sessionId = obj.saveResultstoDB("Storm 1 week Data");
				
				log4j.debug("SP Params: "+ sessionId+ "," +allTasksCount+ "," +allFilesCount+ "," +st.noOfDevelopers+ "," + totalMergeConflicts+ "," +totalBuildConflicts+ "," +totalTestConflicts+ "," +totalConflicts/2);
				String dbStatus = DatabaseManager.callTestCase(sessionId,allTasksCount,allFilesCount,st.noOfDevelopers, totalMergeConflicts,totalBuildConflicts,totalTestConflicts,totalConflicts/2);
				
				log4j.debug(dbStatus);

			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.assertTrue(e.getMessage(), false);
		}

	}

	private void populateDataSets(String output) {
		output = output.trim();

		if (tasksDataSet == null) {
			tasksDataSet = new HashMap<Integer, String>();
			devsDataSet = new HashMap<String, String>();
		} else {
			tasksDataSet.clear();
			devsDataSet.clear();
		}

		String[] allSet = output
				.split("================================================================================");
		{
			for (String singleSet : allSet) {
				singleSet = singleSet.trim();
				String devInfo = singleSet
						.split("--------------------------------------------------------------------------------")[0];
				String taskInfo = singleSet
						.split("--------------------------------------------------------------------------------")[1];

				devInfo = devInfo.trim();
				taskInfo = taskInfo.trim();

				for (String taskData : devInfo.split("\n")) {
					if (taskData.startsWith("Id: ")) {
						devsDataSet.put(taskData.split("Id: ")[1].trim(),
								devInfo);
						break;
					}
				}

				for (String eachTask : taskInfo
						.split("\\Q...............................................................................\\E")) {
					eachTask = eachTask.trim();

					for (String taskData : eachTask.split("\n")) {
						if (taskData.startsWith("WSID: ")) {
							tasksDataSet.put(
									Integer.parseInt(taskData.split("WSID: ")[1].trim()),
									eachTask);
							break;
						}
					}

				}
			}
		}
	}

	@Test
	public void testRatios() {
		
	}

	public class SimulatorSet {

		int noOfDevelopers;
		int noOfTasks;
		int noOfFiles;

		float mergeConflictsRatio;
		float testFailuresRatio;
		float buildFailuresRatio;

		DateRange dateRange = DateRange.Weekly;
	}
}