/*
 * Copyright 2005-2008 Axel Kramer (axelclk@gmail.com)
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
package org.matheclipse.commons.parser.client.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matheclipse.commons.parser.client.ast.ASTNode;
import org.matheclipse.commons.parser.client.ast.FloatNode;
import org.matheclipse.commons.parser.client.ast.FractionNode;
import org.matheclipse.commons.parser.client.ast.FunctionNode;
import org.matheclipse.commons.parser.client.ast.IParserFactory;
import org.matheclipse.commons.parser.client.ast.IntegerNode;
import org.matheclipse.commons.parser.client.ast.Pattern2Node;
import org.matheclipse.commons.parser.client.ast.Pattern3Node;
import org.matheclipse.commons.parser.client.ast.PatternNode;
import org.matheclipse.commons.parser.client.ast.StringNode;
import org.matheclipse.commons.parser.client.ast.SymbolNode;

public class ASTNodeFactory implements IParserFactory {
	public final static int PLUS_PRECEDENCE = 2900;

	public final static int TIMES_PRECEDENCE = 3800;

	public final static int DIVIDE_PRECEDENCE = 4500;

	public final static int POWER_PRECEDENCE = 5700;

	static final String[] HEADER_STRINGS = { "MessageName", "Get", "PatternTest", "MapAll", "TimesBy", "Plus", "UpSet",
			"CompoundExpression", "Map", "Unset", "Apply", "ReplaceRepeated", "Less", "And", "Divide", "Set",
			"Increment", "Factorial2", "LessEqual", "NonCommutativeMultiply", "Factorial", "Times", "Power", "Dot",
			"Not", "PreMinus", "SameQ", "RuleDelayed", "GreaterEqual", "Condition", "Colon", "//", "DivideBy", "Or",
			"Equal", "StringJoin", "Unequal", "Decrement", "SubtractFrom", "PrePlus", "RepeatedNull", "UnsameQ", "Rule",
			"UpSetDelayed", "PreIncrement", "Function", "Greater", "PreDecrement", "Subtract", "SetDelayed",
			"Alternatives", "AddTo", "Repeated", "ReplaceAll" };

	static final String[] OPERATOR_STRINGS = { "::", "<<", "?", "//@", "*=", "+", "^=", ";", "/@", "=.", "@@", "//.",
			"<", "&&", "/", "=", "++", "!!", "<=", "**", "!", "*", "^", ".", "!", "-", "===", ":>", ">=", "/;", ":",
			"//", "/=", "||", "==", "<>", "!=", "--", "-=", "+", "...", "=!=", "->", "^:=", "++", "&", ">", "--", "-",
			":=", "|", "+=", "..", "/." };

	static final Operator[] OPERATORS = { new InfixOperator("::", "MessageName", 7400, InfixOperator.NONE),
			new PrefixOperator("<<", "Get", 7000), new InfixOperator("?", "PatternTest", 6600, InfixOperator.NONE),
			new InfixOperator("//@", "MapAll", 6100, InfixOperator.RIGHT_ASSOCIATIVE),
			new InfixOperator("*=", "TimesBy", 900, InfixOperator.NONE),
			new InfixOperator("+", "Plus", PLUS_PRECEDENCE, InfixOperator.NONE),
			new InfixOperator("^=", "UpSet", 300, InfixOperator.NONE),
			new InfixOperator(";", "CompoundExpression", 100, InfixOperator.NONE),
			new InfixOperator("/@", "Map", 6100, InfixOperator.RIGHT_ASSOCIATIVE),
			new PostfixOperator("=.", "Unset", 300),
			new InfixOperator("@@", "Apply", 6100, InfixOperator.RIGHT_ASSOCIATIVE),
			new InfixOperator("//.", "ReplaceRepeated", 1000, InfixOperator.LEFT_ASSOCIATIVE),
			new InfixOperator("<", "Less", 2600, InfixOperator.NONE),
			new InfixOperator("&&", "And", 2000, InfixOperator.NONE),
			new DivideOperator("/", "Divide", 4500, InfixOperator.LEFT_ASSOCIATIVE),
			new InfixOperator("=", "Set", 300, InfixOperator.RIGHT_ASSOCIATIVE),
			new PostfixOperator("++", "Increment", 6400), new PostfixOperator("!!", "Factorial2", 6000),
			new InfixOperator("<=", "LessEqual", 2600, InfixOperator.NONE),
			new InfixOperator("**", "NonCommutativeMultiply", 5000, InfixOperator.NONE),
			new PostfixOperator("!", "Factorial", 6000),
			new InfixOperator("*", "Times", TIMES_PRECEDENCE, InfixOperator.NONE),
			new InfixOperator("^", "Power", POWER_PRECEDENCE, InfixOperator.RIGHT_ASSOCIATIVE),
			new InfixOperator(".", "Dot", 4700, InfixOperator.NONE), new PrefixOperator("!", "Not", 2100),
			new PreMinusOperator("-", "PreMinus", 4600), new InfixOperator("===", "SameQ", 2400, InfixOperator.NONE),
			new InfixOperator(":>", "RuleDelayed", 1100, InfixOperator.RIGHT_ASSOCIATIVE),
			new InfixOperator(">=", "GreaterEqual", 2600, InfixOperator.NONE),
			new InfixOperator("/;", "Condition", 1200, InfixOperator.LEFT_ASSOCIATIVE),
			new InfixOperator(":", "Colon", 700, InfixOperator.NONE),
			new InfixOperator("//", "//", 600, InfixOperator.NONE),
			new InfixOperator("/=", "DivideBy", 900, InfixOperator.NONE),
			new InfixOperator("||", "Or", 1900, InfixOperator.NONE),
			new InfixOperator("==", "Equal", 2600, InfixOperator.NONE),
			new InfixOperator("<>", "StringJoin", 5800, InfixOperator.NONE),
			new InfixOperator("!=", "Unequal", 2600, InfixOperator.NONE), new PostfixOperator("--", "Decrement", 6400),
			new InfixOperator("-=", "SubtractFrom", 900, InfixOperator.NONE), new PrePlusOperator("+", "PrePlus", 4600),
			new PostfixOperator("...", "RepeatedNull", 1500),
			new InfixOperator("=!=", "UnsameQ", 2400, InfixOperator.NONE),
			new InfixOperator("->", "Rule", 1100, InfixOperator.RIGHT_ASSOCIATIVE),
			new InfixOperator("^:=", "UpSetDelayed", 300, InfixOperator.NONE),
			new PrefixOperator("++", "PreIncrement", 6400), new PostfixOperator("&", "Function", 800),
			new InfixOperator(">", "Greater", 2600, InfixOperator.NONE), new PrefixOperator("--", "PreDecrement", 6400),
			new SubtractOperator("-", "Subtract", 2900, InfixOperator.LEFT_ASSOCIATIVE),
			new InfixOperator(":=", "SetDelayed", 300, InfixOperator.NONE),
			new InfixOperator("|", "Alternatives", 1400, InfixOperator.NONE),
			new InfixOperator("+=", "AddTo", 900, InfixOperator.NONE), new PostfixOperator("..", "Repeated", 1500),
			new InfixOperator("/.", "ReplaceAll", 1000, InfixOperator.LEFT_ASSOCIATIVE) };

	public final static ASTNodeFactory MMA_STYLE_FACTORY = new ASTNodeFactory(false);

	public final static ASTNodeFactory RELAXED_STYLE_FACTORY = new ASTNodeFactory(true);

	/**
	 */
	private static HashMap<String, Operator> fOperatorMap;

	/**
	 */
	private static HashMap<String, ArrayList<Operator>> fOperatorTokenStartSet;

	static {
		fOperatorMap = new HashMap<String, Operator>();
		fOperatorTokenStartSet = new HashMap<String, ArrayList<Operator>>();
		for (int i = 0; i < HEADER_STRINGS.length; i++) {
			addOperator(fOperatorMap, fOperatorTokenStartSet, OPERATOR_STRINGS[i], HEADER_STRINGS[i], OPERATORS[i]);
		}
	}

	static public void addOperator(final Map<String, Operator> operatorMap,
			final Map<String, ArrayList<Operator>> operatorTokenStartSet, final String operatorStr,
			final String headStr, final Operator oper) {
		ArrayList<Operator> list;
		operatorMap.put(headStr, oper);
		list = operatorTokenStartSet.get(operatorStr);
		if (list == null) {
			list = new ArrayList<Operator>(2);
			list.add(oper);
			operatorTokenStartSet.put(operatorStr, list);
		} else {
			list.add(oper);
		}
	}

	static public InfixOperator createInfixOperator(final String operatorStr, final String headStr,
			final int precedence, final int grouping) {
		InfixOperator oper;
		if (headStr.equals("Divide")) {
			oper = new DivideOperator(operatorStr, headStr, precedence, grouping);
		} else if (headStr.equals("Subtract")) {
			oper = new SubtractOperator(operatorStr, headStr, precedence, grouping);
		} else {
			oper = new InfixOperator(operatorStr, headStr, precedence, grouping);
		}
		return oper;
	}

	static public PostfixOperator createPostfixOperator(final String operatorStr, final String headStr,
			final int precedence) {
		return new PostfixOperator(operatorStr, headStr, precedence);
	}

	static public PrefixOperator createPrefixOperator(final String operatorStr, final String headStr,
			final int precedence) {
		PrefixOperator oper;
		if (headStr.equals("PreMinus")) {
			oper = new PreMinusOperator(operatorStr, headStr, precedence);
		} else if (headStr.equals("PrePlus")) {
			oper = new PrePlusOperator(operatorStr, headStr, precedence);
		} else {
			oper = new PrefixOperator(operatorStr, headStr, precedence);
		}
		return oper;
	}

	private final boolean fIgnoreCase;

	/**
	 * Create a default ASTNode factory
	 * 
	 * @param ignoreCase
	 *            don't distinguish between upper and lower case characters in
	 *            identifiers
	 */
	public ASTNodeFactory(boolean ignoreCase) {
		this.fIgnoreCase = ignoreCase;
	}

	/**
	 * Creates a new list with no arguments from the given header object .
	 */
	@Override
	public FunctionNode createAST(final ASTNode headExpr) {
		return new FunctionNode(headExpr);
	}

	@Override
	public ASTNode createDouble(final String doubleString) {
		return new FloatNode(doubleString);
	}

	@Override
	public FractionNode createFraction(final IntegerNode numerator, final IntegerNode denominator) {
		return new FractionNode(numerator, denominator);
	}

	@Override
	public FunctionNode createFunction(final SymbolNode head) {
		return new FunctionNode(head);
	}

	@Override
	public FunctionNode createFunction(final SymbolNode head, final ASTNode arg0) {
		return new FunctionNode(head, arg0);
	}

	@Override
	public FunctionNode createFunction(final SymbolNode head, final ASTNode arg0, final ASTNode arg1) {
		return new FunctionNode(head, arg0, arg1);
	}

	@Override
	public IntegerNode createInteger(final int intValue) {
		return new IntegerNode(intValue);
	}

	@Override
	public IntegerNode createInteger(final String integerString, final int numberFormat) {
		return new IntegerNode(integerString, numberFormat);
	}

	@Override
	public PatternNode createPattern(final SymbolNode patternName, final ASTNode check) {
		return new PatternNode(patternName, check);
	}

	@Override
	public PatternNode createPattern(final SymbolNode patternName, final ASTNode check, boolean optional) {
		return new PatternNode(patternName, check, optional);
	}

	@Override
	public PatternNode createPattern2(final SymbolNode patternName, final ASTNode check) {
		return new Pattern2Node(patternName, check);
	}

	@Override
	public PatternNode createPattern3(final SymbolNode patternName, final ASTNode check) {
		return new Pattern3Node(patternName, check);
	}

	@Override
	public StringNode createString(final StringBuilder buffer) {
		return new StringNode(buffer.toString());
	}

	@Override
	public SymbolNode createSymbol(final String symbolName) {
		String name = symbolName;
		if (fIgnoreCase) {
			name = symbolName.toLowerCase();
		}
		return new SymbolNode(name);
	}

	@Override
	public Operator get(final String identifier) {
		return fOperatorMap.get(identifier);
	}

	/**
	 * public Map<String, Operator> getIdentifier2OperatorMap()
	 */
	@Override
	public Map<String, Operator> getIdentifier2OperatorMap() {
		return fOperatorMap;
	}

	/**
	 * 
	 */
	@Override
	public Map<String, ArrayList<Operator>> getOperator2ListMap() {
		return fOperatorTokenStartSet;
	}

	@Override
	public String getOperatorCharacters() {
		return DEFAULT_OPERATOR_CHARACTERS;
	}

	/**
	 * 
	 */
	@Override
	public List<Operator> getOperatorList(final String key) {
		return fOperatorTokenStartSet.get(key);
	}

	@Override
	public boolean isValidIdentifier(String identifier) {
		return true;
	}

}
