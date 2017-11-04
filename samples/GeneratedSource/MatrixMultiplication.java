public class MatrixMultiplication {
     private static final String kernel_1711690764 = ""+
" __kernel void kernel_1711690764(const int limit0, const int limit1, int v6_INT, int v3_INT, int v7_INT, __global int* v1_90345, __global int* v5_90345, __global int* v0_90345) { "+
"      int dim1 = get_global_id(1);                            "+
"      int dim0 = get_global_id(0);                            "+
"      v7_INT += 1 * dim0;                                     "+
"      v6_INT += 1 * dim1;                                     "+
"      if(v7_INT >= limit0) return;                            "+
"      if(v6_INT >= limit1) return;                            "+
"      int v8_INT;                                             "+
"      {                                                       "+
"            int t0 = 0;                                       "+
"           v8_INT = t0;                                       "+
"           }/*49*/                                            "+
"      {                                                       "+
"           }/*50*/                                            "+
"      {                                                       "+
"            int t1 = v3_INT;                                  "+
"           while(v8_INT < t1) {                               "+
"                {                                             "+
"                     __global int* t2 = v5_90345;             "+
"                      int t3 = v6_INT;                        "+
"                     __global int* t4 = t2[t3];               "+
"                      int t5 = v7_INT;                        "+
"                     __global int* t6 = v5_90345;             "+
"                      int t7 = v6_INT;                        "+
"                     __global int* t8 = t6[t7];               "+
"                      int t9 = v7_INT;                        "+
"                      int t10 = t8[t9];                       "+
"                     __global int* t11 = v0_90345;            "+
"                      int t12 = v6_INT;                       "+
"                     __global int* t13 = t11[t12];            "+
"                      int t14 = v8_INT;                       "+
"                      int t15 = t13[t14];                     "+
"                     __global int* t16 = v1_90345;            "+
"                      int t17 = v8_INT;                       "+
"                     __global int* t18 = t16[t17];            "+
"                      int t19 = v7_INT;                       "+
"                      int t20 = t18[t19];                     "+
"                      int t21 = t15*t20;                      "+
"                      int t22 = t10+t21;                      "+
"                     t4[t5] = t22;                            "+
"                     }/*52*/                                  "+
"                {                                             "+
"                     v8_INT += 1;                             "+
"                     }/*53*/                                  "+
"                {                                             "+
"                     }/*63*/                                  "+
"                }/*loop end*/                                 "+
"           }/*68*/                                            "+
"      {                                                       "+
"           }/*54*/                                            "+
"      {                                                       "+
"           v7_INT += 1;                                       "+
"           }/*55*/                                            "+
"      {                                                       "+
"           }/*65*/                                            "+
"      }";
     private static com.nativelibs4java.opencl.CLContext context = com.nativelibs4java.opencl.JavaCL.createBestContext();
     public static void kernel_1711690764(int limit0, int limit1, int v6_INT, int v3_INT, int v7_INT, int[][] v1_90345, int[][] v5_90345, int[][] v0_90345) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_1711690764};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          int _v6_INT = v6_INT;
          int _v3_INT = v3_INT;
          int _v7_INT = v7_INT;
          com.nativelibs4java.opencl.CLBuffer _v1_90345 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToInts(v1_90345), true);
          com.nativelibs4java.opencl.CLBuffer _v5_90345 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.InputOutput, org.bridj.Pointer.pointerToInts(v5_90345), true);
          com.nativelibs4java.opencl.CLBuffer _v0_90345 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToInts(v0_90345), true);
          kernel.setArgs(new Object[]{limit0, limit1,  _v6_INT,  _v3_INT,  _v7_INT,  _v1_90345,  _v5_90345,  _v0_90345});
          int required0 = (limit0 - v7_INT / 1);
          int required1 = (limit1 - v6_INT / 1);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0, required1}, eventsToWaitFor);
          queue.finish();
          org.bridj.Pointer p_v5_90345 = _v5_90345.read(queue, new com.nativelibs4java.opencl.CLEvent[]{clEvent1});
          long p_v5_90345_length = p_v5_90345.getValidElements();
          for (int i = 0; i < p_v5_90345_length ; ++i)
          {
          org.bridj.Pointer tempPtr = (org.bridj.Pointer) p_v5_90345.get((long)i);
          tempPtr.getInts(v5_90345[i]);
          }
          }
     }

