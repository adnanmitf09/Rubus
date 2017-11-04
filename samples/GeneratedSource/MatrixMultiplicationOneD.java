public class MatrixMultiplicationOneD {
     private static final String kernel_539791456 = ""+
" __kernel void kernel_539791456(const int limit0, int v7_INT, int v5_INT, int v2_INT, __global float* v0_2891, float v6_FLOAT, __global float* v1_2891, int v4_INT) { "+
"      int dim0 = get_global_id(0);                            "+
"      v7_INT += 1 * dim0;                                     "+
"      if(v7_INT >= limit0) return;                            "+
"      {                                                       "+
"            float t0 = v6_FLOAT;                              "+
"           __global float* t1 = v0_2891;                      "+
"            int t2 = v4_INT;                                  "+
"            int t3 = v2_INT;                                  "+
"            int t4 = v7_INT;                                  "+
"            int t5 = t2*t3;                                   "+
"            int t6 = t5+t4;                                   "+
"            float t7 = t1[t6];                                "+
"           __global float* t8 = v1_2891;                      "+
"            int t9 = v7_INT;                                  "+
"            int t10 = v2_INT;                                 "+
"            int t11 = v5_INT;                                 "+
"            int t12 = t9*t10;                                 "+
"            int t13 = t12+t11;                                "+
"            float t14 = t8[t13];                              "+
"            float t15 = t7*t14;                               "+
"            float t16 = t0+t15;                               "+
"           v6_FLOAT = t16;                                    "+
"           }/*15*/                                            "+
"      {                                                       "+
"           v7_INT += 1;                                       "+
"           }/*16*/                                            "+
"      {                                                       "+
"           }/*29*/                                            "+
"      }";
     private static com.nativelibs4java.opencl.CLContext context = com.nativelibs4java.opencl.JavaCL.createBestContext();
     public static void kernel_539791456(int limit0, int v7_INT, int v5_INT, int v2_INT, float[] v0_2891, float v6_FLOAT, float[] v1_2891, int v4_INT) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_539791456};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          int _v7_INT = v7_INT;
          int _v5_INT = v5_INT;
          int _v2_INT = v2_INT;
          com.nativelibs4java.opencl.CLBuffer _v0_2891 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToFloats(v0_2891), true);
          float _v6_FLOAT = v6_FLOAT;
          com.nativelibs4java.opencl.CLBuffer _v1_2891 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToFloats(v1_2891), true);
          int _v4_INT = v4_INT;
          kernel.setArgs(new Object[]{limit0,  _v7_INT,  _v5_INT,  _v2_INT,  _v0_2891,  _v6_FLOAT,  _v1_2891,  _v4_INT});
          int required0 = (limit0 - v7_INT / 1);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0}, eventsToWaitFor);
          queue.finish();
          }
     }

