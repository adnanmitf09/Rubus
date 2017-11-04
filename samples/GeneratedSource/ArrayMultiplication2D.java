public class ArrayMultiplication2D {
     private static final String kernel_M382277171 = ""+
" __kernel void kernel_M382277171(const int limit0, const int limit1, __global int* v2_90345, int v4_INT, int v3_INT, __global int* v1_90345, __global int* v0_90345) { "+
"      int dim1 = get_global_id(1);                            "+
"      int dim0 = get_global_id(0);                            "+
"      v4_INT += 1 * dim0;                                     "+
"      v3_INT += 1 * dim1;                                     "+
"      if(v4_INT >= limit0) return;                            "+
"      if(v3_INT >= limit1) return;                            "+
"      {                                                       "+
"           __global int* t0 = v2_90345;                       "+
"            int t1 = v3_INT;                                  "+
"           __global int* t2 = t0[t1];                         "+
"            int t3 = v4_INT;                                  "+
"           __global int* t4 = v0_90345;                       "+
"            int t5 = v3_INT;                                  "+
"           __global int* t6 = t4[t5];                         "+
"            int t7 = v4_INT;                                  "+
"            int t8 = t6[t7];                                  "+
"           __global int* t9 = v1_90345;                       "+
"            int t10 = v3_INT;                                 "+
"           __global int* t11 = t9[t10];                       "+
"            int t12 = v4_INT;                                 "+
"            int t13 = t11[t12];                               "+
"            int t14 = t8*t13;                                 "+
"           t2[t3] = t14;                                      "+
"           }/*33*/                                            "+
"      {                                                       "+
"           v4_INT += 1;                                       "+
"           }/*34*/                                            "+
"      {                                                       "+
"           }/*42*/                                            "+
"      }";
     private static com.nativelibs4java.opencl.CLContext context = com.nativelibs4java.opencl.JavaCL.createBestContext();
     public static void kernel_M382277171(int limit0, int limit1, int[][] v2_90345, int v4_INT, int v3_INT, int[][] v1_90345, int[][] v0_90345) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_M382277171};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          com.nativelibs4java.opencl.CLBuffer _v2_90345 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.InputOutput, org.bridj.Pointer.pointerToInts(v2_90345), true);
          int _v4_INT = v4_INT;
          int _v3_INT = v3_INT;
          com.nativelibs4java.opencl.CLBuffer _v1_90345 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToInts(v1_90345), true);
          com.nativelibs4java.opencl.CLBuffer _v0_90345 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToInts(v0_90345), true);
          kernel.setArgs(new Object[]{limit0, limit1,  _v2_90345,  _v4_INT,  _v3_INT,  _v1_90345,  _v0_90345});
          int required0 = (limit0 - v4_INT / 1);
          int required1 = (limit1 - v3_INT / 1);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0, required1}, eventsToWaitFor);
          queue.finish();
          org.bridj.Pointer p_v2_90345 = _v2_90345.read(queue, new com.nativelibs4java.opencl.CLEvent[]{clEvent1});
          long p_v2_90345_length = p_v2_90345.getValidElements();
          for (int i = 0; i < p_v2_90345_length ; ++i)
          {
          org.bridj.Pointer tempPtr = (org.bridj.Pointer) p_v2_90345.get((long)i);
          tempPtr.getInts(v2_90345[i]);
          }
          }
     }

