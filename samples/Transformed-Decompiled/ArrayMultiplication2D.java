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

public class ArrayMultiplication2D
{
  private static final String kernel_M382277171 = " __kernel void kernel_M382277171(const int limit0, const int limit1, __global int* v2_90345, int v4_INT, int v3_INT, __global int* v1_90345, __global int* v0_90345) {       int dim1 = get_global_id(1);                                  int dim0 = get_global_id(0);                                  v4_INT += 1 * dim0;                                           v3_INT += 1 * dim1;                                           if(v4_INT >= limit0) return;                                  if(v3_INT >= limit1) return;                                  {                                                                  __global int* t0 = v2_90345;                                   int t1 = v3_INT;                                             __global int* t2 = t0[t1];                                     int t3 = v4_INT;                                             __global int* t4 = v0_90345;                                   int t5 = v3_INT;                                             __global int* t6 = t4[t5];                                     int t7 = v4_INT;                                              int t8 = t6[t7];                                             __global int* t9 = v1_90345;                                   int t10 = v3_INT;                                            __global int* t11 = t9[t10];                                   int t12 = v4_INT;                                             int t13 = t11[t12];                                           int t14 = t8*t13;                                            t2[t3] = t14;                                                 }/*33*/                                                  {                                                                  v4_INT += 1;                                                  }/*34*/                                                  {                                                                  }/*42*/                                                  }";
  private static CLContext context = JavaCL.createBestContext();
  
  private static int[][] multi(int[][] paramArrayOfInt1, int[][] paramArrayOfInt2, int[][] paramArrayOfInt3)
  {
    int i = 0;int j = 0;kernel_M382277171(paramArrayOfInt1[i].length, paramArrayOfInt1.length, paramArrayOfInt3, j, i, paramArrayOfInt2, paramArrayOfInt1);
    
    return paramArrayOfInt3;
  }
  
  public static void kernel_M382277171(int paramInt1, int paramInt2, int[][] paramArrayOfInt1, int paramInt3, int paramInt4, int[][] paramArrayOfInt2, int[][] paramArrayOfInt3)
  {
    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
    String[] arrayOfString = { kernel_M382277171 };
    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
    CLKernel localCLKernel = arrayOfCLKernel[0];
    CLBuffer localCLBuffer1 = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToInts(paramArrayOfInt1), true);
    int i = paramInt3;
    int j = paramInt4;
    CLBuffer localCLBuffer2 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToInts(paramArrayOfInt2), true);
    CLBuffer localCLBuffer3 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToInts(paramArrayOfInt3), true);
    localCLKernel.setArgs(new Object[] { paramInt1, paramInt2, localCLBuffer1, i, j, localCLBuffer2, localCLBuffer3 });
    int k = paramInt1 - paramInt3 / 1;
    int m = paramInt2 - paramInt4 / 1;
    CLEvent[] arrayOfCLEvent = { null };
    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { k, m }, arrayOfCLEvent);
    localCLQueue.finish();
    Pointer localPointer1 = localCLBuffer1.read(localCLQueue, new CLEvent[] { localCLEvent });
    long l = localPointer1.getValidElements();
    for (int n = 0; n < l; n++)
    {
      Pointer localPointer2 = (Pointer)localPointer1.get(n);
      localPointer2.getInts(paramArrayOfInt1[n]);
    }
  }
}
