package br.pucrio.inf.learn.structlearning.application.sequence;

import br.pucrio.inf.learn.structlearning.data.ExampleInput;
import br.pucrio.inf.learn.structlearning.data.ExampleOutput;
import br.pucrio.inf.learn.structlearning.task.Model;

/**
 * Abstract class that represents a gereric HMM including the inference
 * algorithm (Viterbi) and the update procedure. The derived concrete classes
 * must represent the parameters internally and implement the methods to access
 * them (get's and update's).
 * 
 * @author eraldof
 * 
 */
public abstract class Hmm implements Model {

	/**
	 * Return the number of possible states (labels) of this model.
	 * 
	 * @return
	 */
	public abstract int getNumberOfStates();

	/**
	 * Return the code (index) of the default state that is used by the
	 * inference algorithm wherever all states have the same weight.
	 * 
	 * @return
	 */
	protected abstract int getDefaultState();

	/**
	 * Return the weight associated with the given initial state.
	 * 
	 * @param state
	 * @return
	 */
	public abstract double getInitialStateParameter(int state);

	/**
	 * Return the weight associated with the transition from the two given
	 * states.
	 * 
	 * @param fromState
	 *            the origin state.
	 * @param toState
	 *            the end state.
	 * @return
	 */
	public abstract double getTransitionParameter(int fromState, int toState);

	/**
	 * Return the weight associated with the emission of the given symbol from
	 * the given state.
	 * 
	 * @param state
	 * @param symbol
	 * @return
	 */
	public abstract double getEmissionParameter(int state, int symbol);

	/**
	 * Add the given value to the initial-state parameter of the mobel.
	 * 
	 * @param state
	 * @param value
	 */
	protected abstract void updateInitialStateParameter(int state, double value);

	/**
	 * Add the given value to every feature at the token of the input sequence.
	 * 
	 * @param input
	 * @param token
	 * @param state
	 * @param value
	 */
	protected abstract void updateEmissionParameters(SequenceInput input,
			int token, int state, double value);

	/**
	 * Add the given value to the transition parameter.
	 * 
	 * @param fromToken
	 * @param toToken
	 * @param value
	 */
	protected abstract void updateTransitionParameter(int fromToken,
			int toToken, double value);

	/**
	 * Return the sum of the emission weights associated with the features in
	 * the token <code>token</code> of the sequence <code>input</code>.
	 * 
	 * @param input
	 * @param token
	 * @param state
	 * @return
	 */
	public double getTokenEmissionWeight(SequenceInput input, int token,
			int state) {
		double weight = 0d;
		for (int ftr : input.getFeatures(token))
			weight += getEmissionParameter(state, ftr);
		return weight;
	}

	/**
	 * Given an input sequence, tag the given output sequence with the best
	 * label sequence for this HMM.
	 * 
	 * @param input
	 * @param output
	 * @param defaultState
	 */
	public void tag(SequenceInput input, SequenceOutput output) {
		// State used wherever all labels have equal weight.
		int defaultState = getDefaultState();

		// Example length.
		int numberOfStates = getNumberOfStates();
		int lenExample = input.size();

		// Best partial-path weights.
		double[][] delta = new double[lenExample][numberOfStates];
		// Best partial-path backward table.
		int[][] psi = new int[lenExample][numberOfStates];

		// Weights for the first token.
		for (int state = 0; state < numberOfStates; ++state)
			delta[0][state] = getTokenEmissionWeight(input, 0, state)
					+ getInitialStateParameter(state);

		// Apply each step of the Viterbi algorithm.
		for (int tkn = 1; tkn < lenExample; ++tkn)
			for (int state = 0; state < numberOfStates; ++state)
				viterbi(delta, psi, input, tkn, state, defaultState);

		// The default state is always the fisrt option.
		int bestState = defaultState;
		double bestWeight = delta[lenExample - 1][defaultState];

		// Find the best last state.
		for (int state = 0; state < numberOfStates; ++state) {
			double weight = delta[lenExample - 1][state];
			if (weight > bestWeight) {
				bestWeight = weight;
				bestState = state;
			}
		}

		// Reconstruct the best path from the best final state, and tag the
		// input.
		backwardTag(output, psi, bestState);
	}

	/**
	 * Calculate the best previous state (fromState) for the given
	 * <code>toState</code> at the given <code>token</code>.
	 * 
	 * @param delta
	 * @param psi
	 * @param input
	 * @param token
	 * @param toState
	 * @param defaultState
	 */
	protected void viterbi(double[][] delta, int[][] psi, SequenceInput input,
			int token, int toState, int defaultState) {
		// Number of states.
		int numStates = getNumberOfStates();

		// Choose the best previous state (consider only the transition weight).
		int maxState = defaultState;
		double maxWeight = delta[token - 1][defaultState]
				+ getTransitionParameter(defaultState, toState);
		for (int fromState = 0; fromState < numStates; ++fromState) {
			double logProb = delta[token - 1][fromState]
					+ getTransitionParameter(fromState, toState);
			if (logProb > maxWeight) {
				maxWeight = logProb;
				maxState = fromState;
			}
		}

		// Set delta and psi according the best from-state.
		psi[token][toState] = maxState;
		delta[token][toState] = maxWeight
				+ getTokenEmissionWeight(input, token, toState);
	}

	/**
	 * Follow the given psi map (starting at the <code>bestFinalState</code>)
	 * and tag the given output sequence.
	 * 
	 * @param output
	 * @param psi
	 * @param bestFinalState
	 */
	protected void backwardTag(SequenceOutput output, int[][] psi,
			int bestFinalState) {
		int len = output.size();
		for (int token = len - 1; token > 0; --token) {
			output.setLabel(token, bestFinalState);
			bestFinalState = psi[token - 1][bestFinalState];
		}
	}

	/**
	 * Update the parameters of the features that differ from the two given
	 * output sequences and that are present in the given input sequence.
	 * 
	 * @param input
	 * @param outputCorrect
	 * @param outputPredicted
	 * @param learningRate
	 */
	public void update(SequenceInput input, SequenceOutput outputCorrect,
			SequenceOutput outputPredicted, double learningRate) {
		// First token.
		int labelCorrect = outputCorrect.getLabel(0);
		int labelPredicted = outputPredicted.getLabel(0);
		if (labelCorrect != labelPredicted) {
			// Initial state parameters.
			updateInitialStateParameter(labelCorrect, learningRate);
			updateInitialStateParameter(labelPredicted, -learningRate);
			// Emission parameters.
			updateEmissionParameters(input, 0, labelCorrect, learningRate);
			updateEmissionParameters(input, 0, labelPredicted, -learningRate);
		}

		int prevLabelCorrect = labelCorrect;
		int prevLabelPredicted = labelPredicted;
		for (int tkn = 1; tkn < input.size(); ++tkn) {
			labelCorrect = outputCorrect.getLabel(tkn);
			labelPredicted = outputPredicted.getLabel(tkn);
			if (labelCorrect != labelPredicted) {
				// Emission parameters.
				updateEmissionParameters(input, tkn, labelCorrect, learningRate);
				updateEmissionParameters(input, tkn, labelPredicted,
						-learningRate);
				// Transition parameters.
				updateTransitionParameter(prevLabelCorrect, labelCorrect,
						learningRate);
				updateTransitionParameter(prevLabelPredicted, labelPredicted,
						-learningRate);
			} else if (prevLabelCorrect != prevLabelPredicted) {
				// Transition parameters.
				updateTransitionParameter(prevLabelCorrect, labelCorrect,
						learningRate);
				updateTransitionParameter(prevLabelPredicted, labelPredicted,
						-learningRate);
			}

			prevLabelCorrect = labelCorrect;
			prevLabelPredicted = labelPredicted;
		}
	}

	@Override
	public void inference(ExampleInput input, ExampleOutput output) {
		tag((SequenceInput) input, (SequenceOutput) output);
	}

	@Override
	public void update(ExampleInput input, ExampleOutput outputCorrect,
			ExampleOutput outputPredicted, double learningRate) {
		update((SequenceInput) input, (SequenceOutput) outputCorrect,
				(SequenceOutput) outputPredicted, learningRate);
	}

}
