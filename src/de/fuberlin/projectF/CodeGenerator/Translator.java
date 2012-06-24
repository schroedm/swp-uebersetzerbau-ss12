package de.fuberlin.projectF.CodeGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fuberlin.projectF.CodeGenerator.model.Token.Parameter;

import de.fuberlin.projectF.CodeGenerator.model.MMXRegisterAddress;
import de.fuberlin.projectF.CodeGenerator.model.RegisterAddress;
import de.fuberlin.projectF.CodeGenerator.model.Token;
import de.fuberlin.projectF.CodeGenerator.model.TokenType;
import de.fuberlin.projectF.CodeGenerator.model.Variable;

public class Translator {
	
	private MemoryManager mem;
	protected Assembler asm;
	ArrayList<Token> code;

	public Translator(String assembler) {
		mem = new MemoryManager();
		if(assembler.equals("gnu"))
			asm = new GNUAssembler();
		else if(assembler.equals("intel"))
			asm = new IntelAssembler();
		else
			return;
	}

	public void translate(ArrayList<Token> code) {
		this.code = code;
		int tokenNumber = 0;
		for (Token tok : code) {
			
			String op1, op2;
			RegisterAddress res;
			MMXRegisterAddress mmxRes;
			MMXRegisterAddress mmxRes2;

			switch (tok.getType()) {
			case Definition:
				// Neuen Variablenkontext anlegen
				String name = tok.getTarget().substring(1);
				mem.newContext(name);
				mem.setContext(name);
				// Deklaration
				
				asm.funcDec(name);
				
				// Parameterbehandlung
				if (tok.getParameterCount() > 0) {
					for (int i = tok.getParameterCount() - 1; i >= 0; i--) {
						Parameter p = tok.getParameter(i);
						mem.addStackVar(p.getOperand(), p.getType(), 8 + i
								* getSize(p.getType()));
					}
				}
				
				break;

			case Return:
				// Kein Rückgabewert oder bereits in %eax
				if (tok.getTypeOp1().equals("void")
						|| mem.inReg(tok.getOp1(), 0))
					break;
				// Variable zurückgeben
				if (tok.getOp1().startsWith("%")) {
					if(tok.getTypeOp1().equals("double"))
						asm.movsd(mem.getAddress(tok.getOp1()), "%xmm0", "Return value");
					else
						asm.movl(mem.getAddress(tok.getOp1()), "%eax", "Return value");
					
				}
				// Fester Wert
				else {
					if(tok.getTypeOp1().equals("double"))
						asm.movl("$" + tok.getOp1(), "%xmm0", "Return Value");
					else
						asm.movl("$" + tok.getOp1(), "%eax", "Return Value");
				}
				break;

			case DefinitionEnd:
				asm.funcEnd();
				break;

			case Call:
				String function = tok.getOp1().substring(1);
				
				// Variablen, die nur in Registern sind, auf dem Stack speichern
				List<Variable> regVars = mem.getRegVariables(true);
				for (Variable var : regVars) {
					mem.regToStack(var);
					// Stackpointer verschieben
					asm.subl("$" + String.valueOf(var.getSize()), "%esp", "Move var to stack");
				}
				// Alle Register sind nun frei und werden möglicherweise in der
				// Aufgerufenen Funktion verwendet.
				// Parameter auf den Stack legen
				if (tok.getParameterCount() > 0) {
					for (int i = tok.getParameterCount() - 1; i >= 0; i--) {
						Parameter p = tok.getParameter(i);
						String operand;
						System.out.println(p.getOperand());
						if (p.getOperand().startsWith("%"))
							operand = mem.getAddress(p.getOperand());
						else
							operand = "$" + p.getOperand();
						
						
						if (operand.charAt(1) == '@')
							operand = "$" + operand.substring(3);
						
						if(p.getType().equals("double"))
							asm.pushl(mem.getAddress(p.getOperand(), 4), "Parameter " + p.getOperand());
						asm.pushl(operand, "Parameter " + p.getOperand());
					}
				}
				// Funktionsaufruf
				asm.call(function);

				// Rückgabe speichern
				if (tok.getTypeTarget().equals("i32")) {
					mem.addRegVar(tok.getTarget(), tok.getTypeTarget(), mem.getFreeRegister(0));
				}
				else if (tok.getTypeTarget().equals("double")) {
					System.out.println("Register " + tok.getTarget());
					mmxRes = new MMXRegisterAddress(0);
					System.out.println("To: " + mmxRes.getFullName());
					mem.addMMXRegVar(tok.getTarget(), tok.getTypeTarget(), mmxRes);
				}
				// Parameter löschen
				for (int i = 0; i < tok.getParameterCount(); i++) {
					Parameter p = tok.getParameter(i);
					asm.addl("$" + String.valueOf(getSize(p.getType())), "%esp", "Dismiss Parameter");
				}
				break;

			case Allocation:
				// Array
				String tT = tok.getTypeTarget();
				if  (tT.startsWith("[")){
					// Extrahieren der Array-Größen
					ArrayList<Integer> numbers = new ArrayList<Integer>();
					Pattern p = Pattern.compile("(\\d+)(\\sx)");
					Matcher m = p.matcher(tT); 
					while (m.find()) {
					   numbers.add(new Integer(m.group(1)));
					}
					
					// Extrahieren des Typs
					p = Pattern.compile("(i)(\\d+)");
					m = p.matcher(tT);
					m.find();
					String type = m.group();
					System.out.println(type);
					
					// Länge berechnen
					int length = 1;
					for (Integer i : numbers) {
						length *= i;
					}
					Variable newArr = mem.newArrayVar(tok.getTarget(), type, length);
					asm.subl("$" + String.valueOf(newArr.getSize()), "%esp",
							"Allocation " + tok.getTarget());
				}
				// Kein Arryay
				else{
				// Neue Variable anlegen
				Variable newVar = mem.newStackVar(tok.getTarget(),
						tT);
				// Stackpointer verschieben
				asm.subl("$" + String.valueOf(newVar.getSize()), "%esp",
						"Allocation " + tok.getTarget());
				}
				break;

			case Assignment:
				String target = mem.getAddress(tok.getTarget());
				String source;
				// Zuweisung Variable
					
					// Zuweisung Zahl
					if (!tok.getOp1().startsWith("%")) {
						if(tok.getTypeTarget().equals("i32*"))
							asm.movl("$" + tok.getOp1(), target, "Assignment " + tok.getTarget());
						else if(tok.getTypeTarget().equals("double*")) {
							String target2 = mem.getAddress(tok.getTarget(), +4);
							asm.movl("$" + tok.getOp1().substring(0,10), target2, "Assignment " + tok.getTarget());
							asm.movl("$0x" + tok.getOp1().substring(10), target, "Assignment " + tok.getTarget());
						}
					}
					
					// Variable (Stack -> Stack)
					else if (mem.onStack(tok.getOp1()) && mem.onStack(tok.getTarget())) {
						
						if(tok.getTypeOp1().equals("double")) {
							MMXRegisterAddress tmp = mem.getFreeMMXRegister();
							asm.movsd(mem.getAddress(tok.getOp1()), tmp.getFullName(), "Copy assignment");
							asm.movsd(tmp.getFullName(), target, tok.getTarget() + tok.getOp1());
						} else {
							RegisterAddress tmp = mem.getFreeRegister();
							asm.movl(mem.getAddress(tok.getOp1()), tmp.getFullName(), "Copy assignment");
							asm.movl(tmp.getFullName(), target, tok.getTarget() + tok.getOp1());
							mem.freeRegister(tmp);
						}
						// Variable
					} else {
						source = mem.getAddress(tok.getOp1());
						System.out.println("Source: " + source);
						if(tok.getTypeTarget().equals("double*"))
							asm.movsd(source, target, "Assignment double " + tok.getTarget());
						else
							asm.movl(source, target, "Assignment i32 " + tok.getTarget());
					}

				break;

			case Load:
				mem.newVirtualVar(tok.getTarget(), tok.getOp1());
				break;

			case ExpressionInt:
				res = mem.getFreeRegister();
				if (res == null) {
					if (!freeUnusedRegister(tokenNumber))
						System.out.println("Could'nt free register");
					res = mem.getFreeRegister();
				}

				if (tok.getOp1().startsWith("%"))
					op1 = mem.getAddress(tok.getOp1());
				else
					op1 = "$" + tok.getOp1();
				if (tok.getOp2().startsWith("%"))
					op2 = mem.getAddress(tok.getOp2());
				else
					op2 = "$" + tok.getOp2();

				asm.movl(op1, res.getFullName(), "Expression");
				if (tok.getTypeTarget().equals("or"))
					asm.orl(op2, res.getFullName(), tok.getOp1() + " + " + tok.getOp2());
				
				else if (tok.getTypeTarget().equals("and"))
					asm.andl(op2, res.getFullName(), tok.getOp1() + " + " + tok.getOp2());
				
				else if (tok.getTypeTarget().equals("xor"))
					asm.xorl(op2, res.getFullName(), tok.getOp1() + " + " + tok.getOp2());
				
				else if (tok.getTypeTarget().equals("add"))
					asm.addl(op2, res.getFullName(), tok.getOp1() + " + " + tok.getOp2());
				
				else if (tok.getTypeTarget().equals("sub"))
					asm.subl(op2, res.getFullName(), tok.getOp1() + " - " + tok.getOp2());
				
				else if (tok.getTypeTarget().equals("mul"))
					asm.imull(op2, res.getFullName(), tok.getOp1() + " * " + tok.getOp2());
				
				else if (tok.getTypeTarget().equals("sdiv")) {
					if (!isRegisterFree(new RegisterAddress(0))) {
						System.out.println("Register eax is not free");
						saveRegisterValue(new RegisterAddress(0));
					}
					if (!isRegisterFree(new RegisterAddress(3))) {
						System.out.println("Register edx is not free");
						saveRegisterValue(new RegisterAddress(3));
					}
					asm.movl(op1, new RegisterAddress(0).getFullName(), "");
					asm.movl(new String("$0"),
							new RegisterAddress(3).getFullName(), "");

					asm.idivl(op2);
					res = new RegisterAddress(0);
				}
				mem.addRegVar(tok.getTarget(), "i32*", res);
				break;

			case ExpressionDouble:

				if (tok.getOp1().startsWith("%"))
					op1 = mem.getAddress(tok.getOp1());
				else
					op1 = "$" + tok.getOp1();
				if (tok.getOp2().startsWith("%"))
					op2 = mem.getAddress(tok.getOp2());
				else
					op2 = "$" + tok.getOp2();

				//if(!op1.startsWith("%xmm")) {
					mmxRes = mem.getFreeMMXRegister();
					if (mmxRes == null) {
						if (!freeUnusedMMXRegister(tokenNumber)) {
							System.out.println("Could'nt free register");
						}
						mmxRes = mem.getFreeMMXRegister();
					}
					mem.addMMXRegVar(tok.getOp1(), tok.getTypeOp1(), mmxRes);
					asm.movsd(op1, mmxRes.getFullName(), "Expression");
				//}
				
				mmxRes2 = mem.getFreeMMXRegister();
				if (mmxRes2 == null) {
					if (!freeUnusedMMXRegister(tokenNumber)) {
						System.out.println("Could'nt free register");
					}
					mmxRes2 = mem.getFreeMMXRegister();
				}
				asm.movsd(op2, mmxRes2.getFullName(), "Expression");
				mem.addMMXRegVar(tok.getOp2(), tok.getTypeOp2(), mmxRes2);
				
				if (tok.getTypeTarget().equals("fadd"))
					asm.addsd(mmxRes2.getFullName(), mmxRes.getFullName(), tok.getOp1() + " + " + tok.getOp2());
				else if (tok.getTypeTarget().equals("fsub"))
					asm.subsd(mmxRes2.getFullName(), mmxRes.getFullName(), tok.getOp1() + " - " + tok.getOp2());
				else if (tok.getTypeTarget().equals("fmul"))
					asm.mulsd(mmxRes2.getFullName(), mmxRes.getFullName(), tok.getOp1() + " * " + tok.getOp2());
				else if (tok.getTypeTarget().equals("fdiv")) {
					asm.divsd(mmxRes2.getFullName(), mmxRes.getFullName(), tok.getOp1() + " * " + tok.getOp2());
				}
				
				mem.addMMXRegVar(tok.getTarget(), "double*", mmxRes);
				
				break;

			case Cast:
				if(tok.getTypeTarget().equals("i32") && tok.getTypeOp1().equals("double")) {
					System.out.println("Op1: " + tok.getOp1());
					op1 = mem.getAddress(tok.getOp1());
					System.out.println("Address:" + op1);
					System.out.println(op1);
					
					//TODO wenn wert noch nicht in mmx register
					if(!(op1.charAt(0) == '%')) {
						mmxRes = mem.getFreeMMXRegister();
						asm.movss(op1, mmxRes.getFullName(), "Convert to single precision");
						op1 = mmxRes.getFullName();
					} else {
						asm.cvtsd2ss(op1,op1, "Convert to single precision");
					}
					
					res = mem.getFreeRegister();
					if (res == null) {
						if (!freeUnusedRegister(tokenNumber)) {
							System.out.println("Could'nt free register");
						}
						res = mem.getFreeRegister();
					}
					
					asm.cvttss2si(op1,res.getFullName(), "Convert to integer");
					
					mem.addRegVar(tok.getTarget(), "i32*", res);

					
				} else if (tok.getTypeTarget().equals("double") && tok.getTypeOp1().equals("i32")) {
					op1 = mem.getAddress(tok.getOp1());
					mmxRes = mem.getFreeMMXRegister();
					if (mmxRes == null) {
						if (!freeUnusedMMXRegister(tokenNumber)) {
							System.out.println("Could'nt free register");
						}
						mmxRes = mem.getFreeMMXRegister();
					}
					asm.cvtsi2sd(op1, mmxRes.getFullName(), "Cast");
					mem.addMMXRegVar(tok.getTarget(), "double*", mmxRes);
				} else {
					System.out.println("Cast Error");
				}
					
				break;
			case Label:
				asm.label(tok.getTarget());
				break;

			case Compare:
				res = mem.getFreeRegister();
				if (res == null) {
					if (!freeUnusedRegister(tokenNumber)) {
						System.out.println("Could'nt free register");
					}
					res = mem.getFreeRegister();
				}

				if (tok.getOp1().startsWith("%"))
					op1 = mem.getAddress(tok.getOp1());
				else
					op1 = "$" + tok.getOp1();
				if (tok.getOp2().startsWith("%"))
					op2 = mem.getAddress(tok.getOp2());
				else
					op2 = "$" + tok.getOp2();

				asm.movl(op1, res.getFullName(), "Compare");
				asm.cmpl(op2, res.getFullName());

				mem.addRegVar(tok.getOp1(), tok.getTypeOp1(), res);
				break;

			case Branch:
				if (tok.getOp1().isEmpty())
					asm.jmp("label_" + tok.getOp2().substring(1));
				else {
					int result;
					result = findToken(tokenNumber, true, TokenType.Compare,
							null, null, null);
					System.out.println("last compare was in token #" + result
							+ " -> " + code.get(result).getTypeTarget());
					if (code.get(result).getTypeTarget().equals("eq")) {
						asm.je("label_" + tok.getOp1().substring(1));
						asm.jmp("label_" + tok.getOp2().substring(1));
					}
					if (code.get(result).getTypeTarget().equals("ne")) {
						asm.jne("label_" + tok.getOp1().substring(1));
						asm.jmp("label_" + tok.getOp2().substring(1));
					}
					if (code.get(result).getTypeTarget().equals("slt")) {
						asm.jl("label_" + tok.getOp1().substring(1));
						asm.jmp("label_" + tok.getOp2().substring(1));
					}
					if (code.get(result).getTypeTarget().equals("sgt")) {
						asm.jg("label_" + tok.getOp1().substring(1));
						asm.jmp("label_" + tok.getOp2().substring(1));
					}
					if (code.get(result).getTypeTarget().equals("sle")) {
						asm.jle("label_" + tok.getOp1().substring(1));
						asm.jmp("label_" + tok.getOp2().substring(1));
					}
					if (code.get(result).getTypeTarget().equals("sge")) {
						asm.jge("label_" + tok.getOp1().substring(1));
						asm.jmp("label_" + tok.getOp2().substring(1));
					}
				}
				break;

			case String:
				asm.data(tok.getTarget().substring(2), ".ascii", tok.getOp1());

				System.out.println("Size: " + tok.getOp2());

				mem.addHeapVar(tok.getTarget(), 5);

			default:
				break;
			}
			tokenNumber++;
		}

		//erstelle Einstiegspunkt
		asm.createEP();
	}

	

	private boolean freeUnusedRegister(int tokenNumber) {
		boolean result = false;
		for (int i = 0; i < 6; i++) {
			Variable tmp = mem.getVarFromReg(i);
			if (findToken(tokenNumber, false, null, null, tmp.name, tmp.name) == 0) {
				mem.freeRegister(new RegisterAddress(i));
				result = true;
			}
		}
		return result;
	}
	
	private boolean freeUnusedMMXRegister(int tokenNumber) {
		boolean result = false;
		for (int i = 0; i < 6; i++) {
			Variable tmp = mem.getVarFromMMXReg(i);
			if (findToken(tokenNumber, false, null, null, tmp.name, tmp.name) == 0) {
				mem.freeMMXRegister(new MMXRegisterAddress(i));
				result = true;
			}
		}
		return result;
	}

	// TODO
	private boolean isRegisterFree(RegisterAddress res) {
		boolean result = true;

		return result;
	}

	// TODO
	private void saveRegisterValue(RegisterAddress res) {

	}

	private int findToken(int start, boolean backwards, TokenType type,
			String target, String op1, String op2) {
		for (int i = start; i < code.size() && i >= 0;) {
			if (type != null)
				if (code.get(i).getType() == type)
					return i;
			if (target != null)
				if (code.get(i).getTarget().equals(target))
					return i;
			if (op1 != null)
				if (code.get(i).getOp1().equals(op1))
					return i;
			if (op2 != null)
				if (code.get(i).getOp2().equals(op2))
					return i;
			if (backwards)
				i--;
			else
				i++;
		}
		return 0;
	}

	public String getCode() {
		return (asm.getSectionData().toString() + asm.getSectionText().toString());
	}

	public void print() {
		System.out.println("\nGenerated Code:");
		System.out.print(asm.getSectionData());
		System.out.print(asm.getSectionText());
		System.out.println();
	}

	private static int getSize(String type) {
		if (type.equals("double"))
			return 8;
		else
			return 4;
	}

}
