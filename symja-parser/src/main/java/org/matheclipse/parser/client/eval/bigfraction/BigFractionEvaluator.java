package org.matheclipse.parser.client.eval.bigfraction;

import org.apache.commons.math3.fraction.BigFraction;
import org.matheclipse.parser.client.eval.api.FieldElementEvaluator;
import org.matheclipse.parser.client.eval.api.IASTVisitor;

public class BigFractionEvaluator extends FieldElementEvaluator<BigFraction> {
	/**
	 * Returns a <code>String</code> representation of the given
	 * <code>org.apache.commons.math3.fraction.BigFraction</code> number.
	 * 
	 * @param c
	 * @return
	 * 
	 */
	public static String toString(BigFraction bf) {
		return bf.toString();
	}

	public BigFractionEvaluator(IASTVisitor<BigFraction> visitor, boolean relaxedSyntax) {
		super(visitor, relaxedSyntax);
	}

	public BigFractionEvaluator() {
		this(new BigFractionEvalVisitor(false), false);
	}

	public BigFractionEvaluator(boolean relaxedSyntax) {
		this(new BigFractionEvalVisitor(relaxedSyntax), relaxedSyntax);
	}
}
