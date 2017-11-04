package benchmarkwithaparapi;

import java.util.concurrent.TimeUnit;

import org.bridj.Pointer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import annotation.Transform;
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

@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY,timeUnit=TimeUnit.MILLISECONDS,customLabel="Bodies Count")

public class NBody extends RubusBenchmark{

	private static float positionsGPU[];	
	private static float positionsCPU[];
	private static float velocitiesGPU[];	
	private static float velocitiesCPU[];	
	protected static  float delT = .005f;
	protected static  float espSqr = 1.0f;
	protected static  float mass = 5f;
	
	private static float positionsAparApi[];
	private static float velocitiesAparApi[];	

	@BeforeClass
	public static void prepare(){

		
	}
	@Before
	public void prepareData(){
		int bodies = size;
		positionsCPU = new float[bodies * 3];
		velocitiesCPU = new float[bodies * 3];
		positionsAparApi = new float[bodies*3];
		velocitiesAparApi = new float[bodies*3];

		
		final float maxDist = 20f;
		for (int body = 0; body < (bodies * 3); body += 3) {

			final float theta = (float) (Math.random() * Math.PI * 2);
			final float phi = (float) (Math.random() * Math.PI * 2);
			final float radius = (float) (Math.random() * maxDist);

			// get the 3D dimensional coordinates
			positionsCPU[body + 0] = (float) (radius * Math.cos(theta) * Math.sin(phi));
			positionsCPU[body + 1] = (float) (radius * Math.sin(theta) * Math.sin(phi));
			positionsCPU[body + 2] = (float) (radius * Math.cos(phi));

			// divide into two 'spheres of bodies' by adjusting x

			if ((body % 2) == 0) {
				positionsCPU[body + 0] += maxDist * 1.5;
			} else {
				positionsCPU[body + 0] -= maxDist * 1.5;
			}
		}
		positionsGPU = positionsCPU.clone();
		velocitiesGPU = velocitiesCPU.clone();
		positionsAparApi = positionsCPU.clone();
		velocitiesAparApi = velocitiesCPU.clone();
	}

	/**
	 * Here is the kernel entrypoint. Here is where we calculate the position of each body
	 */
	@Transform(loops={"body"})
	public void compute(int bodies, float xyz[],float delT,float espSqr, float mass,float vxyz[]) {
		final int count = bodies * 3;
		for (int body = 0; body < bodies; body++){
			final int globalId = body * 3;

			float accx = 0.f;
			float accy = 0.f;
			float accz = 0.f;

			final float myPosx = xyz[globalId + 0];
			final float myPosy = xyz[globalId + 1];
			final float myPosz = xyz[globalId + 2];
			for (int i = 0; i < count; i += 3) {
				final float dx = xyz[i + 0] - myPosx;
				final float dy = xyz[i + 1] - myPosy;
				final float dz = xyz[i + 2] - myPosz;
				final float invDist = 1f/((float)Math.sqrt((dx * dx) + (dy * dy) + (dz * dz) + espSqr));
				final float s = mass * invDist * invDist * invDist;
				accx = accx + (s * dx);
				accy = accy + (s * dy);
				accz = accz + (s * dz);
			}
			accx = accx * delT;
			accy = accy * delT;
			accz = accz * delT;
			xyz[globalId + 0] = myPosx + (vxyz[globalId + 0] * delT) + (accx * .5f * delT);
			xyz[globalId + 1] = myPosy + (vxyz[globalId + 1] * delT) + (accy * .5f * delT);
			xyz[globalId + 2] = myPosz + (vxyz[globalId + 2] * delT) + (accz * .5f * delT);

			vxyz[globalId + 0] = vxyz[globalId + 0] + accx;
			vxyz[globalId + 1] = vxyz[globalId + 1] + accy;
			vxyz[globalId + 2] = vxyz[globalId + 2] + accz;
		}
	}


	@Test
	@Override
	public void Java() {

		compute(size, positionsCPU, delT, espSqr, mass, velocitiesCPU);
	}
	@Test
	@Override
	public void Rubus() {
		computeGPU(size, positionsGPU, delT, espSqr, mass, velocitiesGPU);

	}
	@AfterClass
	public static void compareResults(){
		if(!compareLogEnable) return;

		printArray(positionsCPU, "CPU",size);
		printArray(positionsGPU, "GPU",size);
		printArray(positionsAparApi, "AparAPI",size);

	}

	private static final String kernel_1387062610 = " __kernel void kernel_1387062610(const int limit0, int v8_INT, float v5_FLOAT, __global float* v6_2891, float v3_FLOAT, int v7_INT, __global float* v2_2891, float v4_FLOAT) {       int dim0 = get_global_id(0);                                  v8_INT += 1 * dim0;                                           if(v8_INT >= limit0) return;                                  float v12_FLOAT;                                              float v15_FLOAT;                                              float v17_FLOAT;                                              float v11_FLOAT;                                              int v9_INT;                                                   float v18_FLOAT;                                              float v13_FLOAT;                                              float v14_FLOAT;                                              float v20_FLOAT;                                              float v19_FLOAT;                                              int v16_INT;                                                  float v10_FLOAT;                                              float v21_FLOAT;                                              {                                                                   int t0 = v8_INT;                                              int t1 = 3;                                                   int t2 = t0*t1;                                              v9_INT = t2;                                                  }/*8*/                                                   {                                                                   float t3 = 0.0f;                                             v10_FLOAT = t3;                                               }/*9*/                                                   {                                                                   float t4 = 0.0f;                                             v11_FLOAT = t4;                                               }/*10*/                                                  {                                                                   float t5 = 0.0f;                                             v12_FLOAT = t5;                                               }/*11*/                                                  {                                                                  __global float* t6 = v2_2891;                                  int t7 = v9_INT;                                              int t8 = 0;                                                   int t9 = t7+t8;                                               float t10 = t6[t9];                                          v13_FLOAT = t10;                                              }/*12*/                                                  {                                                                  __global float* t11 = v2_2891;                                 int t12 = v9_INT;                                             int t13 = 1;                                                  int t14 = t12+t13;                                            float t15 = t11[t14];                                        v14_FLOAT = t15;                                              }/*13*/                                                  {                                                                  __global float* t16 = v2_2891;                                 int t17 = v9_INT;                                             int t18 = 2;                                                  int t19 = t17+t18;                                            float t20 = t16[t19];                                        v15_FLOAT = t20;                                              }/*14*/                                                  {                                                                   int t21 = 0;                                                 v16_INT = t21;                                                }/*15*/                                                  {                                                                  }/*16*/                                                  {                                                                   int t22 = v7_INT;                                            while(v16_INT < t22) {                                             {                                                                  __global float* t23 = v2_2891;                                 int t24 = v16_INT;                                            int t25 = 0;                                                  int t26 = t24+t25;                                            float t27 = t23[t26];                                         float t28 = v13_FLOAT;                                        float t29 = t27-t28;                                         v17_FLOAT = t29;                                              }/*18*/                                                  {                                                                  __global float* t30 = v2_2891;                                 int t31 = v16_INT;                                            int t32 = 1;                                                  int t33 = t31+t32;                                            float t34 = t30[t33];                                         float t35 = v14_FLOAT;                                        float t36 = t34-t35;                                         v18_FLOAT = t36;                                              }/*19*/                                                  {                                                                  __global float* t37 = v2_2891;                                 int t38 = v16_INT;                                            int t39 = 2;                                                  int t40 = t38+t39;                                            float t41 = t37[t40];                                         float t42 = v15_FLOAT;                                        float t43 = t41-t42;                                         v19_FLOAT = t43;                                              }/*20*/                                                  {                                                                   float t44 = v17_FLOAT;                                        float t45 = v17_FLOAT;                                        float t46 = v18_FLOAT;                                        float t47 = v18_FLOAT;                                        float t48 = v19_FLOAT;                                        float t49 = v19_FLOAT;                                        float t50 = v4_FLOAT;                                         float t51 = t44*t45;                                          float t52 = t46*t47;                                          float t53 = t51+t52;                                          float t54 = t48*t49;                                          float t55 = t53+t54;                                          float t56 = t55+t50;                                          float t57 = (float) t56;                                      float t58 = sqrt(t57);                                        float t59 = 1.0f;                                             float t60 = (float) t58;                                      float t61 = t59/t60;                                         v20_FLOAT = t61;                                              }/*21*/                                                  {                                                                   float t62 = v5_FLOAT;                                         float t63 = v20_FLOAT;                                        float t64 = v20_FLOAT;                                        float t65 = v20_FLOAT;                                        float t66 = t62*t63;                                          float t67 = t66*t64;                                          float t68 = t67*t65;                                         v21_FLOAT = t68;                                              }/*22*/                                                  {                                                                   float t69 = v10_FLOAT;                                        float t70 = v21_FLOAT;                                        float t71 = v17_FLOAT;                                        float t72 = t70*t71;                                          float t73 = t69+t72;                                         v10_FLOAT = t73;                                              }/*23*/                                                  {                                                                   float t74 = v11_FLOAT;                                        float t75 = v21_FLOAT;                                        float t76 = v18_FLOAT;                                        float t77 = t75*t76;                                          float t78 = t74+t77;                                         v11_FLOAT = t78;                                              }/*24*/                                                  {                                                                   float t79 = v12_FLOAT;                                        float t80 = v21_FLOAT;                                        float t81 = v19_FLOAT;                                        float t82 = t80*t81;                                          float t83 = t79+t82;                                         v12_FLOAT = t83;                                              }/*25*/                                                  {                                                                  v16_INT += 3;                                                 }/*26*/                                                  {                                                                  }/*43*/                                                  }/*loop end*/                                            }/*45*/                                                  {                                                                  }/*27*/                                                  {                                                                   float t84 = v10_FLOAT;                                        float t85 = v3_FLOAT;                                         float t86 = t84*t85;                                         v10_FLOAT = t86;                                              }/*28*/                                                  {                                                                   float t87 = v11_FLOAT;                                        float t88 = v3_FLOAT;                                         float t89 = t87*t88;                                         v11_FLOAT = t89;                                              }/*29*/                                                  {                                                                   float t90 = v12_FLOAT;                                        float t91 = v3_FLOAT;                                         float t92 = t90*t91;                                         v12_FLOAT = t92;                                              }/*30*/                                                  {                                                                  __global float* t93 = v2_2891;                                 int t94 = v9_INT;                                             float t95 = v13_FLOAT;                                       __global float* t96 = v6_2891;                                 int t97 = v9_INT;                                             int t98 = 0;                                                  int t99 = t97+t98;                                            float t100 = t96[t99];                                        float t101 = v3_FLOAT;                                        float t102 = v10_FLOAT;                                       float t103 = v3_FLOAT;                                        int t104 = t94+t98;                                           float t105 = t100*t101;                                       float t106 = t95+t105;                                        float t107 = 0.5f;                                            float t108 = t102*t107;                                       float t109 = t108*t103;                                       float t110 = t106+t109;                                      t93[t104] = t110;                                             }/*31*/                                                  {                                                                  __global float* t111 = v2_2891;                                int t112 = v9_INT;                                            float t113 = v14_FLOAT;                                      __global float* t114 = v6_2891;                                int t115 = v9_INT;                                            int t116 = 1;                                                 int t117 = t115+t116;                                         float t118 = t114[t117];                                      float t119 = v3_FLOAT;                                        float t120 = v11_FLOAT;                                       float t121 = v3_FLOAT;                                        int t122 = t112+t116;                                         float t123 = t118*t119;                                       float t124 = t113+t123;                                       float t125 = 0.5f;                                            float t126 = t120*t125;                                       float t127 = t126*t121;                                       float t128 = t124+t127;                                      t111[t122] = t128;                                            }/*32*/                                                  {                                                                  __global float* t129 = v2_2891;                                int t130 = v9_INT;                                            float t131 = v15_FLOAT;                                      __global float* t132 = v6_2891;                                int t133 = v9_INT;                                            int t134 = 2;                                                 int t135 = t133+t134;                                         float t136 = t132[t135];                                      float t137 = v3_FLOAT;                                        float t138 = v12_FLOAT;                                       float t139 = v3_FLOAT;                                        int t140 = t130+t134;                                         float t141 = t136*t137;                                       float t142 = t131+t141;                                       float t143 = 0.5f;                                            float t144 = t138*t143;                                       float t145 = t144*t139;                                       float t146 = t142+t145;                                      t129[t140] = t146;                                            }/*33*/                                                  {                                                                  __global float* t147 = v6_2891;                                int t148 = v9_INT;                                           __global float* t149 = v6_2891;                                int t150 = v9_INT;                                            int t151 = 0;                                                 int t152 = t150+t151;                                         float t153 = t149[t152];                                      float t154 = v10_FLOAT;                                       int t155 = t148+t151;                                         float t156 = t153+t154;                                      t147[t155] = t156;                                            }/*34*/                                                  {                                                                  __global float* t157 = v6_2891;                                int t158 = v9_INT;                                           __global float* t159 = v6_2891;                                int t160 = v9_INT;                                            int t161 = 1;                                                 int t162 = t160+t161;                                         float t163 = t159[t162];                                      float t164 = v11_FLOAT;                                       int t165 = t158+t161;                                         float t166 = t163+t164;                                      t157[t165] = t166;                                            }/*35*/                                                  {                                                                  __global float* t167 = v6_2891;                                int t168 = v9_INT;                                           __global float* t169 = v6_2891;                                int t170 = v9_INT;                                            int t171 = 2;                                                 int t172 = t170+t171;                                         float t173 = t169[t172];                                      float t174 = v12_FLOAT;                                       int t175 = t168+t171;                                         float t176 = t173+t174;                                      t167[t175] = t176;                                            }/*36*/                                                  {                                                                  v8_INT += 1;                                                  }/*37*/                                                  {                                                                  }/*41*/                                                  }";	
	private static CLContext context = JavaCL.createBestContext();


	public void computeGPU(int paramInt, float[] paramArrayOfFloat1, float paramFloat1, float paramFloat2, float paramFloat3, float[] paramArrayOfFloat2)
	  {
	    int i = paramInt * 3;
	    int j = 0;kernel_1387062610(paramInt, j, paramFloat3, paramArrayOfFloat2, paramFloat1, i, paramArrayOfFloat1, paramFloat2);
	  }
	  
	  public static void kernel_1387062610(int paramInt1, int paramInt2, float paramFloat1, float[] paramArrayOfFloat1, float paramFloat2, int paramInt3, float[] paramArrayOfFloat2, float paramFloat3)
	  {
	    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
	    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
	    String[] arrayOfString = { kernel_1387062610 };
	    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
	    CLKernel localCLKernel = arrayOfCLKernel[0];
	    int i = paramInt2;
	    float f1 = paramFloat1;
	    CLBuffer localCLBuffer1 = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToFloats(paramArrayOfFloat1), true);
	    float f2 = paramFloat2;
	    int j = paramInt3;
	    CLBuffer localCLBuffer2 = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToFloats(paramArrayOfFloat2), true);
	    float f3 = paramFloat3;
	    localCLKernel.setArgs(new Object[] { paramInt1, i, f1, localCLBuffer1, f2, j, localCLBuffer2, f3 });
	    int k = paramInt1 - paramInt2 / 1;
	    CLEvent[] arrayOfCLEvent = { null };
	    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { k }, arrayOfCLEvent);
	    localCLQueue.finish();
	    localCLBuffer1.read(localCLQueue, new CLEvent[] { localCLEvent }).getFloats(paramArrayOfFloat1);
	    localCLBuffer2.read(localCLQueue, new CLEvent[] { localCLEvent }).getFloats(paramArrayOfFloat2);
	  }



	public static void main(String[] args) {
		runBenchmark(NBody.class,new int[]{
512});//, 1024, 2048, 4096,8192,16384,32768,65536});
//	NBody.size = 64;
//	NBody b = new NBody();
//	b.prepareData();
//	b.CPU();
//	b.GPU();
//	b.compareResults();
	
	}
	@Override
	public void AparAPI() {
		final float delT = this.delT;
		final float espSqr = this.espSqr;
		final float mass = this.mass;
		final float[] xyz = positionsAparApi;
		final float[] vxyz = velocitiesAparApi;
		Kernel kernel = new Kernel(){
		

		
		  
		      @Override public void run() {
		         final int body = getGlobalId();
		         final int count = getGlobalSize(0) * 3;
		         final int globalId = body * 3;

		         float accx = 0.f;
		         float accy = 0.f;
		         float accz = 0.f;

		         final float myPosx = xyz[globalId + 0];
		         final float myPosy = xyz[globalId + 1];
		         final float myPosz = xyz[globalId + 2];
		         for (int i = 0; i < count; i += 3) {
		            final float dx = xyz[i + 0] - myPosx;
		            final float dy = xyz[i + 1] - myPosy;
		            final float dz = xyz[i + 2] - myPosz;
		            final float invDist = rsqrt((dx * dx) + (dy * dy) + (dz * dz) + espSqr);
		            final float s = mass * invDist * invDist * invDist;
		            accx = accx + (s * dx);
		            accy = accy + (s * dy);
		            accz = accz + (s * dz);
		         }
		         accx = accx * delT;
		         accy = accy * delT;
		         accz = accz * delT;
		         xyz[globalId + 0] = myPosx + (vxyz[globalId + 0] * delT) + (accx * .5f * delT);
		         xyz[globalId + 1] = myPosy + (vxyz[globalId + 1] * delT) + (accy * .5f * delT);
		         xyz[globalId + 2] = myPosz + (vxyz[globalId + 2] * delT) + (accz * .5f * delT);

		         vxyz[globalId + 0] = vxyz[globalId + 0] + accx;
		         vxyz[globalId + 1] = vxyz[globalId + 1] + accy;
		         vxyz[globalId + 2] = vxyz[globalId + 2] + accz;
		      }

		    	
	
};
	kernel.execute(size);
	}

}
