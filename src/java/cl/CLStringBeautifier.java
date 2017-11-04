
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * This class is apparently used to write kernel as string in java file
 * It adds "+ at start of each line and +" at end of each line
 */
public class CLStringBeautifier extends CLBeautifier {
  

  /**
   * Wraps a file so that code written to it is correctly indented.
   *
   * @param out    Output stream to wrap.
   */
  public CLStringBeautifier(File out) throws FileNotFoundException {
    super(out);
  }

  /**
   * Wraps an existing output stream so that code written to it is correctly
   * indented.
   *
   * @param out    Output stream to wrap.
   */
  public CLStringBeautifier(OutputStream out) {
    super(out);
  }

  /**
   * When outputting a string, this first alters the indentation accordingly
   * before acting as a standard <code>PrintStream</code>.
   *
   * @param string String of code.
   */
  @Override
  public void print(String string) {
    
	  super.print(string);
  }

  /**
   * Acts identically to <code>print(string); println();</code>.
   *
   * @param string String of code.
   */
  @Override
  public void println(String string) {
    print("+\"    "+string+"    \"+\n");
    println();
  }

	/**
	 * Start of string
	 */
	public void printStart(String string) {
		print("\"" + string);

	}
	
	
	/**
	 * End of string
	 */
	public void printEnd(String string) {
	
		print("+\"    "+string+";    \"");

	}

  /**
   * For each new line this inserts the current indent, and spacing if at the
   * unindented `root' level.
   */
  @Override
  public void println() {
    super.println();

    // Gaps between methods/structs etc.
    if(indent == 0) {
      super.println();
    } else {
      for(int i = 0; i < indent * TAB; i++) {
        super.print(" ");
      }
    }
  }
}
