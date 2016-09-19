package org.matheclipse.commons.parser.test.eval;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllEvalTests extends TestCase {
	public AllEvalTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite s = new TestSuite();

		s.addTestSuite(EvalComplexTestCase.class);
		s.addTestSuite(EvalDoubleTestCase.class);
		s.addTestSuite(EvalDoubleRelaxedTestCase.class);
		s.addTestSuite(EvalComplexRelaxedTestCase.class);
		s.addTestSuite(EvalDfpRelaxedTestCase.class);
		s.addTestSuite(EvalBigFractionRelaxedTestCase.class);
		return s; 
	}

}
