# Apache commons-math parser

Implementation of math expressions parsers for Java double values or [Apache Commons Math](http://commons.apache.org/proper/commons-math/) Complex or Dfp numbers.

## Evaluate a math expresson string

Evaluate a math expression string to a Java double value, with a syntax which doesn't distinguish 
between upper- and lowercase function names:

```java
	public void testDoubleEvaluatorRelaxed() {
		try {
			DoubleEvaluator engine = new DoubleEvaluator(true);
			double d = engine.evaluate("sin(pi/2*cOs(pi))");
			assertEquals(Double.toString(d), "-1.0");
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
```

Evaluate a math expression string to a Java double value, with a syntax which distinguishes 
between upper- and lowercase function names (syntax is similar to Mathematica):

```java
	public void testDoubleEvaluatorMMA() {
		try {
			DoubleEvaluator engine = new DoubleEvaluator();
			double d = engine.evaluate("Sin[Pi/2*Cos[Pi]]");
			assertEquals(Double.toString(d), "-1.0");
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
```

Evaluate a math expression string to a [commons-math Complex](http://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/complex/Complex.html)
value, with a syntax which distinguishes between upper- and lowercase function names (syntax is similar to Mathematica):

```java
	public void testComplexEvaluatorMMA() {
		try {
			ComplexEvaluator engine = new ComplexEvaluator();
			Complex c = engine.evaluate("Sin[Pi/2*Cos[Pi]]");
			assertEquals(ComplexEvaluator.toString(c), "-1.0");
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
```

Evaluate a math expression string to a [commons-math Dfp](http://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/dfp/Dfp.html)
value, with a syntax which doesn't distinguish between upper- and lowercase function names:

```java
	public void testDfpEvaluatorRelaxed() {
		try {
			DfpEvaluator engine = new DfpEvaluator(50, true);
			Dfp d = engine.evaluate("sin(pi/2*cOs(pi))");
			assertEquals(d.toString(), "-1.");
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
```

## Define variables

An example for defining variables for assigning multiple values:

```java
	public void testDefineVariables() {
		try {
			IDoubleValue vd = new DoubleVariable(3.0);
			DoubleEvaluator engine = new DoubleEvaluator(true);
			engine.defineVariable("X", vd);
			double d = engine.evaluate("X^2+3");
			assertEquals(Double.valueOf(d).toString(), "12.0");
			vd.setValue(4);
			d = engine.evaluate();
			assertEquals(Double.valueOf(d).toString(), "19.0");
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
```

## License

Copyright © 2016 Axel Kramer

Distributed under the Apache License.
