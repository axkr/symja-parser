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
package org.matheclipse.commons.parser.client.eval;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.MathUtils;
import org.matheclipse.commons.parser.client.ast.ASTNode;

/**
 * 
 */
public class ComplexNode extends ASTNode {

	private final Complex value; 

	public ComplexNode(Complex comp) {
		super("ComplexNode");
		this.value = comp;
	}

	public ComplexNode(double real) {
		super("ComplexNode");
		this.value = new Complex(real, 0.0);
	}

	public ComplexNode(double real, double imag) {
		super("ComplexNode");
		this.value = new Complex(real, imag);
	}

	public Complex complexValue() {
		return value;
	}

	@Override
	public String toString() {
		return ComplexEvaluator.toString(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ComplexNode) {
			return value == ((ComplexNode) obj).value;
		}
		return false;
	}

	@Override
	public int hashCode() {
		long rbits = MathUtils.hash(value.getReal());
		long ibits = MathUtils.hash(value.getImaginary());
		return (int) (rbits ^ (ibits >>> 32));
	}
}
