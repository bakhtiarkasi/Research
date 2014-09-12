package cse.unl.edu.Framework;

import java.util.ArrayList;
import java.util.List;

public class DependecyMatrix {
	
	
	public List<String> allFiles;
	
	public DependecyMatrix()
	{
		allFiles = new ArrayList();
	}

	public void loadFileNames()
	{
		DBConnector db = new DBConnector();
		db.createConnection();
		
		allFiles = db.getSortedAllFilesList();
		
		db.close();
	}
	
	public void makeDependecyMatrix()
	{
		DBConnector db = new DBConnector();
		db.createConnection();
		
		String [][] array;
		array = db.getAdjancyData();
		int [][] counts = new int [allFiles.size()][allFiles.size()];
		
		int first, second;
		for(int i=0; i<array.length; i++)
		{
			first = allFiles.indexOf(array[i][0]);
			second = allFiles.indexOf(array[i][1]);
			
			if(first == -1 || second == -1)
			{
				System.out.println("Error in finding file " + array[i][0] + " OR " + array[i][1]);
				System.exit(1);
			}
			
			//if(first<second)
				counts[first][second]++;
			//else
				//counts[second][first]++;
		}
		
		
		//printing results
		for(int i=0; i<counts.length; i++)
		{
			for(int j=0; j<counts[i].length; j++)
			{
				if(counts[i][j] > 0)
					System.out.println(allFiles.get(i) + " : " + allFiles.get(j) + " : " + counts[i][j]);	
			}
		}
		
		db.close();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		DependecyMatrix dm = new DependecyMatrix();
		dm.loadFileNames();
		dm.makeDependecyMatrix();

	}

}
