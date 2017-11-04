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
import graph.BlockVisitor;
import graph.ClassNode;
import graph.Method;
import graph.Type;
import graph.instructions.Call;
import graph.instructions.Constant;
import graph.instructions.Producer;
import graph.instructions.Return;
import graph.state.Field;
import graph.state.State;
import graph.state.Variable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import analysis.dataflow.SimpleUsed;
import cl.Config.KernelExportOption;
import codeexport.ClassMerger;
import codeexport.SClassFile;
import codeexport.SField;

/**
 * Top level class for export of code to OpenCL. Used both to invoke other
 * exporter classes (<code>cuda.BlockExporter</code>) and the NVCC compiler
 * itself.
 */
public class CLExporter {
	/**
	 * source model class file
	 */
	static public SClassFile sourceFile;

	/**
	 * Set of methods that have been exported to OpenCL.
	 */
	static private Set<Method> exported = new HashSet<Method>();

	/**
	 * Set of static fields 
	 */
	static private Set<Field> statics = new HashSet<Field>();


	/**
	 * Print stream for source exports.
	 */
	//static private PrintStream out;

	/**
	 * File for source output.
	 */
//	static public File source;

	/**
	 * Output class name.
	 */
  static private String outputClassName;

	

	
	/**
	 * Source dir
	 */
	static private File srcDir;



//	public static String getGeneratedSourceFilePath() {
//		if (source != null)
//			return source.getAbsolutePath();
//		else {
//			return new File(srcDir, System.mapLibraryName(outputClassName)
//					+ ".java").getAbsolutePath();
//		}
//	}

	/**
	 * Sets the destinations for OpenCL exports. These can be changed, but some
	 * methods may then be reexported, as exports are independent.
	 * 
	 * @param dir
	 *            Directory in which outputs should occur.
	 * @param className
	 *            Name of the destination library.
	 * @param compile
	 *            Whether the source should be compiled or not.
	 */
	public static void setDestination(File dir, String className)
			throws IOException {
		srcDir = new File(dir.getPath() + "/"
				);
		outputClassName = className;

		// Create directory.
		srcDir.mkdirs();
//    	source = new File(srcDir, outputClassName + ".java");
//    	source.getParentFile().mkdirs();
		sourceFile = new SClassFile(srcDir);
		sourceFile.setName(outputClassName);
		sourceFile.setExtention(".java");
		// Create Printstream.
		//out = new CLBeautifier(source);

		// File Header
		CLHelper.writeFileStart(sourceFile);
	//	out.println();

		// Clear export sets.
		exported.clear();
		statics.clear();
		
	}

	/**
	 * Exports a kernel to the current OpenCL file.
	 * 
	 * @param kernel
	 *            CLKernel to export.
	 */
	public static void export(CLKernel kernel) throws CLUnsupportedInstruction {
		// Check not already exported.
		if (exported.contains(kernel))
			return;
		
		// Create temporary printstream.
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream temp = new CLBeautifier(bytes);
		
		// Calculate state used by kernel.
		SimpleUsed used = new SimpleUsed(kernel.getImplementation());

		// Define statics used in kernel (and functions called by it).

		for (Field f : used.getStatics()) { if (!statics.contains(f)) {
			temp.println("static " + CLHelper.getCLType(f.getType()) + " " +
					CLHelper.getName(f) + ";"); statics.add(f); } }

		// Define classes used in kernel (and functions called by it).
		// for (ClassNode c : used.getClasses()) {
		// if (!classes.contains(c)) {
		// CLHelper.defineClass(c, out);
		// classes.add(c);
		// }
		// }
	

		// Head
		CLHelper.kernelStart(kernel, temp);

		// String a[] = bytes.toString().split(CLHelper.lineSeparator);
		//
		// for (String x : a) {
		// out.println(x);
		// }

		// Declare local variables.
		final Set<Variable> varDeclare = new HashSet<Variable>(
				used.getVariables());

		varDeclare.removeAll(kernel.getParameterVariables());

		for (State s : varDeclare) {
			temp.println(CLHelper.getJavaType(s.getType()) + " "
					+ CLHelper.getName(s) + ";");
		}

		// Declare stack restoration variables.
		for (int i = 0; i < used.getStackCount(); i++) {
			for (Type t : used.getStackTypes(i)) {
				temp.println(CLHelper.getJavaType(t) + " s"
						+ CLHelper.getName(new Variable(i, t)) + ";");
			}
		}

		// Export Body
		BlockVisitor<Void> ke = null;

		if (Config.kernelExportOptions == KernelExportOption.GOTO_LABEL) {
			ke = new CLBlockExporter(temp);
		} else if (Config.kernelExportOptions == KernelExportOption.METHODS) {
			ke = new CLBlockAsFunctionExporter(temp, kernel);
		} else {
			ke = new CLSimpleBlockExporter(temp);
		}

		kernel.getImplementation().accept(ke);

		// Tail
		CLHelper.kernelEnd(kernel, temp);

		temp.flush();

		String kernelFieldDeclaration = "private static final String "
				+ kernel.getName() + " = \"\"+" + CLHelper.lineSeparator;
		try {
			// out.write(bytes.toByteArray());
			//

			String kernelField = CLHelper.getAsString(bytes);



			/**
			 * TODO: Do this method merging work somewhere else
			 */
			String methodsSource ="";			
			for (Method m : exported) {
				if(m instanceof CLKernel){
					// do nothing
				}else if(m instanceof Method && m.getSource()!=null){
					methodsSource+= m.getSource()+"\"+ \n";
				}
			}


			SField field = new SField(kernelFieldDeclaration+ methodsSource+kernelField+"\";");
			sourceFile.addField(field);

			// out.write(kernelString.getBytes());
		} catch (Exception ex) {
			throw new RuntimeException(
					"Unexpected file error in exporting kernel.");
		}

		bytes.reset();

		temp.println("\n");

		// Launcher
		CLHelper.launcher(kernel, sourceFile);

		// Commit export to file.
		/*
		try {
			out.write(bytes.toByteArray());
			//
			// String kernelString = "private static final String " +
			// kernel.getName() + " = \"\"+\n";
			// String lines[] = bytes.toString().split(CLHelper.lineSeparator);
			// for (int i = 0; i < lines.length; i++) {
			// String line = lines[i];
			//
			// kernelString+="+\" ";
			// kernelString+=line;
			// if(i<lines.length-1){
			// kernelString+=" \"+";
			// kernelString+="\n";
			// }
			//
			// }
			// kernelString+="\";";
			// out.write(kernelString.getBytes());
		} catch (IOException ex) {
			throw new RuntimeException(
					"Unexpected file error in exporting kernel.");
		}
*/
		// Mark kernel as being native.
		// kernel.getModifiers().add(Modifier.NATIVE);

		// Add to exported set.
		exported.add(kernel);





	}

//	public static  void flush(){
//		out.flush();
//		out.close();
//
//	}

	/**
	 * Exports a standard method to the current OpenCL file as a device
	 * function.
	 * 
	 * @param method
	 *            Method to export.
	 */
	public static void export(Method method) throws CLUnsupportedInstruction {
		// Check not already exported.
		if (exported.contains(method))
			return;


		//		Set<Loop> loops = LoopDetector.detect(method.getImplementation());
		//		Set<TrivialLoop> tloops = LoopTrivialiser.convert(loops);
		//		Set<Tree<TrivialLoop>> nesting = LoopNester.nest(tloops);



		// Create temporary printstream.
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream temp = new CLBeautifier(bytes);

		// Head
		CLHelper.methodStart(method, temp);

		// Declare local variables.
		SimpleUsed used = new SimpleUsed(method.getImplementation());
		final Set<Variable> varDeclare = new HashSet<Variable>(
				used.getVariables());

		varDeclare.removeAll(method.getParameterVariables());

		for (State s : varDeclare) {
			temp.println(CLHelper.getCLType(s.getType()) + " "
					+ CLHelper.getName(s) + ";");
		}

		// Declare stack restoration variables.
		for (int i = 0; i < used.getStackCount(); i++) {
			for (Type t : used.getStackTypes(i)) {
				temp.println(CLHelper.getCLType(t) + " s"
						+ CLHelper.getName(new Variable(i, t)) + ";");
			}
		}

		// Export Body
		CLSimpleBlockExporter ke = new CLSimpleBlockExporter(temp);
		method.getImplementation().accept(ke);

		//CLSimpleBlockExporter ke = new CLSimpleBlockExporter(new PrintStream(new ByteArrayOutputStream()));
		//		method.getImplementation().accept(ke);
		//ke.setPs(temp);
		//ke.setVisited(new HashSet<Block>());
		//		method.getImplementation().accept(ke);

		// Tail
		CLHelper.methodEnd(method, temp);

		// Commit export to file.
		//		try {
		//			//out.write(bytes.toByteArray());
		//		} catch (IOException ex) {
		//			throw new RuntimeException(
		//					"Unexpected file error in exporting method.");
		//		}
		method.setSource(CLHelper.getAsString(bytes));

		// Add to exported set.
		exported.add(method);
	}


	public static void addLoad(ClassNode clazz) {

		// add import static here
		Method clinit = clazz.getMethod("<clinit>", "()V");

		BasicBlock bb = new BasicBlock();
		ClassNode system = ClassNode.getClass("java/lang/System");

		bb.getStateful().add(
				new Call(new Producer[] { new Constant(outputClassName) }, system
						.getMethod("loadLibrary", "(Ljava/lang/String;)V"),
						Call.Sort.STATIC));

		if (clinit.getImplementation() == null) {
			bb.setBranch(new Return());
			clinit.setImplementation(bb);
		} else {
			bb.setNext(clinit.getImplementation());
			clinit.setImplementation(bb);
		}
	}

	public static void compile(ClassNode clazz, File outDir) {
		//	File f = new File(outDir, clazz.getName() + ".class");
		try {

			ClassPool pool = ClassPool.getDefault();//new ClassPool();
			pool.insertClassPath(outDir.toString());
			//ClassPool.getDefault().appendClassPath(f.getParent());
			String classname = clazz.getName().replaceAll("/", "\\.");
			ClassMerger merger = new ClassMerger(pool.getCtClass(classname),outDir.getPath());
			merger.visit(sourceFile);
		} catch (Exception e) {
			e.printStackTrace();
			//throw new RuntimeException("Could not create class file: " + f);
		}

	}

}
