/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package mulan.examples;

import java.util.Arrays;

import mulan.data.MultiLabelInstances;
import mulan.dimensionalityReduction.BinaryRelevanceAttributeEvaluator;
import mulan.dimensionalityReduction.Ranker;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Demonstrates the attribute selection capabilities of Mulan
 * 
 * @author Grigorios Tsoumakas
 * @version 2012.02.02
 */
public class ChiSquareReduction {

	/**
	 * Executes this example
	 * 
	 * @param args
	 *            command-line arguments -path and -filestem, e.g. -path
	 *            datasets/ -filestem emotions
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String path = Utils.getOption("path", args);
		String filestem = Utils.getOption("filestem", args);
		String attributesToKeep = Utils.getOption("numattribs", args);
		final int NUM_TO_KEEP = Integer.parseInt(attributesToKeep);

		MultiLabelInstances mlData = new MultiLabelInstances(path + filestem
				+ ".arff", path + filestem + ".xml");

		ASEvaluation ase = new ChiSquaredAttributeEval();
		BinaryRelevanceAttributeEvaluator ae = new BinaryRelevanceAttributeEvaluator(
				ase, mlData, "max", "none", "eval");

		Ranker r = new Ranker();
		int[] result = r.search(ae, mlData);
		// System.out.println(Arrays.toString(result));

		System.out.println(mlData.getDataSet().numAttributes());

		if (NUM_TO_KEEP == 0) {
			for (int i = 0; i < mlData.getFeatureIndices().length; i++) {
				System.out.println("Attribute "
						+ mlData.getDataSet()
								.attribute(mlData.getFeatureIndices()[i])
								.name()
						+ " : "
						+ ae.evaluateAttribute(mlData.getDataSet()
								.attribute(mlData.getFeatureIndices()[i])
								.index()));
			}
		}

		else {
			int[] toKeep = new int[NUM_TO_KEEP + mlData.getNumLabels()];
			System.arraycopy(result, 0, toKeep, 0, NUM_TO_KEEP);
			int[] labelIndices = mlData.getLabelIndices();
			System.arraycopy(labelIndices, 0, toKeep, NUM_TO_KEEP,
					mlData.getNumLabels());

			Remove filterRemove = new Remove();
			filterRemove.setAttributeIndicesArray(toKeep);
			filterRemove.setInvertSelection(true);
			filterRemove.setInputFormat(mlData.getDataSet());
			Instances filtered = Filter.useFilter(mlData.getDataSet(),
					filterRemove);
			MultiLabelInstances mlFiltered = new MultiLabelInstances(filtered,
					mlData.getLabelsMetaData());

			System.out.println(mlFiltered.getDataSet());
		}

	}
}