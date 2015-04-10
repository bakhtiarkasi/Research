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

import weka.classifiers.trees.J48;
import weka.core.Utils;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.BinaryRelevance;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mulan.classifier.lazy.MLkNN;
import mulan.data.InvalidDataFormatException;
import mulan.evaluation.MultipleEvaluation;

/**
 * Class demonstrating a simple cross-validation experiment
 *
 * @author Grigorios Tsoumakas
 * @version 2010.12.15
 */
public class CrossValidationExperim {

    /**
     * Executes this example
     *
     * @param args command-line arguments -arff and -xml
     */
    public static void main(String[] args) {

        try {
            // e.g. -arff emotions.arff
            String arffFilename = Utils.getOption("a", args); 
            // e.g. -xml emotions.xml
            String xmlFilename = Utils.getOption("x", args);

            System.out.println("Loading the dataset...");
            MultiLabelInstances dataset = new MultiLabelInstances(arffFilename, xmlFilename);

            BinaryRelevance SVM = new BinaryRelevance(
					new weka.classifiers.functions.SMO());
			SVM.setDebug(true);
			//SVM.build(data);


            Evaluator eval = new Evaluator();
            MultipleEvaluation results;

            int numFolds = 10;
            results = eval.crossValidate(SVM, dataset, numFolds);
            System.out.println(results);
          } catch (InvalidDataFormatException ex) {
            Logger.getLogger(CrossValidationExperim.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CrossValidationExperim.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}