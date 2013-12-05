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
					if(!directConflicts.contains(conf))
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
						conf.toFile = depFile;
						if(!inDirectConflicts.contains(conf))
							inDirectConflicts.add(conf);
					}
				}
			}
		
		
	}
	
	@Override
    public Merge clone() {
		Merge merge2 = new Merge();
		merge2.mergeId = this.mergeId;
		merge2.masterDevName = this.masterDevName;
		merge2.remoteDevName = this.remoteDevName;
		merge2.percentage = this.percentage;

		merge2.directConflicts = new ArrayList();
		for(Conflict c : this.directConflicts)
			merge2.directConflicts.add(c.clone());
		
		merge2.inDirectConflicts = new ArrayList();
		for(Conflict c : this.inDirectConflicts)
			merge2.inDirectConflicts.add(c.clone());		
		
		merge2.masterFiles = new ArrayList();
		for(File f : this.masterFiles)
			merge2.masterFiles.add(f.clone());
		
		
		merge2.remoteFiles = new ArrayList();
		for(File f : this.remoteFiles)
			merge2.remoteFiles.add(f.clone());
		
        return merge2;
    }
}
