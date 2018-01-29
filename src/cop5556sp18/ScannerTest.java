 /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(pos, t.pos);
		assertEquals(length, t.length);
		assertEquals(line, t.line());
		assertEquals(pos_in_line, t.posInLine());
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}
	


	/**
	 * Simple test case with an empty program.  The only Token will be the EOF Token.
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, the end of line 
	 * character would be inserted by the text editor.
	 * Showing the input will let you check your input is 
	 * what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testAt() throws LexicalException {
		String input = "@@\n@@";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_AT, 0, 1, 1, 1);
		checkNext(scanner, OP_AT, 1, 1, 1, 2);
		checkNext(scanner, OP_AT, 3, 1, 2, 1);
		checkNext(scanner, OP_AT, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	

	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failIllegalChar() throws LexicalException {
		String input = ";;~";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(2,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void testParens() throws LexicalException {
		String input = "()";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, RPAREN, 1, 1, 1, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSq() throws LexicalException {
		String input = "00.0099889";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 9);
//		checkNext(scanner, INTEGER_LITERAL, 2, 1, 2, 1);
//		checkNext(scanner, INTEGER_LITERAL, 3, 1, 2, 2);
//		checkNextIsEOF(scanner);
	}

//	@Test
//	public void testComments() throws LexicalException {
//		String input = "/****//*123*//**//*132142134324fa&**%%%%dsf8*****";
//		show(input);
//		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
//		try {
//			Scanner scanner = new Scanner(input).scan();
//			show(scanner);
//		} catch (LexicalException e) {  //Catch the exception
//			show(e);                    //Display it
//			throw e;                    //Rethrow exception so JUnit will see it
//		}
//	}
	
	@Test
	public void testSeperator() throws LexicalException {
		String input = "()\n[]\n;\n,\n{}\n<<\n>>\n.";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, RPAREN, 1, 1, 1, 2);
		checkNext(scanner, LSQUARE, 3, 1, 2, 1);
		checkNext(scanner, RSQUARE, 4, 1, 2, 2);
		checkNext(scanner, SEMI, 6, 1, 3, 1);
		checkNext(scanner, COMMA, 8, 1, 4, 1);
		checkNext(scanner, LBRACE, 10, 1, 5, 1);
		checkNext(scanner, RBRACE, 11, 1, 5, 2);
		checkNext(scanner, LPIXEL, 13, 2, 6, 1);
		checkNext(scanner, RPIXEL, 16, 2, 7, 1);
		checkNext(scanner, DOT, 19, 1, 8, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test 
	public void testKeyword() throws LexicalException {
		String input = "Z default_width default_height show write to"
				+ " input from cart_x cart_y polar_a polar_r "
				+ "abs sin cos atan log image int float filename boolean red "
				+ "blue green alpha while if width height";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_Z, 1);
		checkNext(scanner, KW_default_width, 13);
		checkNext(scanner, KW_default_height, 14);
		checkNext(scanner, KW_show, 4);
		checkNext(scanner, KW_write, 5);
		checkNext(scanner, KW_to, 2);
		checkNext(scanner, KW_input, 5);
		checkNext(scanner, KW_from, 4);
		checkNext(scanner, KW_cart_x, 6);
		checkNext(scanner, KW_cart_y, 6);
		checkNext(scanner, KW_polar_a, 7);
		checkNext(scanner, KW_polar_r, 7);
		checkNext(scanner, KW_abs, 3);
		checkNext(scanner, KW_sin, 3);
		checkNext(scanner, KW_cos, 3);
		checkNext(scanner, KW_atan, 4);
		checkNext(scanner, KW_log, 3);
		checkNext(scanner, KW_image, 5);
		checkNext(scanner, KW_int, 3);
		checkNext(scanner, KW_float, 5);
		checkNext(scanner, KW_filename, 8);
		checkNext(scanner, KW_boolean, 7);
		checkNext(scanner, KW_red, 3);
		checkNext(scanner, KW_blue, 4);
		checkNext(scanner, KW_green, 5);
		checkNext(scanner, KW_alpha, 5);
		checkNext(scanner, KW_while, 5);
		checkNext(scanner, KW_if, 2);
		checkNext(scanner, KW_width, 5);
		checkNext(scanner, KW_height, 6);
		checkNextIsEOF(scanner);
	}
	
	@Test 
	public void testNumber() throws LexicalException {
		String input = "9.";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 2);
	}
	
	@Test 
	public void testTrick() throws LexicalException {
		String input1 = "***";
		String input2 = "true\nfalse";
		Scanner scanner1 = new Scanner(input1).scan();
		Scanner scanner2 = new Scanner(input2).scan();
		show(input1);
		show(scanner1);
		show(input2);
		show(scanner2);
		checkNext(scanner1, OP_POWER, 2);
		checkNext(scanner2, BOOLEAN_LITERAL, 4);
	}
	
	@Test
	public void testEqual() throws LexicalException {
		String input = "=";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner = new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void testOperator() throws LexicalException {
		String input = "> < ! ? : == != <= >= & | + - * / % ** := @";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
	}
	
	@Test 
	public void testrandom() throws LexicalException {
		String input = "fadsfasdgadgdj234roiefsdn oir23y8ifuejsku;yglsuvdb";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 25);
		checkNext(scanner, IDENTIFIER, 15);
		checkNext(scanner, SEMI, 1);
		checkNext(scanner, IDENTIFIER, 8);
	}
	
	@Test
	public void testIdentifier() throws LexicalException {
		String input = "a_fdasf$";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 8);
	}
	
	@Test
	public void testLen() throws LexicalException {
		String input = "<=";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_LE, 2);
	}
	
}
	

