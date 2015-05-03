package cse.unl.edu.Lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.Version;

import cse.unl.edu.Framework.DBConnector;
import cse.unl.edu.Framework.Task;
import cse.unl.edu.Framework.TaskDownloader;
import cse.unl.edu.util.JCommanderArgs;
import cse.unl.edu.util.Utils;

import org.tartarus.snowball.ext.PorterStemmer;

import com.beust.jcommander.JCommander;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

//-type n -ids 100671,102243,107146,113848,174993,213661,225538,391121 -tagger taggers/left3words-wsj-0-18.tagger
// mylyn 100627,195727,193423,165088,155333,172580,135413,143567,195691,207228,101374,120790,219649,133113,117799,406470,134295,253790,172777,163139,113848,100671,102243,107146,174993,213661,225538,391121
// hbase HBASE-10031,HBASE-8355,HBASE-8832,HBASE-9745,HBASE-9998,HBASE-10024,HBASE-10644,HBASE-11234,HBASE-6479,HBASE-7114,HBASE-7575,HBASE-8101,HBASE-8344,HBASE-8902,HBASE-9446,HBASE-9994,HBASE-9205
// derby  -ids DERBY-1,DERBY-100,DERBY-1000,DERBY-1001,DERBY-1002,DERBY-1004,DERBY-1005,DERBY-1006,DERBY-1007,DERBY-1009,DERBY-1010,DERBY-1014,DERBY-1015,DERBY-1016,DERBY-1019,DERBY-1024,DERBY-1025,DERBY-1028,DERBY-1029,DERBY-1030
public class LuceneIndexer {

	/**
	 * @param args
	 */

	ArrayList<String> StopWords;
	MaxentTagger tagger;
	IndexWriter indexWriter;
	StringBuffer nouns;
	StringBuffer verbs;
	TaskDownloader tw;
	Map<String, Integer> fileFrequency;
	boolean nominal = false;

	public final String descriptionField = "nouns";

	String results[];
	private List<Task> allTasksList;
	String resultText = "";
	String resulNLPtText = "";
	String commitResultText = "";

	int res[];
	private int seedSize;
	private String globalHtml;
	private String finalHtml;
	private String finalResults;

	public final static Logger LOGGER = Logger.getLogger(LuceneIndexer.class
			.getName());

	public LuceneIndexer() {
		nouns = new StringBuffer();
		verbs = new StringBuffer();

		StopWords = new ArrayList();
		StopWords.addAll(Arrays.asList("a", "an", "and", "are", "as", "at",
				"be", "but", "by", "for", "if", "in", "into", "is", "it", "no",
				"not", "of", "on", "or", "such", "that", "the", "their",
				"then", "there", "these", "they", "this", "to", "was", "will",
				"with", "i"));

		LOGGER.setLevel(Level.INFO);
		FileHandler fileHandler = null;
		try {
			fileHandler = new FileHandler("myLogFileH");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in LuceneIndexer() " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in LuceneIndexer() " + e.getMessage());
		}
		LOGGER.addHandler(fileHandler);

	}

	public void initializeforDescription(String idsForAnalysis) {
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_47);

		// 1. create the index
		Directory index = new RAMDirectory();

		DBConnector db = new DBConnector();
		db.createConnection();

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
				analyzer);

		try {

			// "100671,102243,107146,113848,174993,213661,225538,391121"
			indexWriter = new IndexWriter(index, config);
			List<Task> taskList = db.getIssueDescription(idsForAnalysis);

			this.allTasksList = taskList;

			for (int i = 0; i < allTasksList.size(); i++) {
				Task task = taskList.get(i);
				addDocumentToIndexWithDesc(task);
			}

			LOGGER.info("All docs have been added \n");
			indexWriter.close();

			IndexReader reader = DirectoryReader.open(index);
			Fields fields = MultiFields.getFields(reader);
			Terms terms = fields.terms(descriptionField);
			TermsEnum te = terms.iterator(null);
			while (te.next() != null) {
				te.term().utf8ToString();
			}

			res = new int[5]; // 1,3,5,7,10
			
			
			for (int i = 0; i < allTasksList.size(); i++) {

				Task task = taskList.get(i);
				String querystr = task.longDescription + " " + task.comments;		
				
				querystr = QueryParser.escape(querystr);
				BooleanQuery.setMaxClauseCount(80000);

				Query q = new QueryParser(Version.LUCENE_47, descriptionField,
						analyzer).parse(querystr);

				LOGGER.info("\n Query: " + q);

				// 3. search
				int hitsPerPage = 10;

				IndexSearcher searcher = new IndexSearcher(reader);
				TopScoreDocCollector collector = TopScoreDocCollector.create(
						hitsPerPage, true);
				searcher.search(q, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;

				// System.out.print("Here are results for :" + task.taskId);

				compileResultsForMurphy(hits, searcher, task.taskId);

				for (int j = 0; j < hits.length; ++j) {
					int docId = hits[j].doc;
					searcher.doc(docId);
				}
			}

			System.out.println(resultText);
			float allTasksLen = allTasksList.size() * 1.0f;
			System.out.printf(
					"Results:   1\t3\t5\t7\t10\n         %s\t%s\t%s\t%s\t%s",
					(res[0] * 1.0f) / allTasksLen, (res[1] * 1.0f)
							/ allTasksLen, (res[2] * 1.0f) / allTasksLen,
					(res[3] * 1.0f) / allTasksLen, (res[4] * 1.0f)
							/ allTasksLen);
			System.out.println(commitResultText);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initialize() " + e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initialize() " + e.getMessage());
		} finally {
			db.close();
		}
	}

	private void compileResultsForMurphy(ScoreDoc[] hits,
			IndexSearcher searcher, String taskId) {
		try {
			boolean found = false;

			Task task;
			Task firstTask = getTask(taskId);
			String subTaskid;
			List<String> interectionFiles;

			for (int j = 0; j < hits.length; ++j) {

				subTaskid = searcher.doc(hits[j].doc).get("taskid");
				task = getTask(subTaskid);
				if (!task.taskId.equals(taskId)) {

					interectionFiles = Utils.intersection(
							firstTask.getFileNamesList(false),
							task.getFileNamesList(false));

					if (interectionFiles.size() > 0) {
						this.getCommitGraphResults(firstTask, task,
								interectionFiles);
						found = true;

						switch (j) {
						case 1:
							res[0]++;
							res[1]++;
							res[2]++;
							res[3]++;
							res[4]++;
							break;
						case 2:
						case 3:
							res[1]++;
							res[2]++;
							res[3]++;
							res[4]++;
							break;
						case 4:
						case 5:
							res[2]++;
							res[3]++;
							res[4]++;
							break;
						case 6:
						case 7:
							res[3]++;
							res[4]++;
							break;
						default:
							res[4]++;
							break;
						}
						break;
					}
				}
			}

			resultText += (found == true ? "Yes" : "No") + "\n";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void compileResultsLatest(ScoreDoc[] hits, IndexSearcher searcher,
			String taskId) {

		try {

			final int FinalTaskSize = 6;

			// task that is being searched for
			Task firstTask = getTask(taskId);
			String subTaskid;

			String serachRes = taskId + "-> ";
			LOGGER.info("\n");
			LOGGER.info("Running task: " + taskId);

			if (hits.length == 0)
				return;

			// copy all doc scores into a new array excluding score for the
			// searched doc, which is usually the first in search results
			float[] scores = new float[hits.length - 1];
			int l = 0;
			for (int j = 0; j < hits.length; j++) {

				serachRes += searcher.doc(hits[j].doc).get("taskid") + ",";

				if (!searcher.doc(hits[j].doc).get("taskid").equals(taskId)) {

					if (l == scores.length)
						break;

					scores[l++] = hits[j].score;
				}
			}
			LOGGER.info(serachRes);

			// get median value for search results
			float median = getMedianValueIndex(scores);

			// change here
			median = 0.0f;

			// from search results select only docs that have a score of median
			// and above
			List<String> tasksList = new ArrayList();
			for (int j = 0; j < hits.length; j++) {
				if (!searcher.doc(hits[j].doc).get("taskid").equals(taskId)) {
					if (hits.length <= 6) {
						tasksList.add(searcher.doc(hits[j].doc).get("taskid"));
					} else {
						if (hits[j].score >= median)
							tasksList.add(searcher.doc(hits[j].doc).get(
									"taskid"));
						else
							break;
					}
				}
			}
			String tasks[] = new String[tasksList.size()];
			for (int i = 0; i < tasksList.size(); i++) {
				tasks[i] = tasksList.get(i);
			}

			String selectedFile = "";

			DBConnector db = new DBConnector();

			List<String> initList = firstTask.getFileNamesList(true);

			if (initList.size() > FinalTaskSize) {
				selectedFile = "";
				for (String str : initList) {
					if (selectedFile != "")
						selectedFile += ",";
					selectedFile += "'" + str + "'";
				}

				db.createConnection();
				initList = db.getFrequentlyEditedFilesFrom(selectedFile,
						FinalTaskSize, firstTask.fromDate);
				db.close();
			}

			// change here
			/*
			 * finalResults += "\n" + firstTask.taskId + "(" + initList.size() +
			 * ")->" + tasks.length;
			 * 
			 * for (int i = 0; i < tasks.length; i++) { Task task =
			 * getTask(tasks[i]); List<String> comm = Utils.intersection(
			 * task.getFileNamesList(true), initList); if (comm.size() > 0) {
			 * initList.removeAll(comm); finalResults += "\t" + i + ":" +
			 * comm.size() + "/" + task.getFileNamesList(true).size(); } if
			 * (initList.size() == 0) break; } if(!initList.isEmpty())
			 * finalResults += "\t" + "[" + initList.size() + "]";
			 * 
			 * 
			 * if(true) return;
			 */

			// select 3 or more file as the seed set for the commit graph, any
			// file that appears more than twice is a candidiate
			fileFrequency = null;
			fileFrequency = this.getFieFrequencyMap(tasks);
			List keys = new ArrayList(fileFrequency.keySet());

			int count = 0;
			for (int i = 0; i < keys.size(); i++) {
				if (fileFrequency.get(keys.get(i)) > 3)
					count++;
			}

			// select files median and above and send to db for ranking
			// according to file freq function.
			int middle = 0;
			selectedFile = "";

			if (count > 0) {
				middle = Math.max(count, (int) Math.floor(keys.size() / 2));
			} else {
				middle = keys.size();
			}

			// compile results here for NLP search
			db = new DBConnector();
			List<String> intersectionFiles = new ArrayList(
					fileFrequency.keySet());

			if (count > 0) {

				if (count < 3)
					count = Math.max(count, intersectionFiles.size());

				count = Math.min(count, FinalTaskSize);
				intersectionFiles = intersectionFiles.subList(0, count);
			} else {
				if (intersectionFiles.size() > FinalTaskSize) {
					selectedFile = "";
					for (String str : intersectionFiles) {
						if (selectedFile != "")
							selectedFile += ",";
						selectedFile += "'" + str + "'";
					}

					db.createConnection();
					intersectionFiles = db.getFrequentlyEditedFilesFrom(
							selectedFile, FinalTaskSize, firstTask.fromDate);
					db.close();
				}
			}
			// List<String> initList = firstTask.getFileNamesList(true);
			// initList = firstTask.getFirstCommitFile(initList);

			// initList = initList.subList(0,
			// Math.min(initList.size(), FinalTaskSize));

			int TP = Utils.intersection(intersectionFiles, initList).size();
			int FP = 0;
			int FN = 0;
			FP = intersectionFiles.size() - TP;
			FN = initList.size() - TP;

			resulNLPtText += taskId + "\t" + "NLPRes" + "\t" + count + "\t"
					+ TP + "\t" + FP + "\t" + FN + "\n";

			// to remove this block later
			/*
			 * String html = "";
			 * 
			 * Map<String, String[]> htmlMap = new HashMap();
			 * 
			 * int mapindex = -1; for (String fileName :
			 * firstTask.getFileNamesList()) { htmlMap.put(fileName, new
			 * String[hits.length]); htmlMap.get(fileName)[0] = "X"; mapindex =
			 * 0; }
			 * 
			 * for (int j = 0; j < hits.length; j++) { Task task =
			 * this.getTask(searcher.doc(hits[j].doc) .get("taskid"));
			 * 
			 * if (!task.taskId.equals(taskId)) { mapindex++;
			 * 
			 * if (mapindex == hits.length) break;
			 * 
			 * for (String fileName : task.getFileNamesList()) { if
			 * (!htmlMap.containsKey(fileName)) htmlMap.put(fileName, new
			 * String[hits.length]);
			 * 
			 * htmlMap.get(fileName)[mapindex] = "X"; } } }
			 * 
			 * float prec = TP*1.0f; prec = (TP*1.0f + FP*1.0f) == 0.0f ? 0.0f :
			 * (prec/(TP*1.0f + FP*1.0f));
			 * 
			 * float rec = TP*1.0f; rec = (TP*1.0f + FN*1.0f) == 0.0f ? 0.0f :
			 * (prec/(TP*1.0f + FN*1.0f));
			 * 
			 * appendToHtml(htmlMap, taskId, tasks[tasks.length - 1], serachRes,
			 * intersectionFiles, scores, (TP + "\t" + FP + "\t" + FN + "\tP:" +
			 * String.format("%.04f",prec)+ "\tR:"
			 * +String.format("%.04f",rec))); if (true) return;
			 */
			// end remove block here

			LOGGER.info("TP FP FN " + taskId + "\t" + "NLPRes" + "\t" + count
					+ "\t" + TP + "\t" + FP + "\t" + FN);

			intersectionFiles = null;

			// //////////////////////Till
			// here///////////////////////////////////

			if (middle <= 3)
				middle = keys.size() > 3 ? 3 : keys.size();

			keys = keys.subList(0, middle);

			selectedFile = "";
			// for file rankings
			for (Object key : keys) {
				if (selectedFile != "")
					selectedFile += ",";
				selectedFile += "'" + key.toString() + "'";
			}

			db.createConnection();

			count = seedSize;
			List<String> selectFilesList = db.getFrequentlyEditedFilesFrom(
					selectedFile, count, firstTask.fromDate);
			db.close();

			String seedFiles = "";
			for (String str : selectFilesList) {
				if (seedFiles != "")
					seedFiles += ",";
				seedFiles += "'" + str + "'";
			}

			db.createConnection();
			intersectionFiles = null;
			// Map results = new HashMap<String, List<String>>();

			// get adjacent files within the last 6 month of commits for the
			// seed set files.

			String qTaskId = "'" + taskId + "'";
			Map results = db.getCommonAdjacentFiles(seedFiles, qTaskId,
					firstTask.fromDate);
			db.close();

			// Step 0 is to check for common files in adjacent files of seed set
			// files.
			String step = "Step0";
			if (results != null) {
				for (int i = 0; i < selectFilesList.size(); i++) {
					if (results.get(selectFilesList.get(i)) != null) {
						if (intersectionFiles == null)
							intersectionFiles = (List<String>) results
									.get(selectFilesList.get(i));
						else {
							intersectionFiles = Utils.intersection(
									intersectionFiles, (List<String>) results
											.get(selectFilesList.get(i)));
							if (intersectionFiles == null
									|| intersectionFiles.size() == 0) {
								intersectionFiles = null;
								break;
							}
						}
					}
				}
			}

			// seed set could be empty becoz 1) no seed set file had adjacent
			// files, 2) no common adjacent files were found for files in seed
			// set
			if (intersectionFiles == null) {
				if (results != null) {

					LOGGER.info("Step1: Intersection in adjaceny files yields null now doing frequency");
					step = "Step1";

					List<String> uniqFiles = new ArrayList();
					fileFrequency = new HashMap();

					// make frequency matrix, if no seed set file had adjacent
					// files
					// then this step wont result in anything
					for (int i = 0; i < selectFilesList.size(); i++) {
						uniqFiles = (List<String>) results.get(selectFilesList
								.get(i));
						if (uniqFiles != null) {
							for (String fileName : uniqFiles) {
								if (fileFrequency.containsKey(fileName)) {
									fileFrequency.put(fileName,
											fileFrequency.get(fileName) + 1);
								} else {
									fileFrequency.put(fileName, 1);
								}
							}
						}
					}

					fileFrequency = Utils.sortByValue(fileFrequency, true);
					List freqKeys = new ArrayList(fileFrequency.keySet());

					// select only files adjacent to 2 or more seed set files.
					intersectionFiles = new ArrayList();
					for (Object key : freqKeys) {
						if (fileFrequency.get(key) >= 2) {
							intersectionFiles.add(key.toString());
						}
					}

					// have to improve this stpe as it will include all files =>
					// higher FP
					if (intersectionFiles.size() == 0) {
						LOGGER.info("Step3: Frequency of all files not common in 2 files");
						step = "Step3";
						intersectionFiles.addAll(freqKeys);
					}
				}
			}

			// finally make sure to include seed set files in the final list

			if (intersectionFiles == null)
				intersectionFiles = new ArrayList();

			if (intersectionFiles.size() > FinalTaskSize) {
				selectedFile = "";
				for (String str : intersectionFiles) {
					if (selectedFile != "")
						selectedFile += ",";
					selectedFile += "'" + str + "'";
				}

				db.createConnection();
				intersectionFiles = db.getFrequentlyEditedFilesFrom(
						selectedFile, FinalTaskSize, firstTask.fromDate);
				db.close();
			}

			for (Object key : selectFilesList) {
				String fileName = key.toString();
				if (!intersectionFiles.contains(fileName))
					intersectionFiles.add(fileName);
			}

			if (initList == null) {
				initList = firstTask.getFileNamesList(true);
				// initList = firstTask.getFirstCommitFile(initList);
				// initList = initList.subList(0,
				// Math.min(initList.size(), FinalTaskSize));

				if (initList.size() > FinalTaskSize) {
					selectedFile = "";
					for (String str : initList) {
						if (selectedFile != "")
							selectedFile += ",";
						selectedFile += "'" + str + "'";
					}

					db.createConnection();
					initList = db.getFrequentlyEditedFilesFrom(selectedFile,
							FinalTaskSize, firstTask.fromDate);
					db.close();
				}
			}

			/*
			 * if (initList.size() > 20) { selectedFile = ""; for (String str :
			 * initList) { if (selectedFile != "") selectedFile += ",";
			 * selectedFile += "'" + str + "'"; }
			 * 
			 * db.createConnection(); initList =
			 * db.getFrequentlyEditedFilesFrom(selectedFile, 20); db.close(); }
			 */

			TP = Utils.intersection(intersectionFiles, initList).size();
			FP = 0;
			FN = 0;
			FP = intersectionFiles.size() - TP;
			FN = initList.size() - TP;

			LOGGER.info("final target task size " + intersectionFiles.size());
			resultText += taskId + "\t" + step + "\t" + count + "\t" + TP
					+ "\t" + FP + "\t" + FN + "\n";
			LOGGER.info("TP FP FN " + taskId + "\t" + step + "\t" + count
					+ "\t" + TP + "\t" + FP + "\t" + FN);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(resultText);
			System.out
					.println("\n\n\n*******************************************\n");
			System.out.println("Printing results for NLP\n");
			System.out.println(resulNLPtText);
		}

	}

	private void appendToHtml(Map<String, String[]> htmlMap, String firstTask,
			String middleTask, String serachRes,
			List<String> intersectionFiles, float[] scores, String result) {

		globalHtml = "<table><tr><td><table border=\"1\">";

		List keys = new ArrayList(htmlMap.keySet());

		serachRes = serachRes.split("-> ")[1];

		String[] temp = serachRes.split(",");
		globalHtml += "<tr><td><b>File Name</b></td>";
		int count = 0;
		for (int i = 0; i < temp.length; i++) {
			String taskId = temp[i];
			if (taskId.equals(firstTask) || taskId.equals(middleTask))
				taskId = "<b>" + taskId + "<b/>";

			globalHtml += "<td>"
					+ taskId
					+ (temp[i].equals(firstTask) ? "" : ("<br/>" + String
							.format("%.04f", scores[count++]))) + "</td>";
		}

		globalHtml += "</tr>";

		for (int i = 0; i < keys.size(); i++) {
			globalHtml += "<tr>";

			String fileName = keys.get(i).toString();
			String trimFile = "";
			int index = fileName.lastIndexOf("/");
			if (index > 0)
				trimFile = fileName.substring(index + 1, fileName.length());

			if (intersectionFiles.contains(fileName))
				globalHtml += "<td><b>" + trimFile + "</b></td>";
			else
				globalHtml += "<td>" + trimFile + "</td>";

			String vals[] = htmlMap.get(fileName);
			for (int j = 0; j < vals.length; j++) {
				globalHtml += "<td>" + (vals[j] == null ? "" : vals[j])
						+ "</td>";
			}

			globalHtml += "</tr>";
		}

		globalHtml += "</table></td><td></td><td valign=\"top\">";
		for (String fileName : intersectionFiles) {
			String trimFile = "";
			int index = fileName.lastIndexOf("/");
			if (index > 0)
				trimFile = fileName.substring(index + 1, fileName.length());
			globalHtml += trimFile + " <hr/> ";
		}
		globalHtml += result + "<br/>";

		globalHtml += "</td></tr></table><div height=\"350px\"> &nbsp</div>";

	}

	private float getMedianValueIndex(float[] hits) {

		int middle = hits.length / 2;
		float medianValue = 0; // declare variable
		if (hits.length % 2 == 1)
			medianValue = hits[middle];
		else
			medianValue = (hits[middle - 1] + hits[middle]) / 2.0f;
		return medianValue;
	}

	public void initializeforNLP(String idsForAnalysis, String taggerPath) {
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		NLPAnalyzer analyzer = new NLPAnalyzer();

		// 1. create the index
		Directory index = new RAMDirectory();

		DBConnector db = new DBConnector();
		db.createConnection();

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
				analyzer);

		try {

			tagger = new MaxentTagger(taggerPath);

			// "100671,102243,107146,113848,174993,213661,225538,391121"
			indexWriter = new IndexWriter(index, config);

			if (idsForAnalysis != null && !idsForAnalysis.isEmpty()) {
				String tokens[] = idsForAnalysis.split(",");
				idsForAnalysis = "";
				for (int i = 0; i < tokens.length; i++) {
					if (idsForAnalysis != "")
						idsForAnalysis += ",";
					idsForAnalysis += "'" + tokens[i] + "'";
				}
			}

			// here

			IndexReader reader;
			List<Task> taskList = db.getIssueDescription(idsForAnalysis);

			this.allTasksList = taskList;

			for (int i = 0; i < allTasksList.size(); i++) {
				Task task = taskList.get(i); // here changed task: //
				addDocumentToIndexWithNLP(task);
			}

			LOGGER.info("All docs have been added \n"); // here changed task:
			indexWriter.close();

			res = new int[5]; // 1,3,5,7,10

			resultText = "";

			finalHtml = "<!DOCTYPE html>\n<html>\n<boby>\n";
			finalResults = "";

			// allTasksList = allTasksList.subList(0, 1000);
			// System.out.println(allTasksList.size());
			Set<String> t = new HashSet();

			for (int i = 0; i < allTasksList.size(); i++) {

				Task task = taskList.get(i);

				String querystr = task.longDescription + " " + task.comments;
				String[] s = querystr.split("\\s+");
				for (String tee : s)
					t.add(tee.toLowerCase().trim());

				querystr = QueryParser.escape(querystr);
				querystr = tagger.tagString(querystr);
				querystr = QueryParser.escape(querystr);
				BooleanQuery.setMaxClauseCount(80000);

				Query q = new QueryParser(Version.LUCENE_47, descriptionField,
						analyzer).parse(querystr);

				
				// LOGGER.info("\n Query: " + q);
				// LOGGER.info("\n Query: " + q.toString().replaceAll("nouns:",
				// ""));

				// for ML
				task.nouns.addAll(Arrays.asList(q.toString()
						.replaceAll("nouns:", "").split(" ")));

				if (i % 300 == 0)
					LOGGER.info("Done with " + i + " tasks "
							+ allTasksList.size());

				if (true)
					continue;

				// 3. search
				int hitsPerPage = 60;

				IndexSearcher searcher = new IndexSearcher(reader);
				TopScoreDocCollector collector = TopScoreDocCollector.create(
						hitsPerPage, true);
				searcher.search(q, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;

				compileResultsLatest(hits, searcher, task.taskId);

				// finalHtml += globalHtml + "\n";

			}
			System.out.println("Unique " + t.size());

			// finalHtml += " </body>\n</html>"; //
			// System.out.println(finalHtml);

			// System.out.println(finalResults);
			// System.out.println(resultText);
			// System.out
			// .println("\n\n\n*******************************************\n");
			// System.out.println("Printing results for NLP\n");
			// System.out.println(resulNLPtText);

			// for ML
			Set<String> attributes = new HashSet();
			Set<String> labels = new HashSet();
			List<String> filteredNouns = new ArrayList();
			for (int i = 0; i < allTasksList.size(); i++) {
				Task task = taskList.get(i);

				for (String nou : task.nouns) {

					// nou = nou.replaceAll("'|%", "");

					// RAKEL doesn't work with this //

					if (nou.length() >= 3) {
						filteredNouns.add(nou);
					}
				}
				task.nouns = new ArrayList(filteredNouns);
				filteredNouns.clear();

				attributes.addAll(task.nouns);
				labels.addAll(task.getFileNamesList(true));

				if (i % 300 == 0)
					LOGGER.info("Nouns done with " + i + " tasks ");

			}

			// here i need to make a map and check

			// here is teh part that i need to use for tfidf values?
			reader = DirectoryReader.open(index);
			Fields fields = MultiFields.getFields(reader);
			Terms terms = fields.terms(descriptionField);
			TermsEnum te = terms.iterator(null);

			Bits livedocs = MultiFields.getLiveDocs(reader);

			while (te.next() != null) {
				if (te.term().utf8ToString().length() <= 2)
					continue;

				long docCount = te.docFreq();

				DocsEnum docsEnum = MultiFields.getTermDocsEnum(reader,
						livedocs, descriptionField, te.term());

				int doc1 = DocsEnum.NO_MORE_DOCS;
				while ((doc1 = docsEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
					Task task = getTask(reader.document(doc1).get("taskid"));
					double tf = docsEnum.freq();
					double idf1 = Math
							.log10(((reader.numDocs() * 1.0) / docCount));
					/*
					 * System.out.println(te.term().utf8ToString() + " tf: " +
					 * tf + " idf: " + idf1 + " tfidf: " + String.format("%.5f",
					 * tf * idf1));
					 */
					// System.out.println(te.term().utf8ToString());

					// if (!task.tfIDF.containsKey(te.term().utf8ToString())) {

					//System.out.println("Note heres  "+
					//te.term().utf8ToString());
					

					task.tfIDF.put(te.term().utf8ToString(),
							String.format("%.5f", tf * idf1));

				}
			}

			LOGGER.info("Added nouns and files for all tasks");

			List<String> attribs = new ArrayList(attributes);
			List<String> labs = new ArrayList(labels);

			Collections.sort(attribs);
			Collections.sort(labs);

			StringBuilder arFile = new StringBuilder();
			StringBuilder xmlFile = new StringBuilder();

			arFile.append("@relation mylyn_dataset\n\n");
			xmlFile.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			xmlFile.append("<labels xmlns=\"http://mulan.sourceforge.net/labels\">\n");

			for (int i = 0; i < attribs.size(); i++) {
				if (nominal)
					arFile.append("@attribute a" + i + " {0,1}\n");
				else
					arFile.append("@attribute a" + i + " numeric\n");
			}

			for (int i = 0; i < labs.size(); i++) {
				arFile.append("@attribute TAG_" + labs.get(i) + " {0,1}\n");
				xmlFile.append("<label name=\"TAG_" + labs.get(i)
						+ "\"></label>\n");
			}
			arFile.append("\n@data\n");
			xmlFile.append("</labels>");

			for (int i = 0; i < allTasksList.size(); i++) {
				Task task = taskList.get(i);

				List<String> nouns = new ArrayList(new HashSet(task.nouns));
				List<String> files = new ArrayList(new HashSet(
						task.getFileNamesList(true)));

				LOGGER.info("task: " + task.taskId + " nouns: " + nouns.size()
						+ " files: " + files.size() + " arff len "
						+ arFile.length());

				Collections.sort(nouns);
				Collections.sort(files);

				if (nouns.size() > 0)
					arFile.append("{");

				for (int j = 0; j < nouns.size(); j++) {

					int k = attribs.indexOf(nouns.get(j));
					
					if (nominal)
						arFile.append(k + " 1,");
					else
						arFile.append(k + " " + task.tfIDF.get(nouns.get(j)) + ",");

					if (!task.tfIDF.containsKey(nouns.get(j))) {
						
						System.out.println("Not here " + nouns.get(j));

					}

				}
				for (int j = 0; j < files.size(); j++) {
					int k = labs.indexOf(files.get(j)) + attribs.size();
					arFile.append(k + " 1,");
				}

				if (nouns.size() > 0 || files.size() > 0) {
					arFile.setLength(arFile.length() - 1);
					arFile.append("}\n");
				}

				if (i % 300 == 0)
					LOGGER.info("XML done with " + i + " tasks ");
			}

			System.out.println("arf file \n" + arFile.toString());
			System.out.println("xml file \n" + xmlFile.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initialize() " + e.getMessage());
		} catch (ParseException e) { // TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initialize() " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.close();
		}
	}

	public void initialize(String idsPath, String contextFilePath,
			String taggerPath, String taskSavedFilePath) {
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

		// 1. create the index
		Directory index = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
				analyzer);

		try {

			indexWriter = new IndexWriter(index, config);

			tw = new TaskDownloader(idsPath, contextFilePath, taskSavedFilePath);
			ArrayList<Task> taskList = tw.allTasks;

			LOGGER.info("Loading Tagger");

			tagger = new MaxentTagger(taggerPath);

			int allTasks = taskList.size();
			int testTask = (int) (0.1 * allTasks);
			testTask = testTask < 1 ? 1 : testTask;
			int trainingTask = allTasks - testTask;

			LOGGER.info("Total Tasks " + allTasks + " Training Tasks "
					+ trainingTask + " Test Tasks " + testTask);

			StringBuffer allNouns = new StringBuffer();
			StringBuffer allVerbs = new StringBuffer();
			allNouns.append("");
			allVerbs.append("");
			for (int i = 0; i < trainingTask; i++) {
				Task task = taskList.get(i);
				addDocumentToIndex(task, true);

				allNouns.append(nouns.toString());
				allVerbs.append(verbs.toString());

			}
			if (allNouns.length() > 1)
				allNouns.setLength(allNouns.length() - 1);

			if (allVerbs.length() > 1)
				allVerbs.setLength(allVerbs.length() - 1);

			/*
			 * LOGGER.info("All nouns and Verbs \n" + allNouns.toString() + "\n"
			 * + allVerbs.toString());
			 */
			indexWriter.close();

			int count = 1;

			results = new String[] { "", "", "", "", "", "", "", "", "" };

			// for testing
			for (int i = trainingTask; i < allTasks; i++) {
				String querystr = "";

				Task task = taskList.get(i);
				addDocumentToIndex(task, false);

				if (nouns.length() > 1) {
					nouns.setLength(nouns.length() - 1);
					querystr = "nouns:("
							+ QueryParser.escape(nouns.toString().replaceAll(
									Pattern.quote(":"), "")) + ")";

				}

				if (verbs.length() > 1) {
					if (nouns.length() > 1)
						querystr += " OR ";

					verbs.setLength(verbs.length() - 1);
					querystr += "verbs:("
							+ QueryParser.escape(verbs.toString().replaceAll(
									Pattern.quote(":"), "")) + ")";
				}

				/*
				 * LOGGER.info("\nQuery nouns and Verbs " + nouns.toString() +
				 * "\n" + verbs.toString());
				 */
				// querystr =
				// querystr.replaceAll("\\(|\\)|<|>|\\{|\\}|\\[|\\]|\\\\|\""," ");

				// querystr ="nouns: (" +
				// QueryParser.escape("attach 197314 detail screenshot step bug step thi problem screenshot1 wiki page imag name wiki page screenshot2 imag name imag wiki page destin screenshot3 upload imag local directori screenshot4 pre-ent name text box user guid eclips html file text editor screenshot5 html file wiki page sourc code screenshot6 file browser eclips empti box imag list file imag imag file name differ file browser possibl name imag all lowercas both html file imag file name same problem file name wiki page thi white space thi case dash imag file name all lowercas ani separ both problem link jeff johnston comment same issu *lowercase.png* *lowercase.png*. *src= images/lowercase.png * *lowercase.png* .html http//dev.eclipse.org/mhonarc/lists/linuxtools-dev/msg01009.html")
				// +") OR verbs: (" +
				// QueryParser.escape("creat produc here ar produc have creat put save see request click upload again see even want save again see produc open produc see pars expect open instead see pars so ' n't pick so wa wonder mayb ' modifi file produc retriev also wa face underscor convert underscor mention so mayb manipul solv here also'")
				// + ")";

				// System.out.println(QueryParser.escape(querystr));

				// LOGGER.info("\n Query: " + querystr);

				Query q = new QueryParser(Version.LUCENE_47, descriptionField,
						analyzer).parse(querystr);

				// QueryParser.escape

				// 3. search
				int hitsPerPage = 10;
				IndexReader reader = DirectoryReader.open(index);
				IndexSearcher searcher = new IndexSearcher(reader);
				TopScoreDocCollector collector = TopScoreDocCollector.create(
						hitsPerPage, true);
				searcher.search(q, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;

				// 4. display results
				String taskIds = "";
				for (int j = 0; j < hits.length; ++j) {
					int docId = hits[j].doc;
					Document d = searcher.doc(docId);
					taskIds += "";
					taskIds += d.get("taskid");
					taskIds += ",";
				}

				LOGGER.info("\n Found " + hits.length + " hits: " + taskIds);
				LOGGER.info("Task to be tested " + count + " task id "
						+ task.taskId);
				count++;

				LOGGER.info("\n Result for 1");
				compileResults(taskIds, 1, task, 0);

				LOGGER.info("\n Result for 3");
				compileResults(taskIds, 3, task, 3);

				if (hits.length > 3) {
					LOGGER.info("\n Result for 5");
					compileResults(taskIds, 5, task, 6);
				} else {
					LOGGER.info("\n Skipped result for 5");
					results[6] += task.taskId + "\t5\tU Skip\n";
					results[7] += task.taskId + "\t5\tI Skip\n";
					results[8] += task.taskId + "\t5\tF Skip\n";
				}
			}

			printResults();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initializeforDescription() "
					+ e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initializeforDescription() "
					+ e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initializeforDescription() "
					+ e.getMessage());
		}
	}

	private void compileResults(String taskIds, int maxBound, Task targetTask,
			int resultIndex) {

		String tasks[] = taskIds.split(",");
		Task task = getTask(tasks[0]);

		int max = tasks.length > maxBound ? maxBound : tasks.length;

		List<String> unionFiles = task.getFileNamesList(false);
		List<String> intersectionFiles = task.getFileNamesList(false);

		for (int i = 1; i < max; i++) {
			task = getTask(tasks[i]);
			unionFiles = (ArrayList) Utils.union(unionFiles,
					task.getFileNamesList(false));
			intersectionFiles = (ArrayList) Utils.intersection(
					intersectionFiles, task.getFileNamesList(false));
		}

		LOGGER.info("Union length " + unionFiles.size()
				+ " intersection length " + intersectionFiles.size()
				+ " target task lenght "
				+ targetTask.getFileNamesList(false).size());

		// for union
		int TP = Utils.intersection(unionFiles,
				targetTask.getFileNamesList(false)).size();
		int FP = unionFiles.size() - TP;
		int FN = targetTask.getFileNamesList(false).size() - TP;
		LOGGER.info("For union with max bound " + maxBound);
		LOGGER.info("TP FP FN " + TP + "\t" + FP + "\t" + FN);
		results[resultIndex] += task.taskId + "\t" + maxBound
				+ "\tUnion       \t" + TP + "\t" + FP + "\t" + FN + "\n";

		// for intersection
		TP = Utils.intersection(intersectionFiles,
				targetTask.getFileNamesList(false)).size();
		FP = intersectionFiles.size() - TP;
		FN = targetTask.getFileNamesList(false).size() - TP;
		LOGGER.info("For intersection with max bound " + maxBound);
		LOGGER.info("TP FP FN " + TP + "\t" + FP + "\t" + FN);
		results[resultIndex + 1] += task.taskId + "\t" + maxBound
				+ "\tIntersection\t" + TP + "\t" + FP + "\t" + FN + "\n";

		fileFrequency = getFieFrequencyMap(tasks);

		List keys = new ArrayList(fileFrequency.keySet());
		keys = keys.subList(0, max);

		// for file rankings
		for (Entry<String, Integer> entry : fileFrequency.entrySet()) {
			entry.getKey();
			entry.getValue();
		}

		// for Ranking
		TP = Utils.intersection(keys, targetTask.getFileNamesList(false))
				.size();
		FP = keys.size() - TP;
		FN = targetTask.getFileNamesList(false).size() - TP;
		LOGGER.info("For ranking with max bound " + maxBound);
		LOGGER.info("TP FP FN " + TP + "\t" + FP + "\t" + FN);
		results[resultIndex + 2] += task.taskId + "\t" + maxBound
				+ "\tRanking       \t" + TP + "\t" + FP + "\t" + FN + "\n";

	}

	private Map<String, Integer> getFieFrequencyMap(String[] tasks) {

		if (fileFrequency != null)
			return fileFrequency;

		fileFrequency = new HashMap();
		Task task;

		for (int i = 0; i < tasks.length; i++) {
			task = getTask(tasks[i]);

			for (Object f : task.getFileNamesList(false)) {
				String file = (String) f;
				if (fileFrequency.containsKey(file)) {
					fileFrequency.put(file, fileFrequency.get(file) + 1);
				} else {
					fileFrequency.put(file, 1);
				}
			}
		}

		fileFrequency = Utils.sortByValue(fileFrequency, true);
		return fileFrequency;
	}

	private Task getTask(String string) {

		List<Task> taskList = allTasksList;
		for (Task task : taskList)
			if (task.taskId.equals(string))
				return task;
		return null;
	}

	private void printResults() {

		LOGGER.info("In Priting Results:");

		System.out.println("\nPriting Results:");
		System.out.println("Task\tBound\tType        \tTP\tFP\tFN");
		for (int i = 0; i < results.length; i++)
			System.out.print(results[i]);

	}

	private void addDocumentToIndex(Task task, boolean trainingSet)
			throws IOException {

		Document doc = new Document();
		doc.add(new StringField("taskid", task.taskId, Field.Store.YES));

		String taggedString = tagger.tagString(task.longDescription);
		String words[] = taggedString.split(" ");
		PorterStemmer stem = new PorterStemmer();

		nouns.setLength(0);
		verbs.setLength(0);

		for (String word : words) {
			// nouns
			// CD:Cardinal number, DT: Determiner, JJ: Adjective, JJR:
			// Adjective, comparative, JJS: Adjective, superlative, NN:
			// Noun, singular or mass, NNS: Noun, plural,
			// NNP: Proper noun, singular, NNPS: Proper noun, plural
			if (word.endsWith("/CD") || word.endsWith("/DT")
					|| word.endsWith("/JJ") || word.endsWith("/JJR")
					|| word.endsWith("/JJS") || word.endsWith("/NN")
					|| word.endsWith("/NNS") || word.endsWith("/NNP")
					|| word.endsWith("/NNPS")) {

				stem.setCurrent(word.split("/")[0].toLowerCase());
				stem.stem();
				String term = stem.getCurrent();

				if (StopWords.contains(term))
					continue;

				task.nouns.add(term);
				doc.add(new StringField(descriptionField, term, Field.Store.YES));
				nouns.append(term);
				nouns.append(" ");

			}

			// verbs
			// VB: Verb, base form,
			// VBD: Verb, past tense, VBG: Verb,
			// gerund or present participle, VBN: Verb, past participle,
			// VBP: Verb, non-3rd person singular present, VBZ: Verb,
			// 3rd person singular present, RB: Adverb, RBR: Adverb,
			// comparative, RBS: Adverb, superlative
			else if (word.endsWith("/VB") || word.endsWith("/VBD")
					|| word.endsWith("/VBG") || word.endsWith("/VBN")
					|| word.endsWith("/VBP") || word.endsWith("/VBZ")
					|| word.endsWith("/RB") || word.endsWith("/RBR")
					|| word.endsWith("/RBS")) {
				stem.setCurrent(word.split("/")[0].toLowerCase());
				stem.stem();
				String term = stem.getCurrent();

				if (StopWords.contains(term))
					continue;

				/*
				 * task.verbs.add(term); doc.add(new StringField("verbs", term,
				 * Field.Store.YES)); verbs.append(term); verbs.append(" ");
				 */
				task.nouns.add(term);
				doc.add(new StringField(descriptionField, term, Field.Store.YES));
				nouns.append(term);
				nouns.append(" ");

			}
		}

		// adding teh keywords to nouns.
		for (Object word : task.spKeywords) {
			doc.add(new StringField(descriptionField, word.toString(),
					Field.Store.YES));
			nouns.append(word.toString().toLowerCase());
			nouns.append(" ");
		}

		// add to index only training set tasks.
		if (trainingSet)
			indexWriter.addDocument(doc);
	}

	private void addDocumentToIndexWithDesc(Task task) throws IOException {

		Document doc = new Document();
		doc.add(new StoredField("taskid", task.taskId));
		doc.add(new TextField(descriptionField, task.longDescription + " "
				+ task.comments, Field.Store.NO));
		indexWriter.addDocument(doc);
	}

	private void addDocumentToIndexWithNLP(Task task) throws IOException,
			ParseException {

		Document doc = new Document();
		doc.add(new StoredField("taskid", task.taskId));
		String querystr = task.longDescription + " " + task.comments;
		
		querystr = QueryParser.escape(querystr);
		String taggedString = tagger.tagString(querystr);
		querystr = QueryParser.escape(taggedString);
		BooleanQuery.setMaxClauseCount(80000);
		
		Query q = new QueryParser(Version.LUCENE_47, descriptionField,
				new NLPAnalyzerText()).parse(querystr);

		String qtr = q.toString().replaceAll("nouns:", "")
				.replaceAll("\\s+", "/VB ")
				+ "/VB";

		doc.add(new TextField(descriptionField, qtr, Field.Store.NO));
		
		indexWriter.addDocument(doc);

		// Document doc = new Document();
		// doc.add(new StoredField("taskid", task.taskId));
		// String taggedString = tagger.tagString(task.longDescription + " "
		// + task.comments);
		// doc.add(new TextField(descriptionField, taggedString,
		// Field.Store.NO));
		// indexWriter.addDocument(doc);
	}

	private void getCommitGraphResults(Task firstTask, Task task,
			List<String> interectionFiles) {
		// TODO Auto-generated method stub
		String commitIds = "";
		List<String> files;
		DBConnector db = new DBConnector();
		db.createConnection();

		LOGGER.info("Search Result for Task id " + firstTask.taskId
				+ " found: " + task.taskId);

		for (String file : interectionFiles) {
			commitIds = task.getCommitsForFileName(file).toString()
					.replaceAll("\\[|\\]", "");
			files = db.getCommitGraphForFile(file, commitIds);

			if (files == null || files.size() == 0)
				files = task.getFileNamesList(false);

			// for intersection
			int TP = Utils
					.intersection(files, firstTask.getFileNamesList(true))
					.size();
			int FP = 0, FN = 0;
			if (TP > 0) {
				FP = files.size() - TP;
				FN = firstTask.getFileNamesList(true).size() - TP;
			} else {
				fileFrequency = new HashMap();

				for (Object f : files) {
					String fil = (String) f;
					if (fileFrequency.containsKey(fil)) {
						fileFrequency.put(fil, fileFrequency.get(fil) + 1);
					} else {
						fileFrequency.put(fil, 1);
					}
				}

				fileFrequency = Utils.sortByValue(fileFrequency, true);
				List keys = new ArrayList(fileFrequency.keySet());

				String seedFiles = "";

				files = new ArrayList();

				for (Object key : keys) {
					if (fileFrequency.get(key) >= 2) {
						files.add(key.toString());
					}
				}

				if (files.size() == 0)
					files.addAll(keys);

				TP = Utils
						.intersection(files, firstTask.getFileNamesList(true))
						.size();
				FP = 0;
				FN = 0;
				FP = files.size() - TP;
				FN = firstTask.getFileNamesList(true).size() - TP;
			}

			LOGGER.info("For Commit graphs with max bound");
			LOGGER.info("TP FP FN " + TP + "\t" + FP + "\t" + FN);

			commitResultText += TP + "\t" + FP + "\t" + FN + "\n";
		}
		db.close();

	}

	public static void main(String[] args) {

		JCommanderArgs jct = new JCommanderArgs();
		JCommander jc = new JCommander(jct, args);

		if (args == null || args.length == 0) {
			jc.usage();
			System.exit(0);
		}
		LuceneIndexer lc = new LuceneIndexer();
		lc.seedSize = jct.seedSize;
		lc.nominal = Boolean.parseBoolean(jct.nominal);

		LOGGER.info("Starting for " + Arrays.toString(args));
		if (jct.experimentType.equals("s")) {
			lc.initializeforDescription(jct.taskIds.length() == 0 ? null
					: jct.taskIds);
		} else if (jct.experimentType.equals("n")) {
			lc.initializeforNLP(jct.taskIds, jct.taggerPath);
		}
	}

}
