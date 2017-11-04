import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice.QueueProperties;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import javax.imageio.ImageIO;
import org.bridj.Pointer;

public class Mandelbrot
{
  static
  {
    width = 18000;height = 18000;
  }
  
  private static final String kernel_112397371 = " static int Static_Mandelbrot_iterations;                                                                                    static float Static_Mandelbrot_spacing;                                                                                     static int Static_Mandelbrot_height;                                                                                        static short* Static_Mandelbrot_data;                                                                                       __kernel void kernel_112397371(const int limit0, const int limit1, int v1_INT, int v0_INT) {       int dim1 = get_global_id(1);                                  int dim0 = get_global_id(0);                                  v1_INT += 1 * dim0;                                           v0_INT += 1 * dim1;                                           if(v1_INT >= limit0) return;                                  if(v0_INT >= limit1) return;                                  float v7_FLOAT;                                               float v5_FLOAT;                                               float v2_FLOAT;                                               float v3_FLOAT;                                               float v6_FLOAT;                                               float v4_FLOAT;                                               int v8_INT;                                                   {                                                                   float t0 = 0.0f;                                             v2_FLOAT = t0;                                                }/*15*/                                                  {                                                                   float t1 = 0.0f;                                             v3_FLOAT = t1;                                                }/*16*/                                                  {                                                                   int t2 = v1_INT;                                              float t3 = Static_Mandelbrot_spacing;                         float t4 = (float) t2;                                        float t5 = t4*t3;                                             float t6 = 1.5f;                                              float t7 = t5-t6;                                            v4_FLOAT = t7;                                                }/*17*/                                                  {                                                                   int t8 = v0_INT;                                              float t9 = Static_Mandelbrot_spacing;                         float t10 = (float) t8;                                       float t11 = t10*t9;                                           float t12 = 1.0f;                                             float t13 = t11-t12;                                         v5_FLOAT = t13;                                               }/*18*/                                                  {                                                                   float t14 = 0.0f;                                            v6_FLOAT = t14;                                               }/*19*/                                                  {                                                                   float t15 = 0.0f;                                            v7_FLOAT = t15;                                               }/*20*/                                                  {                                                                   int t16 = 0;                                                 v8_INT = t16;                                                 }/*21*/                                                  {                                                                  }/*22*/                                                  {                                                                   int t17 = v8_INT;                                             int t18 = Static_Mandelbrot_iterations;                      if(t17<t18) { {                                                          float t19 = v7_FLOAT;                                         float t20 = v6_FLOAT;                                         float t21 = t19+t20;                                          float t22 = 4.0f;                                             int t23 = (t21<t22) ? -1 : (t21>t22) ? 1 : 0;                       int t24 = 0;                                                 if(t23>t24) { {                                                         }/*31*/                                                  }/* end if */                                            {                                                                   float t25 = v2_FLOAT;                                         float t26 = v3_FLOAT;                                         float t27 = v5_FLOAT;                                         float t28 = 2.0f;                                             float t29 = t28*t25;                                          float t30 = t29*t26;                                          float t31 = t30+t27;                                         v3_FLOAT = t31;                                               }/*24*/                                                  {                                                                   float t32 = v6_FLOAT;                                         float t33 = v7_FLOAT;                                         float t34 = v4_FLOAT;                                         float t35 = t32-t33;                                          float t36 = t35+t34;                                         v2_FLOAT = t36;                                               }/*25*/                                                  {                                                                   float t37 = v3_FLOAT;                                         float t38 = v3_FLOAT;                                         float t39 = t37*t38;                                         v7_FLOAT = t39;                                               }/*26*/                                                  {                                                                   float t40 = v2_FLOAT;                                         float t41 = v2_FLOAT;                                         float t42 = t40*t41;                                         v6_FLOAT = t42;                                               }/*27*/                                                  {                                                                  v8_INT += 1;                                                  }/*28*/                                                  }/*30*/                                                  }/* end if */                                            {                                                                  __global short* t43 = Static_Mandelbrot_data;                  int t44 = v0_INT;                                             int t45 = Static_Mandelbrot_height;                           int t46 = v1_INT;                                             int t47 = v8_INT;                                             int t48 = Static_Mandelbrot_iterations;                       int t49 = t44*t45;                                            int t50 = t49+t46;                                            short t51 = 255;                                              int t52 = t47*t51;                                            int t53 = t52/t48;                                            short t54 = (short) t53;                                     t43[t50] = t54;                                               }/*29*/                                                  {                                                                  v1_INT += 1;                                                  }/*32*/                                                  {                                                                  }/*54*/                                                  }/*23*/                                                  }";
  private static CLContext context = JavaCL.createBestContext();
  static int iterations = 250;
  static float spacing = 4.0F;
  static int width;
  static final float LIMIT = 4.0F;
  static int height;
  static short[] data;
  
  public static void compute()
  {
    data = new short[height * width];
    int i = 0;int j = 0;kernel_112397371(width, height, iterations, spacing, j, i, height, data);
  }
  
  public void output(File paramFile)
  {
    BufferedImage localBufferedImage = new BufferedImage(width, height, 10);
    WritableRaster localWritableRaster = localBufferedImage.getRaster();
    
    int i = 0;
    if (i >= height) {
      ImageIO.write(localBufferedImage, "png", paramFile);
    } else {
      for (int j = 0;; j++)
      {
        if (j >= width)
        {
          i++; break;
        }
        localWritableRaster.setSample(j, i, 0, data[(i * height + j)]);
      }
    }
  }
  
  public static void kernel_112397371(int paramInt1, int paramInt2, int paramInt3, float paramFloat, int paramInt4, int paramInt5, int paramInt6, short[] paramArrayOfShort)
  {
    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
    String[] arrayOfString = { kernel_112397371 };
    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
    CLKernel localCLKernel = arrayOfCLKernel[0];
    int i = paramInt3;
    float f = paramFloat;
    int j = paramInt4;
    int k = paramInt5;
    int m = paramInt6;
    CLBuffer localCLBuffer = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToShorts(paramArrayOfShort), true);
    localCLKernel.setArgs(new Object[] { paramInt1, paramInt2, i, f, j, k, m, localCLBuffer });
    int n = paramInt1 - paramInt4 / 1;
    int i1 = paramInt2 - paramInt5 / 1;
    CLEvent[] arrayOfCLEvent = { null };
    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { n, i1 }, arrayOfCLEvent);
    localCLQueue.finish();
    localCLBuffer.read(localCLQueue, new CLEvent[] { localCLEvent }).getShorts(paramArrayOfShort);
  }
}
