package helper.web;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class WebHelper {
	
	public static void downloadURL(String url, String filename) {
		try {
		URL spec = new URL(url);
		File file = new File(filename);
		
		FileUtils.copyURLToFile(spec, file);
		
		} catch (Exception e) {
		}
	}
	
	public static void main(String[] args) {
		
		//Hbase pre 
		//string : String pre = "https://issues.apache.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=project+%3D+HBASE+AND+created+%3E%3D+";
		
		//Derby pre string
		String pre = "https://issues.apache.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=project+%3D+DERBY+AND+created+%3E%3D+";
		String mid = "+AND+created+%3C+";
		String pos = "&tempMax=100";
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		//HBase Start Date: 
		//DateTime start = new DateTime(2012,6,1,0,0);
		
		//yyyy,m,d
		DateTime start = new DateTime(2004,9,24,0,0); //Derby start date 2004-09-24
		DateTime end = start.plus(Period.days(8));
		DateTime last = new DateTime(2014,9,9,0,0);
		

		
		while (end.isAfter(last) == false) {
			String date1 = fmt.print(start);
			System.out.println("Current: " + date1);
			
			String url = pre + date1 + mid + fmt.print(end) + pos;
			
			//habse helper location
			//WebHelper.downloadURL(url, "files/issues/hbase_" + date1 + ".xml");
			
			//Derby helper location
			WebHelper.downloadURL(url, "files/issues/derby_" + date1 + ".xml");
			
			// next day
			start = end;
			end = start.plus(Period.days(8));
		}
	}
	
}
