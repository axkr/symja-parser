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
package org.matheclipse.parser.client.eval;

/**
 * Represents a predicate (boolean-valued function) of two double-valued
 * arguments.
 * 
 * This is a functional interface whose functional method is
 * <code>test(double, double)</code>.
 *
 */
@FunctionalInterface
public interface DoubleBinaryPredicate {
	/**
	 * Evaluates this predicate on the given arguments.
	 * 
	 * @param arg1
	 *            the first input argument
	 * @param arg2
	 *            the second input argument
	 * @return <code>true</code> if the input argument matches the predicate,
	 *         otherwise <code>false</code>
	 */
	public boolean test(double arg1, double arg2);
}
