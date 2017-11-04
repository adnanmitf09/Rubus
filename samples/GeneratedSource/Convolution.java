public class Convolution {
     private static final String kernel_1817842420 = ""+
" static int checkbound(int v0_INT, int v1_INT, int v2_INT, int v3_INT) { "+
"      int v4_INT;                                             "+
"      {                                                       "+
"           }/*87*/                                            "+
"      {                                                       "+
"            int t14 = 3;                                      "+
"           v4_INT = t14;                                      "+
"           }/*88*/                                            "+
"      {                                                       "+
"            int t15 = v0_INT;                                 "+
"            int t16 = v4_INT;                                 "+
"           if(t15>t16) { {                                    "+
"                      int t17 = v0_INT;                       "+
"                      int t18 = v2_INT;                       "+
"                      int t19 = v4_INT;                       "+
"                      int t20 = v4_INT;                       "+
"                      int t21 = t18*t19;                      "+
"                      int t22 = t21-t20;                      "+
"                     if(t17<t22) { {                          "+
"                                int t23 = v1_INT;             "+
"                                int t24 = 1;                  "+
"                               if(t23>t24) { {                "+
"                                          int t25 = v1_INT;   "+
"                                          int t26 = v3_INT;   "+
"                                          int t27 = 1;        "+
"                                          int t28 = t26-t27;  "+
"                                         if(t25<t28) { {      "+
"                                                   }/*94*/    "+
"                                              {               "+
"                                                    int t29 = 1; "+
"                                                   return t29; "+
"                                                   }/*95*/    "+
"                                              }/* end if */   "+
"                                         {                    "+
"                                               int t30 = 0;   "+
"                                              return t30;     "+
"                                              }/*90*/         "+
"                                         }/*93*/              "+
"                                    }/* end if */             "+
"                               }/*92*/                        "+
"                          }/* end if */                       "+
"                     }/*91*/                                  "+
"                }/* end if */                                 "+
"           }/*89*/                                            "+
"      }"+ 
" static void processPixel(int v0_INT, int v1_INT, int v2_INT, int v3_INT, __global char* v4_2887, __global char* v5_2887, __global float* v6_2891) { "+
"      int v10_INT;                                            "+
"      int v15_INT;                                            "+
"      int v14_INT;                                            "+
"      float v17_FLOAT;                                        "+
"      int v9_INT;                                             "+
"      int v8_INT;                                             "+
"      int v13_INT;                                            "+
"      float v7_FLOAT;                                         "+
"      int v12_INT;                                            "+
"      int v16_INT;                                            "+
"      int v11_INT;                                            "+
"      {                                                       "+
"           }/*97*/                                            "+
"      {                                                       "+
"            float t41 = 0.0f;                                 "+
"           v7_FLOAT = t41;                                    "+
"           }/*98*/                                            "+
"      {                                                       "+
"            int t42 = 0;                                      "+
"           v8_INT = t42;                                      "+
"           }/*99*/                                            "+
"      {                                                       "+
"            char t43 = -3;                                    "+
"           v9_INT = t43;                                      "+
"           }/*100*/                                           "+
"      {                                                       "+
"            char t44 = 6;                                     "+
"           v10_INT = t44;                                     "+
"           }/*101*/                                           "+
"      {                                                       "+
"            int t45 = -1;                                     "+
"           v11_INT = t45;                                     "+
"           }/*102*/                                           "+
"      {                                                       "+
"            int t46 = 2;                                      "+
"           v12_INT = t46;                                     "+
"           }/*103*/                                           "+
"      {                                                       "+
"            int t47 = v9_INT;                                 "+
"           v13_INT = t47;                                     "+
"           }/*104*/                                           "+
"      {                                                       "+
"           }/*105*/                                           "+
"      {                                                       "+
"            int t48 = v13_INT;                                "+
"            int t49 = v10_INT;                                "+
"           if(t48>=t49) { {                                   "+
"                     }/*117*/                                 "+
"                {                                             "+
"                      short t50 = 255;                        "+
"                     v13_INT = t50;                           "+
"                     }/*118*/                                 "+
"                {                                             "+
"                      float t51 = v7_FLOAT;                   "+
"                      int t52 = v13_INT;                      "+
"                      int t53 = (int) t51;                    "+
"                      int t54 = min(t53, t52);                "+
"                      int t55 = 0;                            "+
"                      int t56 = max(t55, t54);                "+
"                      char t57 = (char) t56;                  "+
"                     v14_INT = t57;                           "+
"                     }/*119*/                                 "+
"                {                                             "+
"                     __global char* t58 = v5_2887;            "+
"                      int t59 = v1_INT;                       "+
"                      int t60 = v2_INT;                       "+
"                      int t61 = v0_INT;                       "+
"                      int t62 = v14_INT;                      "+
"                      int t63 = t59*t60;                      "+
"                      int t64 = t63+t61;                      "+
"                     t58[t64] = t62;                          "+
"                     }/*120*/                                 "+
"                {                                             "+
"                     return;                                  "+
"                     }/*121*/                                 "+
"                }/* end if */                                 "+
"           {                                                  "+
"                 int t65 = v11_INT;                           "+
"                v14_INT = t65;                                "+
"                }/*107*/                                      "+
"           {                                                  "+
"                }/*108*/                                      "+
"           {                                                  "+
"                 int t66 = v14_INT;                           "+
"                 int t67 = v12_INT;                           "+
"                if(t66>=t67) { {                              "+
"                          }/*115*/                            "+
"                     {                                        "+
"                          v13_INT += 3;                       "+
"                          }/*116*/                            "+
"                     }/* end if */                            "+
"                {                                             "+
"                     __global char* t68 = v4_2887;            "+
"                      int t69 = v1_INT;                       "+
"                      int t70 = v14_INT;                      "+
"                      int t71 = v2_INT;                       "+
"                      int t72 = v0_INT;                       "+
"                      int t73 = v13_INT;                      "+
"                      int t74 = t69+t70;                      "+
"                      int t75 = t74*t71;                      "+
"                      int t76 = t72+t73;                      "+
"                      int t77 = t75+t76;                      "+
"                      char t78 = t68[t77];                    "+
"                     v15_INT = t78;                           "+
"                     }/*110*/                                 "+
"                {                                             "+
"                      int t79 = v15_INT;                      "+
"                      short t80 = 255;                        "+
"                      int t81 = t80&t79;                      "+
"                     v16_INT = t81;                           "+
"                     }/*111*/                                 "+
"                {                                             "+
"                     __global float* t82 = v6_2891;           "+
"                      int t83 = v8_INT;                       "+
"                     v8_INT += 1;                             "+
"                      float t84 = t82[t83];                   "+
"                     v17_FLOAT = t84;                         "+
"                     }/*112*/                                 "+
"                {                                             "+
"                      float t85 = v7_FLOAT;                   "+
"                      int t86 = v16_INT;                      "+
"                      float t87 = v17_FLOAT;                  "+
"                      float t88 = (float) t86;                "+
"                      float t89 = t88*t87;                    "+
"                      float t90 = t85+t89;                    "+
"                     v7_FLOAT = t90;                          "+
"                     }/*113*/                                 "+
"                {                                             "+
"                     v14_INT += 1;                            "+
"                     }/*114*/                                 "+
"                }/*109*/                                      "+
"           }/*106*/                                           "+
"      }"+ 
" __kernel void kernel_1817842420(const int limit0, int v0_INT, __global float* v4_2891, int v5_INT, int v1_INT, __global char* v2_2887, int v7_INT, int v9_INT, __global char* v3_2887, int v8_INT) { "+
"      int dim0 = get_global_id(0);                            "+
"      v9_INT += 1 * dim0;                                     "+
"      if(v9_INT >= limit0) return;                            "+
"      int v10_INT;                                            "+
"      int v12_INT;                                            "+
"      int v11_INT;                                            "+
"      {                                                       "+
"            int t0 = v9_INT;                                  "+
"            int t1 = v0_INT;                                  "+
"            int t2 = v5_INT;                                  "+
"            int t3 = t1*t2;                                   "+
"            int t4 = t0%t3;                                   "+
"           v10_INT = t4;                                      "+
"           }/*77*/                                            "+
"      {                                                       "+
"            int t5 = v9_INT;                                  "+
"            int t6 = v0_INT;                                  "+
"            int t7 = v5_INT;                                  "+
"            int t8 = t6*t7;                                   "+
"            int t9 = t5/t8;                                   "+
"           v11_INT = t9;                                      "+
"           }/*78*/                                            "+
"      {                                                       "+
"            int t10 = v10_INT;                                "+
"            int t11 = v11_INT;                                "+
"            int t12 = v0_INT;                                 "+
"            int t13 = v1_INT;                                 "+
"            int t31 = checkbound(t10, t11, t12, t13);         "+
"           v12_INT = t31;                                     "+
"           }/*79*/                                            "+
"      {                                                       "+
"            int t32 = v12_INT;                                "+
"            int t33 = v8_INT;                                 "+
"           if(t32==t33) { {                                   "+
"                     }/*82*/                                  "+
"                {                                             "+
"                      int t34 = v10_INT;                      "+
"                      int t35 = v11_INT;                      "+
"                      int t36 = v7_INT;                       "+
"                      int t37 = v1_INT;                       "+
"                     __global char* t38 = v2_2887;            "+
"                     __global char* t39 = v3_2887;            "+
"                     __global float* t40 = v4_2891;           "+
"                     processPixel(t34, t35, t36, t37, t38, t39, t40); "+
"                     }/*83*/                                  "+
"                }/* end if */                                 "+
"           {                                                  "+
"                v9_INT += 1;                                  "+
"                }/*81*/                                       "+
"           {                                                  "+
"                }/*123*/                                      "+
"           }/*80*/                                            "+
"      }";
     private static com.nativelibs4java.opencl.CLContext context = com.nativelibs4java.opencl.JavaCL.createBestContext();
     public static void kernel_1817842420(int limit0, int v0_INT, float[] v4_2891, int v5_INT, int v1_INT, byte[] v2_2887, int v7_INT, int v9_INT, byte[] v3_2887, int v8_INT) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_1817842420};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          int _v0_INT = v0_INT;
          com.nativelibs4java.opencl.CLBuffer _v4_2891 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToFloats(v4_2891), true);
          int _v5_INT = v5_INT;
          int _v1_INT = v1_INT;
          com.nativelibs4java.opencl.CLBuffer _v2_2887 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToBytes(v2_2887), true);
          int _v7_INT = v7_INT;
          int _v9_INT = v9_INT;
          com.nativelibs4java.opencl.CLBuffer _v3_2887 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.InputOutput, org.bridj.Pointer.pointerToBytes(v3_2887), true);
          int _v8_INT = v8_INT;
          kernel.setArgs(new Object[]{limit0,  _v0_INT,  _v4_2891,  _v5_INT,  _v1_INT,  _v2_2887,  _v7_INT,  _v9_INT,  _v3_2887,  _v8_INT});
          int required0 = (limit0 - v9_INT / 1);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0}, eventsToWaitFor);
          queue.finish();
          _v3_2887.read(queue, new com.nativelibs4java.opencl.CLEvent[]{ clEvent1}).getBytes(v3_2887);
          }
     }

