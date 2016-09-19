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
package org.matheclipse.commons.parser.client.eval.bigfraction;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.Field;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.BigFractionField;
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
 * Evaluate math expressions to <code>BigFraction</code> numbers.
 * 
 * @see org.apache.commons.math3.fraction.BigFraction
 */
public class BigFractionEvaluator extends FieldElementEvaluator<BigFraction> {

	static class MaxFunction implements IFieldElementFunctionNode<BigFraction>, IFieldElement2Function<BigFraction> {
		@Override
		public BigFraction evaluate(BigFraction arg1, BigFraction arg2) {
			return (arg1.compareTo(arg2)) > 0 ? arg1 : arg2;
		}

		@Override
		public BigFraction evaluate(IEvaluator<BigFraction> engine, FunctionNode function) {
			BigFraction result = null;
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

	static class MinFunction implements IFieldElementFunctionNode<BigFraction>, IFieldElement2Function<BigFraction> {
		@Override
		public BigFraction evaluate(BigFraction arg1, BigFraction arg2) {
			return (arg1.compareTo(arg2)) < 0 ? arg1 : arg2;
		}

		@Override
		public BigFraction evaluate(IEvaluator<BigFraction> engine, FunctionNode function) {
			BigFraction result = null;
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

	private static Map<String, BigFraction> SYMBOL_MAP;

	private static Map<String, Boolean> SYMBOL_BOOLEAN_MAP;

	private static Map<String, IFieldElementFunction<BigFraction>> FUNCTION_MAP;

	private static Map<String, IBooleanFunction<BigFraction>> FUNCTION_BOOLEAN_MAP;

	static {
		// TODO: get better precision for constants
		SYMBOL_MAP = new ConcurrentHashMap<String, BigFraction>();

		SYMBOL_BOOLEAN_MAP = new ConcurrentHashMap<String, Boolean>();
		SYMBOL_BOOLEAN_MAP.put("False", Boolean.FALSE);
		SYMBOL_BOOLEAN_MAP.put("True", Boolean.TRUE);

		FUNCTION_BOOLEAN_MAP = new ConcurrentHashMap<String, IBooleanFunction<BigFraction>>();

		FUNCTION_BOOLEAN_MAP.put("And", new IBooleanBoolean2Function<BigFraction>() {
			@Override
			public boolean evaluate(boolean arg1, boolean arg2) {
				return arg1 && arg2;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Not", new IBooleanBoolean1Function<BigFraction>() {
			@Override
			public boolean evaluate(boolean arg1) {
				return !arg1;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Or", new IBooleanBoolean2Function<BigFraction>() {
			@Override
			public boolean evaluate(boolean arg1, boolean arg2) {
				return arg1 || arg2;
			}
		});

		FUNCTION_BOOLEAN_MAP.put("Equal", new IBooleanFieldElement2Function<BigFraction>() {
			@Override
			public boolean evaluate(BigFraction arg1, BigFraction arg2) {
				return arg1.equals(arg2);
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Greater", new IBooleanFieldElement2Function<BigFraction>() {
			@Override
			public boolean evaluate(BigFraction arg1, BigFraction arg2) {
				return (arg1.compareTo(arg2)) > 0;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("GreaterEqual", new IBooleanFieldElement2Function<BigFraction>() {
			@Override
			public boolean evaluate(BigFraction arg1, BigFraction arg2) {
				return (arg1.compareTo(arg2)) >= 0;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Less", new IBooleanFieldElement2Function<BigFraction>() {
			@Override
			public boolean evaluate(BigFraction arg1, BigFraction arg2) {
				return (arg1.compareTo(arg2)) < 0;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("LessEqual", new IBooleanFieldElement2Function<BigFraction>() {
			@Override
			public boolean evaluate(BigFraction arg1, BigFraction arg2) {
				return (arg1.compareTo(arg2)) <= 0;
			}
		});
		FUNCTION_BOOLEAN_MAP.put("Unequal", new IBooleanFieldElement2Function<BigFraction>() {
			@Override
			public boolean evaluate(BigFraction arg1, BigFraction arg2) {
				return !arg1.equals(arg2);
			}
		});

		FUNCTION_MAP = new ConcurrentHashMap<String, IFieldElementFunction<BigFraction>>();
		FUNCTION_MAP.put("CompoundExpression", new CompoundExpressionFunction<BigFraction>());
		FUNCTION_MAP.put("Set", new SetFunction<BigFraction>());
		FUNCTION_MAP.put("Max", new MaxFunction());
		FUNCTION_MAP.put("Min", new MinFunction());
		FUNCTION_MAP.put("Plus", new PlusFunction<BigFraction>());
		FUNCTION_MAP.put("Times", new TimesFunction<BigFraction>());
		//
		// Functions with 0 argument
		//
		// FUNCTION_DFP_MAP.put("Random", new IBigFraction0Function() {
		// public BigFraction evaluate() {
		// return BigFractionMath.random();
		// }
		// });
		//
		// Functions with 1 argument
		//

		// FUNCTION_MAP.put("Ceiling", new IFieldElement1Function<BigFraction>()
		// {
		// @Override
		// public BigFraction evaluate(BigFraction arg1) {
		// return arg1.ceil();
		// }
		// });
		// FUNCTION_MAP.put("Floor", new IFieldElement1Function<BigFraction>() {
		// @Override
		// public BigFraction evaluate(BigFraction arg1) {
		// return arg1.floor();
		// }
		// });
		FUNCTION_MAP.put("Sign", new IFieldElement1Function<BigFraction>() {
			@Override
			public BigFraction evaluate(BigFraction arg1) {
				return (arg1.equals(BigFraction.ZERO)) ? BigFraction.ZERO
						: (arg1.compareTo(BigFraction.ZERO) > 0) ? BigFraction.ONE : BigFraction.MINUS_ONE;
			}
		});

		//
		// Functions with 2 arguments
		//
		FUNCTION_MAP.put("Power", new IFieldElementInt2Function<BigFraction>() {
			@Override
			public BigFraction evaluate(BigFraction arg1, BigFraction arg2) {
				if (arg2.getDenominator().equals(BigInteger.ONE)) {
					return arg1.pow(arg2.getNumerator());
				}
				throw new ArithmeticMathException(
						"Power#evaluate(BigFraction, BigFraction) not possible for argument 2: " + arg2.toString());
			}

			@Override
			public BigFraction evaluate(BigFraction arg1, int n) {
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
		BigFractionEvaluator dfpEvaluator = new BigFractionEvaluator(relaxedSyntax);
		return dfpEvaluator.parse(expression);
	}

	private final BigFractionField fDfpField;

	private final BigFractionNode fZERO;

	private ASTNode fNode;

	private final ASTNodeFactory fASTFactory;

	public BigFractionEvaluator() {
		this(null, false);
	}

	public BigFractionEvaluator(ASTNode node, boolean relaxedSyntax) {
		super(relaxedSyntax);
		fASTFactory = new ASTNodeFactory(relaxedSyntax);
		fVariableMap = new HashMap<String, FieldElementVariable<BigFraction>>();
		fBooleanVariables = new HashMap<String, BooleanVariable>();
		fNode = node;
		fDfpField = BigFractionField.getInstance();
		fZERO = new BigFractionNode(fDfpField.getZero());
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

	public BigFractionEvaluator(boolean relaxedSyntax) {
		this(null, relaxedSyntax);
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
	public FieldElementVariable<BigFraction> createVariable(BigFraction value) {
		return new BigFractionVariable(value);
	}

	/**
	 * Define a value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	public void defineVariable(String variableName) {
		if (fRelaxedSyntax) {
			fVariableMap.put(variableName.toLowerCase(), new BigFractionVariable(fDfpField.getZero()));
		} else {
			fVariableMap.put(variableName, new BigFractionVariable(fDfpField.getZero()));
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
	public void defineVariable(String variableName, BigFraction value) {
		if (fRelaxedSyntax) {
			fVariableMap.put(variableName.toLowerCase(), new BigFractionVariable(value));
		} else {
			fVariableMap.put(variableName, new BigFractionVariable(value));
		}
	}

	// public FieldElementVariable<BigFraction> defineVariable(String
	// variableName, double value) {
	// FieldElementVariable<BigFraction> val = new
	// BigFractionVariable(fDfpField.newDfp(value));
	// if (fRelaxedSyntax) {
	// fVariableMap.put(variableName.toLowerCase(), val);
	// } else {
	// fVariableMap.put(variableName, val);
	// }
	// return val;
	// }

	/**
	 * Define a value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	@Override
	public void defineVariable(String variableName, FieldElementVariable<BigFraction> value) {
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
			return new BigFractionNode(fDfpField.getZero());
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
						fun.add(new BigFractionNode(BigFraction.MINUS_ONE));
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
								new BigFractionNode(BigFraction.MINUS_ONE), f.get(2));
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
				return new BigFractionNode(fDfpField.getOne());
			}
			FieldElementVariable<BigFraction> v = fVariableMap.get(node.toString());
			if (v != null) {
				return new BigFractionNode(fDfpField.getZero());
			}
			BigFraction dbl = SYMBOL_MAP.get(node.toString());
			if (dbl != null) {
				return new BigFractionNode(fDfpField.getZero());
			}
			return new BigFractionNode(fDfpField.getZero());
		} else if (node instanceof NumberNode) {
			return new BigFractionNode(fDfpField.getZero());
		}

		throw new ArithmeticMathException(
				"BigFractionEvaluator#derivative(ASTNode, SymbolNode) not possible for: " + node.toString());
	}

	/**
	 * Reevaluate the <code>expression</code> (possibly after a new Variable
	 * assignment)
	 * 
	 * @param Expression
	 * @return
	 * @throws SyntaxError
	 */
	public BigFraction evaluate() {
		if (fNode == null) {
			throw new SyntaxError(0, 0, 0, " ", "No parser input defined", 1);
		}
		return evaluateNode(fNode);
	}

	/**
	 * Parse the given <code>expression String</code> and evaluate it to a
	 * BigFraction value
	 * 
	 * @param expression
	 * @return
	 * @throws SyntaxError
	 */
	public BigFraction evaluate(String expression) {
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
	public BigFraction evaluateFunction(final FunctionNode functionNode) {
		if (!functionNode.isEmpty() && functionNode.getNode(0) instanceof SymbolNode) {
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
				IFieldElementFunction<BigFraction> function = FUNCTION_MAP.get(symbol);
				if (function instanceof IFieldElementFunctionNode) {
					return ((IFieldElementFunctionNode<BigFraction>) function).evaluate(this, functionNode);
				}
				if (functionNode.size() == 1) {
					if (function instanceof IFieldElement0Function) {
						return ((IFieldElement0Function<BigFraction>) function).evaluate();
					}
				} else if (functionNode.size() == 2) {
					if (function instanceof IFieldElement1Function) {
						return ((IFieldElement1Function<BigFraction>) function)
								.evaluate(evaluateNode(functionNode.getNode(1)));
					}
				} else if (functionNode.size() == 3) {
					ASTNode arg2 = functionNode.getNode(2);
					if (function instanceof IFieldElementInt2Function && arg2 instanceof IntegerNode) {
						return ((IFieldElementInt2Function<BigFraction>) function)
								.evaluate(evaluateNode(functionNode.getNode(1)), ((IntegerNode) arg2).getIntValue());
					}
					if (function instanceof IFieldElement2Function) {
						return ((IFieldElement2Function<BigFraction>) function)
								.evaluate(evaluateNode(functionNode.getNode(1)), evaluateNode(arg2));
					}
				}
				// if (fCallbackFunction != null) {
				// BigFraction dfpArgs[] = new BigFraction[functionNode.size() -
				// 1];
				// for (int i = 0; i < dfpArgs.length; i++) {
				// dfpArgs[i] = evaluateNode(functionNode.getNode(i + 1));
				// }
				// return fCallbackFunction.evaluate(this, functionNode,
				// dfpArgs);
				// }
			}
		}
		throw new ArithmeticMathException(
				"DfpEvaluator#evaluateFunction(FunctionNode) not possible for: " + functionNode.toString());
	}

	@Override
	public boolean evaluateFunctionLogical(final FunctionNode functionNode) {
		if (!functionNode.isEmpty() && functionNode.getNode(0) instanceof SymbolNode) {
			String symbol = functionNode.getNode(0).toString();
			if (functionNode.size() == 2) {
				IBooleanFunction<BigFraction> function = FUNCTION_BOOLEAN_MAP.get(symbol);
				if (function instanceof IBooleanBoolean1Function) {
					return ((IBooleanBoolean1Function<BigFraction>) function)
							.evaluate(evaluateNodeLogical(functionNode.getNode(1)));
				}
			} else if (functionNode.size() == 3) {
				IBooleanFunction<BigFraction> function = FUNCTION_BOOLEAN_MAP.get(symbol);
				if (function instanceof IBooleanFieldElement2Function) {
					return ((IBooleanFieldElement2Function<BigFraction>) function)
							.evaluate(evaluateNode(functionNode.getNode(1)), evaluateNode(functionNode.getNode(2)));
				} else if (function instanceof IBooleanBoolean2Function) {
					return ((IBooleanBoolean2Function<BigFraction>) function).evaluate(
							evaluateNodeLogical(functionNode.getNode(1)), evaluateNodeLogical(functionNode.getNode(2)));
				}
				// } else {
				// Object obj = FUNCTION_BOOLEAN_MAP.get(symbol);
				// if (obj instanceof IBooleanBigFractionFunction) {
				// return ((IBooleanBigFractionFunction) obj).evaluate(this,
				// functionNode);
				// }
			}
		}
		throw new ArithmeticMathException(
				"BigFractionEvaluator#evaluateFunctionLogical(FunctionNode) not possible for: "
						+ functionNode.toString());

	}

	/**
	 * Evaluate an already parsed in abstract syntax tree node into a
	 * <code>BigFraction</code> number value.
	 * 
	 * @param node
	 *            abstract syntax tree node
	 * 
	 * @return the evaluated BigFraction number
	 * 
	 * @throws ArithmeticMathException
	 *             if the <code>node</code> cannot be evaluated.
	 */
	@Override
	public BigFraction evaluateNode(final ASTNode node) {
		if (node instanceof BigFractionNode) {
			return ((BigFractionNode) node).getValue();
		}
		if (node instanceof FunctionNode) {
			return evaluateFunction((FunctionNode) node);
		}
		if (node instanceof SymbolNode) {
			FieldElementVariable<BigFraction> v = fVariableMap.get(node.toString());
			if (v != null) {
				return v.getValue();
			}
			BigFraction dbl = SYMBOL_MAP.get(node.toString());
			if (dbl != null) {
				return dbl;
			}
		} else if (node instanceof NumberNode) {
			if (node instanceof FractionNode) {
				return new BigFraction(new BigInteger(((FractionNode) node).getNumerator().getString()),
						new BigInteger(((FractionNode) node).getDenominator().getString()));
			} else if (node instanceof IntegerNode) {
				String iStr = ((NumberNode) node).getString();
				if (iStr != null) {
					return new BigFraction(new BigInteger(iStr), BigInteger.ONE);
				} else {
					return new BigFraction(((IntegerNode) node).getIntValue(), 1);
				}
			}

			return new BigFraction(((NumberNode) node).doubleValue());
		}

		throw new ArithmeticMathException(
				"BigFractionEvaluator#evaluateNode(ASTNode) not possible for: " + node.toString());
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
				"BigFractionEvaluator#evaluateNodeLogical(ASTNode) not possible for: " + node.toString());
	}

	private ASTNode getDerivativeResult(ASTNode arg1Derived, FunctionNode fun) {
		if (!arg1Derived.equals(new BigFractionNode(fDfpField.getOne()))) {
			FunctionNode res = new FunctionNode(fASTFactory.createSymbol("Times"));
			res.add(arg1Derived);
			res.add(fun);
			return res;
		}
		return fun;
	}

	@Override
	public Field<BigFraction> getField() {
		return fDfpField;
	}

	@Override
	public IBooleanFunction<BigFraction> getFunctionBooleanMap(String symbolName) {
		return FUNCTION_BOOLEAN_MAP.get(symbolName);
	}

	@Override
	public IFieldElementFunction<BigFraction> getFunctionMap(String symbolName) {
		return FUNCTION_MAP.get(symbolName);
	}

	@Override
	public Boolean getSymbolBooleanMap(String symbolName) {
		return SYMBOL_BOOLEAN_MAP.get(symbolName);
	}

	@Override
	public BigFraction getSymbolFieldElementMap(String symbolName) {
		return SYMBOL_MAP.get(symbolName);
	}

	/**
	 * Returns the BigFraction variable value to which the specified
	 * variableName is mapped, or {@code null} if this map contains no mapping
	 * for the variableName.
	 * 
	 * @param variableName
	 * @return
	 */
	@Override
	public FieldElementVariable<BigFraction> getVariable(String variableName) {
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
			if (!functionNode.isEmpty() && functionNode.getNode(0) instanceof SymbolNode) {
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
		// SYMBOL_MAP.put("Catalan",
		// fDfpField.newDfp(0.91596559417721901505460351493238411077414937428167));
		// SYMBOL_MAP.put("Degree", fDfpField.newDfp(Math.PI / 180));
		// SYMBOL_MAP.put("E", fDfpField.getE());
		// SYMBOL_MAP.put("Pi", fDfpField.getPi());
		// SYMBOL_MAP.put("EulerGamma",
		// fDfpField.newDfp(0.57721566490153286060651209008240243104215933593992));
		// SYMBOL_MAP.put("Glaisher",
		// fDfpField.newDfp(1.2824271291006226368753425688697917277676889273250));
		// SYMBOL_MAP.put("GoldenRatio",
		// fDfpField.newDfp(1.6180339887498948482045868343656381177203091798058));
		// SYMBOL_MAP.put("Khinchin",
		// fDfpField.newDfp(2.6854520010653064453097148354817956938203822939945));

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
		// if (functionNode.size() > 0) {
		// boolean dfpOnly = true;
		// ASTNode node;
		// for (int i = 1; i < functionNode.size(); i++) {
		// node = functionNode.getNode(i);
		// if (node instanceof NumberNode) {
		// if (node instanceof FractionNode) {
		// functionNode.set(i,
		// new BigFractionNode(fDfpField.newDfp(((FractionNode)
		// node).getNumerator().toString())
		// .divide(fDfpField.newDfp(((FractionNode)
		// node).getDenominator().toString()))));
		// } else if (node instanceof IntegerNode) {
		// String iStr = ((NumberNode) functionNode.getNode(i)).getString();
		// if (iStr != null) {
		// functionNode.set(i, new BigFractionNode(fDfpField.newDfp(iStr)));
		// } else {
		// functionNode.set(i, new BigFractionNode(
		// fDfpField.newDfp(((IntegerNode)
		// functionNode.getNode(i)).getIntValue())));
		// }
		// } else {
		// functionNode.set(i, new BigFractionNode(
		// fDfpField.newDfp(((NumberNode)
		// functionNode.getNode(i)).getString())));
		// }
		// } else if (functionNode.getNode(i) instanceof FunctionNode) {
		// ASTNode optNode = optimizeFunction((FunctionNode)
		// functionNode.getNode(i));
		// if (!(optNode instanceof BigFractionNode)) {
		// dfpOnly = false;
		// }
		// functionNode.set(i, optNode);
		// } else if (node instanceof SymbolNode) {
		// BigFraction dbl = SYMBOL_MAP.get(node.toString());
		// if (dbl != null) {
		// functionNode.set(i, new BigFractionNode(dbl));
		// } else {
		// dfpOnly = false;
		// }
		// } else {
		// dfpOnly = false;
		// }
		// }
		// if (dfpOnly) {
		// try {
		// return new BigFractionNode(evaluateFunction(functionNode));
		// } catch (RuntimeException e) {
		//
		// }
		// }
		// }
		return functionNode;
	}

	/**
	 * Parse the given <code>expression String</code> and store the resulting
	 * ASTNode in this BigFractionEvaluator
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

	@Override
	public void setUp(BigFraction data) {
		super.setUp(data);
	}

	public void setValue(FieldElementVariable<BigFraction> variable, double value) {
		variable.setValue(new BigFraction(value));
	}

	@Override
	public void tearDown() {
	}

	@Override
	public BigFraction visit(ComplexNode node) {
		return null;
	}

	@Override
	public BigFraction visit(DoubleNode node) {
		return new BigFraction(node.doubleValue());
	}

	@Override
	public BigFraction visit(FloatNode node) {
		return new BigFraction(node.doubleValue());
	}

	@Override
	public BigFraction visit(FractionNode node) {
		return new BigFraction(new BigInteger(((FractionNode) node).getNumerator().getString()),
				new BigInteger(((FractionNode) node).getDenominator().getString()));
	}

	@Override
	public BigFraction visit(IntegerNode node) {
		String iStr = ((NumberNode) node).getString();
		if (iStr != null) {
			return new BigFraction(new BigInteger(iStr), BigInteger.ONE);
		} else {
			return new BigFraction(((IntegerNode) node).getIntValue(), 1);
		}
	}

	@Override
	public BigFraction visit(PatternNode node) {
		return null;
	}

	@Override
	public BigFraction visit(StringNode node) {
		return null;
	}

	/**
	 * Returns a <code>String</code> representation of the given
	 * <code>org.apache.commons.math3.fraction.BigFraction</code> number.
	 * 
	 * @param c
	 * @return
	 * 
	 */
	public static String toString(BigFraction bf) {
		return bf.toString();
	}

}
