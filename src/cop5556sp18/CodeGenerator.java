/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

import cop5556sp18.CodeGenUtils;
import cop5556sp18.Scanner.Kind;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */
	FieldVisitor fv;
	static String fieldName;
	static String fieldType;
	static Object initValue;
	private int slot = 1;

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		Label beginBlock = new Label();
		mv.visitLabel(beginBlock);
		Label endBlock = new Label();
		for (ASTNode node : block.decsOrStatements) {
			node.visit(this, null);
		}
		mv.visitLabel(endBlock);
		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		fieldName = declaration.name;
		Type fieldType = declaration.typeName;
		declaration.setSlotNum(slot++);
		if(fieldType == Type.IMAGE) {
			if(declaration.width != null && declaration.height != null) {
			/* visit the Expressions to generate code to evaluate them and leave their value on the stack. 
			 * Then generate code to instantiate an image (invoke RuntimeImageSupport.makeImage)*/

			declaration.width.visit(this, arg);
			declaration.height.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeImage", RuntimeImageSupport.makeImageSig, false);

//			mv.visitFieldInsn(PUTSTATIC, className, fieldName, "I");
			} else if(declaration.width == null && declaration.height == null) {
				// TODO
				mv.visitFieldInsn(GETSTATIC, className, String.valueOf(defaultWidth), "I");
				mv.visitFieldInsn(GETSTATIC, className, String.valueOf(defaultHeight), "I");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeImage", RuntimeImageSupport.makeImageSig, false);
			}
		
			FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, "Ljava/awt/image/BufferedImage;", null, null);
//			mv.visitFieldInsn(PUTSTATIC, className, declaration.name, RuntimeImageSupport.ImageDesc);

			fv.visitEnd();
		}
		return declaration;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Label labelStart = new Label(), labelEnd = new Label();

		if (expressionBinary.leftExpression != null) expressionBinary.leftExpression.visit(this, arg);
		if (expressionBinary.rightExpression != null) expressionBinary.rightExpression.visit(this, arg);
			
		if (expressionBinary.op == Kind.OP_PLUS) {
			mv.visitInsn(IADD);	
		} else if (expressionBinary.op == Kind.OP_MINUS) {
			mv.visitInsn(ISUB);
		}  else if (expressionBinary.op == Kind.OP_TIMES) {
			mv.visitInsn(IMUL);
		} else if (expressionBinary.op == Kind.OP_DIV) {
			mv.visitInsn(IDIV);
		} else if (expressionBinary.op == Kind.OP_MOD) {
			mv.visitInsn(IREM);
		}else if (expressionBinary.op == Kind.OP_AND) {
			mv.visitInsn(IAND);	
		} else if (expressionBinary.op == Kind.OP_OR) {
			mv.visitInsn(IOR);
		}  else if (expressionBinary.op == Kind.OP_EQ) {
			mv.visitJumpInsn(IF_ICMPEQ, labelStart);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitLdcInsn(1);
			mv.visitLabel(labelEnd);
		}else if (expressionBinary.op == Kind.OP_LT) {
			mv.visitJumpInsn(IF_ICMPLT, labelStart);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitLdcInsn(1);
			mv.visitLabel(labelEnd);
		} else if (expressionBinary.op == Kind.OP_NEQ) {
			mv.visitJumpInsn(IF_ICMPNE, labelStart);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitLdcInsn(1);
			mv.visitLabel(labelEnd);
		} else if (expressionBinary.op == Kind.OP_GE) {
			mv.visitJumpInsn(IF_ICMPGE, labelStart);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitLdcInsn(1);
			mv.visitLabel(labelEnd);
		} else if (expressionBinary.op == Kind.OP_LE) {
			mv.visitJumpInsn(IF_ICMPLE, labelStart);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitLdcInsn(1);
			mv.visitLabel(labelEnd);
		} else if (expressionBinary.op == Kind.OP_GT ) {
			mv.visitJumpInsn(IF_ICMPGT, labelStart);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitLdcInsn(1);
			mv.visitLabel(labelEnd);	
		} else if (expressionBinary.op == Kind.OP_LT) {
			mv.visitJumpInsn(IF_ICMPLT, labelStart);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitLdcInsn(1);
			mv.visitLabel(labelEnd);	
		} else if (expressionBinary.op == Kind.OP_POWER) {
			// TODO 
			/*Implement POWER by converting both arguments to double, invoking java/lang/Math.pow, 
			 * and converting the result back to the appropriate type. 8?
			 */
			mv.visitJumpInsn(IF_ICMPLT, labelStart);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitLdcInsn(1);
			mv.visitLabel(labelEnd);	
		}
		CodeGenUtils.genLogTOS(GRADE, mv, expressionBinary.typeName);
		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "parseDouble", "(Ljava/lang/String;)D", false);
//		mv.visitFieldInsn(PUTSTATIC, className, expressionFunctionAppWithExpressionArg.e.name, "Z");
		if (expressionFunctionAppWithExpressionArg.function == Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(F)F", false);
		} else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
		} else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_sin) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(F)F", false);
		} else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_cos) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(F)F", false);
		} else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_atan) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(F)F", false);
		}
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		if (expressionUnary.expression != null) expressionUnary.expression.visit(this, arg);

		if (expressionUnary.op == Kind.OP_PLUS) {
			//mv.visitInsn(IADD); //TODO
		} 
		else if (expressionUnary.op == Kind.OP_MINUS) {
			mv.visitInsn(INEG);
		} 
		else if (expressionUnary.op == Kind.OP_EXCLAMATION) {
			if (expressionUnary.expression.typeName == Type.INTEGER) {
				mv.visitLdcInsn(INTEGER.MAX_VALUE);
				mv.visitInsn(IXOR);
			} else if (expressionUnary.expression.typeName == Type.BOOLEAN) {
				Label startLabel = new Label();
				Label endLabel = new Label();
				mv.visitJumpInsn(IFEQ, endLabel);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, startLabel);
				mv.visitLabel(endLabel);
				mv.visitLdcInsn(1);
				mv.visitLabel(startLabel);
			}
		}
		CodeGenUtils.genLogTOS(GRADE, mv, expressionUnary.typeName);
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		/* If the type is image, the value on top of the stack is actually a reference. Instead of copying the reference, a copy 
		 * of the image should be created and the reference to the copy stored. Use RuntimeImageSupport.deepCopy to copy the image.*/
		CodeGenUtils.genPrintTOS(GRADE, mv, lhsIdent.typeName);
		if(lhsIdent.dec.typeName == Type.IMAGE) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "deepCopy", RuntimeImageSupport.deepCopySig, false);
			mv.visitFieldInsn(PUTSTATIC, className, lhsIdent.name, RuntimeImageSupport.ImageDesc);
		} else {
			mv.visitFieldInsn(PUTSTATIC, className, lhsIdent.name, "Ljava/lang/String;");
		}
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		if (statementAssign.lhs.typeName == Type.INTEGER) {
			CodeGenUtils.genPrintTOS(GRADE, mv, Type.INTEGER);
			mv.visitFieldInsn(PUTSTATIC, className, statementAssign.lhs.name, "I");
		} else if (statementAssign.lhs.typeName == Type.BOOLEAN) {
			mv.visitFieldInsn(PUTSTATIC, className, statementAssign.lhs.name, "Z");
		}
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		statementInput.e.visit(this, arg);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESTATIC, "Ljava/lang/String;", "parseInt", "(Ljava/lang/String;)I", false);
		mv.visitFieldInsn(PUTSTATIC, className, statementInput.destName, "I");
		
		/* TODO If the the type is image, the parameter is a url or file, and the image should be read from its location (invoke 
		 * RuntimeImageSupport.readImage). If a size was specified when the image variable was declared, the image should be resized 
		 * to this value. Otherwise, the image retains its original size.*/
		if(statementInput.e.typeName == Type.IMAGE) {
			statementInput.dec.visit(this, arg);
//				mv.visitFieldInsn(PUTSTATIC, className,declaration_SourceSink.name, "Ljava/lang/String;");
				if(statementInput.dec.width != null) {
					statementInput.dec.width.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;", false);
					statementInput.dec.height.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;", false);
				}else {
					mv.visitInsn(ACONST_NULL);	
				    mv.visitInsn(ACONST_NULL);
				}
				
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18.ImageSupport", "readImage", RuntimeImageSupport.readImageSig, false);
				mv.visitFieldInsn(PUTSTATIC, className, statementInput.destName, RuntimeImageSupport.ImageDesc);// TODO dec name or 
		}
	
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
			}
				break;
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
			}
			// break; commented out because currently unreachable. You will need
			// it.
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false);
			}
			// break; commented out because currently unreachable. You will need
			// it.
			case IMAGE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				//TODO check correctness
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeFrame", RuntimeImageSupport.makeFrameSig, false);
			}

		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		/*The value of the expression is the number of msecs that the program should sleep. Use java.lang.Thread.sleep.*/
		statementSleep.duration.visit(this, null);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
