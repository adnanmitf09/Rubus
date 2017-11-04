package cl;

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

public class JavaCLHelper {

	public static void initialize(){
	/*
		        CLContext context = JavaCL.createBestContext();
		        CLQueue queue = context.createDefaultQueue();
		        ByteOrder byteOrder = context.getByteOrder();
		        
		        int n = 1024;
		        Pointer<Float>
		            aPtr = allocateFloats(n).order(byteOrder),
		            bPtr = allocateFloats(n).order(byteOrder);

		        for (int i = 0; i < n; i++) {
		            aPtr.set(i, (float)cos(i));
		            bPtr.set(i, (float)sin(i));
		        }

		        // Create OpenCL input buffers (using the native memory pointers aPtr and bPtr) :
		        CLBuffer<Float> 
		            a = context.createBuffer(Usage.Input, aPtr),
		            b = context.createBuffer(Usage.Input, bPtr);

		        // Create an OpenCL output buffer :
		        CLBuffer<Float> out = context.createBuffer(Usage.Output, n);

		        // Read the program sources and compile them :
		        String src = IOUtils.readText(JavaCLTutorial1.class.getResource("TutorialKernels.cl"));
		        CLProgram program = context.createProgram(src);

		        // Get and call the kernel :
		        CLKernel addFloatsKernel = program.createKernel("add_floats");
		        addFloatsKernel.setArgs(a, b, out, n);
		        CLEvent addEvt = addFloatsKernel.enqueueNDRange(queue, new int[] { n });
		        
		        Pointer<Float> outPtr = out.read(queue, addEvt); // blocks until add_floats finished

		        // Print the first 10 output values :
		        for (int i = 0; i < 10 && i < n; i++)
		            System.out.println("out[" + i + "] = " + outPtr.get(i));
		        
		    }
		}*/
		
	}
	
	
}
