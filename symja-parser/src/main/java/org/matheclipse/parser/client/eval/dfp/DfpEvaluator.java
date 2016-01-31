package org.matheclipse.parser.client.eval.dfp;

import org.apache.commons.math3.dfp.Dfp;
import org.matheclipse.parser.client.eval.api.FieldElementEvaluator;
import org.matheclipse.parser.client.eval.api.IASTVisitor;

public class DfpEvaluator extends FieldElementEvaluator<Dfp> {
	/**
	 * Returns a <code>String</code> representation of the given
	 * <code>Complex</code> number.
	 * 
	 * @param c
	 * @return
	 * 
	 */
	public static String toString(Dfp c) {
		return c.toString();
	}

	public DfpEvaluator(IASTVisitor<Dfp> visitor, boolean relaxedSyntax) {
		super(visitor, relaxedSyntax);
	}

	public DfpEvaluator(final int decimalDigits) {
		this(new DfpEvalVisitor(decimalDigits, false), false);
	}

	public DfpEvaluator(final int decimalDigits, boolean relaxedSyntax) {
		this(new DfpEvalVisitor(decimalDigits, relaxedSyntax), relaxedSyntax);
	}
}
