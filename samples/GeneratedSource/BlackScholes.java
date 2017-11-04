public class BlackScholes {
     private static final String kernel_378899703 = ""+
" static float phi(float v0_FLOAT) {                           "+
"      float v8_FLOAT;                                         "+
"      float v1_FLOAT;                                         "+
"      float v3_FLOAT;                                         "+
"      float v6_FLOAT;                                         "+
"      float v9_FLOAT;                                         "+
"      float v4_FLOAT;                                         "+
"      float v7_FLOAT;                                         "+
"      float v2_FLOAT;                                         "+
"      float v10_FLOAT;                                        "+
"      float v5_FLOAT;                                         "+
"      {                                                       "+
"           }/*117*/                                           "+
"      {                                                       "+
"            float t90 = 0.31938154f;                          "+
"           v1_FLOAT = t90;                                    "+
"           }/*118*/                                           "+
"      {                                                       "+
"            float t91 = -0.35656378f;                         "+
"           v2_FLOAT = t91;                                    "+
"           }/*119*/                                           "+
"      {                                                       "+
"            float t92 = 1.7814779f;                           "+
"           v3_FLOAT = t92;                                    "+
"           }/*120*/                                           "+
"      {                                                       "+
"            float t93 = -1.8212559f;                          "+
"           v4_FLOAT = t93;                                    "+
"           }/*121*/                                           "+
"      {                                                       "+
"            float t94 = 1.3302745f;                           "+
"           v5_FLOAT = t94;                                    "+
"           }/*122*/                                           "+
"      {                                                       "+
"            float t95 = 0.2316419f;                           "+
"           v6_FLOAT = t95;                                    "+
"           }/*123*/                                           "+
"      {                                                       "+
"            float t96 = 0.3989423f;                           "+
"           v7_FLOAT = t96;                                    "+
"           }/*124*/                                           "+
"      {                                                       "+
"            float t97 = v0_FLOAT;                             "+
"            float t98 = fabs(t97);                            "+
"           v8_FLOAT = t98;                                    "+
"           }/*125*/                                           "+
"      {                                                       "+
"            float t99 = v6_FLOAT;                             "+
"            float t100 = v8_FLOAT;                            "+
"            float t101 = 1.0f;                                "+
"            float t102 = t99*t100;                            "+
"            float t103 = t101+t102;                           "+
"            float t104 = t101/t103;                           "+
"           v9_FLOAT = t104;                                   "+
"           }/*126*/                                           "+
"      {                                                       "+
"            float t105 = v7_FLOAT;                            "+
"            float t106 = v0_FLOAT;                            "+
"            float t107 = v0_FLOAT;                            "+
"            float t108 = -t106;                               "+
"            float t109 = t108*t107;                           "+
"            float t110 = 2.0f;                                "+
"            float t111 = t109/t110;                           "+
"            float t112 = (float) t111;                        "+
"            float t113 = exp(t112);                           "+
"            float t114 = v9_FLOAT;                            "+
"            float t115 = v1_FLOAT;                            "+
"            float t116 = v9_FLOAT;                            "+
"            float t117 = v2_FLOAT;                            "+
"            float t118 = v9_FLOAT;                            "+
"            float t119 = v3_FLOAT;                            "+
"            float t120 = v9_FLOAT;                            "+
"            float t121 = v4_FLOAT;                            "+
"            float t122 = v9_FLOAT;                            "+
"            float t123 = v5_FLOAT;                            "+
"            float t124 = 1.0;                                 "+
"            float t125 = (float) t105;                        "+
"            float t126 = t125*t113;                           "+
"            float t127 = (float) t114;                        "+
"            float t128 = t126*t127;                           "+
"            float t129 = t122*t123;                           "+
"            float t130 = t121+t129;                           "+
"            float t131 = t120*t130;                           "+
"            float t132 = t119+t131;                           "+
"            float t133 = t118*t132;                           "+
"            float t134 = t117+t133;                           "+
"            float t135 = t116*t134;                           "+
"            float t136 = t115+t135;                           "+
"            float t137 = (float) t136;                        "+
"            float t138 = t128*t137;                           "+
"            float t139 = t124-t138;                           "+
"            float t140 = (float) t139;                        "+
"           v10_FLOAT = t140;                                  "+
"           }/*127*/                                           "+
"      {                                                       "+
"            float t141 = v0_FLOAT;                            "+
"            float t142 = 0.0f;                                "+
"            int t143 = (t141<t142) ? -1 : (t141>t142) ? 1 : 0; "+
"            int t144 = 0;                                     "+
"           if(t143<t144) { {                                  "+
"                      float t145 = v10_FLOAT;                 "+
"                      float t146 = 1.0f;                      "+
"                      float t147 = t146-t145;                 "+
"                     return t147;                             "+
"                     }/*130*/                                 "+
"                }/* end if */                                 "+
"           {                                                  "+
"                 float t148 = v10_FLOAT;                      "+
"                return t148;                                  "+
"                }/*129*/                                      "+
"           }/*128*/                                           "+
"      }"+ 
" __kernel void kernel_378899703(const int limit0, int v14_INT, float v8_FLOAT, float v11_FLOAT, __global float* v0_2891, float v6_FLOAT, float v9_FLOAT, __global float* v2_2891, float v4_FLOAT, float v13_FLOAT, float v7_FLOAT, __global float* v1_2891, float v12_FLOAT, float v10_FLOAT, float v5_FLOAT) { "+
"      int dim0 = get_global_id(0);                            "+
"      v14_INT += 1 * dim0;                                    "+
"      if(v14_INT >= limit0) return;                           "+
"      float v22_FLOAT;                                        "+
"      float v17_FLOAT;                                        "+
"      float v27_FLOAT;                                        "+
"      float v25_FLOAT;                                        "+
"      float v20_FLOAT;                                        "+
"      float v18_FLOAT;                                        "+
"      float v21_FLOAT;                                        "+
"      float v23_FLOAT;                                        "+
"      float v15_FLOAT;                                        "+
"      float v16_FLOAT;                                        "+
"      float v26_FLOAT;                                        "+
"      float v19_FLOAT;                                        "+
"      float v24_FLOAT;                                        "+
"      {                                                       "+
"            float t0 = 2.0f;                                  "+
"           v15_FLOAT = t0;                                    "+
"           }/*96*/                                            "+
"      {                                                       "+
"           __global float* t1 = v0_2891;                      "+
"            int t2 = v14_INT;                                 "+
"            float t3 = t1[t2];                                "+
"           v16_FLOAT = t3;                                    "+
"           }/*97*/                                            "+
"      {                                                       "+
"            float t4 = v4_FLOAT;                              "+
"            float t5 = v16_FLOAT;                             "+
"            float t6 = v5_FLOAT;                              "+
"            float t7 = v16_FLOAT;                             "+
"            float t8 = t4*t5;                                 "+
"            float t9 = 1.0f;                                  "+
"            float t10 = t9-t7;                                "+
"            float t11 = t6*t10;                               "+
"            float t12 = t8+t11;                               "+
"           v17_FLOAT = t12;                                   "+
"           }/*98*/                                            "+
"      {                                                       "+
"            float t13 = v6_FLOAT;                             "+
"            float t14 = v16_FLOAT;                            "+
"            float t15 = v7_FLOAT;                             "+
"            float t16 = v16_FLOAT;                            "+
"            float t17 = t13*t14;                              "+
"            float t18 = 1.0f;                                 "+
"            float t19 = t18-t16;                              "+
"            float t20 = t15*t19;                              "+
"            float t21 = t17+t20;                              "+
"           v18_FLOAT = t21;                                   "+
"           }/*99*/                                            "+
"      {                                                       "+
"            float t22 = v8_FLOAT;                             "+
"            float t23 = v16_FLOAT;                            "+
"            float t24 = v9_FLOAT;                             "+
"            float t25 = v16_FLOAT;                            "+
"            float t26 = t22*t23;                              "+
"            float t27 = 1.0f;                                 "+
"            float t28 = t27-t25;                              "+
"            float t29 = t24*t28;                              "+
"            float t30 = t26+t29;                              "+
"           v19_FLOAT = t30;                                   "+
"           }/*100*/                                           "+
"      {                                                       "+
"            float t31 = v10_FLOAT;                            "+
"            float t32 = v16_FLOAT;                            "+
"            float t33 = v11_FLOAT;                            "+
"            float t34 = v16_FLOAT;                            "+
"            float t35 = t31*t32;                              "+
"            float t36 = 1.0f;                                 "+
"            float t37 = t36-t34;                              "+
"            float t38 = t33*t37;                              "+
"            float t39 = t35+t38;                              "+
"           v20_FLOAT = t39;                                   "+
"           }/*101*/                                           "+
"      {                                                       "+
"            float t40 = v12_FLOAT;                            "+
"            float t41 = v16_FLOAT;                            "+
"            float t42 = v13_FLOAT;                            "+
"            float t43 = v16_FLOAT;                            "+
"            float t44 = t40*t41;                              "+
"            float t45 = 1.0f;                                 "+
"            float t46 = t45-t43;                              "+
"            float t47 = t42*t46;                              "+
"            float t48 = t44+t47;                              "+
"           v21_FLOAT = t48;                                   "+
"           }/*102*/                                           "+
"      {                                                       "+
"            float t49 = v21_FLOAT;                            "+
"            float t50 = v19_FLOAT;                            "+
"            float t51 = (float) t50;                          "+
"            float t52 = sqrt(t51);                            "+
"            float t53 = (float) t49;                          "+
"            float t54 = t53*t52;                              "+
"            float t55 = (float) t54;                          "+
"           v22_FLOAT = t55;                                   "+
"           }/*103*/                                           "+
"      {                                                       "+
"            float t56 = v17_FLOAT;                            "+
"            float t57 = v18_FLOAT;                            "+
"            float t58 = t56/t57;                              "+
"            float t59 = (float) t58;                          "+
"            float t60 = log(t59);                             "+
"            float t61 = v20_FLOAT;                            "+
"            float t62 = v21_FLOAT;                            "+
"            float t63 = v21_FLOAT;                            "+
"            float t64 = v15_FLOAT;                            "+
"            float t65 = v19_FLOAT;                            "+
"            float t66 = v22_FLOAT;                            "+
"            float t67 = t62*t63;                              "+
"            float t68 = t67/t64;                              "+
"            float t69 = t61+t68;                              "+
"            float t70 = t69*t65;                              "+
"            float t71 = (float) t70;                          "+
"            float t72 = t60+t71;                              "+
"            float t73 = (float) t66;                          "+
"            float t74 = t72/t73;                              "+
"            float t75 = (float) t74;                          "+
"           v23_FLOAT = t75;                                   "+
"           }/*104*/                                           "+
"      {                                                       "+
"            float t76 = v23_FLOAT;                            "+
"            float t77 = v22_FLOAT;                            "+
"            float t78 = t76-t77;                              "+
"           v24_FLOAT = t78;                                   "+
"           }/*105*/                                           "+
"      {                                                       "+
"            float t79 = v18_FLOAT;                            "+
"            float t80 = v20_FLOAT;                            "+
"            float t81 = v19_FLOAT;                            "+
"            float t82 = -t80;                                 "+
"            float t83 = t82*t81;                              "+
"            float t84 = (float) t83;                          "+
"            float t85 = exp(t84);                             "+
"            float t86 = (float) t79;                          "+
"            float t87 = t86*t85;                              "+
"            float t88 = (float) t87;                          "+
"           v25_FLOAT = t88;                                   "+
"           }/*106*/                                           "+
"      {                                                       "+
"            float t89 = v23_FLOAT;                            "+
"            float t149 = phi(t89);                            "+
"           v26_FLOAT = t149;                                  "+
"           }/*107*/                                           "+
"      {                                                       "+
"            float t150 = v24_FLOAT;                           "+
"            float t151 = phi(t150);                           "+
"           v27_FLOAT = t151;                                  "+
"           }/*108*/                                           "+
"      {                                                       "+
"           __global float* t152 = v1_2891;                    "+
"            int t153 = v14_INT;                               "+
"            float t154 = v17_FLOAT;                           "+
"            float t155 = v26_FLOAT;                           "+
"            float t156 = v25_FLOAT;                           "+
"            float t157 = v27_FLOAT;                           "+
"            float t158 = t154*t155;                           "+
"            float t159 = t156*t157;                           "+
"            float t160 = t158-t159;                           "+
"           t152[t153] = t160;                                 "+
"           }/*109*/                                           "+
"      {                                                       "+
"            float t161 = v23_FLOAT;                           "+
"            float t162 = -t161;                               "+
"            float t163 = phi(t162);                           "+
"           v26_FLOAT = t163;                                  "+
"           }/*110*/                                           "+
"      {                                                       "+
"            float t164 = v24_FLOAT;                           "+
"            float t165 = -t164;                               "+
"            float t166 = phi(t165);                           "+
"           v27_FLOAT = t166;                                  "+
"           }/*111*/                                           "+
"      {                                                       "+
"           __global float* t167 = v2_2891;                    "+
"            int t168 = v14_INT;                               "+
"            float t169 = v25_FLOAT;                           "+
"            float t170 = v27_FLOAT;                           "+
"            float t171 = v17_FLOAT;                           "+
"            float t172 = v26_FLOAT;                           "+
"            float t173 = t169*t170;                           "+
"            float t174 = t171*t172;                           "+
"            float t175 = t173-t174;                           "+
"           t167[t168] = t175;                                 "+
"           }/*112*/                                           "+
"      {                                                       "+
"           v14_INT += 1;                                      "+
"           }/*113*/                                           "+
"      {                                                       "+
"           }/*134*/                                           "+
"      }";
     private static com.nativelibs4java.opencl.CLContext context = com.nativelibs4java.opencl.JavaCL.createBestContext();
     private static final String kernel_1780824482 = ""+
" static float phi(float v0_FLOAT) {                           "+
"      float v8_FLOAT;                                         "+
"      float v1_FLOAT;                                         "+
"      float v3_FLOAT;                                         "+
"      float v6_FLOAT;                                         "+
"      float v9_FLOAT;                                         "+
"      float v4_FLOAT;                                         "+
"      float v7_FLOAT;                                         "+
"      float v2_FLOAT;                                         "+
"      float v10_FLOAT;                                        "+
"      float v5_FLOAT;                                         "+
"      {                                                       "+
"           }/*117*/                                           "+
"      {                                                       "+
"            float t90 = 0.31938154f;                          "+
"           v1_FLOAT = t90;                                    "+
"           }/*118*/                                           "+
"      {                                                       "+
"            float t91 = -0.35656378f;                         "+
"           v2_FLOAT = t91;                                    "+
"           }/*119*/                                           "+
"      {                                                       "+
"            float t92 = 1.7814779f;                           "+
"           v3_FLOAT = t92;                                    "+
"           }/*120*/                                           "+
"      {                                                       "+
"            float t93 = -1.8212559f;                          "+
"           v4_FLOAT = t93;                                    "+
"           }/*121*/                                           "+
"      {                                                       "+
"            float t94 = 1.3302745f;                           "+
"           v5_FLOAT = t94;                                    "+
"           }/*122*/                                           "+
"      {                                                       "+
"            float t95 = 0.2316419f;                           "+
"           v6_FLOAT = t95;                                    "+
"           }/*123*/                                           "+
"      {                                                       "+
"            float t96 = 0.3989423f;                           "+
"           v7_FLOAT = t96;                                    "+
"           }/*124*/                                           "+
"      {                                                       "+
"            float t97 = v0_FLOAT;                             "+
"            float t98 = fabs(t97);                            "+
"           v8_FLOAT = t98;                                    "+
"           }/*125*/                                           "+
"      {                                                       "+
"            float t99 = v6_FLOAT;                             "+
"            float t100 = v8_FLOAT;                            "+
"            float t101 = 1.0f;                                "+
"            float t102 = t99*t100;                            "+
"            float t103 = t101+t102;                           "+
"            float t104 = t101/t103;                           "+
"           v9_FLOAT = t104;                                   "+
"           }/*126*/                                           "+
"      {                                                       "+
"            float t105 = v7_FLOAT;                            "+
"            float t106 = v0_FLOAT;                            "+
"            float t107 = v0_FLOAT;                            "+
"            float t108 = -t106;                               "+
"            float t109 = t108*t107;                           "+
"            float t110 = 2.0f;                                "+
"            float t111 = t109/t110;                           "+
"            float t112 = (float) t111;                        "+
"            float t113 = exp(t112);                           "+
"            float t114 = v9_FLOAT;                            "+
"            float t115 = v1_FLOAT;                            "+
"            float t116 = v9_FLOAT;                            "+
"            float t117 = v2_FLOAT;                            "+
"            float t118 = v9_FLOAT;                            "+
"            float t119 = v3_FLOAT;                            "+
"            float t120 = v9_FLOAT;                            "+
"            float t121 = v4_FLOAT;                            "+
"            float t122 = v9_FLOAT;                            "+
"            float t123 = v5_FLOAT;                            "+
"            float t124 = 1.0;                                 "+
"            float t125 = (float) t105;                        "+
"            float t126 = t125*t113;                           "+
"            float t127 = (float) t114;                        "+
"            float t128 = t126*t127;                           "+
"            float t129 = t122*t123;                           "+
"            float t130 = t121+t129;                           "+
"            float t131 = t120*t130;                           "+
"            float t132 = t119+t131;                           "+
"            float t133 = t118*t132;                           "+
"            float t134 = t117+t133;                           "+
"            float t135 = t116*t134;                           "+
"            float t136 = t115+t135;                           "+
"            float t137 = (float) t136;                        "+
"            float t138 = t128*t137;                           "+
"            float t139 = t124-t138;                           "+
"            float t140 = (float) t139;                        "+
"           v10_FLOAT = t140;                                  "+
"           }/*127*/                                           "+
"      {                                                       "+
"            float t141 = v0_FLOAT;                            "+
"            float t142 = 0.0f;                                "+
"            int t143 = (t141<t142) ? -1 : (t141>t142) ? 1 : 0; "+
"            int t144 = 0;                                     "+
"           if(t143<t144) { {                                  "+
"                      float t145 = v10_FLOAT;                 "+
"                      float t146 = 1.0f;                      "+
"                      float t147 = t146-t145;                 "+
"                     return t147;                             "+
"                     }/*130*/                                 "+
"                }/* end if */                                 "+
"           {                                                  "+
"                 float t148 = v10_FLOAT;                      "+
"                return t148;                                  "+
"                }/*129*/                                      "+
"           }/*128*/                                           "+
"      }"+ 
" __kernel void kernel_1780824482(const int limit0, int v4_INT, __global float* v0_2891, int v3_INT) { "+
"      int dim0 = get_global_id(0);                            "+
"      v4_INT += 1 * dim0;                                     "+
"      if(v4_INT >= limit0) return;                            "+
"      {                                                       "+
"           __global float* t176 = v0_2891;                    "+
"            int t177 = v4_INT;                                "+
"            int t178 = v4_INT;                                "+
"            int t179 = v3_INT;                                "+
"            float t180 = (float) t178;                        "+
"            float t181 = 1.0f;                                "+
"            float t182 = t180*t181;                           "+
"            float t183 = (float) t179;                        "+
"            float t184 = t182/t183;                           "+
"           t176[t177] = t184;                                 "+
"           }/*80*/                                            "+
"      {                                                       "+
"           v4_INT += 1;                                       "+
"           }/*81*/                                            "+
"      {                                                       "+
"           }/*132*/                                           "+
"      }";
     public static void kernel_378899703(int limit0, int v14_INT, float v8_FLOAT, float v11_FLOAT, float[] v0_2891, float v6_FLOAT, float v9_FLOAT, float[] v2_2891, float v4_FLOAT, float v13_FLOAT, float v7_FLOAT, float[] v1_2891, float v12_FLOAT, float v10_FLOAT, float v5_FLOAT) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_378899703};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          int _v14_INT = v14_INT;
          float _v8_FLOAT = v8_FLOAT;
          float _v11_FLOAT = v11_FLOAT;
          com.nativelibs4java.opencl.CLBuffer _v0_2891 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.Input, org.bridj.Pointer.pointerToFloats(v0_2891), true);
          float _v6_FLOAT = v6_FLOAT;
          float _v9_FLOAT = v9_FLOAT;
          com.nativelibs4java.opencl.CLBuffer _v2_2891 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.InputOutput, org.bridj.Pointer.pointerToFloats(v2_2891), true);
          float _v4_FLOAT = v4_FLOAT;
          float _v13_FLOAT = v13_FLOAT;
          float _v7_FLOAT = v7_FLOAT;
          com.nativelibs4java.opencl.CLBuffer _v1_2891 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.InputOutput, org.bridj.Pointer.pointerToFloats(v1_2891), true);
          float _v12_FLOAT = v12_FLOAT;
          float _v10_FLOAT = v10_FLOAT;
          float _v5_FLOAT = v5_FLOAT;
          kernel.setArgs(new Object[]{limit0,  _v14_INT,  _v8_FLOAT,  _v11_FLOAT,  _v0_2891,  _v6_FLOAT,  _v9_FLOAT,  _v2_2891,  _v4_FLOAT,  _v13_FLOAT,  _v7_FLOAT,  _v1_2891,  _v12_FLOAT,  _v10_FLOAT,  _v5_FLOAT});
          int required0 = (limit0 - v14_INT / 1);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0}, eventsToWaitFor);
          queue.finish();
          _v2_2891.read(queue, new com.nativelibs4java.opencl.CLEvent[]{ clEvent1}).getFloats(v2_2891);
          _v1_2891.read(queue, new com.nativelibs4java.opencl.CLEvent[]{ clEvent1}).getFloats(v1_2891);
          }
     public static void kernel_1780824482(int limit0, int v4_INT, float[] v0_2891, int v3_INT) {
          com.nativelibs4java.opencl.CLDevice.QueueProperties[] props = new com.nativelibs4java.opencl.CLDevice.QueueProperties[]{com.nativelibs4java.opencl.CLDevice.QueueProperties.ProfilingEnable};
          com.nativelibs4java.opencl.CLQueue queue = context.createDefaultQueue(props);
          String [] srcs = new String[]{kernel_1780824482};
          com.nativelibs4java.opencl.CLKernel kernels[] = context.createProgram( srcs ).createKernels();
          com.nativelibs4java.opencl.CLKernel kernel = kernels[0];
          int _v4_INT = v4_INT;
          com.nativelibs4java.opencl.CLBuffer _v0_2891 = context.createBuffer(com.nativelibs4java.opencl.CLMem.Usage.InputOutput, org.bridj.Pointer.pointerToFloats(v0_2891), true);
          int _v3_INT = v3_INT;
          kernel.setArgs(new Object[]{limit0,  _v4_INT,  _v0_2891,  _v3_INT});
          int required0 = (limit0 - v4_INT / 1);
          com.nativelibs4java.opencl.CLEvent[] eventsToWaitFor= new com.nativelibs4java.opencl.CLEvent[]{null};
          com.nativelibs4java.opencl.CLEvent clEvent1 = kernel.enqueueNDRange(queue, new int[]{required0}, eventsToWaitFor);
          queue.finish();
          _v0_2891.read(queue, new com.nativelibs4java.opencl.CLEvent[]{ clEvent1}).getFloats(v0_2891);
          }
     }

