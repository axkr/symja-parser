package org.matheclipse.commons.parser.test.eval;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Assert;
import org.matheclipse.commons.parser.client.eval.BooleanVariable;
import org.matheclipse.commons.parser.client.eval.DoubleEvaluator;
import org.matheclipse.commons.parser.client.eval.DoubleVariable;
import org.matheclipse.commons.parser.client.eval.IDoubleValue;

import junit.framework.TestCase;

/**
 * Tests evaluation in <code>double</code> expression mode
 */
public class EvalDoubleRelaxedTestCase extends TestCase {

	public EvalDoubleRelaxedTestCase(String name) {
		super(name);
	}

	public void check(String in, String compareWith) {
		try {
			DoubleEvaluator engine = new DoubleEvaluator(true);
			double d = engine.evaluate(in);
			assertEquals(Double.valueOf(d).toString(), compareWith);
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval001() {
		check("42", "42.0");
		check("1.0E10", "1.0E10");
		check("1.0E-10", "1.0E-10");
		check("1.0E+10", "1.0E10");
		// throws NumberFormatException
		// check("1.0E", "");
		check("1.5", "1.5");
		check("-42", "-42.0");
		check("+42", "42.0");
		check("-42.1", "-42.1");
		check("+42.2", "42.2");
		check("-3/4", "-0.75");
		check("+3/4", "0.75");
		check("3^3", "27.0");
		check("2+2*2", "6.0");
		check("2^9", "512.0");
		check("2^3^2", "512.0");
		check("(2^3)^2", "64.0");
		check("3+4*7", "31.0");
		check("3+4*7*3", "87.0");
		check("1+2+3+4*7*3", "90.0");
		// calculate in Radians
		check("sin(cos(3.2))", "-0.8405484252742996");
		// Pi / 2
		check("90.0*Degree", "1.5707963267948966");
		check("Pi/2", "1.5707963267948966");
		check("Sin(Pi/2*Cos(Pi))", "-1.0");

		check("Max(0,-42,Pi,12)", "12.0");
		check("Min(0,-42,Pi,12)", "-42.0");

		// check("Random[]", "-1.0");
	}

	public void testEval002() {
		check("if(3/4<0.51, 1.1, -1.2)", "-1.2");
		check("if(True, 1.1, -1.2)", "1.1");
		check("if(3/4>0.51 && 3/8>0.1, 1.1, -1.2)", "1.1");
		check("if(3/4>0.51 || 3/8>0.1, 1.1, -1.2)", "1.1");
		check("if(!(3/4>0.51 || 3/8>0.1), 1.1, -1.2)", "-1.2");
	}

	public void testEval003() {
		try {
			DoubleEvaluator engine = new DoubleEvaluator(true);
			double d = engine.evaluate("sin(pi/2*cOs(pi))");
			assertEquals(Double.toString(d), "-1.0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval004() {
		try {
			IDoubleValue vd = new DoubleVariable(3.0);
			DoubleEvaluator engine = new DoubleEvaluator(true);
			engine.defineVariable("X", vd);
			double d = engine.evaluate("X^2+3");
			assertEquals(Double.valueOf(d).toString(), "12.0");
			vd.setValue(4);
			d = engine.evaluate();
			assertEquals(Double.valueOf(d).toString(), "19.0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval005() {
		try {
			IDoubleValue vd = new DoubleVariable(3.0);
			DoubleEvaluator engine = new DoubleEvaluator(true);
			engine.defineVariable("X", vd);
			double d = engine.evaluate("X^2*x^2-1");
			assertEquals(Double.valueOf(d).toString(), "80.0");
			vd.setValue(4);
			d = engine.evaluate();
			assertEquals(Double.valueOf(d).toString(), "255.0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval006() {
		try {
			DoubleEvaluator engine = new DoubleEvaluator(true);

			BooleanVariable vb = new BooleanVariable(true);
			engine.defineVariable("$1", vb);
			BooleanVariable vb2 = new BooleanVariable(true);
			engine.defineVariable("$2", vb2);
			double d = engine.evaluate("if($1 && $2, 1, 0)");
			Assert.assertEquals(d, 1d, DoubleEvaluator.EPSILON);
			vb.setValue(false);
			d = engine.evaluate();
			Assert.assertEquals(d, 0d, DoubleEvaluator.EPSILON);
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	public void testEval007() {
		try {
			DoubleEvaluator engine = new DoubleEvaluator(true);
			// IDoubleValue vdi = new DoubleVariable(1.0);
			// engine.defineVariable("$i", vdi);
			IDoubleValue vd = new DoubleVariable(3.0);
			engine.defineVariable("$1", vd);
			IDoubleValue vd2 = new DoubleVariable(-4.0);
			engine.defineVariable("$2", vd2);
			double d = engine.evaluate("$i = $1+$2; iF($i>0, 1, -1)");
			Assert.assertEquals(d, -1d, DoubleEvaluator.EPSILON);
			vd2.setValue(4.0);
			d = engine.evaluate();
			Assert.assertEquals(d, 1d, DoubleEvaluator.EPSILON);
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	//

	public void testEval008() {
		try {
			DoubleEvaluator engine = new DoubleEvaluator(true);
			HashSet<String> result = new HashSet<String>();
			engine.getVariables("a+2*b+$c", result);
			ArrayList<String> list = new ArrayList<String>();
			for (String string : result) {
				list.add(string);
			}
			Assert.assertEquals(list.toString(), "[a, b, $c]");
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	public void testEval009() {
		try {
			DoubleEvaluator engine = new DoubleEvaluator(true);

			BooleanVariable vb = new BooleanVariable(true);
			engine.defineVariable("$1", vb);
			double d = engine.evaluate("IF($1, 1, 0)");
			Assert.assertEquals(d, 1d, DoubleEvaluator.EPSILON);
			vb.setValue(false);
			d = engine.evaluate();
			Assert.assertEquals(d, 0d, DoubleEvaluator.EPSILON);
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	public void testMissingFunction009() {
		try {
			DoubleEvaluator engine = new DoubleEvaluator(true);
			double d = engine.evaluate("aTest(1.0)");
			assertEquals(Double.toString(d), "");
		} catch (RuntimeException e) {
			assertEquals("EvalDouble#evaluateFunction(FunctionNode) not possible for: atest(1.0)", e.getMessage());
		}
	}
	
	public void testEval011() {
		try {
			DoubleEvaluator engine = new DoubleEvaluator(true);
			engine.defineVariable("x", -1);
			Double d = engine.evaluate("abs(x)");
			assertEquals(Double.toString(d), "1.0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
}