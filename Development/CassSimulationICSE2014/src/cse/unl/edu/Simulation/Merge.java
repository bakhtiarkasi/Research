package cse.unl.edu.Simulation;

import java.util.ArrayList;
import java.util.List;

public class Merge {

	public String mergeId;
	public String masterDevName;
	public String remoteDevName;
	public int percentage = 0;

	public List<Conflict> directConflicts;
	public List<Conflict> inDirectConflicts;

	public List<File> masterFiles;
	public List<File> remoteFiles;

	public Merge() {
		masterFiles = new ArrayList();
		remoteFiles = new ArrayList();
	}

	public void analyzeForConflicts() {
		directConflicts = new ArrayList();
		inDirectConflicts = new ArrayList();
		Conflict conf;

		// analyzing direct conflicts
		for (File masterFile : masterFiles) {
			for (File remoteFile : remoteFiles) {
				if (remoteFile.fileName.equals(masterFile.fileName)) {
					conf = new Conflict();
					conf.fromFile = masterFile.fileName;
					conf.toFile = remoteFile.fileName;
					directConflicts.add(conf);
				}
			}
		}
		// analyzing in direct conflicts
		for (File masterFile : masterFiles)
			for (File remoteFile : remoteFiles)
			for (String depFile : remoteFile.dependencies) {
				 {
					if (masterFile.fileName.equals(depFile)) {
						conf = new Conflict();
						conf.fromFile = masterFile.fileName;
						conf.toFile = remoteFile.fileName;
						inDirectConflicts.add(conf);
					}
				}
			}
		
		
	}
	
	@Override
    public Merge clone() {
		Merge merge = new Merge();
		merge.mergeId = this.mergeId;
		merge.masterDevName = this.masterDevName;
		merge.remoteDevName = this.remoteDevName;
		merge.percentage = this.percentage;

		merge.directConflicts = new ArrayList(this.directConflicts);
		merge.inDirectConflicts = new ArrayList(this.inDirectConflicts);

		merge.masterFiles = new ArrayList(this.masterFiles);
		merge.remoteFiles = new ArrayList(this.remoteFiles);
		
        return merge;
    }

	public class File {
		public String fileName;
		public List<String> dependencies;

		public File() {
			dependencies = new ArrayList();
		}

		public String toString() {
			return fileName + "{" + dependencies.toString() + "}";

		}
	}

	public class Conflict {
		public String fromFile;
		public String toFile;
	}
}
