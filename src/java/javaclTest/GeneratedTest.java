package javaclTest;
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

import static com.nativelibs4java.opencl.JavaCL.createBestContext;
import static org.bridj.Pointer.pointerToInts;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

public class GeneratedTest {
  
	 private static final String kernel_1196558885 = ""+
			 " __kernel void kernel_1196558885(                             "+
			 "                                                              "+
			 " const int limit0, __global int* v1_2894, int v2_INT) {       "+
			 "   int dim0 = get_global_id(0);                               "+
			 "   v2_INT += 1 * dim0;                                        "+
			 "   if(v2_INT >= limit0) return;                               "+
			 "   int v3_INT;                                                "+
			 "   {                                                          "+
			 "     __global int* t0 = v1_2894;                              "+
			 "      int t1 = v2_INT;                                        "+
			 "      int t2 = t0[t1];                                        "+
			 "     v3_INT = t2;                                             "+
			 "     }                                                        "+
			 "   {                                                          "+
			 "     v3_INT += 1;                                             "+
			 "     }                                                        "+
			 "   {                                                          "+
			 "     __global int* t0 = v1_2894;                              "+
			 "      int t1 = v2_INT;                                        "+
			 "      int t2 = v3_INT;                                        "+
			 "     t0[t1] = t2;                                             "+
			 "     }                                                        "+
			 "   {                                                          "+
			 "     v2_INT += 1;                                             "+
			 "     }                                                        "+
			 "   {                                                          "+
			 "     }                                                        "+
			 "   }";


			 public static void activesamples_Loop_kernel_11196558885(int limit0, int[] v1_2894, int v2_INT) {
			   CLContext context = createBestContext();
			   CLQueue queue = context.createDefaultQueue();
			   CLKernel kernel = context.createProgram(kernel_1196558885).createKernel("kernel_1196558885");
			   CLBuffer<Integer> _v1_2894 = context.createBuffer(CLMem.Usage.InputOutput, pointerToInts(v1_2894), true);
			   int _v2_INT = v2_INT;
			   kernel.setArgs(limit0,  _v1_2894,  _v2_INT);
			   int required0 = (limit0 - v2_INT) / 1;
			   CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0});
			   queue.finish();
			   _v1_2894.read(queue, clEvent1).getInts(v1_2894);
			   }

			 }
