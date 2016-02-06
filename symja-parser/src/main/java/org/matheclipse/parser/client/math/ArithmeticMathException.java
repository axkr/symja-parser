package org.matheclipse.parser.client.math;

import org.apache.commons.math3.exception.MathRuntimeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class ArithmeticMathException extends MathRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9048053527631483568L;

	final String fMessage;

	public ArithmeticMathException(String message) {
		super(LocalizedFormats.ILLEGAL_STATE);
		fMessage = message;
	}

	@Override
	public String getLocalizedMessage() {
		return fMessage;
	}

	@Override
	public String getMessage() {
		return fMessage;
	}
}
