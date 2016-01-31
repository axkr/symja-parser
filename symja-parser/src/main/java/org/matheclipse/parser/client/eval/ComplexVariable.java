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

import org.apache.commons.math3.complex.Complex;
import org.matheclipse.parser.client.eval.api.FieldElementVariable;

public class ComplexVariable extends FieldElementVariable<Complex> {
	public ComplexVariable(double v) {
		value = new Complex(v, 0.0);
	}

	public ComplexVariable(Complex c) {
		value = c;
	}

	public void setValue(double real) {
		this.value = new Complex(real, 0.0);
	}

	public void setValue(double real, double imaginary) {
		this.value = new Complex(real, imaginary);
	}

}
