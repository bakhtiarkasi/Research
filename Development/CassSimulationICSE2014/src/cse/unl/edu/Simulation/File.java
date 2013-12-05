package cse.unl.edu.Simulation;

import java.util.ArrayList;
import java.util.List;

public class File  {
	public String fileName;
	public List<String> dependencies;

	public File() {
		dependencies = new ArrayList();
	}

	public String toString() {
		return fileName + "{" + dependencies.toString() + "}";

	}

	@Override
	public File clone() {
		File f = new File();
		f.fileName = this.fileName;
		f.dependencies = new ArrayList(this.dependencies);
		
		return f;
	}
}
