package tagger.examples;

import java.util.Map;

import tagger.core.HmmModel;
import tagger.data.Dataset;
import tagger.evaluation.Evaluation;
import tagger.learning.Verbose_res;

/**
 * Evaluate a saved HMM model on the data within a given file. Write the results
 * in terms of precision, recall and F-1; and also the number of entities, the
 * number of predicted entities and the number of correct predicted entities.
 * 
 * @author eraldof
 * 
 */
public class TestHmm {

	public static void main(String[] args) throws Exception {

		if (args.length != 4) {
			System.err
					.print("Syntax error: more arguments are necessary. Correct syntax:\n"
							+ "	<testfile> <modelfile> <observation_feature> <golden_state_feature>\n");
			System.exit(1);
		}

		int arg = 0;
		String testFileName = args[arg++];
		String modelFileName = args[arg++];
		String observationFeatureLabel = args[arg++];
		String stateFeatureLabel = args[arg++];

		double smooth = 1e-6;

		System.out.println(String.format(
				"Evaluating HMM with the following parameters: \n"
						+ "\tTest file: %s\n" + "\tModel file: %s\n"
						+ "\tObservation feature: %s\n"
						+ "\tState feature: %s\n" + "\tSmoothing: %e\n",
				testFileName, modelFileName, observationFeatureLabel,
				stateFeatureLabel, smooth));

		// Load the model.
		HmmModel model = new HmmModel(modelFileName);
		if (smooth > 0.0) {
			model.setEmissionSmoothingProbability(smooth);
			model.normalizeProbabilities();
			model.applyLog();
		}

		// Load the testset.
		Dataset testset = new Dataset(testFileName,
				model.getFeatureValueEncoding());

		// Test the model on a testset.
		model.setUseFinalProbabilities(false);
		model.tag(testset, observationFeatureLabel, "ne");

		// Evaluate the predicted values.
		Evaluation ev = new Evaluation("0");
		Map<String, Verbose_res> results = ev.evaluateSequences(testset,
				stateFeatureLabel, "ne");

		String[] labelOrder = { "LOC", "MISC", "ORG", "PER", "overall" };

		// Write precision, recall and F-1 values.
		System.out.println();
		System.out.println("|  *Class*  |  *P*  |  *R*  |  *F*  |");
		for (String label : labelOrder) {
			Verbose_res res = results.get(label);
			if (res == null)
				continue;
			System.out.println(String.format(
					"|  %s  |  %6.2f |  %6.2f |  %6.2f |", label,
					100 * res.getPrecision(), 100 * res.getRecall(),
					100 * res.getF1()));
		}

		// // Write number of entities: total, predicted and correct.
		// System.out.println();
		// System.out.println("^  class  ^ Total ^ Retrieved ^ Correct ^");
		// for (String label : labelOrder) {
		// Verbose_res res = results.get(label);
		// if (res == null)
		// continue;
		// System.out.println(String.format("| %7s | %5d |  %8d | %7d |",
		// label, res.nobjects, res.nanswers, res.nfullycorrect));
		// }
	}
}
