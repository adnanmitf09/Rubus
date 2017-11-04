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

public class LoopWithMultipleConditionsInBody
{
  private static final String kernel_381446705 = " __kernel void kernel_381446705(const int limit0, __global int* v0_2894, int v1_INT) {       int dim0 = get_global_id(0);                                  v1_INT += 1 * dim0;                                           if(v1_INT >= limit0) return;                                  {                                                                  __global int* t0 = v0_2894;                                    int t1 = v1_INT;                                              int t2 = t0[t1];                                              char t3 = 50;                                                if(t2>t3) { {                                                           }/*32*/                                                  {                                                                  __global int* t4 = v0_2894;                                    int t5 = v1_INT;                                              int t6 = v1_INT;                                              int t7 = 1;                                                   int t8 = t6+t7;                                              t4[t5] = t8;                                                  }/*33*/                                                  {                                                                  }/*34*/                                                  {                                                                  v1_INT += 1;                                                  }/*35*/                                                  {                                                                  }/*43*/                                                  }/* end if */                                            {                                                                  __global int* t9 = v0_2894;                                    int t10 = v1_INT;                                             int t11 = t9[t10];                                            char t12 = 10;                                               if(t11>t12) { {                                                         }/*37*/                                                  {                                                                  __global int* t13 = v0_2894;                                   int t14 = v1_INT;                                             int t15 = v1_INT;                                             int t16 = 1;                                                  int t17 = t15-t16;                                           t13[t14] = t17;                                               }/*38*/                                                  {                                                                  }/*39*/                                                  }/* end if */                                            {                                                                  __global int* t18 = v0_2894;                                   int t19 = v1_INT;                                             int t20 = v1_INT;                                            t18[t19] = t20;                                               }/*36*/                                                  }/*31*/                                                  }/*30*/                                                  }";
  private static CLContext context = JavaCL.createBestContext();
  
  public static void compute(int[] paramArrayOfInt)
  {
    int i = 0;kernel_381446705(paramArrayOfInt.length, paramArrayOfInt, i);
  }
  
  public static void kernel_381446705(int paramInt1, int[] paramArrayOfInt, int paramInt2)
  {
    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
    String[] arrayOfString = { kernel_381446705 };
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
}
