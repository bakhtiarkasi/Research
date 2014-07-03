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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {

	protected static final int BUFFER_SIZE = 1024;

	public static String taskxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tasks>";

	public static void unzip(ZipFile zipFile, File dstFile) throws IOException {

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

	public static int getRandomNumber(int start, int end, List<Integer> exclude) {
		int pickedNumber = -1;
		try {

			Random rand = new Random();

			do {
				pickedNumber = rand.nextInt(end - start + 1) + start;

				if (exclude != null) {
					if (exclude.size() == (end - start + 1))
						throw new Exception("Exclude List already full");

					else if (exclude.contains(pickedNumber))
						pickedNumber = -1;

				}

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

	public static void writePyhtonFile(String fileName, String script,
			String filePath) throws IOException {

		BufferedReader reader = null;
		BufferedWriter writer = null;

		String templateFile = filePath + "cassTemplate.py";
		String ouputFile = filePath + "/Z3Input/outputFile" + fileName + ".py";
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

	public static <T> List<T> union(List<T> list1, List<T> list2) {
		Set<T> set = new HashSet<T>();

		set.addAll(list1);
		set.addAll(list2);

		return new ArrayList<T>(set);
	}

	public static <T> List<T> intersection(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<T>();

		for (T t : list1) {
			if (list2.contains(t)) {
				list.add(t);
			}
		}

		return list;
	}

	public static Map<String, Integer> sortByValue(Map<String, Integer> map,
			final boolean descending) {
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(
				map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

			public int compare(Map.Entry<String, Integer> m1,
					Map.Entry<String, Integer> m2) {
				if (descending)
					return (m2.getValue()).compareTo(m1.getValue());
				else
					return (m1.getValue()).compareTo(m2.getValue());
			}
		});

		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}