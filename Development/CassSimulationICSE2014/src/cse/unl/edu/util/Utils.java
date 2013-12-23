package cse.unl.edu.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class Utils {

	protected static final int BUFFER_SIZE = 1024;

	public static String taskxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tasks>";

	private static void unzip(ZipFile zipFile, File dstFile) throws IOException {

		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		try {
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				String entryName = entry.getName();

				InputStream src = null;
				OutputStream dst = null;
				try {
					src = zipFile.getInputStream(entry);
					dst = new FileOutputStream(dstFile);
					transferData(src, dst);
				} finally {
					if (dst != null) {
						try {
							dst.close();
						} catch (IOException e) {
							// don't need to catch this
						}
					}
					if (src != null) {
						try {
							src.close();
						} catch (IOException e) {
							// don't need to catch this
						}
					}
				}
			}
		} finally {
			try {
				zipFile.close();
			} catch (IOException e) {
				// don't need to catch this
			}
		}
	}

	private static void transferData(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}

	//return an inclusive value from start to end
	public static int getRandomNumber(int start, int end, List<Integer> exclude, boolean enforced) {
		int pickedNumber = -1;
		//System.out.println("in random");
		
		try {

			Random rand = new Random();

			do {
				pickedNumber = rand.nextInt(end - start + 1) + start;

				if (exclude != null) {
					if (enforced && exclude.size() == (end - start + 1))
						throw new Exception("Exclude List already full" + start + ", " + end + ", " + exclude);

					else if (exclude.contains(pickedNumber))
						pickedNumber = -1;

				}
				
				//System.out.println("in random loop" + start + " " + end + " " + exclude.toString() + " " +enforced );

			} while (pickedNumber == -1);

		} catch (Exception ex) {
			ex.printStackTrace();

		}
		return pickedNumber;

	}

	public static int getNextGaussian(int stdDev, int mean,
			List<Integer> exclude) {

		Random rand = new Random();
		double pickedNumber = -1;
		do {
			pickedNumber = Math.abs(rand.nextGaussian()) * stdDev + mean;

			if (exclude != null && exclude.contains(pickedNumber))
				pickedNumber = -1;

		} while (pickedNumber <= -1);

		return (int) pickedNumber;
	}

	public static String writePyhtonFile(String fileName, String script,
			String filePath) throws IOException {

		BufferedReader reader = null;
		BufferedWriter writer = null;

		String templateFile = filePath + "cassTemplate.py";
		String ouputFile = filePath + "Z3Input/outputFile" + fileName + ".py";
		boolean sectionStart = false;

		try {
			reader = new BufferedReader(new FileReader(templateFile));
			writer = new BufferedWriter(new FileWriter(ouputFile));
			String tmp;

			while ((tmp = reader.readLine()) != null) {

				if (tmp.equals("#Start java insert"))
					sectionStart = true;
				else if (tmp.equals("#end java insert")) {
					writer.write(script);
					writer.newLine();
					sectionStart = false;
					continue;
				}

				if (!sectionStart) {
					writer.write(tmp);
					writer.newLine();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			reader.close();
			writer.close();
			return ouputFile;
		}
	}

	public static String readFile(String fileName) throws IOException {

		BufferedReader reader = null;
		String fileContent = "";

		try {
			reader = new BufferedReader(new FileReader(fileName));
			String tmp;

			while ((tmp = reader.readLine()) != null) {
				fileContent += tmp + "\n";
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			reader.close();
			return fileContent;
		}
	}

	private static Document createDocument() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			return db.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document openTaskList(String inputFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			//Document doc = builder.parse(new InputSource(new StringReader(inputFile)));
			
			Document doc = builder.parse(new FileInputStream(inputFile));

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
		} 
		return null;
	}
	
	public static double standardDeviation (List<Integer> a){
        int sum = 0;
        double mean = mean(a);
 
        for (Integer i : a)
            sum += Math.pow((i - mean), 2);
        return Math.sqrt( sum / ( a.size() - 1 ) ); // sample
    }

	private static double mean(List<Integer> a) {
		int sum = sum(a);
        double mean = 0;
        mean = sum / (a.size() * 1.0);
        return mean;
	}
	
	public static int sum (List<Integer> a){
        if (a.size() > 0) {
            int sum = 0;
 
            for (Integer i : a) {
                sum += i;
            }
            return sum;
        }
        return 0;
    }
	
	public static Integer[] pickKRandomArray(int n, int k) {

		List<Integer> list = new ArrayList<Integer>(n);
	    
		for(int i =0; i< n; i++)
			 list.add(i);
		
	    Collections.shuffle(list);

	    Integer[] answer = new Integer[k];
	    
	    for (int i = 0; i < k; i++)
	        answer[i] = list.get(i);
	    Arrays.sort(answer);

	    return answer;

	}
	
	public static int choose(int n, int k) {
	  /*  if (k == 0 || n == k) {
	        return 1;
	    }
	    else {
	        return choose(n-1, k-1) + choose(n-1, k);
	    }*/
		
		if (n < 2) {
	        return 1;
	    }
	    else {
	        int bin[][] = new int[n+1][n+1];        // CONSTRUCT A TABLE TO STORE

	        for (int r = 0; r <= n; r++) {          // COMPUTED VALUES
	            for (int c = 0; c <= r && c <= k; c++) {
	                if (c == 0 || c == r) {
	                    bin[r][c] = 1;              // ENTER 1 IF BASE CASE
	                }
	                else {                          // OTHERWISE, USE FORMULA
	                    bin[r][c] = bin[r-1][c-1] + bin[r-1][c];
	                }
	            }
	        }
	        return bin[n][k];                       // ANSWER IS AT bin[n][k]
	    }
	}
}