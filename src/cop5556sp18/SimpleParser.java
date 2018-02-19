package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
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


import cop5556sp18.Scanner.Token;
import cop5556sp18.SimpleParser.SyntaxException;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


public class SimpleParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}

	/*
	 * Program ::= Identifier Block
	 */
	public void program() throws SyntaxException {
		match(IDENTIFIER);
		block();
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {KW_input, KW_write, IDENTIFIER, KW_red, KW_blue, 
			KW_green, KW_alpha, KW_while, KW_if, KW_show, KW_sleep};
	
	public void block() throws SyntaxException {
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			declaration();
		} else if (isKind(firstStatement)) {
			statement();
		}
			match(SEMI);
		}
		match(RBRACE);
	}
	
	public void declaration() throws SyntaxException {
		// Declaration ::= Type  IDENTIFIER  |  image   IDENTIFIER   [  Expression  ,  Expression  ]
		if (isKind(KW_image)) {
			consume();
			if (isKind(IDENTIFIER)) {
				consume();
				if (getTypeSets("PixelSelector").contains(t.kind)) {
					pixelSelector();
				} 
			} else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		}else if (getTypeSets("Type").contains(t.kind)) {
			type();
			if (isKind(IDENTIFIER)) {
				consume();
			}else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
//	 Statement ::= StatementInput | StatementWrite | StatementAssignment
//	| StatementWhile | StatementIf | StatementShow | StatementSleep
	public void statement() throws SyntaxException {
		if (getTypeSets("StatementInput").contains(t.kind)) {
			inputStatement();
		} else if (getTypeSets("StatementWrite").contains(t.kind)) {
			writeStatement();
		} else if (getTypeSets("StatementAssignment").contains(t.kind)) {
			assignmentStatement();
		}else if (getTypeSets("StatementIf").contains(t.kind)) {
			ifStatement();
		}else if (getTypeSets("StatementWhile").contains(t.kind)) {
			whileStatement();
		}else if (getTypeSets("StatementShow").contains(t.kind)) {
			showStatement();
		}else if (getTypeSets("StatementSleep").contains(t.kind)) {
			sleepStatement();
		}else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
	public void type() throws SyntaxException {
		// Type ::=  int  |  float  |  boolean  |  image  |  filename
		if (isKind(KW_boolean)) {
			consume();
		} else if (isKind(KW_int)) {
			consume();
		} else if (isKind(KW_float)) {
			consume();
		}else if (isKind(KW_image)) {
			consume();
		}else if (isKind(KW_filename)) {
			consume();
		}else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	private void expression() throws SyntaxException {
		//Expression ::= OrExpression  ?  Expression  :  Expression | OrExpression
		if (getTypeSets("OrExpression").contains(t.kind)) {
			orExpression();
			if (isKind(OP_QUESTION)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					expression();
					if (isKind(OP_COLON)) {
						consume();
						if (getTypeSets("Expression").contains(t.kind)) {
							expression();
						}else {
							throw new SyntaxException(t, "Error while parsing program at " + t.kind);
						}
					} else {
						throw new SyntaxException(t, "Error while parsing program at " + t.kind);
					}
				}else {
					throw new SyntaxException(t, "Error while parsing program at " + t.kind);
				}
			}
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
	private void orExpression() throws SyntaxException {
		// OrExpression ::= AndExpression (  |  AndExpression ) *
		if (getTypeSets("AndExpression").contains(t.kind)) {
			andExpression();
			while(isKind(OP_OR)) {
				consume();
				if (getTypeSets("AndExpression").contains(t.kind)) {
					andExpression();
				}else {
					throw new SyntaxException(t, "Error while parsing program at " + t.kind);
				}
			}
			
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}	
	}
	
	private void andExpression() throws SyntaxException {
		// AndExpression ::= EqExpression ( OP_AND  EqExpression )*
		if (getTypeSets("EqExpression").contains(t.kind)) {
			eqExpression();
			while(isKind(OP_AND)) {
				consume();
				if (getTypeSets("EqExpression").contains(t.kind)) {
					andExpression();
				}else {
					throw new SyntaxException(t, "Error while parsing program at " + t.kind);
				}
			}
			
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}

	private void eqExpression() throws SyntaxException {
		// EqExpression ::= RelExpression ((OP_EQ | OP_NEQ ) RelExpression )*
		if (getTypeSets("RelExpression").contains(t.kind)) {
			relExpression();
			while(isKind(OP_EQ) || isKind(OP_NEQ)) {
				consume();
				if (getTypeSets("RelExpression").contains(t.kind)) {
					relExpression();
				}else {
					throw new SyntaxException(t, "Error while parsing program at " + t.kind);
				}
			}
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}	
	}
	
	private void relExpression() throws SyntaxException {
		// RelExpression ::= AddExpression ( ( <  |  >  |  <=  |  >=  ) AddExpression)*
		if (getTypeSets("AddExpression").contains(t.kind)) {
			addExpression();
			while(isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
				consume();
				if (getTypeSets("AddExpression").contains(t.kind)) {
					addExpression();
				}else {
					throw new SyntaxException(t, "Error while parsing program at " + t.kind);
				}
			}
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
	private void addExpression() throws SyntaxException {
		// AddExpression ::= MultExpression   (  (+ | - ) MultExpression )*
		if (getTypeSets("MultExpression").contains(t.kind)) {
			multExpression();
			while(isKind(OP_PLUS) || isKind(OP_MINUS)) {
				consume();
				if (getTypeSets("MultExpression").contains(t.kind)) {
					multExpression();
				}else {
					throw new SyntaxException(t, "Error while parsing program at " + t.kind);
				}
			}
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
	private void multExpression() throws SyntaxException {
		// MultExpression := PowerExpression ( (  *  |  /  |  %  ) PowerExpression )*
		if (getTypeSets("PowerExpression").contains(t.kind)) {
			powerExpression();
			while(isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
				consume();
				if (getTypeSets("PowerExpression").contains(t.kind)) {
					powerExpression();
				}else {
					throw new SyntaxException(t, "Error while parsing program at " + t.kind);
				}
			}
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
	private void powerExpression() throws SyntaxException {
		// PowerExpression:= UnaryExpression ( ** PowerExpression|ε)
		if (getTypeSets("UnaryExpression").contains(t.kind)) {
			unaryExpression();
			if (isKind(OP_POWER)) {
				consume();
				if (getTypeSets("PowerExpression").contains(t.kind)) {
					powerExpression();
				} else {
					throw new SyntaxException(t, "Error while parsing program at " + t.kind);
				}
			} 
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
	private void pixelExpression() throws SyntaxException{
		// PixelExpression ::=  IDENTIFIER  PixelSelector
		if (isKind(IDENTIFIER)) {
			consume();
			if (getTypeSets("PixelSelector").contains(t.kind)) {
				pixelSelector();
			}else {
				throw new SyntaxException(t, "Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
	private void pixelSelector() throws SyntaxException{
//		PixelSelector ::=  [  Expression  ,  Expression  ]
		if (isKind(LSQUARE)) {
			consume();
			if (getTypeSets("Selector").contains(t.kind)) {
				helperExpCOMExp();
				if (isKind(RSQUARE)) {
					consume();
				}else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		}else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}

	private void pixelConstructor() throws SyntaxException{
//		PixelConstructor ::=  <<   Expression  ,  Expression ,    Expression  ,  Expression  >>
		if (isKind(LPIXEL)) {
			consume();
			if (getTypeSets("Selector").contains(t.kind)) {
				helperExpCOMExp();
				if (isKind(COMMA)) {
					consume();
					if (getTypeSets("Selector").contains(t.kind)) {
						helperExpCOMExp();
						if (isKind(RPIXEL)) {
							consume();
						}else {
							throw new SyntaxException(t, "Error while parsing program at " + t.kind);
						}
					}else {
						throw new SyntaxException(t, "Error while parsing program at " + t.kind);
					}
				}else {
					throw new SyntaxException(t, "Error while parsing program at " + t.kind);
				}
			}else {
				throw new SyntaxException(t, "Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
	private void color() throws SyntaxException {
			// Color ::=  red  |  green  |  blue  |  alpha
		if (isKind(KW_red)) {
			consume();
		}else if (isKind(KW_green)) {
			consume();
		}else if (isKind(KW_blue)) {
			consume();
		}else if (isKind(KW_alpha)) {
			consume();
		}else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
	
	private void assignmentStatement() throws SyntaxException{
		// StatementAssignment ::= LHS  :=  Expression
		if (getTypeSets("LHS").contains(t.kind)) {
			lhs();
			if (isKind(OP_ASSIGN)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					expression();
				}else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void ifStatement() throws SyntaxException{
		//  if (  Expression  )  Block
		if (isKind(KW_if)) {
			consume();
			if (isKind(LPAREN)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					expression();
					if (isKind(RPAREN)) {
						consume();
						if (getTypeSets("Block").contains(t.kind)) {
							block();
						}else {
							throw new SyntaxException(t, "Error while parsing program at " + t.kind);
						}
					}else {
						throw new SyntaxException(t, "Error while parsing program at " + t.kind);
					}
				}else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void whileStatement() throws SyntaxException{
		//  while (  Expression  )  Block
		if (isKind(KW_while)) {
			consume();
			if (isKind(LPAREN)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					expression();
					if (isKind(RPAREN)) {
						consume();
						if (getTypeSets("Block").contains(t.kind)) {
							block();
						}else {
							throw new SyntaxException(t, "Error while parsing program at " + t.kind);
						}
					}else {
						throw new SyntaxException(t, "Error while parsing program at " + t.kind);
					}
				}else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void writeStatement() throws SyntaxException{
		//  StatementWrite ::=  write IDENTIFIER to IDENTIFIER
		if (isKind(KW_write)) {
			consume();
			if (isKind(IDENTIFIER)) {
				consume();
				if (isKind(KW_to)) {
					consume();
					if (isKind(IDENTIFIER)) {
						consume();
					}else {
						throw new SyntaxException(t, "Error while parsing program at " + t.kind);
					}
				}else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void inputStatement() throws SyntaxException{
		//  StatementInput ::=  input IDENTIFIER from @  Expression
		if (isKind(KW_input)) {
			consume();
			if (isKind(IDENTIFIER)) {
				consume();
				if (isKind(KW_from)) {
					consume();
					if (isKind(OP_AT)) {
						consume();
						if (getTypeSets("Expression").contains(t.kind)) {
							expression();
						}else {
							throw new SyntaxException(t, "Error while parsing program at " + t.kind);
						}
					}else {
						throw new SyntaxException(t, "Error while parsing program at " + t.kind);
					}
				}else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void showStatement() throws SyntaxException{
		// show Expression
		if (isKind(KW_show)) {
			consume();
			if (getTypeSets("Expression").contains(t.kind)) {
				expression();
			}else {
				throw new SyntaxException(t, "Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void sleepStatement() throws SyntaxException{
		// sleep Expression
		if (isKind(KW_sleep)) {
			consume();
			if (getTypeSets("Expression").contains(t.kind)) {
				expression();
			}else {
				throw new SyntaxException(t, "Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void lhs() throws SyntaxException {
		// LHS ::=  IDENTIFIER  |  IDENTIFIER  PixelSelector | Color  (   IDENTIFIER  PixelSelector  )
		if (isKind(IDENTIFIER)) {
			consume();
			if (getTypeSets("PixelSelector").contains(t.kind)) {
				pixelSelector();
			} 
		} else if (getTypeSets("Color").contains(t.kind)) {
			color();
			if (isKind(LPAREN)) {
				consume();
				if (isKind(IDENTIFIER)) {
					consume();
					if (getTypeSets("PixelSelector").contains(t.kind)) {
						pixelSelector();
						if (isKind(RPAREN)) {
							consume();
						} else {
							throw new SyntaxException(t,"Error while parsing program at " + t.kind);
						}
					} else {
						throw new SyntaxException(t,"Error while parsing program at " + t.kind);
					}
				} else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}

	
	
	private void primary() throws SyntaxException {
		// INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BOOLEAN_LITERAL
		// Primary ::=  INTEGER_LITERAL  |  BOOLEAN_LITERAL  |  FLOAT_LITERAL  |
//		 (  Expression  )  | FunctionApplication |  IDENTIFIER  | PixelExpression | PredefinedName | PixelConstructor
		if (isKind(INTEGER_LITERAL)) {
			consume();
		} else if(isKind(BOOLEAN_LITERAL)) {
			consume();
		} else if(isKind(FLOAT_LITERAL)) {
			consume();
		} else if(isKind(LPAREN)) {
			consume();
				if(getTypeSets("Expression").contains(t.kind)) {
					expression();
					if(isKind(RPAREN)) {
						consume();
					} else {
						throw new SyntaxException(t,"Error while parsing program at " + t.kind);
					}
				} else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
		} else if (getTypeSets("FunctionApplication").contains(t.kind)) {
			functionApplication();
		} else if (isKind(IDENTIFIER)) {
			consume();
			if (getTypeSets("PixelSelector").contains(t.kind)) {
				pixelSelector();
			}
		} else if (getTypeSets("PredefinedName").contains(t.kind)) {
			predefinedName();
		}  else if (getTypeSets("PixelConstructor").contains(t.kind)) {
			pixelConstructor();
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void functionApplication() throws SyntaxException {
		// FunctionApplication ::= FunctionName (LPAREN Expression RPAREN  |  LSQUARE Selector RSQUARE )
		if (getTypeSets("FunctionName").contains(t.kind)) {
			functionName();
			if (isKind(LPAREN)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					expression();
					if (isKind(RPAREN)) {
						consume();
					}else {
						throw new SyntaxException(t,"Error while parsing program at " + t.kind);
					}
				}else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
			}else if (getTypeSets("PixelSelector").contains(t.kind)) {
				pixelSelector();
			} else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void predefinedName() throws SyntaxException {
		// Z | default_height |default_width
		if (isKind(KW_Z)) {
			consume();
		}else if (isKind(KW_default_height)) {
			consume();
		}else if (isKind(KW_default_width)) {
			consume();
		} else {
			throw new SyntaxException(t, "Error while parsing program at " + t.kind);
		}
	}
		
	private void helperExpCOMExp() throws SyntaxException {
		// Expression COMMA Expression   
		if (getTypeSets("Expression").contains(t.kind)) {
			expression();
			if (isKind(COMMA)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					expression();
				}else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
				}
			} else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}
	
	private void functionName() throws SyntaxException {
		// FunctionName ::=  sin  |  cos  |  atan  |  abs  |  log  |  cart_x  |  cart_y  |  polar_a  |  polar_r
//		 int  |  float  |  width  |  height  | Color
		if (getTypeSets("Color").contains(t.kind)) {
			color();
		} else {
			switch (t.kind) {
				case KW_sin: {
					consume();
				}
				break;
				case KW_int: {
					consume();
				}
				break;
				case KW_float: {
					consume();
				}
				break;
				case KW_width: {
					consume();
				}
				break;
				case KW_height: {
					consume();
				}
				break;
				case KW_cos: {
					consume();
				}
				break;
				case KW_atan: {
					consume();
				}
				break;
				case KW_abs: {
					consume();
				}
					break;
				case KW_log: {
					consume();
				}
					break;
				case KW_cart_x: {
					consume();
				}
					break;
				case KW_cart_y: {
					consume();
				}
					break;
				case KW_polar_a: {
					consume();
				}
					break;
				case KW_polar_r: {
					consume();
				}
				break;
				default:
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		}

	}
			
	private void unaryExpressionNotPlusMinus() throws SyntaxException {
		
		// UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary 
		if (isKind(OP_EXCLAMATION)) {
			consume();
			if (getTypeSets("UnaryExpression").contains(t.kind)) {
				unaryExpression();
			}else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else if (getTypeSets("Primary").contains(t.kind)) {
			primary();
		} else {
			throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
	}

	private void unaryExpression() throws SyntaxException {
		// OP_PLUS UnaryExpression | OP_MINUS UnaryExpression    | UnaryExpressionNotPlusMinus
		if (isKind(OP_PLUS)) {
			consume();
			if (getTypeSets("UnaryExpression").contains(t.kind)) {
				unaryExpression();
			} else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		}else if (isKind(OP_MINUS)) {
			consume();
			if (getTypeSets("UnaryExpression").contains(t.kind)) {
				unaryExpression();
			} else {
					throw new SyntaxException(t,"Error while parsing program at " + t.kind);
			}
		} else if (getTypeSets("UnaryExpressionNotPlusMinus").contains(t.kind)) {
			unaryExpressionNotPlusMinus();
		} else {
				throw new SyntaxException(t,"Error while parsing program at " + t.kind);
		}
		
	}

	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t, "saw " + t.kind + " expected " + kind); 
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"Syntax Error, Precondition: for all given kinds, kind != EOF");
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	private List<Kind> getTypeSets(String element) {
		List<Kind> typeSets = new ArrayList<Kind>();
		return getTypeSets(element, typeSets);
	}

	private List<Kind> getTypeSets(String element, List<Kind> typeSets) {
		switch (element) {
			case "Declaration":
				typeSets = getTypeSets("Type", typeSets);
				typeSets.add(KW_image);
				break;
			case "Type":
				typeSets.add(KW_int);
				typeSets.add(KW_float);
				typeSets.add(KW_boolean);
				typeSets.add(KW_filename);
//				typeSets.add(KW_image);
				break;
			case "Statement":
				typeSets = getTypeSets("StatementSleep", typeSets);
				typeSets = getTypeSets("StatementWhile", typeSets);
				typeSets = getTypeSets("StatementIf", typeSets);
				typeSets = getTypeSets("StatementWrite", typeSets);
				typeSets = getTypeSets("StatementInput", typeSets);
				typeSets = getTypeSets("StatementShow", typeSets);
				typeSets = getTypeSets("StatementAssignment", typeSets);
				break;
			case "StatementWhile":
				typeSets.add(Kind.KW_while);
				break;
			case "StatementIf":
				typeSets.add(Kind.KW_if);
				break;
			case "StatementShow":
				typeSets.add(Kind.KW_show);
				break;
			case "StatementSleep":
				typeSets.add(Kind.KW_sleep);
				break;
			case "StatementWrite":
				typeSets.add(Kind.KW_write);
				break;
			case "StatementInput":
				typeSets.add(Kind.KW_input);
				break;	
			case "StatementAssignment":
				typeSets = getTypeSets("LHS", typeSets);
				break;
			case "Expression":
				typeSets = getTypeSets("OrExpression", typeSets);
				break;
			case "OrExpression":
				typeSets = getTypeSets("AndExpression", typeSets);
				break;
			case "AndExpression":
				typeSets = getTypeSets("EqExpression", typeSets);
				break;
			case "EqExpression":
				typeSets = getTypeSets("RelExpression", typeSets);
				break;
			case "RelExpression":
				typeSets = getTypeSets("AddExpression", typeSets);
				break;
			case "AddExpression":
				typeSets = getTypeSets("MultExpression", typeSets);
				break;
			case "MultExpression":
				typeSets = getTypeSets("PowerExpression", typeSets);
				break;
			case "PowerExpression":
				typeSets = getTypeSets("UnaryExpression", typeSets);
				break;
			case "UnaryExpression":
				typeSets.add(Kind.OP_PLUS);
				typeSets.add(Kind.OP_MINUS);
				typeSets = getTypeSets("UnaryExpressionNotPlusMinus", typeSets);
				break;
			case "UnaryExpressionNotPlusMinus":
				typeSets.add(Kind.OP_EXCLAMATION);
				typeSets = getTypeSets("Primary", typeSets);
				break;
			case "Selector":
				typeSets = getTypeSets("Expression", typeSets);
				break;
			case "Primary":
				typeSets.add(Kind.INTEGER_LITERAL);
				typeSets.add(Kind.BOOLEAN_LITERAL);
				typeSets.add(Kind.FLOAT_LITERAL);
				typeSets.add(Kind.LPAREN);
				typeSets.add(Kind.IDENTIFIER);
				typeSets = getTypeSets("FunctionApplication", typeSets);
				typeSets = getTypeSets("PixelExpression", typeSets);
				typeSets = getTypeSets("PredefinedName", typeSets);
				typeSets = getTypeSets("PixelConstructor", typeSets);
				break;
			case "LHS":
				typeSets.add(Kind.IDENTIFIER);
				typeSets = getTypeSets("Color", typeSets);
				break;
			case "Color":
				typeSets.add(Kind.KW_red);
				typeSets.add(Kind.KW_green);
				typeSets.add(Kind.KW_blue);
				typeSets.add(Kind.KW_alpha);
				break;
			case "PixelConstructor":
				typeSets.add(Kind.LPIXEL);
				break;
			case "PixelSelector":
				typeSets.add(Kind.LSQUARE);
				break;
			case "PixelExpression":
				typeSets.add(Kind.IDENTIFIER);
				break;
			case "FunctionApplication":
				typeSets = getTypeSets("FunctionName", typeSets);
				break;
			case "FunctionName":
				typeSets.add(Kind.KW_sin);
				typeSets.add(Kind.KW_cos);
				typeSets.add(Kind.KW_atan);
				typeSets.add(Kind.KW_abs);
				typeSets.add(Kind.KW_log);
				typeSets.add(Kind.KW_width);
				typeSets.add(Kind.KW_height);
				typeSets.add(Kind.KW_int);
				typeSets.add(Kind.KW_float);
				typeSets.add(Kind.KW_cart_x);
				typeSets.add(Kind.KW_cart_y);
				typeSets.add(Kind.KW_polar_a);
				typeSets.add(Kind.KW_polar_r);
				typeSets = getTypeSets("Color", typeSets);
				break;
			case "PredefinedName":
				typeSets.add(Kind.KW_Z);
				typeSets.add(Kind.KW_default_height);
				typeSets.add(Kind.KW_default_width);
				break;
			case "Block":
				typeSets.add(Kind.LBRACE);
				typeSets.add(Kind.SEMI);
				break;
			default:
				return null;
		}

		Set<Kind> set = new HashSet<>();
		set.addAll(typeSets);
		typeSets.clear();
		typeSets.addAll(set);
		return typeSets;
	}
	
	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error, expected EOF"); 
	}

}

