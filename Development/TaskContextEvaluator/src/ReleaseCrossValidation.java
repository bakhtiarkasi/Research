import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import mulan.classifier.MultiLabelLearner;
import mulan.classifier.lazy.BRkNN;
import mulan.classifier.lazy.MLkNN;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.BinaryRelevance;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluation;
import mulan.evaluation.Evaluator;
import mulan.evaluation.MultipleEvaluation;
import mulan.evaluation.measure.Measure;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemovePercentage;
import weka.filters.unsupervised.instance.RemoveRange;

public class ReleaseCrossValidation {

	public ArrayList<Evaluation> allEvaluations = new ArrayList();

	public MultipleEvaluation releaseWiseCrossValidate(
			MultiLabelLearner learner, MultiLabelInstances trainData,
			MultiLabelInstances testData, boolean hasMeasures,
			List<Measure> measures, int someFolds) {
		Evaluation[] evaluation = new Evaluation[someFolds];

		Instances workingTrainingSet = new Instances(trainData.getDataSet());
		Instances workingTestingSet = new Instances(testData.getDataSet());
		// workingSet.randomize(new Random(seed));

		Evaluator eval = new Evaluator();

		for (int i = 0; i < someFolds; i++) {
			System.out.println("Fold " + (i + 1) + "/" + someFolds);
			try {
				Instances train = workingTrainingSet.trainCV(someFolds, i);
				// change here
				Instances test = workingTestingSet.testCV(someFolds, i);

				System.out.println("Training: " + train.numInstances()
						+ " Test: " + test.numInstances());

				MultiLabelInstances mlTrain = new MultiLabelInstances(train,
						trainData.getLabelsMetaData());
				MultiLabelInstances mlTest = new MultiLabelInstances(test,
						testData.getLabelsMetaData());

				MultiLabelLearner clone = learner.makeCopy();
				clone.build(mlTrain);
				if (hasMeasures) {
					evaluation[i] = eval.evaluate(clone, mlTest, measures);
				} else {
					evaluation[i] = eval.evaluate(clone, mlTest, mlTrain);
				}
				allEvaluations.add(evaluation[i]);

			} catch (Exception ex) {
				Logger.getLogger(Evaluator.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		}
		MultipleEvaluation me = new MultipleEvaluation(evaluation, trainData);
		me.calculateStatistics();
		return me;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String arffFile = null;
		String xmlFile = null;
		String releaseInstancesCSV = null;
		String modelToRun = null;
		int noOfFolds = 10;
		ReleaseCrossValidation rcv = new ReleaseCrossValidation();

		MultiLabelInstances[] releaseDataInstances;

		try {

			arffFile = Utils.getOption('a', args);
			xmlFile = Utils.getOption('x', args);
			releaseInstancesCSV = Utils.getOption("rel", args);
			modelToRun = Utils.getOption("m", args);
			noOfFolds = Integer.parseInt(Utils.getOption("folds", args));

			final MultiLabelInstances fullProjectData = new MultiLabelInstances(
					arffFile, xmlFile);
			final Instances fullDataSet = fullProjectData.getDataSet();

			String[] releases = releaseInstancesCSV.trim().split(",");
			int[] releaseIndexes = new int[releases.length];

			int sum = 0;
			for (int i = 0; i < releases.length; i++) {
				releaseIndexes[i] = sum + 1;
				sum = sum + Integer.parseInt(releases[i]);
			}

			// for each half
			releaseDataInstances = new MultiLabelInstances[2 * releases.length];

			// use remove range filter to remove a set of instances from the
			// data
			// set.
			RemoveRange rrange;

			// For each release separate data into 2 halves
			RemovePercentage rmvp = new RemovePercentage();
			rmvp.setPercentage(50.0);

			Instances trainDataSet;
			Instances testDataSet;

			for (int i = 0; i < releaseIndexes.length; i++) {

				String rangeToRemove = "";
				rrange = new RemoveRange();
				rrange.setInputFormat(fullDataSet);

				// last in range
				if (i == releaseIndexes.length - 1) {
					rangeToRemove += releaseIndexes[i] + "-"
							+ fullDataSet.numInstances();
				}

				// all others ranges
				else {
					rangeToRemove += releaseIndexes[i] + "-"
							+ (releaseIndexes[i + 1] - 1);

				}
				System.out.println("Ranges: " + rangeToRemove);

				rrange.setInstancesIndices(rangeToRemove);
				rrange.setInvertSelection(true);

				Instances releaseDataSet;

				releaseDataSet = Filter.useFilter(fullDataSet, rrange);

				rmvp = new RemovePercentage();
				rmvp.setPercentage(50.0);
				rmvp.setInputFormat(releaseDataSet);

				// separate training set, select first 50% using set invert
				rmvp.setInvertSelection(true);
				trainDataSet = Filter.useFilter(releaseDataSet, rmvp);

				// separate testing set, have to reinitialize every time
				rmvp = new RemovePercentage();
				rmvp.setPercentage(50.0);
				rmvp.setInputFormat(releaseDataSet);
				testDataSet = Filter.useFilter(releaseDataSet, rmvp);

				// each data set has two corresponding sets to be saved
				int releaseIndex = i * 2;

				releaseDataInstances[releaseIndex] = new MultiLabelInstances(
						trainDataSet, xmlFile);
				releaseDataInstances[releaseIndex + 1] = new MultiLabelInstances(
						testDataSet, xmlFile);

			}

			MultipleEvaluation me;
			MultiLabelLearner learner = null;

			if (modelToRun.equals("nb")) {
				Classifier brClassifier = new NaiveBayes();
				BinaryRelevance br = new BinaryRelevance(brClassifier);
				br.setDebug(true);
				learner = br;
				System.out.println("Printing Naive Bayes: \n");

			}

			else if (modelToRun.equals("svm")) {

				BinaryRelevance SVM = new BinaryRelevance(
						new weka.classifiers.functions.SMO());
				SVM.setDebug(true);
				learner = SVM;
				System.out.println("Printing SVM Relevance: \n");
			}

			else if (modelToRun.equals("rakel")) {

				RAkEL rakel = new RAkEL(new LabelPowerset(new J48()));
				rakel.setDebug(true);
				learner = rakel;
				System.out.println("Printing Rakel Relevance: \n");
			}

			else if (modelToRun.equals("brknn")) {

				BRkNN brknn = new BRkNN();
				brknn.setDebug(true);
				learner = brknn;
				System.out.println("Printing brknn Relevance: \n");
			}

			else if (modelToRun.equals("mlknn")) {

				MLkNN mlknn = new MLkNN();
				mlknn.setDebug(true);
				learner = mlknn;
				System.out.println("Printing mlknn Relevance: \n");
			}

			for (int i = 0; i < releaseDataInstances.length - 1; i++) {

				System.out.println("Results for: [" + (i + 1) + "," + (i + 2)
						+ "]");

				me = rcv.releaseWiseCrossValidate(learner, releaseDataInstances[i],
						releaseDataInstances[i + 1], false, null, noOfFolds);

				System.out.println("\n" + me.toString());

			}

			System.out.println("*********************************\n\n");

			Evaluation[] eval = new Evaluation[rcv.allEvaluations.size()];
			eval = rcv.allEvaluations.toArray(eval);

			me = new MultipleEvaluation(eval, releaseDataInstances[0]);
			me.calculateStatistics();
			System.out.println("Printing mean results \n");
			System.out.println(me);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
