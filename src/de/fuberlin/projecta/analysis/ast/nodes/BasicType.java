package de.fuberlin.projecta.analysis.ast.nodes;

import de.fuberlin.projecta.analysis.SymbolTableStack;
import de.fuberlin.projecta.lexer.BasicTokenType;

public class BasicType extends Type {

	BasicTokenType type;

	public BasicType(BasicTokenType t){
		this.type = t;
	}
	
	public void buildSymbolTable(SymbolTableStack tables) {

	}

	@Override
	public String genCode() {
		String ret = "";
		switch (type) {
		case INT:
			ret += "i32";
			break;
		case REAL:
			ret += "double";
			break;
		case STRING:
			ret += "i8*";
			break;
		case BOOL:
			ret += "i8";
			break;
		}
		return ret;
	}

	public BasicTokenType getType() {
		return type;
	}

}
