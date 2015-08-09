import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mulan.classifier.MultiLabelLearner;
import mulan.classifier.lazy.BRkNN;
import mulan.classifier.lazy.BRkNN.ExtensionType;
import mulan.classifier.lazy.MLkNN;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.BinaryRelevance;
import mulan.classifier.transformation.CalibratedLabelRanking;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluation;
import mulan.evaluation.Evaluator;
import mulan.evaluation.MultipleEvaluation;
import mulan.evaluation.measure.HammingLoss;
import mulan.evaluation.measure.MLAverageFMeasure;
import mulan.evaluation.measure.MLAveragePrecision;
import mulan.evaluation.measure.MLAverageRecall;
import mulan.evaluation.measure.MacroAverageMeasure;
import mulan.evaluation.measure.MacroFMeasure;
import mulan.evaluation.measure.MacroPrecision;
import mulan.evaluation.measure.MacroRecall;
import mulan.evaluation.measure.MacroSpecificity;
import mulan.evaluation.measure.Measure;
import mulan.evaluation.measure.MicroFMeasure;
import mulan.evaluation.measure.MicroPrecision;
import mulan.evaluation.measure.MicroRecall;
import mulan.evaluation.measure.MicroSpecificity;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
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

		try {
			Instances train = workingTrainingSet;
			MultiLabelInstances mlTrain = new MultiLabelInstances(train,
					trainData.getLabelsMetaData());
			MultiLabelLearner clone = learner.makeCopy();
			clone.build(mlTrain);

			for (int i = 0; i < someFolds; i++) {
				System.out.println("Fold " + (i + 1) + "/" + someFolds);

				// workingTrainingSet.trainCV(someFolds,
				// i);
				// change here
				Instances test = workingTestingSet.testCV(someFolds, i);

				System.out.println("Training: " + train.numInstances()
						+ " Test: " + test.numInstances());

				MultiLabelInstances mlTest = new MultiLabelInstances(test,
						testData.getLabelsMetaData());

				if (hasMeasures) {
					evaluation[i] = eval.evaluate(clone, mlTest, measures);
				} else {
					evaluation[i] = eval.evaluate(clone, mlTest, mlTrain);
				}
				allEvaluations.add(evaluation[i]);

			}

		} catch (Exception ex) {
			Logger.getLogger(Evaluator.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		MultipleEvaluation me = new MultipleEvaluation(evaluation, testData);
		me.calculateStatistics();
		return me;
	}

	public MultipleEvaluation customReleaseWiseCrossValidate(
			MultiLabelLearner learner, MultiLabelInstances trainData,
			boolean hasMeasures, List<Measure> measures, int someFolds) {
		Evaluation[] evaluation = new Evaluation[someFolds];

		Instances workingTrainingSet = new Instances(trainData.getDataSet());
		// workingSet.randomize(new Random(seed));

		Evaluator eval = new Evaluator();

		for (int i = 0; i < someFolds; i++) {
			System.out.println("Fold " + (i + 1) + "/" + someFolds);
			try {
				Instances train = workingTrainingSet.trainCV(someFolds, i);
				// change here
				Instances test = workingTrainingSet.testCV(someFolds, i);

				System.out.println("Training: " + train.numInstances()
						+ " Test: " + test.numInstances());

				MultiLabelInstances mlTrain = new MultiLabelInstances(train,
						trainData.getLabelsMetaData());
				MultiLabelInstances mlTest = new MultiLabelInstances(test,
						trainData.getLabelsMetaData());

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

	public MultiLabelInstances reintegrateModifiedDataSet(
			MultiLabelInstances modifiedDataSet, int instanceIndex)
			throws Exception {
		if (modifiedDataSet == null) {
			throw new IllegalArgumentException("The modified data set is null.");
		}

		LabelsMetaDataImpl newMetaData = (LabelsMetaDataImpl) modifiedDataSet
				.getLabelsMetaData().clone();

		int numLabels = modifiedDataSet.getNumLabels();
		int[] labelIndices = modifiedDataSet.getLabelIndices();

		String[] labelNames = modifiedDataSet.getLabelNames();
		Set<String> trainingLabels = new HashSet();
		Set<String> testingLabels = new HashSet();
		// Set<Integer> existingIndex = new HashSet();
		Map<String, Integer> labelCountMap = new HashMap();

		for (int j = 0; j < numLabels; j++) {
			for (int k = 0; k < modifiedDataSet.getDataSet().size(); k++) {
				Instance instance = modifiedDataSet.getDataSet().get(k);
				int classIdx = labelIndices[j];
				String classValue = instance.attribute(classIdx).value(
						(int) instance.value(classIdx));

				if (classValue.equals("1")) {
					if (k < instanceIndex) {
						// trainingLabels.add(labelNames[j]);
						// k = instanceIndex-1;
						if (labelCountMap.containsKey(labelNames[j])) {
							labelCountMap.put(labelNames[j],
									labelCountMap.get(labelNames[j]) + 1);

							if (labelCountMap.get(labelNames[j]) >= 2)
								trainingLabels.add(labelNames[j]);
						} else {
							labelCountMap.put(labelNames[j], 1);
						}
						// trainingLabels.add(labelNames[j]);

					} else {
						testingLabels.add(labelNames[j]);
						break;
					}

				}
			}
		}

		Set<String> intersection = new HashSet<String>(trainingLabels);
		intersection.retainAll(testingLabels);
		System.err.println("Final set length " + intersection.size());
		if (intersection.size() > 0) {
			trainingLabels.retainAll(testingLabels);
		} else {
			trainingLabels.addAll(testingLabels);
		}

		int numRemovLabels = modifiedDataSet.getNumLabels()
				- trainingLabels.size();
		int counter = 0;
		if (numRemovLabels > 0) {

			// first remove labels from XMl, or metadata
			int[] toRemove = new int[numRemovLabels];
			for (int j = 0; j < numLabels; j++) {
				String lab = labelNames[j];
				if (!trainingLabels.contains(lab)) {
					newMetaData.removeLabelNode(lab);
					toRemove[counter++] = labelIndices[j];
				}
			}

			Remove filterRemove = new Remove();
			filterRemove.setAttributeIndicesArray(toRemove);
			// filterRemove.setInvertSelection(true);

			filterRemove.setInputFormat(modifiedDataSet.getDataSet());
			Instances filtered = Filter.useFilter(modifiedDataSet.getDataSet(),
					filterRemove);

			return new MultiLabelInstances(filtered, newMetaData);

		}

		return new MultiLabelInstances(modifiedDataSet.getDataSet(),
				newMetaData);
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

			MultiLabelInstances fullData;
			MultiLabelInstances sampSet;

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
				// CalibratedLabelRanking br = new
				// CalibratedLabelRanking(brClassifier);
				BinaryRelevance br = new BinaryRelevance(brClassifier);
				br.setDebug(true);
				learner = br;
				System.out.println("Printing Naive Bayes: \n");

			}

			else if (modelToRun.equals("svm")) {

				// CalibratedLabelRanking SVM = new CalibratedLabelRanking(new
				// weka.classifiers.functions.SMO());
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

				BRkNN brknn = new BRkNN(3, ExtensionType.EXTB);
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
				List<Measure> measures = new ArrayList();
				int numOfLabels = releaseDataInstances[i].getNumLabels();
				measures.add(new HammingLoss());
				measures.add(new MicroPrecision(numOfLabels));
				measures.add(new MicroRecall(numOfLabels));
				measures.add(new MicroFMeasure(numOfLabels));
				measures.add(new MicroSpecificity(numOfLabels));
				measures.add(new MicroPrecision(numOfLabels));
				measures.add(new MicroRecall(numOfLabels));
				measures.add(new MicroFMeasure(numOfLabels));
				measures.add(new MicroSpecificity(numOfLabels));

				// train set
				sampSet = releaseDataInstances[i].clone();
				int index = sampSet.getNumInstances();

				// add all testing instances into test set
				sampSet.getDataSet().addAll(index,
						releaseDataInstances[i + 1].clone().getDataSet());

				int allLabels = sampSet.getNumLabels();
				// remove unused label from dataset and metadeta
				sampSet = rcv.reintegrateModifiedDataSet(sampSet, index);

				System.err.println("Reduced Labels: " + sampSet.getNumLabels()
						+ "/" + allLabels);

				// me = rcv.customReleaseWiseCrossValidate(learner, sampSet,
				// false, null, noOfFolds);
				// System.out.println("\n" + me.toString());

				// if(true)
				// continue;

				String rangeToRemove = "";
				rrange = new RemoveRange();
				rrange.setInputFormat(sampSet.getDataSet());

				rangeToRemove += "1-" + index;

				// get back training instances
				rrange.setInstancesIndices(rangeToRemove);
				rrange.setInvertSelection(true);

				Instances releaseDataSet = Filter.useFilter(
						sampSet.getDataSet(), rrange);
				MultiLabelInstances trainSet = new MultiLabelInstances(
						releaseDataSet, sampSet.getLabelsMetaData());

				// get back testing instances
				// rangeToRemove += index + "-" + sampSet.getNumInstances();
				rrange = new RemoveRange();
				rrange.setInputFormat(sampSet.getDataSet());
				rrange.setInstancesIndices(rangeToRemove);

				releaseDataSet = Filter.useFilter(sampSet.getDataSet(), rrange);
				MultiLabelInstances testSet = new MultiLabelInstances(
						releaseDataSet, sampSet.getLabelsMetaData());

				me = rcv.releaseWiseCrossValidate(learner, trainSet, testSet,
						false, null, noOfFolds);

				// me = rcv.releaseWiseCrossValidate(learner,
				// releaseDataInstances[i], releaseDataInstances[i + 1],
				// false, null, noOfFolds);

				System.out.println("\n" + me.toString());

			}

			System.out.println("*********************************\n\n");

			ArrayList<Evaluation> filteredEvals = (ArrayList<Evaluation>) rcv.allEvaluations
					.clone();
			Measure[] allMeasures = new Measure[filteredEvals.get(0)
					.getMeasures().size()];

			for (int i = 0; i < rcv.allEvaluations.size(); i++) {
				allMeasures = filteredEvals.get(i).getMeasures()
						.toArray(allMeasures);
				for (int j = 0; j < allMeasures.length; j++) {
					Measure m = allMeasures[j];
					if (m instanceof MLAverageRecall
							|| m instanceof MLAveragePrecision
							|| m instanceof MLAverageFMeasure) {
						// System.out.println(m);

					} else {
						filteredEvals.get(i).getMeasures().remove(m);
					}
				}
			}

			// System.out.println(filteredEvals.get(0).getMeasures().size());

			Evaluation[] eval = new Evaluation[filteredEvals.size()];
			eval = filteredEvals.toArray(eval);

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
