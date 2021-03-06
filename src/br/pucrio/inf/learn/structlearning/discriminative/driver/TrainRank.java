package br.pucrio.inf.learn.structlearning.discriminative.driver;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.pucrio.inf.learn.structlearning.discriminative.algorithm.OnlineStructuredAlgorithm.LearnRateUpdateStrategy;
import br.pucrio.inf.learn.structlearning.discriminative.algorithm.perceptron.LossAugmentedPerceptron;
import br.pucrio.inf.learn.structlearning.discriminative.application.rank.RankDataset;
import br.pucrio.inf.learn.structlearning.discriminative.application.rank.RankInference;
import br.pucrio.inf.learn.structlearning.discriminative.application.rank.RankInput;
import br.pucrio.inf.learn.structlearning.discriminative.application.rank.RankModel;
import br.pucrio.inf.learn.structlearning.discriminative.application.rank.RankOutput;
import br.pucrio.inf.learn.structlearning.discriminative.data.DatasetException;
import br.pucrio.inf.learn.structlearning.discriminative.driver.Driver.Command;
import br.pucrio.inf.learn.util.CommandLineOptionsUtil;

/**
 * Driver to discriminatively train a ranking model.
 * 
 * @author eraldo
 * 
 */
public class TrainRank implements Command {

	/**
	 * Logging object.
	 */
	private static final Log LOG = LogFactory.getLog(TrainRank.class);

	@SuppressWarnings("static-access")
	@Override
	public void run(String[] args) {
		Options options = new Options();

		options.addOption(OptionBuilder.withLongOpt("train").isRequired()
				.withArgName("filename").hasArg()
				.withDescription("Training dataset file name.").create());

		// options.addOption(OptionBuilder
		// .withLongOpt("update")
		// .withArgName("strategy")
		// .hasArg()
		// .withDescription(
		// "Update strategy to train: "
		// + "CLUSTER (default), TREE or ALL.").create());

		options.addOption(OptionBuilder.withLongOpt("templates").isRequired()
				.withArgName("filename").hasArg()
				.withDescription("Feature templates file name.").create());

		options.addOption(OptionBuilder
				.withLongOpt("numepochs")
				.withArgName("integer")
				.hasArg()
				.withDescription(
						"Number of epochs: how many iterations over the"
								+ " training set.").create());

		// options.addOption(OptionBuilder.withLongOpt("model")
		// .withArgName("filename").hasArg()
		// .withDescription("File name to save final model.").create());

		options.addOption(OptionBuilder.withLongOpt("testin")
				.withArgName("filename").hasArg()
				.withDescription("Input test dataset file name.").create());

		options.addOption(OptionBuilder.withLongOpt("testout")
				.withArgName("filename").hasArg()
				.withDescription("Output test dataset file name.").create());

		// options.addOption(OptionBuilder.withLongOpt("scriptpath")
		// .withArgName("path").hasArg()
		// .withDescription("Base path for CoNLL and Python scripts.")
		// .create());

		// options.addOption(OptionBuilder.withLongOpt("conlltest")
		// .withArgName("filename").hasArg()
		// .withDescription("Test dataset on CoNLL format.").create());

		// options.addOption(OptionBuilder
		// .withLongOpt("perepoch")
		// .withDescription(
		// "The evaluation on the test corpus will "
		// + "be performed after each training epoch.")
		// .create());

		options.addOption(OptionBuilder.withLongOpt("seed")
				.withArgName("integer").hasArg()
				.withDescription("Random number generator seed.").create());

		options.addOption(OptionBuilder
				.withLongOpt("lossweight")
				.withArgName("double")
				.hasArg()
				.withDescription(
						"Weight of the loss term in the inference objective"
								+ " function.").create());

		options.addOption(OptionBuilder
				.withLongOpt("noavg")
				.withDescription(
						"Turn off the weight vector averaging, i.e.,"
								+ " the algorithm returns only the final weight "
								+ "vector instead of the average of each step "
								+ "vectors.").create());

		// Parse the command-line arguments.
		CommandLine cmdLine = null;
		PosixParser parser = new PosixParser();
		try {
			cmdLine = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			CommandLineOptionsUtil.usage(getClass().getSimpleName(), options);
		}

		// Print the list of options along the values provided by the user.
		CommandLineOptionsUtil.printOptionValues(cmdLine, options);

		/*
		 * Get the options given in the command-line or the corresponding
		 * default values.
		 */
		String inputCorpusFileName = cmdLine.getOptionValue("train");
		String templatesFileName = cmdLine.getOptionValue("templates");
		int numEpochs = Integer.parseInt(cmdLine.getOptionValue("numepochs",
				"10"));
		// String modelFileName = cmdLine.getOptionValue("model");
		String testInFilename = cmdLine.getOptionValue("testin");
		String testOutFilename = cmdLine.getOptionValue("testout");
		// String scriptBasePathStr = cmdLine.getOptionValue("scriptpath");
		// File conllBasePath = null;
		// if (scriptBasePathStr != null)
		// conllBasePath = new File(scriptBasePathStr);
		// String conllTestFileName = cmdLine.getOptionValue("conlltest");
		// boolean evalPerEpoch = cmdLine.hasOption("perepoch");
		String seedStr = cmdLine.getOptionValue("seed");
		double lossWeight = Double.parseDouble(cmdLine.getOptionValue(
				"lossweight", "0d"));
		boolean averageWeights = !cmdLine.hasOption("noavg");

		RankDataset inDataset = null;

		try {
			LOG.info("Loading train dataset...");
			inDataset = new RankDataset(inputCorpusFileName);
		} catch (IOException e) {
			LOG.error("Loading train dataset", e);
			System.exit(1);
		} catch (DatasetException e) {
			LOG.error("Loading train dataset", e);
			System.exit(1);
		}

		try {
			LOG.info("Loading templates and generating features...");
			inDataset.loadTemplates(templatesFileName, true);
		} catch (IOException e) {
			LOG.error("Loading templates and generating features", e);
			System.exit(1);
		}

		// Inference (prediction) algorithm.
		RankInference inference = new RankInference();

		// Template-based model.
		LOG.info("Allocating initial model...");
		RankModel model = new RankModel();

		// Learning algorithm.
		LossAugmentedPerceptron alg = new LossAugmentedPerceptron(inference,
				model, numEpochs, 1, lossWeight, true, averageWeights,
				LearnRateUpdateStrategy.NONE);
		// Perceptron alg = new Perceptron(inference, model, numEpochs, 1, true,
		// averageWeights, LearnRateUpdateStrategy.NONE);

		if (seedStr != null)
			// User provided seed to random number generator.
			alg.setSeed(Long.parseLong(seedStr));

		// Ignore features not seen in the training corpus.
		// featureEncoding.setReadOnly(true);

		LOG.info("Training model...");
		alg.train(inDataset.getInputs(), inDataset.getOutputs());

		LOG.info(String.format("# updated parameters: %d",
				model.getNumberOfUpdatedParameters()));

		LOG.info("Training done!");

		if (testInFilename != null && testOutFilename != null) {

			RankDataset testDataset = null;
			try {
				LOG.info("Loading test dataset...");
				testDataset = new RankDataset(inDataset);
				testDataset.load(testInFilename);
				LOG.info("Generating features...");
				testDataset.generateFeatures();
			} catch (IOException e) {
				LOG.error("Loading test dataset", e);
				System.exit(1);
			} catch (DatasetException e) {
				LOG.error("Loading test dataset", e);
				System.exit(1);
			}

			LOG.info("Predicting test examples...");
			int numExs = testDataset.getNumberOfExamples();
			RankInput[] inputs = testDataset.getInputs();
			RankOutput[] corrects = testDataset.getOutputs();
			RankOutput[] predicteds = new RankOutput[numExs];
			double map = 0;
			for (int idxEx = 0; idxEx < numExs; ++idxEx) {
				predicteds[idxEx] = inputs[idxEx].createOutput();
				inference.inference(model, inputs[idxEx], predicteds[idxEx]);
				/*
				 * The loss is the value that misses to achieve a average
				 * precision of 1.
				 */
				map += 1 - model.loss(inputs[idxEx], corrects[idxEx],
						predicteds[idxEx]);
			}

			// Mean average precision.
			map = map / numExs;

			LOG.info(String.format("Saving predicted examples to %s...",
					testOutFilename));
			try {
				testDataset.save(testOutFilename, predicteds);
			} catch (FileNotFoundException e) {
				LOG.error("Saving predicted test dataset", e);
				System.exit(1);
			}

			LOG.info(String
					.format("Test dataset predicted with MAP = %f.", map));

		}
	}

}
