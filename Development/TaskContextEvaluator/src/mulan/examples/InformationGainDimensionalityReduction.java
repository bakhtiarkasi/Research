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
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class InformationGainDimensionalityReduction {

	public static void main(String[] args) throws Exception {
		String path = Utils.getOption("path", args);
		String filestem = Utils.getOption("filestem", args);
		MultiLabelInstances mlData = new MultiLabelInstances(path + filestem
				+ ".arff", path + filestem + ".xml");
		String attributesToKeep = Utils.getOption("numattribs", args);
		final int NUM_TO_KEEP = Integer.parseInt(attributesToKeep);

		ASEvaluation ase = new GainRatioAttributeEval();
		BinaryRelevanceAttributeEvaluator ae = new BinaryRelevanceAttributeEvaluator(
				ase, mlData, "max", "dl", "eval");

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
		} else {
			Ranker r = new Ranker();
			int[] result = r.search(ae, mlData);
			System.out.println(Arrays.toString(result));

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

			System.out.println("\n\n\n\n" + mlFiltered.getDataSet());
		}
		// You can now work on the reduced multi-label dataset mlFiltered
	}
}