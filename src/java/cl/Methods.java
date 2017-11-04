package cl;
import codeexport.SInstruction;
import codeexport.SMethod;
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

public class Methods {
  public static SMethod copyCharArray = new SMethod(){
	  {setStart("public static void copyCharArray(char [] src , char [] dst){    ");
	  
	  addInstruction(SInstruction.make("if (src == null || dst == null || src.length != dst.length) return;"));
	  addInstruction(SInstruction.make("for (int i = 0 ; i < src.length ; i++ )"));
	  addInstruction(SInstruction.make("   dst[i] = src[i];"));
	  
	  setEnd("}");
	  }
	  
  };
  
  }
