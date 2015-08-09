import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.instance.RemovePercentage;
import mulan.classifier.MultiLabelOutput;
import mulan.classifier.lazy.BRkNN;
import mulan.classifier.lazy.MLkNN;
import mulan.classifier.lazy.BRkNN.ExtensionType;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.BinaryRelevance;
import mulan.classifier.transformation.CalibratedLabelRanking;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.MultiLabelInstances;
import mulan.data.Statistics;
import mulan.evaluation.Evaluation;
import mulan.evaluation.Evaluator;
import mulan.evaluation.measure.ExampleBasedAccuracy;
import mulan.evaluation.measure.ExampleBasedFMeasure;
import mulan.evaluation.measure.HammingLoss;
import mulan.evaluation.measure.MacroPrecision;
import mulan.evaluation.measure.Measure;
import mulan.evaluation.measure.MicroPrecision;

public class TaskContextIndentifierPerc {

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

			Evaluator eval = new Evaluator();

			Evaluation results;

			if (false) {
				System.out.println(dataSet.instance(0));
				System.out.println(dataSet.instance(dataSet.size() - 1));

				System.out.println(trainDataSet.instance(0));
				System.out
						.println(trainDataSet.instance(trainDataSet.size() - 1));

				System.out.println(testDataSet.instance(0));
				System.out
						.println(testDataSet.instance(testDataSet.size() - 1));
			}

			System.out.println(dataSet.size() + " Train: "
					+ trainDataSet.size() + " Test: " + testDataSet.size());

			if (modelToRun.equals("stat")) {
				Statistics st = new Statistics();
				st.calculateStats(data);
				System.out.println("Printing Statistics: \n" + st);
				System.out.println("*********************************\n\n");
			}

			if (modelToRun.equals("nb") || modelToRun.equals("all")) {
				Classifier brClassifier = new NaiveBayes();
				CalibratedLabelRanking br = new CalibratedLabelRanking(brClassifier);
				br.setDebug(true);
				br.build(train);

				int numFolds = 10;
				// results = eval.crossValidate(br, data, numFolds);
				results = eval.evaluate(br, test, train);
				System.out.println("Printing Naive Bayes: \n");
				System.out.println(results.getMeasures().toString()
						.replaceAll(", ", "\n"));
				System.out.println("*********************************\n\n");
			}

			if (modelToRun.equals("svm") || modelToRun.equals("all")) {

				CalibratedLabelRanking SVM = new CalibratedLabelRanking(
						new weka.classifiers.functions.SMO());
				SVM.setDebug(true);
				SVM.build(train);
		
				results = eval.evaluate(SVM, test, train);
				System.out.println("Printing SVM Relevance: \n");
				System.out.println(results.getMeasures().toString()
						.replaceAll(", ", "\n"));
				System.out.println("*********************************\n\n");
			}

			if (modelToRun.equals("rakel") || modelToRun.equals("all")) {

				RAkEL rakel = new RAkEL(new LabelPowerset(new J48()));
				rakel.setDebug(true);
				rakel.build(train);

				results = eval.evaluate(rakel, test, train);
				System.out.println("Printing Rakel Relevance: \n");
				System.out.println(results.getMeasures().toString()
						.replaceAll(", ", "\n"));
				System.out.println("*********************************\n\n");
			}

			if (modelToRun.equals("brknn") || modelToRun.equals("all")) {

				BRkNN brknn = new BRkNN(10, ExtensionType.EXTB);
				brknn.setDebug(true);
				brknn.build(train);
                
				results = eval.evaluate(brknn, test, train);
				System.out.println("Printing brknn Relevance: \n");
				System.out.println(results.getMeasures().toString()
						.replaceAll(", ", "\n"));
				System.out.println("*********************************\n\n");
			}

			if (modelToRun.equals("mlknn") || modelToRun.equals("all")) {

				List<Measure> evaluationMeasures = new ArrayList();
				evaluationMeasures.add(new HammingLoss());
				evaluationMeasures.add(new MacroPrecision(11));

				MLkNN mlknn = new MLkNN();
				mlknn.setDebug(true);
				mlknn.build(train);

			int numInstances = test.getNumInstances();

				for (int instanceIndex = 0; instanceIndex < numInstances; instanceIndex++) {
					Instance instance = test.getDataSet().instance(instanceIndex);
					MultiLabelOutput output = mlknn.makePrediction(instance);
					
					System.out.println(instance.toString());
					
					// do necessary operations with provided prediction output,
					// here just print it out
					//System.out.println(output.hasRanking());
					System.out.println(Arrays.toString(output.getRanking()) );
					//System.out.println(Arrays.toString(output.getBipartition()) );
				}

				
				results = eval.evaluate(mlknn, test, train);
				System.out.println("Printing mlknn Relevance: \n");
				System.out.println(results.getMeasures().toString()
						.replaceAll(", ", "\n"));
				System.out.println("*********************************\n\n");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
