package cop5556sp18;

import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.Types.Type;

import java.util.List;

import cop5556sp18.AST.*;

public class TypeChecker implements ASTVisitor {


	TypeChecker() {
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	SymbolTable symbolTable = new SymbolTable();
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		List<ASTNode> decsOrStatements = block.decsOrStatements;
		symbolTable.enterScope();
		for(ASTNode node : decsOrStatements) {
//			ASTNode node = decsOrStatements.get(i);
//			System.out.println(node.toString());
			node.visit(this, null);
		}
		symbolTable.leaveScope();
		return block;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		Kind kind = declaration.type;
		Type type = Types.getType(kind);
		declaration.typeName = type;
		if(symbolTable.lookup(declaration.name) != null)
			throw new SemanticException(declaration.firstToken, "Duplicate indent declaration");
		Expression e0 = null, e1 = null;
		if(declaration.width != null) {
			e0 = (Expression) declaration.width.visit(this, null);
//			System.out.println(e0.typeName);
		}
		if(declaration.height != null) {
			e1 = (Expression) declaration.height.visit(this, null);
//			System.out.println(e1.typeName);
		}
		if((e0 != null && (e0.typeName == Type.INTEGER && type == Type.IMAGE)) &&
			(e1 != null && (e1.typeName == Type.INTEGER && type == Type.IMAGE)) ||
			(e0 == null && e1 == null)) {
		}
		else throw new SemanticException(declaration.firstToken, "Type mismatch");
		symbolTable.insert(declaration.name, declaration);
		return declaration;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		/* 
		 * StatementWrite.sourceDec ← symbolTable.lookup(StatementWrite.sourceName) 
		 * StatementWrite.sourceDec != null 
		 * StatementWrite.destDec ← symbolTable.lookup(StatementWrite.destName) 
		 * StatementWrite.destDec != null
		 * sourceDec.type == image*/
		statementWrite.srcDec = symbolTable.lookup(statementWrite.sourceName);
		statementWrite.destDec = symbolTable.lookup(statementWrite.destName);
		if (statementWrite.srcDec == null || statementWrite.destDec == null 
				|| statementWrite.srcDec.typeName != Type.IMAGE || statementWrite.destDec.typeName != Type.FILE) {
			throw new SemanticException(statementWrite.firstToken, "Illegal type at visit ident chain");
		}
		return statementWrite;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		/* StatementInput.destName ← IDENTIFIER.name 
		 * StatementInput.dec ← SymbolTable.lookup(StatementInput.destName) 
		 * StatementInput.dec != null
		 * Expression.type ==integer*/
		Expression e = (Expression) statementInput.e.visit(this, null);
		statementInput.dec = symbolTable.lookup(statementInput.destName);
		if (statementInput.dec == null || e.typeName != Type.INTEGER) {
			throw new SemanticException(statementInput.firstToken, "Illegal type at visit ident chain");
		}
		return statementInput;
		
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Expression e0 = (Expression) pixelSelector.ex.visit(this, null);
		Expression e1 = (Expression) pixelSelector.ey.visit(this, null);
		if(e0.typeName != e1.typeName) throw new SemanticException(pixelSelector.firstToken, "Type mismatch");
		if(e0.typeName == Type.INTEGER || e0.typeName == Type.FLOAT) {}	
		else throw new SemanticException(pixelSelector.firstToken, "Type mismatch");
		return pixelSelector;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		Expression e0 = (Expression) expressionConditional.guard.visit(this, null);
		Expression e1 = (Expression) expressionConditional.trueExpression.visit(this, null);
		Expression e2 = (Expression) expressionConditional.falseExpression.visit(this, null);
		
		if(e0.typeName == Type.BOOLEAN && (e1.typeName == e2.typeName)) {
		} else {
			throw new SemanticException(expressionConditional.firstToken, "Type mismatch");
		}
		expressionConditional.typeName = e1.typeName;
		return expressionConditional;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Expression e0 = (Expression) expressionBinary.leftExpression.visit(this, null);
		Expression e1 = (Expression) expressionBinary.rightExpression.visit(this, null);
		Kind op = expressionBinary.op;
		if (e0.typeName == Type.INTEGER && e1.typeName == Type.INTEGER && (op == Kind.OP_PLUS || op == Kind.OP_MINUS 
				|| op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_MOD || op == Kind.OP_POWER 
				|| op == Kind.OP_AND || op == Kind.OP_OR)) {
			expressionBinary.typeName = (Type.INTEGER);
		} else if (e0.typeName == Type.FLOAT && e1.typeName == Type.FLOAT
				&& (op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_POWER)) {
			expressionBinary.typeName = (Type.FLOAT);
		} else if (e0.typeName == Type.FLOAT && e1.typeName == Type.INTEGER
				&& (op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_POWER)) {
			expressionBinary.typeName = (Type.FLOAT);
		} else if (e0.typeName == Type.INTEGER && e1.typeName == Type.FLOAT
				&& (op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_POWER)) {
			expressionBinary.typeName = (Type.FLOAT);
		} else if (e0.typeName == Type.BOOLEAN && e1.typeName == Type.BOOLEAN
				&& (op == Kind.OP_AND || op == Kind.OP_OR)) {
			expressionBinary.typeName = (Type.BOOLEAN);
		} else if (e0.typeName == Type.INTEGER && e1.typeName == Type.INTEGER
				&& (op == Kind.OP_AND || op == Kind.OP_OR)) {
			expressionBinary.typeName = (Type.INTEGER);
		} else if (e0.typeName == Type.INTEGER && e1.typeName == Type.INTEGER
				&& (op == Kind.OP_EQ || op == Kind.OP_NEQ ||op == Kind.OP_GT || op == Kind.OP_LT || op == Kind.OP_LE || op == Kind.OP_GE)) {
			expressionBinary.typeName = (Type.BOOLEAN);
		} else if (e0.typeName == Type.FLOAT && e1.typeName == Type.FLOAT
				&& (op == Kind.OP_EQ || op == Kind.OP_NEQ ||op == Kind.OP_GT || op == Kind.OP_LT || op == Kind.OP_LE || op == Kind.OP_GE)) {
			expressionBinary.typeName = (Type.BOOLEAN);
		}else if (e0.typeName == Type.BOOLEAN && e1.typeName == Type.BOOLEAN
				&& (op == Kind.OP_EQ || op == Kind.OP_NEQ ||op == Kind.OP_GT || op == Kind.OP_LT || op == Kind.OP_LE || op == Kind.OP_GE)) {
			expressionBinary.typeName = (Type.BOOLEAN);
		} else throw new SemanticException(expressionBinary.firstToken, "Illegal Binary Expression Type");
		return expressionBinary;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		Expression e = (Expression) expressionUnary.expression.visit(this, null);
		expressionUnary.typeName = e.typeName;
		return expressionUnary;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.typeName = Type.INTEGER;
		return expressionIntegerLiteral;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.typeName = Type.BOOLEAN;
		return expressionBooleanLiteral;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		expressionPredefinedName.typeName = Type.INTEGER;
		return expressionPredefinedName;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.typeName = Type.FLOAT;
		return expressionFloatLiteral;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		Expression e0 = (Expression) expressionFunctionAppWithExpressionArg.e.visit(this, null);
		Kind op = expressionFunctionAppWithExpressionArg.function;		
		if (e0.typeName == Type.INTEGER && (op == Kind.KW_red || op == Kind.KW_green 
				|| op == Kind.KW_blue || op == Kind.KW_abs || op == Kind.KW_alpha)) {
			expressionFunctionAppWithExpressionArg.typeName = (Type.INTEGER);
		} else if (e0.typeName == Type.FLOAT && (op == Kind.KW_sin || op == Kind.KW_cos 
				|| op == Kind.KW_log || op == Kind.KW_abs || op == Kind.KW_atan)) {
			expressionFunctionAppWithExpressionArg.typeName = (Type.FLOAT);
		} else if (e0.typeName == Type.IMAGE && (op == Kind.KW_width || op == Kind.KW_height)) {
			expressionFunctionAppWithExpressionArg.typeName = (Type.INTEGER);
		} else if (e0.typeName == Type.INTEGER && (op == Kind.KW_float)) {
			expressionFunctionAppWithExpressionArg.typeName = (Type.FLOAT);
		} else if (e0.typeName == Type.FLOAT && (op == Kind.KW_float)) {
			expressionFunctionAppWithExpressionArg.typeName = (Type.FLOAT);
		} else if (e0.typeName == Type.FLOAT && (op == Kind.KW_int)) {
			expressionFunctionAppWithExpressionArg.typeName = (Type.INTEGER);
		}  else if (e0.typeName == Type.INTEGER && (op == Kind.KW_int)) {
			expressionFunctionAppWithExpressionArg.typeName = (Type.INTEGER);
		} else throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken, "Illegal Binary Expression Type");
		return expressionFunctionAppWithExpressionArg;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		Kind name = (Kind) expressionFunctionAppWithPixel.name;
		Expression e0 = (Expression) expressionFunctionAppWithPixel.e0.visit(this, null);
		Expression e1 = (Expression) expressionFunctionAppWithPixel.e1.visit(this, null);
		if(name == Kind.KW_cart_x || name == Kind.KW_cart_y) {
			if(e0.typeName == Type.FLOAT && e1.typeName == Type.FLOAT ) {
			} else throw new SemanticException(expressionFunctionAppWithPixel.firstToken, "Type mismatch");
			expressionFunctionAppWithPixel.typeName = Type.INTEGER;
		} else if(name == Kind.KW_polar_a || name == Kind.KW_polar_r) {
			if(e0.typeName == Type.INTEGER && e1.typeName == Type.INTEGER ) {
			} else throw new SemanticException(expressionFunctionAppWithPixel.firstToken, "Type mismatch");
			expressionFunctionAppWithPixel.typeName = Type.FLOAT;
		} else throw new SemanticException(expressionFunctionAppWithPixel.firstToken, "Type mismatch");
		return expressionFunctionAppWithPixel;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		Expression alpha = (Expression) expressionPixelConstructor.alpha.visit(this, null);
		Expression red = (Expression) expressionPixelConstructor.red.visit(this, null);
		Expression green = (Expression) expressionPixelConstructor.green.visit(this, null);
		Expression blue = (Expression) expressionPixelConstructor.blue.visit(this, null);
		if(alpha.typeName != Type.INTEGER || red.typeName != Type.INTEGER || green.typeName != Type.INTEGER || blue.typeName != Type.INTEGER) {
			throw new SemanticException(expressionPixelConstructor.firstToken, "Type mismatch");
		}
		expressionPixelConstructor.typeName = Type.INTEGER;
		return expressionPixelConstructor;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		LHS lhs = (LHS) statementAssign.lhs.visit(this, null);
		Expression expr = (Expression) statementAssign.e.visit(this, null);
//		Type lhsType = null;
		if(lhs.typeName != expr.getTypeName()) {
			throw new SemanticException(statementAssign.firstToken, "Type mismatch: statementAssign.lhs.typeName != expr.typeName");
		}
		return statementAssign;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		Expression e = (Expression) statementShow.e.visit(this, null);
		if (e.typeName == Type.BOOLEAN || e.typeName == Type.INTEGER || e.typeName == Type.FLOAT || e.typeName == Type.IMAGE) {
		} else throw new SemanticException(statementShow.firstToken, "Illegal type in Show statement");
		return statementShow;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		/*
		 * ExpressionPixel.dec ← SymbolTable.lookup(ExpressionPixel.name) 
		 * ExpressionPixel.dec != null 
		 * ExpressionPixel.dec.type == image 
		 * ExpressionPixel.type ← integer*/
		expressionPixel.dec = symbolTable.lookup(expressionPixel.name);
		if (expressionPixel.dec != null) {
		} else {
			throw new SemanticException(expressionPixel.firstToken, "Illegal type at visit ident chain");
		}
		if(expressionPixel.dec.typeName != Type.IMAGE) 			
			throw new SemanticException(expressionPixel.firstToken, "Illegal type at visit ident chain");
		expressionPixel.typeName = Type.INTEGER;
		return expressionPixel;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		/*
		 * ExpressionIdent.dec ← SymbolTable.lookup(ExpressionIdent.name) ExpressionIdent.dec != null
			ExpressionIdent.type ← ExpressionIdent.dec.type
	*/
		expressionIdent.dec = symbolTable.lookup(expressionIdent.name);
		if (expressionIdent.dec != null) {
		} else {
			throw new SemanticException(expressionIdent.firstToken, "Illegal type at visit ident chain");
		}
		expressionIdent.typeName = expressionIdent.dec.typeName;
		return expressionIdent;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		/*
			LHSSample.dec ← SymbolTable.lookup(LHSSample.name) 
			LHSSample.dec != null
			LHSSample.dec.type == image
			LHSSample.type ← integer*/
		lhsSample.dec = symbolTable.lookup(lhsSample.name);
		lhsSample.pixelSelector.visit(this, null);
		if (lhsSample.dec == null || lhsSample.dec.typeName != Type.IMAGE) 			
			throw new SemanticException(lhsSample.firstToken, "Illegal type at visit ident chain");
		lhsSample.typeName = Type.INTEGER;
		return lhsSample;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		/*
			LHSPixel.dec ← SymbolTable.lookup(LHSPixel.name) 
			LHSPixel.dec != null
			LHSPixel.dec.type == image
			LHSPixel.type ← integer*/
		lhsPixel.dec = symbolTable.lookup(lhsPixel.name);
		lhsPixel.pixelSelector.visit(this, null);
		if (lhsPixel.dec != null) {
		} else {
			throw new SemanticException(lhsPixel.firstToken, "Illegal type at visit ident chain");
		}
		if(lhsPixel.dec.typeName != Type.IMAGE) 			
			throw new SemanticException(lhsPixel.firstToken, "Illegal type at visit ident chain");
		lhsPixel.typeName = Type.INTEGER;
		return lhsPixel;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		/*
		 * LHSIdent.dec ←SymbolTable.lookup(LHSIdent.name) 
		 * LHSIdent.dec != null
		 * LHSIdent.type ← LHSIdent.dec.type*/
		lhsIdent.dec = symbolTable.lookup(lhsIdent.name);
		if (lhsIdent.dec != null) {
		} else {
			throw new SemanticException(lhsIdent.firstToken, "Illegal type at visit ident chain");
		}
		lhsIdent.typeName = lhsIdent.dec.typeName;
		return lhsIdent;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		Expression e = (Expression) statementIf.guard.visit(this, null);
		if (e.typeName != Type.BOOLEAN)
			throw new SemanticException(statementIf.firstToken, "Illegal type in If statement");
		statementIf.b.visit(this, null);
		return statementIf;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		Expression e = (Expression) statementWhile.guard.visit(this, null);
		if (e.typeName != Type.BOOLEAN)
			throw new SemanticException(statementWhile.firstToken, "Illegal type in While statement");
		statementWhile.b.visit(this, null);
		return statementWhile;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		Expression e = (Expression) statementSleep.duration.visit(this, null);
		if (e.typeName != Type.INTEGER)
			throw new SemanticException(statementSleep.firstToken, "Illegal type in Sleep statement");
		return statementSleep;
	}

}
