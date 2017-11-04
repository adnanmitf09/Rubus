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

public class Observations {

/**
 *  
 *  call to 'min' is ambiguous
 *  
 *  <stdin>:3640:38: note: candidate function
 *  short2 __attribute__((overloadable)) min(short2 x, short y);
 *  
 *  if you have used a constant in min function ie min(x, 255). 255 will convert to short instead of 
 *  int and will cause this error. Manuly convert short to int or appropriate type, for floates there is some other method 
 *  like fmin and same with max and some other methods
 *  
 *  
 *  Some time there is no error but wrong output : I caused this due to in some place I used int instead of float, be careful
 *   about data types
 *   
 *   Data type Map: https://code.google.com/p/javacl/wiki/UnderstandOpenCLKernelArguments
 *  
 *  Sometimes I get exception 
 *  com.nativelibs4java.opencl.CLException$InvalidCommandQueue: InvalidCommandQueue
 *   in opencl but no trace. That was due to I was accessing an array
 *  out of bound. Be careful about array sizes 
 *  
 *  
 *  to write in document: dont use hard coded value, it might leads to imbigus method error because data type is 
 *  generated based on value, like 255 will goes to short instead of int and will case a kernel or math function failure. 
 *  
 *  dont use double
 *  
 *  dont use char,    your byte is equlant to char in opencl	
 */
	
/**
 * Helpful Links - java code snnipts
 * http://www.codeproject.com/Articles/227478/Cool-Java-Programs
 * http://viralpatel.net/blogs/20-useful-java-code-snippets-for-java-developers/
 * 	
 */

}
