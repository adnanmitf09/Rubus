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

import static org.bridj.Pointer.pointerToInt;
import static org.bridj.Pointer.pointerToInts;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

public class SimpleAdd {

	public static void main(String[] args) {
		

		try {

			 
			int ar[] = new int[] { 1, 2, 3,4,5 };
			
		
			Pointer<Integer> a = pointerToInts(ar);
		
			Pointer<Integer> sum = add(a);
			for (long i = 0, n = sum.getValidElements(); i < n; i++)
				System.out.println(sum.get(i));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	 private static final String kernel_1725551088 = ""+
			 " __kernel void kernel_1725551088(                             "+
			 "                                                              "+
			 "  const int limit0, __global int *v2_INT, __global int *v1_2894) { "+
			 "   int dim0 = get_group_id(0) * get_local_size(0) + get_local_id(0); "+
			 "   v2_INT =  dim0;                                        "+
			 "   if(v2_INT >= limit0) return;                               "+
			 "   int v3_INT;                                                "+
			 "   {                                                          "+
			 "     __global  int* t0 = v1_2894;                             "+
			 "      int t1 = v2_INT;                                        "+
			 "      int t2 = t0[t1];                                        "+
			 "     v3_INT = t2;                                             "+
			 "     }                                                        "+
			 "   {                                                          "+
			 "     v3_INT += 1;                                             "+
			 "     }                                                        "+
			 "   {                                                          "+
			 "     __global  int* t0 = v1_2894;                             "+
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
	 
	  
	public static Pointer<Integer> add(Pointer<Integer> a)
			throws CLBuildException {
		int n = (int) a.getValidElements();

		CLContext context = JavaCL.createBestContext();
		CLQueue queue = context.createDefaultQueue();


		CLKernel kernel1 = context.createProgram(kernel_1725551088).createKernel("kernel_1725551088");
		
	
		
		CLBuffer<Integer> outBuf = context.createBuffer(CLMem.Usage.InputOutput, a, true);
		CLBuffer<Integer> aa = context.createBuffer(CLMem.Usage.InputOutput, pointerToInt(0), true);

		
		kernel1.setArgs(aa,aa,outBuf);

		kernel1.enqueueNDRange(queue, new int[] { n });
		queue.finish();

		return outBuf.read(queue);
	}
}