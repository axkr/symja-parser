package org.matheclipse.commons.parser.client.eval.api;

import org.apache.commons.math3.FieldElement;

public abstract class FieldElementVariable<T extends FieldElement<T>> {

	protected T value;

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
}