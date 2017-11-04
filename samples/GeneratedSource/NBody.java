public class NBody {
     private static final String kernel_1983995535 = ""+
" __kernel void kernel_1983995535(const int limit0, float v13_FLOAT, float v10_FLOAT, float v3_FLOAT, float v11_FLOAT, float v9_FLOAT, float v4_FLOAT, float v12_FLOAT, int v15_INT, float v14_FLOAT, __global float* v1_2891) { "+
"      int dim0 = get_global_id(0);                            "+
"      v15_INT += 3 * dim0;                                    "+
"      if(v15_INT >= limit0) return;                           "+
"      float v19_FLOAT;                                        "+
"      float v16_FLOAT;                                        "+
"      float v20_FLOAT;                                        "+
"      float v18_FLOAT;                                        "+
"      float v17_FLOAT;                                        "+
"      {                                                       "+
"           __global float* t0 = v1_2891;                      "+
"            int t1 = v15_INT;                                 "+
"            int t2 = 0;                                       "+
"            int t3 = t1+t2;                                   "+
"            float t4 = t0[t3];                                "+
"            float t5 = v12_FLOAT;                             "+
"            float t6 = t4-t5;                                 "+
"           v16_FLOAT = t6;                                    "+
"           }/*46*/                                            "+
"      {                                                       "+
"           __global float* t7 = v1_2891;                      "+
"            int t8 = v15_INT;                                 "+
"            int t9 = 1;                                       "+
"            int t10 = t8+t9;                                  "+
"            float t11 = t7[t10];                              "+
"            float t12 = v13_FLOAT;                            "+
"            float t13 = t11-t12;                              "+
"           v17_FLOAT = t13;                                   "+
"           }/*47*/                                            "+
"      {                                                       "+
"           __global float* t14 = v1_2891;                     "+
"            int t15 = v15_INT;                                "+
"            int t16 = 2;                                      "+
"            int t17 = t15+t16;                                "+
"            float t18 = t14[t17];                             "+
"            float t19 = v14_FLOAT;                            "+
"            float t20 = t18-t19;                              "+
"           v18_FLOAT = t20;                                   "+
"           }/*48*/                                            "+
"      {                                                       "+
"            float t21 = v16_FLOAT;                            "+
"            float t22 = v16_FLOAT;                            "+
"            float t23 = v17_FLOAT;                            "+
"            float t24 = v17_FLOAT;                            "+
"            float t25 = v18_FLOAT;                            "+
"            float t26 = v18_FLOAT;                            "+
"            float t27 = v3_FLOAT;                             "+
"            float t28 = t21*t22;                              "+
"            float t29 = t23*t24;                              "+
"            float t30 = t28+t29;                              "+
"            float t31 = t25*t26;                              "+
"            float t32 = t30+t31;                              "+
"            float t33 = t32+t27;                              "+
"            float t34 = (float) t33;                          "+
"            float t35 = sqrt(t34);                            "+
"            float t36 = 1.0f;                                 "+
"            float t37 = (float) t35;                          "+
"            float t38 = t36/t37;                              "+
"           v19_FLOAT = t38;                                   "+
"           }/*49*/                                            "+
"      {                                                       "+
"            float t39 = v4_FLOAT;                             "+
"            float t40 = v19_FLOAT;                            "+
"            float t41 = v19_FLOAT;                            "+
"            float t42 = v19_FLOAT;                            "+
"            float t43 = t39*t40;                              "+
"            float t44 = t43*t41;                              "+
"            float t45 = t44*t42;                              "+
"           v20_FLOAT = t45;                                   "+
"           }/*50*/                                            "+
"      {                                                       "+
"            float t46 = v9_FLOAT;                             "+
"            float t47 = v20_FLOAT;                            "+
"            float t48 = v16_FLOAT;                            "+
"            float t49 = t47*t48;                              "+
"            float t50 = t46+t49;                              "+
"           v9_FLOAT = t50;                                    "+
"           }/*51*/                                            "+
"      {                                                       "+
"            float t51 = v10_FLOAT;                            "+
"            float t52 = v20_FLOAT;                            "+
"            float t53 = v17_FLOAT;                            "+
"            float t54 = t52*t53;                              "+
"            float t55 = t51+t54;                              "+
"           v10_FLOAT = t55;                                   "+
"           }/*52*/                                            "+
"      {                                                       "+
"            float t56 = v11_FLOAT;                            "+
"            float t57 = v20_FLOAT;                            "+
"            float t58 = v18_FLOAT;                            "+
"            float t59 = t57*t58;                              "+
"            float t60 = t56+t59;                              "+
"           v11_FLOAT = t60;                                   "+
"           }/*53*/                                            "+
"      {                                                       "+
"           v15_INT += 3;                                      "+
"           }/*54*/                                            "+
"      {                                                       "+
"           }/*69*/                                            "+
"      }";
     private static com.nativelibs4java.opencl.CLContext context = com.nativelibs4java.opencl.JavaCL.createBestContext();
     public static void kernel_1983995535(int limit0, float v13_FLOAT, float v10_FLOAT, float v3_FLOAT, float v11_FLOAT, float v9_FLOAT, float v4_FLOAT, float v12_FLOAT, int v15_INT, float v14_FLOAT, float[] v1_2891) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_1983995535};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          float _v13_FLOAT = v13_FLOAT;
          float _v10_FLOAT = v10_FLOAT;
          float _v3_FLOAT = v3_FLOAT;
          float _v11_FLOAT = v11_FLOAT;
          float _v9_FLOAT = v9_FLOAT;
          float _v4_FLOAT = v4_FLOAT;
          float _v12_FLOAT = v12_FLOAT;
          int _v15_INT = v15_INT;
          float _v14_FLOAT = v14_FLOAT;
          com.nativelibs4java.opencl.CLBuffer _v1_2891 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToFloats(v1_2891), true);
          kernel.setArgs(new Object[]{limit0,  _v13_FLOAT,  _v10_FLOAT,  _v3_FLOAT,  _v11_FLOAT,  _v9_FLOAT,  _v4_FLOAT,  _v12_FLOAT,  _v15_INT,  _v14_FLOAT,  _v1_2891});
          int required0 = (limit0 - v15_INT / 3);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0}, eventsToWaitFor);
          queue.finish();
          }
     }

