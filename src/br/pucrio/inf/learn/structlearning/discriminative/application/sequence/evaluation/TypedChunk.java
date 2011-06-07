package br.pucrio.inf.learn.structlearning.discriminative.application.sequence.evaluation;

import br.pucrio.inf.learn.util.HashCodeUtil;

/**
 * Represent a typed chunk of tokens.
 * 
 * @author eraldof
 * 
 */
public class TypedChunk implements Comparable<TypedChunk> {

	/**
	 * The index of the sentence where the chunk lies in.
	 */
	public int sentence;

	/**
	 * The index of the first token of this chunk.
	 */
	public int tokenBeg;

	/**
	 * The index of the last token of this chunk.
	 */
	public int tokenEnd;

	/**
	 * The type of this chunk.
	 */
	public String type;

	/**
	 * Constructor.
	 * 
	 * @param sentence
	 * @param tokenBeg
	 * @param tokenEnd
	 * @param type
	 */
	public TypedChunk(int sentence, int tokenBeg, int tokenEnd, String type) {
		this.sentence = sentence;
		this.tokenBeg = tokenBeg;
		this.tokenEnd = tokenEnd;
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypedChunk))
			return false;
		TypedChunk other = (TypedChunk) obj;
		return sentence == other.sentence && tokenBeg == other.tokenBeg
				&& tokenEnd == other.tokenEnd && type.equals(other.type);
	}

	@Override
	public int hashCode() {
		Object[] array = { sentence, tokenBeg, tokenEnd, type };
		return HashCodeUtil.hash(HashCodeUtil.SEED, array);
	}

	@Override
	public int compareTo(TypedChunk o) {
		if (sentence < o.sentence)
			return -1;
		if (sentence > o.sentence)
			return 1;

		if (tokenBeg < o.tokenBeg)
			return -1;
		if (tokenBeg > o.tokenBeg)
			return 1;

		if (tokenEnd < o.tokenEnd)
			return -1;
		if (tokenEnd > o.tokenEnd)
			return 1;

		return type.compareTo(o.type);
	}

}