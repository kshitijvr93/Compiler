package cop5556fa18.PLPAST;

import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;

public abstract class Declaration extends PLPASTNode {
	public Kind type;
	public int current_slot;
	public Declaration(Token firstToken) {
		super(firstToken);
	}
}
