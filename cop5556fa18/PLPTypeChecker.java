package cop5556fa18;

import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPTypes;
import cop5556fa18.PLPTypes.Type;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
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
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.Symbol_Table;
public class PLPTypeChecker implements PLPASTVisitor {
	
	Symbol_Table stable = new Symbol_Table();
	
	PLPTypeChecker() {
	}
	
	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	// Name is only used for naming the output file. 
		// Visit the child block to type check program.
		@Override
		public Object visitProgram(Program program, Object arg) throws Exception {
			program.block.visit(this, arg);
			return null;
		}
		
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		
		stable.enterScope();
		for(PLPASTNode node : block.declarationsAndStatements){
			node.visit(this, arg);
		}
		
		
		stable.leaveScope();
		return null;
		
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		
		boolean bool = stable.insert(declaration.name,declaration);
		if(bool){
			if(declaration.expression != null) {
				Type t0 = (Type)declaration.expression.visit(this, arg);				
				if(t0.equals(PLPTypes.getType(declaration.type))) {
					
					
				}
				else {
					throw new SemanticException(declaration.firstToken,"Error in declaration");
				}
			}
			
			
			
		}
		else{
			throw new SemanticException(declaration.firstToken,"Error in declaration");
		}
		
		return null;
		
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		
		for(String name: declaration.names) {
			boolean bool = stable.insert(name,declaration);
			if(!bool){
				throw new SemanticException(declaration.firstToken,"Error in declaration");
				
			}			
			
		}
		
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		
		expressionBooleanLiteral.typ = Type.BOOLEAN;
		return expressionBooleanLiteral.typ; 
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		
		Type t0 = (Type) expressionBinary.leftExpression.visit(this, arg);
		Type t1 = (Type) expressionBinary.rightExpression.visit(this, arg);
		PLPScanner.Kind k1 = expressionBinary.op;
		if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.OP_PLUS)||k1.equals(PLPScanner.Kind.OP_MINUS)||k1.equals(PLPScanner.Kind.OP_TIMES)||k1.equals(PLPScanner.Kind.OP_DIV)||k1.equals(PLPScanner.Kind.OP_MOD)||k1.equals(PLPScanner.Kind.OP_POWER)||k1.equals(PLPScanner.Kind.OP_AND)||k1.equals(PLPScanner.Kind.OP_OR)) && t1.equals(Type.INTEGER)){
        	expressionBinary.typ = Type.INTEGER;
        }
       
        else if(t0.equals(Type.FLOAT) && (k1.equals(PLPScanner.Kind.OP_PLUS)||k1.equals(PLPScanner.Kind.OP_MINUS)||k1.equals(PLPScanner.Kind.OP_TIMES)||k1.equals(PLPScanner.Kind.OP_DIV)||k1.equals(PLPScanner.Kind.OP_POWER)) && t1.equals(Type.FLOAT)){
        	expressionBinary.typ = Type.FLOAT;
        }
        
        else if(t0.equals(Type.FLOAT) && (k1.equals(PLPScanner.Kind.OP_PLUS)||k1.equals(PLPScanner.Kind.OP_MINUS)||k1.equals(PLPScanner.Kind.OP_TIMES)||k1.equals(PLPScanner.Kind.OP_DIV)||k1.equals(PLPScanner.Kind.OP_POWER)) && t1.equals(Type.INTEGER)){
        	expressionBinary.typ = Type.FLOAT;
        }
        
        else if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.OP_PLUS)||k1.equals(PLPScanner.Kind.OP_MINUS)||k1.equals(PLPScanner.Kind.OP_TIMES)||k1.equals(PLPScanner.Kind.OP_DIV)||k1.equals(PLPScanner.Kind.OP_POWER)) && t1.equals(Type.FLOAT)){
        	expressionBinary.typ = Type.FLOAT;
        }
        
        else if(t0.equals(Type.BOOLEAN) && (k1.equals(PLPScanner.Kind.OP_AND)||k1.equals(PLPScanner.Kind.OP_OR)||k1.equals(PLPScanner.Kind.OP_EQ)||k1.equals(PLPScanner.Kind.OP_NEQ)||k1.equals(PLPScanner.Kind.OP_GT)||k1.equals(PLPScanner.Kind.OP_GE)||k1.equals(PLPScanner.Kind.OP_LT)||k1.equals(PLPScanner.Kind.OP_LE)) && t1.equals(Type.BOOLEAN)){
        	expressionBinary.typ = Type.BOOLEAN;
        }
		
        else if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.OP_AND)||k1.equals(PLPScanner.Kind.OP_OR)) && t1.equals(Type.INTEGER)){
        	expressionBinary.typ = Type.INTEGER;
        }
		
        else if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.OP_EQ)||k1.equals(PLPScanner.Kind.OP_NEQ)||k1.equals(PLPScanner.Kind.OP_GT)||k1.equals(PLPScanner.Kind.OP_GE)||k1.equals(PLPScanner.Kind.OP_LT)||k1.equals(PLPScanner.Kind.OP_LE)) && t1.equals(Type.INTEGER)){
        	expressionBinary.typ = Type.BOOLEAN;
        }
       
        else if(t0.equals(Type.FLOAT) && (k1.equals(PLPScanner.Kind.OP_EQ)||k1.equals(PLPScanner.Kind.OP_NEQ)||k1.equals(PLPScanner.Kind.OP_GT)||k1.equals(PLPScanner.Kind.OP_GE)||k1.equals(PLPScanner.Kind.OP_LT)||k1.equals(PLPScanner.Kind.OP_LE)) && t1.equals(Type.FLOAT)){
        	expressionBinary.typ = Type.BOOLEAN;
        }
		
        else if(t0.equals(Type.STRING) && (k1.equals(PLPScanner.Kind.OP_PLUS)) && t1.equals(Type.STRING)){
        	expressionBinary.typ = Type.STRING;
        }
        else{
        	throw new SemanticException(expressionBinary.firstToken,"Illegal Operation");
        }
		
		return expressionBinary.typ;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		
		Type t0 = (Type) expressionConditional.condition.visit(this, arg);
		Type t1 = (Type) expressionConditional.trueExpression.visit(this, arg);
		Type t2 = (Type) expressionConditional.falseExpression.visit(this, arg);
		if( (t0 == Type.BOOLEAN) && (t1==t2)) {
			expressionConditional.typ = t1;
		}
		else {
			throw new SemanticException(expressionConditional.firstToken,"Illegal Operation");
		}
		
		return expressionConditional.typ;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.typ = Type.FLOAT;
		return expressionFloatLiteral.typ;
		
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		Type t0 = (Type)FunctionWithArg.expression.visit(this, arg);
		PLPScanner.Kind k1 = FunctionWithArg.functionName;
		
		if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.KW_abs) ) ){
			FunctionWithArg.typ = Type.INTEGER;
			return FunctionWithArg.typ;
		}
		
		else if(t0.equals(Type.FLOAT) && (k1.equals(PLPScanner.Kind.KW_abs) || k1.equals(PLPScanner.Kind.KW_sin) || k1.equals(PLPScanner.Kind.KW_cos) || k1.equals(PLPScanner.Kind.KW_atan) ||k1.equals(PLPScanner.Kind.KW_log) ) ){
			FunctionWithArg.typ = Type.FLOAT;
			return FunctionWithArg.typ;
		}
		
		
		else if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.KW_float)) ){
			FunctionWithArg.typ = Type.FLOAT;
			return FunctionWithArg.typ;
		}
		
		else if(t0.equals(Type.FLOAT) && (k1.equals(PLPScanner.Kind.KW_float)) ){
			FunctionWithArg.typ = Type.FLOAT;
			return FunctionWithArg.typ;
		}
		
		else if(t0.equals(Type.FLOAT) && (k1.equals(PLPScanner.Kind.KW_int)) ){
			FunctionWithArg.typ = Type.INTEGER;
			return FunctionWithArg.typ;
		}
		
		else if(t0.equals(Type.INTEGER) && (k1.equals(PLPScanner.Kind.KW_int)) ){
			FunctionWithArg.typ = Type.INTEGER;
			return FunctionWithArg.typ;
		}
		
		else{
			throw new SemanticException(FunctionWithArg.firstToken,"Illegal Operation");
			
		}
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		Declaration d = stable.lookup(expressionIdent.name);
		if(d ==null){
			throw new SemanticException(expressionIdent.firstToken,"Illegal Operation");
		}
		expressionIdent.dec = d;
		expressionIdent.typ = PLPTypes.getType(expressionIdent.dec.type);
		return expressionIdent.typ;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.typ = Type.INTEGER;
		return expressionIntegerLiteral.typ;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		expressionStringLiteral.typ = Type.STRING;
		return expressionStringLiteral.typ;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		expressionCharLiteral.typ = Type.CHAR;
		
		return expressionCharLiteral.typ;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		
		Type e1 = (Type) statementAssign.expression.visit(this, arg);
		
		Type l1 = (Type) statementAssign.lhs.visit(this, arg);		
		if( e1 != l1 ) {
			throw new SemanticException(statementAssign.firstToken,"Illegal Operation");
		}
		
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Type t0  = (Type) ifStatement.condition.visit(this, arg);
		if( t0 != Type.BOOLEAN ) {
			throw new SemanticException(ifStatement.firstToken,"Illegal Operation");
		}
		ifStatement.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Type t0  = (Type) whileStatement.condition.visit(this, arg);
		if( t0 != Type.BOOLEAN ) {
			throw new SemanticException(whileStatement.firstToken,"Illegal Operation");
		}
		whileStatement.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		Type t0  = (Type) printStatement.expression.visit(this, arg);
		if( t0 != Type.BOOLEAN && t0 != Type.INTEGER && t0 != Type.FLOAT && t0 != Type.STRING && t0 != Type.CHAR ) {
			throw new SemanticException(printStatement.firstToken,"Illegal Operation");
		}
		
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Type t0  = (Type) sleepStatement.time.visit(this, arg);
		if( t0 != Type.INTEGER ) {
			throw new SemanticException(sleepStatement.firstToken,"Illegal Operation");
		}
		
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		
		expressionUnary.typ = (Type) expressionUnary.expression.visit(this, arg);
		PLPScanner.Kind k1 =  expressionUnary.op;
		
		if((k1.equals(PLPScanner.Kind.OP_EXCLAMATION)) && (expressionUnary.typ.equals(Type.INTEGER) || expressionUnary.typ.equals(Type.BOOLEAN) )  ) {
			
		}		
		else if(     ( k1.equals(PLPScanner.Kind.OP_PLUS) || k1.equals(PLPScanner.Kind.OP_MINUS)  ) && (    expressionUnary.typ.equals(Type.INTEGER) || expressionUnary.typ.equals(Type.FLOAT)   ) ) {
			
		}
		else {
			throw new SemanticException(expressionUnary.firstToken,"Illegal Operation");
		}
		
		return expressionUnary.typ;
		
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		Declaration d = stable.lookup(lhs.identifier);
		lhs.dec = d;
		if( lhs.dec == null) {
			throw new SemanticException(lhs.firstToken,"Illegal Operation");
		}
		lhs.typ = PLPTypes.getType(lhs.dec.type);
		return lhs.typ;
	}

}
