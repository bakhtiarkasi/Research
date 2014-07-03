package cse.unl.edu.Lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import cse.unl.edu.Framework.Task;
import cse.unl.edu.Framework.TaskDownloader;
import cse.unl.edu.util.Utils;

import org.tartarus.snowball.ext.PorterStemmer;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class LuceneIndexer {

	/**
	 * @param args
	 */

	ArrayList<String> StopWords;
	MaxentTagger tagger;
	IndexWriter w;
	StringBuffer nouns;
	StringBuffer verbs;
	TaskDownloader tw;
	Map<String, Integer> fileFrequency;

	String results[];

	private final static Logger LOGGER = Logger.getLogger(LuceneIndexer.class
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

	public void initializeforDescription(String idsPath,
			String contextFilePath, String taggerPath, String taskSavedFilePath) {
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

		// 1. create the index
		Directory index = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
				analyzer);

		try {

			w = new IndexWriter(index, config);

			tw = new TaskDownloader(idsPath, contextFilePath, taskSavedFilePath);
			ArrayList<Task> taskList = tw.allTasks;

			int allTasks = taskList.size();
			int testTask = (int) (0.1 * allTasks);
			testTask = testTask < 1 ? 1 : testTask;
			int trainingTask = allTasks - testTask;

			LOGGER.info("Total Tasks " + allTasks + " Training Tasks "
					+ trainingTask + " Test Tasks " + testTask);

			for (int i = 0; i < trainingTask; i++) {
				Task task = taskList.get(i);
				addDocumentToIndexWithDesc(task, true);
			}

			LOGGER.info("All docs have been added \n");
			w.close();

			int count = 1;

			results = new String[] { "", "", "", "", "", "", "", "", "" };

			// for testing
			for (int i = trainingTask; i < allTasks; i++) {
				String querystr = "";

				Task task = taskList.get(i);

				querystr = "nouns:("
						+ QueryParser.escape(task.longDescription.replaceAll(
								Pattern.quote(":"), "")) + ")";

				LOGGER.info("\n Query: " + querystr);

				Query q = new QueryParser(Version.LUCENE_47, "nouns", analyzer)
						.parse(querystr);

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
				String sep = "";

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

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initialize() " + e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initialize() " + e.getMessage());
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

			w = new IndexWriter(index, config);

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

			LOGGER.info("All nouns and Verbs \n" + allNouns.toString() + "\n"
					+ allVerbs.toString());
			w.close();

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

				LOGGER.info("\nQuery nouns and Verbs " + nouns.toString()
						+ "\n" + verbs.toString());

				// querystr =
				// querystr.replaceAll("\\(|\\)|<|>|\\{|\\}|\\[|\\]|\\\\|\""," ");

				// querystr ="nouns: (" +
				// QueryParser.escape("attach 197314 detail screenshot step bug step thi problem screenshot1 wiki page imag name wiki page screenshot2 imag name imag wiki page destin screenshot3 upload imag local directori screenshot4 pre-ent name text box user guid eclips html file text editor screenshot5 html file wiki page sourc code screenshot6 file browser eclips empti box imag list file imag imag file name differ file browser possibl name imag all lowercas both html file imag file name same problem file name wiki page thi white space thi case dash imag file name all lowercas ani separ both problem link jeff johnston comment same issu *lowercase.png* *lowercase.png*. *src= images/lowercase.png * *lowercase.png* .html http//dev.eclipse.org/mhonarc/lists/linuxtools-dev/msg01009.html")
				// +") OR verbs: (" +
				// QueryParser.escape("creat produc here ar produc have creat put save see request click upload again see even want save again see produc open produc see pars expect open instead see pars so ' n't pick so wa wonder mayb ' modifi file produc retriev also wa face underscor convert underscor mention so mayb manipul solv here also'")
				// + ")";

				// System.out.println(QueryParser.escape(querystr));

				LOGGER.info("\n Query: " + querystr);

				Query q = new QueryParser(Version.LUCENE_47, "nouns", analyzer)
						.parse(querystr);

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
				String sep = "";

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
			LOGGER.info("Exception in initializeforDescription() " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initializeforDescription() " + e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initializeforDescription() " + e.getMessage());
		}
	}

	private void compileResults(String taskIds, int maxBound, Task targetTask,
			int resultIndex) {

		String tasks[] = taskIds.split(",");
		Task task = getTask(tasks[0]);

		int max = tasks.length > maxBound ? maxBound : tasks.length;

		ArrayList unionFiles = task.files;
		ArrayList intersectionFiles = task.files;

		for (int i = 1; i < max; i++) {
			task = getTask(tasks[i]);
			unionFiles = (ArrayList) Utils.union(unionFiles, task.files);
			intersectionFiles = (ArrayList) Utils.intersection(
					intersectionFiles, task.files);
		}

		LOGGER.info("Union length " + unionFiles.size()
				+ " intersection length " + intersectionFiles.size()
				+ " target task lenght " + targetTask.files.size());

		// for union
		int TP = Utils.intersection(unionFiles, targetTask.files).size();
		int FP = unionFiles.size() - TP;
		int FN = targetTask.files.size() - TP;
		LOGGER.info("For union with max bound " + maxBound);
		LOGGER.info("TP FP FN " + TP + "\t" + FP + "\t" + FN);
		results[resultIndex] += task.taskId + "\t" + maxBound
				+ "\tUnion       \t" + TP + "\t" + FP + "\t" + FN + "\n";

		// for intersection
		TP = Utils.intersection(intersectionFiles, targetTask.files).size();
		FP = intersectionFiles.size() - TP;
		FN = targetTask.files.size() - TP;
		LOGGER.info("For intersection with max bound " + maxBound);
		LOGGER.info("TP FP FN " + TP + "\t" + FP + "\t" + FN);
		results[resultIndex + 1] += task.taskId + "\t" + maxBound
				+ "\tIntersection\t" + TP + "\t" + FP + "\t" + FN + "\n";

		fileFrequency = getFieFrequencyMap(tasks);

		List keys = new ArrayList(fileFrequency.keySet());
		keys = keys.subList(0, max);

		// for file rankings
		for (Entry<String, Integer> entry : fileFrequency.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
		}

		// for Ranking
		TP = Utils.intersection(keys, targetTask.files).size();
		FP = keys.size() - TP;
		FN = targetTask.files.size() - TP;
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

			for (Object f : task.files) {
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

		ArrayList<Task> taskList = tw.allTasks;
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
				doc.add(new StringField("nouns", term, Field.Store.YES));
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
				doc.add(new StringField("nouns", term, Field.Store.YES));
				nouns.append(term);
				nouns.append(" ");

			}
		}

		// adding teh keywords to nouns.
		for (Object word : task.spKeywords) {
			doc.add(new StringField("nouns", word.toString(), Field.Store.YES));
			nouns.append(word.toString().toLowerCase());
			nouns.append(" ");
		}

		// add to index only training set tasks.
		if (trainingSet)
			w.addDocument(doc);
	}

	private void addDocumentToIndexWithDesc(Task task, boolean trainingSet)
			throws IOException {

		Document doc = new Document();
		doc.add(new StringField("taskid", task.taskId, Field.Store.YES));
		doc.add(new StringField("nouns", task.longDescription, Field.Store.YES));

		if (trainingSet)
			w.addDocument(doc);
	}

	public static void main(String[] args) {

		LOGGER.info("Starting for taskIds " + args[0] + " Context saved at  "
				+ args[1] + " Tagger " + args[2] + " Task saved at "
				+ (args.length == 3 ? "NULL" : args[3]));

		LuceneIndexer lc = new LuceneIndexer();
//		lc.initialize(args[0], args[1], args[2], (args.length == 3 ? null
	//			: args[3]));
		
		lc.initializeforDescription(args[0], args[1], args[2], (args.length == 3 ? null
				: args[3]));

	}

}
