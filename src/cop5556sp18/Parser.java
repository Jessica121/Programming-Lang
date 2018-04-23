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
import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


public class Parser {
	
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

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		// 	public Program(Token firstToken, Token progName, Block block) {
		Token firstToken = t;
		Token progName = match(IDENTIFIER);
		Block block = block();
		return new Program(firstToken, progName, block);
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {KW_input, KW_write, IDENTIFIER, KW_red, KW_blue, 
			KW_green, KW_alpha, KW_while, KW_if, KW_show, KW_sleep};
	
	public Block block() throws SyntaxException {
		// public Block(Token firstToken, List<ASTNode> decsOrStatements) 
		Token firstToken = t;
		List<ASTNode> decsOrStatements = new ArrayList<>();
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
	    	decsOrStatements.add(declaration());
		} else if (isKind(firstStatement)) {
			decsOrStatements.add(statement());
		}
			match(SEMI);
		}
		match(RBRACE);
		return new Block(firstToken, decsOrStatements);
	}
	
	public Declaration declaration() throws SyntaxException {
		// Declaration ::= Type  IDENTIFIER  |  image   IDENTIFIER   [  Expression  ,  Expression  ]
		// public Declaration(Token firstToken, Token type, Token name, Expression width, Expression height) {
		 Token firstToken = t;
		 PixelSelector ps = null;
		 Expression width = null, height = null;
	     Token type = t;
	     Token name = t;
		if (isKind(KW_image)) {
			type = consume();
			if (isKind(IDENTIFIER)) {
				name = consume();
				if (getTypeSets("PixelSelector").contains(t.kind)) {
					ps = pixelSelector();
					width = ps.ex;
					height = ps.ey;
				} 
			} else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		}else if (getTypeSets("Type").contains(t.kind)) {
			type = type();
			if (isKind(IDENTIFIER)) {
				name = consume();
				if (getTypeSets("PixelSelector").contains(t.kind)) {
					ps = pixelSelector();
					width = ps.ex;
					height = ps.ey;
				} 
			}else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return new Declaration(firstToken, type, name, width, height);
	}
	
//	 Statement ::= StatementInput | StatementWrite | StatementAssignment
//	| StatementWhile | StatementIf | StatementShow | StatementSleep
	public Statement statement() throws SyntaxException {
		Statement statement = null;
		if (getTypeSets("StatementInput").contains(t.kind)) {
			statement = inputStatement();
		} else if (getTypeSets("StatementWrite").contains(t.kind)) {
			statement = writeStatement();
		} else if (getTypeSets("StatementAssignment").contains(t.kind)) {
			statement = assignmentStatement();
		}else if (getTypeSets("StatementIf").contains(t.kind)) {
			statement = ifStatement();
		}else if (getTypeSets("StatementWhile").contains(t.kind)) {
			statement = whileStatement();
		}else if (getTypeSets("StatementShow").contains(t.kind)) {
			statement = showStatement();
		}else if (getTypeSets("StatementSleep").contains(t.kind)) {
			statement = sleepStatement();
		}else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return statement;
	}
	
	public Token type() throws SyntaxException {
		// Type ::=  int  |  float  |  boolean  |  image  |  filename
		Token token = null;
		if (isKind(KW_boolean)) {
			token = consume();
		} else if (isKind(KW_int)) {
			token = consume();
		} else if (isKind(KW_float)) {
			token = consume();
		}else if (isKind(KW_image)) {
			token = consume();
		}else if (isKind(KW_filename)) {
			token = consume();
		}else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return token;
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
	
	public Expression expression() throws SyntaxException {
		//Expression ::= OrExpression  ?  Expression  :  Expression | OrExpression
		Token firstToken = t;
		Expression condition = null;
		Expression e0 = null;
		Expression e1 = null;
		if (getTypeSets("OrExpression").contains(t.kind)) {
			condition = orExpression();
			if (isKind(OP_QUESTION)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					e0 = expression();
					if (isKind(OP_COLON)) {
						consume();
						if (getTypeSets("Expression").contains(t.kind)) {
							e1 = expression();
						}else {
							throw new SyntaxException(t, "Parser found error at" + t.kind);
						}
						condition = new ExpressionConditional(firstToken, condition, e0, e1);
					} else {
						throw new SyntaxException(t, "Parser found error at" + t.kind);
					}
				}else {
					throw new SyntaxException(t, "Parser found error at" + t.kind);
				}
			}
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return condition;
	}
	
	private Expression orExpression() throws SyntaxException {
		// OrExpression ::= AndExpression (  |  AndExpression ) *
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		if (getTypeSets("AndExpression").contains(t.kind)) {
			e0 = andExpression();
			while(isKind(OP_OR)) {
				op = consume();
				if (getTypeSets("AndExpression").contains(t.kind)) {
					e1 = andExpression();
				}else {
					throw new SyntaxException(t, "Parser found error at" + t.kind);
				}
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}	
		return e0;
	}
	
	private Expression andExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		if (getTypeSets("EqExpression").contains(t.kind)) {
			e0 = eqExpression();
			while(isKind(OP_AND)) {
				op = consume();
				if (getTypeSets("EqExpression").contains(t.kind)) {
					e1 = eqExpression();
				}else {
					throw new SyntaxException(t, "Parser found error at" + t.kind);
				}
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
			
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return e0;
	}

	private Expression eqExpression() throws SyntaxException {
		// EqExpression ::= RelExpression ((OP_EQ | OP_NEQ ) RelExpression )*
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		if (getTypeSets("RelExpression").contains(t.kind)) {
			e0 = relExpression();
			while(isKind(OP_EQ) || isKind(OP_NEQ)) {
				op = consume();
				if (getTypeSets("RelExpression").contains(t.kind)) {
					e1 = relExpression();
				}else {
					throw new SyntaxException(t, "Parser found error at" + t.kind);
				}
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return e0;
	}
	
	private Expression relExpression() throws SyntaxException {
		// RelExpression ::= AddExpression ( ( <  |  >  |  <=  |  >=  ) AddExpression)*
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		if (getTypeSets("AddExpression").contains(t.kind)) {
			e0 = addExpression();
			while(isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
				op = consume();
				if (getTypeSets("AddExpression").contains(t.kind)) {
					e1 = addExpression();
				}else {
					throw new SyntaxException(t, "Parser found error at" + t.kind);
				}
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return e0;
	}
	
	private Expression addExpression() throws SyntaxException {
		// AddExpression ::= MultExpression   (  (+ | - ) MultExpression )*
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		if (getTypeSets("MultExpression").contains(t.kind)) {
			e0 = multExpression();
			while(isKind(OP_PLUS) || isKind(OP_MINUS)) {
				op = consume();
				if (getTypeSets("MultExpression").contains(t.kind)) {
					e1 = multExpression();
				}else {
					throw new SyntaxException(t, "Parser found error at" + t.kind);
				}
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return e0;
	}
	
	private Expression multExpression() throws SyntaxException {
		// MultExpression := PowerExpression ( (  *  |  /  |  %  ) PowerExpression )*
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		if (getTypeSets("PowerExpression").contains(t.kind)) {
			e0 = powerExpression();
			while(isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
				op = consume();
				if (getTypeSets("PowerExpression").contains(t.kind)) {
					e1 = powerExpression();
				}else {
					throw new SyntaxException(t, "Parser found error at" + t.kind);
				}
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return e0;
	}
	
	private Expression powerExpression() throws SyntaxException {
		// PowerExpression:= UnaryExpression ( ** PowerExpression|ε)
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		if (getTypeSets("UnaryExpression").contains(t.kind)) {
			e0 = unaryExpression();
			if (isKind(OP_POWER)) {
				op = consume();
				if (getTypeSets("PowerExpression").contains(t.kind)) {
					e1 = powerExpression();
				} else {
					throw new SyntaxException(t, "Parser found error at" + t.kind);
				}
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			} 
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return e0;
	}
	
	private Expression pixelExpression() throws SyntaxException{
		// PixelExpression ::=  IDENTIFIER  PixelSelector
		Expression expression = null;
		PixelSelector ps = null;
		Token firstToken = t;
		Token name = t;
		if (isKind(IDENTIFIER)) {
			consume();
			if (getTypeSets("PixelSelector").contains(t.kind)) {
				ps = pixelSelector();
			}else {
				throw new SyntaxException(t, "Parser found error at" + t.kind);
			}
			expression = new ExpressionPixel(firstToken, name, ps);
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return expression;
	}
	
	private PixelSelector pixelSelector() throws SyntaxException{
//		PixelSelector ::=  [  Expression  ,  Expression  ] 
		 Expression e0 = null;
		 Expression e1 = null;
	     Token firstToken = t;
		if (isKind(LSQUARE)) {
			consume();
				// Expression COMMA Expression   
				if (getTypeSets("Expression").contains(t.kind)) {
					e0 = expression();
					if (isKind(COMMA)) {
						consume();
						if (getTypeSets("Expression").contains(t.kind)) {
							e1 = expression();
							if (isKind(RSQUARE)) {
								consume();
							}else {
								throw new SyntaxException(t,"Parser found error at" + t.kind);
							}
						}else {
							throw new SyntaxException(t,"Parser found error at" + t.kind);
						}
					} else {
						throw new SyntaxException(t,"Parser found error at" + t.kind);
					}
				} else {
						throw new SyntaxException(t,"Parser found error at" + t.kind);
				}
		}else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return new PixelSelector(firstToken, e0, e1);
	}

	private Expression pixelConstructor() throws SyntaxException{
//		PixelConstructor ::=  <<   Expression  ,  Expression ,    Expression  ,  Expression  >>
		Token firstToken = t;
		Expression a = null, r = null, g = null, b = null;
		if (isKind(LPIXEL)) {
			consume();
				// Expression COMMA Expression   
				if (getTypeSets("Expression").contains(t.kind)) {
					a = expression();
					if (isKind(COMMA)) {
						consume();
						if (getTypeSets("Expression").contains(t.kind)) {
							r = expression();
							if (isKind(COMMA)) {
								consume();
								if (getTypeSets("Expression").contains(t.kind)) {
									g = expression();
								if (isKind(COMMA)) {
									consume();
									if (getTypeSets("Expression").contains(t.kind)) {
										b = expression();
										if (isKind(RPIXEL)) {
											consume();
										} else throw new SyntaxException(t, "Parser found error at" + t.kind);
									} else throw new SyntaxException(t, "Parser found error at" + t.kind);
								} else throw new SyntaxException(t, "Parser found error at" + t.kind);
							} else throw new SyntaxException(t, "Parser found error at" + t.kind);
						} else throw new SyntaxException(t, "Parser found error at" + t.kind);
					} else throw new SyntaxException(t, "Parser found error at" + t.kind);
				} else throw new SyntaxException(t, "Parser found error at" + t.kind);
			} else throw new SyntaxException(t, "Parser found error at" + t.kind);
		} else throw new SyntaxException(t, "Parser found error at" + t.kind);
		return new ExpressionPixelConstructor(firstToken, a, r, g, b);
	}
	
	private Token color() throws SyntaxException {
			// Color ::=  red  |  green  |  blue  |  alpha
		Token tok = null;
		if (isKind(KW_red)) {
			tok = consume();
		}else if (isKind(KW_green)) {
			tok = consume();
		}else if (isKind(KW_blue)) {
			tok = consume();
		}else if (isKind(KW_alpha)) {
			tok = consume();
		}else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return tok;
	}
	
	private StatementAssign assignmentStatement() throws SyntaxException{
		// StatementAssignment ::= LHS  :=  Expression
		 Token firstToken = t;
	     LHS lhs = null;
	     Expression expression = null;
		if (getTypeSets("LHS").contains(t.kind)) {
			lhs = lhs();
			if (isKind(OP_ASSIGN)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					expression = expression();
				}else {
					throw new SyntaxException(t,"Parser found error at" + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return new StatementAssign(firstToken, lhs, expression);
	}
	
	private StatementIf ifStatement() throws SyntaxException{
		//  if (  Expression  )  Block
		Token firstToken = t;
		Expression guard = null;
		Block b = null;
		if (isKind(KW_if)) {
			consume();
			if (isKind(LPAREN)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					guard = expression();
					if (isKind(RPAREN)) {
						consume();
						if (getTypeSets("Block").contains(t.kind)) {
							b = block();
						}else {
							throw new SyntaxException(t, "Parser found error at" + t.kind);
						}
					}else {
						throw new SyntaxException(t, "Parser found error at" + t.kind);
					}
				}else {
					throw new SyntaxException(t,"Parser found error at" + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return new StatementIf(firstToken, guard, b);
	}
	
	private StatementWhile whileStatement() throws SyntaxException{
		//  while (  Expression  )  Block
		Token firstToken = t;
		Expression guard = null;
		Block b = null;
		if (isKind(KW_while)) {
			consume();
			if (isKind(LPAREN)) {
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					guard = expression();
					if (isKind(RPAREN)) {
						consume();
						if (getTypeSets("Block").contains(t.kind)) {
							b = block();
						}else {
							throw new SyntaxException(t, "Parser found error at" + t.kind);
						}
					}else {
						throw new SyntaxException(t, "Parser found error at" + t.kind);
					}
				}else {
					throw new SyntaxException(t,"Parser found error at" + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return new StatementWhile(firstToken, guard, b);
	}
	
	private StatementWrite writeStatement() throws SyntaxException{
		//  StatementWrite ::=  write IDENTIFIER to IDENTIFIER TODO
		Token firstToken = t, sourceName = t, destName = t;
		if (isKind(KW_write)) {
			consume();
			if (isKind(IDENTIFIER)) {
				sourceName = consume();
				if (isKind(KW_to)) {
					consume();
					if (isKind(IDENTIFIER)) {
						destName = consume();
					}else {
						throw new SyntaxException(t, "Parser found error at" + t.kind);
					}
				}else {
					throw new SyntaxException(t,"Parser found error at" + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return new StatementWrite(firstToken, sourceName, destName);
	}
	
	private StatementInput inputStatement() throws SyntaxException{
		//  StatementInput ::=  input IDENTIFIER from @  Expression
		Token firstToken = t, destName = t;
		Expression e = null;
		if (isKind(KW_input)) {
			consume();
			if (isKind(IDENTIFIER)) {
				destName = consume();
				if (isKind(KW_from)) {
					consume();
					if (isKind(OP_AT)) {
							consume();
						if (getTypeSets("Expression").contains(t.kind)) {
							e = expression();
						}else {
							throw new SyntaxException(t, "Parser found error at" + t.kind);
						}
					}else {
						throw new SyntaxException(t, "Parser found error at" + t.kind);
					}
				}else {
					throw new SyntaxException(t,"Parser found error at" + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return new StatementInput(firstToken, destName, e);
	}
	
	private StatementShow showStatement() throws SyntaxException{
		// show Expression
		Token firstToken = t;
		Expression e = null;
		if (isKind(KW_show)) {
			consume();
			if (getTypeSets("Expression").contains(t.kind)) {
				e = expression();
			}else {
				throw new SyntaxException(t, "Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return new StatementShow(firstToken, e);
	}
	
	private StatementSleep sleepStatement() throws SyntaxException{
		// sleep Expression
		Token firstToken = t;
		Expression e = null;
		if (isKind(KW_sleep)) {
			consume();
			if (getTypeSets("Expression").contains(t.kind)) {
				e = expression();
			}else {
				throw new SyntaxException(t, "Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return new StatementSleep(firstToken, e);
	}
	
	private LHS lhs() throws SyntaxException {
		// LHS ::=  IDENTIFIER  |  IDENTIFIER  PixelSelector | Color  (   IDENTIFIER  PixelSelector  )
		LHS lhs = null;
        Token firstToken = t, name = t, color = t;
        PixelSelector ps = null;
		if (isKind(IDENTIFIER)) {
			name = consume();
			lhs = new LHSIdent(firstToken, name);
			if (getTypeSets("PixelSelector").contains(t.kind)) {
				ps = pixelSelector();
				lhs = new LHSPixel(firstToken, name, ps);
			}
		} else if (getTypeSets("Color").contains(t.kind)) {
			color = color();
			if (isKind(LPAREN)) {
				consume();
				if (isKind(IDENTIFIER)) {
					name = consume();
					if (getTypeSets("PixelSelector").contains(t.kind)) {
						ps = pixelSelector();
						if (isKind(RPAREN)) {
							consume();
						} else {
							throw new SyntaxException(t,"Parser found error at" + t.kind);
						}
						lhs = new LHSSample(firstToken, name, ps, color);
					} else {
						throw new SyntaxException(t,"Parser found error at" + t.kind);
					}
				} else {
					throw new SyntaxException(t,"Parser found error at" + t.kind);
				}
			}else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return lhs;
	}

	private Expression primary() throws SyntaxException {
		// Primary ::=  INTEGER_LITERAL  |  BOOLEAN_LITERAL  |  FLOAT_LITERAL  |
//		 (  Expression  )  | FunctionApplication |  IDENTIFIER  | PixelExpression | PredefinedName | PixelConstructor
		Expression expression = null;
	    Token firstToken = t;
	    Token name = t;
	    PixelSelector ps = null;
		if (isKind(INTEGER_LITERAL)) {
			name = consume();
			expression = new ExpressionIntegerLiteral(firstToken, name);
		} else if(isKind(BOOLEAN_LITERAL)) {
			name = consume();
			expression = new ExpressionBooleanLiteral(firstToken, name);
		} else if(isKind(FLOAT_LITERAL)) {
			name = consume();
			expression = new ExpressionFloatLiteral(firstToken, name);
		} else if(isKind(LPAREN)) {
			consume();
				if(getTypeSets("Expression").contains(t.kind)) {
					expression = expression();
					if(isKind(RPAREN)) {
						consume();
					} else {
						throw new SyntaxException(t,"Parser found error at" + t.kind);
					}
				} else {
					throw new SyntaxException(t,"Parser found error at" + t.kind);
				}
		} else if (getTypeSets("FunctionApplication").contains(t.kind)) {
			expression = functionApplication();
		} else if (isKind(IDENTIFIER)) {
			name = consume(); 
			expression = new ExpressionIdent(firstToken, name);
			if (getTypeSets("PixelSelector").contains(t.kind)) {
				ps = pixelSelector();
				expression = new ExpressionPixel(firstToken, name, ps);
			}
		} else if (getTypeSets("PredefinedName").contains(t.kind)) {
			expression = predefinedName();
		}  else if (getTypeSets("PixelConstructor").contains(t.kind)) {
			expression = pixelConstructor();
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return expression;
	}
	
	private Expression functionApplication() throws SyntaxException {
//		Token firstToken, Token function, Expression e)
		Expression expression = null;
        Token firstToken = t, func = null;
		if (getTypeSets("FunctionName").contains(t.kind)) {
			func = functionName();
			if (isKind(LPAREN)) {
				Expression arg = null;
				consume();
				if (getTypeSets("Expression").contains(t.kind)) {
					arg = expression();
					if (isKind(RPAREN)) {
						consume();
					}else {
						throw new SyntaxException(t,"Parser found error at" + t.kind);
					}
					expression = new ExpressionFunctionAppWithExpressionArg(firstToken, func, arg);
				}else {
					throw new SyntaxException(t,"Parser found error at" + t.kind);
				}
			}else if (getTypeSets("PixelSelector").contains(t.kind)) { //TODO
				PixelSelector ps = pixelSelector();
				expression = new ExpressionFunctionAppWithPixel(firstToken, func, ps.ex, ps.ey);
			} else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return expression;
	}
	
	private Expression predefinedName() throws SyntaxException {
		// Z | default_height |default_width 
		Token firstToken = t, name = null;
		if (isKind(KW_Z)) {
			name = consume();
		}else if (isKind(KW_default_height)) {
			name = consume();
		}else if (isKind(KW_default_width)) {
			name = consume();
		} else {
			throw new SyntaxException(t, "Parser found error at" + t.kind);
		}
		return new ExpressionPredefinedName(firstToken , name);
	}
	
	private Token functionName() throws SyntaxException {
		// FunctionName ::=  sin  |  cos  |  atan  |  abs  |  log  |  cart_x  |  cart_y  |  polar_a  |  polar_r
//		 int  |  float  |  width  |  height  | Color  TODO
		Token tok = null;
		if (getTypeSets("Color").contains(t.kind)) {
			tok = color();
		} else {
			switch (t.kind) {
				case KW_sin: {
					tok = consume();
				}
				break;
				case KW_int: {
					tok = consume();
				}
				break;
				case KW_float: {
					tok = consume();
				}
				break;
				case KW_width: {
					tok = consume();
				}
				break;
				case KW_height: {
					tok = consume();
				}
				break;
				case KW_cos: {
					tok = consume();
				}
				break;
				case KW_atan: {
					tok = consume();
				}
				break;
				case KW_abs: {
					tok = consume();
				}
					break;
				case KW_log: {
					tok = consume();
				}
					break;
				case KW_cart_x: {
					tok = consume();
				}
					break;
				case KW_cart_y: {
					tok = consume();
				}
					break;
				case KW_polar_a: {
					tok = consume();
				}
					break;
				case KW_polar_r: {
					tok = consume();
				}
				break;
				default:
					throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		}
		return tok;
	}
			
	private Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		// 	public ExpressionUnary(Token firstToken, Token op, Expression expression) {
		// UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary 
		Expression expressionUnary = null;
		Token firstToken = t;
		Token op = null;
		Expression expression = null;
		if (isKind(OP_EXCLAMATION)) {
			op = consume();
			if (getTypeSets("UnaryExpression").contains(t.kind)) {
				expression = unaryExpression();
				expressionUnary = new ExpressionUnary(firstToken, op, expression);
			}else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else if (getTypeSets("Primary").contains(t.kind)) {
			expressionUnary = primary();
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return expressionUnary;
	}

	private Expression unaryExpression() throws SyntaxException {
		// 	public ExpressionUnary(Token firstToken, Token op, Expression expression) {
		Token firstToken = t;
		Token op = null;
		Expression e = null;
		if (isKind(OP_PLUS)) {
			op = consume();
			if (getTypeSets("UnaryExpression").contains(t.kind)) {
				e = unaryExpression();
			} else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		}else if (isKind(OP_MINUS)) {
			op = consume();
			if (getTypeSets("UnaryExpression").contains(t.kind)) {
				e = unaryExpression();
			} else {
				throw new SyntaxException(t,"Parser found error at" + t.kind);
			}
		} else if (getTypeSets("UnaryExpressionNotPlusMinus").contains(t.kind)) {
			return unaryExpressionNotPlusMinus();
		} else {
			throw new SyntaxException(t,"Parser found error at" + t.kind);
		}
		return new ExpressionUnary(firstToken, op, e);
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

