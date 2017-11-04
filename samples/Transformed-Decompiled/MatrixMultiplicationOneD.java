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

public class MatrixMultiplicationOneD
{
  private static final String kernel_539791456 = " __kernel void kernel_539791456(const int limit0, int v7_INT, int v5_INT, int v2_INT, __global float* v0_2891, float v6_FLOAT, __global float* v1_2891, int v4_INT) {       int dim0 = get_global_id(0);                                  v7_INT += 1 * dim0;                                           if(v7_INT >= limit0) return;                                  {                                                                   float t0 = v6_FLOAT;                                         __global float* t1 = v0_2891;                                  int t2 = v4_INT;                                              int t3 = v2_INT;                                              int t4 = v7_INT;                                              int t5 = t2*t3;                                               int t6 = t5+t4;                                               float t7 = t1[t6];                                           __global float* t8 = v1_2891;                                  int t9 = v7_INT;                                              int t10 = v2_INT;                                             int t11 = v5_INT;                                             int t12 = t9*t10;                                             int t13 = t12+t11;                                            float t14 = t8[t13];                                          float t15 = t7*t14;                                           float t16 = t0+t15;                                          v6_FLOAT = t16;                                               }/*15*/                                                  {                                                                  v7_INT += 1;                                                  }/*16*/                                                  {                                                                  }/*29*/                                                  }";
  private static CLContext context = JavaCL.createBestContext();
  
  public static float[] multiplyMatrices(float[] paramArrayOfFloat1, float[] paramArrayOfFloat2, int paramInt)
  {
    float[] arrayOfFloat = new float[paramInt * paramInt];
    for (int i = 0; i < paramInt; i++) {
      for (int j = 0; j < paramInt; j++)
      {
        float f = 0.0F;
        int k = 0;kernel_539791456(paramInt, k, j, paramInt, paramArrayOfFloat1, f, paramArrayOfFloat2, i);
        
        arrayOfFloat[(i * paramInt + j)] = f;
      }
    }
    return arrayOfFloat;
  }
  
  public static void kernel_539791456(int paramInt1, int paramInt2, int paramInt3, int paramInt4, float[] paramArrayOfFloat1, float paramFloat, float[] paramArrayOfFloat2, int paramInt5)
  {
    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
    String[] arrayOfString = { kernel_539791456 };
    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
    CLKernel localCLKernel = arrayOfCLKernel[0];
    int i = paramInt2;
    int j = paramInt3;
    int k = paramInt4;
    CLBuffer localCLBuffer1 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToFloats(paramArrayOfFloat1), true);
    float f = paramFloat;
    CLBuffer localCLBuffer2 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToFloats(paramArrayOfFloat2), true);
    int m = paramInt5;
    localCLKernel.setArgs(new Object[] { paramInt1, i, j, k, localCLBuffer1, f, localCLBuffer2, m });
    int n = paramInt1 - paramInt2 / 1;
    CLEvent[] arrayOfCLEvent = { null };
    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { n }, arrayOfCLEvent);
    localCLQueue.finish();
  }
}
