package de.fuberlin.projecta.analysis.ast.nodes;

public abstract class Type extends AbstractSyntaxTree {

	public static final String TYPE_BOOL_STRING = "bool";
	public static final String TYPE_REAL_STRING = "real";
	public static final String TYPE_INT_STRING = "int";
	public static final String TYPE_STRING_STRING = "string";
	public static final String TYPE_VOID_STRING = "void";

	private int valMemory;

	public String genCode() {
		return "";
	}

	public String toTypeString() {
		return "";
	}

	/**
	 * genCode must be called before this is set.
	 * 
	 * @return the memory address, in which the node's value is stored
	 */
	public int getVar() {
		return valMemory;
	}

	public void setValMemory(int valMemory) {
		this.valMemory = valMemory;
	}

	public String fromTypeStringToLLVMType() {
		String type = "";
		if (this.toTypeString().equals(TYPE_INT_STRING))
			type = "i32";
		else if (this.toTypeString().equals(TYPE_REAL_STRING))
			type = "double";
		else if (this.toTypeString().equals(TYPE_BOOL_STRING))
			type = "i1";
		else if (this.toTypeString().equals(TYPE_STRING_STRING))
			type = "i8*";
		return type;
	}
}
