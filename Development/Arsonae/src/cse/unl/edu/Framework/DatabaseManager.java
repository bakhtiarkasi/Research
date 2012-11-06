package cse.unl.edu.Framework;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

	private static final String dbHost = "localhost";
	private static final String dbName = "arsonae";
	private static final String dbUsername = "root";
	private static final String dbPassword = "arsonae";

	private static Connection conn = null;
	private Statement stmt = null;

	public static void addEvent()
	{
		try
		{
			if(conn == null)
				conn = DatabaseManager.getConnection();
			
			conn.createStatement().executeUpdate("Use " + dbName);
			CallableStatement proc = conn.prepareCall("{ call getAllEvents() }");
			ResultSet rs = proc.executeQuery();
			
		      
		}
		catch(SQLException e){System.out.println(e.getMessage());}
	}
	
	public static Connection getConnection()
	{	
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String sourceURL = "jdbc:mysql://" + DatabaseManager.dbHost;
			return DriverManager.getConnection(sourceURL, DatabaseManager.dbUsername, DatabaseManager.dbPassword);

		}
		catch(IllegalAccessException iae)
		{
		}
		catch(InstantiationException ie)
		{
		}
		catch(ClassNotFoundException cnfe)
		{
			System.out.println(cnfe.getMessage());
		}
		catch(SQLException sqle)
		{
		}
		return null;
	}
	
	
	/*****************************************************SIMULATOR METHODS***************************************************************
	 * 
	 *************************************************************************************************************************************/


	public static Connection getSimulatorDBConnection()
	{	
		Connection conn = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String sourceURL = "jdbc:mysql://localhost:3306/mytask";
			conn =  DriverManager.getConnection(sourceURL, DatabaseManager.dbUsername, DatabaseManager.dbPassword);

		}
		catch(IllegalAccessException iae)
		{
			System.out.println(iae.getMessage());
		}
		catch(InstantiationException ie)
		{
			System.out.println(ie.getMessage());
		}
		catch(ClassNotFoundException cnfe)
		{
			System.out.println(cnfe.getMessage());
		}
		catch(SQLException sqle)
		{
			System.out.println(sqle.getMessage());
		}
		return conn;
	}
	
	public static String[] getUsers()
	{
		String [] users = null;
		try
		{
			if(conn == null)
				conn = DatabaseManager.getSimulatorDBConnection();
			
			conn.createStatement().executeUpdate("Use mytask");
			PreparedStatement proc = conn.prepareStatement("Select distinct author from task");
			ResultSet rs = proc.executeQuery();
			rs.last();
			users = new String[rs.getRow()];
			int index = 0;
			rs.first();
			do
			{
				users[index++] = rs.getString(1);
				rs.next();
			}
			while(!rs.isAfterLast());
		      
		}
		catch(SQLException e){System.out.println(e.getMessage());}
		
		return users;	
	}
	
	public static String[] getFiles(int count)
	{
		String [] files = null;
		try
		{
			if(conn == null)
				conn = DatabaseManager.getSimulatorDBConnection();
			
			conn.createStatement().executeUpdate("Use mytask");
			PreparedStatement proc = conn.prepareStatement("SELECT distinct SUBSTRING_INDEX(SUBSTRING_INDEX(filename, '/', -1), '{', -1) as filenames from event LIMIT "+ count);
			ResultSet rs = proc.executeQuery();
			rs.last();
			
			files = new String[rs.getRow()];
			
			int index = 0;
			rs.first();
			do
			{
				files[index++] = rs.getString(1);
				rs.next();
			}
			while(!rs.isAfterLast());      
		}
		catch(SQLException e){System.out.println(e.getMessage());}
		
		return files;	
	}

}
