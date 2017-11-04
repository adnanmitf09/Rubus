package javacl;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.util.IOUtils;

public class JavaCLUtil {

	public static void setup() {
		// Create a context and program using the devices discovered.
		CLContext context = createBestContext();
		CLQueue queue = context.createDefaultQueue();

	}

	public static String readKerne(String filePath)
			 {
		try {
			return IOUtils.readTextClose(new FileInputStream(new File(filePath)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
	}

	private static long buildAndExecuteKernel(CLQueue queue, float realMin,
			float imaginaryMin, int realResolution, int imaginaryResolution,
			int maxIter, int magicNumber, float deltaReal,
			float deltaImaginary, Pointer<Integer> results, String src)
			throws CLBuildException, IOException {

		CLContext context = queue.getContext();
		long startTime = System.nanoTime();
//		if (useAutoGenWrapper) {
//			Mandelbrot mandelbrot = new Mandelbrot(context);
//			mandelbrot.mandelbrot(queue, new float[] { deltaReal,
//					deltaImaginary }, new float[] { realMin, imaginaryMin },
//
//			maxIter, magicNumber, realResolution,
//					context.createBuffer(CLMem.Usage.Output, results, false),
//
//					new int[] { realResolution, imaginaryResolution },
//					new int[] { 1, 1 });
//		} else {
			CLProgram program = context.createProgram(src).build();

			// Create a kernel instance from the mandelbrot kernel, passing in
			// parameters.
			CLKernel kernel = program.createKernel("mandelbrot", new float[] {
					deltaReal, deltaImaginary }, new float[] { realMin,
					imaginaryMin },

			maxIter, magicNumber, realResolution,
					context.createBuffer(CLMem.Usage.Output, results, false));

			// Enqueue and complete work using a 2D range of work groups
			// corrsponding to individual pizels in the set.
			// The work groups are 1x1 in size and their range is defined by the
			// desired resolution. This corresponds
			// to one device thread per pixel.

			kernel.enqueueNDRange(queue, new int[] { realResolution,
					imaginaryResolution }, new int[] { 1, 1 });
//		}
		queue.finish();
		long time = System.nanoTime() - startTime;
		return time;

	}

}
