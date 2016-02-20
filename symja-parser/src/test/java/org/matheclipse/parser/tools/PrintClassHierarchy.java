package org.matheclipse.parser.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.ast.FloatNode;
import org.matheclipse.parser.client.ast.FractionNode;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.ast.IntegerNode;
import org.matheclipse.parser.client.ast.NumberNode;
import org.matheclipse.parser.client.ast.Pattern2Node;
import org.matheclipse.parser.client.ast.Pattern3Node;
import org.matheclipse.parser.client.ast.PatternNode;
import org.matheclipse.parser.client.ast.StringNode;
import org.matheclipse.parser.client.ast.SymbolNode;
import org.matheclipse.parser.client.eval.ComplexNode;
import org.matheclipse.parser.client.eval.DoubleNode;
import org.matheclipse.parser.client.eval.bigfraction.BigFractionNode;
import org.matheclipse.parser.client.eval.dfp.DfpNode;

public class PrintClassHierarchy {
	private static final String PADDING = "        ";
	private static final String PADDING_WITH_COLUMN = "   |    ";
	private static final String PADDING_WITH_ENTRY = "   |--- ";
	private static final String BASE_CLASS = Object.class.getName();

	private final Map<String, List<String>> subClazzEntries = new HashMap<String, List<String>>();

	public static void main(final String[] args) {
		new PrintClassHierarchy(ASTNode.class, SymbolNode.class, FunctionNode.class, NumberNode.class, FloatNode.class,
				FractionNode.class, IntegerNode.class, PatternNode.class, Pattern2Node.class, Pattern3Node.class,
				BigFractionNode.class, DoubleNode.class, ComplexNode.class, DfpNode.class, StringNode.class)
						.printHierarchy();
	}

	public PrintClassHierarchy(final Class<?>... clazzes) {
		// get all entries of tree
		traverseClasses(clazzes);
	}

	public void printHierarchy() {
		// print collected entries as ASCII tree
		printHierarchy(BASE_CLASS, new Stack<Boolean>());
	}

	private void printHierarchy(final String clazzName, final Stack<Boolean> moreClassesInHierarchy) {
		if (!moreClassesInHierarchy.empty()) {
			for (final Boolean hasColumn : moreClassesInHierarchy.subList(0, moreClassesInHierarchy.size() - 1)) {
				System.out.print(hasColumn.booleanValue() ? PADDING_WITH_COLUMN : PADDING);
			}
		}

		if (!moreClassesInHierarchy.empty()) {
			System.out.print(PADDING_WITH_ENTRY);
		}

		System.out.println(clazzName);

		if (subClazzEntries.containsKey(clazzName)) {
			final List<String> list = subClazzEntries.get(clazzName);

			for (int i = 0; i < list.size(); i++) {
				// if there is another class that comes beneath the next class,
				// flag this level
				moreClassesInHierarchy.push(new Boolean(i < list.size() - 1));

				printHierarchy(list.get(i), moreClassesInHierarchy);

				moreClassesInHierarchy.removeElementAt(moreClassesInHierarchy.size() - 1);
			}
		}
	}

	private void traverseClasses(final Class<?>... clazzes) {
		// do the traverseClasses on each provided class (possible since Java 8)
		List<Class<?>> list = Arrays.asList(clazzes);// .forEach(c ->
														// traverseClasses(c,
														// 0));
		for (int i = 0; i < list.size(); i++) {
			traverseClasses(list.get(i), 0);
		}
	}

	private void traverseClasses(final Class<?> clazz, final int level) {
		final Class<?> superClazz = clazz.getSuperclass();

		if (superClazz == null) {
			// we arrived java.lang.Object
			return;
		}

		final String name = clazz.getName();
		final String superName = superClazz.getName();

		if (subClazzEntries.containsKey(superName)) {
			final List<String> list = subClazzEntries.get(superName);

			if (!list.contains(name)) {
				list.add(name);
				Collections.sort(list); // SortedList
			}
		} else {
			subClazzEntries.put(superName, new ArrayList<String>(Arrays.asList(name)));
		}

		traverseClasses(superClazz, level + 1);
	}
}