package org.matheclipse.commons.parser.test;

import org.matheclipse.commons.parser.client.Parser;
import org.matheclipse.commons.parser.client.ast.ASTNode;

import junit.framework.TestCase;

/**
 * Tests parser function for SimpleParserFactory
 */
public class ParserTestCase extends TestCase {

	public ParserTestCase(String name) {
		super(name);
	}

	public void testParser() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("-a-b*c!!+d");
			assertEquals(obj.toString(), "Plus(Plus(Times(-1, a), Times(-1, Times(b, Factorial2(c)))), d)");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser0() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("(#^3)&[x][y,z].{a,b,c}");
			assertEquals(obj.toString(), "Dot(Function(Power(Slot(1), 3))[x][y, z], List(a, b, c))");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser1() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("Integrate(Sin(x)^2+3*x^4, x)");
			assertEquals(obj.toString(), "Integrate(Plus(Power(Sin(x), 2), Times(3, Power(x, 4))), x)");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser2() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("a()[0][1]f[[x]]");
			assertEquals(obj.toString(), "Times(a()[0][1], Part(f, x))");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser3() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("f(y,z)*(a+b+c)");
			assertEquals(obj.toString(), "Times(f(y, z), Plus(Plus(a, b), c))");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
	public void testParser3a() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("f(y,z) (a+b+c)");
			assertEquals(obj.toString(), "Times(f(y, z), Plus(Plus(a, b), c))");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser4() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("$a=2");
			assertEquals(obj.toString(), "Set($a, 2)");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser5() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("4.7942553860420304E-1");
			assertEquals(obj.toString(), "4.7942553860420304E-1");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser6() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("a+%%%+%3*4!");
			assertEquals(obj.toString(), "Plus(Plus(a, Out(-3)), Times(Out(3), Factorial(4)))");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser7() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("a+%%%+%3*:=4!");
			fail("A SyntaxError exception should occur here");
		} catch (RuntimeException e) {
			assertEquals("Syntax error in line: 1 - Operator: := is no prefix operator.\n" + "a+%%%+%3*:=4!\n" + "          ^", e
					.getMessage());
		}
	}

	public void testParser8() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("-42424242424242424242");
			assertEquals(obj.toString(), "-42424242424242424242");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser9() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("-42424242424242424242.125");
			assertEquals(obj.toString(), "-42424242424242424242.125");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser10() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("-3/4");
			assertEquals(obj.toString(), "-3/4");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser11() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("-(3/4)");
			assertEquals(obj.toString(), "-3/4");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser12() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("-(Pi/4)");
			assertEquals(obj.toString(), "Times(-1, Times(1/4, Pi))");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser13() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("a*b*c*d");
			assertEquals(obj.toString(), "Times(Times(Times(a, b), c), d)");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser14() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("-a-b*c!!+d");
			assertEquals(obj.dependsOn("d"), true);
			assertEquals(obj.dependsOn("x"), false);
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser15() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p
					.parse("Integrate(Sin(a_.*x_)^n_IntegerQ, x_Symbol):= -Sin(a*x)^(n-1)*Cos(a*x)/(n*a)+(n-1)/n*Integrate(Sin(a*x)^(n-2),x)/;Positive(n)&&FreeQ(a,x)");
			assertEquals(
					obj.toString(),
					"SetDelayed(Integrate(Power(Sin(Times(a_., x_)), n_IntegerQ), x_Symbol), Condition(Plus(Times(Times(-1, Power(Sin(Times(a, x)), Plus(n, Times(-1, 1)))), Times(Cos(Times(a, x)), Power(Times(n, a), -1))), Times(Times(Plus(n, Times(-1, 1)), Power(n, -1)), Integrate(Power(Sin(Times(a, x)), Plus(n, Times(-1, 2))), x))), And(Positive(n), FreeQ(a, x))))");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}

	public void testParser16() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("f[[1,2]]");
			assertEquals(obj.toString(), "Part(f, 1, 2)");
			obj = p.parse("f[[1]][[2]]");
			assertEquals(obj.toString(), "Part(Part(f, 1), 2)");
			obj = p.parse("f[[1,2,f(x)]]");
			assertEquals(obj.toString(), "Part(f, 1, 2, f(x))");
			obj = p.parse("f[[1]][[2]][[f(x)]]");
			assertEquals(obj.toString(), "Part(Part(Part(f, 1), 2), f(x))");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
	
	public void testParser17() {
		try { 
			Parser p = new Parser(true);
			Object obj = p.parse("Integrate(Sin(x)^2+3*x^4, x)");
			assertEquals(obj.toString(), "Integrate(Plus(Power(Sin(x), 2), Times(3, Power(x, 4))), x)");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void testParser18() {
		try {
			Parser p = new Parser(true);
			Object obj = p.parse("a()[0][1]f[[x]]");
			assertEquals(obj.toString(), "Times(a()[0][1], Part(f, x))");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void testParser19() {
		try {
			Parser p = new Parser(true);
			Object obj = p.parse("a sin()cos()x()y z");
			assertEquals(obj.toString(), "Times(Times(Times(Times(Times(a, sin()), cos()), x()), y), z)");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	public void testParser20() {
		try {
			Parser p = new Parser(true);
			Object obj = p.parse("((1+x) (5+x))");
			assertEquals(obj.toString(), "Times(Plus(1, x), Plus(5, x))");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	public void testParser21() {
		try {
			Parser p = new Parser(true);
			Object obj = p.parse("1/((1+x) (5+x))");
			assertEquals(obj.toString(), "Power(Times(Plus(1, x), Plus(5, x)), -1)");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	public void testParser22() {
		try {
			Parser p = new Parser(true);
			Object obj = p.parse("2(x^3)");
			assertEquals(obj.toString(), "Times(2, Power(x, 3))");
			obj = p.parse("2*(x^3)");
			assertEquals(obj.toString(), "Times(2, Power(x, 3))");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	public void testParser23() {
		try {
			Parser p = new Parser(true);
			Object obj = p.parse("1/2(x^3)");
			assertEquals(obj.toString(), "Times(1/2, Power(x, 3))");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	} 
	
	public void testParser24() {
		try {
			Parser p = new Parser(true);
			Object obj = p.parse("(a+b)^2 (x+y)^3 (u+w)^4");
			assertEquals(obj.toString(), "Times(Times(Power(Plus(a, b), 2), Power(Plus(x, y), 3)), Power(Plus(u, w), 4))");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	public void testParser25() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("{ArcCsc}[[1]][x]");
			assertEquals(obj.toString(), "Part(List(ArcCsc), 1)[x]");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
	
	public void testParser26() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("\\[alpha]+\\[alpha]");
			assertEquals(obj.toString(), "Plus(\\[alpha], \\[alpha])");
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
	
	public void testParserDerive() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("y'''(x)");
			assertEquals(obj.toString(), "Derivative(3)[y][x]");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
	
	public void testParserFunction() {
		try {
			Parser p = new Parser(true);
			ASTNode obj = p.parse("#^2-3#-1&");
			assertEquals(obj.toString(), "Function(Plus(Plus(Power(Slot(1), 2), Times(-1, Times(3, Slot(1)))), Times(-1, 1)))");
		} catch (RuntimeException e) {
			e.printStackTrace();
			assertEquals("", e.getMessage());
		}
	}
}