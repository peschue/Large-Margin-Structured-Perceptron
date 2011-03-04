package br.pucrio.inf.learn.structlearning.task;

import java.io.PrintStream;

import br.pucrio.inf.learn.structlearning.data.ExampleInput;
import br.pucrio.inf.learn.structlearning.data.ExampleOutput;
import br.pucrio.inf.learn.structlearning.data.StringEncoding;

/**
 * Interface of a task-specific model.
 * 
 * @author eraldof
 * 
 */
public interface Model {

	/**
	 * Update this model according to the two outputs (correct and predicted)
	 * for the given input.
	 * 
	 * @param input
	 * @param outputCorrect
	 * @param outputPredicted
	 * @param learningRate
	 * @return the loss between the correct and the predicted outputs.
	 */
	double update(ExampleInput input, ExampleOutput outputCorrect,
			ExampleOutput outputPredicted, double learningRate);

	/**
	 * Account the updates done during the last iteration.
	 * 
	 * @param iteration
	 */
	void sumUpdates(int iteration);

	/**
	 * Average the parameters of all iterations.
	 * 
	 * @param numberOfIterations
	 */
	void average(int numberOfIterations);

	/**
	 * Serialize the model to the given stream.
	 * 
	 * @param ps
	 * @param featureEncoding
	 * @param stateEncoding
	 */
	void save(PrintStream ps, StringEncoding featureEncoding,
			StringEncoding stateEncoding);

	Object clone() throws CloneNotSupportedException;

}
