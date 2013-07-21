package cse.unl.edu.Simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cse.unl.edu.Simulation.Merge.File;
import cse.unl.edu.util.Utils;

public class Author {

	public String authorName;
	public int allFilesCount = 0;

	Map<Integer, List> filesMap;

	public Author() {
		filesMap = new HashMap();

	}

	public Author(String authorName, String files) {
		this();
		this.authorName = authorName;
		this.populateFiles(files);
		int i = 10;
		i = 20;

		// System.out.println(filesMap);

	}

	private void populateFiles(String files) {

		String[] fileNames = files.split("\n");
		this.allFilesCount = fileNames.length;

		String[] fileInfo = new String[2];
		int lastCount = -1;
		int keys = -1;

		for (int i = 0; i < fileNames.length; i++) {
			fileInfo = fileNames[i].split(" : ");

			if (lastCount != -1 && lastCount == Integer.parseInt(fileInfo[1])) {
				filesMap.get(keys).add(fileInfo[0]);
			} else {
				lastCount = Integer.parseInt(fileInfo[1]);
				keys++;

				filesMap.put(keys, new ArrayList());
				filesMap.get(keys).add(fileInfo[0]);
			}
		}
	}

	public List getFiles(int count, List<File> remoteFiles) {

		if (count > allFilesCount)
			return null;

		List files = new ArrayList();

		int requiredFiles = count;

		for (int i = 0; i < filesMap.size(); i++) {
			if (requiredFiles > 0) {
				files.addAll(getRandomFiles(filesMap.get(i), requiredFiles,
						remoteFiles));
				requiredFiles = count - files.size();
			} else
				break;
		}

		return files;
	}

	private Collection getRandomFiles(List list, int requiredFiles,
			List<File> remoteFiles) {

		List files = new ArrayList();

		if (requiredFiles >= list.size()) {
			for (Object file : list) {
				String fileName = (String) file;
				fileName = fileName.replaceAll("storm--src/jvm", "classes")
						.replaceAll("storm--src/clj", "classes").split("\\.")[0];

				boolean bAdd = true;
				for (File remFile : remoteFiles) {
					if (remFile.fileName.equals(fileName)) {
						bAdd = false;
					}
				}

				if (bAdd)
					files.add(file);
			}
			return files;
		}

		List<Integer> exclude = new ArrayList();

		for (int i = 0; i < requiredFiles; i++) {
			int pickedNumer = Utils.getRandomNumber(0, requiredFiles, exclude);
			exclude.add(pickedNumer);

			String fileName = (String) list.get(pickedNumer);
			// fileName = fileName.replaceAll("storm--src/jvm",
			// "classes").replaceAll("storm--src/clj",
			// "classes").split("//.")[0];

			String tempFile = fileName.replaceAll("storm--src/jvm", "classes")
					.replaceAll("storm--src/clj", "classes").split("\\.")[0];

			boolean bAdd = true;
			for (File file : remoteFiles) {
				if (file.fileName.equals(tempFile)) {
					i--;
					bAdd = false;
					;
				}
			}

			if (bAdd)
				files.add(fileName);
		}

		return files;
	}

	public static void main(String[] args) {

		String fileContents = "storm--test/clj/backtype/storm/integration_test.clj : 3\n"
				+ "storm--src/jvm/backtype/storm/Config.java : 3\n"
				+ "storm--src/clj/backtype/storm/testing.clj : 3\n"
				+ "storm--src/clj/backtype/storm/messaging/zmq.clj : 1\n"
				+ "storm--src/clj/backtype/storm/messaging/local.clj : 1\n"
				+ "storm--src/clj/backtype/storm/daemon/worker.clj : 1\n"
				+ "storm--src/clj/backtype/storm/daemon/task.clj : 1";

		Author auth = new Author("Nathan", fileContents);

		/*
		 * 
		 * int i = 25; int requiredFile = (int) Math.round(i * 0.01 * 22);
		 * 
		 * String fileName="storm--test/clj/zilch/test/mq.clj";
		 * 
		 * fileName = fileName.replaceAll("storm--src/jvm",
		 * "classes").replaceAll("storm--src/clj", "classes").split("\\.")[0];
		 * 
		 * System.out.println(fileName);
		 * 
		 * fileName = fileName.split("\\.")[0]; System.out.println(fileName);
		 */
	}

}
