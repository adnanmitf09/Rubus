
package cl;
/*
 * Rubus: A Compiler for Seamless and Extensible Parallelism
 * 
 * Copyright (C) 2017 Muhammad Adnan - University of the Punjab
 * 
 * This file is part of Rubus.
 * Rubus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.

 * Rubus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Rubus. If not, see <http://www.gnu.org/licenses/>.
 */

import graph.BasicBlock;
import graph.Block;
import graph.BlockVisitor;
import graph.CodeVisitor;
import graph.Method;
import graph.Type;
import graph.instructions.Arithmetic;
import graph.instructions.ArrayLength;
import graph.instructions.Call;
import graph.instructions.Compare;
import graph.instructions.Condition;
import graph.instructions.Condition.Operator;
import graph.instructions.Constant;
import graph.instructions.Convert;
import graph.instructions.Increment;
import graph.instructions.Instruction;
import graph.instructions.Negate;
import graph.instructions.NewArray;
import graph.instructions.NewMultiArray;
import graph.instructions.NewObject;
import graph.instructions.Producer;
import graph.instructions.Read;
import graph.instructions.RestoreStack;
import graph.instructions.Return;
import graph.instructions.Switch;
import graph.instructions.ValueReturn;
import graph.instructions.Write;
import graph.state.ArrayElement;
import graph.state.InstanceField;
import graph.state.State;
import graph.state.Variable;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.TransformIterable;
import util.Utils;
import cl.Config.KernelExportOption;

/**
 *
 */
public class CLCppGenerator extends CodeVisitor<String> {
	private static int nextTemporary = 0;
	private PrintStream output;
	private BlockVisitor<Void> exporter;

	public CLCppGenerator(BlockVisitor<Void> clBlockExporter, PrintStream output) {
		this.output = output;
		this.exporter = clBlockExporter;
	}

	/*

floatn fabs(floatn x) 		Returns the absolute value of the argument, |x|
floatn fma(floatn a, floatn b, floatn c)Returns a * b + c, where the multiplication is performed
with precision
floatn fmod(floatn x,
floatn y)
Returns the modulus of x and y: x  (y * trunc(y/x))
floatn mad(floatn a,
floatn b, floatn c)
Returns a * b + c
floatn remainder(floatn x,
floatn y)
Returns the remainder of x and y: x  n * y, where n is
the integer closest to x/y
floatn remquo(floatn x,
floatn y, __(g|l|p) *quo)
Returns the remainder of x and y: x  n * y, where n is
the integer closest to x/y; places the signed lowest seven
bits of the quotient (x/y) in quo
floatn rint(floatn x) Returns the closest integer as a floatif two integers are
equally close, it returns the even integer as a float
floatn round(floatn x) Returns the integer closest to xif two integers are equally
close, it returns the one farther from 0
floatn ceil(floatn x) Returns the closest integer larger than x
floatn floor(floatn x) Returns the closest integer smaller than x
floatn trunc(floatn x) Removes the fractional part of x and returns the integer

floatn clamp(floatn x,
float/n min, float/n max)
Returns min if x < min; returns max if x > max;
otherwise returns x
floatn fdim(floatn x, floatn y) Returns x  y if x > y; returns 0 if x <= y
floatn fmax(floatn x, float/n y) Returns x if x >= y; returns y if y > x
floatn fmin(floatn x, float/n y) Returns x if x <= y; returns y if y < x
floatn max(floatn x, float/n y) Returns x if x >= y; returns y if y > x
floatn min(floatn x, float/n y) Returns x if x <= y; returns y if y < x
floatn mix(floatn x, floatn y,
float/n a)
Interpolates between x and y using the equation
x + (y  x) * a, where 0.0 < a < 1.0
floatn maxmag(floatn x, floatn y) Returns x if |x| >= |y|; returns y if |y| > |x|
floatn minmag(floatn x, floatn y) Returns x if |x| <= |y|; returns y if |y| < |x|
floatn step(float/n edge,
floatn x)
Returns 0.0 if x < edge; returns 1.0 if x >= edge
floatn smoothstep(float/n edge1,
float/n edge2, floatn x)
Returns 0.0 if x <= edge1; returns 1.0 if x >=
edge1; uses smooth interpolation if edge0 < x <
edge1

floatn pow(floatn x, floatn y) Returns xy
floatn pown(floatn x, intn y) Returns xy, where y is an integer
floatn powr(floatn x, floatn y) Returns xy, where x is greater than or equal to 0
floatn exp/expm1(floatn x) Returns ex and ex  1
floatn exp2/exp10(floatn x) Returns 2x and 10x
floatn ldexp(floatn x, intn n)) Returns x * 2n
floatn rootn(floatn x, floatn y) Returns x1/y
floatn sqrt/cbrt(floatn x) Returns the square root/cube root of x
floatn rsqrt(floatn x) Returns the inverse square root of x
floatn log/log1p(floatn x) Returns ln(x) and ln(1.0 + x)
floatn log2/log10(floatn x) Returns log2 x and log10 x
floatn logb(floatn x) Returns the integral part of log2 x
floatn erf/erfc(floatn x) Returns the error function and the complementary
error function
floatn tgamma/lgamma(floatn x) Returns the gamma function and the log gamma function
floatn lgamma_r(floatn x,
__(g|l|p) intn *mem)
Returns the log gamma function and places the sign in
the memory referenced by mem

floatn sin/cos/tan(floatn) Returns the sine, cosine, and tangent
floatn sinpi/cospi/tanpi(floatn x) Returns the sine, cosine, and tangent of x
floatn asin/acos/atan(floatn) Returns the arcsine, arccosine, and arctangent
floatn asinpi/acospi/atanpi
(floatn x)
Returns the arcsine, arccosine, and arctangent of x
floatn sinh/cosh/tanh(floatn) Returns the hyperbolic sine, cosine, and tangent
floatn asinh/acosh/atanh(floatn) Returns the hyperbolic arcsine, arccosine, and
arctangent
floatn sincos(floatn x,
__(g|l|p) floatn *mem)
Returns the sine of x and places the cosine in the
memory referenced by mem
floatn atan2/atan2pi(floatn x) Returns the sine, cosine, and tangent of x
	 */




	//  private static final Set<String> mathSupported = new HashSet<String>() {{
	//    add("sin"); add("cos"); add("tan"); add("pow");
	//  }};

	private static final Set<String> mathSupportedFloat = new HashSet<String>() {{
		add("fabs");
		add("fdim");	 	 	 
		add("fmax");
		add("fma");
		add("fmin");	 
		add("fmod");	 
		add("fract");	 
		add("frexp");}};

	private static final Set<String> mathSupported = new HashSet<String>() {{
		add("abs"); 
		add("acos"); 
		add("asin"); 
		add("atan"); 
		add("atan2"); 
		add("ceil"); 
		add("cos"); 
		add("exp"); 
		add("floor"); 
		add("log"); 
		add("max"); 
		add("min"); 
		add("pow"); 
		add("rint"); 
		add("round"); 
		add("sin"); 
		add("sqrt"); 
		add("tan"); 

		//extended
		add("acosh");	 
		add("acospi");	 
		add("asinh");	 
		add("asinpi");	 	 
		add("atanh");	 
		add("atanpi");	 
		add("atan2pi");	 
		add("cbrt");
		add("ceil");	 
		add("copysign");	 	 
		add("cosh");
		add("cospi");	 
		add("erfc");	 
		add("erf");	 
		add("exp2");	 
		add("exp10");	 
		add("expm1");	 
		
//		add("fabs");
//		add("fdim");	 	 	 
//		add("fmax");
//		add("fmin");	 
//		add("fmod");	 
//		add("fract");	 
//		add("frexp");
		
		add("hypot");	 
		add("ilogb");	 
		add("ldexp");	 
		add("lgamma");
		add("lgamma_r");	 	 
		add("log2");	 
		add("log10");
		add("log1p");	 
		add("logb");	 
		add("mad");	 
		add("modf");
		add("nan");	 
		add("nextafter");	 	 
		add("pown");
		add("powr");	 
		add("remainder");	 
		add("remquo");	 
		add("rootn");	 	 
		add("rsqrt");	 
		add("sincos");	 
		add("sinh");	 
		add("sinpi");	 
		add("tanh");	 
		add("tanpi");	 
		add("tgamma");
		add("trunc");


	}};


	public String assignTemporary(Type type, String value) {
		String temporary = "t" + (nextTemporary++);

		//by Adnan
		String globalModifire ="";
		if(CLHelper.isArryType(type))
			globalModifire = "__global";
		//	

		output.print(globalModifire +" "+  CLHelper.getCLType(type) + " " + temporary + " = ");
		//by Adnan
		// if(CLHelper.isArryType(type))
		//	value = "&"+value;
		//	
		output.println(value + ";");

		return temporary;
	}

	@Override
	public String visit(Instruction instruction) {
		throw new CLUnsupportedInstruction(instruction, "OpenCL");
	}

	@Override
	public String visit(RestoreStack instruction) {
		return assignTemporary(
				instruction.getType(),
				"s" + CLHelper.getName(
						new Variable(instruction.getIndex(), instruction.getType())
						)
				);
	}

	@Override
	public String visit(Condition instruction) {
		//orgnl
		//		String operator = chooseOperatorSymbol(instruction.getOperator());

		String operator = chooseOperatorSymbol(instruction.getOperator());





		if(Config.kernelExportOptions == KernelExportOption.GOTO_LABEL){
			output.println(
					"if(" + instruction.getOperandA().accept(this) + operator +
					instruction.getOperandB().accept(this) + ") goto " +
					CLBlockExporter.getLabel(instruction.getDestination()) + ";"
					);


		}else   if(Config.kernelExportOptions == KernelExportOption.METHODS){

			// function calling


			output.println(
					"if(" + instruction.getOperandA().accept(this) + operator +
					instruction.getOperandB().accept(this) + ")" +
					CLBlockAsFunctionExporter.getFunctionCall(instruction.getDestination())
					);

		}else if(Config.kernelExportOptions == KernelExportOption.TRANSFORM_TO_CONDITIONS){
			if(((CLSimpleBlockExporter)exporter).shouldExport(instruction.getDestination())){		
				instruction.getDestination().setForceSkip(true);
				///////////////////////////////

				String string = "if(" + instruction.getOperandA().accept(this) + chooseOperatorSymbol(instruction.getOperator().not())+ instruction.getOperandB().accept(this)+") { " ;	
				output.print(string);
				instruction.getParent().getNext().accept(exporter);
				output.println("}/* end if */"
						);
				instruction.getDestination().setForceSkip(false);
				instruction.getDestination().accept(exporter);


			}else{// already exported

				// adi - force export
				// instruction.getDestination().setForceExport(true);
				///////////////////////////////
// this is not working yet, will we completed in future work
				String string = "}while(" + instruction.getOperandA().accept(this) + operator + instruction.getOperandB().accept(this)+");" ;	
				output.print(string);
				((BasicBlock)instruction.getDestination()).setHaveBackEdge(true);
			}
		}



		/*/			
//originol			
			String string = "if(" + instruction.getOperandA().accept(this) + operator + instruction.getOperandB().accept(this)+") { " ;	
			output.print(string);


					instruction.getDestination().accept(exporter);


					output.println("}"
					);

		}*/
		//







		//    if(Config.gotoOptions == GotoOption.DONOTHING){
		//    output.println(
		//      "if(" + instruction.getOperandA().accept(this) + operator +
		//      instruction.getOperandB().accept(this) + ") goto " +
		//      CLBlockExporter.getLabel(instruction.getDestination()) + ";"
		//    );
		//  
		//    
		//    }else     if(Config.gotoOptions == GotoOption.TRANSFORM_TO_CONDITIONS){
		//    	 output.println(
		//    		      "if(" + instruction.getOperandA().accept(this) + operator +
		//    		      instruction.getOperandB().accept(this) + ") { " +
		//    		     instruction.getDestination().accept(exporter) + "}");
		//    		   
		//    
		//    }

		return null;
	}

	private String chooseOperatorSymbol(Operator op) {
		// Choose operator symbol
		String operator="";
		switch(op) {
		case EQ: operator = "==";  break;
		case NE: operator = "!=";  break;
		case LT: operator = "<";  break;
		case GE: operator = ">=";  break;
		case GT: operator = ">";  break;
		case LE: operator = "<=";  break;
		default: operator = "";
		}
		return operator;
	}

	@Override
	public String visit(Switch instruction) {

		output.println("switch(" + instruction.getOperand().accept(this) + ") {");


		if(Config.kernelExportOptions == KernelExportOption.GOTO_LABEL){


			for(Map.Entry<Integer, Block> mapping : instruction.getMapping().entrySet()) {
				output.print("  case " + mapping.getKey() + ": goto ");
				output.println(CLBlockExporter.getLabel(mapping.getValue()) + ";");
			}

			if(instruction.getDefault() != null) {
				output.println("  default: goto " + CLBlockExporter.getLabel(instruction.getDefault()) + ";");
			}


		}else   if(Config.kernelExportOptions == KernelExportOption.TRANSFORM_TO_CONDITIONS){

			for(Map.Entry<Integer, Block> mapping : instruction.getMapping().entrySet()) {
				output.print("  case " + mapping.getKey() + ":  ");
				mapping.getValue().accept(exporter);
				output.println("break;");

			}

			if(instruction.getDefault() != null) {
				output.println("  default:  "  );
				instruction.getDefault().accept(exporter);
				output.println("break;");

			}
		}else{
			for(Map.Entry<Integer, Block> mapping : instruction.getMapping().entrySet()) {
				output.print("  case " + mapping.getKey() + ":  ");
				mapping.getValue().accept(exporter);
				output.println("break;");

			}

			if(instruction.getDefault() != null) {
				output.println("  default:  "  );
				instruction.getDefault().accept(exporter);
				output.println("break;");

			}
		}

		output.println("}");

		return null;
	}

	@Override
	public String visit(Compare instruction) {
		String a = instruction.getOperandA().accept(this);
		String b = instruction.getOperandB().accept(this);

		// TODO: Correct treatment of NaNs (but GPUs don't support NaN properly!)
		return assignTemporary(
				instruction.getType(),
				"(" + a + "<" + b + ") ? -1 : (" + a + ">" + b + ") ? 1 : 0"
				);
	}

	@Override
	public String visit(Arithmetic instruction) {
		String operator;

		// Choose operator symbol
		switch(instruction.getOperator()) {
		case ADD: operator = "+";  break;
		case SUB: operator = "-";  break;
		case MUL: operator = "*";  break;
		case DIV: operator = "/";  break;
		case REM: operator = "%";  break;
		case AND: operator = "&";  break;
		case OR:  operator = "|";  break;
		case XOR: operator = "^";  break;
		case SHL: operator = "<<"; break;
		case SHR: operator = ">>"; break;
		case USHR:operator = ">>>";break;
		default:  operator = "";
		}

		return assignTemporary(
				instruction.getType(),
				instruction.getOperandA().accept(this) + operator +
				instruction.getOperandB().accept(this)
				);
	}

	@Override
	public String visit(Constant instruction) {
		// Object constants
		if(instruction.getType().getSort() == Type.Sort.REF) {
			throw new RuntimeException("Object/String constants not supported");
			// Primitive Types
		} else {
			switch(instruction.getType().getSort()) {
			// Longs (L suffix)
			case LONG:   return assignTemporary(
					instruction.getType(),
					instruction.getConstant().toString() + "L"
					);
			// Floats (f suffix)
			case FLOAT: return assignTemporary(
					instruction.getType(),
					instruction.getConstant().toString() + "f"
					);
			// Others
			default:     return assignTemporary(
					instruction.getType(),
					instruction.getConstant().toString()
					);
			}
		}
	}

	@Override
	public String visit(Negate instruction) {
		return assignTemporary(
				instruction.getType(),
				"-" + instruction.getOperand().accept(this)
				);
	}

	@Override
	public String visit(Convert instruction) {
		return assignTemporary(
				instruction.getType(),
				"(" + CLHelper.getCLType(instruction.getType()) + ") " +
						instruction.getOperand().accept(this)
				);
	}

	@Override
	public String visit(Increment instruction) {
		output.println(
				CLHelper.getName(instruction.getState()) + " += " +
						instruction.getIncrement() + ";"
				);

		return null;
	}
	//
	//  @Override
	//  public String visit(Read instruction) {
	//    State  state = instruction.getState();
	//    String value;
	//
	//    // Object Fields
	//    if(state instanceof InstanceField) {
	//      InstanceField f = (InstanceField) state;
	//
	//      value = "DEVPTR(" + f.getObject().accept(this) + ".device)->" + f.getField().getName();
	//    // Array Elements
	//    } else if(state instanceof ArrayElement) {
	//      ArrayElement e = (ArrayElement) state;
	//
	//      value = "DEVPTR(" + e.getArray().accept(this) + ".device)[" + e.getIndex().accept(this) + "]";
	//    // Variables and Statics
	//    } else {
	//      // TODO: Math.E and Math.PI
	//      value = CLHelper.getName(state);
	//    }
	//
	//    return assignTemporary(instruction.getType(), value);
	//  }
	//
	//  @Override
	//  public String visit(Write instruction) {
	//    String line = "";
	//    State  state = instruction.getState();
	//
	//    // Object Fields
	//    if(state instanceof InstanceField) {
	//      InstanceField f = (InstanceField) state;
	//
	//      line += "DEVPTR(" + f.getObject().accept(this) + ".device)->" + f.getField().getName();
	//    // Array Elements
	//    } else if(state instanceof ArrayElement) {
	//      ArrayElement e = (ArrayElement) state;
	//
	//      line += "DEVPTR(" + e.getArray().accept(this) + ".device)[" + e.getIndex().accept(this) + "]";
	//    // Variables and Statics
	//    } else {
	//      line += CLHelper.getName(state);
	//    }
	//
	//    output.println(line + " = " + instruction.getValue().accept(this) + ";");
	//
	//    return null;
	//  }

	@Override
	public String visit(Read instruction) {
		State  state = instruction.getState();
		String value;

		// Object Fields
		if(state instanceof InstanceField) {
			throw new CLUnsupportedInstruction(instruction, "Instance fields are not supported yet.");
			/*
    	InstanceField f = (InstanceField) state;

      value = "" + f.getObject().accept(this) + "->" + f.getField().getName();*/
			// Array Elements
		} else if(state instanceof ArrayElement) {
			ArrayElement e = (ArrayElement) state;

			value = "" + e.getArray().accept(this) + "[" + e.getIndex().accept(this) + "]";
			// Variables and Statics
		} else {
			// TODO: Math.E and Math.PI
			value = CLHelper.getName(state);
		}

		return assignTemporary(instruction.getType(), value);
	}

	@Override
	public String visit(Write instruction) {
		String line = "";
		State  state = instruction.getState();

		// Object Fields
		if(state instanceof InstanceField) {
			throw new CLUnsupportedInstruction(instruction, "Instance fields are not supported yet.");
			/*
    	InstanceField f = (InstanceField) state;

      line += "" + f.getObject().accept(this) + "->" + f.getField().getName();*/
			// Array Elements
		} else if(state instanceof ArrayElement) {
			ArrayElement e = (ArrayElement) state;

			line += "" + e.getArray().accept(this) + "[" + e.getIndex().accept(this) + "]";
			// Variables and Statics
		} else {
			line += CLHelper.getName(state);
		}

		output.println(line + " = " + instruction.getValue().accept(this) + ";");

		return null;
	}

	@Override
	public String visit(Call instruction) {
		Method method = instruction.getMethod();

		// Math Functions (use built-in OpenCL versions).
		if(method.getOwner().getName().equals("java/lang/Math")) {

			// some functions are specific for flots
			boolean haveFloatParam = false;
			
			Producer[] ops = instruction.getOperands();
			if(ops!=null){
				for (Producer op:ops) {
					if(CLHelper.getCLType(op.getType()).contains("float")){
						haveFloatParam = true;
					}
				}
				
			}
			// OpenCL have different functions for float params
			 if(haveFloatParam && mathSupportedFloat.contains("f"+method.getName())) {
					return assignTemporary(
							instruction.getType(),
							"f"+method.getName() + "(" +
									Utils.join(new TransformIterable<Producer,String>(Arrays.asList(instruction.getOperands())) {
										@Override
										protected String transform(Producer obj) {
											return obj.accept(CLCppGenerator.this);
										}
									}, ", ") + ")"
							);
				}  
				
				else if(mathSupported.contains(method.getName())) {
				return assignTemporary(
						instruction.getType(),
						method.getName() + "(" +
								Utils.join(new TransformIterable<Producer,String>(Arrays.asList(instruction.getOperands())) {
									@Override
									protected String transform(Producer obj) {
										return obj.accept(CLCppGenerator.this);
									}
								}, ", ") + ")"
						);
			}
		
			else{
				throw new CLUnsupportedInstruction(
						instruction,
						"OpenCL",
						"Math." + method.getName() + " is not supported yet."
						);
			}
			// Standard Method
		} else {
			if(method.getImplementation() != null) {
				String arguments = Utils.join(
						new TransformIterable<Producer, String>(Arrays.asList(instruction.getOperands())) {
							@Override
							protected String transform(Producer arg) {
								return arg.accept(CLCppGenerator.this);
							}
						},
						", "
						);

				CLExporter.export(method);

				// Void return (no value).
				if(method.getReturnType() == Type.VOID) {
					output.println(CLHelper.getSimpleName(method) + "(" + arguments + ");");

					return null;
					// Value return.
				} else {
					return assignTemporary(
							instruction.getType(),
							CLHelper.getSimpleName(method) + "(" + arguments + ")"
							);
				}
			} else {
				throw new CLUnsupportedInstruction(
						instruction,
						"OpenCL",
						method + " outside scope of transform."
						);
			}
		}
	}



	/**
	 * Javac optimizes some branches to avoid goto->goto, branch->goto etc.  
	 * 
	 * This method specifically deals with reverse branches which are the result of such optimisations. 
	 * 
	 * <code><pre>
	 * 
	 * </pre></code>
	 * 
	 *  Remove goto
	 * 
	 */
	/*   public void deoptimizeReverseBranches() {

	      for (Instruction instruction = pcHead; instruction != null; instruction = instruction.getNextPC()) {
	         if (instruction.isBranch()) {
	            final Branch branch = instruction.asBranch();
	            if (branch.isReverse()) {
	               final Instruction target = branch.getTarget();
	               final LinkedList<Branch> list = target.getReverseUnconditionalBranches();
	               if ((list != null) && (list.size() > 0) && (list.get(list.size() - 1) != branch)) {
	                  final Branch unconditional = list.get(list.size() - 1).asBranch();
	                  branch.retarget(unconditional);

	               }
	            }
	         }
	      }
	   }
	 */
	@Override
	public String visit(Return instruction) {
		output.println("return;");

		return null;
	}

	@Override
	public String visit(ValueReturn instruction) {
		output.println("return " + instruction.getOperand().accept(this) + ";");

		return null;
	}

	@Override
	public String visit(ArrayLength instruction) {
		return assignTemporary(
				instruction.getType(),
				instruction.getOperand().accept(this) + ".length"
				);
	}

	@Override
	public String visit(NewArray instruction) {
		return super.visit(instruction);
	}

	@Override
	public String visit(NewMultiArray instruction) {
		return super.visit(instruction);
	}

	@Override
	public String visit(NewObject instruction) {
		return super.visit(instruction);
	}
}
