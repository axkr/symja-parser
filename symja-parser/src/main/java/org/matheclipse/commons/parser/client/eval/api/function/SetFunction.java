package org.matheclipse.commons.parser.client.eval.api.function;

import org.apache.commons.math3.FieldElement;
import org.matheclipse.commons.parser.client.ast.FunctionNode;
import org.matheclipse.commons.parser.client.ast.SymbolNode;
import org.matheclipse.commons.parser.client.eval.api.FieldElementVariable;
import org.matheclipse.commons.parser.client.eval.api.IEvaluator;
import org.matheclipse.commons.parser.client.eval.api.IFieldElementFunctionNode;
import org.matheclipse.commons.parser.client.math.ArithmeticMathException;

public class SetFunction<T extends FieldElement<T>> implements IFieldElementFunctionNode<T> {
	@Override
	public T evaluate(IEvaluator<T> engine, FunctionNode function) {
		if (function.size() != 3) {
			throw new ArithmeticMathException(
					"SetFunction#evaluate(DoubleEvaluator,FunctionNode) needs 2 arguments: " + function.toString());
		}
		if (!(function.getNode(1) instanceof SymbolNode)) {
			throw new ArithmeticMathException(
					"SetFunction#evaluate(DoubleEvaluator,FunctionNode) symbol required on the left hand side: "
							+ function.toString());
		}
		String variableName = ((SymbolNode) function.getNode(1)).getString();
		T result = engine.evaluateNode(function.getNode(2));
		FieldElementVariable<T> dv = engine.getVariable(variableName);
		if (dv == null) {
			dv = engine.createVariable(result);
		} else {
			dv.setValue(result);
		}
		engine.defineVariable(variableName, dv);
		return result;
	}
}