package cse.unl.edu.Simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cse.unl.edu.util.Utils;

public class WeeklySimulator {

	HashMap<String, Author> authorsMap;
	private static String project;

	private String DC;
	private String IC;
	private int median;
	private int percentage;
	ArrayList<Integer[]> combinations;
	int[] allCombinations;
	float[] results;
	int lastDCCount;
	Task[] task;
	Task[] initialTask;

	public List<Conflict> directConflicts;
	public List<Conflict> inDirectConflicts;
	public List<String> taskConstMap;

	public List<Conflict> groundTDC;
	public List<Conflict> groundTIC;
	public List<String> groundTaskConstMap;

	// private final String path = "/Users/bkasi/Documents/Research/DAScripts/";

	// private final String path = "/work/esquared/bkasi/DataAnalysis/Storm/";
	private String path = "";// "/work/esquared/bkasi/DataAnalysis/Voldemort/";
	private String runEnvironment;
	private String xmlFileName;
	private String simulationType;

	public WeeklySimulator() {
		authorsMap = new HashMap();
	}

	private void loadAuthorFileCount() {
		String stormFileCount = "";

		if (project.toLowerCase().equals("s")) {
			if (runEnvironment.toLowerCase().equals("l"))
				path = "/Users/bkasi/Documents/Research/DAScripts/";
			else
				path = "/work/esquared/bkasi/DataAnalysis/Storm/";

			stormFileCount = path + "StormFileCounts.txt";
		} else if (project.toLowerCase().equals("v")) {
			if (runEnvironment.toLowerCase().equals("l"))
				path = "/Users/bkasi/Documents/Research/DAScripts/";
			else
				path = "/work/esquared/bkasi/DataAnalysis/Voldemort/";

			stormFileCount = path + "VoldemortFileCounts.txt";
		}

		try {
			String contents = Utils.readFile(stormFileCount);

			String[] hashIds = contents
					.split("=========================================\n");

			for (String hash : hashIds) {

				String[] devFiles = hash
						.split("\n-----------------------------------------\n");

				if (devFiles.length < 2)
					continue;

				Author auth = new Author(devFiles[0], devFiles[1]);

				authorsMap.put(devFiles[0], auth);

				// System.out.println(devFiles[0]);
				// System.out.println(devFiles[1]);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public void startSimulation() {

		this.loadAuthorFileCount();

		String rubyFilePath = "", xmlFilePath = "";

		if (project.toLowerCase().equals("s")) {
			rubyFilePath = path + "StormDFIcseWeekly.rb";
			xmlFilePath = path + xmlFileName;
		} else if (project.toLowerCase().equals("v")) {
			rubyFilePath = path + "VoldemortDFIcseWeekly.rb";
			xmlFilePath = path + xmlFileName;
		}

		String hash = "";
		Document doc = Utils.openTaskList(xmlFilePath);
		Element root = doc.getDocumentElement();

		NodeList allCommits = root.getElementsByTagName("Commit");

		for (int i = 0; i < allCommits.getLength(); i++) {

			// i =5;
			Node comit = allCommits.item(i);
			Element element = (Element) comit;

			task = new Task[element.getElementsByTagName("Remote").getLength() + 1];
			initialTask = new Task[task.length];

			hash = element.getAttribute("MergeId");

			Element masterElement = (Element) element.getElementsByTagName(
					"Master").item(0);

			task[0] = new Task();
			task[0].developerName = masterElement.getAttribute("DevName");
			task[0].taskId = 0;
			addFilesFor(task[0], masterElement.getElementsByTagName("File"));

			for (int j = 1; j <= element.getElementsByTagName("Remote")
					.getLength(); j++) {
				Element remoteElement = (Element) element.getElementsByTagName(
						"Remote").item(j - 1);

				task[j] = new Task();
				task[j].developerName = remoteElement.getAttribute("DevName");
				task[j].taskId = j;
				addFilesFor(task[j], remoteElement.getElementsByTagName("File"));

			}

			// System.out.println("Tasks " + task.length);
			for (int l = 0; l < task.length; l++)
				initialTask[l] = task[l].clone();

			this.analyzeForConflicts();

			this.groundTDC = new ArrayList();
			for (Conflict c : this.directConflicts)
				this.groundTDC.add(c.clone());

			this.groundTIC = new ArrayList();
			for (Conflict c : this.inDirectConflicts)
				this.groundTIC.add(c.clone());

			this.groundTaskConstMap = new ArrayList(taskConstMap);

			DC = "";
			IC = "";

			int maxFiles = 0;
			for (int j = 1; j < task.length; j++) {
				if (task[j].filesList.size() > maxFiles)
					maxFiles = task[j].filesList.size();
			}

			if (maxFiles > this.median)
				maxFiles = this.median;

			for (int j = 1; j < task.length; j++) {
				Author ath = authorsMap.get(task[j].developerName);
				int count = ath.allFilesCount;

				int setSize = Math.abs(percentage);
				int requiredFile = (int) Math.round(setSize * 0.01
						* task[j].filesList.size());

				requiredFile = requiredFile == 0 ? 1 : requiredFile;

				// System.out.println(i + 1);

				if ((count - requiredFile - task[j].filesList.size()) > 0) {

					processSubsets(task[j].filesList.size(), requiredFile);

					task[j].combinations = new ArrayList();
					for (Integer[] comb : this.combinations)
						task[j].combinations.add(comb.clone());

					combinations.clear();
				}

			}
			results = new float[12];

			int gTDC = 0;
			int gTIC = 0;

			for (String cons : groundTaskConstMap)
				if (cons.startsWith("D"))
					gTDC++;
				else
					gTIC++;

			for (int k = 0; k < maxFiles; k++) {
				int dcContains = 0;
				int dcNewAdds = 0;
				int dcRemoved = 0;

				int icContains = 0;
				int icNewAdds = 0;
				int icRemoved = 0;

				int dcTContains = 0;
				int dcTNewAdds = 0;
				int dcTRemoved = 0;

				int icTContains = 0;
				int icTNewAdds = 0;
				int icTRemoved = 0;
				Task taskTemp = null;

				for (int j = 1; j < task.length; j++) {

					if (task[j].combinations != null
							&& task[j].combinations.size() > 0) {
						Integer[] comb = task[j].combinations.get(0);
						task[j].combinations.remove(0);
						if (simulationType.toLowerCase().equals("f"))
							taskTemp = this.simulateConstraintAssignment(
									task[j], comb, percentage, rubyFilePath,
									hash);
						else
							taskTemp = this.simulateTaskAssignment(task[j],
									comb, percentage, rubyFilePath, hash);
						initialTask[j] = taskTemp;
					}

				}
				this.analyzeForConflicts();

				for (int l = 0; l < task.length; l++)
					initialTask[l] = task[l].clone();

				for (Conflict conf : directConflicts) {
					if (contains(groundTDC, conf)) {
						dcContains++;
					} else {
						dcNewAdds++;
					}
				}
				dcRemoved = groundTDC.size() - dcContains;

				for (Conflict conf : inDirectConflicts) {
					if (contains(groundTIC, conf)) {
						icContains++;
					} else {
						icNewAdds++;
					}
				}
				icRemoved = groundTIC.size() - icContains;

				for (String taskCon : taskConstMap) {

					if (groundTaskConstMap.contains(taskCon)) {
						if (taskCon.startsWith("D"))
							dcTContains++;
						else
							icTContains++;
					} else {
						if (taskCon.startsWith("D"))
							dcTNewAdds++;
						else
							icTNewAdds++;
					}
				}
				dcTRemoved = gTDC - dcTContains;
				icTRemoved = gTIC - icTContains;

				DC += "DC\t" + groundTDC.size() + "\t" + dcContains + "\t"
						+ dcNewAdds + "\t" + dcRemoved + "\t" + gTDC + "\t"
						+ dcTContains + "\t" + dcTNewAdds + "\t" + dcTRemoved
						+ "\n";
				IC += "IC\t" + groundTIC.size() + "\t" + icContains + "\t"
						+ icNewAdds + "\t" + icRemoved + "\t" + gTIC + "\t"
						+ icTContains + "\t" + icTNewAdds + "\t" + icTRemoved
						+ "\n";
				lastDCCount = dcContains;
				results[0] += dcContains;
				results[1] += dcNewAdds;
				results[2] += dcRemoved;
				results[3] += dcTContains;
				results[4] += dcTNewAdds;
				results[5] += dcTRemoved;
				results[6] += icContains;
				results[7] += icNewAdds;
				results[8] += icRemoved;
				results[9] += icTContains;
				results[10] += icTNewAdds;
				results[11] += icTRemoved;
			}

			DC += "Reps\t" + maxFiles + "\t";
			IC += "Reps\t" + maxFiles + "\t";
			for (int j = 0; j < results.length; j++) {
				results[j] = results[j] / maxFiles;

				if (j < 6)
					DC += results[j] + "\t";
				else
					IC += results[j] + "\t";

				if (j == 2)
					DC += "\t";

				else if (j == 8)
					IC += "\t";
			}

			System.out.println(i + 1);
			System.out.println(DC);
			System.out.println(IC);

			System.out.println("\n");

		}

		// break;

	}

	public void analyzeForConflicts() {
		directConflicts = new ArrayList();
		inDirectConflicts = new ArrayList();
		taskConstMap = new ArrayList();

		Conflict conf;

		for (int i = 0; i < initialTask.length; i++) {
			for (int j = i + 1; j < initialTask.length; j++) {
				if (!initialTask[i].developerName
						.equals(initialTask[j].developerName)) {
					for (File masterFile : initialTask[i].filesList) {
						for (File remoteFile : initialTask[j].filesList) {
							if (remoteFile.fileName.equals(masterFile.fileName)) {
								conf = new Conflict();
								conf.fromTask = initialTask[i].taskId;
								conf.toTask = initialTask[j].taskId;
								conf.fromFile = masterFile.fileName;
								conf.toFile = remoteFile.fileName;
								if (!taskConstMap.contains("D" + conf.fromTask
										+ conf.toTask))
									taskConstMap.add("D" + conf.fromTask
											+ conf.toTask);
								if (!directConflicts.contains(conf))
									directConflicts.add(conf);
							} else {

								/*
								 * String masFile = masterFile.fileName; masFile
								 * = masFile.substring(masFile .lastIndexOf('/')
								 * + 1); String remFile = remoteFile.fileName;
								 * remFile = remFile.substring(remFile
								 * .lastIndexOf('/') + 1);
								 * 
								 * if (remFile.contains(masFile))
								 * System.out.println("ErrorD: " +
								 * masterFile.fileName + " : " +
								 * remoteFile.fileName);
								 */

							}
						}
					}
					// analyzing in direct conflicts
					for (File masterFile : initialTask[i].filesList)
						for (File remoteFile : initialTask[j].filesList)
							for (String depFile : remoteFile.dependencies) {
								{
									if (masterFile.fileName.equals(depFile)) {
										conf = new Conflict();
										conf.fromTask = initialTask[i].taskId;
										conf.toTask = initialTask[j].taskId;
										conf.fromFile = masterFile.fileName;
										conf.toFile = depFile;
										if (!taskConstMap.contains("I"
												+ conf.fromTask + conf.toTask))
											taskConstMap.add("I"
													+ conf.fromTask
													+ conf.toTask);
										if (!inDirectConflicts.contains(conf))
											inDirectConflicts.add(conf);
									} else {

										/*
										 * String masFile = masterFile.fileName;
										 * masFile = masFile.substring(masFile
										 * .lastIndexOf('/') + 1); String
										 * remFile = depFile; remFile =
										 * remFile.substring(remFile
										 * .lastIndexOf('/') + 1);
										 * 
										 * if (remFile.contains(masFile))
										 * System.out.println("ErrorI: " +
										 * masterFile.fileName + " : " +
										 * depFile);
										 */

									}
								}
							}

				}

			}
		}

		// analyzing direct conflicts

	}

	public static boolean isAlive(Process p) {
		try {
			p.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}

	// pass set to this funct...set will contain files that must be retained in
	// the new mutant.
	// do not pic at random files but pick based on teh index in set.
	// that means move required files logic upstairs...
	private Task simulateConstraintAssignment(final Task task2, Integer[] comb,
			int i, String rubyFilePath, String hash) {

		try {

			Task task25 = new Task();
			task25.percentage = i;
			task25 = task2.clone();

			int requiredFile = comb.length;
			Author ath = authorsMap.get(task25.developerName);

			List files = ath.getFiles(requiredFile, task25.filesList);

			int pickedNumer = 0;
			for (Object file : files) {
				String filename = (String) file;

				if (project.toLowerCase().equals("s")) {
					filename = filename.replaceAll("storm--src/jvm", "classes")
							.replaceAll("storm--src/clj", "classes");

					if (filename.endsWith(".java") || filename.endsWith(".clj"))
						filename = filename.split("\\.")[0];

				} else if (project.toLowerCase().equals("v")) {
					filename = filename.replaceAll("voldemort--", "");
					filename = filename.split("\\.")[0];

				}
				if (filename.trim().length() > 0) {
					task25.filesList.get(comb[pickedNumer]).fileName = filename;
					task25.filesList.get(comb[pickedNumer]).dependencies
							.clear();
				}
				pickedNumer++;
			}

			String allFiles = files.toString().replaceAll("\\[", "")
					.replaceAll("\\]", "").replaceAll(" ", "");

			if (project.toLowerCase().equals("v")) {
				allFiles = allFiles.replaceAll("voldemort--", "");
			}

			// System.out.println("ruby " + rubyFilePath + " " + hash + " " +
			// allFiles);

			Process process = Runtime.getRuntime().exec(
					"ruby " + rubyFilePath + " " + hash + " " + allFiles);

			String tmp, output = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			while (isAlive(process)) {
				while (br.ready() && (tmp = br.readLine()) != null) {
					output += tmp;
				}
			}
			while (br.ready() && (tmp = br.readLine()) != null) {
				output += tmp;
			}
			process.waitFor();
			// System.out.println(process.waitFor());
			br.close();

			String[] dataArray = output.split(",");
			for (String item : dataArray) {
				if (project.toLowerCase().equals("s")) {

					if (item.endsWith(".class"))
						item = item.split("\\.")[0];
				} else if (project.toLowerCase().equals("v")) {
					item = item.replaceAll("voldemort--", "");

					if (item.endsWith(".java"))
						item = item.split("\\.")[0];
				}
				if (item.trim().length() > 0)
					task25.filesList.get(comb[comb.length - 1]).dependencies
							.add(item);
			}
			return task25;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private Task simulateTaskAssignment(final Task task2, Integer[] comb,
			int i, String rubyFilePath, String hash) {

		try {

			Boolean reduced = false;

			Task task25 = new Task();
			task25.percentage = i;
			task25 = task2.clone();

			if (i < 0) {
				reduced = true;
				i = i * -1;
			}

			int requiredFile = comb.length;
			Author ath = authorsMap.get(task25.developerName);

			if (reduced) {
				if (task25.filesList.size() < requiredFile) {
					Arrays.sort(comb);

					for (int j = comb.length - 1; j >= 0; j--) {
						int pickedNumber = comb[j];
						task25.filesList.remove(pickedNumber);
					}
				}

			} else {

				List files = ath.getFiles(requiredFile, task25.filesList);

				File fileT = null;

				for (Object file : files) {
					String filename = (String) file;

					if (project.toLowerCase().equals("s")) {
						filename = filename.replaceAll("storm--src/jvm",
								"classes").replaceAll("storm--src/clj",
								"classes");

						if (filename.endsWith(".java")
								|| filename.endsWith(".clj"))
							filename = filename.split("\\.")[0];

					} else if (project.toLowerCase().equals("v")) {
						filename = filename.replaceAll("voldemort--", "");
						filename = filename.split("\\.")[0];

					}
					if (filename.trim().length() > 0) {
						fileT = new File();
						fileT.fileName = filename;

						task25.filesList.add(fileT);

					}
				}

				String allFiles = files.toString().replaceAll("\\[", "")
						.replaceAll("\\]", "").replaceAll(" ", "");

				if (project.toLowerCase().equals("v")) {
					allFiles = allFiles.replaceAll("voldemort--", "");
				}

				// System.out.println("ruby " + rubyFilePath + " " + hash + " "
				// +
				// allFiles);

				Process process = Runtime.getRuntime().exec(
						"ruby " + rubyFilePath + " " + hash + " " + allFiles);

				String tmp, output = "";
				BufferedReader br = new BufferedReader(new InputStreamReader(
						process.getInputStream()));

				while (isAlive(process)) {
					while (br.ready() && (tmp = br.readLine()) != null) {
						output += tmp;
					}
				}
				while (br.ready() && (tmp = br.readLine()) != null) {
					output += tmp;
				}
				process.waitFor();
				// System.out.println(process.waitFor());
				br.close();

				String[] dataArray = output.split(",");
				for (String item : dataArray) {
					if (project.toLowerCase().equals("s")) {

						if (item.endsWith(".class"))
							item = item.split("\\.")[0];
					} else if (project.toLowerCase().equals("v")) {
						item = item.replaceAll("voldemort--", "");

						if (item.endsWith(".java"))
							item = item.split("\\.")[0];
					}
					if (item.trim().length() > 0)
						task25.filesList.get(task25.filesList.size() - 1).dependencies
								.add(item);
				}
			}

			return task25;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private boolean contains(List<Conflict> inDirectConfs, Conflict conf) {

		return inDirectConfs.contains(conf);
		/*
		 * for (Conflict confl : inDirectConfs) { if
		 * (confl.fromFile.equals(conf.fromFile) &&
		 * confl.toFile.equals(conf.toFile) && confl.fromTask == conf.fromTask
		 * && confl.toTask == conf.toTask) return true; } // TODO Auto-generated
		 * method stub return false;
		 */

	}

	private void processSubsets(int n, int k) {
		allCombinations = new int[n];
		for (int i = 0; i < n; i++)
			allCombinations[i] = i;

		int[] subset = new int[k];
		combinations = new ArrayList();
		processLargerSubsets(allCombinations, subset, 0, 0);
	}

	private boolean processLargerSubsets(int[] set, int[] subset,
			int subsetSize, int nextIndex) {
		if (subsetSize == subset.length) {
			Integer[] newArray = new Integer[subset.length];
			int i = 0;
			for (int value : subset) {
				newArray[i++] = Integer.valueOf(value);
			}

			combinations.add(newArray);

			if (combinations.size() >= this.median)
				return false;
		} else {
			for (int j = nextIndex; j < set.length; j++) {
				subset[subsetSize] = set[j];
				if (!processLargerSubsets(set, subset, subsetSize + 1, j + 1))
					return false;
			}
		}
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		WeeklySimulator sim = new WeeklySimulator();
		sim.project = args[0];
		sim.median = Integer.parseInt(args[1]);
		sim.percentage = Integer.parseInt(args[2]);
		sim.simulationType = args[3];
		sim.runEnvironment = args[4];
		sim.xmlFileName = args[5];
		//sim.printSummary();
		sim.startSimulation();

	}

	public void printSummary() {
		
		if (project.toLowerCase().equals("s")) {
			if (runEnvironment.toLowerCase().equals("l"))
				path = "/Users/bkasi/Documents/Research/DAScripts/";
			else
				path = "/work/esquared/bkasi/DataAnalysis/Storm/";

		} else if (project.toLowerCase().equals("v")) {
			if (runEnvironment.toLowerCase().equals("l"))
				path = "/Users/bkasi/Documents/Research/DAScripts/";
			else
				path = "/work/esquared/bkasi/DataAnalysis/Voldemort2/";
		}

		String rubyFilePath = "", xmlFilePath = "";

		if (project.toLowerCase().equals("s")) {
			xmlFilePath = path + xmlFileName;
		} else if (project.toLowerCase().equals("v")) {
			xmlFilePath = path + xmlFileName;
		}

		String hash = "";
		Document doc = Utils.openTaskList(xmlFilePath);
		Element root = doc.getDocumentElement();

		NodeList allCommits = root.getElementsByTagName("Commit");

		System.out.println("Printing Summary for " + xmlFileName);
		System.out.println("No of Weeks:" + allCommits.getLength());

		int allFilesCount = 0;
		int alldevscount = 0;
		int alltaskscount = 0;
		List<String> devs = new ArrayList();

		for (int i = 0; i < allCommits.getLength(); i++) {

			// i =5;
			Node comit = allCommits.item(i);
			Element element = (Element) comit;

			alltaskscount += element.getElementsByTagName("Remote").getLength() + 1;

			Element masterElement = (Element) element.getElementsByTagName(
					"Master").item(0);

			devs.add(masterElement.getAttribute("DevName"));
			allFilesCount += masterElement.getElementsByTagName("File")
					.getLength();

			for (int j = 1; j <= element.getElementsByTagName("Remote")
					.getLength(); j++) {
				Element remoteElement = (Element) element.getElementsByTagName(
						"Remote").item(j - 1);

				String devName = remoteElement.getAttribute("DevName");

				if (!devs.contains(devName))
					devs.add(devName);

				allFilesCount += remoteElement.getElementsByTagName("File")
						.getLength();

			}
			
			alldevscount += devs.size();
			devs.clear();
		}
		
		float avg = (alldevscount+0.0f)/(allCommits.getLength()+0.0f);
		System.out.println("Avg devs per week:" + avg);
		avg = (alltaskscount+0.0f)/(allCommits.getLength()+0.0f);
		System.out.println("Avg tasks per week:" + avg);
		avg = (allFilesCount+0.0f)/(alldevscount+0.0f);
		System.out.println("Avg files per tasks:" + avg);
	}

	private static void addFilesFor(Task task, NodeList allFiles) {

		for (int i = 0; i < allFiles.getLength(); i++) {
			Element file = (Element) allFiles.item(i);
			File fileElem = new File();
			String fileName = file.getAttribute("FileName");
			boolean trun = true;

			if (project.toLowerCase().equals("s")) {
				if (fileName.endsWith(".java") || fileName.endsWith(".clj"))
					trun = true;
				else
					trun = false;
			}

			if (trun)
				fileElem.fileName = fileName.split("\\.")[0];
			else
				fileElem.fileName = fileName;

			if (file.hasChildNodes()) {
				NodeList depList = file.getElementsByTagName("Dependency");

				for (int j = 0; j < depList.getLength(); j++) {

					Element dependecy = (Element) depList.item(j);
					fileElem.dependencies.add(dependecy
							.getAttribute("FileName").split("\\.")[0]);
				}

			}
			if (fileElem.fileName.trim().length() > 0)
				task.filesList.add(fileElem);

		}

	}

}
