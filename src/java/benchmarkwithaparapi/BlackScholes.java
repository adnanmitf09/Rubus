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
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY,timeUnit=TimeUnit.MILLISECONDS,customLabel="Calculations")

public class BlackScholes extends RubusBenchmark{

	static private  float[] randArray;
	static private  float[] call_java;
	static private  float[] put_java;

	static private  float[] call_Rubus;
	static private  float[] put_Rubus;
	static private  float[] call_AparAPI;
	static private  float[] put_AparAPI;

	
	/*
	 * For a description of the algorithm and the terms used, please see the
	 * documentation for this sample.
	 *
	 * On invocation of kernel blackScholes, each work thread calculates call price
	 * and put price values for given stock price, option strike price,
	 * time to expiration date, risk free interest and volatility factor.
	 */

	/**
	 * Taken from Aparapi samples
	 * @param X input value
	 * @brief Abromowitz Stegun approxmimation for PHI (Cumulative Normal Distribution Function)
	 */
	static float  phi(float X) {
		 float c1 = 0.319381530f;
		 float c2 = -0.356563782f;
		 float c3 = 1.781477937f;
		 float c4 = -1.821255978f;
		 float c5 = 1.330274429f;

		 float temp4 = 0.2316419f;

		 float oneBySqrt2pi = 0.398942280f;

		float absX = Math.abs(X);
		float t = 1f / (1f + temp4 * absX);

		float y = (float) (1f - oneBySqrt2pi * Math.exp(-X * X / 2f) * t * (c1 + t * (c2 + t * (c3 + t * (c4 + t * c5)))));

		if (X < 0f) return  1f - y;


		return y;
	}





	@BeforeClass
	public static void prepare() {
	
		randArray = new float[size];
		call_java = new float[size];
		put_java = new float[size];
		call_Rubus = new float[size];
		put_Rubus = new float[size];
		 call_AparAPI = new float[size];
		   put_AparAPI = new float[size];

		for (int i = 0; i < size; i++) {
			randArray[i] = i * 1.0f / size;
		}
		System.out.println("\n\n************************************************************\n");
		System.out.println(BlackScholes.class.getSimpleName()+" : Benchmark started for size: "+size);

	}
	/*
	 * @brief   Calculates the call and put prices by using Black Scholes model
	 * @param   s       Array of random values of current option price
	 * @param   sigma   Array of random values sigma
	 * @param   k       Array of random values strike price
	 * @param   t       Array of random values of expiration time
	 * @param   r       Array of random values of risk free interest rate
	 * @param   width   Width of call price or put price array
	 * @param   call    Array of calculated call price values
	 * @param   put     Array of calculated put price values
	 */
	@Test
	public  void Java() {
		 float S_LOWER_LIMIT = 10.0f;

		 float S_UPPER_LIMIT = 100.0f;

		 float K_LOWER_LIMIT = 10.0f;

		 float K_UPPER_LIMIT = 100.0f;

		 float T_LOWER_LIMIT = 1.0f;

		 float T_UPPER_LIMIT = 10.0f;

		 float R_LOWER_LIMIT = 0.01f;

		 float R_UPPER_LIMIT = 0.05f;

		 float SIGMA_LOWER_LIMIT = 0.01f;

		 float SIGMA_UPPER_LIMIT = 0.10f;


		for (int gid = 0; gid < size; gid++) {

			 float two = 2.0f;
			 float inRand = randArray[gid];
			 float S = S_LOWER_LIMIT * inRand + S_UPPER_LIMIT * (1.0f - inRand);
			 float K = K_LOWER_LIMIT * inRand + K_UPPER_LIMIT * (1.0f - inRand);
			 float T = T_LOWER_LIMIT * inRand + T_UPPER_LIMIT * (1.0f - inRand);
			 float R = R_LOWER_LIMIT * inRand + R_UPPER_LIMIT * (1.0f - inRand);
			 float sigmaVal = SIGMA_LOWER_LIMIT * inRand + SIGMA_UPPER_LIMIT * (1.0f - inRand);

			 float sigmaSqrtT = (float) (sigmaVal * Math.sqrt(T));

			 float d1 = (float) ((Math.log(S / K) + (R + sigmaVal * sigmaVal / two) * T) / sigmaSqrtT);
			 float d2 = d1 - sigmaSqrtT;

			 float KexpMinusRT = (float) (K * Math.exp(-R * T));

			float phiD1 = phi(d1);
			float phiD2 = phi(d2);

			call_java[gid] = S * phiD1 - KexpMinusRT * phiD2;

			phiD1 = phi(-d1);
			phiD2 = phi(-d2);

			put_java[gid] = KexpMinusRT * phiD2 - S * phiD1;
		}
	}
	
	public  void AparAPI() {
		final  float[] call_AparAPI = this.call_AparAPI;
		final  float[] put_AparAPI=this.put_AparAPI;
		final int size = this.size;
		final float randArray[] = this.randArray;
		Kernel kernel = new Kernel(){
		      /*
		      * For a description of the algorithm and the terms used, please see the
		      * documentation for this sample.
		      *
		      * On invocation of kernel blackScholes, each work thread calculates call price
		      * and put price values for given stock price, option strike price, 
		      * time to expiration date, risk free interest and volatility factor.
		      */

		      final float S_LOWER_LIMIT = 10.0f;

		      final float S_UPPER_LIMIT = 100.0f;

		      final float K_LOWER_LIMIT = 10.0f;

		      final float K_UPPER_LIMIT = 100.0f;

		      final float T_LOWER_LIMIT = 1.0f;

		      final float T_UPPER_LIMIT = 10.0f;

		      final float R_LOWER_LIMIT = 0.01f;

		      final float R_UPPER_LIMIT = 0.05f;

		      final float SIGMA_LOWER_LIMIT = 0.01f;

		      final float SIGMA_UPPER_LIMIT = 0.10f;

		      /**
		      * @brief   Abromowitz Stegun approxmimation for PHI (Cumulative Normal Distribution Function)
		      * @param   X input value
		      * @param   phi pointer to store calculated CND of X
		      */
		      float phi(float X) {
		         final float c1 = 0.319381530f;
		         final float c2 = -0.356563782f;
		         final float c3 = 1.781477937f;
		         final float c4 = -1.821255978f;
		         final float c5 = 1.330274429f;

		         final float zero = 0.0f;
		         final float one = 1.0f;
		         final float two = 2.0f;
		         final float temp4 = 0.2316419f;

		         final float oneBySqrt2pi = 0.398942280f;

		         float absX = abs(X);
		         float t = one / (one + temp4 * absX);

		         float y = one - oneBySqrt2pi * exp(-X * X / two) * t * (c1 + t * (c2 + t * (c3 + t * (c4 + t * c5))));

		         float result = (X < zero) ? (one - y) : y;

		         return result;
		      }

		      /*
		      * @brief   Calculates the call and put prices by using Black Scholes model
		      * @param   s       Array of random values of current option price
		      * @param   sigma   Array of random values sigma
		      * @param   k       Array of random values strike price
		      * @param   t       Array of random values of expiration time
		      * @param   r       Array of random values of risk free interest rate
		      * @param   width   Width of call price or put price array
		      * @param   call    Array of calculated call price values
		      * @param   put     Array of calculated put price values
		      */
		      @Override public void run() {
		         float d1, d2;
		         float phiD1, phiD2;
		         float sigmaSqrtT;
		         float KexpMinusRT;

		         int gid = getGlobalId();
		         float two = 2.0f;
		         float inRand = randArray[gid];
		         float S = S_LOWER_LIMIT * inRand + S_UPPER_LIMIT * (1.0f - inRand);
		         float K = K_LOWER_LIMIT * inRand + K_UPPER_LIMIT * (1.0f - inRand);
		         float T = T_LOWER_LIMIT * inRand + T_UPPER_LIMIT * (1.0f - inRand);
		         float R = R_LOWER_LIMIT * inRand + R_UPPER_LIMIT * (1.0f - inRand);
		         float sigmaVal = SIGMA_LOWER_LIMIT * inRand + SIGMA_UPPER_LIMIT * (1.0f - inRand);

		         sigmaSqrtT = sigmaVal * sqrt(T);

		         d1 = (log(S / K) + (R + sigmaVal * sigmaVal / two) * T) / sigmaSqrtT;
		         d2 = d1 - sigmaSqrtT;

		         KexpMinusRT = K * exp(-R * T);

		         phiD1 = phi(d1);
		         phiD2 = phi(d2);

		         call_AparAPI[gid] = S * phiD1 - KexpMinusRT * phiD2;

		         phiD1 = phi(-d1);
		         phiD2 = phi(-d2);

		         put_AparAPI[gid] = KexpMinusRT * phiD2 - S * phiD1;
		      }
		    
		
	};
	
	kernel.execute(size);
	kernel.dispose();
	}
//	 public static class BlackScholesKernel extends Kernel{
//		   /*
//		   Copyright (c) 2010-2011, Advanced Micro Devices, Inc.
//		   All rights reserved.
//
//		   Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
//		   following conditions are met:
//
//		   Redistributions of source code must retain the above copyright notice, this list of conditions and the following
//		   disclaimer. 
//
//		   Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
//		   disclaimer in the documentation and/or other materials provided with the distribution. 
//
//		   Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
//		   derived from this software without specific prior written permission. 
//
//		   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
//		   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//		   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//		   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//		   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
//		   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
//		   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//		   If you use the software (in whole or in part), you shall adhere to all applicable U.S., European, and other export
//		   laws, including but not limited to the U.S. Export Administration Regulations ("EAR"), (15 C.F.R. Sections 730 through
//		   774), and E.U. Council Regulation (EC) No 1334/2000 of 22 June 2000.  Further, pursuant to Section 740.6 of the EAR,
//		   you hereby certify that, except pursuant to a license granted by the United States Department of Commerce Bureau of 
//		   Industry and Security or as otherwise permitted pursuant to a License Exception under the U.S. Export Administration 
//		   Regulations ("EAR"), you will not (1) export, re-export or release to a national of a country in Country Groups D:1,
//		   E:1 or E:2 any restricted technology, software, or source code you receive hereunder, or (2) export to Country Groups
//		   D:1, E:1 or E:2 the direct product of such technology or software, if such foreign produced direct product is subject
//		   to national security controls as identified on the Commerce Control List (currently found in Supplement 1 to Part 774
//		   of EAR).  For the most current Country Group listings, or for additional information about the EAR or your obligations
//		   under those regulations, please refer to the U.S. Bureau of Industry and Security's website at http://www.bis.doc.gov/. 
//
//		   */
//	      /*
//	      * For a description of the algorithm and the terms used, please see the
//	      * documentation for this sample.
//	      *
//	      * On invocation of kernel blackScholes, each work thread calculates call price
//	      * and put price values for given stock price, option strike price, 
//	      * time to expiration date, risk free interest and volatility factor.
//	      */
//
//	      final float S_LOWER_LIMIT = 10.0f;
//
//	      final float S_UPPER_LIMIT = 100.0f;
//
//	      final float K_LOWER_LIMIT = 10.0f;
//
//	      final float K_UPPER_LIMIT = 100.0f;
//
//	      final float T_LOWER_LIMIT = 1.0f;
//
//	      final float T_UPPER_LIMIT = 10.0f;
//
//	      final float R_LOWER_LIMIT = 0.01f;
//
//	      final float R_UPPER_LIMIT = 0.05f;
//
//	      final float SIGMA_LOWER_LIMIT = 0.01f;
//
//	      final float SIGMA_UPPER_LIMIT = 0.10f;
//
//	      /**
//	      * @brief   Abromowitz Stegun approxmimation for PHI (Cumulative Normal Distribution Function)
//	      * @param   X input value
//	      * @param   phi pointer to store calculated CND of X
//	      */
//	      float phi(float X) {
//	         final float c1 = 0.319381530f;
//	         final float c2 = -0.356563782f;
//	         final float c3 = 1.781477937f;
//	         final float c4 = -1.821255978f;
//	         final float c5 = 1.330274429f;
//
//	         final float zero = 0.0f;
//	         final float one = 1.0f;
//	         final float two = 2.0f;
//	         final float temp4 = 0.2316419f;
//
//	         final float oneBySqrt2pi = 0.398942280f;
//
//	         float absX = abs(X);
//	         float t = one / (one + temp4 * absX);
//
//	         float y = one - oneBySqrt2pi * exp(-X * X / two) * t * (c1 + t * (c2 + t * (c3 + t * (c4 + t * c5))));
//
//	         float result = (X < zero) ? (one - y) : y;
//
//	         return result;
//	      }
//
//	      /*
//	      * @brief   Calculates the call and put prices by using Black Scholes model
//	      * @param   s       Array of random values of current option price
//	      * @param   sigma   Array of random values sigma
//	      * @param   k       Array of random values strike price
//	      * @param   t       Array of random values of expiration time
//	      * @param   r       Array of random values of risk free interest rate
//	      * @param   width   Width of call price or put price array
//	      * @param   call    Array of calculated call price values
//	      * @param   put     Array of calculated put price values
//	      */
//	      @Override public void run() {
//	         float d1, d2;
//	         float phiD1, phiD2;
//	         float sigmaSqrtT;
//	         float KexpMinusRT;
//
//	         int gid = getGlobalId();
//	         float two = 2.0f;
//	         float inRand = randArray[gid];
//	         float S = S_LOWER_LIMIT * inRand + S_UPPER_LIMIT * (1.0f - inRand);
//	         float K = K_LOWER_LIMIT * inRand + K_UPPER_LIMIT * (1.0f - inRand);
//	         float T = T_LOWER_LIMIT * inRand + T_UPPER_LIMIT * (1.0f - inRand);
//	         float R = R_LOWER_LIMIT * inRand + R_UPPER_LIMIT * (1.0f - inRand);
//	         float sigmaVal = SIGMA_LOWER_LIMIT * inRand + SIGMA_UPPER_LIMIT * (1.0f - inRand);
//
//	         sigmaSqrtT = sigmaVal * sqrt(T);
//
//	         d1 = (log(S / K) + (R + sigmaVal * sigmaVal / two) * T) / sigmaSqrtT;
//	         d2 = d1 - sigmaSqrtT;
//
//	         KexpMinusRT = K * exp(-R * T);
//
//	         phiD1 = phi(d1);
//	         phiD2 = phi(d2);
//
//	         call[gid] = S * phiD1 - KexpMinusRT * phiD2;
//
//	         phiD1 = phi(-d1);
//	         phiD2 = phi(-d2);
//
//	         put[gid] = KexpMinusRT * phiD2 - S * phiD1;
//	      }
//
//	      private float randArray[];
//
//	      private float put[];
//
//	      private float call[];
//
//	      public BlackScholesKernel(int size, float[]randArray, float[] call, floa) {
//	         randArray = new float[size];
//	         call = new float[size];
//	         put = new float[size];
//
//	         for (int i = 0; i < size; i++) {
//	            randArray[i] = i * 1.0f / size;
//	         }
//	      }
//
//	      public void showArray(float ary[], String name, int count) {
//	         String line;
//	         line = name + ": ";
//	         for (int i = 0; i < count; i++) {
//	            if (i > 0)
//	               line += ", ";
//	            line += ary[i];
//	         }
//	         System.out.println(line);
//	      }
//
//	      public void showResults(int count) {
//	         showArray(call, "Call Prices", count);
//	         showArray(put, "Put  Prices", count);
//	      }
//	   }
//
//	   public static void main(String[] _args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
//
//	      int size = Integer.getInteger("size", 512);
//	      Range range = Range.create(size);
//	      int iterations = Integer.getInteger("iterations", 5);
//	      System.out.println("size =" + size);
//	      System.out.println("iterations =" + iterations);
//	      BlackScholesKernel kernel = new BlackScholesKernel(size);
//
//	      long totalExecTime = 0;
//	      long iterExecTime = 0;
//	      /*
//	      for (int i = 0; i < iterations; i++) {
//	         iterExecTime = kernel.execute(size).getExecutionTime();
//	         totalExecTime += iterExecTime;
//	      }*/
//	      kernel.execute(range, iterations);
//	      System.out.println("Average execution time " + kernel.getAccumulatedExecutionTime() / iterations);
//	      kernel.showResults(10);
//
//	      kernel.dispose();
//	   }
	
	 private static CLContext context = JavaCL.createBestContext();


			   private static final String kernel_1672197010 = ""+
					   " static float phi(float v0_FLOAT) {                           "+
					   "      float v7_FLOAT;                                         "+
					   "      float v3_FLOAT;                                         "+
					   "      float v9_FLOAT;                                         "+
					   "      float v5_FLOAT;                                         "+
					   "      float v2_FLOAT;                                         "+
					   "      float v6_FLOAT;                                         "+
					   "      float v8_FLOAT;                                         "+
					   "      float v10_FLOAT;                                        "+
					   "      float v1_FLOAT;                                         "+
					   "      float v4_FLOAT;                                         "+
					   "      {                                                       "+
					   "           }/*0*/                                             "+
					   "      {                                                       "+
					   "            float t90 = 0.31938154f;                          "+
					   "           v1_FLOAT = t90;                                    "+
					   "           }/*1*/                                             "+
					   "      {                                                       "+
					   "            float t91 = -0.35656378f;                         "+
					   "           v2_FLOAT = t91;                                    "+
					   "           }/*2*/                                             "+
					   "      {                                                       "+
					   "            float t92 = 1.7814779f;                           "+
					   "           v3_FLOAT = t92;                                    "+
					   "           }/*3*/                                             "+
					   "      {                                                       "+
					   "            float t93 = -1.8212559f;                          "+
					   "           v4_FLOAT = t93;                                    "+
					   "           }/*4*/                                             "+
					   "      {                                                       "+
					   "            float t94 = 1.3302745f;                           "+
					   "           v5_FLOAT = t94;                                    "+
					   "           }/*5*/                                             "+
					   "      {                                                       "+
					   "            float t95 = 0.2316419f;                           "+
					   "           v6_FLOAT = t95;                                    "+
					   "           }/*6*/                                             "+
					   "      {                                                       "+
					   "            float t96 = 0.3989423f;                           "+
					   "           v7_FLOAT = t96;                                    "+
					   "           }/*7*/                                             "+
					   "      {                                                       "+
					   "            float t97 = v0_FLOAT;                             "+
					   "            float t98 = fabs(t97);                            "+
					   "           v8_FLOAT = t98;                                    "+
					   "           }/*8*/                                             "+
					   "      {                                                       "+
					   "            float t99 = v8_FLOAT;                             "+
					   "            float t100 = 1.0f;                                "+
					   "            float t101 = 0.2316419f;                          "+
					   "            float t102 = t101*t99;                            "+
					   "            float t103 = t100+t102;                           "+
					   "            float t104 = t100/t103;                           "+
					   "           v9_FLOAT = t104;                                   "+
					   "           }/*9*/                                             "+
					   "      {                                                       "+
					   "            float t105 = v0_FLOAT;                            "+
					   "            float t106 = v0_FLOAT;                            "+
					   "            float t107 = -t105;                               "+
					   "            float t108 = t107*t106;                           "+
					   "            float t109 = 2.0f;                                "+
					   "            float t110 = t108/t109;                           "+
					   "            float t111 = (float) t110;                        "+
					   "            float t112 = exp(t111);                           "+
					   "            float t113 = v9_FLOAT;                            "+
					   "            float t114 = v9_FLOAT;                            "+
					   "            float t115 = v9_FLOAT;                            "+
					   "            float t116 = v9_FLOAT;                            "+
					   "            float t117 = v9_FLOAT;                            "+
					   "            float t118 = 1.0;                                 "+
					   "            float t119 = 0.3989422917366028;                  "+
					   "            float t120 = t119*t112;                           "+
					   "            float t121 = (float) t113;                        "+
					   "            float t122 = t120*t121;                           "+
					   "            float t123 = 0.31938154f;                         "+
					   "            float t124 = -0.35656378f;                        "+
					   "            float t125 = 1.7814779f;                          "+
					   "            float t126 = -1.8212559f;                         "+
					   "            float t127 = 1.3302745f;                          "+
					   "            float t128 = t117*t127;                           "+
					   "            float t129 = t126+t128;                           "+
					   "            float t130 = t116*t129;                           "+
					   "            float t131 = t125+t130;                           "+
					   "            float t132 = t115*t131;                           "+
					   "            float t133 = t124+t132;                           "+
					   "            float t134 = t114*t133;                           "+
					   "            float t135 = t123+t134;                           "+
					   "            float t136 = (float) t135;                        "+
					   "            float t137 = t122*t136;                           "+
					   "            float t138 = t118-t137;                           "+
					   "            float t139 = (float) t138;                        "+
					   "           v10_FLOAT = t139;                                  "+
					   "           }/*10*/                                            "+
					   "      {                                                       "+
					   "            float t140 = v0_FLOAT;                            "+
					   "            float t141 = 0.0f;                                "+
					   "            int t142 = (t140<t141) ? -1 : (t140>t141) ? 1 : 0; "+
					   "            int t143 = 0;                                     "+
					   "           if(t142<t143) { {                                  "+
					   "                      float t144 = v10_FLOAT;                 "+
					   "                      float t145 = 1.0f;                      "+
					   "                      float t146 = t145-t144;                 "+
					   "                     return t146;                             "+
					   "                     }/*13*/                                  "+
					   "                }/* end if */                                 "+
					   "           {                                                  "+
					   "                 float t147 = v10_FLOAT;                      "+
					   "                return t147;                                  "+
					   "                }/*12*/                                       "+
					   "           }/*11*/                                            "+
					   "      }"+ 
					   " __kernel void kernel_1672197010(const int limit0, float v7_FLOAT, float v6_FLOAT, float v8_FLOAT, __global float* v3_2891, float v14_FLOAT, float v9_FLOAT, __global float* v1_2891, float v5_FLOAT, float v13_FLOAT, float v10_FLOAT, float v12_FLOAT, int v15_INT, __global float* v2_2891, float v11_FLOAT) { "+
					   "      int dim0 = get_global_id(0);                            "+
					   "      v15_INT += 1 * dim0;                                    "+
					   "      if(v15_INT >= limit0) return;                           "+
					   "      float v28_FLOAT;                                        "+
					   "      float v18_FLOAT;                                        "+
					   "      float v23_FLOAT;                                        "+
					   "      float v20_FLOAT;                                        "+
					   "      float v25_FLOAT;                                        "+
					   "      float v17_FLOAT;                                        "+
					   "      float v22_FLOAT;                                        "+
					   "      float v27_FLOAT;                                        "+
					   "      float v24_FLOAT;                                        "+
					   "      float v19_FLOAT;                                        "+
					   "      float v16_FLOAT;                                        "+
					   "      float v26_FLOAT;                                        "+
					   "      float v21_FLOAT;                                        "+
					   "      {                                                       "+
					   "            float t0 = 2.0f;                                  "+
					   "           v16_FLOAT = t0;                                    "+
					   "           }/*29*/                                            "+
					   "      {                                                       "+
					   "           __global float* t1 = v1_2891;                      "+
					   "            int t2 = v15_INT;                                 "+
					   "            float t3 = t1[t2];                                "+
					   "           v17_FLOAT = t3;                                    "+
					   "           }/*30*/                                            "+
					   "      {                                                       "+
					   "            float t4 = v5_FLOAT;                              "+
					   "            float t5 = v17_FLOAT;                             "+
					   "            float t6 = v6_FLOAT;                              "+
					   "            float t7 = v17_FLOAT;                             "+
					   "            float t8 = t4*t5;                                 "+
					   "            float t9 = 1.0f;                                  "+
					   "            float t10 = t9-t7;                                "+
					   "            float t11 = t6*t10;                               "+
					   "            float t12 = t8+t11;                               "+
					   "           v18_FLOAT = t12;                                   "+
					   "           }/*31*/                                            "+
					   "      {                                                       "+
					   "            float t13 = v7_FLOAT;                             "+
					   "            float t14 = v17_FLOAT;                            "+
					   "            float t15 = v8_FLOAT;                             "+
					   "            float t16 = v17_FLOAT;                            "+
					   "            float t17 = t13*t14;                              "+
					   "            float t18 = 1.0f;                                 "+
					   "            float t19 = t18-t16;                              "+
					   "            float t20 = t15*t19;                              "+
					   "            float t21 = t17+t20;                              "+
					   "           v19_FLOAT = t21;                                   "+
					   "           }/*32*/                                            "+
					   "      {                                                       "+
					   "            float t22 = v9_FLOAT;                             "+
					   "            float t23 = v17_FLOAT;                            "+
					   "            float t24 = v10_FLOAT;                            "+
					   "            float t25 = v17_FLOAT;                            "+
					   "            float t26 = t22*t23;                              "+
					   "            float t27 = 1.0f;                                 "+
					   "            float t28 = t27-t25;                              "+
					   "            float t29 = t24*t28;                              "+
					   "            float t30 = t26+t29;                              "+
					   "           v20_FLOAT = t30;                                   "+
					   "           }/*33*/                                            "+
					   "      {                                                       "+
					   "            float t31 = v11_FLOAT;                            "+
					   "            float t32 = v17_FLOAT;                            "+
					   "            float t33 = v12_FLOAT;                            "+
					   "            float t34 = v17_FLOAT;                            "+
					   "            float t35 = t31*t32;                              "+
					   "            float t36 = 1.0f;                                 "+
					   "            float t37 = t36-t34;                              "+
					   "            float t38 = t33*t37;                              "+
					   "            float t39 = t35+t38;                              "+
					   "           v21_FLOAT = t39;                                   "+
					   "           }/*34*/                                            "+
					   "      {                                                       "+
					   "            float t40 = v13_FLOAT;                            "+
					   "            float t41 = v17_FLOAT;                            "+
					   "            float t42 = v14_FLOAT;                            "+
					   "            float t43 = v17_FLOAT;                            "+
					   "            float t44 = t40*t41;                              "+
					   "            float t45 = 1.0f;                                 "+
					   "            float t46 = t45-t43;                              "+
					   "            float t47 = t42*t46;                              "+
					   "            float t48 = t44+t47;                              "+
					   "           v22_FLOAT = t48;                                   "+
					   "           }/*35*/                                            "+
					   "      {                                                       "+
					   "            float t49 = v22_FLOAT;                            "+
					   "            float t50 = v20_FLOAT;                            "+
					   "            float t51 = (float) t50;                          "+
					   "            float t52 = sqrt(t51);                            "+
					   "            float t53 = (float) t49;                          "+
					   "            float t54 = t53*t52;                              "+
					   "            float t55 = (float) t54;                          "+
					   "           v23_FLOAT = t55;                                   "+
					   "           }/*36*/                                            "+
					   "      {                                                       "+
					   "            float t56 = v18_FLOAT;                            "+
					   "            float t57 = v19_FLOAT;                            "+
					   "            float t58 = t56/t57;                              "+
					   "            float t59 = (float) t58;                          "+
					   "            float t60 = log(t59);                             "+
					   "            float t61 = v21_FLOAT;                            "+
					   "            float t62 = v22_FLOAT;                            "+
					   "            float t63 = v22_FLOAT;                            "+
					   "            float t64 = v16_FLOAT;                            "+
					   "            float t65 = v20_FLOAT;                            "+
					   "            float t66 = v23_FLOAT;                            "+
					   "            float t67 = t62*t63;                              "+
					   "            float t68 = t67/t64;                              "+
					   "            float t69 = t61+t68;                              "+
					   "            float t70 = t69*t65;                              "+
					   "            float t71 = (float) t70;                          "+
					   "            float t72 = t60+t71;                              "+
					   "            float t73 = (float) t66;                          "+
					   "            float t74 = t72/t73;                              "+
					   "            float t75 = (float) t74;                          "+
					   "           v24_FLOAT = t75;                                   "+
					   "           }/*37*/                                            "+
					   "      {                                                       "+
					   "            float t76 = v24_FLOAT;                            "+
					   "            float t77 = v23_FLOAT;                            "+
					   "            float t78 = t76-t77;                              "+
					   "           v25_FLOAT = t78;                                   "+
					   "           }/*38*/                                            "+
					   "      {                                                       "+
					   "            float t79 = v19_FLOAT;                            "+
					   "            float t80 = v21_FLOAT;                            "+
					   "            float t81 = v20_FLOAT;                            "+
					   "            float t82 = -t80;                                 "+
					   "            float t83 = t82*t81;                              "+
					   "            float t84 = (float) t83;                          "+
					   "            float t85 = exp(t84);                             "+
					   "            float t86 = (float) t79;                          "+
					   "            float t87 = t86*t85;                              "+
					   "            float t88 = (float) t87;                          "+
					   "           v26_FLOAT = t88;                                   "+
					   "           }/*39*/                                            "+
					   "      {                                                       "+
					   "            float t89 = v24_FLOAT;                            "+
					   "            float t148 = phi(t89);                            "+
					   "           v27_FLOAT = t148;                                  "+
					   "           }/*40*/                                            "+
					   "      {                                                       "+
					   "            float t149 = v25_FLOAT;                           "+
					   "            float t150 = phi(t149);                           "+
					   "           v28_FLOAT = t150;                                  "+
					   "           }/*41*/                                            "+
					   "      {                                                       "+
					   "           __global float* t151 = v2_2891;                    "+
					   "            int t152 = v15_INT;                               "+
					   "            float t153 = v18_FLOAT;                           "+
					   "            float t154 = v27_FLOAT;                           "+
					   "            float t155 = v26_FLOAT;                           "+
					   "            float t156 = v28_FLOAT;                           "+
					   "            float t157 = t153*t154;                           "+
					   "            float t158 = t155*t156;                           "+
					   "            float t159 = t157-t158;                           "+
					   "           t151[t152] = t159;                                 "+
					   "           }/*42*/                                            "+
					   "      {                                                       "+
					   "            float t160 = v24_FLOAT;                           "+
					   "            float t161 = -t160;                               "+
					   "            float t162 = phi(t161);                           "+
					   "           v27_FLOAT = t162;                                  "+
					   "           }/*43*/                                            "+
					   "      {                                                       "+
					   "            float t163 = v25_FLOAT;                           "+
					   "            float t164 = -t163;                               "+
					   "            float t165 = phi(t164);                           "+
					   "           v28_FLOAT = t165;                                  "+
					   "           }/*44*/                                            "+
					   "      {                                                       "+
					   "           __global float* t166 = v3_2891;                    "+
					   "            int t167 = v15_INT;                               "+
					   "            float t168 = v26_FLOAT;                           "+
					   "            float t169 = v28_FLOAT;                           "+
					   "            float t170 = v18_FLOAT;                           "+
					   "            float t171 = v27_FLOAT;                           "+
					   "            float t172 = t168*t169;                           "+
					   "            float t173 = t170*t171;                           "+
					   "            float t174 = t172-t173;                           "+
					   "           t166[t167] = t174;                                 "+
					   "           }/*45*/                                            "+
					   "      {                                                       "+
					   "           v15_INT += 1;                                      "+
					   "           }/*46*/                                            "+
					   "      {                                                       "+
					   "           }/*94*/                                            "+
					   "      }";
			  
    @Test
	public  void Rubus() {
	
		    float f1 = 10.0F;
		    
		    float f2 = 100.0F;
		    
		    float f3 = 10.0F;
		    
		    float f4 = 100.0F;
		    
		    float f5 = 1.0F;
		    
		    float f6 = 10.0F;
		    
		    float f7 = 0.01F;
		    
		    float f8 = 0.05F;
		    
		    float f9 = 0.01F;
		    
		    float f10 = 0.1F;
	
		    int i = 0;
		    
		    kernel_1672197010(size, f3, f2, f4, put_Rubus, f10, f5, randArray, f1, f9, f6, f8, i, call_Rubus, f7);
		  }
	  
		  
		  public static void kernel_1672197010(int paramInt1, float paramFloat1, float paramFloat2, float paramFloat3, float[] paramArrayOfFloat1, float paramFloat4, float paramFloat5, float[] paramArrayOfFloat2, float paramFloat6, float paramFloat7, float paramFloat8, float paramFloat9, int paramInt2, float[] paramArrayOfFloat3, float paramFloat10)
		  {
		    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
		    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
		    String[] arrayOfString = { kernel_1672197010 };
		    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
		    CLKernel localCLKernel = arrayOfCLKernel[0];
		    float f1 = paramFloat1;
		    float f2 = paramFloat2;
		    float f3 = paramFloat3;
		    CLBuffer localCLBuffer1 = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToFloats(paramArrayOfFloat1), true);
		    float f4 = paramFloat4;
		    float f5 = paramFloat5;
		    CLBuffer localCLBuffer2 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToFloats(paramArrayOfFloat2), true);
		    float f6 = paramFloat6;
		    float f7 = paramFloat7;
		    float f8 = paramFloat8;
		    float f9 = paramFloat9;
		    int i = paramInt2;
		    CLBuffer localCLBuffer3 = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToFloats(paramArrayOfFloat3), true);
		    float f10 = paramFloat10;
		    localCLKernel.setArgs(new Object[] { paramInt1, f1, f2, f3, localCLBuffer1, f4, f5, localCLBuffer2, f6, f7, f8, f9, i, localCLBuffer3, f10 });
		    int j = paramInt1 - paramInt2 / 1;
		    CLEvent[] arrayOfCLEvent = { null };
		    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { j }, arrayOfCLEvent);
		    localCLQueue.finish();
		    localCLBuffer1.read(localCLQueue, new CLEvent[] { localCLEvent }).getFloats(paramArrayOfFloat1);
		    localCLBuffer3.read(localCLQueue, new CLEvent[] { localCLEvent }).getFloats(paramArrayOfFloat3);
		  }




	@AfterClass
	public static void compareResult() {

		if(!compareLogEnable)return;
		
		System.out.println(BlackScholes.class.getSimpleName()+" : Benchmark completed for size: "+size);
		System.out.println(BlackScholes.class.getSimpleName()+" : Comparing Output : ");
		boolean isEqual = compareArrays(call_java, call_Rubus) && compareArrays(put_java, put_Rubus);
		
		printArray(call_java, "CPU call", size);
		printArray(call_Rubus, "GPU call", size);
		System.out.println();

		printArray(put_java, "CPU put", size);
		printArray(put_Rubus, "GPU put", size);
		
		printArray(call_AparAPI, "Apar Call", size);
		printArray(put_AparAPI, "Apar Put", size);
		
		
		//System.out.println(BlackScholes.class.getSimpleName()+" : Result : "+(isEqual?"OK":"Failed"));

		System.out.println("\n\n************************************************************\n");
	}

	private static boolean compareArrays(float inA[], float inB[]){
		if(inA==null || inB==null) return false;
		if(inA.length != inB.length) return false;

		for (int i = 0; i < inA.length; i++) {
			if(inA[i]!=inB[i]) return false;
		}

		return true;


	}



	public static void main(String[] _args) {
		setupDBFile(BlackScholes.class.getName());

		deleteDBFileIfExist();
	//	setInput(new int[]{512,1024,2048,4096,8192,16384,32768,65536});
		setInput(new int[]{64});

		
		//startBenchmark(BlackScholes.class);
//size = 64;
		//prepare();
		//GPU();
//		for (int i = 0; i < inputSizes.length; i++) {
//			JUnitCore junit = new JUnitCore();
//			size = inputSizes[i];
//			junit.run(BlackScholes.class);
//		}
		
	
	startBenchmark(BlackScholes.class);	
		//prepare();
	//	GPU();

	//	CPU();
		
	//	compareResult();
		
	}


}
