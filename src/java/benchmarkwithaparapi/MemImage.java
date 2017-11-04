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

@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY,timeUnit=TimeUnit.MILLISECONDS,customLabel="MemImage Size")

public class MemImage extends RubusBenchmark{
 


	private static int outGPU[];	
	private static int outCPU[];	
	private static int outAprApi[];	
	@BeforeClass
	public static void prepare(){
		
		outGPU = new int[size];
		outCPU = new int[size];
		outAprApi = new int[size];
	}

	public static int[] memImage(int height, int width){
	
		    int pix[] = new int[width * height];
		    int index = 0;
		    for (int y = 0; y < height; y++) {
		      int red = (y * 255) / (height - 1);
		      for (int x = 0; x < width; x++) {
		        int blue = (x * 255) / (width - 1);
		        pix[index++] = (255 << 24) | (red << 16) | blue;
		      }
		    }
		   return pix;
		}
	@Test
	@Override
	public void Java() {
		outCPU = memImage(size,size);
	}
	@Test
	@Override
	public void Rubus() {
		
	outGPU = memImageGPU(size, size);

	}
	@AfterClass
	public static void compareResults(){
		if(!compareLogEnable) return;

		printArray(outCPU, "CPU",size);
		printArray(outGPU, "GPU",size);
		printArray(outAprApi, "AparAPi",size);


	}
	private static final String kernel_14740318 = " __kernel void kernel_14740318(const int limit0, int v2_INT, int v1_INT, int v4_INT, __global int* v3_2894) {       int dim0 = get_global_id(0);                                  v4_INT += 1 * dim0;                                           if(v4_INT >= limit0) return;                                  int v6_INT;                                                   int v5_INT;                                                   int v7_INT;                                                   {                                                                   int t0 = v4_INT;                                              int t1 = v2_INT;                                              short t2 = 255;                                               int t3 = t0*t2;                                               int t4 = 1;                                                   int t5 = t1-t4;                                               int t6 = t3/t5;                                              v5_INT = t6;                                                  }/*20*/                                                  {                                                                   int t7 = 0;                                                  v6_INT = t7;                                                  }/*21*/                                                  {                                                                  }/*22*/                                                  {                                                                   int t8 = v1_INT;                                             while(v6_INT < t8) {                                               {                                                                   int t9 = v6_INT;                                              int t10 = v1_INT;                                             short t11 = 255;                                              int t12 = t9*t11;                                             int t13 = 1;                                                  int t14 = t10-t13;                                            int t15 = t12/t14;                                           v7_INT = t15;                                                 }/*24*/                                                  {                                                                  __global int* t16 = v3_2894;                                   int t17 = v6_INT;                                             int t18 = v2_INT;                                             int t19 = v4_INT;                                             int t20 = v5_INT;                                             int t21 = v7_INT;                                             int t22 = t17*t18;                                            int t23 = t22+t19;                                            int t24 = -16777216;                                          int t25 = 16;                                                int t26 = t20<<t25;                                           int t27 = t24|t26;                                            int t28 = t27|t21;                                           t16[t23] = t28;                                               }/*25*/                                                  {                                                                  v6_INT += 1;                                                  }/*26*/                                                  {                                                                  }/*44*/                                                  }/*loop end*/                                            }/*47*/                                                  {                                                                  }/*27*/                                                  {                                                                  v4_INT += 1;                                                  }/*28*/                                                  {                                                                  }/*42*/                                                  }";
	  	  public int[] memImageGPU(int paramInt1, int paramInt2)
	  {
	    int[] arrayOfInt = new int[paramInt1 * paramInt2];
	    
	    int i = 0;kernel_14740318(paramInt2, paramInt2, paramInt1, i, arrayOfInt);
	    





	    return arrayOfInt;
	  }
	  	  private static CLContext context = JavaCL.createBestContext();

	  public static void kernel_14740318(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt)
	  {
	    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
	    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
	    String[] arrayOfString = { kernel_14740318 };
	    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
	    CLKernel localCLKernel = arrayOfCLKernel[0];
	    int i = paramInt2;
	    int j = paramInt3;
	    int k = paramInt4;
	    CLBuffer localCLBuffer = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToInts(paramArrayOfInt), true);
	    localCLKernel.setArgs(new Object[] { paramInt1, i, j, k, localCLBuffer });
	    int m = paramInt1 - paramInt4 / 1;
	    CLEvent[] arrayOfCLEvent = { null };
	    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { m }, arrayOfCLEvent);
	    localCLQueue.finish();
	    localCLBuffer.read(localCLQueue, new CLEvent[] { localCLEvent }).getInts(paramArrayOfInt);
	  }

	  public static void main(String[] args) {

			runBenchmark(MemImage.class,new int[]{32,64,128,256,512, 1024, 2048, 4096});
	}

	@Override
	public void AparAPI() {
		
		final int height = this.size;
		final int width = this.size;
		 final  int pix[] = outAprApi;
		   
		  Kernel kernel = new Kernel(){
			  
			  public void run() {
			         int gid = getGlobalId();

				  int x = (gid % width);

			         int y = (gid / height);
				  int red = (y * 255) / (height - 1);
			        int blue = (x * 255) / (width - 1);
			        pix[gid] = (255 << 24) | (red << 16) | blue;
			   		
				}
			
		  };
		  kernel.execute(height*width);
		  kernel.dispose();
		     
		
	}


}
