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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.bridj.Pointer;
import org.junit.After;
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

/**
 * Mandelbrot Benchmark. Adapted from the Computer Language Benchmarks Game
 * (http://shootout.alioth.debian.org/) so that all cells are calculated before
 * outputing the complete image (since sequentially outputing pixels restricts
 * parallelisation). Some modifications are made like 2d arrays are converted to
 * 1d arrays
 *
 * Note that <code>float</code>s are used since not all GPUs support double
 * precision, and also that exceptions are just thrown out of <code>main</code>
 * to avoid <code>try { ... } catch { ... }</code> blocks.
 *
 * @author Stefan Krause
 * @author Peter Calvert
 * @author Muhammad Adnan
 */
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY,timeUnit=TimeUnit.MILLISECONDS,customLabel="Size")
public class Mandelbrot extends RubusBenchmark {

	// Test on /////////////////////////
	public static int iterations = 512;
	public static float LIMIT = 4.0f;
	// /////////////////////////////////
	// Data///////////////////////////
	public static int width, height;
	private static short dataGPU[];
	private static short dataCPU[];
	private static short dataAparAPI[];
	private static float spacing;
	// private static int colorRange = 255;
	// ////////////////////////////////
	static int i = 0;

	@BeforeClass
	public static void initialize() {
		width = height = size;
		spacing = 2.0f / width;
		dataGPU = new short[height * width];
		dataCPU = new short[height * width];
		dataAparAPI = new short[height * width];

	}

	@After
	public void after() {
		try {
			output(new File((i++) + "abc.jpg"), dataAparAPI);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Mandelbrot() {
		initialize();
	}

	public void Java() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float Zr = 0.0f;
				float Zi = 0.0f;
				float Cr = (x * spacing - 1.5f);
				float Ci = (y * spacing - 1.0f);

				float ZrN = 0;
				float ZiN = 0;
				int i;
				int j = 0;

				for (i = 0; (i < iterations); i++) { // && (ZiN + ZrN <= LIMIT)
					if (ZiN + ZrN <= LIMIT) {
						j = i;

						Zi = 2.0f * Zr * Zi + Ci;
						Zr = ZrN - ZiN + Cr;
						ZiN = Zi * Zi;
						ZrN = Zr * Zr;

					}

				}
				dataCPU[y * height + x] = (short) ((j * 255) / iterations);
			}
		}
	}

	private static final String kernel_20211761 = "" + " __kernel void kernel_20211761(const int limit0, int v3_INT, int v5_INT, float v6_FLOAT, __global short* v1_2904, int v2_INT, int v7_INT, float v4_FLOAT) { " + "      int dim0 = get_global_id(0);                            " + "      v7_INT += 1 * dim0;                                     " + "      if(v7_INT >= limit0) return;                            " + "      float v13_FLOAT;                                        " + "      float v10_FLOAT;                                        " + "      int v15_INT;                                            " + "      float v9_FLOAT;                                         " + "      int v16_INT;                                            "
			+ "      int v8_INT;                                             " + "      float v11_FLOAT;                                        " + "      float v12_FLOAT;                                        " + "      float v14_FLOAT;                                        " + "      {                                                       " + "            int t0 = 0;                                       " + "           v8_INT = t0;                                       " + "           }/*12*/                                            " + "      {                                                       " + "           }/*13*/                                            " + "      {                                                       "
			+ "            int t1 = v3_INT;                                  " + "           while(v8_INT < t1) {                               " + "                {                                             " + "                      float t2 = 0.0f;                        " + "                     v9_FLOAT = t2;                           " + "                     }/*15*/                                  " + "                {                                             " + "                      float t3 = 0.0f;                        " + "                     v10_FLOAT = t3;                          " + "                     }/*16*/                                  " + "                {                                             "
			+ "                      int t4 = v8_INT;                        " + "                      float t5 = v4_FLOAT;                    " + "                      float t6 = (float) t4;                  " + "                      float t7 = t6*t5;                       " + "                      float t8 = 1.5f;                        " + "                      float t9 = t7-t8;                       " + "                     v11_FLOAT = t9;                          " + "                     }/*17*/                                  " + "                {                                             " + "                      int t10 = v7_INT;                       " + "                      float t11 = v4_FLOAT;                   "
			+ "                      float t12 = (float) t10;                " + "                      float t13 = t12*t11;                    " + "                      float t14 = 1.0f;                       " + "                      float t15 = t13-t14;                    " + "                     v12_FLOAT = t15;                         " + "                     }/*18*/                                  " + "                {                                             " + "                      float t16 = 0.0f;                       " + "                     v13_FLOAT = t16;                         " + "                     }/*19*/                                  " + "                {                                             "
			+ "                      float t17 = 0.0f;                       " + "                     v14_FLOAT = t17;                         " + "                     }/*20*/                                  " + "                {                                             " + "                      int t18 = 0;                            " + "                     v16_INT = t18;                           " + "                     }/*21*/                                  " + "                {                                             " + "                      int t19 = 0;                            " + "                     v15_INT = t19;                           " + "                     }/*22*/                                  "
			+ "                {                                             " + "                     }/*23*/                                  " + "                {                                             " + "                      int t20 = v5_INT;                       " + "                     while(v15_INT < t20) {                   " + "                          {                                   " + "                                float t21 = v14_FLOAT;        " + "                                float t22 = v13_FLOAT;        " + "                                float t23 = v6_FLOAT;         " + "                                float t24 = t21+t22;          " + "                                int t25 = (t24<t23) ? -1 : (t24>t23) ? 1 : 0; "
			+ "                                int t26 = 0;                  " + "                               if(t25<=t26) { {               " + "                                         }/*27*/              " + "                                    {                         " + "                                          int t27 = v15_INT;  " + "                                         v16_INT = t27;       " + "                                         }/*28*/              " + "                                    {                         " + "                                          float t28 = v9_FLOAT; " + "                                          float t29 = v10_FLOAT; " + "                                          float t30 = v12_FLOAT; "
			+ "                                          float t31 = 2.0f;   " + "                                          float t32 = t31*t28; " + "                                          float t33 = t32*t29; " + "                                          float t34 = t33+t30; " + "                                         v10_FLOAT = t34;     " + "                                         }/*29*/              " + "                                    {                         " + "                                          float t35 = v13_FLOAT; " + "                                          float t36 = v14_FLOAT; " + "                                          float t37 = v11_FLOAT; " + "                                          float t38 = t35-t36; "
			+ "                                          float t39 = t38+t37; " + "                                         v9_FLOAT = t39;      " + "                                         }/*30*/              " + "                                    {                         " + "                                          float t40 = v10_FLOAT; " + "                                          float t41 = v10_FLOAT; " + "                                          float t42 = t40*t41; " + "                                         v14_FLOAT = t42;     " + "                                         }/*31*/              " + "                                    {                         " + "                                          float t43 = v9_FLOAT; "
			+ "                                          float t44 = v9_FLOAT; " + "                                          float t45 = t43*t44; " + "                                         v13_FLOAT = t45;     " + "                                         }/*32*/              " + "                                    }/* end if */             " + "                               {                              " + "                                    v15_INT += 1;             " + "                                    }/*26*/                   " + "                               {                              " + "                                    }/*43*/                   " + "                               }/*25*/                        "
			+ "                          }/*loop end*/                       " + "                     }/*49*/                                  " + "                {                                             " + "                     }/*33*/                                  " + "                {                                             " + "                     __global short* t46 = v1_2904;           " + "                      int t47 = v7_INT;                       " + "                      int t48 = v2_INT;                       " + "                      int t49 = v8_INT;                       " + "                      int t50 = v16_INT;                      " + "                      int t51 = v5_INT;                       "
			+ "                      int t52 = t47*t48;                      " + "                      int t53 = t52+t49;                      " + "                      short t54 = 255;                        " + "                      int t55 = t50*t54;                      " + "                      int t56 = t55/t51;                      " + "                      short t57 = (short) t56;                " + "                     t46[t53] = t57;                          " + "                     }/*34*/                                  " + "                {                                             " + "                     v8_INT += 1;                             " + "                     }/*35*/                                  "
			+ "                {                                             " + "                     }/*41*/                                  " + "                }/*loop end*/                                 " + "           }/*48*/                                            " + "      {                                                       " + "           }/*36*/                                            " + "      {                                                       " + "           v7_INT += 1;                                       " + "           }/*37*/                                            " + "      {                                                       " + "           }/*45*/                                            " + "      }";

	public void computeGPU(short[] paramArrayOfShort) {
		int i = size;
		int j = size;
		float f1 = spacing;
		int k = iterations;
		float f2 = LIMIT;
		int m = 0;
		kernel_20211761(i, j, k, f2, paramArrayOfShort, i, m, f1);
	}

	public static void kernel_20211761(int paramInt1, int paramInt2, int paramInt3, float paramFloat1, short[] paramArrayOfShort, int paramInt4, int paramInt5, float paramFloat2) {
		CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
		CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
		String[] arrayOfString = { kernel_20211761 };
		CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
		CLKernel localCLKernel = arrayOfCLKernel[0];
		int i = paramInt2;
		int j = paramInt3;
		float f1 = paramFloat1;
		CLBuffer localCLBuffer = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToShorts(paramArrayOfShort), true);
		int k = paramInt4;
		int m = paramInt5;
		float f2 = paramFloat2;
		localCLKernel.setArgs(new Object[] { paramInt1, i, j, f1, localCLBuffer, k, m, f2 });
		int n = paramInt1 - paramInt5 / 1;
		CLEvent[] arrayOfCLEvent = { null };
		CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { n }, arrayOfCLEvent);
		localCLQueue.finish();
		localCLBuffer.read(localCLQueue, new CLEvent[] { localCLEvent }).getShorts(paramArrayOfShort);
	}

	public void output(File out, short data[]) throws IOException {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r = img.getRaster();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// System.out.print(" "+x+","+y);
				r.setSample(x, y, 0, data[y * height + x]);
			}
			// System.out.println();
		}

		ImageIO.write(img, "png", out);
	}

	public static void main(String[] args) throws IOException {
		// Mandelbrot.size = 4096;
		// Mandelbrot m = new Mandelbrot();
		// m.CPU();
		// m.GPU();
		// printArray( m.dataCPU, "CPU", size);
		// printArray( m.dataGPU, "GPU", size);
		// File outimage = new File(System.currentTimeMillis()+".png");
		// m.output(outimage, m.dataGPU);
		// FileUtil.openFile(outimage);

		// System.out.println("Completed");

		 runBenchmark(Mandelbrot.class,new int[]{32,64,128,256,512, 1024,
		 2048, 4096});

		//runBenchmark(Mandelbrot.class, new int[] { 32 });
	}

	private static CLContext context = JavaCL.createBestContext();

	@Override
	@Test
	public void Rubus() {
		computeGPU(dataGPU);
	}

	@Override
	@Test
	public void AparAPI() {

		final short rgb[] = this.dataAparAPI;
		/** Mandelbrot image width. */
		final int width = this.width;
		final float LIMIT = this.LIMIT;
		final int iterations = this.iterations;
		/** Mandelbrot image height. */
		final int height = this.height;
		final float spacing = this.spacing;
		Kernel kernel = new Kernel() {

			/**
			 * RGB buffer used to store the Mandelbrot image. This buffer holds
			 * (width * height) RGB values.
			 */

			/** Maximum iterations for Mandelbrot. */

			/**
			 * Mutable values of scale, offsetx and offsety so that we can
			 * modify the zoom level and position of a view.
			 */
			// private float scale = .0f;
			//
			// private float offsetx = .0f;
			//
			// private float offsety = .0f;

			/**
			 * Initialize the Kernel.
			 * 
			 * @param _width
			 *            Mandelbrot image width
			 * @param _height
			 *            Mandelbrot image height
			 * @param _rgb
			 *            Mandelbrot image RGB buffer
			 * @param _pallette
			 *            Mandelbrot image palette
			 */
			// public MandelKernel(int _width, int _height, int[] _rgb) {
			// //Initialize palette values
			// for (int i = 0; i < maxIterations; i++) {
			// final float h = i / (float) maxIterations;
			// final float b = 1.0f - (h * h);
			// pallette[i] = Color.HSBtoRGB(h, 1f, b);
			// }
			//
			// width = _width;
			// height = _height;
			// rgb = _rgb;
			//
			// }

			// public int getCount(float x, float y) {
			// int count = 0;
			//
			// float zx = x;
			// float zy = y;
			// float new_zx = 0f;
			//
			// // Iterate until the algorithm converges or until maxIterations
			// are reached.
			// while ((count < iterations) && (((zx * zx) + (zy * zy)) < limit))
			// {
			// new_zx = ((zx * zx) - (zy * zy)) + x;
			// zy = (2 * zx * zy) + y;
			// zx = new_zx;
			// count++;
			// }
			//
			// return count;
			// }

			@Override
			public void run() {

				// /** Determine which RGB value we are going to process
				// (0..RGB.length). */
				final int gid = getGlobalId();
				//
				// /** Translate the gid into an x an y value. */
				final float x = ((gid % width));

				final float y = ((gid / height));
				//
				// int count = getCount(x, y);
				//
				// // Pull the value out of the palette for this iteration
				// count.
				// rgb[gid] = (short)((count * 255) / iterations); ;

				float Zr = 0.0f;
				float Zi = 0.0f;
				float Cr = (x * spacing - 1.5f);
				float Ci = (y * spacing - 1.0f);

				float ZrN = 0;
				float ZiN = 0;
				int i;
				int j = 0;

				for (i = 0; (i < iterations) && (ZiN + ZrN <= LIMIT); i++) {
					// if(ZiN + ZrN <= LIMIT){
					j = i;

					Zi = 2.0f * Zr * Zi + Ci;
					Zr = ZrN - ZiN + Cr;
					ZiN = Zi * Zi;
					ZrN = Zr * Zr;

					// }

				}

				rgb[gid] = (short) ((j * 255) / iterations);
			}

		};

		int size = this.size;
		kernel.execute(size * size);
		kernel.dispose();
	}

	@After
	public void compareResult() {
		if(!compareLogEnable) return;
		printArray(dataCPU, "Java", height * width);
		printArray(dataGPU, "Rubus", height * width);
		printArray(dataAparAPI, "AparAPI", height * width);

	}

}
