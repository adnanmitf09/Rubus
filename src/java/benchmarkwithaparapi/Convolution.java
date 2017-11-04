package benchmarkwithaparapi;

/*
 Copyright (c) 2010-2011, Advanced Micro Devices, Inc.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 following conditions are met:

 Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 disclaimer. 

 Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 disclaimer in the documentation and/or other materials provided with the distribution. 

 Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 If you use the software (in whole or in part), you shall adhere to all applicable U.S., European, and other export
 laws, including but not limited to the U.S. Export Administration Regulations ("EAR"), (15 C.F.R. Sections 730 through
 774), and E.U. Council Regulation (EC) No 1334/2000 of 22 June 2000.  Further, pursuant to Section 740.6 of the EAR,
 you hereby certify that, except pursuant to a license granted by the United States Department of Commerce Bureau of 
 Industry and Security or as otherwise permitted pursuant to a License Exception under the U.S. Export Administration 
 Regulations ("EAR"), you will not (1) export, re-export or release to a national of a country in Country Groups D:1,
 E:1 or E:2 any restricted technology, software, or source code you receive hereunder, or (2) export to Country Groups
 D:1, E:1 or E:2 the direct product of such technology or software, if such foreign produced direct product is subject
 to national security controls as identified on the Commerce Control List (currently found in Supplement 1 to Part 774
 of EAR).  For the most current Country Group listings, or for additional information about the EAR or your obligations
 under those regulations, please refer to the U.S. Bureau of Industry and Security's website at http://www.bis.doc.gov/. 

 */

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.bridj.Pointer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import annotation.Transform;
import benchmark.RubusBenchmark;

import com.amd.aparapi.Kernel;
import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

public class Convolution extends RubusBenchmark {

	static String imgFilePath = "testCard.jpg";

	static int height, width;
	static float convMatrix3x3[] = new float[] { 0f, -10f, 0f, -10f, 40f, -10f, 0f, -10f, 0f, };
	private static byte[] imageOutCPU;
	private static byte[] imageOutGPU;
	private static byte[] imageIn;
	private static BufferedImage outputImageCPU;
	private static BufferedImage outputImageGPU;
	private static BufferedImage outputImageApar;
	private static byte[] imageOutApar;

	@Before
	public void prepare() {
		try {

			File _file = new File(imgFilePath);
			BufferedImage inputImage = ImageIO.read(_file);
			inputImage = resizeImageWithHint(inputImage, inputImage.getType(), size, size);
			height = inputImage.getHeight();
			width = inputImage.getWidth();

			outputImageCPU = new BufferedImage(width, height, inputImage.getType());
			outputImageGPU = new BufferedImage(width, height, inputImage.getType());
			outputImageApar = new BufferedImage(width, height, inputImage.getType());

			imageIn = ((DataBufferByte) inputImage.getRaster().getDataBuffer()).getData();
			imageOutCPU = ((DataBufferByte) outputImageCPU.getRaster().getDataBuffer()).getData();
			imageOutGPU = ((DataBufferByte) outputImageGPU.getRaster().getDataBuffer()).getData();
			imageOutApar = ((DataBufferByte) outputImageApar.getRaster().getDataBuffer()).getData();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@After
	public void compareresults(){
		if(!compareLogEnable) return;

	try {// save image to file
	//	ImageIO.write(outputImageApar, "JPEG", new File("conv.jpg"));
	} catch (Exception e) {
		e.printStackTrace();
	}
	}
	@Override
	@Test
	public void Java() {

		compute(width, height, imageIn, imageOutCPU, convMatrix3x3);

	}

	@Override
	@Test
	public void Rubus() {
		computeGPU(width, height, imageIn, imageOutGPU, convMatrix3x3);

	}

	@Override
	@Test
	public void AparAPI() {
		final float convMatrix3x3[] = this.convMatrix3x3;
		final int height = this.height;
		final int width = this.width;
		final byte imageIn[] = this.imageIn;
		final byte imageOut[] = this.imageOutApar;

		Kernel kernel = new Kernel() {
     	public void processPixel(int x, int y, int w, int h) {
				float accum = 0f;
				int count = 0;
				for (int dx = -3; dx < 6; dx += 3) {
					for (int dy = -1; dy < 2; dy += 1) {
						final int rgb = 0xff & imageIn[((y + dy) * w) + (x + dx)];

						accum += rgb * convMatrix3x3[count++];
					}
				}
				final byte value = (byte) (max(0, min((int) accum, 255)));
				imageOut[(y * w) + x] = value;

			}

			@Override
			public void run() {
				final int x = getGlobalId(0) % (width * 3);
				final int y = getGlobalId(0) / (width * 3);

				if ((x > 3) && (x < ((width * 3) - 3)) && (y > 1) && (y < (height - 1))) {
					processPixel(x, y, width * 3, height);
				}

			}

		};
		int factor = 3;
		int n = factor * width * height;
		kernel.execute(n);
		kernel.dispose();

	}

	public static int checkbound(int x, int y, int width, int height) {
		int factor = 3;
		if (x > factor && x < (width * factor - factor) && y > 1 && y < (height - 1))
			return 1;
		return 0;
	}

	public static void processPixel(int x, int y, int w, int h, byte[] imageIn, byte[] imageOut, float convMatrix3x3[]) {
		float accum = 0f;
		int count = 0;
		int st = -3, ed = 6, st2 = -1, ed2 = 2;
		for (int dx = st; dx < ed; dx += 3) {
			for (int dy = st2; dy < ed2; dy++) {
				byte px = imageIn[((y + dy) * w) + (x + dx)];
				int rgb = 0xff & px;
				float weight = convMatrix3x3[count++];
				accum = accum + (rgb * weight);
			}
		}
		int colorRange = 255;
		byte value = (byte) (Math.max(0, Math.min((int) accum, colorRange)));
		imageOut[y * w + x] = value;

	}

	@Transform(loops = "i")
	public static void compute(int width, int height, byte[] imageIn, byte[] imageOut, float[] convMatrix3x32) {
		int factor = 3;
		int n = factor * width * height;
		int w = width * factor;
		int success = 1;

		for (int i = 0; i < n; i++) {

			int x = i % (width * factor);
			int y = i / (width * factor);

			int b = checkbound(x, y, width, height);
			if (b == success)
				processPixel(x, y, w, height, imageIn, imageOut, convMatrix3x32);
		}
	}

	private static CLContext context = JavaCL.createBestContext();

	public static void computeGPU(int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, float[] paramArrayOfFloat) {
		int i = 3;
		int j = i * paramInt1 * paramInt2;
		int k = paramInt1 * i;
		int m = 1;

		int n = 0;
		kernel_14309029(j, k, paramInt2, n, paramArrayOfByte2, i, paramInt1, m, paramArrayOfFloat, paramArrayOfByte1);
	}

	public static void kernel_14309029(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, int paramInt5, int paramInt6, int paramInt7, float[] paramArrayOfFloat, byte[] paramArrayOfByte2) {
		CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
		CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
		String[] arrayOfString = { kernel_14309029 };
		CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
		CLKernel localCLKernel = arrayOfCLKernel[0];
		int i = paramInt2;
		int j = paramInt3;
		int k = paramInt4;
		CLBuffer localCLBuffer1 = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToBytes(paramArrayOfByte1), true);
		int m = paramInt5;
		int n = paramInt6;
		int i1 = paramInt7;
		CLBuffer localCLBuffer2 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToFloats(paramArrayOfFloat), true);
		CLBuffer localCLBuffer3 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToBytes(paramArrayOfByte2), true);
		localCLKernel.setArgs(new Object[] { paramInt1, i, j, k, localCLBuffer1, m, n, i1, localCLBuffer2, localCLBuffer3 });
		int i2 = paramInt1 - paramInt4 / 1;
		CLEvent[] arrayOfCLEvent = { null };
		CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { i2 }, arrayOfCLEvent);
		localCLQueue.finish();
		localCLBuffer1.read(localCLQueue, new CLEvent[] { localCLEvent }).getBytes(paramArrayOfByte1);
	}

	private static final String kernel_14309029 = "" + " static void processPixel(int v0_INT, int v1_INT, int v2_INT, int v3_INT, __global char* v4_2887, __global char* v5_2887, __global float* v6_2891) { " + "      int v14_INT;                                            " + "      int v10_INT;                                            " + "      int v9_INT;                                             " + "      int v13_INT;                                            " + "      int v17_INT;                                            " + "      int v8_INT;                                             " + "      int v15_INT;                                            " + "      int v16_INT;                                            "
			+ "      int v11_INT;                                            " + "      float v7_FLOAT;                                         " + "      int v12_INT;                                            " + "      float v18_FLOAT;                                        " + "      {                                                       " + "           }/*12*/                                            " + "      {                                                       " + "            float t41 = 0.0f;                                 " + "           v7_FLOAT = t41;                                    " + "           }/*13*/                                            " + "      {                                                       "
			+ "            int t42 = 0;                                      " + "           v8_INT = t42;                                      " + "           }/*14*/                                            " + "      {                                                       " + "            char t43 = -3;                                    " + "           v9_INT = t43;                                      " + "           }/*15*/                                            " + "      {                                                       " + "            char t44 = 6;                                     " + "           v10_INT = t44;                                     " + "           }/*16*/                                            "
			+ "      {                                                       " + "            int t45 = 3;                                      " + "           v11_INT = t45;                                     " + "           }/*17*/                                            " + "      {                                                       " + "            int t46 = -1;                                     " + "           v12_INT = t46;                                     " + "           }/*18*/                                            " + "      {                                                       " + "            int t47 = 2;                                      " + "           v13_INT = t47;                                     "
			+ "           }/*19*/                                            " + "      {                                                       " + "            int t48 = v9_INT;                                 " + "           v14_INT = t48;                                     " + "           }/*20*/                                            " + "      {                                                       " + "           }/*21*/                                            " + "      {                                                       " + "            int t49 = v10_INT;                                " + "           while(v14_INT < t49) {                             " + "                {                                             "
			+ "                      int t50 = v12_INT;                      " + "                     v15_INT = t50;                           " + "                     }/*23*/                                  " + "                {                                             " + "                     }/*24*/                                  " + "                {                                             " + "                      int t51 = v13_INT;                      " + "                     while(v15_INT < t51) {                   " + "                          {                                   " + "                               __global char* t52 = v4_2887;  " + "                                int t53 = v1_INT;             "
			+ "                                int t54 = v15_INT;            " + "                                int t55 = v2_INT;             " + "                                int t56 = v0_INT;             " + "                                int t57 = v14_INT;            " + "                                int t58 = t53+t54;            " + "                                int t59 = t58*t55;            " + "                                int t60 = t56+t57;            " + "                                int t61 = t59+t60;            " + "                                char t62 = t52[t61];          " + "                               v16_INT = t62;                 " + "                               }/*26*/                        "
			+ "                          {                                   " + "                                int t63 = v16_INT;            " + "                                int t64 = 255;              " + "                                int t65 = t64&t63;            " + "                               v17_INT = t65;                 " + "                               }/*27*/                        " + "                          {                                   " + "                               __global float* t66 = v6_2891; " + "                                int t67 = v8_INT;             " + "                               v8_INT += 1;                   " + "                                float t68 = t66[t67];         "
			+ "                               v18_FLOAT = t68;               " + "                               }/*28*/                        " + "                          {                                   " + "                                float t69 = v7_FLOAT;         " + "                                int t70 = v17_INT;            " + "                                float t71 = v18_FLOAT;        " + "                                float t72 = (float) t70;      " + "                                float t73 = t72*t71;          " + "                                float t74 = t69+t73;          " + "                               v7_FLOAT = t74;                " + "                               }/*29*/                        "
			+ "                          {                                   " + "                               v15_INT += 1;                  " + "                               }/*30*/                        " + "                          {                                   " + "                               }/*56*/                        " + "                          }/*loop end*/                       " + "                     }/*61*/                                  " + "                {                                             " + "                     }/*31*/                                  " + "                {                                             " + "                     v14_INT += 3;                            "
			+ "                     }/*32*/                                  " + "                {                                             " + "                     }/*58*/                                  " + "                }/*loop end*/                                 " + "           }/*60*/                                            " + "      {                                                       " + "           }/*33*/                                            " + "      {                                                       " + "            float t75 = v7_FLOAT;                             " + "            int t76 = (int) t75;                              " + "            int t77 = 255;                                  "
			+ "            int t78 = min(t76, t77);                          " + "            int t79 = 0;                                      " + "            int t80 = max(t79, t78);                          " + "            char t81 = (char) t80;                            " + "           v14_INT = t81;                                     " + "           }/*34*/                                            " + "      {                                                       " + "           __global char* t82 = v5_2887;                      " + "            int t83 = v1_INT;                                 " + "            int t84 = v2_INT;                                 " + "            int t85 = v0_INT;                                 "
			+ "            int t86 = v14_INT;                                " + "            int t87 = t83*t84;                                " + "            int t88 = t87+t85;                                " + "           t82[t88] = t86;                                    " + "           }/*35*/                                            " + "      {                                                       " + "           return;                                            " + "           }/*36*/                                            " + "      }" + " static int checkbound(int v0_INT, int v1_INT, int v2_INT, int v3_INT) { " + "      int v4_INT;                                             " + "      {                                                       "
			+ "           }/*3*/                                             " + "      {                                                       " + "            int t14 = 3;                                      " + "           v4_INT = t14;                                      " + "           }/*4*/                                             " + "      {                                                       " + "            int t15 = v0_INT;                                 " + "            int t16 = v4_INT;                                 " + "           if(t15>t16) { {                                    " + "                      int t17 = v0_INT;                       " + "                      int t18 = v2_INT;                       "
			+ "                      int t19 = v4_INT;                       " + "                      int t20 = v4_INT;                       " + "                      int t21 = t18*t19;                      " + "                      int t22 = t21-t20;                      " + "                     if(t17<t22) { {                          " + "                                int t23 = v1_INT;             " + "                                int t24 = 1;                  " + "                               if(t23>t24) { {                " + "                                          int t25 = v1_INT;   " + "                                          int t26 = v3_INT;   " + "                                          int t27 = 1;        "
			+ "                                          int t28 = t26-t27;  " + "                                         if(t25<t28) { {      " + "                                                    int t29 = 1; " + "                                                   return t29; " + "                                                   }/*10*/    " + "                                              }/* end if */   " + "                                         {                    " + "                                               int t30 = 0;   " + "                                              return t30;     " + "                                              }/*6*/          " + "                                         }/*9*/               "
			+ "                                    }/* end if */             " + "                               }/*8*/                         " + "                          }/* end if */                       " + "                     }/*7*/                                   " + "                }/* end if */                                 " + "           }/*5*/                                             " + "      }" + " __kernel void kernel_14309029(const int limit0, int v7_INT, int v1_INT, int v9_INT, __global char* v3_2887, int v5_INT, int v0_INT, int v8_INT, __global float* v4_2891, __global char* v2_2887) { " + "      int dim0 = get_global_id(0);                            " + "      v9_INT += 1 * dim0;                                     "
			+ "      if(v9_INT >= limit0) return;                            " + "      int v10_INT;                                            " + "      int v11_INT;                                            " + "      int v12_INT;                                            " + "      {                                                       " + "            int t0 = v9_INT;                                  " + "            int t1 = v0_INT;                                  " + "            int t2 = v5_INT;                                  " + "            int t3 = t1*t2;                                   " + "            int t4 = t0%t3;                                   " + "           v10_INT = t4;                                      "
			+ "           }/*46*/                                            " + "      {                                                       " + "            int t5 = v9_INT;                                  " + "            int t6 = v0_INT;                                  " + "            int t7 = v5_INT;                                  " + "            int t8 = t6*t7;                                   " + "            int t9 = t5/t8;                                   " + "           v11_INT = t9;                                      " + "           }/*47*/                                            " + "      {                                                       " + "            int t10 = v10_INT;                                "
			+ "            int t11 = v11_INT;                                " + "            int t12 = v0_INT;                                 " + "            int t13 = v1_INT;                                 " + "            int t31 = checkbound(t10, t11, t12, t13);         " + "           v12_INT = t31;                                     " + "           }/*48*/                                            " + "      {                                                       " + "            int t32 = v12_INT;                                " + "            int t33 = v8_INT;                                 " + "           if(t32==t33) { {                                   " + "                     }/*51*/                                  "
			+ "                {                                             " + "                      int t34 = v10_INT;                      " + "                      int t35 = v11_INT;                      " + "                      int t36 = v7_INT;                       " + "                      int t37 = v1_INT;                       " + "                     __global char* t38 = v2_2887;            " + "                     __global char* t39 = v3_2887;            " + "                     __global float* t40 = v4_2891;           " + "                     processPixel(t34, t35, t36, t37, t38, t39, t40); " + "                     }/*52*/                                  " + "                }/* end if */                                 "
			+ "           {                                                  " + "                v9_INT += 1;                                  " + "                }/*50*/                                       " + "           {                                                  " + "                }/*62*/                                       " + "           }/*49*/                                            " + "      }";

	private static BufferedImage resizeImage(BufferedImage originalImage, int type, int IMG_HEIGHT, int IMG_WIDTH) {
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();

		return resizedImage;
	}

	private static BufferedImage resizeImageWithHint(BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT) {

		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		return resizedImage;
	}

	public static void main(String[] args) {
//		runBenchmark(Convolution.class, new int[] { 64, 128, 256, 512, 1024, 2048, 4096, 8192 });
		runBenchmark(Convolution.class, new int[] { 64});

	}

	public static void main1(final String[] _args) {
		// File file = new File(_args.length == 1 ? _args[0] : "testcard.jpg");

		try {
			File _file = new File("testcard.jpg");
			BufferedImage inputImage = ImageIO.read(_file);

			Image image = Toolkit.getDefaultToolkit().createImage("testcard.jpg");
			// inputImage = resizeImageWithHint(inputImage,
			// inputImage.getType(), 4096, 8192);

			int height = inputImage.getHeight();

			int width = inputImage.getWidth();

			BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());

			byte[] inBytes = ((DataBufferByte) inputImage.getRaster().getDataBuffer()).getData();
			byte[] outBytes = ((DataBufferByte) outputImage.getRaster().getDataBuffer()).getData();

			// new Convolution().computeGPU( width, height,inBytes,outBytes,
			// convMatrix3x3);

			// filter(inputImage, outputImage);

			// new Convolution().compute(width, height, inBytes, outBytes,
			// convMatrix3x3);
			new Convolution().computeGPU(width, height, inBytes, outBytes, convMatrix3x3);

			final JLabel imageLabel = new JLabel(new ImageIcon(outputImage));

			JFrame ff1 = new JFrame();
			ff1.getContentPane().add(imageLabel);
			ff1.setSize(500, 500);
			ff1.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}