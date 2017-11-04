public class LoopWithMultipleConditionsInBody {
     private static final String kernel_381446705 = ""+
" __kernel void kernel_381446705(const int limit0, __global int* v0_2894, int v1_INT) { "+
"      int dim0 = get_global_id(0);                            "+
"      v1_INT += 1 * dim0;                                     "+
"      if(v1_INT >= limit0) return;                            "+
"      {                                                       "+
"           __global int* t0 = v0_2894;                        "+
"            int t1 = v1_INT;                                  "+
"            int t2 = t0[t1];                                  "+
"            char t3 = 50;                                     "+
"           if(t2>t3) { {                                      "+
"                     }/*32*/                                  "+
"                {                                             "+
"                     __global int* t4 = v0_2894;              "+
"                      int t5 = v1_INT;                        "+
"                      int t6 = v1_INT;                        "+
"                      int t7 = 1;                             "+
"                      int t8 = t6+t7;                         "+
"                     t4[t5] = t8;                             "+
"                     }/*33*/                                  "+
"                {                                             "+
"                     }/*34*/                                  "+
"                {                                             "+
"                     v1_INT += 1;                             "+
"                     }/*35*/                                  "+
"                {                                             "+
"                     }/*43*/                                  "+
"                }/* end if */                                 "+
"           {                                                  "+
"                __global int* t9 = v0_2894;                   "+
"                 int t10 = v1_INT;                            "+
"                 int t11 = t9[t10];                           "+
"                 char t12 = 10;                               "+
"                if(t11>t12) { {                               "+
"                          }/*37*/                             "+
"                     {                                        "+
"                          __global int* t13 = v0_2894;        "+
"                           int t14 = v1_INT;                  "+
"                           int t15 = v1_INT;                  "+
"                           int t16 = 1;                       "+
"                           int t17 = t15-t16;                 "+
"                          t13[t14] = t17;                     "+
"                          }/*38*/                             "+
"                     {                                        "+
"                          }/*39*/                             "+
"                     }/* end if */                            "+
"                {                                             "+
"                     __global int* t18 = v0_2894;             "+
"                      int t19 = v1_INT;                       "+
"                      int t20 = v1_INT;                       "+
"                     t18[t19] = t20;                          "+
"                     }/*36*/                                  "+
"                }/*31*/                                       "+
"           }/*30*/                                            "+
"      }";
     private static com.nativelibs4java.opencl.CLContext context = com.nativelibs4java.opencl.JavaCL.createBestContext();
     public static void kernel_381446705(int limit0, int[] v0_2894, int v1_INT) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_381446705};
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

