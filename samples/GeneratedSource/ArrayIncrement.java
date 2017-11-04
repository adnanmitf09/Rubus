public class ArrayIncrement {
     private static final String kernel_1507237915 = ""+
" __kernel void kernel_1507237915(const int limit0, __global int* v0_2894, int v1_INT) { "+
"      int dim0 = get_global_id(0);                            "+
"      v1_INT += 1 * dim0;                                     "+
"      if(v1_INT >= limit0) return;                            "+
"      int v2_INT;                                             "+
"      {                                                       "+
"           __global int* t0 = v0_2894;                        "+
"            int t1 = v1_INT;                                  "+
"            int t2 = t0[t1];                                  "+
"           v2_INT = t2;                                       "+
"           }/*20*/                                            "+
"      {                                                       "+
"           v2_INT += 1;                                       "+
"           }/*21*/                                            "+
"      {                                                       "+
"           __global int* t3 = v0_2894;                        "+
"            int t4 = v1_INT;                                  "+
"            int t5 = v2_INT;                                  "+
"           t3[t4] = t5;                                       "+
"           }/*22*/                                            "+
"      {                                                       "+
"           v1_INT += 1;                                       "+
"           }/*23*/                                            "+
"      {                                                       "+
"           }/*27*/                                            "+
"      }";
     private static com.nativelibs4java.opencl.CLContext context = com.nativelibs4java.opencl.JavaCL.createBestContext();
     private static final String kernel_2128203651 = ""+
" __kernel void kernel_2128203651(const int limit0, __global int* v0_2894, int v1_INT) { "+
"      int dim0 = get_global_id(0);                            "+
"      v1_INT += 1 * dim0;                                     "+
"      if(v1_INT >= limit0) return;                            "+
"      int v2_INT;                                             "+
"      {                                                       "+
"           __global int* t6 = v0_2894;                        "+
"            int t7 = v1_INT;                                  "+
"            int t8 = t6[t7];                                  "+
"           v2_INT = t8;                                       "+
"           }/*9*/                                             "+
"      {                                                       "+
"           v2_INT += 1;                                       "+
"           }/*10*/                                            "+
"      {                                                       "+
"           __global int* t9 = v0_2894;                        "+
"            int t10 = v1_INT;                                 "+
"            int t11 = v2_INT;                                 "+
"           t9[t10] = t11;                                     "+
"           }/*11*/                                            "+
"      {                                                       "+
"           v1_INT += 1;                                       "+
"           }/*12*/                                            "+
"      {                                                       "+
"           }/*31*/                                            "+
"      }";
     public static void kernel_1507237915(int limit0, int[] v0_2894, int v1_INT) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_1507237915};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          com.nativelibs4java.opencl.CLBuffer _v0_2894 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.InputOutput, org.bridj.Pointer.pointerToInts(v0_2894), true);
          int _v1_INT = v1_INT;
          kernel.setArgs(new Object[]{limit0,  _v0_2894,  _v1_INT});
          int required0 = (limit0 - v1_INT / 1);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0}, eventsToWaitFor);
          queue.finish();
          _v0_2894.read(queue, new com.nativelibs4java.opencl.CLEvent[]{ clEvent1}).getInts(v0_2894);
          }
     public static void kernel_2128203651(int limit0, int[] v0_2894, int v1_INT) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_2128203651};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          com.nativelibs4java.opencl.CLBuffer _v0_2894 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.InputOutput, org.bridj.Pointer.pointerToInts(v0_2894), true);
          int _v1_INT = v1_INT;
          kernel.setArgs(new Object[]{limit0,  _v0_2894,  _v1_INT});
          int required0 = (limit0 - v1_INT / 1);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0}, eventsToWaitFor);
          queue.finish();
          _v0_2894.read(queue, new com.nativelibs4java.opencl.CLEvent[]{ clEvent1}).getInts(v0_2894);
          }
     }

