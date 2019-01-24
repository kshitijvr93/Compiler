package cop5556fa18;

import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPTypes.Type;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import java.util.ArrayList; 
public class PLPCodeGen implements PLPASTVisitor, Opcodes {
	
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	ArrayList<Block> blist = new ArrayList<Block>();
	private int slot=1;
	
	
	public PLPCodeGen(String sourceFileName, boolean dEVEL, boolean gRADE) {
		super();
		this.sourceFileName = sourceFileName;
		DEVEL = dEVEL;
		GRADE = gRADE;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		try {
		
		block.bstart = new Label();
		block.bend = new Label();
		mv.visitLabel(block.bstart);			
		
		blist.add(block);
		
		
		for (PLPASTNode node : block.declarationsAndStatements) {
			node.visit(this, null);
		}
				
		mv.visitLabel(block.bend);
		
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in Block");
		}
		
		
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		try {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		//cw = new ClassWriter(0); 
		// If the call to mv.visitMaxs(1, 1) crashes, it is sometimes helpful 
		// to temporarily run it without COMPUTE_FRAMES. You probably won't 
		// get a completely correct classfile, but you will be able to see the 
		// code that was generated.
		
		className = program.name;
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

		PLPCodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		PLPCodeGenUtils.genLog(DEVEL, mv, "leaving main");
		
		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		
		for(Block b : blist){
			for(PLPASTNode node : b.declarationsAndStatements){
				if(node instanceof VariableDeclaration){
					
					VariableDeclaration dec = (VariableDeclaration)node;	
					
					mv.visitLocalVariable(dec.name, PLPTypes.getJVMTypeDesc(PLPTypes.getType(dec.type)), null, b.bstart, b.bend, dec.current_slot);
					
				}
				
				if(node instanceof VariableListDeclaration){
					VariableListDeclaration dec = (VariableListDeclaration)node;
					int slot_val = dec.current_slot;
					for(String name : dec.names) {
						mv.visitLocalVariable(name, PLPTypes.getJVMTypeDesc(PLPTypes.getType(dec.type)), null, b.bstart, b.bend, slot_val);
						slot_val++;
					}
					
					
				}
				
			}
			
		}
		
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor, asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0,0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();	
		}
		
		catch(Exception e){
			throw new Exception("Error in Main Function");
		}
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		
		try {
			
		declaration.current_slot = slot;			
		slot++;			
		if(declaration.expression != null){
			
			declaration.expression.visit(this, arg);
			switch(declaration.type) {
			
			case KW_int :			
				mv.visitVarInsn(ISTORE,declaration.current_slot);
			break;
			case KW_float :			
				mv.visitVarInsn(FSTORE,declaration.current_slot);
			break;
			case KW_boolean :
				mv.visitVarInsn(ISTORE,declaration.current_slot);
			break;
			case KW_string :			
				mv.visitVarInsn(ASTORE,declaration.current_slot);
			break;
			case KW_char :			
				mv.visitVarInsn(ISTORE,declaration.current_slot);
			break;
			
			default:
				throw new Exception("Error in Declaration");
			
			}
			
			
			
		}
		
			
		
		return null;
		}
		
		catch(Exception e){
			throw new Exception("Error in Declaration");
		}
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		try {
		
		declaration.current_slot = slot;
		int declist_size = declaration.names.size();
		slot= slot + declist_size;			
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in Declaration List");
		}
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		try {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in Function application with expression argument");
		}
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		try {
			
		
		Label ITRUE = new Label();
		Label IFALSE = new Label();
		PLPTypes.Type t0 = expressionBinary.leftExpression.getType();
		PLPTypes.Type t1 = expressionBinary.rightExpression.getType();
		PLPScanner.Kind k1 = expressionBinary.op;
		
		
		if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.OP_PLUS)||k1.equals(PLPScanner.Kind.OP_MINUS)||k1.equals(PLPScanner.Kind.OP_TIMES)||k1.equals(PLPScanner.Kind.OP_DIV)||k1.equals(PLPScanner.Kind.OP_MOD)||k1.equals(PLPScanner.Kind.OP_POWER)||k1.equals(PLPScanner.Kind.OP_AND)||k1.equals(PLPScanner.Kind.OP_OR)) && t1.equals(Type.INTEGER)){
        	
			switch(k1){
        	case OP_PLUS:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(IADD);
        		break;
        	case OP_MINUS:
        		
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(ISUB);
        		break;
        	case OP_TIMES:
        		
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(IMUL);
        		break;
        	case OP_DIV:
        		
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(IDIV);
        		break;
        	case OP_MOD:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(IREM);
        		break;
        	case OP_POWER:
        		expressionBinary.leftExpression.visit(this, arg);
        		mv.visitInsn(I2D);     		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(I2D);
        		
        		mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","pow", "(DD)D", false);
        		mv.visitInsn(D2I);
        		
        		break;
        	case OP_AND:
        		
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(IAND);
        		break;
        	case OP_OR:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(IOR);
        		break;
        	default:        		
        		throw new Exception("Error in Binary Expression");
        	}
        }
       
        else if(t0.equals(Type.FLOAT) && (k1.equals(PLPScanner.Kind.OP_PLUS)||k1.equals(PLPScanner.Kind.OP_MINUS)||k1.equals(PLPScanner.Kind.OP_TIMES)||k1.equals(PLPScanner.Kind.OP_DIV)||k1.equals(PLPScanner.Kind.OP_POWER)) && t1.equals(Type.FLOAT)){
        	        	
        	
        	switch(k1){
        	case OP_PLUS:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FADD);
        		break;
        	case OP_MINUS:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FSUB);
        		break;
        	case OP_TIMES:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FMUL);
        		break;
        	case OP_DIV:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FDIV);
        		break;
        	
        	case OP_POWER:
        		expressionBinary.leftExpression.visit(this, arg);
        		mv.visitInsn(F2D);   
        		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(F2D);   
        		
        		mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","pow", "(DD)D", false);
        		mv.visitInsn(D2F);
        		
        		break;
        	default:
        		throw new Exception("Error in Binary Expression");
        	
        	}
        	
        	
        	
        }
        
        else if(t0.equals(Type.FLOAT) && (k1.equals(PLPScanner.Kind.OP_PLUS)||k1.equals(PLPScanner.Kind.OP_MINUS)||k1.equals(PLPScanner.Kind.OP_TIMES)||k1.equals(PLPScanner.Kind.OP_DIV)||k1.equals(PLPScanner.Kind.OP_POWER)) && t1.equals(Type.INTEGER)){
        	
      
        	switch(k1){
        	case OP_PLUS:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(I2F);
        		mv.visitInsn(FADD);
        		break;
        	case OP_MINUS:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(I2F);
        		mv.visitInsn(FSUB);
        		break;
        	case OP_TIMES:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(I2F);
        		mv.visitInsn(FMUL);
        		break;
        	case OP_DIV:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(I2F);
        		mv.visitInsn(FDIV);
        		break;
        	
        	case OP_POWER:
        		expressionBinary.leftExpression.visit(this, arg);
        		mv.visitInsn(F2D);
        		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(I2D);
        		
        		mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","pow", "(DD)D", false);
        		mv.visitInsn(D2F);
        		
        		break;
        	default:
        		throw new Exception("Error in Binary Expression");
        	}  
        }
		   
        else if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.OP_PLUS)||k1.equals(PLPScanner.Kind.OP_MINUS)||k1.equals(PLPScanner.Kind.OP_TIMES)||k1.equals(PLPScanner.Kind.OP_DIV)||k1.equals(PLPScanner.Kind.OP_POWER)) && t1.equals(Type.FLOAT)){
        	
        	switch(k1){
        	case OP_PLUS:
        		expressionBinary.leftExpression.visit(this, arg);
        		mv.visitInsn(I2F);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FADD);
        		break;
        	case OP_MINUS:
        		expressionBinary.leftExpression.visit(this, arg);
        		mv.visitInsn(I2F);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FSUB);
        		break;
        	case OP_TIMES:
        		expressionBinary.leftExpression.visit(this, arg);
        		mv.visitInsn(I2F);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FMUL);
        		break;
        	case OP_DIV:
        		expressionBinary.leftExpression.visit(this, arg);
        		mv.visitInsn(I2F);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FDIV);
        		break;
        	
        	case OP_POWER:
        		expressionBinary.leftExpression.visit(this, arg);
        		mv.visitInsn(I2D);
        		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(F2D);
        		
        		mv.visitMethodInsn(INVOKESTATIC,"java/lang/Math","pow", "(DD)D", false);
        		mv.visitInsn(D2F);
        		
        		break;
        	default:
        		throw new Exception("Error in Binary Expression");
        	
        	}
        	
              	
        }
        
        else if(t0.equals(Type.BOOLEAN) && (k1.equals(PLPScanner.Kind.OP_AND)||k1.equals(PLPScanner.Kind.OP_OR)||k1.equals(PLPScanner.Kind.OP_EQ)||k1.equals(PLPScanner.Kind.OP_NEQ)||k1.equals(PLPScanner.Kind.OP_GT)||k1.equals(PLPScanner.Kind.OP_GE)||k1.equals(PLPScanner.Kind.OP_LT)||k1.equals(PLPScanner.Kind.OP_LE)) && t1.equals(Type.BOOLEAN)){
        	switch(k1){
        	
        	
        	case OP_AND:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(IAND);
        		break;
        	case OP_OR:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(IOR);
        		break;
        	case OP_EQ:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPNE, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        		break;
        	case OP_NEQ:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPEQ, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        		break;
        	
        	case OP_GT:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPLE, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        	
        		break;
        	case OP_GE:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPLT, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
            	
        		break;
        	case OP_LT:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPGE, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
            	
        		break;
        	case OP_LE:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPGT, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        		break;
        	default:
        		throw new Exception("Error in Binary Expression");
        	
        	}
        	
        	
        	
        	
        	
        }
        else if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.OP_EQ)||k1.equals(PLPScanner.Kind.OP_NEQ)||k1.equals(PLPScanner.Kind.OP_GT)||k1.equals(PLPScanner.Kind.OP_GE)||k1.equals(PLPScanner.Kind.OP_LT)||k1.equals(PLPScanner.Kind.OP_LE)) && t1.equals(Type.INTEGER)){
        	
        	switch(k1){
        	
        	
        	case OP_EQ:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPNE, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        		break;
        	case OP_NEQ:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPEQ, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        		break;
        	
        	case OP_GT:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPLE, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        	
        		break;
        	case OP_GE:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPLT, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
            	
        		break;
        	case OP_LT:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPGE, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
            	
        		break;
        	case OP_LE:
        		expressionBinary.leftExpression.visit(this, arg);
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitJumpInsn(IF_ICMPGT, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        		break;
        	default:
        		throw new Exception("Error in Binary Expression");
        	}	
        	
        	
        	
        	
        	
        	
        }
		
        else if(t0.equals(Type.FLOAT) && (k1.equals(PLPScanner.Kind.OP_EQ)||k1.equals(PLPScanner.Kind.OP_NEQ)||k1.equals(PLPScanner.Kind.OP_GT)||k1.equals(PLPScanner.Kind.OP_GE)||k1.equals(PLPScanner.Kind.OP_LT)||k1.equals(PLPScanner.Kind.OP_LE)) && t1.equals(Type.FLOAT)){
        
        	
        	switch(k1){
        	
        	
        	case OP_EQ:
        		expressionBinary.leftExpression.visit(this, arg);        		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FCMPL);
        		
        		mv.visitJumpInsn(IFNE, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        		break;
        	case OP_NEQ:
        		
        		expressionBinary.leftExpression.visit(this, arg);        		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FCMPL);
        		
        		mv.visitJumpInsn(IFEQ, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        		break;
        	
        	case OP_GT:
        		expressionBinary.leftExpression.visit(this, arg);        		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FCMPL);
        		mv.visitInsn(ICONST_0);
        		mv.visitJumpInsn(IF_ICMPLE, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        	
        		break;
        	case OP_GE:
        		expressionBinary.leftExpression.visit(this, arg);        		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FCMPL);
        		mv.visitInsn(ICONST_0);
        		mv.visitJumpInsn(IF_ICMPLT, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
            	
        		break;
        	case OP_LT:
        		expressionBinary.leftExpression.visit(this, arg);        		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FCMPL);
        		mv.visitInsn(ICONST_0);
        		mv.visitJumpInsn(IF_ICMPGE, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
            	
        		break;
        	case OP_LE:
        		expressionBinary.leftExpression.visit(this, arg);        		
        		expressionBinary.rightExpression.visit(this, arg);
        		mv.visitInsn(FCMPL);
        		mv.visitInsn(ICONST_0);
        		mv.visitJumpInsn(IF_ICMPGT, ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitLabel(IFALSE);
        		break;
        		
        	default:
        		throw new Exception("Error in Binary Expression");
        	
        	}
        	
        	       	
            	
        }
		
        else if(t0.equals(Type.STRING) && (k1.equals(PLPScanner.Kind.OP_PLUS)) && t1.equals(Type.STRING)){
        
        	      	
        	
        	expressionBinary.rightExpression.visit(this, arg);
        	mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        	mv.visitInsn(DUP);
        	expressionBinary.leftExpression.visit(this, arg);
        	mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf","(Ljava/lang/Object;)Ljava/lang/String;",false);
        	mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>","(Ljava/lang/String;)V",false); 
        	expressionBinary.rightExpression.visit(this, arg);
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append","(Ljava/lang/String;)Ljava/lang/StringBuilder;",false); 
        	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
             	
                    	
        }
		
		
        else{
        	throw new Exception("Error in Binary Expression");
        }
      
        return null;
		
		
		}
		catch(Exception e){
			throw new Exception("Error in Binary Expression");
		}
		
		
		
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		try {
		expressionConditional.condition.visit(this, arg);
		Label ITRUE = new Label();		
		Label IFALSE = new Label();
		
		mv.visitJumpInsn(IFEQ, IFALSE);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, ITRUE);
		mv.visitLabel(IFALSE);
		expressionConditional.falseExpression.visit(this, arg);			
		mv.visitLabel(ITRUE);
				
		
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in ExpressionConditional");
		}
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		try {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in ExpressionFloatLiteral");
		}
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		try {
		FunctionWithArg.expression.visit(this, arg);
		 switch(FunctionWithArg.functionName){
		 case KW_sin:			 			 
			 mv.visitInsn(F2D);
			 mv.visitMethodInsn(Opcodes.INVOKESTATIC,"java/lang/Math","sin", "(D)D", false);
			 mv.visitInsn(D2F);				 
			 break;
		 case KW_cos:			
			 mv.visitInsn(F2D);
			 mv.visitMethodInsn(Opcodes.INVOKESTATIC,"java/lang/Math","cos", "(D)D", false);
			 mv.visitInsn(D2F);
			 break;
		 case KW_atan:
			 mv.visitInsn(F2D);
			 mv.visitMethodInsn(Opcodes.INVOKESTATIC,"java/lang/Math","atan", "(D)D", false);
			 mv.visitInsn(D2F);
			 break;
		 case KW_abs:
			 if(FunctionWithArg.expression.getType() == Type.INTEGER){
				 mv.visitInsn(I2D);
				 mv.visitMethodInsn(Opcodes.INVOKESTATIC,"java/lang/Math","abs", "(D)D", false);
				 mv.visitInsn(D2I);
			 }
			 else if(FunctionWithArg.expression.getType() == Type.FLOAT){
				 mv.visitInsn(F2D);
				 mv.visitMethodInsn(Opcodes.INVOKESTATIC,"java/lang/Math","abs", "(D)D", false);
				 mv.visitInsn(D2F);
			 }
			 
			 break;
		 case KW_log:
			 mv.visitInsn(F2D);
			 mv.visitMethodInsn(Opcodes.INVOKESTATIC,"java/lang/Math","log", "(D)D", false);
			 mv.visitInsn(D2F);
			 break;
		 
		 case KW_int:
			 
			 if(FunctionWithArg.expression.getType() == Type.FLOAT){
				 mv.visitInsn(F2I);
			 }
			 
			 break;
		 case KW_float:
			 if(FunctionWithArg.expression.getType() == Type.INTEGER){
				 mv.visitInsn(I2F); 
			 }			 
			 break;
		default:
			throw new Exception("Error in FunctionWithArg");			
		 }
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in FunctionWithArg");
		}
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		//TODO
		try {
			
		if(expressionIdent.dec instanceof VariableListDeclaration) {
			VariableListDeclaration var1 = (VariableListDeclaration)expressionIdent.dec;
			
			int index1 = var1.names.indexOf(expressionIdent.name);
			int slot_val = expressionIdent.dec.current_slot + index1;
			
			switch(expressionIdent.typ){
			case INTEGER:
				mv.visitVarInsn(ILOAD, slot_val);
				break;
			case FLOAT:
				mv.visitVarInsn(FLOAD, slot_val);
				break;
			case BOOLEAN:
				mv.visitVarInsn(ILOAD, slot_val);
				break;
			case STRING:
				mv.visitVarInsn(ALOAD, slot_val);
				break;
			case CHAR:
				mv.visitVarInsn(ILOAD, slot_val);
				break;
			default:
				throw new Exception("Error in Expression Ident");
			
			}	
			
		}
		else if(expressionIdent.dec instanceof VariableDeclaration) {
			switch(expressionIdent.typ){
			case INTEGER:
				mv.visitVarInsn(ILOAD, expressionIdent.dec.current_slot);
				break;
			case FLOAT:
				mv.visitVarInsn(FLOAD, expressionIdent.dec.current_slot);
				break;
			case BOOLEAN:
				mv.visitVarInsn(ILOAD, expressionIdent.dec.current_slot);
				break;
			case STRING:
				mv.visitVarInsn(ALOAD, expressionIdent.dec.current_slot);
				break;
			case CHAR:
				mv.visitVarInsn(ILOAD, expressionIdent.dec.current_slot);
				break;
			default:
				throw new Exception("Error in Expression Ident");
			
			}	
			
		}
			
		
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in Expression Ident");
		}
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		try {		
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in ExpressionIntegerLiteral");
		}
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		try {
		mv.visitLdcInsn(expressionStringLiteral.text);
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in ExpressionStringLiteral");
		}
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		try {		
		mv.visitLdcInsn(expressionCharLiteral.text);		
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in ExpressionCharLiteral");
		}
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		try {
		
		statementAssign.expression.visit(this, arg);		
		statementAssign.lhs.visit(this, arg);
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in AssignmentStatement");
		}
		
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO
		
		try {
		if(lhs.dec instanceof VariableListDeclaration) {
			VariableListDeclaration var1 = (VariableListDeclaration)lhs.dec;
			
			int index1 = var1.names.indexOf(lhs.identifier);
			int slot_val = lhs.dec.current_slot + index1;			
			switch(lhs.dec.type) {
			
			case KW_int :			
				mv.visitVarInsn(ISTORE,slot_val);
			break;
			case KW_float :						
				mv.visitVarInsn(FSTORE,slot_val);
			break;
			case KW_boolean :
				mv.visitVarInsn(ISTORE,slot_val);
			break;
			case KW_string :			
				mv.visitVarInsn(ASTORE,slot_val);
			break;
			case KW_char :
				
				mv.visitVarInsn(ISTORE,slot_val);
			break;
			
			default:
				throw new Exception("Error in LHSIdent");
			
			}		
			
		}
		else if(lhs.dec instanceof VariableDeclaration) {
			
			switch(lhs.dec.type) {
			
			case KW_int :			
				mv.visitVarInsn(ISTORE,lhs.dec.current_slot);
			break;
			case KW_float :					
				mv.visitVarInsn(FSTORE,lhs.dec.current_slot);
			break;
			case KW_boolean :
				mv.visitVarInsn(ISTORE,lhs.dec.current_slot);
			break;
			case KW_string :			
				mv.visitVarInsn(ASTORE,lhs.dec.current_slot);
			break;
			case KW_char :			
				mv.visitVarInsn(ISTORE,lhs.dec.current_slot);
			break;
			
			default:
				throw new Exception("Error in LHSIdent");
			
			}
		}
		
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in LHS");
		}
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		try {
		ifStatement.condition.visit(this, arg);
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		ifStatement.block.visit(this, arg);
		mv.visitLabel(l1);
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in IF");
		}
		
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		try {
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l1);		
		mv.visitLabel(l2);
		whileStatement.b.visit(this, arg);
		mv.visitLabel(l1);
		whileStatement.condition.visit(this, arg);
		mv.visitJumpInsn(IFNE, l2);
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in While");
		}
		
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		try {
				
		printStatement.expression.visit(this, arg);
		

		Type type = printStatement.expression.getType();
		switch (type) {
		case INTEGER : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(I)V", false);
		}
		break;
		case BOOLEAN : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Z)V", false);
			
		}
		break;
		
		case FLOAT : {
			
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(F)V", false);
		}
		break;
		case CHAR : {
			
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(C)V", false);
		}
		break; 
		case STRING : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Ljava/lang/String;)V", false);
		}
		default:
			break;

		}
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in Print");
		}
		
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		try {
		sleepStatement.time.visit(this, arg);
		
		if(sleepStatement.time.typ == Type.INTEGER){
			
			mv.visitInsn(I2L);
			
		}
		else if(sleepStatement.time.typ == Type.FLOAT){
			mv.visitInsn(F2L);
		}
		
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in Sleep");
		}
		
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		try {
		PLPTypes.Type t0 = expressionUnary.expression.getType() ;
		PLPScanner.Kind k0 = expressionUnary.op;
		expressionUnary.expression.visit(this, arg);
		if(k0.equals(PLPScanner.Kind.OP_EXCLAMATION)){
			switch(t0){
			case INTEGER :
				mv.visitInsn(ICONST_M1);
				mv.visitInsn(IXOR);
				break;
			case BOOLEAN :
				Label ITRUE = new Label();
				Label IFALSE = new Label();
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(IF_ICMPNE, ITRUE);
        		mv.visitInsn(ICONST_0);
        		mv.visitJumpInsn(GOTO, IFALSE);
        		mv.visitLabel(ITRUE);
        		mv.visitInsn(ICONST_1);
        		mv.visitLabel(IFALSE);
				
				break;
			default:
				throw new Exception("Error in Unary Expression");
				
			}
		}
		else if(k0.equals(PLPScanner.Kind.OP_MINUS)){
			switch(t0){
			case INTEGER :
				mv.visitInsn(INEG);
				break;
			case FLOAT :
				mv.visitInsn(FNEG);
				break;
			default:
				throw new Exception("Error in Unary Expression");
						
			}
		}		
		return null;
		}
		catch(Exception e){
			throw new Exception("Error in Unary");
		}
	}

}
