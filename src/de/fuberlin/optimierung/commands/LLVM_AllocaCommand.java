package de.fuberlin.optimierung.commands;

import de.fuberlin.optimierung.ILLVM_Block;
import de.fuberlin.optimierung.ILLVM_Command;
import de.fuberlin.optimierung.LLVM_Operation;
import de.fuberlin.optimierung.LLVM_Optimization;
import de.fuberlin.optimierung.LLVM_Parameter;

/*
 * Syntax:

  <result> = alloca <type>[, <ty> <NumElements>][, align <alignment>]     ; yields {type*}:result
 */

public class LLVM_AllocaCommand extends LLVM_GenericCommand{
	
	public LLVM_AllocaCommand(String cmdLine, ILLVM_Command predecessor, ILLVM_Block block){
		super(predecessor, block, cmdLine);
		setOperation(LLVM_Operation.ALLOCA);
		
		String[] cmd = cmdLine.split(" ");
		// <result> <type>
		target = new LLVM_Parameter(cmd[0], cmd[3]);
		
		// optionale Parameter
		for (int j = 4; (j + 1 < cmd.length); j = j + 2){
			// <ty> <num>
			operands.add(new LLVM_Parameter(cmd[j+1], cmd[j]));
		}
		
		if (LLVM_Optimization.DEBUG) System.out.println("Operation generiert: " + this.toString());
	}
	
	public String toString() {
		String cmd_output = target.getName() + " = ";
		
		switch(operation){
			case ALLOCA :
				cmd_output += "alloca ";
				break;
			default:
				return "";
		}
		
		cmd_output += target.getTypeString();
		
		for (int i = 0; i < operands.size(); i++){
			cmd_output += ", " + operands.get(i).getTypeString() + " ";
			cmd_output += operands.get(i).getName();
		}
		
		cmd_output += " " + getComment();

		return cmd_output;
	}
}
