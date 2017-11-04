import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice.QueueProperties;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import org.bridj.Pointer;

public class NBody
{
  private static final String kernel_1983995535 = " __kernel void kernel_1983995535(const int limit0, float v13_FLOAT, float v10_FLOAT, float v3_FLOAT, float v11_FLOAT, float v9_FLOAT, float v4_FLOAT, float v12_FLOAT, int v15_INT, float v14_FLOAT, __global float* v1_2891) {       int dim0 = get_global_id(0);                                  v15_INT += 3 * dim0;                                          if(v15_INT >= limit0) return;                                 float v19_FLOAT;                                              float v16_FLOAT;                                              float v20_FLOAT;                                              float v18_FLOAT;                                              float v17_FLOAT;                                              {                                                                  __global float* t0 = v1_2891;                                  int t1 = v15_INT;                                             int t2 = 0;                                                   int t3 = t1+t2;                                               float t4 = t0[t3];                                            float t5 = v12_FLOAT;                                         float t6 = t4-t5;                                            v16_FLOAT = t6;                                               }/*46*/                                                  {                                                                  __global float* t7 = v1_2891;                                  int t8 = v15_INT;                                             int t9 = 1;                                                   int t10 = t8+t9;                                              float t11 = t7[t10];                                          float t12 = v13_FLOAT;                                        float t13 = t11-t12;                                         v17_FLOAT = t13;                                              }/*47*/                                                  {                                                                  __global float* t14 = v1_2891;                                 int t15 = v15_INT;                                            int t16 = 2;                                                  int t17 = t15+t16;                                            float t18 = t14[t17];                                         float t19 = v14_FLOAT;                                        float t20 = t18-t19;                                         v18_FLOAT = t20;                                              }/*48*/                                                  {                                                                   float t21 = v16_FLOAT;                                        float t22 = v16_FLOAT;                                        float t23 = v17_FLOAT;                                        float t24 = v17_FLOAT;                                        float t25 = v18_FLOAT;                                        float t26 = v18_FLOAT;                                        float t27 = v3_FLOAT;                                         float t28 = t21*t22;                                          float t29 = t23*t24;                                          float t30 = t28+t29;                                          float t31 = t25*t26;                                          float t32 = t30+t31;                                          float t33 = t32+t27;                                          float t34 = (float) t33;                                      float t35 = sqrt(t34);                                        float t36 = 1.0f;                                             float t37 = (float) t35;                                      float t38 = t36/t37;                                         v19_FLOAT = t38;                                              }/*49*/                                                  {                                                                   float t39 = v4_FLOAT;                                         float t40 = v19_FLOAT;                                        float t41 = v19_FLOAT;                                        float t42 = v19_FLOAT;                                        float t43 = t39*t40;                                          float t44 = t43*t41;                                          float t45 = t44*t42;                                         v20_FLOAT = t45;                                              }/*50*/                                                  {                                                                   float t46 = v9_FLOAT;                                         float t47 = v20_FLOAT;                                        float t48 = v16_FLOAT;                                        float t49 = t47*t48;                                          float t50 = t46+t49;                                         v9_FLOAT = t50;                                               }/*51*/                                                  {                                                                   float t51 = v10_FLOAT;                                        float t52 = v20_FLOAT;                                        float t53 = v17_FLOAT;                                        float t54 = t52*t53;                                          float t55 = t51+t54;                                         v10_FLOAT = t55;                                              }/*52*/                                                  {                                                                   float t56 = v11_FLOAT;                                        float t57 = v20_FLOAT;                                        float t58 = v18_FLOAT;                                        float t59 = t57*t58;                                          float t60 = t56+t59;                                         v11_FLOAT = t60;                                              }/*53*/                                                  {                                                                  v15_INT += 3;                                                 }/*54*/                                                  {                                                                  }/*69*/                                                  }";
  private static CLContext context = JavaCL.createBestContext();
  
  public static void kernel_1983995535(int paramInt1, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, int paramInt2, float paramFloat8, float[] paramArrayOfFloat)
  {
    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
    String[] arrayOfString = { kernel_1983995535 };
    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
    CLKernel localCLKernel = arrayOfCLKernel[0];
    float f1 = paramFloat1;
    float f2 = paramFloat2;
    float f3 = paramFloat3;
    float f4 = paramFloat4;
    float f5 = paramFloat5;
    float f6 = paramFloat6;
    float f7 = paramFloat7;
    int i = paramInt2;
    float f8 = paramFloat8;
    CLBuffer localCLBuffer = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToFloats(paramArrayOfFloat), true);
    localCLKernel.setArgs(new Object[] { paramInt1, f1, f2, f3, f4, f5, f6, f7, i, f8, localCLBuffer });
    int j = paramInt1 - paramInt2 / 3;
    CLEvent[] arrayOfCLEvent = { null };
    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { j }, arrayOfCLEvent);
    localCLQueue.finish();
  }
  
  public static void compute()
  {
    float f1 = 0.005F;
    float f2 = 1.0F;
    float f3 = 5.0F;
    int i = 9999999;
    int j = i;
    float[] arrayOfFloat1 = new float[j * 3];
    float[] arrayOfFloat2 = new float[j * 3];
    
    float f4 = 20.0F;
    int k = 0;
    if (k >= j * 3)
    {
      compute(i, arrayOfFloat1, f1, f2, f3, arrayOfFloat2);
    }
    else
    {
      float f5 = (float)(Math.random() * 3.141592653589793D * 2.0D);
      float f6 = (float)(Math.random() * 3.141592653589793D * 2.0D);
      float f7 = (float)(Math.random() * 20.0D);
      
      arrayOfFloat1[(k + 0)] = ((float)(f7 * Math.cos(f5) * Math.sin(f6)));
      arrayOfFloat1[(k + 1)] = ((float)(f7 * Math.sin(f5) * Math.sin(f6)));
      arrayOfFloat1[(k + 2)] = ((float)(f7 * Math.cos(f6)));
      if (k % 2 == 0)
      {
        int tmp174_173 = (k + 0);arrayOfFloat1[tmp174_173] = ((float)(arrayOfFloat1[tmp174_173] + 30.0D));
      }
      for (;;)
      {
        k += 3; break; int 
        
          tmp194_193 = (k + 0);arrayOfFloat1[tmp194_193] = ((float)(arrayOfFloat1[tmp194_193] - 30.0D));
      }
    }
  }
  
  public static void compute(int paramInt, float[] paramArrayOfFloat1, float paramFloat1, float paramFloat2, float paramFloat3, float[] paramArrayOfFloat2)
  {
    int i = paramInt * 3;
    for (int j = 0; j < paramInt; j++)
    {
      int k = j * 3;
      
      float f1 = 0.0F;
      float f2 = 0.0F;
      float f3 = 0.0F;
      
      float f4 = paramArrayOfFloat1[(k + 0)];
      float f5 = paramArrayOfFloat1[(k + 1)];
      float f6 = paramArrayOfFloat1[(k + 2)];
      int m = 0;kernel_1983995535(i, f5, f2, paramFloat2, f3, f1, paramFloat3, f4, m, f6, paramArrayOfFloat1);
      
      f1 *= paramFloat1;
      f2 *= paramFloat1;
      f3 *= paramFloat1;
      paramArrayOfFloat1[(k + 0)] = (f4 + paramArrayOfFloat2[(k + 0)] * paramFloat1 + f1 * 0.5F * paramFloat1);
      paramArrayOfFloat1[(k + 1)] = (f5 + paramArrayOfFloat2[(k + 1)] * paramFloat1 + f2 * 0.5F * paramFloat1);
      paramArrayOfFloat1[(k + 2)] = (f6 + paramArrayOfFloat2[(k + 2)] * paramFloat1 + f3 * 0.5F * paramFloat1);
      
      paramArrayOfFloat2[(k + 0)] += f1;
      paramArrayOfFloat2[(k + 1)] += f2;
      paramArrayOfFloat2[(k + 2)] += f3;
    }
  }
}
