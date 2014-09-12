package helper.r;

import rleano.util.FileUtil;
import rleano.util.RUtil;

public class RHelper {

	public static void main(String[] args) {
		
		String filename = "rcode/output/pca_save.rout";
		String contents = FileUtil.readFile(filename);
		
		String[] pieces = contents.split("\n\n");
		
		FileUtil.writeLine(filename, "", true);
		for (String piece : pieces) {
			
			String[] results = RUtil.formatResults(piece, "\t", true);

			for (String line : results) {
				FileUtil.writeLine(filename, line + "\n", false);
			}
			
			FileUtil.writeLine(filename, "\n", false);
		}
		
	}

}
