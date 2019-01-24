package cop5556fa18;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPParser.SyntaxException;
import cop5556fa18.PLPScanner.Kind;
import java.util.ArrayList;
import java.util.List;

import cop5556fa18.PLPAST.*;
public class PLPParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}
	
	PLPScanner scanner;
	Token t;

	PLPParser(PLPScanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	
	/*
	 * Program -> Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token first = t;
		Token t1;
		t1 = match(Kind.IDENTIFIER);
		Block b0 = block();
		Program p = new Program(first,t1.getText(),b0);
		return p;
	}
	
	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float , Kind.KW_char , Kind.KW_string /* Complete this */ };
	Kind[] firstStatement = {Kind.KW_if, Kind.KW_while, Kind.KW_sleep, Kind.KW_print , Kind.IDENTIFIER};
	Kind[] firstPrimary = {Kind.INTEGER_LITERAL, Kind.BOOLEAN_LITERAL, Kind.FLOAT_LITERAL, Kind.CHAR_LITERAL , Kind.STRING_LITERAL, Kind.LPAREN, Kind.IDENTIFIER , Kind.KW_abs , Kind.KW_atan , Kind.KW_sin, Kind.KW_cos , Kind.KW_log, Kind.KW_int , Kind.KW_float };
	Kind[] firstFunction = {Kind.KW_abs , Kind.KW_atan , Kind.KW_sin, Kind.KW_cos , Kind.KW_log, Kind.KW_int , Kind.KW_float };
	
	public Block block() throws SyntaxException {
		Token first = t;
		Block b1;
		Declaration d1;
		Statement s1;
		List<PLPASTNode> decsAndStats = new ArrayList<PLPASTNode>();
		match(Kind.LBRACE);
		while (checkKind(firstDec) | checkKind(firstStatement)) {
	     if (checkKind(firstDec)) {
			d1 = declaration();
			decsAndStats.add(d1);
		} else if (checkKind(firstStatement)) {
			s1 = statement();
			decsAndStats.add(s1);
		}
			match(Kind.SEMI);
		}
		match(Kind.RBRACE);
		b1 = new Block(first,decsAndStats);
		return b1;
	}
	
	public Declaration declaration() throws SyntaxException {
		Token first = t;
		Token name,t1;
		Expression e1;
		t1 = type();
		name = match(Kind.IDENTIFIER);
		if (checkKind(Kind.OP_ASSIGN)) {
			match(Kind.OP_ASSIGN);
			e1 = expression();
			Declaration d1 = new VariableDeclaration(first, t1.kind, name.getText(), e1);
			return d1;
		}
		else {
			if(checkKind(Kind.COMMA)) {
				List<String> list1 = new ArrayList<String>();
				list1.add(name.getText());
				while (checkKind(Kind.COMMA)) {
					match(Kind.COMMA);
					name=match(Kind.IDENTIFIER);
					list1.add(name.getText());
				}
				Declaration d2 = new VariableListDeclaration(first, t1.kind, list1);
				return d2;
				
			}
			else {
				Declaration d1 = new VariableDeclaration(first, t1.kind, name.getText(), null);
				return d1;
				
			}
			
			
		}
		
	}
	
	public Statement statement() throws SyntaxException {		
		Statement s;		
		if (checkKind(Kind.KW_if)) {
			s = ifstatement();
		}
		else if(checkKind(Kind.IDENTIFIER)) {
			s = assignmentStatement();
		}
		else if(checkKind(Kind.KW_sleep)) {
			s = sleepStatement();
		}
		else if(checkKind(Kind.KW_print)) {
			
			s = printStatement();
		}
		else if(checkKind(Kind.KW_while)) {
			s = whileStatment();
		}
		else {
			throw new SyntaxException(t,"Syntax Error");
		}
		return s;
	}
	
	public Token type() throws SyntaxException {
		Token t1;
		if (checkKind(Kind.KW_int)) {
			t1 = match(Kind.KW_int);
		}
		else if(checkKind(Kind.KW_float)) {
			t1 = match(Kind.KW_float);
		}
		else if(checkKind(Kind.KW_boolean)) {
			t1 = match(Kind.KW_boolean);
		}
		else if(checkKind(Kind.KW_char)) {
			t1 = match(Kind.KW_char);
		}
		else if(checkKind(Kind.KW_string)) {
			t1 = match(Kind.KW_string);
		}
		else {
			throw new SyntaxException(t,"Syntax Error");
		}	
		return t1;
	}
		
	public Statement ifstatement() throws SyntaxException {
		Token first = t;
		Expression e1;
		Block b1;
		Statement s1;
		match(Kind.KW_if);
		match(Kind.LPAREN);
		e1 = expression();
		match(Kind.RPAREN);
		b1 = block();
		s1 = new IfStatement(first,e1,b1);
		return s1;
		
	}
	
	public Statement assignmentStatement() throws SyntaxException {
		Token first = t;
		Statement s1;
		
		Expression e1;
		LHS l1 = lhs();
		
		match(Kind.OP_ASSIGN);
		e1 = expression();
		s1 = new AssignmentStatement(first, l1, e1);
		return s1;
	}
	
	
	
	public Statement sleepStatement() throws SyntaxException {
		Token first = t;
		Statement s1;
		Expression e1;
		match(Kind.KW_sleep);
		e1 = expression();
		s1 = new SleepStatement(first, e1);
		return s1;
	}
	
	public Statement printStatement() throws SyntaxException {
		Token first = t;
		Statement s1;
		Expression e1;
		match(Kind.KW_print);
		e1 = expression();
		s1 = new PrintStatement(first, e1);
		return s1;
	}
	
	public Statement whileStatment() throws SyntaxException {
		Token first = t;
		Expression e1;
		Block b1;
		Statement s1;
		match(Kind.KW_while);
		match(Kind.LPAREN);
		e1 = expression();
		match(Kind.RPAREN);
		b1 = block();
		s1 = new WhileStatement(first, e1, b1);
		return s1;
	}
	
	public LHS lhs() throws SyntaxException{
		Token first = t;
		LHS l1 = null;
		Token name;
		name = match(Kind.IDENTIFIER);
		l1 = new LHS(first,name.getText());
		return l1;
	}
	
	
	
	public Expression expression() throws SyntaxException {
		Token first = t;
		Expression e1,e2,e3;		
		e1 = or_expression();
		
		if(checkKind(Kind.OP_QUESTION)) {
			match(Kind.OP_QUESTION);
			e2 = expression();
			match(Kind.OP_COLON);
			e3 = expression();
			e1 = new ExpressionConditional(first, e1, e2, e3);
			
		}
		
		return e1;
	}
	
	public Expression or_expression() throws SyntaxException {
		Token first = t;
		Token op;
		Expression e1,e2;
		e1 = and_expression();
		while (checkKind(Kind.OP_OR)) {
			op = match(Kind.OP_OR);
			e2 = and_expression();
			e1 = new ExpressionBinary(first,e1,op.kind,e2);
		}
		
		return e1;
	}
	
	public Expression and_expression() throws SyntaxException {
		Token first = t;
		Token op;
		Expression e1,e2;		
		e1 = eq_expression();
		while (checkKind(Kind.OP_AND)) {
			op = match(Kind.OP_AND);
			e2 = eq_expression();
			e1 = new ExpressionBinary(first, e1, op.kind, e2);
		}
		return e1;
	}
	
	public Expression eq_expression() throws SyntaxException {
		Token first = t;
		Token op;
		Expression e1,e2;
		e1 = rel_expression();
		while (checkKind(Kind.OP_EQ) || checkKind(Kind.OP_NEQ)) {
			if(checkKind(Kind.OP_EQ)) {
				op = match(Kind.OP_EQ);
			}
			else if(checkKind(Kind.OP_NEQ)) {
				 op = match(Kind.OP_NEQ);
			}
			else {
				throw new SyntaxException(t,"Syntax Error");
			}
			
			e2 = rel_expression();
			e1 = new ExpressionBinary(first, e1, op.kind, e2);
			
		}
		return e1;
	}
	
	public Expression rel_expression() throws SyntaxException {
		Token first = t;
		Token op;
		Expression e1,e2;
		e1 = add_expression();
		while (checkKind(Kind.OP_LE) || checkKind(Kind.OP_LT) || checkKind(Kind.OP_GE) || checkKind(Kind.OP_GT)) {
			if(checkKind(Kind.OP_LE)) {
				op = match(Kind.OP_LE);
			}
			else if(checkKind(Kind.OP_LT)) {
				op = match(Kind.OP_LT);
			}
			else if(checkKind(Kind.OP_GE)) {
				op = match(Kind.OP_GE);
			}
			else if(checkKind(Kind.OP_GT)) {
				op = match(Kind.OP_GT);
			}
			else {
				throw new SyntaxException(t,"Syntax Error");
			}
			
			e2 = add_expression();
			e1 = new ExpressionBinary(first, e1, op.kind, e2);
		}
		return e1;
	}
	
	public Expression add_expression() throws SyntaxException {
		Token first = t;
		Token op;
		Expression e1,e2;
		e1 = mult_expression();
		while (checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS)) {
			if(checkKind(Kind.OP_PLUS)) {
				op = match(Kind.OP_PLUS);
			}
			else if(checkKind(Kind.OP_MINUS)) {
				op = match(Kind.OP_MINUS);
			}
			else {
				throw new SyntaxException(t,"Syntax Error");
			}
			
			e2 = mult_expression();
			e1 = new ExpressionBinary(first, e1, op.kind, e2);
		}
		return e1;
	}
	
	public Expression mult_expression() throws SyntaxException {
		Token first = t;
		Token op;
		Expression e1,e2;
		e1 = power_expression();
		while (checkKind(Kind.OP_MOD) || checkKind(Kind.OP_DIV) || checkKind(Kind.OP_TIMES)) {
			if(checkKind(Kind.OP_MOD)) {
				op = match(Kind.OP_MOD);
			}
			else if(checkKind(Kind.OP_DIV)) {
				op = match(Kind.OP_DIV);
			}
			else if(checkKind(Kind.OP_TIMES)) {
				 op = match(Kind.OP_TIMES);
			}
			else {
				throw new SyntaxException(t,"Syntax Error");
			}
			
			e2 = power_expression();
			e1 = new ExpressionBinary(first, e1, op.kind, e2);
		}
		return e1;
	}
	
	public Expression power_expression() throws SyntaxException {
		Token first = t;
		Token op;
		Expression e1,e2;
		e1 = unary_expression();
		if(checkKind(Kind.OP_POWER)) {
			op = match(Kind.OP_POWER);
			e2 = power_expression();
			e1 = new ExpressionBinary(first, e1, op.kind, e2);
		}
		return e1;
	}
	
	public Expression unary_expression() throws SyntaxException {
		Token first = t;
		Token op;
		Expression e1;
		if(checkKind(Kind.OP_PLUS)) {
			op = match(Kind.OP_PLUS);
			e1 = unary_expression();
			e1 = new ExpressionUnary(first, op.kind, e1);
		}
		else if(checkKind(Kind.OP_MINUS)) {
			op = match(Kind.OP_MINUS);
			e1 = unary_expression();
			e1 = new ExpressionUnary(first, op.kind, e1);
		}
		
		else if(checkKind(Kind.OP_EXCLAMATION)) {
			op = match(Kind.OP_EXCLAMATION);
			e1 = unary_expression();
			e1 = new ExpressionUnary(first, op.kind, e1);
		}
		
		else if(checkKind(firstPrimary)) {			
			e1 = primary();
		}
		
		else {
			throw new SyntaxException(t,"Syntax Error");
		}
		
		return e1;
	}
	
	public Expression primary() throws SyntaxException {
		Token first = t;
		Token t1;
		Expression e1;
		
		if(checkKind(Kind.INTEGER_LITERAL)) {
			t1 = match(Kind.INTEGER_LITERAL);
			int val = Integer.parseInt(t1.getText());
			e1 = new ExpressionIntegerLiteral(first, val);
		}
		
		else if(checkKind(Kind.BOOLEAN_LITERAL)) {
			t1 = match(Kind.BOOLEAN_LITERAL);
			boolean val = Boolean.parseBoolean(t1.getText());
			e1 = new ExpressionBooleanLiteral(first, val);
		}
		
		else if(checkKind(Kind.FLOAT_LITERAL)) {
			t1 = match(Kind.FLOAT_LITERAL);
			float val = Float.parseFloat(t1.getText());
			e1 = new ExpressionFloatLiteral(first, val);
		}
		
		else if(checkKind(Kind.CHAR_LITERAL)) {
			t1 = match(Kind.CHAR_LITERAL);
			char val = t1.getText().charAt(1);			
			e1 = new ExpressionCharLiteral(first, val);
		}
		
		else if(checkKind(Kind.STRING_LITERAL)) {
			t1 = match(Kind.STRING_LITERAL);
			String s1 = t1.getText();
			int len1 = s1.length();
			
			e1 = new ExpressionStringLiteral(first, s1.substring(1, len1-1));
		}
		
		else if(checkKind(Kind.LPAREN)) {
			match(Kind.LPAREN);
			e1 = expression();
			match(Kind.RPAREN);
		}
		
		else if(checkKind(Kind.IDENTIFIER)) {
			t1 = match(Kind.IDENTIFIER);
			e1 = new ExpressionIdentifier(first, t1.getText());
		}
		
		else if(checkKind(firstFunction)) {
			e1 = function();			
		}
		
		else {
			throw new SyntaxException(t,"Syntax Error");
		}
		return e1;
	}
	
	public Expression function() throws SyntaxException {
		Token first = t;
		Token t1;
		Expression e1;
		if(checkKind(Kind.KW_sin)) {
			t1 = match(Kind.KW_sin);			
		}
		
		else if(checkKind(Kind.KW_cos)) {
			t1 = match(Kind.KW_cos);			
		}
		
		else if(checkKind(Kind.KW_abs)) {
			t1 = match(Kind.KW_abs);			
		}
		
		else if(checkKind(Kind.KW_atan)) {
			t1 = match(Kind.KW_atan);			
		}
		
		else if(checkKind(Kind.KW_log)) {
			t1 = match(Kind.KW_log);			
		}
		
		else if(checkKind(Kind.KW_int)) {
			t1 = match(Kind.KW_int);			
		}
		
		else if(checkKind(Kind.KW_float)) {
			t1 = match(Kind.KW_float);			
		}
		else {
			throw new SyntaxException(t,"Syntax Error");
		}
		
		match(Kind.LPAREN);
		e1 = expression();
		match(Kind.RPAREN);
		
		e1 = new FunctionWithArg(first, t1.kind, e1);
		return e1;
	}
	
	

	protected boolean checkKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean checkKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	private Token matchEOF() throws SyntaxException {
		if (checkKind(Kind.EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}
	/**
	 * @param kind
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tok;
		
		if (checkKind(kind)) {
			tok=t;
			t = scanner.nextToken();			
		}
		else {
			//TODO  give a better error message!
			throw new SyntaxException(t,"Syntax Error");
		}
		
		return tok;
	}

}

