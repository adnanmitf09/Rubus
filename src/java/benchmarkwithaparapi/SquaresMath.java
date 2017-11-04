package benchmarkwithaparapi;
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

import java.util.concurrent.TimeUnit;

import org.bridj.Pointer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import benchmark.RubusBenchmark;

import com.amd.aparapi.Kernel;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
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

@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY,timeUnit=TimeUnit.MILLISECONDS,customLabel="Calculations")

public class SquaresMath extends RubusBenchmark{
	private static float inputValues[];	

	private static float arrGPU[];	
	private static float arrCPU[];	
	private static float arrAparAPi[];	
	@BeforeClass
	public static void prepare(){
		inputValues = new float[size];
		arrGPU = new float[size];
		arrCPU = new float[size];
		arrAparAPi = new float[size];
		sequentialFill(inputValues);
	}

	public static float[] squaresMath(float inA[]){
		float result[] = new float[inA.length];
		for (int i = 0; i < inA.length; i++) {
			result[i] = (float) Math.pow(inA[i], 2);
		}
		return result;
	}
	
	@Test
	@Override
	public void Java() {
		arrCPU = squaresMath(inputValues);
	}
	@Test
	@Override
	public void Rubus() {
		arrGPU= squaresMathGPU(inputValues);

	}
	
	public static float[] squaresMathGPU(float[] paramArrayOfFloat)
	  {
	    float[] arrayOfFloat = new float[paramArrayOfFloat.length];
	    int i = 0;kernel_1365023967(paramArrayOfFloat.length, arrayOfFloat, paramArrayOfFloat, i);
	    

	    return arrayOfFloat;
	  }
	
	@AfterClass
	public static void compareResults(){
		if(!compareLogEnable) return;

		printArray(arrCPU, "CPU",size);
		printArray(arrGPU, "GPU",size);
		printArray(arrAparAPi, "AparAPI",size);


	}

	  private static final String kernel_1365023967 = " __kernel void kernel_1365023967(const int limit0, __global float* v1_2891, __global float* v0_2891, int v2_INT) {       int dim0 = get_global_id(0);                                  v2_INT += 1 * dim0;                                           if(v2_INT >= limit0) return;                                  {                                                                  __global float* t9 = v1_2891;                                  int t10 = v2_INT;                                            __global float* t11 = v0_2891;                                 int t12 = v2_INT;                                             float t13 = t11[t12];                                         float t14 = (float) t13;                                      float t15 = 2.0;                                              float t16 = pow(t14, t15);                                    float t17 = (float) t16;                                     t9[t10] = t17;                                                }/*18*/                                                  {                                                                  v2_INT += 1;                                                  }/*19*/                                                  {                                                                  }/*27*/                                                  }";
	  private static CLContext context = JavaCL.createBestContext();
	  
	
	  public static void kernel_1365023967(int paramInt1, float[] paramArrayOfFloat1, float[] paramArrayOfFloat2, int paramInt2)
	  {
	    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
	    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
	    String[] arrayOfString = { kernel_1365023967 };
	    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
	    CLKernel localCLKernel = arrayOfCLKernel[0];
	    CLBuffer localCLBuffer1 = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToFloats(paramArrayOfFloat1), true);
	    CLBuffer localCLBuffer2 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToFloats(paramArrayOfFloat2), true);
	    int i = paramInt2;
	    localCLKernel.setArgs(new Object[] { paramInt1, localCLBuffer1, localCLBuffer2, i });
	    int j = paramInt1 - paramInt2 / 1;
	    CLEvent[] arrayOfCLEvent = { null };
	    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { j }, arrayOfCLEvent);
	    localCLQueue.finish();
	    localCLBuffer1.read(localCLQueue, new CLEvent[] { localCLEvent }).getFloats(paramArrayOfFloat1);
	  }
	  
	
	 
	
	public static void main(String[] args) {
		runBenchmark(SquaresMath.class,new int[]{1024});//, 2048, 4096,8192,16384,32768,65536,131072});
	}

	@Override
	public void AparAPI() {
		 final float[] sum = this.arrAparAPi;
final float inA[] = this.inputValues;
final int size = this.size;
	   
Kernel kernel = new Kernel(){
	         @Override public void run() {
	            int gid = getGlobalId();
	            sum[gid] =  pow(inA[gid], 2);
	            }
	      };

	      kernel.execute(size);
	      kernel.dispose();
	}


}
