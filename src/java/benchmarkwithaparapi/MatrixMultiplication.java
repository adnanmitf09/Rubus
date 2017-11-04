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


import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.bridj.Pointer;
import org.junit.After;
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

//http://www.tutorialspoint.com/junit/junit_parameterized_test.htm
//@RunWith(Parameterized.class)   // to test class for multiple parameters 
//@BenchmarkOptions(warmupRounds=0,benchmarkRounds=1)
//@AxisRange(min = 0, max = 1)
//@BenchmarkMethodChart(filePrefix = "benchmark-matrix")
//@BenchmarkHistoryChart(filePrefix = "benchmark-matrixHistory")
//@RunWith(OrderedTestRunner.class)
//Note: you are must to add -Djub.consumers=CONSOLE,H2 -Djub.db.file=.benchmarks
// options to generate any history or method chart

@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY,timeUnit=TimeUnit.MILLISECONDS,customLabel="Matrix Size")
//@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY)

public class MatrixMultiplication extends RubusBenchmark{
//	@Rule  // Just to tell JUnit that it is a benchmark
//	public BenchmarkRule benchmarkRun = new BenchmarkRule();
	
//	public static int[] matrixSize = new int[]{32,64,128,256,512,1024,2048,4096};
	public static int[] matrixSize = new int[]{32,64};

	public static void main(String[] args) throws IOException {
		runBenchmark(MatrixMultiplication.class, matrixSize);
     
	System.out.println("All Done");

	}
	
	
	
	
//
//    @Rule
//    public BenchmarkRule benchmarkRun = new BenchmarkRule(h2consumer);
//
//    @BeforeClass
//    public static void initialize() throws SQLException
//    {	System.setProperty(BenchmarkOptionsSystemProperties.CUSTOMKEY_PROPERTY,""+n);
//        h2consumer = new H2Consumer(dbFile);
//    
//
//    }
//	
    @AfterClass
    public static void cleanup(){
    	//h2consumer.close();
    }
    
    
    
	private static float[] inA=null;// data
	private static float[] inB=null;
	private static float[] outJava,outRubus,outAparApi; // output


	/*
	@Parameterized.Parameters
	public static Collection<Object[]> primeNumbers() {
		return Arrays.asList(new Object[][] {
				{ 2 },
				{ 4 },
				{ 8},
				{ 16},
				{ 32},
				{ 64},
				{ 128},
				{ 256}
				//,{ 512}
				//,{ 1024}
		});
	}

	public MatrixMultiplication(int n) {
		this.n = n;
	}
	 */

	public MatrixMultiplication() {
		// TODO Auto-generated constructor stub
	}

	@BeforeClass
	public static void prepare() {

		// loading dummy data
		inA = new float[size*size];
		inB = new float[size*size];
		outJava = new float[size * size];
		outRubus = new float[size * size];
		outAparApi = new float[size * size];

		int min = 2; 
		int max = 20;
		for (int i = 0; i < size*size; i++) {
			inA[i] = min + (float)(Math.random() * ((max - min) + 1));
			inB[i] = min + (float)(Math.random() * ((max - min) + 1));
		}
//		Arrays.fill(inA, 0);
//		Arrays.fill(inB, 0);
      
		  

	}


	@Test
	public void Java() {
		//System.out.println(n);
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				float sum = 0;
				for (int k = 0; k < size; k++)
					sum = sum + inA[i * size + k] * inB[k * size + j];
				outJava[i * size + j] = sum;

			}
		}
		
	}

	
	
	

	

	@Test
	public void AparAPI() {
		System.out.println("AparApi");
	final float[]in1 = inA;
	final float[]in2 = inB;
	final int size = this.size;
	final float [] outAparApi = this.outAparApi;
	Kernel kernel = new Kernel(){
		    @Override
		    public void run() {
		        int i = getGlobalId();
		        int j = getPassId();
		    
		        float value = 0;
		        for(int k = 0; k < size; k++)
		        {
		            value += in1[k + i * size] * in2[k * size + j];
		        }
		        outAparApi[i * size + j] = value;
		    }
		 
		};
//		 
		kernel.execute(size, size);
		    
		
	}
	
	@Test
	public void Rubus() 
	{
		int i = 0;int j = 0;	kernel_332684807(size, size, size, inB, i, j, outRubus, inA);

		
	}
	class AparapiMatMul extends Kernel {
		 
	    float matA[];
	    float matB[];
	    float matC[];
	    float C[];
	     int size;
	
	 
	    @Override
	    public void run() {
	        int i = getGlobalId();
	        int j = getPassId();
	        float value = 0;
	        for(int k = 0; k < size; k++)
	        {
	            value += matA[k + i * size] * matB[k * size + j];
	        }
	        matC[i * size + j] = value;
	    }
	 
	   public AparapiMatMul(float inA[], float inB[],float out[])
	    {
	        matA = inA;
	        matB = inB;
	        C = out;
	        size = inA.length;
	        
	    
	    }
	 
	}    
	
	private static  final String kernel_332684807 = " __kernel void kernel_332684807(const int limit0, const int limit1, int v2_INT, __global float* v1_2891, int v4_INT, int v5_INT, __global float* v3_2891, __global float* v0_2891) {       int dim1 = get_global_id(1);                                  int dim0 = get_global_id(0);                                  v5_INT += 1 * dim0;                                           v4_INT += 1 * dim1;                                           if(v5_INT >= limit0) return;                                  if(v4_INT >= limit1) return;                                  int v7_INT;                                                   float v6_FLOAT;                                               {                                                                   float t0 = 0.0f;                                             v6_FLOAT = t0;                                                }                                                        {                                                                   int t0 = 0;                                                  v7_INT = t0;                                                  }                                                        {                                                                  }                                                        {                                                                   int t0 = v2_INT;                                             while(v7_INT < t0) {                                               {                                                                   float t0 = v6_FLOAT;                                         __global float* t1 = v0_2891;                                  int t2 = v4_INT;                                              int t3 = v2_INT;                                              int t4 = v7_INT;                                              int t5 = t2*t3;                                               int t6 = t5+t4;                                               float t7 = t1[t6];                                           __global float* t8 = v1_2891;                                  int t9 = v7_INT;                                              int t10 = v2_INT;                                             int t11 = v5_INT;                                             int t12 = t9*t10;                                             int t13 = t12+t11;                                            float t14 = t8[t13];                                          float t15 = t7*t14;                                           float t16 = t0+t15;                                          v6_FLOAT = t16;                                               }                                                        {                                                                  v7_INT += 1;                                                  }                                                        {                                                                  }                                                        }                                                        }                                                        {                                                                  }                                                        {                                                                  __global float* t0 = v3_2891;                                  int t1 = v4_INT;                                              int t2 = v2_INT;                                              int t3 = v5_INT;                                              float t4 = v6_FLOAT;                                          int t5 = t1*t2;                                               int t6 = t5+t3;                                              t0[t6] = t4;                                                  }                                                        {                                                                  v5_INT += 1;                                                  }                                                        {                                                                  }                                                        }";
	private static CLContext context = JavaCL.createBestContext();

	public  void kernel_332684807(int paramInt1, int paramInt2, int paramInt3, float[] paramArrayOfFloat1, int paramInt4, int paramInt5, float[] paramArrayOfFloat2, float[] paramArrayOfFloat3)
	{
		CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
		CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
		String[] arrayOfString = { kernel_332684807 };
		CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
		CLKernel localCLKernel = arrayOfCLKernel[0];
		int i = paramInt3;
		CLBuffer localCLBuffer1 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToFloats(paramArrayOfFloat1), true);
		int j = paramInt4;
		int k = paramInt5;
		CLBuffer localCLBuffer2 = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToFloats(paramArrayOfFloat2), true);
		CLBuffer localCLBuffer3 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToFloats(paramArrayOfFloat3), true);
		localCLKernel.setArgs(new Object[] { paramInt1, paramInt2, i, localCLBuffer1, j, k, localCLBuffer2, localCLBuffer3 });
		int m = paramInt1 - paramInt5 / 1;
		int n = paramInt2 - paramInt4 / 1;
		CLEvent[] arrayOfCLEvent = { null };
		CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { m, n }, arrayOfCLEvent);
		localCLQueue.finish();

		localCLBuffer2.read(localCLQueue, new CLEvent[] { localCLEvent }).getFloats(paramArrayOfFloat2);

	}

	

	@After
	public void compareResult(){
		if(!compareLogEnable) return;

		// rubus and AparAPi are ) at the start just because this method is called each time after each test and while running Java test, they will be empty
	//	printArray(outJava,"Java", size);
	//printArray(outAparApi,"AparAPi", size);
	//	printArray(outRubus,"Rubus", size);
//		boolean isEqual = true;
//		for (int i = 0; i < outJava.length; i++) {
//			if(outJava[i]!=outRubus[i]){
//				isEqual = false;
//				break;
//			}
//		}
//		assert(isEqual);	

	}




	

}
