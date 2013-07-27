package cse.unl.edu.Simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cse.unl.edu.Simulation.Merge.Conflict;
import cse.unl.edu.Simulation.Merge.File;
import cse.unl.edu.util.Utils;

public class Simulator {

	HashMap<String, Author> authorsMap;

	public Simulator() {
		authorsMap = new HashMap();
		this.loadAuthorFileCount();
	}

	private void loadAuthorFileCount() {
		String stormFileCount = "/Users/bkasi/Documents/Research/DAScripts/StormFileCounts.txt";

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
		String rubyFilePath = "/Users/bkasi/Documents/Research/DAScripts/StormDFIcse.rb";

		String hash = "";
		Document doc = Utils
				.openTaskList("/Users/bkasi/Documents/Research/DAScripts/StromMerges.xml");
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

			// System.out.println(merge.mergeId);
			// System.out.println(merge.masterDevName);
			// System.out.println("\n\n" + merge.remoteDevName);
			// System.out.println(merge.masterFiles);
			// System.out.println(merge.remoteFiles);

			merge.analyzeForConflicts();

			/*
			 * System.out.println("For 25");
			 * this.simulateConstraintAssignment(merge, 25, rubyFilePath, hash);
			 * 
			 * System.out.println("\nFor 50");
			 * this.simulateConstraintAssignment(merge, 50, rubyFilePath, hash);
			 * 
			 * System.out.println("\nFor 75");
			 */

			for (int j = 0; j < 15; j++) {
				this.simulateConstraintAssignment(merge, 25, rubyFilePath, hash);
				this.simulateConstraintAssignment(merge, 50, rubyFilePath, hash);
				this.simulateConstraintAssignment(merge, 75, rubyFilePath, hash);

				simulateTaskAssignment(merge, 25, rubyFilePath, hash);
				simulateTaskAssignment(merge, -25, rubyFilePath, hash);

			}
			System.out.println("");

			// simulateTaskAssignment(merge, 25, rubyFilePath, hash);
			// simulateTaskAssignment(merge, -25, rubyFilePath, hash);

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

	private void simulateConstraintAssignment(Merge merge, int i,
			String rubyFilePath, String hash) {

		try {

			Merge merge25 = new Merge();
			merge25 = merge.clone();

			merge25.percentage = i;

			Author ath = authorsMap.get(merge25.remoteDevName);

			int count = ath.allFilesCount;
			int requiredFile = (int) Math.round(i * 0.01
					* merge25.remoteFiles.size());

			requiredFile = requiredFile == 0 ? 1 : requiredFile;

			if ((count - requiredFile - merge25.remoteFiles.size()) <= 0) {
				System.out.println("Skipped");
				return;
			}

			List files = ath.getFiles(requiredFile, merge25.remoteFiles);

			List<Integer> exclude = new ArrayList();

			int pickedNumer = 0;

			for (Object file : files) {
				String filename = (String) file;

				pickedNumer = Utils.getRandomNumber(0,
						merge25.remoteFiles.size() - 1, exclude, true);
				exclude.add(pickedNumer);

				filename = filename.replaceAll("storm--src/jvm", "classes")
						.replaceAll("storm--src/clj", "classes").split("\\.")[0];

				merge25.remoteFiles.get(pickedNumer).fileName = filename;
				merge25.remoteFiles.get(pickedNumer).dependencies.clear();

			}

			String allFiles = files.toString().replaceAll("\\[", "")
					.replaceAll("\\]", "").replaceAll(" ", "");

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

				item = item.split("\\.")[0];
				merge25.remoteFiles.get(pickedNumer).dependencies.add(item);
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

			System.out.println("DC : " + merge25.remoteFiles.size() + "\t"
					+ requiredFile + "\t" + merge.directConflicts.size() + "\t"
					+ dcContains + "\t" + dcNewAdds + "\t" + dcRemoved);

			System.out.println("IC : " + merge25.remoteFiles.size() + "\t"
					+ requiredFile + "\t" + merge.inDirectConflicts.size()
					+ "\t" + icContains + "\t" + icNewAdds + "\t" + icRemoved);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void simulateTaskAssignment(Merge merge, int i,
			String rubyFilePath, String hash) {

		try {

			Boolean reduced = false;
			Merge merge25 = new Merge();
			merge25 = merge.clone();

			merge25.percentage = i;

			if (i < 0) {
				reduced = true;
				i = i * -1;
			}

			Author ath = authorsMap.get(merge25.remoteDevName);

			int count = ath.allFilesCount;
			int requiredFile = (int) Math.round(i * 0.01
					* merge25.remoteFiles.size());

			requiredFile = requiredFile == 0 ? 1 : requiredFile;

			if (reduced) {
				if (merge25.remoteFiles.size() == requiredFile) {
					System.out.println("Skipped");
					return;
				}
				int pickedNumer = 0;
				int size = merge25.remoteFiles.size() - 1;
				for (int j = 0; j < requiredFile; j++) {
					pickedNumer = Utils.getRandomNumber(0, size, null, true);
					merge25.remoteFiles.remove(pickedNumer);
					size--;
				}
			} else {

				if (count - requiredFile - merge25.remoteFiles.size() <= 0) {
					System.out.println("Skipped");
					return;
				}

				List files = ath.getFiles(requiredFile, merge25.remoteFiles);

				File fileT = null;

				for (Object file : files) {
					String filename = (String) file;

					filename = filename.replaceAll("storm--src/jvm", "classes")
							.replaceAll("storm--src/clj", "classes")
							.split("\\.")[0];

					fileT = merge25.new File();
					fileT.fileName = filename;

					merge25.remoteFiles.add(fileT);
				}

				String allFiles = files.toString().replaceAll("\\[", "")
						.replaceAll("\\]", "").replaceAll(" ", "");

				Process process = Runtime.getRuntime().exec(
						"ruby " + rubyFilePath + " " + hash + " " + allFiles);

				process.waitFor();
				// System.out.println(process.waitFor());

				String tmp = "";
				String output = "";

				BufferedReader br = new BufferedReader(new InputStreamReader(
						process.getInputStream()));

				while ((tmp = br.readLine()) != null) {
					output += tmp;
				}
				br.close();

				String[] dataArray = output.split(",");
				for (String item : dataArray) {

					item = item.split("\\.")[0];
					fileT.dependencies.add(item);
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
			String reqs = (reduced ? "" : "+") + requiredFile;

			System.out.println("DC : " + merge25.remoteFiles.size() + "\t"
					+ reqs + "\t" + merge.directConflicts.size() + "\t"
					+ dcContains + "\t" + dcNewAdds + "\t" + dcRemoved);

			System.out.println("IC : " + merge25.remoteFiles.size() + "\t"
					+ reqs + "\t" + merge.inDirectConflicts.size()
					+ "\t" + icContains + "\t" + icNewAdds + "\t" + icRemoved);

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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Simulator sim = new Simulator();
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
			File fileElem = merge.new File();
			fileElem.fileName = file.getAttribute("FileName").split("\\.")[0];

			if (file.hasChildNodes()) {
				NodeList depList = file.getElementsByTagName("Dependency");

				for (int j = 0; j < depList.getLength(); j++) {

					Element dependecy = (Element) depList.item(j);
					fileElem.dependencies.add(dependecy
							.getAttribute("FileName").split("\\.")[0]);
				}

			}
			filesList.add(fileElem);

		}

	}

}
