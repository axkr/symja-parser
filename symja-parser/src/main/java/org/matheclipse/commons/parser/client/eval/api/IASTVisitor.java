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
package org.matheclipse.commons.parser.client.eval.api;

import org.apache.commons.math3.FieldElement;
import org.matheclipse.commons.parser.client.ast.FloatNode;
import org.matheclipse.commons.parser.client.ast.FractionNode;
import org.matheclipse.commons.parser.client.ast.FunctionNode;
import org.matheclipse.commons.parser.client.ast.IntegerNode;
import org.matheclipse.commons.parser.client.ast.PatternNode;
import org.matheclipse.commons.parser.client.ast.StringNode;
import org.matheclipse.commons.parser.client.ast.SymbolNode;
import org.matheclipse.commons.parser.client.eval.ComplexNode;
import org.matheclipse.commons.parser.client.eval.DoubleNode;

/**
 * Visitor interface to run through an abstract syntax tree (AST) generated by
 * the parser.
 * 
 */
public interface IASTVisitor<T extends FieldElement<T>> extends IEvaluator<T> {
	/**
	 * Before a visitor run starts this method will be called.
	 * 
	 * @param value
	 *            an initial value which maybe <code>null</code>.
	 */
	public void setUp(T value);

	/**
	 * After a visitor run has finished this method will be called. Typically
	 * this method will be called before the getResult() method.
	 */
	public void tearDown();

	public T visit(ComplexNode node);

	public T visit(DoubleNode node);

	public T visit(FloatNode node);

	public T visit(FractionNode node);

	public T visit(FunctionNode node);

	public T visit(IntegerNode node);

	public T visit(PatternNode node);

	public T visit(StringNode node);

	public T visit(SymbolNode node);
}