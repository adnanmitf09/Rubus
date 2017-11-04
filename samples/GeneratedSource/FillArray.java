public class FillArray {
     private static final String kernel_741680667 = ""+
" __kernel void kernel_741680667(const int limit0, int v1_INT, __global float* v0_2891) { "+
"      int dim0 = get_global_id(0);                            "+
"      v1_INT += 1 * dim0;                                     "+
"      if(v1_INT >= limit0) return;                            "+
"      {                                                       "+
"           __global float* t2 = v0_2891;                      "+
"            int t3 = v1_INT;                                  "+
"            int t4 = v1_INT;                                  "+
"            float t5 = (float) t4;                            "+
"            float t6 = log(t5);                               "+
"            int t7 = (int) t6;                                "+
"            float t8 = (float) t7;                            "+
"           t2[t3] = t8;                                       "+
"           }/*18*/                                            "+
"      {                                                       "+
"           v1_INT += 1;                                       "+
"           }/*19*/                                            "+
"      {                                                       "+
"           }/*26*/                                            "+
"      }";
     private static com.nativelibs4java.opencl.CLContext context = com.nativelibs4java.opencl.JavaCL.createBestContext();
     public static void kernel_741680667(int limit0, int v1_INT, float[] v0_2891) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_741680667};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          int _v1_INT = v1_INT;
          com.nativelibs4java.opencl.CLBuffer _v0_2891 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.InputOutput, org.bridj.Pointer.pointerToFloats(v0_2891), true);
          kernel.setArgs(new Object[]{limit0,  _v1_INT,  _v0_2891});
          int required0 = (limit0 - v1_INT / 1);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0}, eventsToWaitFor);
          queue.finish();
          _v0_2891.read(queue, new com.nativelibs4java.opencl.CLEvent[]{ clEvent1}).getFloats(v0_2891);
          }
     }

