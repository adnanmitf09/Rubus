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
import java.io.PrintStream;

import util.Utils;

/**
 * Attempts to make outputted curly-brace style code slightly easier to read by
 * inserting some indentation and spacing between methods.
 */
public class CLBeautifier extends PrintStream {
  /**
   * Number of spaces per `tab' or indent.
   */
  public static final int TAB = 5;

  /**
   * Current indentation (measured in tabs).
   */
  public int indent = 0;

  /**
   * Wraps a file so that code written to it is correctly indented.
   *
   * @param out    Output stream to wrap.
   */
  public CLBeautifier(File out) throws FileNotFoundException {
    super(out);
  }

  /**
   * Wraps an existing output stream so that code written to it is correctly
   * indented.
   *
   * @param out    Output stream to wrap.
   */
  public CLBeautifier(OutputStream out) {
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
    indent += Utils.count("{", string);

    if(indent > 0) {
      indent -= Utils.count("}", string);
    }
    super.print(string);
  }

  /**
   * Acts identically to <code>print(string); println();</code>.
   *
   * @param string String of code.
   */
  @Override
  public void println(String string) {
    print(string);
    println();
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
