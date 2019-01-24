package cop5556fa18;

import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import cop5556fa18.PLPAST.*;
public class Symbol_Table 
{
	
	public Symbol_Table() 
	{
		this.c_scope = 0;
		this.n_scope = 1;
		scope_stack.push(0);
	}
	
	
	HashMap <String, ArrayList<Pair>> hashm = new HashMap <String, ArrayList<Pair>>();
	int  c_scope, n_scope;
	Stack <Integer>scope_stack = new Stack<Integer>();
	

	/**
	 * leaves scope
	 */
	public void leaveScope()
	{
		
		scope_stack.pop();
		c_scope = scope_stack.peek();
	}
	
	

	/** 
	 * to be called when block entered enter scope
	 */
	public void enterScope()
	{
		c_scope = n_scope++; 
		scope_stack.push(c_scope);
	}
	
	public class Pair 
	{

		  int scope;
		  Declaration dec;
		  public Pair(int s, Declaration d)
		  {
			  this.scope = s;
			  this.dec = d;
		  }
		  
		  public Declaration getDec()
		  {
			  return dec;
		  }
		  
		  public int getScope()
		  {
			  return scope;
		  }
	}
	
	public boolean insert(String ident, Declaration dec)
	{
		ArrayList<Pair> ps = new ArrayList<Pair>();
		Pair p = new Pair(c_scope, dec);
		if(hashm.containsKey(ident))
		{
			ps = hashm.get(ident);
			for(Pair it: ps)
			{
				if(it.getScope()==c_scope)
					return false;
			}
		}
		ps.add(p);
		hashm.put(ident, ps);		
		return true;
	}
	
	public Declaration lookup(String ident)
	{
		if(!hashm.containsKey(ident)){
			return null;
		}	
			
		
		Declaration dec=null;
		ArrayList<Pair> pairlist = hashm.get(ident);
		for(int i=pairlist.size()-1;i>=0;i--)
		{
			int temp_scope = pairlist.get(i).getScope();
			if(scope_stack.contains(temp_scope))
			{
				dec = pairlist.get(i).getDec();
				break;
			}
		}
		return dec;
	}
		
	
	@Override
	public String toString() 
	{
		return this.toString();
	}
	
	
}
