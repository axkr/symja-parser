package org.matheclipse.commons.parser.test.eval;

import org.apache.commons.math3.complex.Complex;
import org.junit.Assert;
import org.matheclipse.commons.parser.client.eval.BooleanVariable;
import org.matheclipse.commons.parser.client.eval.ComplexEvaluator;
import org.matheclipse.commons.parser.client.eval.ComplexVariable;

import junit.framework.TestCase;

/**
 * Tests evaluation in <code>Complex</code> expression mode
 * 
 */
public class EvalComplexRelaxedTestCase extends TestCase {

	public EvalComplexRelaxedTestCase(String name) {
		super(name);
	}

	public void check(String in, String compareWith) {
		try {
			ComplexEvaluator engine = new ComplexEvaluator(true);
			Complex c = engine.evaluate(in);
			String result = ComplexEvaluator.toString(c);
			assertEquals(result, compareWith);
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval001() {
		check("-I", "-0.0+I*(-1.0)");
		check("I", "0.0+I*1.0");
		check("-42-I", "-42.0+I*(-1.0)");
		check("-42-i*1.0E10", "-42.0+I*(-1.0E10)");
		check("-42-I*1.0E+10", "-42.0+I*(-1.0E10)");
		check("-42-I*1.0E-10", "-42.0+I*(-1.0E-10)");
		check("42", "42.0");
		check("1.5", "1.5");
		check("-42", "-42.0");
		check("+42", "42.0");
		check("-42.1", "-42.1");
		check("+42.2", "42.2");
		check("-3/4", "-0.75");
		check("+3/4", "0.75");
		check("0^2", "0.0");
		check("0^0", "NaN+I*(NaN)");
		check("0^I", "0.0");
		check("i^0.5", "0.7071067811865476+I*0.7071067811865475");
		check("(-I)^(0.5)", "0.7071067811865476+I*(-0.7071067811865475)");
		check("(-1)^0.5", "6.123233995736766E-17+I*1.0");
		check("3^3", "27.0");
		check("2^9", "511.99999999999994");
		check("2^3^2", "512.0000000000005");
		check("(2^3)^2", "63.99999999999998");
		check("3+4*7", "31.0");
		check("3+4*7*3", "87.0");
		check("1+2+3+4*7*3", "90.0");
		// calculate in Radians
		check("sin(cos(3.2))", "-0.8405484252742996");
		check("SIN(cOs(3.2)*I)", "-0.0+I*(-1.172571602538769)");
		// Pi / 2
		check("90.0*Degree", "1.5707963267948966");
		check("Pi/2", "1.5707963267948966");
		check("3/2+Pi/2*I", "1.5+I*1.5707963267948966");
		check("Sin(Pi/2*Cos(pi))", "-1.0");

		// example
		// check("Random[]", "0.4045583086777451+I*0.7318437222786236");
	}

	public void testEval002() {
		try {
			ComplexEvaluator engine = new ComplexEvaluator(true);
			Complex c = engine.evaluate("Sin(pi/2*Cos(PI))");
			assertEquals(ComplexEvaluator.toString(c), "-1.0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval003() {
		try {
			ComplexVariable vc = new ComplexVariable(3.0);
			ComplexEvaluator engine = new ComplexEvaluator(true);
			engine.defineVariable("x", vc);
			Complex c = engine.evaluate("x^2+3*x*i");
			String result = ComplexEvaluator.toString(c);
			assertEquals(result, "9.000000000000002+I*9.0");
			vc.setValue(4);
			c = engine.evaluate();
			result = ComplexEvaluator.toString(c);
			assertEquals(result, "15.999999999999998+I*12.0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval006() {
		try {
			ComplexEvaluator engine = new ComplexEvaluator(true);
			BooleanVariable vb = new BooleanVariable(true);
			engine.defineVariable("$1", vb);
			BooleanVariable vb2 = new BooleanVariable(true);
			engine.defineVariable("$2", vb2);
			Complex cmp = engine.evaluate("if($1 && $2, 1, 0)");
			Assert.assertEquals(ComplexEvaluator.toString(cmp), "1.0");
			vb.setValue(false);
			cmp = engine.evaluate();
			Assert.assertEquals(ComplexEvaluator.toString(cmp), "0.0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	public void testEval007() {
		try {
			ComplexEvaluator engine = new ComplexEvaluator(true);

			ComplexVariable vc = new ComplexVariable(3.0);
			engine.defineVariable("$1", vc);
			ComplexVariable vc2 = new ComplexVariable(-4.0);
			engine.defineVariable("$2", vc2);
			Complex cmp = engine.evaluate("$i = $1+$2; IF($i==0, 1, -1)");
			Assert.assertEquals(ComplexEvaluator.toString(cmp), "-1.0");
			vc2.setValue(-3.0);
			cmp = engine.evaluate();
			Assert.assertEquals(ComplexEvaluator.toString(cmp), "1.0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	public void testEval010() {
		try {
			ComplexEvaluator engine = new ComplexEvaluator(true);
			engine.defineVariable("x", new ComplexVariable(Complex.I));
			Complex c = engine.evaluate("abs(x)");
			assertEquals(ComplexEvaluator.toString(c), "1.0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
}