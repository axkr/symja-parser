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

import org.apache.commons.math3.FieldElement;
import org.matheclipse.parser.client.Parser;
import org.matheclipse.parser.client.SyntaxError;
import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.operator.ASTNodeFactory;

/**
 * Evaluates a given expression (as <code>String</code> or <code>ASTNode</code>)
 * into a <code>T</code> object type. For example the
 * <code>ComplexEvaluator</code> class uses the <code>Complex</code> class data
 * type.
 * 
 * @param <T>
 *            the FieldElement value which should be evaluated
 * 
 * @see org.matheclipse.parser.client.eval.ComplexEvaluator
 * @see org.matheclipse.parser.client.math.Complex
 */
public abstract class FieldElementEvaluator<T extends FieldElement<T>> extends AbstractASTVisitor<T> {
	protected ASTNode fNode;

	protected final boolean fRelaxedSyntax;

	public FieldElementEvaluator(boolean relaxedSyntax) {
		super(relaxedSyntax);
		fRelaxedSyntax = relaxedSyntax;
	}

	/**
	 * Reevaluate the <code>expression</code> (possibly after a new Variable
	 * assignment)
	 * 
	 * @return the resulting FieldElement
	 * @throws SyntaxError
	 */
	public T evaluate() {
		if (fNode == null) {
			throw new SyntaxError(0, 0, 0, " ", "No parser input defined", 1);
		}
		return evaluateNode(fNode);
	}

	/**
	 * Parse the given expression <code>String</code> and evaluate it to a
	 * <code>DATA</code> value.
	 * 
	 * @param expression
	 * @return
	 * @throws SyntaxError
	 */
	public T evaluate(String expression) {
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

	@Override
	public boolean isRelaxedSyntax() {
		return fRelaxedSyntax;
	}

}
