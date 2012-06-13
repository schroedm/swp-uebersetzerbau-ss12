package de.fuberlin.projecta.analysis.ast.nodes;

import de.fuberlin.projecta.analysis.SymbolTableStack;
import de.fuberlin.projecta.lexer.TokenType;


public class UnaryOp extends AbstractSyntaxTree {
	
	TokenType op;
	
	public UnaryOp(TokenType op){
		this.op = op;
	}
	
	public void buildSymbolTable(SymbolTableStack tables) {
		//Is this correct? first child is type, second id?
		//Yep, this should be correct xD
		tables.top().insertEntry((Id) getChild(1), (Type) getChild(0));
	}

	@Override
	public boolean checkSemantics() {
		return true;
	}

	@Override
	public String genCode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public TokenType getOp() {
		return this.op;
	}
}
