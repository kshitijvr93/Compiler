package cop5556fa18.PLPAST;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes.Type;

public abstract class Expression extends PLPASTNode {
	public Type typ;
	public Expression(Token firstToken) {
		super(firstToken);
	}	
	
	public Type getType() { 
		return typ; 
	} 

}
