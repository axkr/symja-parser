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
package org.matheclipse.parser.client.eval.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.FieldElement;
import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.ast.FloatNode;
import org.matheclipse.parser.client.ast.FractionNode;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.ast.IntegerNode;
import org.matheclipse.parser.client.ast.NumberNode;
import org.matheclipse.parser.client.ast.PatternNode;
import org.matheclipse.parser.client.ast.StringNode;
import org.matheclipse.parser.client.ast.SymbolNode;
import org.matheclipse.parser.client.eval.BooleanVariable;
import org.matheclipse.parser.client.eval.ComplexNode;
import org.matheclipse.parser.client.eval.DoubleNode;
import org.matheclipse.parser.client.math.ArithmeticMathException;

/**
 * Abstract AST visitor with empty default method implementations.
 * 
 * @param <T>
 */
public abstract class AbstractASTVisitor<T extends FieldElement<T>> implements IASTVisitor<T> {
	public final static boolean DEBUG = false;

	protected Map<String, FieldElementVariable<T>> fVariableMap;

	protected Map<String, BooleanVariable> fBooleanVariables;

	protected final boolean fRelaxedSyntax;

	public AbstractASTVisitor(boolean relaxedSyntax) {
		fRelaxedSyntax = relaxedSyntax;
		fVariableMap = new HashMap<String, FieldElementVariable<T>>();
		fBooleanVariables = new HashMap<String, BooleanVariable>();
	}

	public boolean evaluateFunctionLogical(final FunctionNode functionNode) {
		if (functionNode.size() > 0 && functionNode.getNode(0) instanceof SymbolNode) {
			String symbol = functionNode.getNode(0).toString();
			if (functionNode.size() == 2) {
				IBooleanFunction<T> function = getFunctionBooleanMap(symbol);
				if (function instanceof IBooleanBoolean1Function) {
					return ((IBooleanBoolean1Function) function).evaluate(evaluateNodeLogical(functionNode.getNode(1)));
				}
			} else if (functionNode.size() == 3) {
				IBooleanFunction<T> function = getFunctionBooleanMap(symbol);
				if (function instanceof IBooleanFieldElement2Function) {
					return ((IBooleanFieldElement2Function<T>) function).evaluate(evaluateNode(functionNode.getNode(1)),
							evaluateNode(functionNode.getNode(2)));
				} else if (function instanceof IBooleanBoolean2Function) {
					return ((IBooleanBoolean2Function) function).evaluate(evaluateNodeLogical(functionNode.getNode(1)),
							evaluateNodeLogical(functionNode.getNode(2)));
				}
				// } else {
				// Object obj = FUNCTION_BOOLEAN_MAP.get(symbol);
				// if (obj instanceof IBooleanDoubleFunction) {
				// return ((IBooleanDoubleFunction) obj).evaluate(this,
				// functionNode);
				// }
			}
		}
		throw new ArithmeticMathException("AbstractASTVisitor#evaluateFunctionLogical(FunctionNode) not possible for: "
				+ functionNode.toString());

	}

	/**
	 * Evaluate an already parsed-in abstract syntax tree node (ASTNode) into a
	 * <code>T</code> value.
	 * 
	 * @param node
	 *            abstract syntax tree node
	 * 
	 * @return the evaluated value
	 * 
	 */
	@Override
	public T evaluateNode(ASTNode node) {
		if (node instanceof DoubleNode) {
			return visit((DoubleNode) node);
		}
		if (node instanceof ComplexNode) {
			return visit((ComplexNode) node);
		}
		if (node instanceof FunctionNode) {
			return visit((FunctionNode) node);
		}
		if (node instanceof NumberNode) {
			if (node instanceof FloatNode) {
				return visit((FloatNode) node);
			}
			if (node instanceof FractionNode) {
				return visit((FractionNode) node);
			}
			if (node instanceof IntegerNode) {
				return visit((IntegerNode) node);
			}
		}
		if (node instanceof PatternNode) {
			return visit((PatternNode) node);
		}
		if (node instanceof StringNode) {
			return visit((StringNode) node);
		}
		if (node instanceof SymbolNode) {
			return visit((SymbolNode) node);
		}
		return null;
	}

	/**
	 * Evaluate an already parsed-in abstract syntax tree node into a
	 * <code>T</code> value.
	 * 
	 * @param node
	 *            abstract syntax tree node
	 * @param value
	 *            an initial value for the node visitors <code>setup()</code>
	 *            method.
	 * 
	 * @return the evaluated Complex number
	 * 
	 */
	public T evaluateNode(ASTNode node, T value) {
		try {
			setUp(value);
			return evaluateNode(node);
		} finally {
			tearDown();
		}
	}

	public boolean evaluateNodeLogical(final ASTNode node) {
		if (node instanceof FunctionNode) {
			return evaluateFunctionLogical((FunctionNode) node);
		}
		if (node instanceof SymbolNode) {
			BooleanVariable v = fBooleanVariables.get(node.toString());
			if (v != null) {
				return v.getValue();
			}
			Boolean boole = getSymbolBooleanMap(node.toString());
			if (boole != null) {
				return boole.booleanValue();
			}
		}

		throw new ArithmeticMathException(
				"AbstractASTVisitor#evaluateNodeLogical(ASTNode) not possible for: " + node.toString());
	}

	abstract public IBooleanFunction<T> getFunctionBooleanMap(String symbolName);

	abstract public IFieldElementFunction<T> getFunctionMap(String symbolName);

	abstract public Boolean getSymbolBooleanMap(String symbolName);

	abstract public T getSymbolFieldElementMap(String symbolName);

	public boolean isRelaxedSyntax() {
		return fRelaxedSyntax;
	}

	@Override
	public void setUp(T value) {
	}

	@Override
	public void tearDown() {
	}

	@Override
	public T visit(ComplexNode node) {
		return null;
	}

	@Override
	public T visit(DoubleNode node) {
		return null;
	}

	@Override
	public T visit(FloatNode node) {
		return null;
	}

	@Override
	public T visit(FractionNode node) {
		return null;
	}

	@Override
	public T visit(FunctionNode functionNode) {
		if (functionNode.size() > 0 && functionNode.getNode(0) instanceof SymbolNode) {
			String symbol = functionNode.getNode(0).toString();
			if (symbol.equals("If") || (fRelaxedSyntax && symbol.equalsIgnoreCase("If"))) {
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
				IFieldElementFunction<T> function = getFunctionMap(symbol);
				if (function instanceof IFieldElementFunctionNode) {
					return ((IFieldElementFunctionNode<T>) function).evaluate(this, functionNode);
				}
				if (functionNode.size() == 1) {
					if (function instanceof IFieldElement0Function) {
						return ((IFieldElement0Function<T>) function).evaluate();
					}
				} else if (functionNode.size() == 2) {
					if (function instanceof IFieldElement1Function) {
						return ((IFieldElement1Function<T>) function).evaluate(evaluateNode(functionNode.getNode(1)));
					}
				} else if (functionNode.size() == 3) {
					ASTNode arg2 = functionNode.getNode(2);
					if (function instanceof IFieldElementInt2Function && arg2 instanceof IntegerNode) {
						return ((IFieldElementInt2Function<T>) function).evaluate(evaluateNode(functionNode.getNode(1)),
								((IntegerNode) arg2).getIntValue());
					}
					if (function instanceof IFieldElement2Function) {
						return ((IFieldElement2Function<T>) function).evaluate(evaluateNode(functionNode.getNode(1)),
								evaluateNode(functionNode.getNode(2)));
					}
				}
			}
		}
		throw new ArithmeticMathException(
				"AbstractASTVisitor#evaluateFunction(FunctionNode) not possible for: " + functionNode.toString());

	}

	@Override
	public T visit(IntegerNode node) {
		return null;
	}

	@Override
	public T visit(PatternNode node) {
		return null;
	}

	@Override
	public T visit(StringNode node) {
		return null;
	}

	@Override
	public T visit(SymbolNode node) {
		FieldElementVariable<T> v = fVariableMap.get(node.toString());
		if (v != null) {
			return v.getValue();
		}
		// Dfp c = SYMBOL_DFP_MAP.get(node.toString());
		T c = getSymbolFieldElementMap(node.toString());
		if (c != null) {
			return c;
		}
		throw new ArithmeticMathException("ComplexEvalVisitor#visit(SymbolNode) not possible for: " + node.toString());

	}
}
