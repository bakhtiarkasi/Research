package cse.unl.edu.Lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
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

	public final String descriptionField = "nouns";

	String results[];
	private List<Task> allTasksList;
	String resultText = "";
	String commitResultText = "";

	int res[];

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
			fileHandler = new FileHandler("myLogFile");
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
				BooleanQuery.setMaxClauseCount(20000);

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
							firstTask.getFileNamesList(),
							task.getFileNamesList());

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

			// task that is being searched for
			Task firstTask = getTask(taskId);
			String subTaskid;

			String serachRes = taskId + "-> ";
			LOGGER.info("\n");
			LOGGER.info("Running task: " + taskId);

			// copy all doc scores into a new array excluding score for the
			// searched doc, which is usually the first in search results
			float[] scores = new float[hits.length - 1];
			int l = 0;
			for (int j = 0; j < hits.length; j++) {

				serachRes += searcher.doc(hits[j].doc).get("taskid") + ",";
				
				//here
			//	System.out.println(serachRes);
				//System.out.println(taskId + " : " + searcher.doc(hits[j].doc).get("taskid"));

				if (!searcher.doc(hits[j].doc).get("taskid").equals(taskId))
				{
					//here
					//System.out.println("Not Same");
					
					if(l == scores.length)
						break;
					
					scores[l++] = hits[j].score;
				}
			}
			LOGGER.info(serachRes);


			// get median value for search results
			float median = getMedianValueIndex(scores);

			// from search results select only docs that have a score of median
			// and above
			List<String> tasksList = new ArrayList();
			for (int j = 0; j < hits.length; j++) {
				if (!searcher.doc(hits[j].doc).get("taskid").equals(taskId)) {
					if (hits[j].score >= median)
						tasksList.add(searcher.doc(hits[j].doc).get("taskid"));
					else
						break;
				}
			}
			String tasks[] = new String[tasksList.size()];
			for (int i = 0; i < tasksList.size(); i++) {
				tasks[i] = tasksList.get(i);
			}

			// select 3 or more file as the seed set for the commit graph, any
			// file that appears more than twice is a candidiate
			fileFrequency = null;
			fileFrequency = this.getFieFrequencyMap(tasks);
			List keys = new ArrayList(fileFrequency.keySet());

			int count = 0;
			for (int i = 0; i < keys.size(); i++) {
				if (fileFrequency.get(keys.get(i)) > 1)
					count++;
			}
			if (count <= 3)
				count = keys.size() > 3 ? 3 : keys.size();

			
			// reduce key size to the number of files in seed set
			keys = keys.subList(0, count);

			String seedFiles = "";
			// for file rankings
			for (Object key : keys) {
				if (seedFiles != "")
					seedFiles += ",";
				seedFiles += "'" + key.toString() + "'";
			}

			DBConnector db = new DBConnector();
			db.createConnection();

			List<String> intersectionFiles = null;
			// Map results = new HashMap<String, List<String>>();

			// get adjacent files within the last 6 month of commits for the
			// seed set files.
			Map results = db.getCommonAdjacentFiles(seedFiles, taskId);
			db.close();

			// Step 0 is to check for common files in adjacent files of seed set
			// files.
			String step = "Step0";
			if (results != null) {
				for (int i = 0; i < keys.size(); i++) {
					if (results.get(keys.get(i)) != null) {
						if (intersectionFiles == null)
							intersectionFiles = (List<String>) results.get(keys
									.get(i));
						else {
							intersectionFiles = Utils.intersection(
									intersectionFiles,
									(List<String>) results.get(keys.get(i)));
							if (intersectionFiles == null || intersectionFiles.size() == 0) 
							{
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
					for (int i = 0; i < keys.size(); i++) {
						uniqFiles = (List<String>) results.get(keys.get(i));
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
			
			if(intersectionFiles == null)
				intersectionFiles = new ArrayList();
				
			for (Object key : keys) {
				String fileName = key.toString();
				if (!intersectionFiles.contains(fileName))
					intersectionFiles.add(fileName);
			}

			int TP = Utils.intersection(intersectionFiles,
					firstTask.getFileNamesList()).size();
			int FP = 0;
			int FN = 0;
			FP = intersectionFiles.size() - TP;
			FN = firstTask.getFileNamesList().size() - TP;

			LOGGER.info("final target task size " + intersectionFiles.size());
			resultText += taskId + "\t" + step + "\t" + count + "\t" + TP
					+ "\t" + FP + "\t" + FN + "\n";
			LOGGER.info("TP FP FN " + taskId + "\t" + step + "\t" + count
					+ "\t" + TP + "\t" + FP + "\t" + FN);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(resultText);
		}

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
			List<Task> taskList = db.getIssueDescription(idsForAnalysis);

			this.allTasksList = taskList;

			for (int i = 0; i < allTasksList.size(); i++) {
				Task task = taskList.get(i);
				addDocumentToIndexWithNLP(task);
			}

			LOGGER.info("All docs have been added \n");
			indexWriter.close();

			IndexReader reader = DirectoryReader.open(index);

			/*
			 * Fields fields = MultiFields.getFields(reader); Terms terms =
			 * fields.terms(descriptionField); TermsEnum te =
			 * terms.iterator(null); while (te.next() != null) {
			 * te.term().utf8ToString(); }
			 */

			res = new int[5]; // 1,3,5,7,10

			resultText = "";
			
			//here
			for (int i = 0; i < allTasksList.size(); i++) {

				Task task = taskList.get(i);
				String querystr = task.longDescription + " " + task.comments;
				querystr = QueryParser.escape(querystr);
				querystr = tagger.tagString(querystr);
				querystr = QueryParser.escape(querystr);
				BooleanQuery.setMaxClauseCount(20000);

				Query q = new QueryParser(Version.LUCENE_47, descriptionField,
						analyzer).parse(querystr);

				// LOGGER.info("\n Query: " + q);

				// 3. search
				int hitsPerPage = 10;

				IndexSearcher searcher = new IndexSearcher(reader);
				TopScoreDocCollector collector = TopScoreDocCollector.create(
						hitsPerPage, true);
				searcher.search(q, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;

				compileResultsLatest(hits, searcher, task.taskId);

				/*
				 * for (int j = 0; j < hits.length; ++j) { int docId =
				 * hits[j].doc; searcher.doc(docId); }
				 */
			}

			/*
			 * float allTasksLen = allTasksList.size() * 1.0f;
			 * System.out.printf(
			 * "Results:  1\t3\t5\t7\t10\n       %s\t%s\t%s\t%s\t%s", (res[0] *
			 * 1.0f) / allTasksLen, (res[1] * 1.0f) / allTasksLen, (res[2] *
			 * 1.0f) / allTasksLen, (res[3] * 1.0f) / allTasksLen, (res[4] *
			 * 1.0f) / allTasksLen);
			 */

			System.out.println(resultText);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initialize() " + e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
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

		List<String> unionFiles = task.getFileNamesList();
		List<String> intersectionFiles = task.getFileNamesList();

		for (int i = 1; i < max; i++) {
			task = getTask(tasks[i]);
			unionFiles = (ArrayList) Utils.union(unionFiles,
					task.getFileNamesList());
			intersectionFiles = (ArrayList) Utils.intersection(
					intersectionFiles, task.getFileNamesList());
		}

		LOGGER.info("Union length " + unionFiles.size()
				+ " intersection length " + intersectionFiles.size()
				+ " target task lenght " + targetTask.getFileNamesList().size());

		// for union
		int TP = Utils.intersection(unionFiles, targetTask.getFileNamesList())
				.size();
		int FP = unionFiles.size() - TP;
		int FN = targetTask.getFileNamesList().size() - TP;
		LOGGER.info("For union with max bound " + maxBound);
		LOGGER.info("TP FP FN " + TP + "\t" + FP + "\t" + FN);
		results[resultIndex] += task.taskId + "\t" + maxBound
				+ "\tUnion       \t" + TP + "\t" + FP + "\t" + FN + "\n";

		// for intersection
		TP = Utils.intersection(intersectionFiles,
				targetTask.getFileNamesList()).size();
		FP = intersectionFiles.size() - TP;
		FN = targetTask.getFileNamesList().size() - TP;
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
		TP = Utils.intersection(keys, targetTask.getFileNamesList()).size();
		FP = keys.size() - TP;
		FN = targetTask.getFileNamesList().size() - TP;
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

			for (Object f : task.getFileNamesList()) {
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

	private void addDocumentToIndexWithNLP(Task task) throws IOException {

		Document doc = new Document();
		doc.add(new StoredField("taskid", task.taskId));
		String taggedString = tagger.tagString(task.longDescription + " "
				+ task.comments);
		doc.add(new TextField(descriptionField, taggedString, Field.Store.NO));
		indexWriter.addDocument(doc);
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
				files = task.getFileNamesList();

			// for intersection
			int TP = Utils.intersection(files, firstTask.getFileNamesList())
					.size();
			int FP = 0, FN = 0;
			if (TP > 0) {
				FP = files.size() - TP;
				FN = firstTask.getFileNamesList().size() - TP;
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

				TP = Utils.intersection(files, firstTask.getFileNamesList())
						.size();
				FP = 0;
				FN = 0;
				FP = files.size() - TP;
				FN = firstTask.getFileNamesList().size() - TP;
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

		LOGGER.info("Starting for " + Arrays.toString(args));
		if (jct.experimentType.equals("s")) {
			lc.initializeforDescription(jct.taskIds.length() == 0 ? null
					: jct.taskIds);
		} else if (jct.experimentType.equals("n")) {
			lc.initializeforNLP(jct.taskIds, jct.taggerPath);
		}
	}

}
