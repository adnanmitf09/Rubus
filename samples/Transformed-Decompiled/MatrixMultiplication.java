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

public class MatrixMultiplication
{
  private static final String kernel_1711690764 = " __kernel void kernel_1711690764(const int limit0, const int limit1, int v6_INT, int v3_INT, int v7_INT, __global int* v1_90345, __global int* v5_90345, __global int* v0_90345) {       int dim1 = get_global_id(1);                                  int dim0 = get_global_id(0);                                  v7_INT += 1 * dim0;                                           v6_INT += 1 * dim1;                                           if(v7_INT >= limit0) return;                                  if(v6_INT >= limit1) return;                                  int v8_INT;                                                   {                                                                   int t0 = 0;                                                  v8_INT = t0;                                                  }/*49*/                                                  {                                                                  }/*50*/                                                  {                                                                   int t1 = v3_INT;                                             while(v8_INT < t1) {                                               {                                                                  __global int* t2 = v5_90345;                                   int t3 = v6_INT;                                             __global int* t4 = t2[t3];                                     int t5 = v7_INT;                                             __global int* t6 = v5_90345;                                   int t7 = v6_INT;                                             __global int* t8 = t6[t7];                                     int t9 = v7_INT;                                              int t10 = t8[t9];                                            __global int* t11 = v0_90345;                                  int t12 = v6_INT;                                            __global int* t13 = t11[t12];                                  int t14 = v8_INT;                                             int t15 = t13[t14];                                          __global int* t16 = v1_90345;                                  int t17 = v8_INT;                                            __global int* t18 = t16[t17];                                  int t19 = v7_INT;                                             int t20 = t18[t19];                                           int t21 = t15*t20;                                            int t22 = t10+t21;                                           t4[t5] = t22;                                                 }/*52*/                                                  {                                                                  v8_INT += 1;                                                  }/*53*/                                                  {                                                                  }/*63*/                                                  }/*loop end*/                                            }/*68*/                                                  {                                                                  }/*54*/                                                  {                                                                  v7_INT += 1;                                                  }/*55*/                                                  {                                                                  }/*65*/                                                  }";
  private static CLContext context = JavaCL.createBestContext();
  
  public static int[][] multiply(int[][] paramArrayOfInt1, int[][] paramArrayOfInt2)
  {
    int i = paramArrayOfInt1.length;
    int j = paramArrayOfInt1[0].length;
    int k = paramArrayOfInt2.length;
    int[][] arrayOfInt = new int[i][k];
    int m = 0;int n = 0;kernel_1711690764(k, i, m, j, n, paramArrayOfInt2, arrayOfInt, paramArrayOfInt1);
    
    return arrayOfInt;
  }
  
  public static void kernel_1711690764(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int[][] paramArrayOfInt1, int[][] paramArrayOfInt2, int[][] paramArrayOfInt3)
  {
    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
    String[] arrayOfString = { kernel_1711690764 };
    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
    CLKernel localCLKernel = arrayOfCLKernel[0];
    int i = paramInt3;
    int j = paramInt4;
    int k = paramInt5;
    CLBuffer localCLBuffer1 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToInts(paramArrayOfInt1), true);
    CLBuffer localCLBuffer2 = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToInts(paramArrayOfInt2), true);
    CLBuffer localCLBuffer3 = context.createBuffer(CLMem.Usage.Input, Pointer.pointerToInts(paramArrayOfInt3), true);
    localCLKernel.setArgs(new Object[] { paramInt1, paramInt2, i, j, k, localCLBuffer1, localCLBuffer2, localCLBuffer3 });
    int m = paramInt1 - paramInt5 / 1;
    int n = paramInt2 - paramInt3 / 1;
    CLEvent[] arrayOfCLEvent = { null };
    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { m, n }, arrayOfCLEvent);
    localCLQueue.finish();
    Pointer localPointer1 = localCLBuffer2.read(localCLQueue, new CLEvent[] { localCLEvent });
    long l = localPointer1.getValidElements();
    for (int i1 = 0; i1 < l; i1++)
    {
      Pointer localPointer2 = (Pointer)localPointer1.get(i1);
      localPointer2.getInts(paramArrayOfInt2[i1]);
    }
  }
}
