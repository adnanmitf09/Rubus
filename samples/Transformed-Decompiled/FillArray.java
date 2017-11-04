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

public class FillArray
{
  private static final String kernel_741680667 = " __kernel void kernel_741680667(const int limit0, int v1_INT, __global float* v0_2891) {       int dim0 = get_global_id(0);                                  v1_INT += 1 * dim0;                                           if(v1_INT >= limit0) return;                                  {                                                                  __global float* t2 = v0_2891;                                  int t3 = v1_INT;                                              int t4 = v1_INT;                                              float t5 = (float) t4;                                        float t6 = log(t5);                                           int t7 = (int) t6;                                            float t8 = (float) t7;                                       t2[t3] = t8;                                                  }/*18*/                                                  {                                                                  v1_INT += 1;                                                  }/*19*/                                                  {                                                                  }/*26*/                                                  }";
  private static CLContext context = JavaCL.createBestContext();
  
  private static void randomFill()
  {
    int[] arrayOfInt = new int[100];
    for (int i = 0; i < arrayOfInt.length; i++) {
      arrayOfInt[i] = ((int)Math.random());
    }
  }
  
  private static void logFill()
  {
    float[] arrayOfFloat = new float[100];
    int i = 0;kernel_741680667(arrayOfFloat.length, i, arrayOfFloat);
  }
  
  public static void kernel_741680667(int paramInt1, int paramInt2, float[] paramArrayOfFloat)
  {
    CLDevice.QueueProperties[] arrayOfQueueProperties = { CLDevice.QueueProperties.ProfilingEnable };
    CLQueue localCLQueue = context.createDefaultQueue(arrayOfQueueProperties);
    String[] arrayOfString = { kernel_741680667 };
    CLKernel[] arrayOfCLKernel = context.createProgram(arrayOfString).createKernels();
    CLKernel localCLKernel = arrayOfCLKernel[0];
    int i = paramInt2;
    CLBuffer localCLBuffer = context.createBuffer(CLMem.Usage.InputOutput, Pointer.pointerToFloats(paramArrayOfFloat), true);
    localCLKernel.setArgs(new Object[] { paramInt1, i, localCLBuffer });
    int j = paramInt1 - paramInt2 / 1;
    CLEvent[] arrayOfCLEvent = { null };
    CLEvent localCLEvent = localCLKernel.enqueueNDRange(localCLQueue, new int[] { j }, arrayOfCLEvent);
    localCLQueue.finish();
    localCLBuffer.read(localCLQueue, new CLEvent[] { localCLEvent }).getFloats(paramArrayOfFloat);
  }
}
