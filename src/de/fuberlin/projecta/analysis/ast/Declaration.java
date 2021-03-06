package de.fuberlin.projecta.analysis.ast;

import de.fuberlin.projecta.analysis.SemanticException;
import de.fuberlin.projecta.analysis.SymbolTableStack;

/**
 * Declaration consists of two child nodes
 * 
 * First child: Type node
 * Second child: Id node
 */
public class Declaration extends AbstractSyntaxTree {

	@Override
	public void buildSymbolTable(SymbolTableStack tables) {
		Type type = (Type) getChild(0);
		Id id = (Id) getChild(1);		
		tables.top().insertEntry(id	, type );
		if(type instanceof Record){
			type.buildSymbolTable(tables);
		}
	}

	@Override
	public void checkTypes() {
		if (getType().toTypeString().equals(BasicType.TYPE_VOID_STRING)) {
			throw new SemanticException("Variable cannot be from type void", this);
		}

		for (int i = 0; i < getChildrenCount(); ++i)
			((AbstractSyntaxTree)getChild(i)).checkTypes();
	}

	@Override
	public String genCode() {
		String ret = "";
		ret += "%" + ((Id) getChild(1)).genCode() + " = alloca "
				+ ((Type) getChild(0)).genCode();
		return ret;
	}

	public Type getType() {
		return (Type)getChild(0);
	}

	public Id getId() {
		return (Id)getChild(1);
	}
}
