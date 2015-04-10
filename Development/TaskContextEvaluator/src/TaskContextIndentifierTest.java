import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemovePercentage;
import mulan.classifier.lazy.BRkNN;
import mulan.classifier.lazy.MLkNN;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.BinaryRelevance;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.MultiLabelInstances;
import mulan.data.Statistics;
import mulan.evaluation.Evaluation;
import mulan.evaluation.Evaluator;
import mulan.evaluation.measure.Measure;

public class TaskContextIndentifierTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String arffFile = null;
		String xmlFile = null;
		try {

			
			double percentage = 90;
			
			
			arffFile = Utils.getOption('a', args);
			xmlFile = Utils.getOption('x', args);
			String unlabeledFilename = Utils.getOption("u", args);
			unlabeledFilename = arffFile;

			String modelToRun = Utils.getOption("m", args);

			MultiLabelInstances data = new MultiLabelInstances(arffFile,
					xmlFile);

			Instances dataSet = data.getDataSet();

			RemovePercentage rmvp = new RemovePercentage();
			rmvp.setInvertSelection(true);
			rmvp.setPercentage(percentage);
			rmvp.setInputFormat(dataSet);
			Instances trainDataSet = Filter.useFilter(dataSet, rmvp);

			rmvp = new RemovePercentage();
			rmvp.setPercentage(percentage);
			rmvp.setInputFormat(dataSet);
			Instances testDataSet = Filter.useFilter(dataSet, rmvp);

			MultiLabelInstances train = new MultiLabelInstances(trainDataSet,
					xmlFile);
			MultiLabelInstances test = new MultiLabelInstances(testDataSet,
					xmlFile);
			
			//test = train;

			Evaluator eval = new Evaluator();
			
			Evaluation results;
			
			if(false)
			{
			System.out.println(dataSet.instance(0));
			System.out.println(dataSet.instance(dataSet.size()-1));
			
			System.out.println(trainDataSet.instance(0));
			System.out.println(trainDataSet.instance(trainDataSet.size()-1));
			
			System.out.println(testDataSet.instance(0));
			System.out.println(testDataSet.instance(testDataSet.size()-1));
			}
			
			System.out.println("Train \n\n" + train.getDataSet());
			System.out.println("\n\nTest \n\n" + test.getDataSet());
			
			
			System.out.println(dataSet.size() + " Train: " + trainDataSet.size() + " Test: " + testDataSet.size() + "\n");

			int trainSize =0;
			int testSize =0;
			int bothSet =0;
			
			int minStartIndex = -1;
			
			
			for(int i=0; i<data.getDataSet().size(); i++)
			{
				Instance dataitem = data.getDataSet().get(i);
				
				//System.out.println(train.getDataSet().contains(dataitem));
							
				
				if(isExists(train.getDataSet(),dataitem.toString()) && isExists(test.getDataSet(),dataitem.toString()))
				{
					bothSet++;
					System.out.println(dataitem.toString());
				}
				if(isExists(train.getDataSet(),dataitem.toString()))
					trainSize++;
				if(isExists(test.getDataSet(),dataitem.toString()))
				{
					if(minStartIndex == -1)
						minStartIndex = i;
					testSize++;
				}
			}
			
			System.out.println(" Train: " + trainSize + " Test: " + testSize + " Both: " + bothSet + " index " + minStartIndex);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static boolean isExists(Instances dataSet, String string) {
		for(Instance dataItem: dataSet)
		{
			if(dataItem.toString().equals(string))
				return true;
		}
		return false;
	}

}
