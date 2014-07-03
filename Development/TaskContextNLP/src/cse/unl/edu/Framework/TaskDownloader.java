package cse.unl.edu.Framework;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import cse.unl.edu.util.Utils;

public class TaskDownloader {

	/**
	 * @param args
	 */

	Document doc, subDoc;
	String baseURL = "https://bugs.eclipse.org/bugs/";
	public ArrayList<Task> allTasks;
	private String contextFilesPath;
	private final static Logger LOGGER = Logger.getLogger(TaskDownloader.class
			.getName());

	private int noEdits = 0;

	public TaskDownloader(String idsFile, String mylynContextPath,
			String taskSavedFilePath) {
		// read csv file and get task one by one
		try {
			LOGGER.setLevel(Level.INFO);
			FileHandler fileHandler = null;
			fileHandler = new FileHandler("myLogFile");
			LOGGER.addHandler(fileHandler);

			allTasks = new ArrayList();
			String mylynTasksIds;

			if (taskSavedFilePath != null && taskSavedFilePath.length() > 1) {
				this.loadTaskFromFile(taskSavedFilePath);
				return;
			}

			LOGGER.info("Downloading Task Contexts");

			contextFilesPath = mylynContextPath;

			mylynTasksIds = Utils.readFile(idsFile);

			String[] allTasksIDS = mylynTasksIds.split("\n");
			LOGGER.info("All tasks length " + allTasksIDS.length);

			for (int i = 0; i < allTasksIDS.length; i++) {
				Task task = new Task();
				if (initializeWithContext(allTasksIDS[i], task)) {
					allTasks.add(task);
				}
			}

			saveTasksToFile(mylynContextPath);

			LOGGER.info("Tasks with Contexts and Edit events "
					+ allTasks.size());
			LOGGER.info("Tasks with Contexts and no Edit events " + noEdits);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in TaskDownloader() " + e.getMessage());
		}

	}

	private void saveTasksToFile(String mylynContextPath) {
		// TODO Auto-generated method stub

		try {

			String filePath = mylynContextPath + "AllTaskContexts.txt";
			LOGGER.info("Saving task Contexts to file " + filePath);

			FileWriter fw = new FileWriter(new File(filePath));
			String seperator = "";
			for (int i = 0; i < allTasks.size(); i++) {
				Task task = allTasks.get(i);
				fw.write(seperator);
				seperator = "-----------------------------------------------------------\n";
				fw.write(task.taskId + "\n");
				fw.write(task.longDescription + "\n");
				// fw.write("Noun:"+task.nouns.toString() + "\n");
				// fw.write("Verbs:"+task.verbs.toString() + "\n");
				// fw.write("Keywords:"+task.spKeywords.toString() + "\n");
				fw.write(task.files.toString().substring(1,
						task.files.toString().length() - 1)
						+ "\n");
				// LOGGER.info("Task " + task.taskId + " files " +
				// task.files.size());
			}
			fw.flush();
			fw.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in saveTasksToFile() " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in saveTasksToFile() " + e.getMessage());
		}
	}

	private void loadTaskFromFile(String taskSavedFilePath) {
		LOGGER.info("Loading Task Contexts from file " + taskSavedFilePath);
		try {

			Task task;
			String fileCont = Utils.readFile(taskSavedFilePath);
			String blocks[] = fileCont
					.split("-----------------------------------------------------------\n");
			for (int i = 0; i < blocks.length; i++) {
				String lines[] = blocks[i].split("\n");
				task = new Task();
				task.taskId = lines[0];
				task.longDescription = lines[1];
				task.filteredDescription = getSpecialKeywords(task,
						task.longDescription);
				task.files = new ArrayList(Arrays.asList(lines[2].split(",")));
				allTasks.add(task);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in loadTaskFromFile() " + e.getMessage());
		}

	}

	private boolean initializeWithContext(String taskId, Task task) {

		boolean hasContext = false;
		try {

			String urlId = baseURL + "show_bug.cgi?id=" + taskId;
			String longDescription = "";

			int time = 100 * 1000;

			doc = Jsoup.connect(urlId).timeout(time).get();

			Element table = doc.getElementById("attachment_table");
			Elements rows = table.getElementsByTag("tr");
			if (rows.size() >= 3) {
				for (int i = 1; i < rows.size() - 1; i++) {
					Elements links = rows.get(i).getElementsByTag("a");
					for (Element link : links) {
						if (link.text().equals("mylyn/context/zip")) {

							String attachURL = baseURL + link.attr("href");

							URL url = new URL(attachURL);
							InputStream in = url.openStream();
							File file = new File(contextFilesPath + taskId
									+ "/" + link.attr("href").split("id=")[1]
									+ ".xml.rar");
							if (file.exists())
								file.delete();
							file.getParentFile().mkdir();

							OutputStream out = new BufferedOutputStream(
									new FileOutputStream(file));
							for (int b; (b = in.read()) != -1;) {
								out.write(b);
							}
							out.flush();
							out.close();
							in.close();

							ZipFile zip = new ZipFile(file);
							File xmlfile = new File(contextFilesPath + taskId
									+ "/" + link.attr("href").split("id=")[1]
									+ ".xml");

							Utils.unzip(zip, xmlfile);
							task.taskId = taskId;
							// read xml to load context here
							readTaskContext(xmlfile, task);
							hasContext = true;

						}
					}
				}
				if (hasContext) {

					if (task.files.size() == 0) {
						noEdits++;
						hasContext = false;
						return hasContext;
					}

					// read task description here
					table = doc.getElementsByClass("bz_comment_table").get(0);
					Element text = table.getElementsByClass("bz_comment_text")
							.get(0);

					longDescription = getFilteredDescription(text.text());
					task.longDescription = longDescription;

					task.filteredDescription = getSpecialKeywords(task,
							longDescription);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.info("Exception in initializeWithContext() "
					+ e.getMessage());
		}

		return hasContext;
	}

	private String getSpecialKeywords(Task task, String longDescription) {

		String messages[] = longDescription.split("\\s+");
		StringBuffer filtered = new StringBuffer();
		filtered.append("");
		String line = "";

		for (int i = 0; i < messages.length; i++) {
			line = messages[i];
			if (line.contains(".") && line.indexOf('.') < line.length() - 1) {
				if (!task.spKeywords.contains(line))
					task.spKeywords.add(line);
				// ignore System.out.println("\n\n"+line+ "\n\n");
			} else if (line.matches("^\\W")) {
				// ignore System.out.println("\n\n non word::"+line+ "\n\n");
			} else {
				filtered.append(line);
				filtered.append(" ");
			}
		}

		return filtered.toString();
	}

	private String getFilteredDescription(String text) {

		String messages[] = text.split("\n");
		StringBuffer filtered = new StringBuffer();
		filtered.append("");
		String line = "";

		for (int i = 0; i < messages.length; i++) {
			line = messages[i];
			if (line.length() > 0) {
				if (line.matches("!?(ENTRY|MESSAGE|STACK).*|\\s*at.*\\.java\\:.*|\\s*at.*Native Method.*")) {
					// ignore System.out.println("true2 " + line);
					if (line.matches("\\s*at.*\\.java\\:.*")) {
					//	System.out.println("true2 " + line);
						filtered.append(line);
						filtered.append("\n");
					}
				} else {
					filtered.append(line);
					filtered.append("\n");
				}
			} else {
				filtered.append("\n");
			}
		}

		return filtered.toString();
	}

	private void readTaskContext(File xmlfile, Task task) {

		org.w3c.dom.Document doc = this.getXMLDocument(xmlfile);
		org.w3c.dom.Element root = doc.getDocumentElement();
		String fileName = "";
		NodeList allEvents = root.getElementsByTagName("InteractionEvent");

		for (int i = 0; i < allEvents.getLength(); i++) {
			org.w3c.dom.Node event = allEvents.item(i);
			org.w3c.dom.Element element = (org.w3c.dom.Element) event;

			if (element.getAttribute("Kind").equalsIgnoreCase("manipulation")) {
				fileName = element.getAttribute("StructureHandle");

				if (fileName.trim().length() > 1) {
					if (!task.files.contains(fileName))
						task.files.add(fileName);
				}
			}

		}

	}

	private org.w3c.dom.Document getXMLDocument(File xmlfile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			org.w3c.dom.Document doc = builder.parse(new FileInputStream(
					xmlfile));
			doc.getDocumentElement().normalize();

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			String output = writer.getBuffer().toString()
					.replaceAll("\n|\r|\t", "");

			return builder.parse(new InputSource(new StringReader(output)));

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("Exception in getXMLDocument() " + e.getMessage());
		}
		return null;
	}
}
