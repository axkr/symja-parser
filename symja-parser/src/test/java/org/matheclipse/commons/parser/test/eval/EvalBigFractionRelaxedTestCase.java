package org.matheclipse.commons.parser.test.eval;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Assert;
import org.matheclipse.commons.parser.client.eval.BooleanVariable;
import org.matheclipse.commons.parser.client.eval.bigfraction.BigFractionEvaluator;
import org.matheclipse.commons.parser.client.eval.bigfraction.BigFractionVariable;

import junit.framework.TestCase;

/**
 * Tests evaluation in <code>BigFraction</code> expression mode
 * 
 * @see org.apache.commons.math3.fraction.BigFraction
 */
public class EvalBigFractionRelaxedTestCase extends TestCase {

	public EvalBigFractionRelaxedTestCase(String name) {
		super(name);
	}

	public void check(String in, String compareWith) {
		try {
			BigFractionEvaluator engine = new BigFractionEvaluator(true);
			BigFraction bf = engine.evaluate(in);
			String result = BigFractionEvaluator.toString(bf);
			assertEquals(result, compareWith);
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval001() {

		check("42", "42");
		check("1.5", "3 / 2");
		check("-42", "-42");
		check("+42", "42");
		check("-42.1", "-5925048259759309 / 140737488355328");
		check("+42.2", "2969561004297421 / 70368744177664");
		check("-3/4", "-3 / 4");
		check("+3/4", "3 / 4");
		check("0^2", "0");
		check("3^3", "27");
		check("2^9", "512");
		check("2^3^2", "512");
		check("(2^3)^2", "64");
		check("3+4*7", "31");
		check("3+4*7*3", "87");
		check("1+2+3+4*7*3", "90");
	}

	public void testEval003() {
		try {
			BigFractionVariable vc = new BigFractionVariable(new BigFraction(3));
			BigFractionEvaluator engine = new BigFractionEvaluator(true);
			engine.defineVariable("x", vc);
			BigFraction bf = engine.evaluate("x^2+3*x^3");
			String result = BigFractionEvaluator.toString(bf);
			assertEquals(result, "90");

			vc.setValue(new BigFraction(4));
			bf = engine.evaluate();
			result = BigFractionEvaluator.toString(bf);
			assertEquals(result, "208");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testEval006() {
		try {
			BigFractionEvaluator engine = new BigFractionEvaluator(true);
			BooleanVariable vb = new BooleanVariable(true);
			engine.defineVariable("$1", vb);
			BooleanVariable vb2 = new BooleanVariable(true);
			engine.defineVariable("$2", vb2);
			BigFraction bf = engine.evaluate("if($1 && $2, 1, 0)");
			Assert.assertEquals(BigFractionEvaluator.toString(bf), "1");
			vb.setValue(false);
			bf = engine.evaluate();
			Assert.assertEquals(BigFractionEvaluator.toString(bf), "0");
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	}

	public void testEval007() {
		try {
			
			BigFractionEvaluator engine = new BigFractionEvaluator(true);
			
			BigFractionVariable vc = new BigFractionVariable(new BigFraction(3));
			engine.defineVariable("$1", vc); 

			BigFractionVariable vc2 = new BigFractionVariable(new BigFraction(-4));
			engine.defineVariable("$2", vc2);
			BigFraction bf = engine.evaluate("$i = $1+$2; IF($i==0, 1, -1)");
			Assert.assertEquals(BigFractionEvaluator.toString(bf), "-1");
			vc2.setValue(-3);
			bf = engine.evaluate();
			Assert.assertEquals(BigFractionEvaluator.toString(bf), "1");
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertEquals("", e.getMessage());
		}
	} 
}