/*
 * Copyright 2005-2009 Axel Kramer (axelclk@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matheclipse.parser.client.eval;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.Field;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.ast.FloatNode;
import org.matheclipse.parser.client.ast.FractionNode;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.ast.IntegerNode;
import org.matheclipse.parser.client.ast.NumberNode;
import org.matheclipse.parser.client.ast.PatternNode;
import org.matheclipse.parser.client.ast.StringNode;
import org.matheclipse.parser.client.ast.SymbolNode;
import org.matheclipse.parser.client.eval.api.FieldElementEvaluator;
import org.matheclipse.parser.client.eval.api.FieldElementVariable;
import org.matheclipse.parser.client.eval.api.IBooleanBoolean1Function;
import org.matheclipse.parser.client.eval.api.IBooleanBoolean2Function;
import org.matheclipse.parser.client.eval.api.IBooleanFieldElement2Function;
import org.matheclipse.parser.client.eval.api.IBooleanFunction;
import org.matheclipse.parser.client.eval.api.IFieldElement0Function;
import org.matheclipse.parser.client.eval.api.IFieldElement1Function;
import org.matheclipse.parser.client.eval.api.IFieldElement2Function;
import org.matheclipse.parser.client.eval.api.IFieldElementFunction;
import org.matheclipse.parser.client.eval.api.IFieldElementInt2Function;
import org.matheclipse.parser.client.eval.api.function.CompoundExpressionFunction;
import org.matheclipse.parser.client.eval.api.function.PlusFunction;
import org.matheclipse.parser.client.eval.api.function.SetFunction;
import org.matheclipse.parser.client.eval.api.function.TimesFunction;
import org.matheclipse.parser.client.math.ArithmeticMathException;

/**
 * Abstract AST visitor with empty default method implementation.
 * 
 * @param <T>
 */
public class ComplexEvaluator extends FieldElementEvaluator<Complex> {

	static class ArcTanFunction implements IFieldElement1Function<Complex> {
		@Override
		public Complex evaluate(Complex arg1) {
			return arg1.atan();
		}

	}

	static class LogFunction implements IFieldElement1Function<Complex>, IFieldElement2Function<Complex> {
		@Override
		public Complex evaluate(Complex arg1) {
			return arg1.log();
		}

		@Override
		public Complex evaluate(Complex base, Complex z) {
			return z.log().divide(base.log());
		}
	}

	private static Map<String, IBooleanFunction<Complex>> FUNCTION_BOOLEAN_MAP;

	private static Map<String, Complex> SYMBOL_MAP;

	private static Map<String, Boolean> SYMBOL_BOOLEAN_MAP;

	private static Map<String, IFieldElementFunction<Complex>> FUNCTION_MAP;

	static {
		FUNCTION_BOOLEAN_MAP = new ConcurrentHashMap<>();

		FUNCTION_BOOLEAN_MAP.put("And", new IBooleanBoolean2Function<Complex>() {
			@Override
			public boolean evaluate(boolean arg1, boolean arg2) {
				return arg1 && arg2;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Not", new IBooleanBoolean1Function<Complex>() {
			@Override
			public boolean evaluate(boolean arg1) {
				return !arg1;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Or", new IBooleanBoolean2Function<Complex>() {
			@Override
			public boolean evaluate(boolean arg1, boolean arg2) {
				return arg1 || arg2;
			}
		});

		FUNCTION_BOOLEAN_MAP.put("Equal", new IBooleanFieldElement2Function<Complex>() {
			@Override
			public boolean evaluate(Complex arg1, Complex arg2) {
				return arg1.equals(arg2);
			}
		});

		FUNCTION_BOOLEAN_MAP.put("Unequal", new IBooleanFieldElement2Function<Complex>() {
			@Override
			public boolean evaluate(Complex arg1, Complex arg2) {
				return !arg1.equals(arg2);
			}
		});

		SYMBOL_MAP = new ConcurrentHashMap<>();
		SYMBOL_MAP.put("Catalan", new Complex(0.91596559417721901505460351493238411077414937428167, 0.0));
		SYMBOL_MAP.put("Degree", new Complex(Math.PI / 180, 0.0));
		SYMBOL_MAP.put("E", new Complex(Math.E, 0.0));
		SYMBOL_MAP.put("I", new Complex(0.0, 1.0));
		SYMBOL_MAP.put("Pi", new Complex(Math.PI, 0.0));
		SYMBOL_MAP.put("EulerGamma", new Complex(0.57721566490153286060651209008240243104215933593992, 0.0));
		SYMBOL_MAP.put("Glaisher", new Complex(1.2824271291006226368753425688697917277676889273250, 0.0));
		SYMBOL_MAP.put("GoldenRatio", new Complex(1.6180339887498948482045868343656381177203091798058, 0.0));
		SYMBOL_MAP.put("Khinchin", new Complex(2.6854520010653064453097148354817956938203822939945, 0.0));

		SYMBOL_BOOLEAN_MAP = new ConcurrentHashMap<>();
		SYMBOL_BOOLEAN_MAP.put("False", Boolean.FALSE);
		SYMBOL_BOOLEAN_MAP.put("True", Boolean.TRUE);

		FUNCTION_MAP = new ConcurrentHashMap<>();
		FUNCTION_MAP.put("ArcTan", new ArcTanFunction());
		FUNCTION_MAP.put("Log", new LogFunction());
		FUNCTION_MAP.put("CompoundExpression", new CompoundExpressionFunction<Complex>());
		FUNCTION_MAP.put("Set", new SetFunction<Complex>());
		FUNCTION_MAP.put("Plus", new PlusFunction<Complex>());
		FUNCTION_MAP.put("Times", new TimesFunction<Complex>());
		//
		// Functions with 0 argument
		//
		FUNCTION_MAP.put("Random", new IFieldElement0Function<Complex>() {
			@Override
			public Complex evaluate() {
				return new Complex(Math.random(), Math.random());
			}
		});
		//
		// Functions with 1 argument
		//
		FUNCTION_MAP.put("Abs", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return new Complex(arg1.abs());
			}
		});
		FUNCTION_MAP.put("ArcCos", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.acos();
			}
		});
		FUNCTION_MAP.put("ArcSin", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.asin();
			}
		});

		FUNCTION_MAP.put("Cos", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.cos();
			}
		});
		FUNCTION_MAP.put("Cosh", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.cosh();
			}
		});
		FUNCTION_MAP.put("Exp", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.exp();
			}
		});
		FUNCTION_MAP.put("Sin", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.sin();
			}
		});
		FUNCTION_MAP.put("Sinh", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.sinh();
			}
		});
		FUNCTION_MAP.put("Sqrt", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.sqrt();
			}
		});
		FUNCTION_MAP.put("Tan", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.tan();
			}
		});
		FUNCTION_MAP.put("Tanh", new IFieldElement1Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1) {
				return arg1.tanh();
			}
		});

		//
		// Functions with 2 arguments
		//
		FUNCTION_MAP.put("Power", new IFieldElementInt2Function<Complex>() {
			@Override
			public Complex evaluate(Complex arg1, Complex arg2) {
				if (arg1.equals(Complex.ZERO) && !arg2.equals(Complex.ZERO)) {
					return Complex.ZERO;
				}
				return arg1.pow(arg2);
			}

			@Override
			public Complex evaluate(Complex arg1, int n) {
				if (n == 0 && arg1.equals(Complex.ZERO)) {
					return Complex.ZERO;
				}
				return arg1.pow(n);
			}
		});

	}

	/**
	 * Returns a <code>String</code> representation of the given
	 * <code>Complex</code> number.
	 * 
	 * @param c
	 * @return
	 * 
	 */
	public static String toString(Complex c) {
		double real = c.getReal();
		double imag = c.getImaginary();
		if (imag == 0.0) {
			return Double.toString(real);
		} else {
			if (imag >= 0.0) {
				return Double.toString(real) + "+I*" + Double.toString(imag);
			} else {
				return Double.toString(real) + "+I*(" + Double.toString(imag) + ")";
			}
		}
	}

	public ComplexEvaluator() {
		this(true);
	}

	public ComplexEvaluator(boolean relaxedSyntax) {
		super(relaxedSyntax);
		if (fRelaxedSyntax && SYMBOL_MAP.get("pi") == null) {
			// init tables for relaxed mode
			for (Map.Entry<String, Complex> entry : SYMBOL_MAP.entrySet()) {
				SYMBOL_MAP.put(entry.getKey().toLowerCase(), entry.getValue());
			}
			for (Map.Entry<String, Boolean> entry : SYMBOL_BOOLEAN_MAP.entrySet()) {
				SYMBOL_BOOLEAN_MAP.put(entry.getKey().toLowerCase(), entry.getValue());
			}
			for (Map.Entry<String, IFieldElementFunction<Complex>> entry : FUNCTION_MAP.entrySet()) {
				FUNCTION_MAP.put(entry.getKey().toLowerCase(), entry.getValue());
			}
			for (Map.Entry<String, IBooleanFunction<Complex>> entry : FUNCTION_BOOLEAN_MAP.entrySet()) {
				FUNCTION_BOOLEAN_MAP.put(entry.getKey().toLowerCase(), entry.getValue());
			}
		}
	}

	/**
	 * Clear all defined variables for this evaluator.
	 */
	@Override
	public void clearVariables() {
		fVariableMap.clear();
	}

	@Override
	public FieldElementVariable<Complex> createVariable(Complex value) {
		return new ComplexVariable(value);
	}

	/**
	 * Define a boolean value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	@Override
	public void defineVariable(String variableName, BooleanVariable value) {
		fBooleanVariables.put(variableName, value);
	}

	/**
	 * Define a value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	@Override
	public void defineVariable(String variableName, FieldElementVariable<Complex> value) {
		fVariableMap.put(variableName, value);
	}

	@Override
	public Field<Complex> getField() {
		return ComplexField.getInstance();
	}

	@Override
	public IBooleanFunction<Complex> getFunctionBooleanMap(String symbolName) {
		return FUNCTION_BOOLEAN_MAP.get(symbolName);
	}

	@Override
	public IFieldElementFunction<Complex> getFunctionMap(String symbolName) {
		return FUNCTION_MAP.get(symbolName);
	}

	@Override
	public Boolean getSymbolBooleanMap(String symbolName) {
		return SYMBOL_BOOLEAN_MAP.get(symbolName);
	}

	@Override
	public Complex getSymbolFieldElementMap(String symbolName) {
		return SYMBOL_MAP.get(symbolName);
	}

	/**
	 * Returns the Complex variable value to which the specified variableName is
	 * mapped, or {@code null} if this map contains no mapping for the
	 * variableName.
	 * 
	 * @param variableName
	 * @return
	 */
	@Override
	public FieldElementVariable<Complex> getVariable(String variableName) {
		return fVariableMap.get(variableName);
	}

	/**
	 * Optimize an already parsed in <code>functionNode</code> into an
	 * <code>ASTNode</code>.
	 * 
	 * @param functionNode
	 * @return the resulting ASTNode
	 * 
	 */
	@Override
	public ASTNode optimizeFunction(final FunctionNode functionNode) {
		if (!functionNode.isEmpty()) {
			boolean complexOnly = true;
			ASTNode node;
			for (int i = 1; i < functionNode.size(); i++) {
				node = functionNode.getNode(i);
				if (node instanceof NumberNode) {
					functionNode.set(i, new ComplexNode(((NumberNode) functionNode.getNode(i)).doubleValue()));
				} else if (functionNode.getNode(i) instanceof FunctionNode) {
					ASTNode optNode = optimizeFunction((FunctionNode) functionNode.getNode(i));
					if (!(optNode instanceof ComplexNode)) {
						complexOnly = false;
					}
					functionNode.set(i, optNode);
				} else if (node instanceof SymbolNode) {
					Complex c = SYMBOL_MAP.get(node.toString());
					if (c != null) {
						functionNode.set(i, new ComplexNode(c));
					} else {
						complexOnly = false;
					}
				} else {
					complexOnly = false;
				}
			}
			if (complexOnly) {
				try {
					return new ComplexNode(visit(functionNode));
				} catch (RuntimeException e) {

				}
			}
		}
		return functionNode;
	}

	@Override
	public void setUp(Complex data) {
		super.setUp(data);
	}

	@Override
	public void tearDown() {
		// no action necessary
	}

	@Override
	public Complex visit(ComplexNode node) {
		return node.complexValue();
	}

	@Override
	public Complex visit(DoubleNode node) {
		return new Complex(node.doubleValue(), 0.0);
	}

	@Override
	public Complex visit(FloatNode node) {
		return new Complex(node.doubleValue(), 0.0);
	}

	@Override
	public Complex visit(FractionNode node) {
		return new Complex(node.doubleValue(), 0.0);
	}

	@Override
	public Complex visit(IntegerNode node) {
		return new Complex(node.doubleValue(), 0.0);
	}

	@Override
	public Complex visit(PatternNode node) {
		return null;
	}

	@Override
	public Complex visit(StringNode node) {
		return null;
	}

	@Override
	public Complex visit(SymbolNode node) {
		FieldElementVariable<Complex> v = fVariableMap.get(node.toString());
		if (v != null) {
			return v.getValue();
		}
		Complex c = SYMBOL_MAP.get(node.toString());
		if (c != null) {
			return c;
		}
		throw new ArithmeticMathException("ComplexEvalVisitor#visit(SymbolNode) not possible for: " + node.toString());
	}
}
