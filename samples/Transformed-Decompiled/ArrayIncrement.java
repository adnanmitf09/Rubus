import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice.QueueProperties;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import java.util.Arrays;
import org.bridj.Pointer;

public class ArrayIncrement
{
  private static final String kernel_1507237915 = " __kernel void kernel_1507237915(const int limit0, __global int* v0_2894, int v1_INT) {       int dim0 = get_global_id(0);                                  v1_INT += 1 * dim0;                                           if(v1_INT >= limit0) return;                                  int v2_INT;                                                   {                                                                  __global int* t0 = v0_2894;                                    int t1 = v1_INT;                                              int t2 = t0[t1];                                             v2_INT = t2;                                                  }/*20*/                                                  {                                                                  v2_INT += 1;                                                  }/*21*/                                                  {                                                                  __global int* t3 = v0_2894;                                    int t4 = v1_INT;                                              int t5 = v2_INT;                                             t3[t4] = t5;                                                  }/*22*/                                                  {                                                                  v1_INT += 1;                                                  }/*23*/                                                  {                                                                  }/*27*/                                                  }";
  private static CLContext context = JavaCL.createBestContext();
  private static final String kernel_2128203651 = " __kernel void kernel_2128203651(const int limit0, __global int* v0_2894, int v1_INT) {       int dim0 = get_global_id(0);                                  v1_INT += 1 * dim0;                                           if(v1_INT >= limit0) return;                                  int v2_INT;                                                   {                                                                  __global int* t6 = v0_2894;                                    int t7 = v1_INT;                                              int t8 = t6[t7];                                             v2_INT = t8;                                                  }/*9*/                                                   {                                                                  v2_INT += 1;                                                  }/*10*/                                                  {                                                                  __global int* t9 = v0_2894;                                    int t10 = v1_INT;                                             int t11 = v2_INT;                                            t9[t10] = t11;                                                }/*11*/                                                  {                                                                  v1_INT += 1;                                                  }/*12*/                                                  {                                                                  }/*31*/                                                  }";
  
  public static void kernel_2128203651(int paramInt1, int[] paramArrayOfInt, int paramInt2)
  {
    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
    String[] arrayOfString = { kernel_2128203651 };
    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
    CLKernel localCLKernel = arrayOfCLKernel[0];
    CLBuffer localCLBuffer = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToInts(paramArrayOfInt), true);
    int i = paramInt2;
    localCLKernel.setArgs(new Object[] { paramInt1, localCLBuffer, i });
    int j = paramInt1 - paramInt2 / 1;
    CLEvent[] arrayOfCLEvent = { null };
    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { j }, arrayOfCLEvent);
    localCLQueue.finish();
    localCLBuffer.read(localCLQueue, new CLEvent[] { localCLEvent }).getInts(paramArrayOfInt);
  }
  
  public static void kernel_1507237915(int paramInt1, int[] paramArrayOfInt, int paramInt2)
  {
    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
    String[] arrayOfString = { kernel_1507237915 };
    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
    CLKernel localCLKernel = arrayOfCLKernel[0];
    CLBuffer localCLBuffer = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToInts(paramArrayOfInt), true);
    int i = paramInt2;
    localCLKernel.setArgs(new Object[] { paramInt1, localCLBuffer, i });
    int j = paramInt1 - paramInt2 / 1;
    CLEvent[] arrayOfCLEvent = { null };
    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { j }, arrayOfCLEvent);
    localCLQueue.finish();
    localCLBuffer.read(localCLQueue, new CLEvent[] { localCLEvent }).getInts(paramArrayOfInt);
  }
  
  public static void doIncrement()
  {
    int[] arrayOfInt = new int[100];
    Arrays.fill(arrayOfInt, 45);
    int i = 0;kernel_2128203651(arrayOfInt.length, arrayOfInt, i);
  }
  
  public static void doIncrement(int[] paramArrayOfInt)
  {
    int i = 0;kernel_1507237915(paramArrayOfInt.length, paramArrayOfInt, i);
  }
}
