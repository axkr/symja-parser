package org.matheclipse.commons.parser.client.eval.api.function;

import org.apache.commons.math3.FieldElement;
import org.matheclipse.commons.parser.client.ast.FunctionNode;
import org.matheclipse.commons.parser.client.eval.api.IEvaluator;
import org.matheclipse.commons.parser.client.eval.api.IFieldElement2Function;
import org.matheclipse.commons.parser.client.eval.api.IFieldElementFunctionNode;

public class TimesFunction<T extends FieldElement<T>>
		implements IFieldElementFunctionNode<T>, IFieldElement2Function<T> {
	@Override
	public T evaluate(T arg1, T arg2) {
		return arg1.multiply(arg2);
	}

	@Override
	public T evaluate(IEvaluator<T> engine, FunctionNode function) {
		T result = engine.getField().getOne();
		for (int i = 1; i < function.size(); i++) {
			result = result.multiply(engine.evaluateNode(function.getNode(i)));
		}
		return result;
	}
}
