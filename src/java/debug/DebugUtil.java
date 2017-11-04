package debug;

import graph.Block;
import graph.Loop;
import graph.TrivialLoop;

import java.io.PrintStream;
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

public class DebugUtil {
	
	public static final String BASIC_BLOCK_PROPERTIES = "[shape=circle];";
	public static final String LOOP_PROPERTIES = "[shape=doublecircle];";
	public static final String TRIVIAL_LOOP_PROPERTIES = "[shape=Mcircle];";
	
	public static void printNodeDiscription(PrintStream ps) {
		  ps.println("\"BB\" "+BASIC_BLOCK_PROPERTIES);
		  ps.println("\"L\" "+LOOP_PROPERTIES);
		  ps.println("\"TL\" "+TRIVIAL_LOOP_PROPERTIES);

		   
		
	}

	public static void printNode(Block b, PrintStream ps) {
		 ps.println("\"" + b.getID() + "\""+ BASIC_BLOCK_PROPERTIES +" ");
		
	}
	public static void printNode(Loop b, PrintStream ps) {
		 ps.println("\"" + b.getID() + "\""+ LOOP_PROPERTIES +" ");
		
	}
	public static void printNode(TrivialLoop b, PrintStream ps) {
		 ps.println("\"" + b.getID() + "\""+ TRIVIAL_LOOP_PROPERTIES +" ");
		
	}

	public static void printProperties(PrintStream outPS) {
		    outPS.println("d2toptions =\"-fpgf --figonly --figpreamble=\\tiny\";");
	        outPS.println("graph [  ranksep=0.3,minlen=0.5,nodesep=0.3];");
	        outPS.println("node [shape=circle, margin=0, width=\"0.3\", fontsize=10];");
		
	}

}
