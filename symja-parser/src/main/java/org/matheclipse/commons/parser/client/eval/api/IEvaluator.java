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

import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.matheclipse.commons.parser.client.ast.ASTNode;
import org.matheclipse.commons.parser.client.ast.FunctionNode;
import org.matheclipse.commons.parser.client.eval.BooleanVariable;

/**
 * Interface for <code>java.lang.Object</code> based evaluation engines
 * (typically implemented as <code>IASTVisitor</code>s). For example the
 * <code>ComplexEvalVisitor</code> class uses the <code>Complex</code> class
 * data type.
 * 
 * @param <T>
 * 
 * @see org.matheclipse.commons.parser.client.eval.api.IASTVisitor
 * @see org.matheclipse.commons.parser.client.eval.ComplexEvaluator
 */
public interface IEvaluator<T extends FieldElement<T>> {
	/**
	 * Clear all defined variables for this evaluator.
	 */
	public void clearVariables();

	/**
	 * Define a boolean value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	public void defineVariable(String variableName, BooleanVariable value);

	/**
	 * Define a value for a given variable name.
	 * 
	 * @param variableName
	 * @param value
	 */
	public void defineVariable(String variableName, FieldElementVariable<T> value);

	/**
	 * Evaluate an already parsed in abstract syntax tree node into a
	 * <code>DATA</code> value.
	 * 
	 * @param node
	 *            abstract syntax tree node
	 * 
	 * @return the evaluated DATA
	 * 
	 */
	public T evaluateNode(ASTNode node);

	/**
	 * Returns the data variable value to which the specified variableName is
	 * mapped, or {@code null} if this map contains no mapping for the
	 * variableName.
	 * 
	 * @param variableName
	 * @return
	 */
	public FieldElementVariable<T> getVariable(String variableName);

	/**
	 * Returns a new data variable instance.
	 * 
	 * @param variableName
	 * @return
	 */
	public FieldElementVariable<T> createVariable(T value);

	/**
	 * Optimize an already parsed in <code>functionNode</code> into an
	 * <code>ASTNode</code>.
	 * 
	 * @param functionNode
	 * @return
	 * 
	 */
	public ASTNode optimizeFunction(final FunctionNode functionNode);
	
	public Field<T> getField();
}
