package cse.unl.edu.Simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

		try {
			String contents = Utils
					.readFile("/Users/bkasi/Documents/Research/DAScripts/storm.ids");

			String xmlstr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Merges><Commit MergeId=\"0d863929027453aafdd65f81772604d03986c8fb\"><Master DevName=\"Nathan Marz\"><File FileName=\"storm--test/clj/backtype/storm/scheduler_test.clj\"></File><File FileName=\"storm--test/clj/backtype/storm/nimbus_test.clj\"></File><File FileName=\"storm--test/clj/backtype/storm/integration_test.clj\"></File><File FileName=\"storm--src/storm.thrift\"></File><File FileName=\"storm--src/py/storm/Nimbus.py\"></File><File FileName=\"classes/backtype/storm/scheduler/SchedulerAssignmentImpl.java\"><Dependency FileName=\"classes/backtype/storm/scheduler/ExecutorDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SchedulerAssignment.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/WorkerSlot.class\"></Dependency></File><File FileName=\"classes/backtype/storm/scheduler/SchedulerAssignment.java\"><Dependency FileName=\"classes/backtype/storm/scheduler/ExecutorDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/WorkerSlot.class\"></Dependency></File><File FileName=\"classes/backtype/storm/scheduler/Cluster.java\"><Dependency FileName=\"classes/backtype/storm/Config.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/ExecutorDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SchedulerAssignment.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SchedulerAssignmentImpl.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SupervisorDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/Topologies.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/TopologyDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/WorkerSlot.class\"></Dependency></File><File FileName=\"classes/backtype/storm/generated/Nimbus.java\"><Dependency FileName=\"classes/backtype/storm/Config.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/ExecutorDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SchedulerAssignment.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SchedulerAssignmentImpl.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SupervisorDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/Topologies.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/TopologyDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/WorkerSlot.class\"></Dependency></File><File FileName=\"classes/backtype/storm/Config.java\"><Dependency FileName=\"classes/backtype/storm/Config.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/ExecutorDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SchedulerAssignment.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SchedulerAssignmentImpl.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/SupervisorDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/Topologies.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/TopologyDetails.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/WorkerSlot.class\"></Dependency></File><File FileName=\"classes/backtype/storm/scheduler/EvenScheduler.clj\"><Dependency FileName=\"classes/backtype/storm/scheduler/Cluster.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/IScheduler.class\"></Dependency><Dependency FileName=\"classes/backtype/storm/scheduler/Topologies.class\"></Dependency></File><File FileName=\"classes/backtype/storm/daemon/nimbus.clj\"><Dependency FileName=\"classes/backtype/storm/scheduler/INimbus.class\"></Dependency></File><File FileName=\"classes/backtype/storm/daemon/common.clj\"></File></Master><Remote DevName=\"\"><File FileName=\"Nathan Marz\"></File><File FileName=\"storm--test/clj/backtype/storm/nimbus_test.clj\"></File></Remote></Commit></Merges>";

			String[] hashIds = contents.split("\n");

			int kk = 0;

			//String hash = "8283fba12859c819375489b58467c8d232f1e2a2";
			for (String hash : hashIds) {
			

			kk++;
			// if (kk < 4)
			// continue;

			// if (kk == 5)
			// break;

			//System.out.println("ruby " + rubyFilePath + " " + hash);
			
			Process process = Runtime.getRuntime().exec(
					"ruby " + rubyFilePath + " " + hash);
			
			//process.waitFor();
			
			String tmp, output = "";

			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			
			while(isAlive(process))
			{
				while (br.ready() && (tmp = br.readLine()) != null) {
					output += tmp;
				}
				
			}
			while (br.ready() && (tmp = br.readLine()) != null) {
				output += tmp;
			}
			
			process.waitFor();
			br.close();

			// System.out.println(output);

			Document doc = Utils.openTaskList(output);
			Element root = doc.getDocumentElement();

			NodeList allCommits = root.getElementsByTagName("Commit");

			for (int i = 0; i < allCommits.getLength(); i++) {
				Node comit = allCommits.item(i);
				Element element = (Element) comit;

				Merge merge = new Merge();

				merge.mergeId = element.getAttribute("MergeId");

				Element masterElement = (Element) element.getElementsByTagName(
						"Master").item(0);
				Element remoteElement = (Element) element.getElementsByTagName(
						"Remote").item(0);

				merge.masterDevName = masterElement.getAttribute("DevName");
				merge.remoteDevName = remoteElement.getAttribute("DevName");

				addFilesFor(merge, masterElement.getElementsByTagName("File"),
						true);
				addFilesFor(merge, remoteElement.getElementsByTagName("File"),
						false);

				// System.out.println(merge.mergeId);
				// System.out.println(merge.masterDevName);
				// System.out.println("\n\n" + merge.remoteDevName);
				// System.out.println(merge.masterFiles);
				//System.out.println(merge.remoteFiles);

				merge.analyzeForConflicts();

				//System.out.println(merge.inDirectConflicts.size());
				
				

				// System.out.println("conflicts: ::");
				// System.out.println(merge.directConflicts);

				for (Conflict conf : merge.inDirectConflicts) {
					// System.out.println(conf.fromFile + " : " + conf.toFile);
				}

				/*
				 * System.out.println("For 25");
				 * this.simulateConstraintAssignment(merge, 25, rubyFilePath,
				 * hash);
				 * 
				 * System.out.println("\nFor 50");
				 * this.simulateConstraintAssignment(merge, 50, rubyFilePath,
				 * hash);
				 * 
				 * System.out.println("\nFor 75");
				 */
				this.simulateConstraintAssignment(merge, 75, rubyFilePath, hash);

				}
				 //break;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public static boolean isAlive( Process p ) {
	    try
	    {
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

			//System.out.println(merge25.remoteFiles.size());
			//System.out.println("reqs' " + requiredFile);

			if (count - requiredFile - merge25.remoteFiles.size() <= 0) {
				System.out.println("Skipped");
				return;
			}

			List files = ath.getFiles(requiredFile, merge25.remoteFiles);

			List<Integer> exclude = new ArrayList();

			int pickedNumer = 0;

			for (Object file : files) {
				String filename = (String) file;

				pickedNumer = Utils.getRandomNumber(0,
						merge25.remoteFiles.size() - 1, exclude);
				exclude.add(pickedNumer);
				
				filename = filename.replaceAll("storm--src/jvm", "classes").replaceAll("storm--src/clj", "classes").split("\\.")[0];

				merge25.remoteFiles.get(pickedNumer).fileName = filename;
				merge25.remoteFiles.get(pickedNumer).dependencies.clear();

			}

			// System.out.println("Selected: ");
			// System.out.println(files);

			String allFiles = files.toString().replaceAll("\\[", "")
					.replaceAll("\\]", "").replaceAll(" ", "");

			Process process = Runtime.getRuntime().exec(
					"ruby " + rubyFilePath + " " + hash + " " + allFiles);

			// System.out.println("ruby " + rubyFilePath + " " + hash
			// + " " + allFiles);

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
				merge25.remoteFiles.get(pickedNumer).dependencies.add(item);
			}

			//System.out.println("\n remote: " + merge25.masterFiles);
			//System.out.println("\n remote: " + merge25.remoteFiles);
			
			

			// System.out.println("\n\n-----------------------");

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

			// System.out.println(merge25.masterFiles);
			// System.out.println(merge25.remoteFiles);

			System.out.println("DC Conflicts Orignal : "
					+ merge.directConflicts.size() + "\t"
					+ dcContains + "\t" + dcNewAdds + "\t"
					+ dcRemoved);
			System.out.println("IC Conflicts Orignal : "
					+ merge.inDirectConflicts.size() + "\t"
					+ icContains + "\t" + icNewAdds + "\t"
					+ icRemoved);

			for (Conflict conf : merge25.directConflicts) {
				// System.out.println("D " + conf.fromFile + " : " +
				// conf.toFile);
			}

			for (Conflict conf : merge25.inDirectConflicts) {
				// System.out.println(conf.fromFile + " : " + conf.toFile);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean contains(List<Conflict> inDirectConflicts, Conflict conf) {
		
		
		for(Conflict confl : inDirectConflicts)
		{
			if(confl.fromFile.equals(conf.fromFile) && confl.toFile.equals(conf.toFile))
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
