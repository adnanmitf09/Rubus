package rubus;
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

import graph.ClassNode;
import graph.Loop;
import graph.Method;
import graph.Modifier;
import graph.TrivialLoop;
import graph.Type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;

import util.FileUtil;
import util.Tree;
import util.Utils;
import analysis.dependency.AnnotationCheck;
import analysis.dependency.BasicCheck;
import analysis.dependency.CombinedCheck;
import analysis.dependency.DependencyCheck;
import analysis.loops.LoopDetector;
import analysis.loops.LoopNester;
import analysis.loops.LoopTrivialiser;
import bytecode.ClassExporter;
import bytecode.ClassFinder;
import bytecode.ClassImporter;
import cl.CLExporter;
import cl.CLKernelExtractor;
import codeexport.SourceExporter;

/**
 *
 */
public class Rubus {

	public static void main(String[] args) {

		// args = new String[]{ "-e", "-o", "-a","-m","--path","."
		// ,"./bin/benchmark/current/" };

		Rubus rubus = new Rubus();

		if (args == null || args.length == 0) {
			rubus.initializeUI();
		} else {

			rubus.parseArguments(args);
			rubus.transform();
		}
	}

	private void initializeUI() {
		RubusGUI.main(null);

	}

	@Option(name = "-export", usage = "Export generate kernel and executor code")
	private boolean export;
	@Option(name = "-open", usage = "Open generated source file")
	private boolean openAfterExport = false;

	@Option(name = "--path", usage = "Class path to serach class files from. All path should be separated by comma ','. ")
	private List<File> classPath = new ArrayList<File>();

	@Option(name = "--dist", usage = "Destination path to export transformed code")
	private File destination = new File(".");

	@Option(name = "-debug", usage = "Should show logs")
	private boolean debug = false;

	@Option(name = "-auto", usage = "Should perform auto analysis")
	private boolean auto = true;

	@Option(name = "-manual", usage = "Should perform manual analysis")
	private boolean manual = false;

	@Option(name = "-clean", usage = "Clear destination dir before this run")
	private boolean clearDist = false;

	// All classes to transform
	@Argument
	private List<String> arguments = new ArrayList<String>();

	public static UILogger uiLogger;
	public static UILogger transformedFilesLogger;

	private void parseArguments(String args[]) {
		CmdLineParser parser = new CmdLineParser(Rubus.this);
		parser.setUsageWidth(80);

		try {
			// parse the arguments.
			parser.parseArgument(args);

			// you can parse additional arguments if you want.
			// parser.parseArgument("more","args");

			// after parsing arguments, you should check
			// if enough arguments are given.
			if (arguments.isEmpty())
				throw new CmdLineException(parser, "No argument is provided");

		} catch (CmdLineException e) {

			System.err.println(e.getMessage());
			System.err.println("java Rubus [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println(" Example: java Rubus "
					+ parser.printExample(ExampleMode.ALL));

			return;
		}

		if (classPath != null && !classPath.isEmpty()) {
			ClassFinder.setClassPath(classPath);
		}

	}

	public void transform() {

		BasicConfigurator.configure();
		// Setup Logging
		Logger rootLogger = Logger.getRootLogger();
		
	
		addFileAppender(rootLogger);
		
		
		rootLogger.setLevel(Level.OFF);
		if (debug) {
			rootLogger.setLevel(Level.DEBUG);
		}

		if (clearDist) {
			Utils.deleteDirectory(destination);
		}

		// Select dependency checker.
		DependencyCheck check;

		if (auto && manual) {
			check = new CombinedCheck(new AnnotationCheck(), new BasicCheck());
		} else if (manual) {
			check = new AnnotationCheck();
		} else {
			check = new BasicCheck();
		}

		ClassExporter.setOutputDirectory(destination);

		Set<String> classes = new HashSet<String>();
		if (classPath == null || classPath.isEmpty()) {
			classPath = new ArrayList<File>();
			ArrayList<String> fileNames = new ArrayList<String>();
			for (int i = 0; i < arguments.size(); i++) {
				String input = arguments.get(i);
				File file = new File(input);
				
				if (file.getName().toLowerCase().endsWith(".class") && !classPath.contains(file.getParent())) {
				    classPath.add(file.getParentFile());
					fileNames.add(file.getName().replace(".class", ""));
									
				}else if(file.getName().toLowerCase().endsWith(".jar")){
					 classPath.add(file);
					 fileNames.add(file.getName().replace(".jar", ""));
				}
				System.out.println(file.getName().replace(".class", "").replace(".jar", ""));
			
			}
			arguments = fileNames;
			ClassFinder.setClassPath(classPath);
		}

		for (String input : arguments) {
			classes.addAll(ClassFinder.listClasses(input.toString().replace(
					".", "/")));
		}

		// else{
		// classPath = new ArrayList<File>();
		//
		// for(String input : arguments) {
		// input = input.replaceAll("/./", "/");
		//
		// if(input.toLowerCase().endsWith(".class"))
		// {
		// File file = new File(input);
		// if(!classPath.contains(file.getParent())){
		//
		// classPath.add(file.getParentFile());
		// }
		// classes.add(file.getName());
		// }
		// }
		//
		// ClassFinder.setClassPath(classPath);
		// }

		if (classes.size() == 0) {
			log("No class file found for analysis.");
			return;
		}

		// Apply Transformations
		for (String className : classes) {
			// mac file name problem solved
			className = className.replaceAll("/./", "/");

			ClassNode clazz = ClassImporter.getClass(className);
			log("Considering class: " + clazz.getName());
			try {

				CLExporter.setDestination(destination, clazz.getName());

			} catch (IOException ex) {
				ex.printStackTrace();
				log("Exception occured: "+ex.getMessage());
			}

			boolean transformed = false;
			int loopCount = 0;
			for (Method m : clazz.getMethods()) {
				if (!validMethodForTransfor(m))
					continue;

				Logger.getLogger("Rubus").info("Transforming " + m);

				log("Considering Method: " + m);

				log("Detecting Loop(s)...");
				// Detect Loops.
				Set<Loop> loops = LoopDetector.detect(m.getImplementation());

				// Trivialise Loops.
				Set<TrivialLoop> tloops = LoopTrivialiser.convert(loops);

				// Calculate Nesting.
				Set<Tree<TrivialLoop>> nesting = LoopNester.nest(tloops);

				check.setContext(m);

				// Extract Kernels
				 loopCount = CLKernelExtractor
						.extract(clazz, nesting, check);
				if (loopCount > 0) {
					transformed = true;
					log("Loop(s) Accepted : " + loopCount);
				} else {

					log("No Loop Accepted ");

				}
			}

			if (transformed) {
				if (transformedFilesLogger != null)
					transformedFilesLogger.Log(clazz.getName());
				log("Exporting class : " + clazz);
				ClassExporter.export(clazz);

				try {

					CLExporter.compile(clazz,
							ClassExporter.getOutputDirectory());
				} catch (Exception ex) {
					ex.printStackTrace();
					log("Error while exporting class file: "+ex.getMessage());

				}

				/**
				 * Actual generated source export
				 */
				if (export) {
					log("Exporting source : " + clazz);

					SourceExporter srcExporter = new SourceExporter();
					srcExporter.visit(CLExporter.sourceFile);
				}
				/**
				 * open file
				 */
				if (openAfterExport) {
					log("Opening source file : " + clazz);

					FileUtil.openFile(CLExporter.sourceFile
							.getDestinationFile());

				}
			}

		}
		
		log("Transformation Completed...!!!");

		System.out.println("Transformation Completed...!!!");

	}

	private void addFileAppender(Logger rootLogger) {
		FileAppender fa = new FileAppender();
		  fa.setName("FileLogger");
		  fa.setFile("rubus.log");
		  fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		  fa.setThreshold(Level.DEBUG);
		  fa.setAppend(true);
		  fa.activateOptions();

		  //add appender to any Logger (here is root)
		  Logger.getRootLogger().addAppender(fa);
		
	}

	private void log(String message) {
		if (debug) {
			if (uiLogger != null) {
				uiLogger.Log(message);
				System.out.println(message);
			}

		}

	}

	private boolean validMethodForTransfor(Method m) {

		return m.getImplementation() != null
				&& !m.getModifiers().contains(Modifier.INHERITED)
				&& m.getAnnotation(Type.getObjectType("annotation/Ignore")) == null;

	}

	public static String getEnvironment(String key, String def) {
		String result = System.getenv(key);

		if (result == null) {
			return def;
		} else {
			return result;
		}
	}
}
