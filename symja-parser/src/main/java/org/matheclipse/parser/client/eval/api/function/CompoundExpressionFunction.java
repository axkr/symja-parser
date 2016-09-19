package org.matheclipse.parser.client.eval.api.function;

import org.apache.commons.math3.FieldElement;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.eval.api.AbstractASTVisitor;
import org.matheclipse.parser.client.eval.api.IEvaluator;
import org.matheclipse.parser.client.eval.api.IFieldElementFunctionNode;

public class CompoundExpressionFunction<T extends FieldElement<T>> implements IFieldElementFunctionNode<T> {
	@Override
	public T evaluate(IEvaluator<T> engine, FunctionNode function) {
		T result = null;
		int end = function.size();
		for (int i = 1; i < end; i++) {
			result = engine.evaluateNode(function.getNode(i));
		}
		return result;
	}
}
