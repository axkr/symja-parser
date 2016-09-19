/*
 * Copyright 2005-2014 Axel Kramer (axelclk@gmail.com)
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
package org.matheclipse.commons.parser.client.eval.dfp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.Field;
import org.apache.commons.math3.dfp.Dfp;
import org.apache.commons.math3.dfp.DfpField;
import org.apache.commons.math3.dfp.DfpMath;
import org.matheclipse.commons.parser.client.Parser;
import org.matheclipse.commons.parser.client.SyntaxError;
import org.matheclipse.commons.parser.client.ast.ASTNode;
import org.matheclipse.commons.parser.client.ast.FloatNode;
import org.matheclipse.commons.parser.client.ast.FractionNode;
import org.matheclipse.commons.parser.client.ast.FunctionNode;
import org.matheclipse.commons.parser.client.ast.IntegerNode;
import org.matheclipse.commons.parser.client.ast.NumberNode;
import org.matheclipse.commons.parser.client.ast.PatternNode;
import org.matheclipse.commons.parser.client.ast.StringNode;
import org.matheclipse.commons.parser.client.ast.SymbolNode;
import org.matheclipse.commons.parser.client.eval.BooleanVariable;
import org.matheclipse.commons.parser.client.eval.ComplexNode;
import org.matheclipse.commons.parser.client.eval.DoubleNode;
import org.matheclipse.commons.parser.client.eval.api.FieldElementEvaluator;
import org.matheclipse.commons.parser.client.eval.api.FieldElementVariable;
import org.matheclipse.commons.parser.client.eval.api.IBooleanBoolean1Function;
import org.matheclipse.commons.parser.client.eval.api.IBooleanBoolean2Function;
import org.matheclipse.commons.parser.client.eval.api.IBooleanFieldElement2Function;
import org.matheclipse.commons.parser.client.eval.api.IBooleanFunction;
import org.matheclipse.commons.parser.client.eval.api.IEvaluator;
import org.matheclipse.commons.parser.client.eval.api.IFieldElement0Function;
import org.matheclipse.commons.parser.client.eval.api.IFieldElement1Function;
import org.matheclipse.commons.parser.client.eval.api.IFieldElement2Function;
import org.matheclipse.commons.parser.client.eval.api.IFieldElementFunction;
import org.matheclipse.commons.parser.client.eval.api.IFieldElementFunctionNode;
import org.matheclipse.commons.parser.client.eval.api.IFieldElementInt2Function;
import org.matheclipse.commons.parser.client.eval.api.function.CompoundExpressionFunction;
import org.matheclipse.commons.parser.client.eval.api.function.PlusFunction;
import org.matheclipse.commons.parser.client.eval.api.function.SetFunction;
import org.matheclipse.commons.parser.client.eval.api.function.TimesFunction;
import org.matheclipse.commons.parser.client.math.ArithmeticMathException;
import org.matheclipse.commons.parser.client.operator.ASTNodeFactory;

/**
 * Evaluate math expressions to <code>Dfp</code> numbers.
 * 
 * @see Dfp
 */
public class DfpEvaluator extends FieldElementEvaluator<Dfp> {

	static class ArcTanFunction implements IFieldElement1Function<Dfp>, IFieldElement2Function<Dfp> {
		@Override
		public Dfp evaluate(Dfp arg1) {
			return DfpMath.atan(arg1);
		}

		@Override
		public Dfp evaluate(Dfp arg1, Dfp arg2) {
			return arg1.atan2(arg2);
		}
	}

	static class LogFunction implements IFieldElement1Function<Dfp>, IFieldElement2Function<Dfp> {
		@Override
		public Dfp evaluate(Dfp arg1) {
			return DfpMath.log(arg1);
		}

		@Override
		public Dfp evaluate(Dfp base, Dfp z) {
			return DfpMath.log(z).divide(DfpMath.log(base));
		}
	}

	static class MaxFunction implements IFieldElementFunctionNode<Dfp>, IFieldElement2Function<Dfp> {
		@Override
		public Dfp evaluate(Dfp arg1, Dfp arg2) {
			return (arg1.greaterThan(arg2)) ? arg1 : arg2;
		}

		@Override
		public Dfp evaluate(IEvaluator<Dfp> engine, FunctionNode function) {
			Dfp result = null;
			int end = function.size();
			if (end > 1) {
				result = engine.evaluateNode(function.getNode(1));
				for (int i = 2; i < end; i++) {
					result = evaluate(result, engine.evaluateNode(function.getNode(i)));
				}
			}
			return result;
		}
	}

	static class MinFunction implements IFieldElementFunctionNode<Dfp>, IFieldElement2Function<Dfp> {
		@Override
		public Dfp evaluate(Dfp arg1, Dfp arg2) {
			return (arg1.lessThan(arg2)) ? arg1 : arg2;
		}

		@Override
		public Dfp evaluate(IEvaluator<Dfp> engine, FunctionNode function) {
			Dfp result = null;
			int end = function.size();
			if (end > 1) {
				result = engine.evaluateNode(function.getNode(1));
				for (int i = 2; i < end; i++) {
					result = evaluate(result, engine.evaluateNode(function.getNode(i)));
				}
			}
			return result;
		}
	}

	private static Map<String, Dfp> SYMBOL_MAP;

	private static Map<String, Boolean> SYMBOL_BOOLEAN_MAP;

	private static Map<String, IFieldElementFunction<Dfp>> FUNCTION_MAP;

	private static Map<String, IBooleanFunction<Dfp>> FUNCTION_BOOLEAN_MAP;

	static {
		// TODO: get better precision for constants
		SYMBOL_MAP = new ConcurrentHashMap<String, Dfp>();

		SYMBOL_BOOLEAN_MAP = new ConcurrentHashMap<String, Boolean>();
		SYMBOL_BOOLEAN_MAP.put("False", Boolean.FALSE);
		SYMBOL_BOOLEAN_MAP.put("True", Boolean.TRUE);

		FUNCTION_BOOLEAN_MAP = new ConcurrentHashMap<String, IBooleanFunction<Dfp>>();

		FUNCTION_BOOLEAN_MAP.put("And", new IBooleanBoolean2Function<Dfp>() {
			@Override
			public boolean evaluate(boolean arg1, boolean arg2) {
				return arg1 && arg2;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Not", new IBooleanBoolean1Function<Dfp>() {
			@Override
			public boolean evaluate(boolean arg1) {
				return !arg1;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Or", new IBooleanBoolean2Function<Dfp>() {
			@Override
			public boolean evaluate(boolean arg1, boolean arg2) {
				return arg1 || arg2;
			}
		});

		FUNCTION_BOOLEAN_MAP.put("Equal", new IBooleanFieldElement2Function<Dfp>() {
			@Override
			public boolean evaluate(Dfp arg1, Dfp arg2) {
				return arg1.equals(arg2);
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Greater", new IBooleanFieldElement2Function<Dfp>() {
			@Override
			public boolean evaluate(Dfp arg1, Dfp arg2) {
				return arg1.greaterThan(arg2);
			}
		});
		FUNCTION_BOOLEAN_MAP.put("GreaterEqual", new IBooleanFieldElement2Function<Dfp>() {
			@Override
			public boolean evaluate(Dfp arg1, Dfp arg2) {
				return !arg1.lessThan(arg2);
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Less", new IBooleanFieldElement2Function<Dfp>() {
			@Override
			public boolean evaluate(Dfp arg1, Dfp arg2) {
				return arg1.lessThan(arg2);
			}
		});
		FUNCTION_BOOLEAN_MAP.put("LessEqual", new IBooleanFieldElement2Function<Dfp>() {
			@Override
			public boolean evaluate(Dfp arg1, Dfp arg2) {
				return !arg1.greaterThan(arg2);
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Unequal", new IBooleanFieldElement2Function<Dfp>() {
			@Override
			public boolean evaluate(Dfp arg1, Dfp arg2) {
				return !arg1.equals(arg2);
			}
		});

		FUNCTION_MAP = new ConcurrentHashMap<String, IFieldElementFunction<Dfp>>();
		FUNCTION_MAP.put("ArcTan", new ArcTanFunction());
		FUNCTION_MAP.put("CompoundExpression", new CompoundExpressionFunction<Dfp>());
		FUNCTION_MAP.put("Set", new SetFunction<Dfp>());
		FUNCTION_MAP.put("Log", new LogFunction());
		FUNCTION_MAP.put("Max", new MaxFunction());
		FUNCTION_MAP.put("Min", new MinFunction());
		FUNCTION_MAP.put("Plus", new PlusFunction<Dfp>());
		FUNCTION_MAP.put("Times", new TimesFunction<Dfp>());
		//
		// Functions with 0 argument
		//
		// FUNCTION_DFP_MAP.put("Random", new IDfp0Function() {
		// public Dfp evaluate() {
		// return DfpMath.random();
		// }
		// });
		//
		// Functions with 1 argument
		//
		FUNCTION_MAP.put("ArcCos", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return DfpMath.acos(arg1);
			}
		});
		FUNCTION_MAP.put("ArcSin", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return DfpMath.asin(arg1);
			}
		});
		FUNCTION_MAP.put("Ceiling", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return arg1.ceil();
			}
		});
		FUNCTION_MAP.put("Cos", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return arg1.cos();
			}
		});
		FUNCTION_MAP.put("Cosh", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return arg1.cosh();
			}
		});
		FUNCTION_MAP.put("Exp", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return DfpMath.exp(arg1);
			}
		});
		FUNCTION_MAP.put("Floor", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return arg1.floor();
			}
		});
		// FUNCTION_DFP_MAP.put("Round", new IDfp1Function() {
		// public Dfp evaluate(Dfp arg1) {
		// return arg1.round();
		// }
		// });
		FUNCTION_MAP.put("Sign", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return arg1.signum();
			}
		});
		FUNCTION_MAP.put("Sin", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return DfpMath.sin(arg1);
			}
		});
		FUNCTION_MAP.put("Sinh", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return arg1.sinh();
			}
		});
		FUNCTION_MAP.put("Sqrt", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return arg1.sqrt();
			}
		});
		FUNCTION_MAP.put("Tan", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return DfpMath.tan(arg1);
			}
		});
		FUNCTION_MAP.put("Tanh", new IFieldElement1Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1) {
				return arg1.tanh();
			}
		});

		//
		// Functions with 2 arguments
		//
		FUNCTION_MAP.put("Power", new IFieldElementInt2Function<Dfp>() {
			@Override
			public Dfp evaluate(Dfp arg1, Dfp arg2) {
				return arg1.pow(arg2);
			}

			@Override
			public Dfp evaluate(Dfp arg1, int n) {
				return arg1.pow(n);
			}
		});

	}

	/**
	 * Parse the given <code>expression String</code> and return the resulting
	 * ASTNode
	 * 
	 * @param expression
	 * @return the resulting ASTNode
	 * @throws SyntaxError
	 */
	public static ASTNode parseNode(final int decimalDigits, String expression, boolean relaxedSyntax) {
		DfpEvaluator dfpEvaluator = new DfpEvaluator(decimalDigits, relaxedSyntax);
		return dfpEvaluator.parse(expression);
	}

	private IDfpCallbackFunction fCallbackFunction = null;

	private final DfpField fDfpField;

	private final DfpNode fZERO;

	private ASTNode fNode;

	private final ASTNodeFactory fASTFactory;

	public DfpEvaluator(final int decimalDigits) {
		this(decimalDigits, null, false);
	}

	public DfpEvaluator(final int decimalDigits, ASTNode node, boolean relaxedSyntax) {
		super(relaxedSyntax);
		fASTFactory = new ASTNodeFactory(relaxedSyntax);
		fVariableMap = new HashMap<String, FieldElementVariable<Dfp>>();
		fBooleanVariables = new HashMap<String, BooleanVariable>();
		fNode = node;
		fDfpField = new DfpField(decimalDigits);
		fZERO = new DfpNode(fDfpField.getZero());
		init();
		if (fRelaxedSyntax) {
			if (SYMBOL_MAP.get("pi") == null) {
				// init tables for relaxed mode
				for (String key : SYMBOL_MAP.keySet()) {
					SYMBOL_MAP.put(key.toLowerCase(), SYMBOL_MAP.get(key));
				}
				for (String key : SYMBOL_BOOLEAN_MAP.keySet()) {
					SYMBOL_BOOLEAN_MAP.put(key.toLowerCase(), SYMBOL_BOOLEAN_MAP.get(key));
				}
				for (String key : FUNCTION_MAP.keySet()) {
					FUNCTION_MAP.put(key.toLowerCase(), FUNCTION_MAP.get(key));
				}
				for (String key : FUNCTION_BOOLEAN_MAP.keySet()) {
					FUNCTION_BOOLEAN_MAP.put(key.toLowerCase(), FUNCTION_BOOLEAN_MAP.get(key));
				}
			}
		}
	}

	public DfpEvaluator(final int decimalDigits, boolean relaxedSyntax) {
		this(decimalDigits, null, relaxedSyntax);
	}

	/**
	 * Clear all defined variables for this evaluator.
	 */
	@Override
	public void clearVariables() {
		fVariableMap.clear();
		fBooleanVariables.clear();
	}

	@Override
	public FieldElementVariable<Dfp> createVariable(Dfp value) {
		return new DfpVariable(value);
	}

	/**
	 * Define a value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	public void defineVariable(String variableName) {
		if (fRelaxedSyntax) {
			fVariableMap.put(variableName.toLowerCase(), new DfpVariable(fDfpField.getZero()));
		} else {
			fVariableMap.put(variableName, new DfpVariable(fDfpField.getZero()));
		}
	}

	/**
	 * Define a boolean value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	@Override
	public void defineVariable(String variableName, BooleanVariable value) {
		if (fRelaxedSyntax) {
			fBooleanVariables.put(variableName.toLowerCase(), value);
		} else {
			fBooleanVariables.put(variableName, value);
		}

	}

	/**
	 * Define a value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	public void defineVariable(String variableName, Dfp value) {
		if (fRelaxedSyntax) {
			fVariableMap.put(variableName.toLowerCase(), new DfpVariable(value));
		} else {
			fVariableMap.put(variableName, new DfpVariable(value));
		}
	}

	public FieldElementVariable<Dfp> defineVariable(String variableName, double value) {
		FieldElementVariable<Dfp> val = new DfpVariable(fDfpField.newDfp(value));
		if (fRelaxedSyntax) {
			fVariableMap.put(variableName.toLowerCase(), val);
		} else {
			fVariableMap.put(variableName, val);
		}
		return val;
	}

	/**
	 * Define a value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	@Override
	public void defineVariable(String variableName, FieldElementVariable<Dfp> value) {
		if (fRelaxedSyntax) {
			fVariableMap.put(variableName.toLowerCase(), value);
		} else {
			fVariableMap.put(variableName, value);
		}
	}

	/**
	 * 
	 * @param node
	 * @param var
	 * @return
	 */
	public ASTNode derivative(final ASTNode node, String var) {
		SymbolNode sym = fASTFactory.createSymbol(var);
		return derivative(node, sym);
	}

	/**
	 * 
	 * TODO: add more derivation rules
	 * 
	 * @param node
	 * @param var
	 * @return
	 */
	public ASTNode derivative(final ASTNode node, SymbolNode var) {
		if (node.isFree(var)) {
			return new DfpNode(fDfpField.getZero());
		}
		if (node instanceof FunctionNode) {
			FunctionNode f = (FunctionNode) node;
			if (f.size() > 1 && f.getNode(0) instanceof SymbolNode) {
				SymbolNode head = (SymbolNode) f.getNode(0);
				if (f.size() == 2) {
					ASTNode arg1Derived = derivative(f.getNode(1), var);
					if (isSymbol(head, "Exp")) {
						FunctionNode fun = new FunctionNode(fASTFactory.createSymbol("Exp"));
						fun.add(f.getNode(1));
						return getDerivativeResult(arg1Derived, fun);
					}
					if (isSymbol(head, "Cos")) {
						FunctionNode fun = new FunctionNode(fASTFactory.createSymbol("Times"));
						fun.add(new DfpNode(fDfpField.newDfp(-1)));
						fun.add(new FunctionNode(fASTFactory.createSymbol("Cos"), f.getNode(1)));
						return getDerivativeResult(arg1Derived, fun);
					}
					if (isSymbol(head, "Sin")) {
						FunctionNode fun = new FunctionNode(fASTFactory.createSymbol("Cos"));
						fun.add(f.getNode(1));
						return getDerivativeResult(arg1Derived, fun);
					}
				} else if (f.size() == 3 && isSymbol(head, "Power")) {
					if (f.get(2).isFree(var)) {// derive x^r
						ASTNode arg1Derived = derivative(f.getNode(1), var);
						// (r-1)
						FunctionNode exponent = fASTFactory.createFunction(fASTFactory.createSymbol("Plus"),
								new DfpNode(fDfpField.newDfp(-1)), f.get(2));
						// r*x^(r-1)
						FunctionNode fun = fASTFactory.createFunction(fASTFactory.createSymbol("Times"), f.get(2),
								fASTFactory.createFunction(fASTFactory.createSymbol("Power"), f.get(1), exponent));
						return getDerivativeResult(arg1Derived, fun);
					}
					if (f.get(1).isFree(var)) {// derive a^x
						ASTNode arg2Derived = derivative(f.getNode(2), var);
						// log(a) * a^x
						FunctionNode fun = fASTFactory.createFunction(fASTFactory.createSymbol("Times"),
								fASTFactory.createFunction(fASTFactory.createSymbol("Log"), f.get(1)), f);
						return getDerivativeResult(arg2Derived, fun);
					}
				} else {
					if (isSymbol(head, "Plus")) {
						FunctionNode result = new FunctionNode(f.getNode(0));
						for (int i = 1; i < f.size(); i++) {
							ASTNode deriv = derivative(f.getNode(i), var);
							if (!deriv.equals(fZERO)) {
								result.add(deriv);
							}
						}
						return result;
					}
					if (isSymbol(head, "Times")) {
						FunctionNode plusResult = new FunctionNode(fASTFactory.createSymbol("Plus"));
						for (int i = 1; i < f.size(); i++) {
							FunctionNode timesResult = new FunctionNode(f.getNode(0));
							boolean valid = true;
							for (int j = 1; j < f.size(); j++) {
								if (j == i) {
									ASTNode deriv = derivative(f.getNode(j), var);
									if (deriv.equals(fZERO)) {
										valid = false;
									} else {
										timesResult.add(deriv);
									}
								} else {
									timesResult.add(f.getNode(j));
								}
							}
							if (valid) {
								plusResult.add(timesResult);
							}
						}
						return plusResult;
					}
				}
			}
			return new FunctionNode(new SymbolNode("D"), node, var);
			// return evaluateFunction((FunctionNode) node);
		}
		if (node instanceof SymbolNode) {
			if (isSymbol((SymbolNode) node, var)) {
				return new DfpNode(fDfpField.getOne());
			}
			FieldElementVariable<Dfp> v = fVariableMap.get(node.toString());
			if (v != null) {
				return new DfpNode(fDfpField.getZero());
			}
			Dfp dbl = SYMBOL_MAP.get(node.toString());
			if (dbl != null) {
				return new DfpNode(fDfpField.getZero());
			}
			return new DfpNode(fDfpField.getZero());
		} else if (node instanceof NumberNode) {
			return new DfpNode(fDfpField.getZero());
		}

		throw new ArithmeticMathException(
				"DfpEvaluator#derivative(ASTNode, SymbolNode) not possible for: " + node.toString());
	}

	/**
	 * Reevaluate the <code>expression</code> (possibly after a new Variable
	 * assignment)
	 * 
	 * @param Expression
	 * @return
	 * @throws SyntaxError
	 */
	public Dfp evaluate() {
		if (fNode == null) {
			throw new SyntaxError(0, 0, 0, " ", "No parser input defined", 1);
		}
		return evaluateNode(fNode);
	}

	/**
	 * Parse the given <code>expression String</code> and evaluate it to a Dfp
	 * value
	 * 
	 * @param expression
	 * @return
	 * @throws SyntaxError
	 */
	public Dfp evaluate(String expression) {
		Parser p;
		if (fRelaxedSyntax) {
			p = new Parser(ASTNodeFactory.RELAXED_STYLE_FACTORY, true);
		} else {
			p = new Parser(ASTNodeFactory.MMA_STYLE_FACTORY, false);
		}
		fNode = p.parse(expression);
		if (fNode instanceof FunctionNode) {
			fNode = optimizeFunction((FunctionNode) fNode);
		}
		return evaluateNode(fNode);
	}

	/**
	 * Evaluate an already parsed in <code>FunctionNode</code> into a
	 * <code>souble</code> number value.
	 * 
	 * @param functionNode
	 * @return
	 * 
	 * @throws ArithmeticMathException
	 *             if the <code>functionNode</code> cannot be evaluated.
	 */
	public Dfp evaluateFunction(final FunctionNode functionNode) {
		if (functionNode.size() > 0 && functionNode.getNode(0) instanceof SymbolNode) {
			String symbol = functionNode.getNode(0).toString();
			if (symbol.equals("If") || (fRelaxedSyntax && symbol.equalsIgnoreCase("if"))) {
				if (functionNode.size() == 3) {
					if (evaluateNodeLogical(functionNode.getNode(1))) {
						return evaluateNode(functionNode.getNode(2));
					}
				} else if (functionNode.size() == 4) {
					if (evaluateNodeLogical(functionNode.getNode(1))) {
						return evaluateNode(functionNode.getNode(2));
					} else {
						return evaluateNode(functionNode.getNode(3));
					}
				}
			} else {
				IFieldElementFunction<Dfp> function = FUNCTION_MAP.get(symbol);
				if (function instanceof IFieldElementFunctionNode) {
					return ((IFieldElementFunctionNode<Dfp>) function).evaluate(this, functionNode);
				}
				if (functionNode.size() == 1) {
					if (function instanceof IFieldElement0Function) {
						return ((IFieldElement0Function<Dfp>) function).evaluate();
					}
				} else if (functionNode.size() == 2) {
					if (function instanceof IFieldElement1Function) {
						return ((IFieldElement1Function<Dfp>) function).evaluate(evaluateNode(functionNode.getNode(1)));
					}
				} else if (functionNode.size() == 3) {
					ASTNode arg2 = functionNode.getNode(2);
					if (function instanceof IFieldElementInt2Function && arg2 instanceof IntegerNode) {
						return ((IFieldElementInt2Function<Dfp>) function)
								.evaluate(evaluateNode(functionNode.getNode(1)), ((IntegerNode) arg2).getIntValue());
					}
					if (function instanceof IFieldElement2Function) {
						return ((IFieldElement2Function<Dfp>) function).evaluate(evaluateNode(functionNode.getNode(1)),
								evaluateNode(arg2));
					}
				}
				if (fCallbackFunction != null) {
					Dfp dfpArgs[] = new Dfp[functionNode.size() - 1];
					for (int i = 0; i < dfpArgs.length; i++) {
						dfpArgs[i] = evaluateNode(functionNode.getNode(i + 1));
					}
					return fCallbackFunction.evaluate(this, functionNode, dfpArgs);
				}
			}
		}
		throw new ArithmeticMathException(
				"DfpEvaluator#evaluateFunction(FunctionNode) not possible for: " + functionNode.toString());
	}

	@Override
	public boolean evaluateFunctionLogical(final FunctionNode functionNode) {
		if (functionNode.size() > 0 && functionNode.getNode(0) instanceof SymbolNode) {
			String symbol = functionNode.getNode(0).toString();
			if (functionNode.size() == 2) {
				IBooleanFunction<Dfp> function = FUNCTION_BOOLEAN_MAP.get(symbol);
				if (function instanceof IBooleanBoolean1Function) {
					return ((IBooleanBoolean1Function<Dfp>) function)
							.evaluate(evaluateNodeLogical(functionNode.getNode(1)));
				}
			} else if (functionNode.size() == 3) {
				IBooleanFunction<Dfp> function = FUNCTION_BOOLEAN_MAP.get(symbol);
				if (function instanceof IBooleanFieldElement2Function) {
					return ((IBooleanFieldElement2Function<Dfp>) function)
							.evaluate(evaluateNode(functionNode.getNode(1)), evaluateNode(functionNode.getNode(2)));
				} else if (function instanceof IBooleanBoolean2Function) {
					return ((IBooleanBoolean2Function<Dfp>) function).evaluate(
							evaluateNodeLogical(functionNode.getNode(1)), evaluateNodeLogical(functionNode.getNode(2)));
				}
				// } else {
				// Object obj = FUNCTION_BOOLEAN_MAP.get(symbol);
				// if (obj instanceof IBooleanDfpFunction) {
				// return ((IBooleanDfpFunction) obj).evaluate(this,
				// functionNode);
				// }
			}
		}
		throw new ArithmeticMathException(
				"DfpEvaluator#evaluateFunctionLogical(FunctionNode) not possible for: " + functionNode.toString());

	}

	/**
	 * Evaluate an already parsed in abstract syntax tree node into a
	 * <code>Dfp</code> number value.
	 * 
	 * @param node
	 *            abstract syntax tree node
	 * 
	 * @return the evaluated Dfp number
	 * 
	 * @throws ArithmeticMathException
	 *             if the <code>node</code> cannot be evaluated.
	 */
	@Override
	public Dfp evaluateNode(final ASTNode node) {
		if (node instanceof DfpNode) {
			return fDfpField.newDfp(((DfpNode) node).getDfpValue());
		}
		if (node instanceof FunctionNode) {
			return evaluateFunction((FunctionNode) node);
		}
		if (node instanceof SymbolNode) {
			FieldElementVariable<Dfp> v = fVariableMap.get(node.toString());
			if (v != null) {
				return v.getValue();
			}
			Dfp dbl = SYMBOL_MAP.get(node.toString());
			if (dbl != null) {
				return dbl;
			}
		} else if (node instanceof NumberNode) {
			if (node instanceof FractionNode) {
				return fDfpField.newDfp(((FractionNode) node).getNumerator().toString())
						.divide(fDfpField.newDfp(((FractionNode) node).getDenominator().toString()));
			} else if (node instanceof IntegerNode) {
				String iStr = ((NumberNode) node).getString();
				if (iStr != null) {
					return fDfpField.newDfp(iStr);
				} else {
					return fDfpField.newDfp(((IntegerNode) node).getIntValue());
				}
			}

			return fDfpField.newDfp(((NumberNode) node).getString());
		}

		throw new ArithmeticMathException("DfpEvaluator#evaluateNode(ASTNode) not possible for: " + node.toString());
	}

	@Override
	public boolean evaluateNodeLogical(final ASTNode node) {
		if (node instanceof FunctionNode) {
			return evaluateFunctionLogical((FunctionNode) node);
		}
		if (node instanceof SymbolNode) {
			BooleanVariable v = fBooleanVariables.get(node.toString());
			if (v != null) {
				return v.getValue();
			}
			Boolean boole = SYMBOL_BOOLEAN_MAP.get(node.toString());
			if (boole != null) {
				return boole.booleanValue();
			}
		}

		throw new ArithmeticMathException(
				"DfpEvaluator#evaluateNodeLogical(ASTNode) not possible for: " + node.toString());
	}

	/**
	 * A callback function for unknown function names.
	 * 
	 * @return the callback function
	 */
	public IDfpCallbackFunction getCallbackFunction() {
		return fCallbackFunction;
	}

	private ASTNode getDerivativeResult(ASTNode arg1Derived, FunctionNode fun) {
		if (!arg1Derived.equals(new DfpNode(fDfpField.getOne()))) {
			FunctionNode res = new FunctionNode(fASTFactory.createSymbol("Times"));
			res.add(arg1Derived);
			res.add(fun);
			return res;
		}
		return fun;
	}

	@Override
	public Field<Dfp> getField() {
		return fDfpField;
	}

	@Override
	public IBooleanFunction<Dfp> getFunctionBooleanMap(String symbolName) {
		return FUNCTION_BOOLEAN_MAP.get(symbolName);
	}

	@Override
	public IFieldElementFunction<Dfp> getFunctionMap(String symbolName) {
		return FUNCTION_MAP.get(symbolName);
	}

	@Override
	public Boolean getSymbolBooleanMap(String symbolName) {
		return SYMBOL_BOOLEAN_MAP.get(symbolName);
	}

	@Override
	public Dfp getSymbolFieldElementMap(String symbolName) {
		return SYMBOL_MAP.get(symbolName);
	}

	/**
	 * Returns the Dfp variable value to which the specified variableName is
	 * mapped, or {@code null} if this map contains no mapping for the
	 * variableName.
	 * 
	 * @param variableName
	 * @return
	 */
	@Override
	public FieldElementVariable<Dfp> getVariable(String variableName) {
		if (fRelaxedSyntax) {
			return fVariableMap.get(variableName.toLowerCase());
		} else {
			return fVariableMap.get(variableName);
		}
	}

	/**
	 * Get the variable names from the given AST node.
	 * 
	 * @param node
	 *            an already parsed AST node
	 * @param result
	 *            a set which contains the variable names
	 */
	public void getVariables(final ASTNode node, Set<String> result) {
		if (node instanceof FunctionNode) {
			FunctionNode functionNode = (FunctionNode) node;
			if (functionNode.size() > 0 && functionNode.getNode(0) instanceof SymbolNode) {
				for (int i = 1; i < functionNode.size(); i++) {
					getVariables(functionNode.getNode(i), result);
				}
			}
		}
		if (node instanceof SymbolNode) {
			Object obj = SYMBOL_MAP.get(node.toString());
			if (obj == null) {
				obj = SYMBOL_BOOLEAN_MAP.get(node.toString());
				if (obj == null) {
					result.add(node.toString());
				}
			}

		}
	}

	/**
	 * Get the variable names from the given expression.
	 * 
	 * @param expression
	 * @param result
	 *            a set which contains the variable names
	 */
	public void getVariables(String expression, Set<String> result) {
		getVariables(expression, result, true);
	}

	/**
	 * Get the variable names from the given expression.
	 * 
	 * @param expression
	 * @param result
	 *            a set which contains the variable names
	 * @param relaxedSyntax
	 *            if <code>true</code> us e function syntax like
	 *            <code>sin(x)</code> otherwise use <code>Sin[x]</code>.
	 */
	public void getVariables(String expression, Set<String> result, boolean relaxedSyntax) {
		Parser p = new Parser(relaxedSyntax ? ASTNodeFactory.RELAXED_STYLE_FACTORY : ASTNodeFactory.MMA_STYLE_FACTORY,
				relaxedSyntax);
		ASTNode node = p.parse(expression);
		getVariables(node, result);
	}

	void init() {
		// TODO: get better precision for constants
		SYMBOL_MAP.put("Catalan", fDfpField.newDfp(0.91596559417721901505460351493238411077414937428167));
		SYMBOL_MAP.put("Degree", fDfpField.newDfp(Math.PI / 180));
		SYMBOL_MAP.put("E", fDfpField.getE());
		SYMBOL_MAP.put("Pi", fDfpField.getPi());
		SYMBOL_MAP.put("EulerGamma", fDfpField.newDfp(0.57721566490153286060651209008240243104215933593992));
		SYMBOL_MAP.put("Glaisher", fDfpField.newDfp(1.2824271291006226368753425688697917277676889273250));
		SYMBOL_MAP.put("GoldenRatio", fDfpField.newDfp(1.6180339887498948482045868343656381177203091798058));
		SYMBOL_MAP.put("Khinchin", fDfpField.newDfp(2.6854520010653064453097148354817956938203822939945));

	}

	/**
	 * Check if the given symbol is a <code>SymbolNode</code> and test if the
	 * names are equal.
	 * 
	 * @param symbol1
	 * @param symbol2Name
	 * @return
	 */
	public boolean isSymbol(SymbolNode symbol1, String symbol2Name) {
		if (fRelaxedSyntax) {
			return symbol1.getString().equalsIgnoreCase(symbol2Name);
		}
		return symbol1.getString().equals(symbol2Name);
	}

	/**
	 * Check if the given symbol is a <code>SymbolNode</code> and test if the
	 * names are equal.
	 * 
	 * @param symbol1
	 * @param symbol2
	 * @return
	 */
	public boolean isSymbol(SymbolNode symbol1, SymbolNode symbol2) {
		if (fRelaxedSyntax) {
			return symbol1.getString().equalsIgnoreCase(symbol2.getString());
		}
		return symbol1.equals(symbol2);
	}

	/**
	 * Optimize an already parsed in <code>functionNode</code> into an
	 * <code>ASTNode</code>.
	 * 
	 * @param functionNode
	 * @return
	 * 
	 */
	@Override
	public ASTNode optimizeFunction(final FunctionNode functionNode) {
		if (functionNode.size() > 0) {
			boolean dfpOnly = true;
			ASTNode node;
			for (int i = 1; i < functionNode.size(); i++) {
				node = functionNode.getNode(i);
				if (node instanceof NumberNode) {
					if (node instanceof FractionNode) {
						functionNode.set(i,
								new DfpNode(fDfpField.newDfp(((FractionNode) node).getNumerator().toString())
										.divide(fDfpField.newDfp(((FractionNode) node).getDenominator().toString()))));
					} else if (node instanceof IntegerNode) {
						String iStr = ((NumberNode) functionNode.getNode(i)).getString();
						if (iStr != null) {
							functionNode.set(i, new DfpNode(fDfpField.newDfp(iStr)));
						} else {
							functionNode.set(i, new DfpNode(
									fDfpField.newDfp(((IntegerNode) functionNode.getNode(i)).getIntValue())));
						}
					} else {
						functionNode.set(i,
								new DfpNode(fDfpField.newDfp(((NumberNode) functionNode.getNode(i)).getString())));
					}
				} else if (functionNode.getNode(i) instanceof FunctionNode) {
					ASTNode optNode = optimizeFunction((FunctionNode) functionNode.getNode(i));
					if (!(optNode instanceof DfpNode)) {
						dfpOnly = false;
					}
					functionNode.set(i, optNode);
				} else if (node instanceof SymbolNode) {
					Dfp dbl = SYMBOL_MAP.get(node.toString());
					if (dbl != null) {
						functionNode.set(i, new DfpNode(dbl));
					} else {
						dfpOnly = false;
					}
				} else {
					dfpOnly = false;
				}
			}
			if (dfpOnly) {
				try {
					return new DfpNode(evaluateFunction(functionNode));
				} catch (RuntimeException e) {

				}
			}
		}
		return functionNode;
	}

	/**
	 * Parse the given <code>expression String</code> and store the resulting
	 * ASTNode in this DfpEvaluator
	 * 
	 * @param expression
	 * @return
	 * @throws SyntaxError
	 */
	public ASTNode parse(String expression) {
		Parser p;
		if (fRelaxedSyntax) {
			p = new Parser(ASTNodeFactory.RELAXED_STYLE_FACTORY, true);
		} else {
			p = new Parser(ASTNodeFactory.MMA_STYLE_FACTORY, false);
		}
		fNode = p.parse(expression);
		if (fNode instanceof FunctionNode) {
			fNode = optimizeFunction((FunctionNode) fNode);
		}
		return fNode;
	}

	/**
	 * Set a callback function for unknown function names.
	 * 
	 * @param callbackFunction
	 *            the callback function to set
	 */
	public void setCallbackFunction(IDfpCallbackFunction callbackFunction) {
		fCallbackFunction = callbackFunction;
	}

	@Override
	public void setUp(Dfp data) {
		super.setUp(data);
	}

	public void setValue(FieldElementVariable<Dfp> variable, double value) {
		variable.setValue(fDfpField.newDfp(value));
	}

	@Override
	public void tearDown() {
	}

	@Override
	public Dfp visit(ComplexNode node) {
		return null;
	}

	@Override
	public Dfp visit(DoubleNode node) {
		return fDfpField.newDfp(node.getString());
	}

	@Override
	public Dfp visit(FloatNode node) {
		return fDfpField.newDfp(node.getString());
	}

	@Override
	public Dfp visit(FractionNode node) {
		return fDfpField.newDfp(node.doubleValue());
	}

	@Override
	public Dfp visit(IntegerNode node) {
		return fDfpField.newDfp(node.getIntValue());
	}

	@Override
	public Dfp visit(PatternNode node) {
		return null;
	}

	@Override
	public Dfp visit(StringNode node) {
		return null;
	}

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

}
