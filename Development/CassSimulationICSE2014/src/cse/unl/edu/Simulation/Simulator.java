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
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cse.unl.edu.util.Utils;

public class Simulator {

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

	private String path = "";
	private String runEnvironment;
	private String xmlFileName;
	private String simulationType;

	public Simulator() {
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
			rubyFilePath = path + "StormDFIcse.rb";
			xmlFilePath = path + xmlFileName;
		} else if (project.toLowerCase().equals("v")) {
			rubyFilePath = path + "VoldemortDFIcse.rb";
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

			Merge merge = new Merge();

			merge.mergeId = element.getAttribute("MergeId");
			hash = element.getAttribute("MergeId");

			Element masterElement = (Element) element.getElementsByTagName(
					"Master").item(0);
			Element remoteElement = (Element) element.getElementsByTagName(
					"Remote").item(0);

			merge.masterDevName = masterElement.getAttribute("DevName");
			merge.remoteDevName = remoteElement.getAttribute("DevName");

			addFilesFor(merge, masterElement.getElementsByTagName("File"), true);
			addFilesFor(merge, remoteElement.getElementsByTagName("File"),
					false);

			merge.analyzeForConflicts();

			DC = "";
			IC = "";

			Author ath = authorsMap.get(merge.remoteDevName);

			int count = ath.allFilesCount;
			int setSize = Math.abs(percentage);
			int requiredFile = (int) Math.round(setSize * 0.01
					* merge.remoteFiles.size());

			requiredFile = requiredFile == 0 ? 1 : requiredFile;

			System.out.println(i + 1);

			if (simulationType.toLowerCase().equals("b")) {
				results = new float[6];
				
				for (int k = 0; k < 3; k++)
					this.simulateBaseCase(merge, rubyFilePath, hash);

			} else {

				if (percentage > 0
						&& (count - requiredFile - merge.remoteFiles.size()) <= 0) {
					System.out.println("Skipped");
					continue;
				}

				if (merge.remoteFiles.size() <= this.median) {

					processSubsets(merge.remoteFiles.size(), requiredFile);

					results = new float[6];

					for (Integer[] comb : combinations) {
						if (simulationType.toLowerCase().equals("f"))
							this.simulateConstraintAssignment(merge, comb,
									percentage, rubyFilePath, hash);
						else
							this.simulateTaskAssignment(merge, comb,
									percentage, rubyFilePath, hash);
					}
				} else {

					combinations = new ArrayList();
					List<Integer> hashCodes = new ArrayList();
					List<Integer> vals = new ArrayList();
					results = new float[6];

					// in while loop
					Integer[] selectedSet;
					boolean bContinue = true;
					double prevStdDeviation = 0.0;
					double tempStdDev;
					int iCount = 0;

					while (bContinue) {
						selectedSet = Utils.pickKRandomArray(
								merge.remoteFiles.size(), requiredFile);

						if (!hashCodes.contains(Arrays.hashCode(selectedSet))) {
							combinations.add(selectedSet);
							hashCodes.add(Arrays.hashCode(selectedSet));

							if (simulationType.toLowerCase().equals("f"))
								this.simulateConstraintAssignment(merge,
										selectedSet, percentage, rubyFilePath,
										hash);
							else
								this.simulateTaskAssignment(merge, selectedSet,
										percentage, rubyFilePath, hash);

							// vals.add((int) (results[0]));
							vals.add(lastDCCount);

							if (vals.size() >= 2) {
								tempStdDev = Utils.standardDeviation(vals);

								// System.out.println(prevStdDeviation + " " +
								// tempStdDev + " : " +
								// Math.abs(prevStdDeviation -
								// tempStdDev));

								if (Math.abs(prevStdDeviation - tempStdDev) < 0.15) {
									iCount++;
									if (iCount >= 5)
										bContinue = false;
								}

								prevStdDeviation = tempStdDev;
							}

							if (combinations.size() == Utils.choose(
									merge.remoteFiles.size(), requiredFile))
								bContinue = false;
						}

					}

				}
			}

			int combSize = combinations != null ? combinations.size() : 1;
			DC += "\t\tReps\t" + combSize + "\t\t";
			IC += "\t\tReps\t" + combSize + "\t\t";
			for (int j = 0; j < results.length; j++) {
				results[j] = results[j] / combSize;

				if (j < 3)
					DC += results[j] + "\t";
				else
					IC += results[j] + "\t";
			}

			System.out.println(DC);
			System.out.println(IC);

			System.out.println("\n");

		}

		// break;

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
	private void simulateConstraintAssignment(final Merge merge,
			Integer[] comb, int i, String rubyFilePath, String hash) {

		try {

			Merge merge25 = new Merge();
			merge25.percentage = i;
			merge25 = merge.clone();

			int requiredFile = comb.length;
			Author ath = authorsMap.get(merge25.remoteDevName);

			List files = ath.getFiles(requiredFile, merge25.remoteFiles);

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
					merge25.remoteFiles.get(comb[pickedNumer]).fileName = filename;
					merge25.remoteFiles.get(comb[pickedNumer]).dependencies
							.clear();
				}
				pickedNumer++;
			}

			String allFiles = files.toString().replaceAll("\\[", "")
					.replaceAll("\\]", "").replaceAll(" ", "");

			if (project.toLowerCase().equals("v")) {
				allFiles = allFiles.replaceAll("voldemort--", "");
			}

			// System.out.println("ruby " + rubyFilePath + " " + hash + " "
			// + allFiles);

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
				}

				else if (project.toLowerCase().equals("v")) {
					item = item.replaceAll("voldemort--", "");

					if (item.endsWith(".java"))
						item = item.split("\\.")[0];
				}
				if (item.trim().length() > 0)
					merge25.remoteFiles.get(comb[comb.length - 1]).dependencies
							.add(item);
			}

			merge25.analyzeForConflicts();

			int dcContains = 0;
			int dcNewAdds = 0;
			int dcRemoved = 0;

			for (Conflict conf : merge25.directConflicts) {
				if (contains(merge.directConflicts, conf)) {
					dcContains++;
				} else {
					dcNewAdds++;
				}
			}
			dcRemoved = merge.directConflicts.size() - dcContains;

			int icContains = 0;
			int icNewAdds = 0;
			int icRemoved = 0;

			for (Conflict conf : merge25.inDirectConflicts) {
				if (contains(merge.inDirectConflicts, conf)) {
					icContains++;
				} else {
					icNewAdds++;
				}
			}
			icRemoved = merge.inDirectConflicts.size() - icContains;

			DC += "DC\t" + merge25.masterFiles.size() + "\t"
					+ merge25.remoteFiles.size() + "\t" + requiredFile + "\t"
					+ merge.directConflicts.size() + "\t" + dcContains + "\t"
					+ dcNewAdds + "\t" + dcRemoved + "\n";
			IC += "IC\t" + merge25.masterFiles.size() + "\t"
					+ merge25.remoteFiles.size() + "\t" + requiredFile + "\t"
					+ merge.inDirectConflicts.size() + "\t" + icContains + "\t"
					+ icNewAdds + "\t" + icRemoved + "\n";
			lastDCCount = dcContains;
			results[0] += dcContains;
			results[1] += dcNewAdds;
			results[2] += dcRemoved;
			results[3] += icContains;
			results[4] += icNewAdds;
			results[5] += icRemoved;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void simulateTaskAssignment(final Merge merge, Integer[] comb,
			int i, String rubyFilePath, String hash) {

		try {

			Boolean reduced = false;

			Merge merge25 = new Merge();
			merge25.percentage = i;
			merge25 = merge.clone();

			if (i < 0) {
				reduced = true;
				i = i * -1;
			}

			int requiredFile = comb.length;
			Author ath = authorsMap.get(merge25.remoteDevName);

			// when removing files, we use combinations, since files can be
			// replaced in many different ways
			if (reduced) {
				if (merge25.remoteFiles.size() == requiredFile) {
					System.out.println("Skipped");
					return;
				}

				Arrays.sort(comb);

				for (int j = comb.length - 1; j >= 0; j--) {
					int pickedNumber = comb[j];
					merge25.remoteFiles.remove(pickedNumber);
				}

			}
			// when adding new files, we just add them to the end
			else {

				List files = ath.getFiles(requiredFile, merge25.remoteFiles);

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

						merge25.remoteFiles.add(fileT);
					}

				}

				String allFiles = files.toString().replaceAll("\\[", "")
						.replaceAll("\\]", "").replaceAll(" ", "");

				if (project.toLowerCase().equals("v")) {
					allFiles = allFiles.replaceAll("voldemort--", "");
				}

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
					}

					else if (project.toLowerCase().equals("v")) {
						item = item.replaceAll("voldemort--", "");

						if (item.endsWith(".java"))
							item = item.split("\\.")[0];
					}
					if (item.trim().length() > 0)
						merge25.remoteFiles.get(merge25.remoteFiles.size() - 1).dependencies
								.add(item);
				}

			}

			merge25.analyzeForConflicts();

			int dcContains = 0;
			int dcNewAdds = 0;
			int dcRemoved = 0;

			for (Conflict conf : merge25.directConflicts) {
				if (contains(merge.directConflicts, conf)) {
					dcContains++;
				} else {
					dcNewAdds++;
				}
			}
			dcRemoved = merge.directConflicts.size() - dcContains;

			int icContains = 0;
			int icNewAdds = 0;
			int icRemoved = 0;

			for (Conflict conf : merge25.inDirectConflicts) {
				if (contains(merge.inDirectConflicts, conf)) {
					icContains++;
				} else {
					icNewAdds++;
				}
			}
			icRemoved = merge.inDirectConflicts.size() - icContains;

			DC += "DC\t" + merge25.masterFiles.size() + "\t"
					+ merge25.remoteFiles.size() + "\t" + requiredFile + "\t"
					+ merge.directConflicts.size() + "\t" + dcContains + "\t"
					+ dcNewAdds + "\t" + dcRemoved + "\n";
			IC += "IC\t" + merge25.masterFiles.size() + "\t"
					+ merge25.remoteFiles.size() + "\t" + requiredFile + "\t"
					+ merge.inDirectConflicts.size() + "\t" + icContains + "\t"
					+ icNewAdds + "\t" + icRemoved + "\n";
			lastDCCount = dcContains;
			results[0] += dcContains;
			results[1] += dcNewAdds;
			results[2] += dcRemoved;
			results[3] += icContains;
			results[4] += icNewAdds;
			results[5] += icRemoved;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void simulateBaseCase(final Merge merge, String rubyFilePath,
			String hash) {

		try {

			Merge merge25 = new Merge();
			merge25 = merge.clone();
			Author ath = authorsMap.get(merge25.remoteDevName);

			List files = ath.getFilesForBaseCase(merge25.remoteFiles.size());
			
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
					merge25.remoteFiles.get(pickedNumer).fileName = filename;
					merge25.remoteFiles.get(pickedNumer).dependencies.clear();
				}
				pickedNumer++;
			}

			String allFiles = files.toString().replaceAll("\\[", "")
					.replaceAll("\\]", "").replaceAll(" ", "");

			if (project.toLowerCase().equals("v")) {
				allFiles = allFiles.replaceAll("voldemort--", "");
			}

			// System.out.println("ruby " + rubyFilePath + " " + hash + " "
			// + allFiles);

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
				}

				else if (project.toLowerCase().equals("v")) {
					item = item.replaceAll("voldemort--", "");

					if (item.endsWith(".java"))
						item = item.split("\\.")[0];
				}
				if (item.trim().length() > 0)
					merge25.remoteFiles.get(merge25.remoteFiles.size() - 1).dependencies
							.add(item);
			}

			merge25.analyzeForConflicts();

			int dcContains = 0;
			int dcNewAdds = 0;
			int dcRemoved = 0;

			for (Conflict conf : merge25.directConflicts) {
				if (contains(merge.directConflicts, conf)) {
					dcContains++;
				} else {
					dcNewAdds++;
				}
			}
			dcRemoved = merge.directConflicts.size() - dcContains;

			int icContains = 0;
			int icNewAdds = 0;
			int icRemoved = 0;

			for (Conflict conf : merge25.inDirectConflicts) {
				if (contains(merge.inDirectConflicts, conf)) {
					icContains++;
				} else {
					icNewAdds++;
				}
			}
			icRemoved = merge.inDirectConflicts.size() - icContains;

			DC += "DC\t" + merge25.masterFiles.size() + "\t"
					+ merge25.remoteFiles.size() + "\t"
					+ merge25.remoteFiles.size() + "\t"
					+ merge.directConflicts.size() + "\t" + dcContains + "\t"
					+ dcNewAdds + "\t" + dcRemoved + "\n";
			IC += "IC\t" + merge25.masterFiles.size() + "\t"
					+ merge25.remoteFiles.size() + "\t"
					+ merge25.remoteFiles.size() + "\t"
					+ merge.inDirectConflicts.size() + "\t" + icContains + "\t"
					+ icNewAdds + "\t" + icRemoved + "\n";
			lastDCCount = dcContains;
			results[0] += dcContains;
			results[1] += dcNewAdds;
			results[2] += dcRemoved;
			results[3] += icContains;
			results[4] += icNewAdds;
			results[5] += icRemoved;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean contains(List<Conflict> inDirectConflicts, Conflict conf) {

		for (Conflict confl : inDirectConflicts) {
			if (confl.fromFile.equals(conf.fromFile)
					&& confl.toFile.equals(conf.toFile))
				return true;
		}
		// TODO Auto-generated method stub
		return false;

	}

	private void processSubsets(int n, int k) {
		allCombinations = new int[n];
		for (int i = 0; i < n; i++)
			allCombinations[i] = i;

		int[] subset = new int[k];
		combinations = new ArrayList();
		processLargerSubsets(allCombinations, subset, 0, 0);
	}

	private void processLargerSubsets(int[] set, int[] subset, int subsetSize,
			int nextIndex) {
		if (subsetSize == subset.length) {
			Integer[] newArray = new Integer[subset.length];
			int i = 0;
			for (int value : subset) {
				newArray[i++] = Integer.valueOf(value);
			}
			combinations.add(newArray);
		} else {
			for (int j = nextIndex; j < set.length; j++) {
				subset[subsetSize] = set[j];
				processLargerSubsets(set, subset, subsetSize + 1, j + 1);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Simulator sim = new Simulator();
		sim.project = args[0];
		sim.median = Integer.parseInt(args[1]);
		sim.percentage = Integer.parseInt(args[2]);
		sim.simulationType = args[3];
		sim.runEnvironment = args[4];
		sim.xmlFileName = args[5];
		sim.startSimulation();

	}

	private static void addFilesFor(Merge merge, NodeList allFiles,
			boolean master) {

		List<File> filesList;

		if (master)
			filesList = merge.masterFiles;
		else
			filesList = merge.remoteFiles;

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
				filesList.add(fileElem);

		}

	}

}
