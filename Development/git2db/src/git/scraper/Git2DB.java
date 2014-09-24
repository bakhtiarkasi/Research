package git.scraper;

import git.scraper.db.DBConnector;
import git.scraper.pojo.Commit;
import git.scraper.pojo.FileData;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jira.scraper.HistoryScraper;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import rleano.util.ArrayUtil;
import rleano.util.CollectionUtil;
import rleano.util.FileUtil;
import rleano.util.GitUtil;
import rleano.util.MathUtil;
import rleano.util.PrintUtil;
import rleano.util.PropUtil;
import rleano.util.RUtil;
import rleano.util.Util;

@SuppressWarnings("unused")
public class Git2DB {

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	public static void main(String[] args) {

		String syntax = "Git2DB -[cfrauxjo] -i <number> [-d]";

		CommandLineParser parser = new BasicParser();
		HelpFormatter formatter = new HelpFormatter();
		Options options = new Options();

		Option help = new Option("h", "print this message");
		Option desc = new Option("d", "sort descending");

		OptionGroup action = new OptionGroup();
		Option fill = new Option("c", "fills Commits from .git to MySQL");
		Option find = new Option("f",
				"finds FileData (from commit and files) and adds it to DB");
		Option build = new Option("a",
				"builds Adjacency tables to rcode/files folder");
		Option rcent = new Option("r",
				"runs R to find centrality using the adjacency tables");
		Option complex = new Option("x", "updates compleXity into DB");
		Option jira = new Option("j", "convert jira XML issues to database");
		Option opened = new Option("o",
				"analyze jira XML and website for history of Opened issues");

		action.setRequired(true);
		action.addOption(fill);
		action.addOption(find);
		action.addOption(build);
		action.addOption(rcent);
		action.addOption(complex);
		action.addOption(jira);
		action.addOption(opened);

		@SuppressWarnings("static-access")
		Option init = OptionBuilder.withLongOpt("integer-option")
				.withDescription("used to paginate the filedata action")
				.withType(Number.class).hasArg().withArgName("init")
				.create("i");

		options.addOptionGroup(action);
		options.addOption(init);
		options.addOption(desc);
		options.addOption(help);

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (args.length == 0 || line.hasOption("h")) {
				formatter.printHelp(syntax, options);
				return;
			}

			int count = line.hasOption("c") ? 1 : 0;
			count += line.hasOption("f") ? 1 : 0;
			count += line.hasOption("a") ? 1 : 0;
			count += line.hasOption("r") ? 1 : 0;
			count += line.hasOption("x") ? 1 : 0;
			count += line.hasOption("j") ? 1 : 0;
			count += line.hasOption("o") ? 1 : 0;

			if (count != 1) {
				throw new ParseException(
						" -[cfarx] ONE and only ONE option needs to be specified");
			}

			int start = -1;
			if (line.hasOption("i") == true) {
				start = ((Number) line.getParsedOptionValue("i")).intValue();
			}

			boolean descending = line.hasOption("d") ? true : false;

			Git2DB git2db = new Git2DB(descending);

			if (line.hasOption("c")) {

				// git2db.getCommitsForProject();
				//git2db.getCommits();
				git2db.updateCommitDates();
			} else if (line.hasOption("f")) {
				git2db.getFileData(start);
			} else if (line.hasOption("a")) {
				git2db.getAdjacency();
			} else if (line.hasOption("r")) {
				git2db.getCentrality();
			} else if (line.hasOption("o")) {
				git2db.getActions();
			} else {
				git2db.updateComplexity();
			}

		} catch (ParseException exp) {
			System.out.println("Error: " + exp.getMessage());
			formatter.printHelp(syntax, options);
		}
	}

	private void getActions() {
		HistoryScraper scraper = new HistoryScraper();
		FileUtil.writeLine("errors.log", "", true);
		File folder = new File("files/issues");
		File[] files = folder.listFiles();
		// boolean ok = false;
		for (File file : files) {
			// if (file.getName().equals("hbase_2013-01-03.xml")){
			// ok = true;
			// }
			// if (ok == true) {
			scraper.getIssues("files/issues/" + file.getName());
			// }
		}

	}

	private String project;
	private String gitFolder;
	private PropUtil props;
	private String[] extensions;
	private String[] exceptions;
	boolean desc = false;

	public Git2DB(boolean desc) {
		this.props = new PropUtil("config.properties");
		this.project = this.props.get("project.name");
		this.gitFolder = Util.fixPath(this.props.get("git.home")
				+ this.props.get("project.location") + this.project);
		this.extensions = this.props.getArray("extensions");
		this.exceptions = this.props.getArray("file.exceptions");
		this.desc = desc;

		print("gitFolder: " + this.gitFolder);
	}

	public void getCommits() {
		List<String> hashes = GitUtil.getCommits(this.gitFolder, "HEAD", null);
		DBConnector db = new DBConnector(this.props);
		db.createConnection();

		FileUtil.writeLine("log.txt", "hashes\n", true);

		for (String hash : hashes) {
			FileUtil.writeLine("log.txt", hash + "\n", false);

			Commit commit = new Commit(hash, this.gitFolder);
			String comment = GitUtil.getCommitComment(this.gitFolder, hash);

			int id = db.insertCommit(commit, comment, this.gitFolder);
			int other;
			if (id == -1) {
				System.err.println("FINISHING EARLY");
				return;
			}

			for (String parent : commit.getParents()) {
				other = db.insertParent(id, hash, parent);
				if (other == -1) {
					System.err.println("FINISHING EARLY");
					return;
				}
			}

			Set<String> files = GitUtil.getFiles(this.gitFolder, hash);
			for (String file : files) {
				other = db.insertFile(file, hash, id,
						FileUtil.getExtension(file));
				if (other == -1) {
					System.err.println("FINISHING EARLY");
					return;
				}
			}
		}
		db.close();
	}

	public void updateCommitDates() {
		List<String> hashes = GitUtil.getCommits(this.gitFolder, "HEAD", null);
		DBConnector db = new DBConnector(this.props);
		db.createConnection();

		FileUtil.writeLine("log.txt", "hashes\n", true);

		for (String hash : hashes) {
			FileUtil.writeLine("log.txt", hash + "\n", false);

			Commit commit = new Commit(hash, this.gitFolder);
			db.updateCommitDate(commit);
			db.close();
		}
	}

	private void getFileData(int start) {
		int year = this.props.getInt("year.init");
		int endYear = this.props.getInt("year.end");
		int month = this.props.getInt("month.init");
		int endMonth = this.props.getInt("month.end");
		String loadFile = this.props.get("file.load");
		String monthString = Util.formatNumber("00", (double) month);
		String initDate = year + "-" + monthString + "-01";
		monthString = Util.formatNumber("00", (double) endMonth);
		String endDate = endYear + "-" + monthString + "-01";

		DBConnector db = new DBConnector(props);
		db.createConnection();
		List<FileData> datas = null;
		if (start == -1) {
			datas = db.getFiledata(this.extensions, initDate, endDate, desc);
		} else {
			start--;
			datas = db.getFiledata(start * 10, 10, this.extensions, initDate,
					endDate, desc);
		}
		db.close();

		print("Total: " + datas.size());

		int counter = 0;

		for (FileData data : datas) {
			counter++;
			print(counter + "\t" + data.getCommitHash() + " - "
					+ data.getFilename());
			print("\t\t\t\t\t" + Util.getTime());
			log(counter + "\t" + data.getCommitHash() + " - "
					+ data.getFilename());

			db.createConnection();
			if (db.checkFileData(data) == false
					&& this.isException(data) == false) {

				db.getPastAuthorsFrequency(data);
				db.close();
				print("\t\tGetting file contents\t" + Util.getTime());

				// get file contents
				String extension = FileUtil.getExtension(data.getFilename());
				String contents = Util.cmdExec(
						("git show " + data.getCommitHash() + ":" + data
								.getFilename()).split(" "), this.gitFolder);
				print("\t\tGot Contents\t" + Util.getTime());
				FileUtil.writeFile("temp_file." + extension, contents);

				String cloc = "./cloc-1.56.pl temp_file." + extension
						+ " --quiet --csv";
				String[] output = Util.cmdExec(cloc.split(" "), "./").split(
						"\n");
				try {
					String[] result = output[2].split(",");
					int code = new Integer(result[4]);
					int comment = new Integer(result[3]);
					data.setCode(code);
					data.setComment(comment);
					data.setLoc(code + comment);
					double temp = MathUtil.div(comment, comment + code);
					float ratio = new Float(temp);
					data.setCommentRatio(ratio);
				} catch (IndexOutOfBoundsException e) {
					data.setCode(0);
					data.setComment(0);
					data.setLoc(0);
					data.setCommentRatio(0);
				}

				print("\t\tGot LOC [" + data.getLoc() + "]");
				log("\t\tGot LOC [" + data.getLoc() + "]");

				db.createConnection();
				db.insertFileData(data);
				db.close();
			}
		}

	}

	private boolean isException(FileData data) {
		String fullpath = data.getFilename();
		String filename = FileUtil.getSimpleFilename(data.getFilename());
		if (fullpath.contains("/generated/") == true) {
			return true;
		}

		for (String exception : this.exceptions) {
			if (filename.equalsIgnoreCase(exception) == true) {
				return true;
			}
		}
		return false;
	}

	private void getFileDataFiles(int start) {

		int year = this.props.getInt("year.init");
		int endYear = this.props.getInt("year.end");
		int month = this.props.getInt("month.init");
		int endMonth = this.props.getInt("month.end");
		String loadFile = this.props.get("file.load");
		String monthString = Util.formatNumber("00", (double) month);
		String initDate = year + "-" + monthString + "-01";
		monthString = Util.formatNumber("00", (double) endMonth);
		String endDate = endYear + "-" + monthString + "-01";

		DBConnector db = new DBConnector(props);
		db.createConnection();
		List<FileData> datas = null;
		if (start == -1) {
			datas = db.getFiledata(this.extensions, initDate, endDate, desc);
		} else {
			start--;
			datas = db.getFiledata(start * 10, 10, this.extensions, initDate,
					endDate, desc);
		}
		db.close();

		print("Total: " + datas.size());

		int counter = 0;

		for (FileData data : datas) {
			counter++;
			print(counter + "\t" + data.getCommitHash() + " - "
					+ data.getFilename());
			print("\t\t\t\t\t" + Util.getTime());
			log(counter + "\t" + data.getCommitHash() + " - "
					+ data.getFilename());

			db.createConnection();
			if (db.checkFileData(data) == false) {

				db.getPastAuthorsFrequency(data);
				db.close();
				print("\t\tGetting file contents\t" + Util.getTime());

				// get file contents
				String extension = FileUtil.getExtension(data.getFilename());
				String contents = Util.cmdExec(
						("git show " + data.getCommitHash() + ":" + data
								.getFilename()).split(" "), this.gitFolder);
				print("\t\tGot Contents\t" + Util.getTime());

				// write it in a special folder
				String newFilename = "files/" + data.getCommitHash() + "/"
						+ data.getFilename();
				FileUtil.writeFile(newFilename, contents);

				// String cloc = "./cloc-1.56.pl temp_file." + extension +
				// " --quiet --csv";
				// String[] output = Util.cmdExec(cloc.split(" "),
				// this.props.get("git.home") + "git2db").split("\n");
				// try {
				// String[] result = output[2].split(",");
				// int code = new Integer(result[4]);
				// int comment = new Integer(result[3]);
				// data.setCode(code);
				// data.setComment(comment);
				// data.setLoc(code + comment);
				// double temp = MathUtil.div(comment, comment + code);
				// float ratio = new Float(temp);
				// data.setCommentRatio(ratio);
				// } catch (IndexOutOfBoundsException e) {
				// data.setCode(0);
				// data.setComment(0);
				// data.setLoc(0);
				// data.setCommentRatio(0);
				// }
				//
				// print("\t\tGot LOC [" + data.getLoc() + "]");
				// log("\t\tGot LOC [" + data.getLoc() + "]");
				//
				// db.createConnection();
				// db.insertFileData(data);
				// db.close();
			}
		}

	}

	public void getAdjacency() {
		String filename = this.props.get("file.load");
		String folder = this.props.get("file.folder");
		int year = this.props.getInt("year.init");
		int endYear = this.props.getInt("year.end");
		int month = this.props.getInt("month.init");
		int endMonth = this.props.getInt("month.end");
		String monthString = Util.formatNumber("00", (double) month);

		DBConnector db = new DBConnector(props);

		Set<String> allFiles = new LinkedHashSet<String>();
		int[][] oldAdjacency = null;

		if (filename != null && filename.isEmpty() == false) {
			this.print("Reading file: " + filename);
			String[] date = filename.split("-");
			year = Integer.parseInt(date[0]);
			month = Integer.parseInt(date[1].substring(0, 2));
			month++;
			if (month == 13) {
				month = 1;
				year++;
			}
			monthString = Util.formatNumber("00", (double) month);
			this.print("Year-Month: " + year + "-" + monthString);

			String[] recoveredFiles = FileUtil.readCSVFirst(folder + filename,
					true);
			allFiles.addAll(Arrays.asList(recoveredFiles));
			this.print("\tfiles: " + allFiles.size());

			this.print("\t...reading data ");
			List<String[]> data = FileUtil.readCSV(folder + filename, false);
			this.print("\tdata: " + data.size());
			oldAdjacency = new int[data.size()][data.size()];

			int counter = 0;
			for (String[] line : data) {
				oldAdjacency[counter++] = ArrayUtil.StringToInt(line);
			}
		}

		while (year <= endYear) {
			while (month <= 12
					&& this.beforeEquals(year, month, endYear, endMonth)) {
				monthString = Util.formatNumber("00", (double) month);
				int counter = 0;
				this.print(year + "-" + monthString);

				db.createConnection();
				List<String> files = db.getFilesByYearMonth(year, monthString,
						this.extensions);
				db.close();
				allFiles.addAll(files);
				this.print("\tFiles: " + files.size() + " [" + allFiles.size()
						+ "]");

				int[][] adjacency = this.copyAdjacency(oldAdjacency,
						allFiles.size());

				db.createConnection();
				List<Integer> ids = db
						.getCommitIdByYearMonth(year, monthString);
				db.close();

				this.print("\tCommits: " + ids.size());

				// for each commit
				for (Integer id : ids) {
					db.createConnection();
					List<String> filesCommit = db.getFilesByCommit(id);
					db.close();

					for (String file : filesCommit) {
						int posFile = CollectionUtil.getPosition(files, file,
								true);
						for (String neighbor : filesCommit) {
							if (file.equals(neighbor) == false) {
								int posNeighbor = CollectionUtil.getPosition(
										files, neighbor, true);
								if (posNeighbor == -1) {
									// System.err.println("neigh not found: [" +
									// neighbor + "]");
								} else if (posFile == -1) {
									// System.err.println("file not found: [" +
									// file + "]");
								} else {
									adjacency[posFile][posNeighbor]++;
								}
							}

						}
					}

					counter++;
					if (counter % 25 == 0) {
						this.print("ids ... " + counter + "\t\t"
								+ Util.getTime());
					}
				}

				String outputFile = folder + year + "-" + monthString + ".csv";
				this.print("Writing file: " + outputFile + "\t"
						+ Util.getTime());

				String result = PrintUtil.printCollection(allFiles, ",") + "\n";
				FileUtil.writeLine(outputFile, result, true);
				for (int[] row : adjacency) {
					FileUtil.writeLineArrayInt(outputFile, row, ",", false);
				}

				this.print("Endeng writing file. \t\t" + Util.getTime());

				oldAdjacency = adjacency;
				month++;
			}
			month = 1;
			year++;
		}
		db.close();
	}

	private int[][] copyAdjacency(int[][] old, int size) {
		int[][] adjacency = new int[size][size];
		for (int row = 0; row < adjacency.length; row++) {
			for (int col = 0; col < adjacency.length; col++) {
				if (old != null) {
					if (row < old.length && col < old.length) {
						adjacency[row][col] = old[row][col];
					} else {
						adjacency[row][col] = 0;
					}
				} else {
					adjacency[row][col] = 0;
				}
			}
		}

		return adjacency;
	}

	private void getCentrality() {

		DBConnector db = new DBConnector(props);
		String folder = this.props.get("file.folder");
		int year = this.props.getInt("year.init");
		int endYear = this.props.getInt("year.end");
		int month = this.props.getInt("month.init");
		int endMonth = this.props.getInt("month.end");
		String loadFile = this.props.get("file.load");
		String monthString = Util.formatNumber("00", (double) month);

		if (loadFile != null && loadFile.isEmpty() == false) {
			this.print("Reading file: " + loadFile);
			String[] date = loadFile.split("-");
			year = Integer.parseInt(date[0]);
			month = Integer.parseInt(date[1].substring(0, 2));
			monthString = Util.formatNumber("00", (double) month);
		}

		while (year <= endYear) {
			while (month <= 12
					&& this.beforeEquals(year, month, endYear, endMonth)) {
				monthString = Util.formatNumber("00", (double) month);
				String filenameNoExt = year + "-" + monthString;

				this.print("Current: " + filenameNoExt + "\t\t"
						+ Util.getTime());
				String[] files = FileUtil.readCSVFirst(folder + filenameNoExt
						+ ".csv", true);

				// Run R
				String infile = filenameNoExt + ".csv";
				String outfile = filenameNoExt + ".cent";
				if (FileUtil.fileExists(folder + outfile) == false) {
					String result = this.runCentrality(
							this.props.get("r.folder"), "files/" + infile,
							"files/" + outfile);
					print(result + "\n\t\t\t\t" + Util.getTime());
				}
				// read generated file
				List<String> centrality = FileUtil.readFileList(folder
						+ outfile);
				print("Files: " + centrality.size());

				db.createConnection();
				int index = 0;
				for (String file : files) {
					String value = centrality.get(index);
					Float temp = new Float(value);
					int affected = db.updateCentrality(file, filenameNoExt,
							temp);
					// this.print(affected + "\t[" + file + "] on [" +
					// filenameNoExt + "] --> " + temp );
					index++;

					if (index % 25 == 0) {
						print("\t..." + index);
					}
				}
				db.close();
				month++;
			}
			month = 1;
			year++;
		}

	}

	/**
	 * @param workingDir
	 *            where the R script is, and the working directory for R
	 * @param infile
	 *            where R will find the input file (relative to working dir)
	 * @param outfile
	 *            where R will put the outfile (relative to working dir)
	 */
	private String runCentrality(String workingDir, String infile,
			String outfile) {
		workingDir = Util.fixPath(workingDir);
		String[] params = { workingDir, infile, outfile };
		String result = RUtil.runScript("centrality.r", workingDir, params);
		return result;
	}

	private void updateComplexity() {
		String folder = this.props.get("file.folder");
		String file = "complexity.csv";
		List<String[]> contents = FileUtil.readCSV(folder + file, false);
		DBConnector db = new DBConnector(props);
		db.createConnection();
		int size = contents.size();
		print("Total: " + size);

		int count = 0;
		for (String[] line : contents) {
			String filename = line[0];
			String date = line[1];
			float complexity = Float.parseFloat(line[2]);

			db.updateComplexity(filename, date, complexity);

			if (count % 25 == 0) {
				print("\t... " + count + " / " + size);
			}
			count++;
		}

		db.close();

	}

	private boolean beforeEquals(int yearA, int monthA, int yearB, int monthB) {
		if (yearA < yearB) {
			return true;
		} else if (yearA == yearB) {
			if (monthA <= monthB) {
				return true;
			}
		}
		return false;
	}

	private void print(String msg) {
		System.out.println(msg);
		FileUtil.writeLine(this.getClass().getSimpleName().toLowerCase()
				+ ".log", msg + "\n", false);
	}

	private void log(String msg) {
		FileUtil.writeLine(this.getClass().getSimpleName().toLowerCase()
				+ ".log", msg + "\n", false);
	}

	private void getCommitsForProject() {

		if (this.project.equalsIgnoreCase("Mylyn")) {
			String projects[] = new String[8];

			projects[0] = "org.eclipse.mylyn";
			projects[1] = "org.eclipse.mylyn.builds";
			projects[2] = "org.eclipse.mylyn.commons";
			projects[3] = "org.eclipse.mylyn.context";
			projects[4] = "org.eclipse.mylyn.docs";
			projects[5] = "org.eclipse.mylyn.reviews";
			projects[6] = "org.eclipse.mylyn.tasks";
			projects[7] = "org.eclipse.mylyn.versions";

			for (int i = 0; i < projects.length; i++) {
				this.gitFolder = Util.fixPath(this.props.get("git.home")
						+ this.props.get("project.location") + "Mylyn/"
						+ projects[i]);
				this.getCommits();
			}

		} else
			this.getCommits();

	}

}
