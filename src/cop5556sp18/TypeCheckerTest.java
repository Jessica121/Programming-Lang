package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;

public class TypeCheckerTest {

	/*
	 * set Junit to be able to catch exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scans, parses, and type checks the input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		ASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}
//
//	@Test
//	public void expression1() throws Exception {
////		String input = "prog {int var1; var1 := cart_x[1.0,1.0]; var1 := cart_y[1.0,1.0];}";
////		String input = "prog{filename f1; write f1 to f1;}";
////		String input = "prog{if(true){int var;}; if(true){input var from @1;};}";
////		String input = "prog{image var1; red( var1[0.0,0]) := 5;}";
////		String input = "prog{image var1; red( var1[true,false]) := 5;}";
////		String input = "prog{image image1; write image1 to image1;}";
//		String input = "prog{boolean a; boolean b; while(a & b){};}";
//		typeCheck(input);
//	}

	@Test
	public void expression2_fail() throws Exception {
//		String input = "prog{if(true){int var;}; if(true){input var from @1;};}"; //error, incompatible types in binary expression
		String input = "prog{filename f1; write f1 to f1;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
}
