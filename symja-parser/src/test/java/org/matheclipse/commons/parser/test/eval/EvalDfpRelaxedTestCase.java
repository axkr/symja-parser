package org.matheclipse.commons.parser.test.eval;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.math3.dfp.Dfp;
import org.junit.Assert;
import org.matheclipse.commons.parser.client.eval.BooleanVariable;
import org.matheclipse.commons.parser.client.eval.DoubleEvaluator;
import org.matheclipse.commons.parser.client.eval.api.FieldElementVariable;
import org.matheclipse.commons.parser.client.eval.dfp.DfpEvaluator;

import junit.framework.TestCase;

/**
 * Tests evaluation in <code>double</code> expression mode
 */
public class EvalDfpRelaxedTestCase extends TestCase {

	public EvalDfpRelaxedTestCase(String name) {
		super(name);
	}

	public void check(String in, String compareWith) {
		try {
			DfpEvaluator engine = new DfpEvaluator(50, true);
			Dfp d = engine.evaluate(in);
			assertEquals(d.toString(), compareWith);
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval001() {
		check("42", "42.");
		check("1.0E10", "10000000000.");
		check("1.0E-10", "1.00000000000000000000000000000000000000000000000000e-10");
		check("1.0E+10", "10000000000.");
		check("1.5", "1.5");
		check("-42", "-42.");
		check("+42", "42.");
		check("-42.1", "-42.1");
		check("+42.2", "42.2");
		check("1/7", "0.1428571428571428571428571428571428571428571428571429");
		check("-1/7", "-0.1428571428571428571428571428571428571428571428571429");
		check("-3/4", "-0.75");
		check("+3/4", "0.75");
		check("3^3", "27.");
		check("2+2*2", "6.");
		check("2^9", "512.");
		check("2^3^2", "512.");
		check("(2^3)^2", "64.");
		check("3+4*7", "31.");
		check("3+4*7*3", "87.");
		check("1+2+3+4*7*3", "90.");
		// calculate in Radians
		check("sin(cos(3.2))", "-0.840548425274299610607724353218603116852955931619219");
		// Pi / 2
		check("90.0*Degree", "1.570796326794896592693451253808234469033777713776");
		check("Pi/2", "1.570796326794896619231321691639751442098584699688");
		check("Sin(Pi/2*Cos(Pi))", "-1.");

		check("Max(0,-42,Pi,12)", "12.");
		check("Min(0,-42,Pi,12)", "-42.");

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
			DfpEvaluator engine = new DfpEvaluator(50, true);
			Dfp d = engine.evaluate("sin(pi/2*cOs(pi))");
			assertEquals(d.toString(), "-1.");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval004() {
		try {
			DfpEvaluator engine = new DfpEvaluator(50, true);
			FieldElementVariable<Dfp> vd = engine.defineVariable("X", 3.0);
			Dfp d = engine.evaluate("X^2+3");
			assertEquals(d.toString(), "12.");
			engine.setValue(vd, 4);
			d = engine.evaluate();
			assertEquals(d.toString(), "19.");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval005() {
		try {
			DfpEvaluator engine = new DfpEvaluator(50, true);
			FieldElementVariable<Dfp> vd = engine.defineVariable("X", 3.0);
			Dfp d = engine.evaluate("X^2*x^2-1");
			assertEquals(d.toString(), "80.");
			engine.setValue(vd, 4);
			d = engine.evaluate();
			assertEquals(d.toString(), "255.");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval006() {
		try {
			DfpEvaluator engine = new DfpEvaluator(50, true);

			BooleanVariable vb = new BooleanVariable(true);
			engine.defineVariable("$1", vb);
			BooleanVariable vb2 = new BooleanVariable(true);
			engine.defineVariable("$2", vb2);
			Dfp d = engine.evaluate("if($1 && $2, 1, 0)");
			Assert.assertEquals(d.getReal(), 1d, DoubleEvaluator.EPSILON);
			vb.setValue(false);
			d = engine.evaluate();
			Assert.assertEquals(d.getReal(), 0d, DoubleEvaluator.EPSILON);
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	public void testEval007() {
		try {
			DfpEvaluator engine = new DfpEvaluator(50, true);
			// IDoubleValue vdi = new DoubleVariable(1.0);
			// engine.defineVariable("$i", vdi);
			FieldElementVariable<Dfp> vd = engine.defineVariable("$1", 3.0);
			FieldElementVariable<Dfp> vd2 = engine.defineVariable("$2", -4.0);
			Dfp d = engine.evaluate("$i = $1+$2; iF($i>0, 1, -1)");
			Assert.assertEquals(d.getReal(), -1d, DoubleEvaluator.EPSILON);
			engine.setValue(vd2, 4.0);
			d = engine.evaluate();
			Assert.assertEquals(d.getReal(), 1d, DoubleEvaluator.EPSILON);
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	//

	public void testEval008() {
		try {
			DfpEvaluator engine = new DfpEvaluator(50, true);
			HashSet<String> result = new HashSet<String>();
			engine.getVariables("a+2*b+$c", result);
			Assert.assertEquals(result.size(), 3);
			Assert.assertTrue(result.contains("a"));
			Assert.assertTrue(result.contains("b"));
			Assert.assertTrue(result.contains("$c"));
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	public void testEval009() {
		try {
			DfpEvaluator engine = new DfpEvaluator(50, true);

			BooleanVariable vb = new BooleanVariable(true);
			engine.defineVariable("$1", vb);
			Dfp d = engine.evaluate("IF($1, 1, 0)");
			Assert.assertEquals(d.getReal(), 1d, DoubleEvaluator.EPSILON);
			vb.setValue(false);
			d = engine.evaluate();
			Assert.assertEquals(d.getReal(), 0d, DoubleEvaluator.EPSILON);
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	public void testMissingFunction009() {
		try {
			DfpEvaluator engine = new DfpEvaluator(50, true);
			Dfp d = engine.evaluate("aTest(1.0)");
			assertEquals(d.toString(), "");
		} catch (RuntimeException e) {
			assertEquals("DfpEvaluator#evaluateFunction(FunctionNode) not possible for: atest(1.)", e.getMessage());
		}
	}
}
