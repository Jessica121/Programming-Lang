 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.SimpleParser;
import cop5556sp18.Scanner;
import cop5556sp18.SimpleParser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;

public class SimpleParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private SimpleParser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);
		return parser;
	}
	
	

	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block. The test case passes because
	 * it expects an exception
	 *  
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		SimpleParser parser = makeParser(input);
		parser.parse();
	}	
	
	
	//This test should pass in your complete parser.  It will fail in the starter code.
	//Of course, you would want a better error message. 
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}
	


    @Test
    public void testDemo1() throws LexicalException, SyntaxException {
    	String input = "g{[width(h),height(h)]};";
    	// + 
//        "{int y;y:=;"
//        + "while(y<height(g)){g[x,y]:=h[y,x];y:=y+1;};x:=x+1;};show g;sleep(4000);}"
        SimpleParser parser = makeParser(input);
        parser.parse();
   }
            
   @Test
   public void makeRedImage() throws LexicalException, SyntaxException {
                    String input = "makeRedImage{image im[256,256];}";
                    SimpleParser parser = makeParser(input);
                    parser.parse();
            }
   
   @Test
   public void myTest4() throws LexicalException, SyntaxException {
          String input = "mytest{"
             + "\n filename haha; "
             + "\n float xixi; "
             + "\n image keke; "
             + "\n int haha; "
             + "\n boolean bb; "
             + "\n  input hahagua from @ a == b; " //statement input
             + "\n x:=0; "
             + "\n while (x < width(g)) { "
             + "\n int y; "
             + "\n y:=0; "
             + "\n while (y < height(g)) { g[x,y] := h[y,x]; "
             + "\n y:=y+1; \n }; "
             + "\n x:=x+1; \n }; "
             + "\n show g; "
             + "\n sleep(4000);"
             + "if (a > 3 ? a > 1 : a < 3) {}; "  // thresome
             + "if (a | a | a) {}; "  // or
             + "if (a & a & a) {}; "  // and
             + "if (a == a != a) {}; "  // eq
                + "if (a < a > a <= a >= a) {}; "  // real
             + "if (a + a - a) {}; "  // add
             + "if (a * a / a % a) {}; "  // multi
             + "if (a * a / a % a) {}; "  // power
             + "if (a) {}; "  // power
             + "if (+-!+-!+-!+-!+-!+-1) {}; "  // unary
             + "if (1.1) {}; "  // primary
             + "if (1) {}; "  // primary
             + "if (true) {}; "  // primary
             + "if (false) {}; "  // primary
             + "if ((a == 1)) {}; "  // primary
             + "if ((width[a, a])) {}; "  // primary
             + "if (sin(a)) {}; "  // primary
             + "if (cos(a)) {}; "  // primary
             + "if (atan(a)) {}; "  // primary
             + "if (abs(a)) {}; "  // primary
             + "if (log(a)) {}; "  // primary
             + "if (cart_x(a)) {}; "  // primary
             + "if (polar_a(a)) {}; "  // primary
             + "if (polar_r(a)) {}; "  // primary
             + "if (int(a)) {}; "  // primary
             + "if (float(a)) {}; "  // primary
             + "if (width(a)) {}; "  // primary
             + "if (height(a)) {}; "  // primary
             + "if (abs(a)) {}; "  // primary
             + "if (red(a)) {}; "  // primary
             + "if (green(a)) {}; "  // primary
             + "if (blue(a)) {}; "  // primary
             + "if (alpha(a)) {}; "  // primary
             + "if (Z) {}; "  // primary
             + "if (default_height) {}; "  // primary
             + "if (default_width) {}; "  // primary
             + "a[k, k] := a;"
             + "red(a[b,b]) := c;"
             + "if (a * a / a % a) {}; "  // multi

             + "if (a & a & a) {}; "  // and
             + "if (a & a & a) {}; "  // and

             + "\n }";
           SimpleParser parser = makeParser(input);
           parser.parse();
   }
   
   
   @Test
   public void myTest1() throws LexicalException, SyntaxException {
          String input = "makeRedImage{\n image im[256, 256]; \n int x; \n int y; \n x:=0; \n y:=0; \n while (x < width(im)) {\n y:=0; \n while(y < height(im)) { \n im[x,y] := <<255, 255, 0, 0>>;\n"
                                          + "y:=y+1; \n }; \n x:= x + 1;}; \n show im; \n}";
          SimpleParser parser = makeParser(input);
          parser.parse();
   }

            
            @Test
            public void testPolarR2() throws LexicalException, SyntaxException {
                    String input = "PolarR2{image im[1024,1024];int x;x:=0;while(x<width(im)) {int y;y:=0;while(y<height(im)) {float p;p:=polar_r[x,y];int r;r:=int(p)%Z;im[x,y]:=<<Z,0,0,r>>;y:=y+1;};x:=x+1;};show im;}";
                    SimpleParser parser = makeParser(input);
                    parser.parse();
            }
    
            @Test
            public void testSamples() throws LexicalException, SyntaxException {
                    String input = "samples{image bird; input bird from @0;bird;sleep(4000);image bird2[width(bird),height(bird)];int x;x:=0;while(x<width(bird2)) {int y;y:=0;while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]);green(bird2[x,y]):=blue(bird[x,y]);red(bird2[x,y]):=green(bird[x,y]);alpha(bird2[x,y]):=Z;y:=y+1;};x:=x+1;};bird2;sleep(4000);}";
                    SimpleParser parser = makeParser(input);
                    parser.parse();
            }

}
	

