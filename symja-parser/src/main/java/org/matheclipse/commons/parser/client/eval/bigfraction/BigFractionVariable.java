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

import org.apache.commons.math3.fraction.BigFraction;
import org.matheclipse.commons.parser.client.eval.api.FieldElementVariable;

public class BigFractionVariable extends FieldElementVariable<BigFraction> {
	BigFraction value;

	public BigFractionVariable(BigFraction v) {
		value = v;
	}

	@Override
	public BigFraction getValue() {
		return value;
	}

	@Override
	public void setValue(BigFraction value) {
		this.value = value;
	}

	public void setValue(int value) {
		this.value = new BigFraction(value);
	}

	public void setValue(int numerator, int denominator) {
		this.value = new BigFraction(numerator, denominator);
	}
}
