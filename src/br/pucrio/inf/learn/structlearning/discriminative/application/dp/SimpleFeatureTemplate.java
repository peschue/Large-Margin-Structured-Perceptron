package br.pucrio.inf.learn.structlearning.discriminative.application.dp;

import br.pucrio.inf.learn.structlearning.discriminative.application.dp.data.DPInput;

/**
 * Feature template that conjoins a given set of basic features.
 * 
 * @author eraldo
 * 
 */
public class SimpleFeatureTemplate implements FeatureTemplate {

	/**
	 * Template index.
	 */
	private int index;

	/**
	 * Feature combination given by feature indexes.
	 */
	private int[] features;

	/**
	 * Temporary feature used to instantiate new features.
	 */
	private final Feature tempFeature;

	/**
	 * Create a template with the given feature combination.
	 * 
	 * @param features
	 */
	public SimpleFeatureTemplate(int index, int[] features) {
		this.index = index;
		this.features = features;
		this.tempFeature = new Feature(index, new int[features.length]);
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int[] getFeatures() {
		return features;
	}

	@Override
	public Feature getInstance(DPInput input, int idxHead, int idxDep) {
		int[] basicFeatures = input.getBasicFeatures(idxHead, idxDep);
		if (basicFeatures == null)
			return null;
		int[] values = tempFeature.getValues();
		for (int idx = 0; idx < features.length; ++idx)
			values[idx] = basicFeatures[features[idx]];
		tempFeature.setTemplateIndex(index);
		return tempFeature;
	}

	@Override
	public Feature newInstance(DPInput input, int idxHead, int idxDep) {
		int[] basicFeatures = input.getBasicFeatures(idxHead, idxDep);
		if (basicFeatures == null)
			return null;
		int[] values = new int[features.length];
		for (int idx = 0; idx < features.length; ++idx)
			values[idx] = basicFeatures[features[idx]];
		return new Feature(index, values);
	}

}