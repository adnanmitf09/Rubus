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

import graph.Method;
import graph.Modifier;
import graph.Type;
import graph.state.Field;
import graph.state.State;
import graph.state.Variable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.bridj.Pointer;

import util.TransformIterable;
import util.Utils;
import cl.CLKernel.Parameter;
import codeexport.SClassFile;
import codeexport.SField;
import codeexport.SInstruction;
import codeexport.SMethod;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.opencl.CLDevice.QueueProperties;

/**
 * Helper methods for exporting methods to C code (of the CUDA/JNI variety).
 */
public class CLHelper {

	public static String lineSeparator = System.getProperty("line.separator");;

	
	public static String getSimpleName(Method method) {
		return method.getName();
	}

	/**
	 * Returns a variable name for the given <code>State</code> object.
	 * 
	 * @param state
	 *            State.
	 * @return C variable name.
	 */
	public static String getName(State state) {
		// Variables
		if (state instanceof Variable) {
			Variable v = (Variable) state;

			if (v.getType().getSort() == Type.Sort.REF) {
				if (v.getType().hashCode() < 0) {
					return "v" + v.getIndex() + "_M"
							+ (-v.getType().hashCode());
				} else {
					return "v" + v.getIndex() + "_" + v.getType().hashCode();
				}
			} else {
				return "v" + v.getIndex() + "_" + v.getType().getSort();
			}
			// Static Fields
		} else if (state instanceof Field) {
			Field f = (Field) state;

			return "Static_"
					+ f.getOwner().getName().replace('/', '_') + "_"
					+ f.getName();
		}

		throw new RuntimeException(
				"States that aren't variables or statics must be handled case-by-case.");
	}

	/**
	 *
	 * 
	 *         bool A Boolean condition: true (1) or false (0) char Signed twos
	 *         complement 8-bit integer unsigned char/uchar Unsigned twos
	 *         complement 8-bit integer short Signed twos complement 16-bit
	 *         integer unsigned short/ushort Unsigned twos complement 16-bit
	 *         integer int Signed twos complement 32-bit integer unsigned
	 *         int/uint Unsigned twos complement 32-bit integer long Signed twos
	 *         complement 64-bit integer unsigned long/ulong Unsigned twos
	 *         complement 64-bit integer half 16-bit floating-point value,
	 *         IEEE-754-2008 conformant float 32-bit floating-point value,
	 *         IEEE-754 conformant intptr_t Signed integer to which a void
	 *         pointer can be converted uintptr_t Unsigned integer to which a
	 *         void pointer can be converted ptrdiff_t Signed integer produced
	 *         by pointer subtraction size_t Unsigned integer produced by the
	 *         size of operator void Untyped data
	 * 
	 * 
	 *         Table 4.3 OpenCL vector data types
	 * 
	 *         charn Vector containing n 8-bit signed twos complement integers
	 *         ucharn Vector containing n 8-bit unsigned twos complement
	 *         integers shortn Vector containing n 16-bit signed twos complement
	 *         integers ushortn Vector containing n 16-bit unsigned twos
	 *         complement integers intn Vector containing n 32-bit signed twos
	 *         complement integers uintn Vector containing n 32-bit unsigned
	 *         twos complement integers longn Vector containing n 64-bit signed
	 *         twos complement integers ulongn Vector containing n 64-bit
	 *         unsigned twos complement integers floatn Vector containing n
	 *         32-bit single-precision floating-point values
	 * 
	 * 
	 *         Accessing the double data type The double data type can be
	 *         accessed if the target device supports the cl_khr_fp64 extension.
	 *         From the host, you can determine whether this extension is
	 *         available by calling clGetDeviceInfo, a function explained in
	 *         chapter 2. If the extension is supported, you can enable its
	 *         capability in the kernel with the following pragma statement:
	 * 
	 *         #pragma OPENCL EXTENSION cl_khr_fp64 : enable
	 * 
	 *         When this is present, you can declare double variables and
	 *         operate on them normally. If you want to enable every supported
	 *         extension, replace cl_khr_fp64 with all. To disable an extension,
	 *         replace enable with disable. In the Ch4/double_test project, the
	 *         kernel uses the double type if its supported and uses the float
	 *         type if its not. This is shown in the following listing.
	 * 
	 *         #ifdef FP_64 #pragma OPENCL EXTENSION cl_khr_fp64: enable #endif
	 *         __kernel void double_test(__global float* a, __global float* b,
	 *         __global float* out) { #ifdef FP_64 double c = (double)(*a / *b);
	 *         out = (float)c; #else out = *a * *b; #endif }
	 * 
	 * 
	 *         Table 5.1 OpenCL operators Operator Purpose Operator Purpose
	 *         Operator Purpose + Addition == Equal to ! Logical NOT -
	 *         Subtraction != Not equal to & Bitwise AND Multiplication >
	 *         Greater than | Bitwise OR / Division (quotient) >= Greater than
	 *         or equal to ^ Bitwise XOR % Division (modulus) < Less than ~
	 *         Bitwise NOT ++ Increment <= Less than or equal to >> Right-shift
	 *         -- Decrement && Logical AND << Left-shift || Logical OR ?:
	 *         Ternary selection
	 */

	public static String getJavaType(Type type) {
		// Void
		if (type.getSort() == null) {
			return "void";
			// Primitive Types
		} else if (type.getSort() != Type.Sort.REF) {
			switch (type.getSort()) {
			// changed here
			case BOOL:
				return "boolean";
			case BYTE:
				return "byte";
			case CHAR:
				return "char";
			case SHORT:
				return "short";
			case INT:
				return "int";
			case FLOAT:
				return "float";
			case LONG:
				return "long";
			case DOUBLE:
				return "double";
			default:
				throw new UnsupportedOperationException("ADDRESS TYPE");
			}
			// Array Types
		} else if (type.getElementType() != null) {
			return "" + getJavaType(type.getElementType()) + "[]";
			// Object Types
		} else {
			return "Object";
		}
	}

	public static boolean isArryType(Type type) {
		return (type.getElementType() != null);
	}

	public static boolean is2DArryType(Type type) {
		try {
			return (type.getElementType().getElementType() != null);

		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * Open cl use * for array reference, not []
	 * 
	 * @param type
	 * @return
	 */

	public static String getCLType(Type type) {
		// Void
		if (type.getSort() == null) {
			return "void";
			// Primitive Types
		} else if (type.getSort() != Type.Sort.REF) {
			switch (type.getSort()) {
			// changed here
			case BOOL:
				return "bool";
			case BYTE:           // byte maps to char in opencl Ref: https://code.google.com/p/javacl/wiki/UnderstandOpenCLKernelArguments
 				return "char";
			case CHAR:
				return "char";
			case SHORT:
				return "short";
			case INT:
				return "int";
			case FLOAT:
				return "float";
			case LONG:
				return "long";
			case DOUBLE:
				return "float";//*fix* "double"  double is not yet supported on all SDKs;
			default:
				throw new UnsupportedOperationException("ADDRESS TYPE");
			}
			// Array Types
		} else if (type.getElementType() != null) {
			String typeS = getCLType(type.getElementType());
			// fix - 2D array must not have 2 stars
			if (!typeS.contains("*")) {
				typeS = typeS + "*";
			}
			return typeS;
			// Object Types
		} else {
			throw new UnsupportedOperationException("OBJECT TYPE is not supported yet : "+type);

		}
	}

	public static String getJavaCLPointerType(Type type) {
		// Void

		if (type.getSort() == null) {
			return "void";
			// Primitive Types
		} else if (type.getSort() != Type.Sort.REF) {
			switch (type.getSort()) {
			// changed here
			case BOOL:
				return "Boolean";
			case BYTE:
				return "Byte";
			case CHAR:
				return "Char";
			case SHORT:
				return "Short";
			case INT:
				return "Integer";
			case FLOAT:
				return "Float";
			case LONG:
				return "Long";
			case DOUBLE:
				return "Double";
			default:
				throw new UnsupportedOperationException("ADDRESS TYPE");
			}
			// Array Types
		} else if (type.getElementType() != null) {
			String typeS = getJavaCLPointerType(type.getElementType());
			// fix - 2D array must not ends with 2 s
			if (!typeS.endsWith("s")) {
				typeS = typeS + "s";
			}
			return typeS;

			// Object Types - not supported yet
		} else {
			throw new UnsupportedOperationException("OBJECT TYPE is not supported yet");
		}
	}

	public static String getJavaCLPointer(Type type) {
		// Void
		if (type.getSort() == null) {
			return "void";
			// Primitive Types
		} else if (type.getSort() != Type.Sort.REF) {
			switch (type.getSort()) {
			// changed here
			case BOOL:
				return getFullName(Boolean.class);
			case BYTE:
				return getFullName(Byte.class);
			case CHAR:
				return getFullName(Character.class);
			case SHORT:
				return getFullName(Short.class);
			case INT:
				return getFullName(Integer.class);
			case FLOAT:
				return getFullName(Float.class);
			case LONG:
				return getFullName(Long.class);
			case DOUBLE:
				return getFullName(Double.class);
			default:
				throw new UnsupportedOperationException("ADDRESS TYPE");
			}
			// Array Types
		} else if (type.getElementType() != null) {
			String typeS = getFullName(Pointer.class) + "<"
					+ getJavaCLPointer(type.getElementType()) + ">";

			return typeS;

			// Object Types - not supported yet
		} else {
			throw new UnsupportedOperationException("OBJECT TYPE is not supported yet");
		}
	}

	public static String getJavaCLBufferType(Type type) {
		// Void

		if (type.getSort() == null) {
			return "void";
			// Primitive Types
		} else if (type.getSort() != Type.Sort.REF) {
			switch (type.getSort()) {
			// changed here
			case BOOL:
				return getFullName(Boolean.class);
			case BYTE:
				return getFullName(Byte.class);
			case CHAR:
				return getFullName(Character.class);
			case SHORT:
				return getFullName(Short.class);
			case INT:
				return getFullName(Integer.class);
			case FLOAT:
				return getFullName(Float.class);
			case LONG:
				return getFullName(Long.class);
			case DOUBLE:
				return getFullName(Double.class);
			default:
				throw new UnsupportedOperationException("ADDRESS TYPE");
			}
			// Array Types
		} else if (type.getElementType().getElementType() != null) {
			String typeS = getJavaCLBufferType(type.getElementType());
			// fix
			if (!typeS.contains("Pointer")) {
				typeS = getFullName(Pointer.class) + "<" + typeS + ">";
			}

			return typeS;

			// Object Types - not supported yet
		}
		if (type.getElementType() != null) {
			return getJavaCLBufferType(type.getElementType());

		} else {
			throw new UnsupportedOperationException("OBJECT TYPE is not supported yet");
		}
	}


	/**
	 * Outputs the code for a given state to be imported, with or without
	 * specifying copy out when the corresponding export occurs.
	 * 
	 * @param state
	 *            State to import.
	 * @param copy
	 *            <code>true</code> if the state should be copied back to Java,
	 *            <code>false</code> otherwise.
	 * @param method
	 *            Output printstream.
	 */
	public static void importState(State state, boolean copyOut, SMethod method) {
		Type type = state.getType();
		String pType = getJavaCLPointerType(type);
	//	String bufferType = getJavaCLBufferType(type);
		// // If not Array Types - remove s for first generic, need to add good
		// // check, if integer add integer instead of float and so on
		// String genericType = pType;
		// if (state.getType().getElementType() != null && pType.endsWith("s"))
		// {
		// genericType = genericType.substring(0, genericType.length() - 1);
		// }

		// special case
		pType = pType.replace("Integer", "Int");

		/*
		 * 
		 * if (copyOut)
		 * method.addInstruction(SInstruction.make(getFullName(CLBuffer
		 * .class)+"<"+bufferType+">"+ " _" + getName(state) +
		 * " = context.createBuffer("
		 * +getFullName(CLMem.class)+".Usage.InputOutput, "
		 * +getFullName(Pointer.class)+".pointerTo" + pType + "(" +
		 * getName(state) + "), true);"));
		 * 
		 * else
		 * method.addInstruction(SInstruction.make(getFullName(CLBuffer.class
		 * )+"<"+bufferType+">"+ " _" + getName(state) +
		 * " = context.createBuffer("
		 * +getFullName(CLMem.class)+".Usage.Input, "+getFullName
		 * (Pointer.class)+".pointerTo" + pType + "(" + getName(state) +
		 * "), true);"));
		 */
		/**
		 * Fix after discussion Prof. Chiba (Javassist don't support generics)
		 * Shigeru Chiba 11:36 AM 5/5/2014(8 hours ago)
		 * 
		 * to me No, any valid Java methods do not have type parameters at the
		 * bytecode level. Please google "erasure generics".
		 * 
		 */
		
		
		  if (copyOut)
		  method.addInstruction(SInstruction.make(getFullName(CLBuffer
		  .class)+/*"<"+bufferType+">"+*/ " _" + getName(state) +
		  " = context.createBuffer("
		  +getFullName(CLMem.class)+".Usage.InputOutput, "
		  +getFullName(Pointer.class)+".pointerTo" + pType + "(" +
		  getName(state) + "), true);"));
		  
		  else
		  method.addInstruction(SInstruction.make(getFullName(CLBuffer.class
		  )+/*"<"+bufferType+">"+*/ " _" + getName(state) +
		  " = context.createBuffer("
		  +getFullName(CLMem.class)+".Usage.Input, "+getFullName
		  (Pointer.class)+".pointerTo" + pType + "(" + getName(state) +
		  "), true);"));
		 

	}

	/**
	 * File header - including imports, package, class and static/member
	 * variable declaration if required
	 * 
	 * @param classFile
	 * @param source
	 */

	public static void writeFileStart(SClassFile classFile) {
		// package
		if (classFile.getPackageName()!= null
				&& classFile.getPackageName().trim().length() > 0)
			classFile.setPackageName(classFile.getPackageName());/// "+source.getPath().replaceAll("\\", "."));
		// imports
		// classFile.println("import org.bridj.Pointer;");
		// classFile.println("import static org.bridj.Pointer.*;");
		// classFile.println("import static com.nativelibs4java.opencl.JavaCL.createBestContext;");
		// classFile.println("import com.nativelibs4java.opencl.*;");
		// out.println("import com.nativelibs4java.opencl.CLContext;");
		// out.println("import com.nativelibs4java.opencl.CLKernel;");
		// out.println("import com.nativelibs4java.opencl.CLMem;");
		// out.println("import com.nativelibs4java.opencl.CLProgram;");
		// out.println("import com.nativelibs4java.opencl.CLQueue;");
		// classFile.println("import com.nativelibs4java.util.IOUtils;");
		// class declaration
		String filename = classFile.getName();
		
		try {
			String parts [] = filename.split("/");
			filename = parts[parts.length-1];
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		classFile.setStart("public class " + filename+ " {");
	}
/**
 * Kernel prefix
 * @param kernel
 * @param out
 */
	public static void kernelStart(CLKernel kernel, PrintStream out) {

		out.print("__kernel void " + kernel.getName() + "(");

		// Dimension limits. Only need to pass these for X and Y, since OpenCL
		// will
		// never need to execute 'extra' threads in Z, and does not support
		// higher
		// dimensions.
		for (int d = 0; (d < 2) && (d < kernel.getDimensions()); d++) {
			if (d != 0) {
				out.print(", ");
			}
			// __global, __constant, __local, __private

			out.print("const int limit" + d);
		}

		// Real parameters

		for (Parameter p : kernel.getRealParameters()) {
			if (p.getState() instanceof Variable) {
				// use C type array pointer (*)
				Type type = p.getState().getType();
				// by Adnan
				// in cae of array, need to add * and global modifire
				String globalModifire = "";
				String staric = "";
				if (CLHelper.isArryType(type)) {
					globalModifire = "__global ";
					// staric = "*";
				} else {
					// this const create problem, for example: if have increment
					// > 1 like i+=2. This varible will be incremented
					// by that increment, so it can not be constant
				}
				out.print(", " + globalModifire
						+ getCLType(p.getState().getType()) + " " + staric
						+ getName(p.getState()));
			}
		}

		out.println(") {");

		/*
		 * global uint get_work_dim() Returns the number of dimensions in the
		 * kernels index space size_t get_global_size(uint dim) Returns the
		 * number of work-items for a given dimension size_t get_global_id(uint
		 * dim) Returns the element of the work-items global ID for a given
		 * dimension size_t get_global_offset(uint dim) Returns the initial
		 * offset used to compute global IDs
		 * 
		 * workgroup
		 * 
		 * size_t get_num_groups(uint dim) Returns the number of work-groups for
		 * a given dimension size_t get_group_id(uint dim) Returns the ID of the
		 * work-items work-group for a given dimension size_t get_local_id(uint
		 * dim) Returns the ID of the work-item within its work-group for a
		 * given dimension size_t get_local_size(uint dim) Returns the number of
		 * work-items in the work-group for a given dimension
		 * 
		 * get_global_size tells you how many work-items are executing the same
		 * kernel. get_local_size tells you how many work-items are in the same
		 * work-group as the calling work-item.
		 */

		// Calculate dimensions.
		switch (kernel.getDimensions()) {
		default:
		case 3:
			out.println("int dim2 = get_global_id(2);");
														
		case 2:
			out.println("int dim1 = get_global_id(1);");
		case 1:
			out.println("int dim0 = get_global_id(0);");
														
		}

		
		// Perform increments.
		for (int d = 0; (d < 2) && (d < kernel.getDimensions()); d++) {
			for (Map.Entry<Variable, Integer> inc : kernel.getIncrements(d)
					.entrySet()) {
				// out.println(getName(inc.getKey()) + " += "
				// + inc.getValue().intValue() + " * dim" + d + ";");

				out.println(getName(inc.getKey()) + " += "
						+ inc.getValue().intValue() + " * dim" + d + ";");

			}
		}

		// Check limits ( only for 3 dimensions).
		for (int d = 0; (d < 2) && (d < kernel.getDimensions()); d++) {
			Variable index = kernel.getIndex(d);

			out.print("if(" + getName(index));

			if (kernel.getIncrements(d).get(index).intValue() > 0) {
				out.print(" >= ");
			} else {
				out.print(" <= ");
			}

			out.println("limit" + d + ") return;");
		}
	}

	/**
	 * File end
	 * 
	 * @param out
	 */
	public static void fileEnd(PrintStream out) {
		out.println("}");
	}

	/**
	 * Byte order Table 4.1 tells you how many bytes are in a data type, but it
	 * doesnt say anything about how the bytes are ordered. Neither does the
	 * OpenCL standard. The reason for this is that different devices and
	 * operating systems order bytes differently. Therefore, if youre going to
	 * perform an operation that involves byte order, such as accessing data
	 * with pointers, you need to determine the endianness of the target device.
	 * This tells you whether bytes become more or less significant as memory
	 * addresses run from low to high. Figure 4.1 depicts this graphically.
	 */
	/**
	 * Outputs the code for the start of a kernel implementation. This
	 * calculates the values of any variables that depend on the loop indices,
	 * and also returns if a thread is above the limit.
	 * 
	 * @param kernel
	 *            CLKernel to export.
	 * @param out
	 *            Output printstream.
	 * 
	 */
	// public static void kernelStart(CLKernel kernel, StringBuilder builder) {
	//
	// builder.append("// " + kernel.getName()+lineSeparator);
	//
	// builder.append("private static " + kernel.getName() +
	// "= \"__kernel void "
	// + kernel.getName() + "(");
	//
	// // Dimension limits. Only need to pass these for X and Y, since OpenCL
	// // will
	// // never need to execute 'extra' threads in Z, and does not support
	// // higher
	// // dimensions.
	// for (int d = 0; (d < 2) && (d < kernel.getDimensions()); d++) {
	// if (d != 0) {
	// builder.append(", ");
	// }
	// // __global, __constant, __local, __private
	//
	// builder.append("constant int limit" + d);
	// }
	// /*
	// * __kernel void test(__global int4 *in_data, __global int4 *out_data) {
	// * __local int4 local_data; int id = get_local_id(0); local_data =
	// * in_data[id]; ...process data in local memory... out_data[id] =
	// * local_data; } In this code, the = operator is used twice to transfer
	// * data between global memory and local memory. The data doesnt have to
	// * be vector-based; the code will work just as well if int4 is changed
	// * to int. Similarly, to change from local memory to private memory,
	// * remove the __local specifier in the data declaration. The default
	// * specifier is __private, so the compiler will store variables without
	// * specifiers in private memory
	// */
	// // Variable kernel parameters (statics are passed via __constant__
	// // memory).
	// for (Parameter p : kernel.getRealParameters()) {
	// if (p.getState() instanceof Variable) {
	// builder.append(",__global " + getType(p.getState().getType()) + " "
	// + getName(p.getState()));
	// }
	// }
	//
	// builder.append(") {"+lineSeparator);
	//
	// /*
	// * global uint get_work_dim() Returns the number of dimensions in the
	// * kernels index space size_t get_global_size(uint dim) Returns the
	// * number of work-items for a given dimension size_t get_global_id(uint
	// * dim) Returns the element of the work-items global ID for a given
	// * dimension size_t get_global_offset(uint dim) Returns the initial
	// * offset used to compute global IDs
	// *
	// * workgroup
	// *
	// * size_t get_num_groups(uint dim) Returns the number of work-groups for
	// * a given dimension size_t get_group_id(uint dim) Returns the ID of the
	// * work-items work-group for a given dimension size_t get_local_id(uint
	// * dim) Returns the ID of the work-item within its work-group for a
	// * given dimension size_t get_local_size(uint dim) Returns the number of
	// * work-items in the work-group for a given dimension
	// *
	// * get_global_size tells you how many work-items are executing the same
	// * kernel. get_local_size tells you how many work-items are in the same
	// * work-group as the calling work-item.
	// */
	//
	// /*
	// * Example
	// *
	// * __kernel void id_check(__global float *output) { size_t global_id_0 =
	// * get_global_id(0); size_t global_id_1 = get_global_id(1); size_t
	// * global_size_0 = get_global_size(0); size_t offset_0 =
	// * get_global_offset(0); size_t offset_1 = get_global_offset(1); size_t
	// * local_id_0 = get_local_id(0); size_t local_id_1 = get_local_id(1);
	// * int index_0 = global_id_0 - offset_0; int index_1 = global_id_1 -
	// * offset_1; int index = index_1 * global_size_0 + index_0; float f =
	// * global_id_0 * 10.0f + global_id_1 * 1.0f; f += local_id_0 * 0.1f +
	// * local_id_1 * 0.01f; output[index] = f; }
	// */
	// // Calculate dimensions.
	// switch (kernel.getDimensions()) {
	// default:
	// case 3:
	// builder.append("int dim2 = get_global_id(2) * get_local_size(2);"+lineSeparator);
	// case 2:
	// builder.append("int dim1 = get_group_id(1) * get_local_size(1) + get_local_id(1);"+lineSeparator);
	// case 1:
	// builder.append("int dim0 = get_group_id(0) * get_local_size(0) + get_local_id(0);"+lineSeparator);
	// }
	//
	// // TODO: Allow variables that are incremented not just on a single
	// // dimension
	// // - or put in code elsewhere that puts it in the increment sets for
	// // each relevant dimension.
	//
	// // Perform increments.
	// for (int d = 0; (d < 2) && (d < kernel.getDimensions()); d++) {
	// for (Map.Entry<Variable, Integer> inc : kernel.getIncrements(d)
	// .entrySet()) {
	// builder.append(getName(inc.getKey()) + " += "
	// + inc.getValue().intValue() + " * dim" + d + ";"+lineSeparator);
	// }
	// }
	//
	// // Check limits (again only for 3 dimensions).
	// for (int d = 0; (d < 2) && (d < kernel.getDimensions()); d++) {
	// Variable index = kernel.getIndex(d);
	//
	// builder.append("if(" + getName(index));
	//
	// if (kernel.getIncrements(d).get(index).intValue() > 0) {
	// builder.append(" >= ");
	// } else {
	// builder.append(" <= ");
	// }
	//
	// builder.append("limit" + d + ") return;");
	// }
	// }
	//
	public static void kernelEnd(CLKernel kernel, PrintStream out) {
		out.println("}");
	}

	/**
	 * JavaCL kernel launcher
	 * 
	 * @param kernel
	 * @param out
	 */
	public static void launcher(CLKernel kernel, SClassFile classFile) {
// for performence, let the context be static
		
		
		classFile.addUniqueField(SField.make("private static "+getFullName(CLContext.class)
				+ " context = " + getFullName(JavaCL.class)
				+ ".createBestContext();"));
		
		
		
		// Creates prototype for launcher, according to JNI name mangling.
		SMethod method = new SMethod();

		String mStart = "public static void " + getSimpleName(kernel) + "(";
		// Limits for each of the requried dimensions.
		for (int d = 0; d < kernel.getDimensions(); d++) {
			if (d == 0)
				mStart += ("int limit" + d);
			else
				mStart += (", int limit" + d);
		}

		// CLKernel Parameters
		for (Parameter p : kernel.getRealParameters()) {
			if (p.getState().getType().getSort() == Type.Sort.REF) {
				mStart += (", " + CLHelper.getJavaType(p.getState().getType())
						+ " " + getName(p.getState()));
			} else {
				mStart += (", " + getJavaType(p.getState().getType()) + " " + getName(p
						.getState()));
			}
		}

		mStart += (") {");

		method.setStart(mStart);

		// Create a context and program using the devices discovered.
//		method.addInstruction(SInstruction.make(getFullName(CLContext.class)
//				+ " context = " + getFullName(JavaCL.class)
//				+ ".createBestContext();"));
		String queueClassName = getFullName(QueueProperties.class).replaceAll(
				"\\$", "\\.");
		method.addInstruction(SInstruction.make(queueClassName
				+ "[] props = new " + queueClassName + "[]{" + queueClassName
				+ ".ProfilingEnable};"));
		method.addInstruction(SInstruction.make(getFullName(CLQueue.class)
				+ " queue = context.createDefaultQueue(props);"));

		/*
		 * public static Pointer<Float> add(Pointer<Float> a, Pointer<Float> b)
		 * throws CLBuildException { int n = (int)a.getValidElements(); v *
		 * CLContext context = JavaCL.createBestContext(); CLQueue queue =
		 * context.createDefaultQueue();
		 * 
		 * String source =
		 * "__kernel void addFloats(__global const float* a, __global const float* b, __global float* output)     "
		 * +
		 * "{                                                                                                     "
		 * +
		 * "   int i = get_global_id(0);                                                                          "
		 * +
		 * "   output[i] = a[i] + b[i];                                                                           "
		 * +
		 * "}                                                                                                     "
		 * ;
		 * 
		 * CLKernel kernel =
		 * context.createProgram(source).createKernel("addFloats");
		 * CLBuffer<Float> aBuf = context.createBuffer(CLMem.Usage.Input, a,
		 * true); CLBuffer<Float> bBuf = context.createBuffer(CLMem.Usage.Input,
		 * b, true); CLBuffer<Float> outBuf =
		 * context.createBuffer(CLMem.Usage.Output, Float.class, n);
		 * kernel.setArgs(aBuf, bBuf, outBuf);
		 * 
		 * kernel.enqueueNDRange(queue, new int[]{n}); queue.finish();
		 * 
		 * return outBuf.read(queue);
		 */

		String kernelName = kernel.getName();
		method.addInstruction(SInstruction
				.make("String [] srcs = new String[]{" + kernelName + "};"));
		kernelName = "\"" + kernelName + "\"";
		// method.addInstruction(SInstruction.make(getFullName(com.nativelibs4java.opencl.CLKernel.class)+" kernel = context.createProgram( srcs ).createKernel("+kernelName+",new Object[]{});"));

		method.addInstruction(SInstruction
				.make(getFullName(com.nativelibs4java.opencl.CLKernel.class)
						+ " kernels[] = context.createProgram( srcs ).createKernels();"));

		method.addInstruction(SInstruction
				.make(getFullName(com.nativelibs4java.opencl.CLKernel.class)
						+ " kernel = kernels[0];"));

		// CLBuffer<Float> aBuf = context.createBuffer(CLMem.Usage.Input, a,
		// true);
		// CLBuffer<Float> bBuf = context.createBuffer(CLMem.Usage.Input, b,
		// true);
		// CLBuffer<Float> outBuf = context.createBuffer(CLMem.Usage.Output,
		// Float.class, n);
		// kernel.setArgs(aBuf, bBuf, outBuf);

		// kernel.enqueueNDRange(queue, new int[]{n});
		// queue.finish();

		// return outBuf.read(queue);

		// Import 'copy in' state.
		for (Parameter p : kernel.getRealParameters()) {
			// we need pointers if parameter is an array otherwise just a
			// variable - avoid this temp variable in future work
			Type type = p.getState().getType();
			if (isArryType(type)) {
				importState(p.getState(), p.getCopyOut(), method);
			} else {
				String name = getName(p.getState());
				method.addInstruction(SInstruction.make(getCLType(type) + " _"
						+ name + " = " + name + ";"));
			}
			// Statics reside in __constant__ memory.
			if (p.getState() instanceof Field) {
				// out.println("cudaMemcpyToSymbol(\"" + getName(p.getState())
				// + "\", &" + getName(p.getState()) + ", sizeof("
				// + getName(p.getState()) + "));");
			}
		}

		/**

		// set kernel param
		String setArgus = ("kernel.setArgs(");
		int i = 0;
		// Limits for each of the requried dimensions.
		for (i = 0; i < kernel.getDimensions(); i++) {
			if (i == 0)
				setArgus += ("limit" + i);
			else
				setArgus += (", limit" + i);
		}

		for (Parameter p : kernel.getRealParameters()) {
			if (i > 0)
				setArgus += (", ");
			setArgus += (" _" + getName(p.getState()));

			i++;
		}

		setArgus += (");");
		*/
		
		/**
		 * Fix as per discussed with chibe - javassist do not support varargs 
		 */
		
		// set kernel param
				String setArgus = ("kernel.setArgs(new Object[]{");
				int i = 0;
				// Limits for each of the requried dimensions.
				for (i = 0; i < kernel.getDimensions(); i++) {
					if (i == 0)
						setArgus += ("limit" + i);
					else
						setArgus += (", limit" + i);
				}

				for (Parameter p : kernel.getRealParameters()) {
					if (i > 0)
						setArgus += (", ");
					setArgus += (" _" + getName(p.getState()));

					i++;
				}

				setArgus += ("});");
		
		
		// out.println("Allocator::copyToDevice(e);");
		method.addInstruction(SInstruction.make(setArgus));
		// Calculate required iterations
		for (int d = 0; d < kernel.getDimensions(); d++) {
			Variable index = kernel.getIndex(d);
			method.addInstruction(SInstruction.make("int required" + d
					+ " = (limit" + d + " - " + getName(index) + " / "
					+ kernel.getIncrements(d).get(index) + ");"));
		}

		/*
		 * // Represent required for first 3 dimensions in CUDA structure.
		 * switch(kernel.getDimensions()) { case 1:
		 * out.println("dim3 required(required0);"); break; case 2:
		 * out.println("dim3 required(required0, required1);"); break;
		 * default:out
		 * .println("dim3 required(required0, required1, required2);");break; }
		 * 
		 * // Calculate how much can be done in a single invocation.
		 * out.println("dim3 gridSize;"); out.println("dim3 blockSize;");
		 * out.print("dim3 inc = calculateDimensions((void *) &" +
		 * kernel.getName());
		 * out.println(", &gridSize, &blockSize, required);");
		 * 
		 * // Debug execution size.
		 * if(Logger.getLogger("cuda").isDebugEnabled()) {
		 * out.print("printf(\"DEBUG: Required %dx%dx%d, Per-Invocation: %dx%dx%d "
		 * );
		 * out.print("(%dx%dx%d)\\n\", required.x, required.y, required.z, inc.x, "
		 * );
		 * out.println("inc.y, inc.z, blockSize.x, blockSize.y, blockSize.z);");
		 * }
		 * 
		 * // Iterate over outer dimensions (i.e. >= 3). // for(int d =
		 * kernel.getDimensions(); d >= 3; d--) { // out.println("for(int d" + d
		 * + "; d < required" + d + "; d++) {"); // }
		 * 
		 * // Iterate over inner 3 dimensions (as 'inc' maybe smaller than
		 * 'required'). // switch(kernel.getDimensions()) { // default: // case
		 * 3: out.println("for(int d2 = 0; d2 < required2; d2 += inc.z) {"); //
		 * case 2:
		 * out.println("for(int d1 = 0; d1 < required1; d1 += inc.y) {"); //
		 * case 1:
		 * out.println("for(int d0 = 0; d0 < required0; d0 += inc.x) {"); // }
		 */
		// out.print(kernel.getName() + "<<<gridSize, blockSize>>>(");

		// Dimension limits for kernel checks (the grid will normally invoke
		// extra
		// kernels around the edge).

		// in case of multiple events - kernel after kernel execution, in this
		// case one
		// kernel event would pass to next kernel wnqueue
	
		// after discussion with prof. chiba
		// why empty array not allowed? error on []{} instead of  []{null}
		
		method.addInstruction(SInstruction.make(getFullName(CLEvent.class)+ "[] eventsToWaitFor= new "+ getFullName(CLEvent.class)+"[]{null};"));
		int clEventNumber = 0;
		String enqueueNRange = (getFullName(CLEvent.class) + " clEvent"
				+ (++clEventNumber) + " = kernel.enqueueNDRange(queue, new int[]{");

		for (int d = 0; d < kernel.getDimensions(); d++) {
			if (d != 0) {
				enqueueNRange += (", ");
			}

			enqueueNRange += ("required" + d);
			// out.print("limit" + d);
		}
// after discussion with prof chiba => after discussion with prof chiba
		
		enqueueNRange += ("}, eventsToWaitFor);");

		method.addInstruction(SInstruction.make(enqueueNRange));

		// Kernel parameters.
		// for(Parameter p : kernel.getRealParameters()) {
		// // Only variables must be passed - statics are in constant memory.
		// if(p.getState() instanceof Variable) {
		// Variable var = (Variable) p.getState();
		//
		// out.print(", " + getName(var));
		//
		// for(int d = 0; d < kernel.getDimensions(); d++) {
		// if(kernel.getIncrements(d).containsKey(var)) {
		// out.print(" + (d" + d + " * " + kernel.getIncrements(d).get(var) +
		// ")");
		// }
		// }
		// }
		// }
		//
		// out.println(");");

		// for(int d = 0; d < kernel.getDimensions(); d++) {
		// out.println("}");
		// }

		// Export 'copy out' state.
		// out.println("Allocator::copyToHost();");

		// out.println("exportAll(e);");
		// out.println("Allocator::freeAll();");
		// out.println("kernel.enqueueNDRange(queue, new int[]{n});");
		method.addInstruction(SInstruction.make("queue.finish();"));

		// return outBuf.read(queue);

		// export 'copy out' state.
		for (Parameter p : kernel.getRealParameters()) {
			Type type = p.getState().getType();
			if (isArryType(type) || is2DArryType(type)) {
				exportState(p.getState(), p.getCopyOut(), method, "clEvent"
						+ clEventNumber,classFile);
			}

		}

		// Output function, and C export block ends.
		method.setEnd("}");
		classFile.addMethod(method);
		classFile.setEnd("}");

	}

	/**
	 * Full Name of the class
	 * 
	 * @param class1
	 * @return full name with package name
	 */
	private static String getFullName(Class<?> class1) {

		return class1.getName();
	}

	/**
	 * Simple Name of the class
	 * 
	 * @param class1
	 * @return full name with package name
	 */
	private static String getName(Class<?> class1) {

		return class1.getSimpleName();
	}

	/**
	 * copy data back to host from device
	 * 
	 * @param state
	 * @param copyOut
	 * @param out
	 * @param clEventsToWaitFor
	 *            - Array of events ... coma seperated- can be null
	 */
	/**
	private static void exportState(State state, boolean copyOut,
			SMethod method, String clEventsToWaitFor) {
		Type type = state.getType();
		String pType = getJavaCLPointerType(type);
		String name = getName(state);
		// special case
		pType = pType.replace("Integer", "Int");
		if (copyOut) {

			if (is2DArryType(type)) {
				String pName = "p_" + name;
				String pLengthVName = pName + "_length";

				String buffPointer = getJavaCLPointer(type);
				method.addInstruction(SInstruction.make(buffPointer + " "
						+ pName + " = _" + name + ".read(queue, "
						+ clEventsToWaitFor + ");"));

				method.addInstruction(SInstruction.make("long " + pLengthVName
						+ " = " + pName + ".getValidElements();"));
				method.addInstruction(SInstruction.make("for (int i = 0; i < "
						+ pLengthVName + " ; ++i)"));
				method.addInstruction(SInstruction.make("{"));
				method.addInstruction(SInstruction.make(pName + ".get(i).get"
						+ pType + "(" + name + "[i]);"));
				method.addInstruction(SInstruction.make("}"));
			} else if (isArryType(type)) {

				method.addInstruction(SInstruction.make("_" + name
						+ ".read(queue, " + clEventsToWaitFor + ").get" + pType
						+ "(" + name + ");"));

			}
		}

	}
	
	*/
	
	
	/**
	 * Fixed method as per discussion with prof. chiba (Remove all generics)
	 * copy data back to host from device
	 * 
	 * @param state
	 * @param copyOut
	 * @param out
	 * @param clEventsToWaitFor
	 *            - Array of events ... coma seperated- can be null
	 */
	private static void exportState(State state, boolean copyOut,
			SMethod method, String clEventsToWaitFor, SClassFile classFile) {
		Type type = state.getType();
		String pType = getJavaCLPointerType(type);
		String name = getName(state);
		// special case
		pType = pType.replace("Integer", "Int");
		
		String pointerClass = getFullName(Pointer.class);
		
		if (copyOut) {

			if (is2DArryType(type)) {
				
				String pName = "p_" + name;
				String pLengthVName = pName + "_length";

				String buffPointer = getJavaCLPointer(type);
				// Fix - as per discussion with prof chiba
				method.addInstruction(SInstruction.make(/*buffPointer +*/ pointerClass +" "
						+ pName + " = _" + name + ".read(queue, new " +getFullName(CLEvent.class)+"[]{"
						+ clEventsToWaitFor + "});"));

				method.addInstruction(SInstruction.make("long " + pLengthVName
						+ " = " + pName + ".getValidElements();"));
				method.addInstruction(SInstruction.make("for (int i = 0; i < "
						+ pLengthVName + " ; ++i)"));
				method.addInstruction(SInstruction.make("{"));
				// Fix - as per discussion with prof chiba
				method.addInstruction(SInstruction.make(pointerClass+ " tempPtr = ("+pointerClass+") "+pName + ".get((long)i);"));
				
				// special case
              if(pType.toLowerCase().contains("char")){
            		classFile.addUniqueMethod(Methods.copyCharArray);
            		method.addInstruction(SInstruction.make("copyCharArray(tempPtr.get"
    						+ pType + "()"+","+name+"[i]);"));
  
			
              }else{
            		method.addInstruction(SInstruction.make("tempPtr.get"
    						+ pType + "(" + name + "[i]);"));
              }
				method.addInstruction(SInstruction.make("}"));
			
			
			} else if (isArryType(type)) {
                if(pType.toLowerCase().contains("char")){
                	classFile.addUniqueMethod(Methods.copyCharArray);
            		method.addInstruction(SInstruction.make("copyCharArray( _" + name
    						+ ".read(queue, new "+getFullName(CLEvent.class)+"[]{ " + clEventsToWaitFor + "}).get" + pType
    						+ "(),"+name+");"));
                }
                else{
				method.addInstruction(SInstruction.make("_" + name
						+ ".read(queue, new "+getFullName(CLEvent.class)+"[]{ " + clEventsToWaitFor + "}).get" + pType
						+ "(" + name + ");"));
                }
			}
		}

	}
	

	public static void methodStart(Method method, PrintStream out) {
		// Creates prototype for function, according to JNI name mangling.
		if(method.getModifiers().contains(Modifier.STATIC))
			out.print("static ");
		out.print(getCLType(method.getReturnType()) + " ");
		out.print(getSimpleName(method) + "(");

		// Arguments.
		out.print(Utils.join(
				new TransformIterable<Variable, String>(method
						.getParameterVariables()) {
					@Override
					protected String transform(Variable variable) {
						if(CLHelper.isArryType(variable.getType()))
							return "__global "+getCLType(variable.getType()) + " "
							+ getName(variable);
			
							return getCLType(variable.getType()) + " "
								+ getName(variable);
					}
				}, ", "));

		out.println(") {");
	}

	public static void methodEnd(Method method, PrintStream out) {
		out.println("}");
	}

	public static String getAsString(ByteArrayOutputStream bytes) {
		String stringValue = "";
		String lines[] = bytes.toString().split(CLHelper.lineSeparator);
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			stringValue += "\" ";
			stringValue += line;
			if (i < lines.length - 1) {
				stringValue += spaces(line.length()) + " \"+";
				stringValue += CLHelper.lineSeparator;
			}

		}

		return stringValue;
	}


private static String spaces(int length) {
	String spaces = "";
	for (int i = 0; i < Config.spacesBeforePlusSignInKernel - length; i++) {
		spaces += " ";
	}
	return spaces;
}
}
/*
 * Table 5.4 Arithmetic and rounding functions Function Purpose floatn
 * fabs(floatn x) Returns the absolute value of the argument, |x| floatn
 * fma(floatn a, floatn b, floatn c) Returns a * b + c, where the multiplication
 * is performed with precision floatn fmod(floatn x, floatn y) Returns the
 * modulus of x and y: x (y * trunc(y/x)) floatn mad(floatn a, floatn b, floatn
 * c) Returns a * b + c floatn remainder(floatn x, floatn y) Returns the
 * remainder of x and y: x n * y, where n is the integer closest to x/y floatn
 * remquo(floatn x, floatn y, __(g|l|p) *quo) Returns the remainder of x and y:
 * x n * y, where n is the integer closest to x/y; places the signed lowest
 * seven bits of the quotient (x/y) in quo floatn rint(floatn x) Returns the
 * closest integer as a floatif two integers are equally close, it returns the
 * even integer as a float floatn round(floatn x) Returns the integer closest to
 * xif two integers are equally close, it returns the one farther from 0 floatn
 * ceil(floatn x) Returns the closest integer larger than x floatn floor(floatn
 * x) Returns the closest integer smaller than x floatn trunc(floatn x) Removes
 * the fractional part of x and returns the integer
 */

/*
 * Table 5.5 Comparison functions Function Purpose floatn clamp(floatn x,
 * float/n min, float/n max) Returns min if x < min; returns max if x > max;
 * otherwise returns x floatn fdim(floatn x, floatn y) Returns x y if x > y;
 * returns 0 if x <= y floatn fmax(floatn x, float/n y) Returns x if x >= y;
 * returns y if y > x floatn fmin(floatn x, float/n y) Returns x if x <= y;
 * returns y if y < x floatn max(floatn x, float/n y) Returns x if x >= y;
 * returns y if y > x floatn min(floatn x, float/n y) Returns x if x <= y;
 * returns y if y < x floatn mix(floatn x, floatn y, float/n a) Interpolates
 * between x and y using the equation x + (y x) * a, where 0.0 < a < 1.0 floatn
 * maxmag(floatn x, floatn y) Returns x if |x| >= |y|; returns y if |y| > |x|
 * floatn minmag(floatn x, floatn y) Returns x if |x| <= |y|; returns y if |y| <
 * |x| floatn step(float/n edge, floatn x) Returns 0.0 if x < edge; returns 1.0
 * if x >= edge floatn smoothstep(float/n edge1, float/n edge2, floatn x)
 * Returns 0.0 if x <= edge1; returns 1.0 if x >= edge1; uses smooth
 * interpolation if edge0 < x < edge1
 */
/*
 * Table 5.6 Exponential and logarithmic functions Function Purpose floatn
 * pow(floatn x, floatn y) Returns xy floatn pown(floatn x, intn y) Returns xy,
 * where y is an integer floatn powr(floatn x, floatn y) Returns xy, where x is
 * greater than or equal to 0 floatn exp/expm1(floatn x) Returns ex and ex 1
 * floatn exp2/exp10(floatn x) Returns 2x and 10x floatn ldexp(floatn x, intn
 * n)) Returns x * 2n floatn rootn(floatn x, floatn y) Returns x1/y floatn
 * sqrt/cbrt(floatn x) Returns the square root/cube root of x floatn
 * rsqrt(floatn x) Returns the inverse square root of x floatn log/log1p(floatn
 * x) Returns ln(x) and ln(1.0 + x) floatn log2/log10(floatn x) Returns log2 x
 * and log10 x floatn logb(floatn x) Returns the integral part of log2 x floatn
 * erf/erfc(floatn x) Returns the error function and the complementary error
 * function floatn tgamma/lgamma(floatn x) Returns the gamma function and the
 * log gamma function floatn lgamma_r(floatn x, __(g|l|p) intn *mem) Returns the
 * log gamma function and places the sign in the memory referenced by mem
 */

/*
 * 
 * Table 5.7 Trigonometric functions Function Purpose floatn sin/cos/tan(floatn)
 * Returns the sine, cosine, and tangent floatn sinpi/cospi/tanpi(floatn x)
 * Returns the sine, cosine, and tangent of x floatn asin/acos/atan(floatn)
 * Returns the arcsine, arccosine, and arctangent floatn asinpi/acospi/atanpi
 * (floatn x) Returns the arcsine, arccosine, and arctangent of x floatn
 * sinh/cosh/tanh(floatn) Returns the hyperbolic sine, cosine, and tangent
 * floatn asinh/acosh/atanh(floatn) Returns the hyperbolic arcsine, arccosine,
 * and arctangent floatn sincos(floatn x, __(g|l|p) floatn *mem) Returns the
 * sine of x and places the cosine in the memory referenced by mem floatn
 * atan2/atan2pi(floatn x) Returns the sine, cosine, and tangent of x
 */

/*
 * 
 * Table 5.8 Floating-point constants Constant Content Constant Content M_E_F
 * Value of e M_1_PI_F Value of 1/ M_LOG2E_F Value of log2e M_2_PI_F Value of 2/
 * M_LOG10E_F Value of log10e M_2_SQRTPI_F Value of 2/sqrt() M_LN2_F Value of
 * loge2 M_SQRT2_F Value of sqrt(2) M_LN10_F Value of loge10 M_SQRT1_2_F Value
 * of 1/sqrt(2) M_PI_F Value of MAXFLOAT Maximum float value M_PI_2_F Value of
 * /2 HUGE_VALF Positive floating-point infinity M_PI_4_F Value of /4 INFINITY
 * Unsigned infinity NAN Not-a-Number value
 */
