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

import static org.bridj.Pointer.pointerToFloats;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

public class VectorAdd {

	public static void main(String[] args) {
		

		try {
//			int ar[] = new int[] { 1, 2, 3, 4, 3, 6, 7, 8, 3, 2, 4, 4 };
//			int br[] = new int[] { 1, 2, 3, 4, 3, 6, 7, 8, 3, 2, 4, 4 };
//			int cr[] = new int[ar.length];
			
			//Src2598218282026810383.samples_HelloWorld_kernel_12144151946(ar.length, ar, br, 0,cr);

			 
			float ar[] = new float[] { 1, 2, 3 };
			float br[] = new float[] { 4, 5, 6 };
			float cr[] = new float[ar.length];
		
			Pointer<Float> a = pointerToFloats(ar);
			Pointer<Float> b = pointerToFloats(br);

			Pointer<Float> sum = add(a, b);
			for (long i = 0, n = sum.getValidElements(); i < n; i++)
				System.out.println(sum.get(i));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static final String kernel_1354633072 = ""+
			" __kernel void kernel_1354633072(                             "+
			"                                                              "+
			"   __global int* v0_2894, __global int* v2_2894, __global int* v1_2894) { "+
			"   int dim0 = get_group_id(0) * get_local_size(0) + get_local_id(0); "+
			"  int  v4_INT =  dim0;                                        "+
//			"   if(v4_INT >= limit0) return;                               "+
			"   int v5_INT;                                                "+
			"   l0: { // BasicBlock #15 (line 29)                          "+
			"     const int[] t0 = v0_2894;                                "+
			"     const int t1 = v4_INT;                                   "+
			"     const int t2 = t0[t1];                                   "+
			"     const int[] t3 = v1_2894;                                "+
			"     const int t4 = v4_INT;                                   "+
			"     const int t5 = t3[t4];                                   "+
			"     const int t6 = t2*t5;                                    "+
			"     v5_INT = t6;                                             "+
			"     }                                                        "+
			"   l1: { // BasicBlock #16 (line 30)                          "+
			"     const int t0 = v5_INT;                                   "+
			"     const int t1 = 0;                                        "+
			"     if(t0<=t1) goto l2;                                      "+
			"     }                                                        "+
			"   l3: { // BasicBlock #18 (line 30)                          "+
			"     }                                                        "+
			"   l4: { // BasicBlock #19 (line 31)                          "+
			"     const int[] t0 = v2_2894;                                "+
			"     const int t1 = v4_INT;                                   "+
			"     const int t2 = v5_INT;                                   "+
			"     t0[t1] = t2;                                             "+
			"     }                                                        "+
			"   l2: { // BasicBlock #17 (line 27)                          "+
			"     v4_INT += 1;                                             "+
			"     }                                                       "+
			"   l5: { // BasicBlock #23                                    "+
			"     }                                                        "+
			"   }";
	
	public static Pointer<Float> add(Pointer<Float> a, Pointer<Float> b)
			throws CLBuildException {
		int n = (int) a.getValidElements();

		CLContext context = JavaCL.createBestContext();
		CLQueue queue = context.createDefaultQueue();

		String source = " void plusplus(int *i){i[0]= i[0]+1;}  " +
				" void add(__global float *a,__global float *b,__global float *c,__global int *i){int j = (int)i;c[j] = a[j] + b[j]; } \n" +
				"__kernel void kernel1 (__global  float* a, __global  float* b, __global float* output)     "
				+ "{                                                                                                     "
				+ "   int i = get_global_id(0);  " +
				   "  "+
				"    add(a,b,output,i); " +
				
				"     " +
				"                                                                 "
				+ "   " +
				"                                                                           "
				+ "}                                                                                                     "
				+" ";

		//CLKernel kernel = context.createProgram(kernel_1354633072).createKernel(		"kernel_1354633072");
		
		CLKernel kernel1 = context.createProgram(source).createKernel("kernel1");
		CLBuffer<Float> aBuf = context.createBuffer(CLMem.Usage.Input, a, true);
		CLBuffer<Float> bBuf = context.createBuffer(CLMem.Usage.Input, b, true);
		
		CLBuffer<Float> outBuf = context.createBuffer(CLMem.Usage.InputOutput, a, true);
//		CLBuffer<Float> outBuf = context.createBuffer(CLMem.Usage.InputOutput,
//				Float.class, n);
		kernel1.setArgs(aBuf, bBuf, outBuf);

		kernel1.enqueueNDRange(queue, new int[] { n });
		queue.finish();

		return outBuf.read(queue);
	}
}