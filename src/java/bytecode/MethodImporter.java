/*
 * Parallelising JVM Compiler
 *
 * Copyright 2010 Peter Calvert, University of Cambridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package bytecode;

import graph.Annotation;
import graph.BasicBlock;
import graph.Block;
import graph.ClassNode;
import graph.Method;
import graph.Modifier;
import graph.Type;
import graph.instructions.Arithmetic;
import graph.instructions.ArrayLength;
import graph.instructions.Branch;
import graph.instructions.Call;
import graph.instructions.CheckCast;
import graph.instructions.Compare;
import graph.instructions.Condition;
import graph.instructions.Constant;
import graph.instructions.Convert;
import graph.instructions.Increment;
import graph.instructions.InstanceOf;
import graph.instructions.Instruction;
import graph.instructions.Negate;
import graph.instructions.NewArray;
import graph.instructions.NewMultiArray;
import graph.instructions.NewObject;
import graph.instructions.Producer;
import graph.instructions.Read;
import graph.instructions.RestoreStack;
import graph.instructions.Return;
import graph.instructions.StackOperation;
import graph.instructions.Switch;
import graph.instructions.Throw;
import graph.instructions.TryCatch;
import graph.instructions.ValueReturn;
import graph.instructions.Write;
import graph.state.ArrayElement;
import graph.state.Field;
import graph.state.InstanceField;
import graph.state.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import analysis.dataflow.LiveVariable;
import debug.LinePropagator;

/**
 * Visits JVM bytecode, as represented by the ASM library, converting this into
 * a joint control-flow and data-flow graph that is used for analysis and
 * transformation.
 */
public class MethodImporter implements MethodVisitor {
  /**
   * Method being imported.
   */
  private Method                 method;

  /**
   * Working stack.
   */
  private Stack<Producer>        stack = new Stack<Producer>();

  /**
   * Current block to which instructions should be added.
   */
  private BasicBlock             current;

  /**
   * Mapping between JVM labels and graph blocks.
   */
  private Map<Label, BasicBlock> labels = new HashMap<Label, BasicBlock>();

  /**
   * Used for caching flattened order of instructions to ease exporting later.
   */
  private List<Instruction>      ordered = new LinkedList<Instruction>();

  /**
   * variable array - local to a method
   * Used to allocate the same variable object to each memory access in a block,
   * this allows variable names to be allocated.
   */
  private Map<Block, Map<Integer, Variable>> variables =
                                   new HashMap<Block, Map<Integer, Variable>>();

  /**
   * Constructs a method importer, starting on the given basic block.
   *
   * @param method    Method to import.
   */
  public MethodImporter(Method method) {
    this.method    = method;
  }

  /**
   * Called at start of code. Used to mark method as having an implementation.
   */
  @Override
  public void visitCode() {
    current = new BasicBlock();
    // This reference
    variables.put(current, new HashMap<Integer, Variable>());
    method.setImplementation(current);
  }

  /**
   * Called for each annotation defined on the method. Used to store these
   * annotations which are later used to determine which classes to transform.
   *
   * @param  desc     Descriptor for the annotation type.
   * @param  visible  Visibility of annotation (unused).
   * @return          AnnotationVisitor that imports the annotation properties.
   */
  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    Annotation a = new Annotation(Type.getType(desc));

    method.addAnnotation(a);

    return new AnnotationImporter(a);
  }
  
  /**
   * Called for each annotation defined on one of the method's parameters. Used
   * to store these annotations which are later used to guide alias analysis.
   * 
   * @param parameter Index of the parameter to annotate.
   * @param desc      Descriptor for the annotation type.
   * @param visible   Visibility of annotation (unused).
   * @return          AnnotationVisitor that imports the annotation properties.
   */
  @Override
  public AnnotationVisitor visitParameterAnnotation(final int parameter,
                                     final String desc, final boolean visible) {
    Annotation a = new Annotation(Type.getType(desc));

    if(method.getModifiers().contains(Modifier.STATIC)) {
      method.addParameterAnnotation(parameter, a);
    } else {
      method.addParameterAnnotation(parameter + 1, a);
    }

    return new AnnotationImporter(a);
  }

  /**
   * Imports instructions with no immediate operands.
   *
   * @param opcode Opcode.
   */
  @Override
  public void visitInsn(final int opcode) {
    Producer a, b, c, d;
    Type top;
  
    switch(opcode) {
      // Constants
      case Opcodes.ACONST_NULL:createConstant(null);                       break;
      case Opcodes.ICONST_M1:  createConstant(new Integer(-1));            break;
      case Opcodes.ICONST_0:   createConstant(new Integer(0));             break;
      case Opcodes.ICONST_1:   createConstant(new Integer(1));             break;
      case Opcodes.ICONST_2:   createConstant(new Integer(2));             break;
      case Opcodes.ICONST_3:   createConstant(new Integer(3));             break;
      case Opcodes.ICONST_4:   createConstant(new Integer(4));             break;
      case Opcodes.ICONST_5:   createConstant(new Integer(5));             break;
      case Opcodes.LCONST_0:   createConstant(new Long(0));                break;
      case Opcodes.LCONST_1:   createConstant(new Long(1));                break;
      case Opcodes.FCONST_0:   createConstant(new Float(0.0f));            break;
      case Opcodes.FCONST_1:   createConstant(new Float(1.0f));            break;
      case Opcodes.FCONST_2:   createConstant(new Float(2.0f));            break;
      case Opcodes.DCONST_0:   createConstant(new Double(0.0f));           break;
      case Opcodes.DCONST_1:   createConstant(new Double(1.0f));           break;
      
      // Binary Operations
      case Opcodes.IADD: createArithmetic(Arithmetic.Operator.ADD, Type.INT);    break;
      case Opcodes.LADD: createArithmetic(Arithmetic.Operator.ADD, Type.LONG);   break;
      case Opcodes.FADD: createArithmetic(Arithmetic.Operator.ADD, Type.FLOAT);  break;
      case Opcodes.DADD: createArithmetic(Arithmetic.Operator.ADD, Type.DOUBLE); break;
      case Opcodes.ISUB: createArithmetic(Arithmetic.Operator.SUB, Type.INT);    break;
      case Opcodes.LSUB: createArithmetic(Arithmetic.Operator.SUB, Type.LONG);   break;
      case Opcodes.FSUB: createArithmetic(Arithmetic.Operator.SUB, Type.FLOAT);  break;
      case Opcodes.DSUB: createArithmetic(Arithmetic.Operator.SUB, Type.DOUBLE); break;
      case Opcodes.IMUL: createArithmetic(Arithmetic.Operator.MUL, Type.INT);    break;
      case Opcodes.LMUL: createArithmetic(Arithmetic.Operator.MUL, Type.LONG);   break;
      case Opcodes.FMUL: createArithmetic(Arithmetic.Operator.MUL, Type.FLOAT);  break;
      case Opcodes.DMUL: createArithmetic(Arithmetic.Operator.MUL, Type.DOUBLE); break;
      case Opcodes.IDIV: createArithmetic(Arithmetic.Operator.DIV, Type.INT);    break;
      case Opcodes.LDIV: createArithmetic(Arithmetic.Operator.DIV, Type.LONG);   break;
      case Opcodes.FDIV: createArithmetic(Arithmetic.Operator.DIV, Type.FLOAT);  break;
      case Opcodes.DDIV: createArithmetic(Arithmetic.Operator.DIV, Type.DOUBLE); break;
      case Opcodes.IREM: createArithmetic(Arithmetic.Operator.REM, Type.INT);    break;
      case Opcodes.LREM: createArithmetic(Arithmetic.Operator.REM, Type.LONG);   break;
      case Opcodes.FREM: createArithmetic(Arithmetic.Operator.REM, Type.FLOAT);  break;
      case Opcodes.DREM: createArithmetic(Arithmetic.Operator.REM, Type.DOUBLE); break;
      case Opcodes.IAND: createArithmetic(Arithmetic.Operator.AND, Type.INT);    break;
      case Opcodes.LAND: createArithmetic(Arithmetic.Operator.AND, Type.LONG);   break;
      case Opcodes.IOR : createArithmetic(Arithmetic.Operator.OR , Type.INT);    break;
      case Opcodes.LOR : createArithmetic(Arithmetic.Operator.OR , Type.LONG);   break;
      case Opcodes.IXOR: createArithmetic(Arithmetic.Operator.XOR, Type.INT);    break;
      case Opcodes.LXOR: createArithmetic(Arithmetic.Operator.XOR, Type.LONG);   break;
      case Opcodes.ISHL: createArithmetic(Arithmetic.Operator.SHL, Type.INT);    break;
      case Opcodes.LSHL: createArithmetic(Arithmetic.Operator.SHL, Type.LONG);   break;
      case Opcodes.ISHR: createArithmetic(Arithmetic.Operator.SHR, Type.INT);    break;
      case Opcodes.LSHR: createArithmetic(Arithmetic.Operator.SHR, Type.LONG);   break;
      case Opcodes.IUSHR:createArithmetic(Arithmetic.Operator.USHR, Type.INT);   break;
      case Opcodes.LUSHR:createArithmetic(Arithmetic.Operator.USHR, Type.LONG);  break;

      case Opcodes.LCMP:    createCompare(false, Type.LONG);              break;
      case Opcodes.FCMPL:   createCompare(false, Type.FLOAT);             break;
      case Opcodes.FCMPG:   createCompare(true,  Type.FLOAT);             break;
      case Opcodes.DCMPL:   createCompare(false, Type.DOUBLE);            break;
      case Opcodes.DCMPG:   createCompare(true,  Type.DOUBLE);            break;
      case Opcodes.INEG:    createNegate();                               break;
      case Opcodes.LNEG:    createNegate();                               break;
      case Opcodes.FNEG:    createNegate();                               break;
      case Opcodes.DNEG:    createNegate();                               break;
      case Opcodes.I2L:     createConvert(Type.LONG);                     break;
      case Opcodes.I2F:     createConvert(Type.FLOAT);                    break;
      case Opcodes.I2D:     createConvert(Type.DOUBLE);                   break;
      case Opcodes.I2B:     createConvert(Type.BYTE);                     break;
      case Opcodes.I2C:     createConvert(Type.CHAR);                     break;
      case Opcodes.I2S:     createConvert(Type.SHORT);                    break;
      case Opcodes.L2I:     createConvert(Type.INT);                      break;
      case Opcodes.L2F:     createConvert(Type.FLOAT);                    break;
      case Opcodes.L2D:     createConvert(Type.DOUBLE);                   break;
      case Opcodes.F2I:     createConvert(Type.INT);                      break;
      case Opcodes.F2L:     createConvert(Type.LONG);                     break;
      case Opcodes.F2D:     createConvert(Type.DOUBLE);                   break;
      case Opcodes.D2I:     createConvert(Type.INT);                      break;
      case Opcodes.D2F:     createConvert(Type.FLOAT);                    break;
      case Opcodes.D2L:     createConvert(Type.LONG);                     break;
      case Opcodes.IALOAD:  createArrayRead(Type.INT);                    break;
      case Opcodes.LALOAD:  createArrayRead(Type.LONG);                   break;
      case Opcodes.FALOAD:  createArrayRead(Type.FLOAT);                  break;
      case Opcodes.DALOAD:  createArrayRead(Type.DOUBLE);                 break;
      case Opcodes.AALOAD:  createArrayRead(Type.getFreshRef());          break;
      case Opcodes.BALOAD:  createArrayRead(Type.BYTE);                   break;
      case Opcodes.CALOAD:  createArrayRead(Type.CHAR);                   break;
      case Opcodes.SALOAD:  createArrayRead(Type.SHORT);                  break;
      case Opcodes.IASTORE: createArrayWrite(Type.INT);                   break;
      case Opcodes.LASTORE: createArrayWrite(Type.LONG);                  break;
      case Opcodes.FASTORE: createArrayWrite(Type.FLOAT);                 break;
      case Opcodes.DASTORE: createArrayWrite(Type.DOUBLE);                break;
      case Opcodes.AASTORE: createArrayWrite(Type.getFreshRef());         break;
      case Opcodes.BASTORE: createArrayWrite(Type.BYTE);                  break;
      case Opcodes.CASTORE: createArrayWrite(Type.CHAR);                  break;
      case Opcodes.SASTORE: createArrayWrite(Type.SHORT);                 break;
      case Opcodes.IRETURN: createReturn(Type.INT);                       break;
      case Opcodes.LRETURN: createReturn(Type.LONG);                      break;
      case Opcodes.FRETURN: createReturn(Type.FLOAT);                     break;
      case Opcodes.DRETURN: createReturn(Type.DOUBLE);                    break;
      case Opcodes.ARETURN: createReturn(Type.REF);                       break;
      case Opcodes.RETURN:  createReturn(null);                           break;
      case Opcodes.ATHROW:  createThrow();                                break;
      
      // Array Length
      case Opcodes.ARRAYLENGTH: ordered.add(stack.push(new ArrayLength(stack.pop()))); break;
      
      // Swap
      case Opcodes.SWAP:
        a = stack.pop();
        b = stack.pop();
        
        stack.push(a);
        stack.push(b);
        
        ordered.add(new StackOperation(StackOperation.Sort.SWAP));

        break;
        
      // Duplicates
      case Opcodes.DUP:
        stack.push(stack.peek());
        ordered.add(new StackOperation(StackOperation.Sort.DUP));
        break;
      case Opcodes.DUP2:
        top = stack.peek().getType();
        
        // Type 2 Values
        if(top.getSize() == 2) {
            stack.push(stack.peek());
        // Type 1 Values
        } else {
            b = stack.pop();
            a = stack.pop();
            
            stack.push(a);
            stack.push(b);
            stack.push(a);
            stack.push(b);
        }

        ordered.add(new StackOperation(StackOperation.Sort.DUP2));
        
        break;
      case Opcodes.DUP_X1:
        b = stack.pop();
        a = stack.pop();

        stack.push(b);
        stack.push(a);
        stack.push(b);

        ordered.add(new StackOperation(StackOperation.Sort.DUP_X1));

        break;
      case Opcodes.DUP_X2:
        top = stack.peek().getType();

        // Type 2 Values
        if(top.getSize() == 2) {
          b = stack.pop();
          a = stack.pop();

          stack.push(b);
          stack.push(a);
          stack.push(b);
        // Type 1 Values
        } else {
          c = stack.pop();
          b = stack.pop();
          a = stack.pop();

          stack.push(c);
          stack.push(a);
          stack.push(b);
          stack.push(c);
        }

        ordered.add(new StackOperation(StackOperation.Sort.DUP_X2));

        break;
      
      // Pops
      case Opcodes.POP:
        stack.pop();
        ordered.add(new StackOperation(StackOperation.Sort.POP));
        break;
      case Opcodes.POP2:
        top = stack.peek().getType();
        
        // Type 2 Values
        if(top.getSize() == 2) {
          stack.pop();
        // Type 1 Values
        } else {
          stack.pop();
          stack.pop();
        }

        ordered.add(new StackOperation(StackOperation.Sort.POP2));
        
        break;
     
      // TODO: DUP2_X1, DUP2_X2, MONITORENTER, MONITOREXIT
      case Opcodes.MONITORENTER: throw new RuntimeException("Not supported yet: visitInsn: MONITORENTER");
      case Opcodes.MONITOREXIT:  throw new RuntimeException("Not supported yet: visitInsn: MONITOREXIT");
      case Opcodes.DUP2_X1:      throw new RuntimeException("Not supported yet: visitInsn: DUP2_X1");
      case Opcodes.DUP2_X2:      throw new RuntimeException("Not supported yet: visitInsn: DUP2_X2");
      default:                   throw new RuntimeException("Not supported yet: visitInsn: " + opcode);
    }
  }

  /**
   * Imports constant loads from the constant pool.
   *
   * @param constant Constant to be loaded.
   */
  @Override
  public void visitLdcInsn(final Object constant) {
    createConstant(constant);
  }

  /**
   * Imports instructions with a single integer operand (byte push, short push
   * and allocation of primitive arrays).
   *
   * @param opcode  Opcode.
   * @param operand Integer operand.
   */
  @Override
  public void visitIntInsn(final int opcode, final int operand) {
    switch(opcode) {
      // Constants
      case Opcodes.BIPUSH: createConstant(new Byte((byte) operand));      break;
      case Opcodes.SIPUSH: createConstant(new Short((short) operand));    break;
      
      // New Array (Primitive)
      case Opcodes.NEWARRAY:
        Type type = null;
        
        switch(operand) {
          case Opcodes.T_BOOLEAN: type = Type.BOOL;   break;
          case Opcodes.T_CHAR:    type = Type.CHAR;   break;
          case Opcodes.T_FLOAT:   type = Type.FLOAT;  break;
          case Opcodes.T_DOUBLE:  type = Type.DOUBLE; break;
          case Opcodes.T_BYTE:    type = Type.BYTE;   break;
          case Opcodes.T_SHORT:   type = Type.SHORT;  break;
          case Opcodes.T_INT:     type = Type.INT;    break;
          case Opcodes.T_LONG:    type = Type.LONG;   break;
        }
        
        ordered.add(stack.push(new NewArray(type, stack.pop())));
        break;
    }
  }

  /**
   * Imports instructions with a single type operand (allocation of reference
   * arrays, allocation of objects, casting and type checks).
   *
   * @param opcode     Opcode.
   * @param descriptor String descriptor of the type operand.
   */
  @Override
  public void visitTypeInsn(final int opcode, final String descriptor) {
    Type type = Type.getObjectType(descriptor);

    switch(opcode) {
      // News
      case Opcodes.ANEWARRAY: ordered.add(stack.push(new NewArray(type, stack.pop())));  break;
      case Opcodes.NEW:       ordered.add(stack.push(new NewObject(type)));              break;

      // Type Checks
      case Opcodes.CHECKCAST: ordered.add(stack.push(new CheckCast(stack.pop(), type))); break;
      case Opcodes.INSTANCEOF:ordered.add(stack.push(new InstanceOf(stack.pop(), type)));break;
    }
  }

  /**
   * Imports multiple dimension array allocations.
   *
   * @param descriptor Descriptor of the type.
   * @param dimensions Number of dimensions.
   */
  @Override
  public void visitMultiANewArrayInsn(final String descriptor, final int dimensions) {
    Producer[] counts = new Producer[dimensions];

    for(int i = dimensions - 1; i >= 0; i--) {
      counts[i] = stack.pop();
    }

    ordered.add(stack.push(new NewMultiArray(
      Type.getObjectType(descriptor),
      counts
    )));
  }

  /**
   * Imports variable load and store operations, as well as RET?!?
   *
   * @param opcode Opcode.
   * @param var    Variable index.
   */
  @Override
  public void visitVarInsn(final int opcode, final int var) {
    switch(opcode) {
      case Opcodes.ILOAD:  createVarRead(var, Type.INT);                  break;
      case Opcodes.LLOAD:  createVarRead(var, Type.LONG);                 break;
      case Opcodes.FLOAD:  createVarRead(var, Type.FLOAT);                break;
      case Opcodes.DLOAD:  createVarRead(var, Type.DOUBLE);               break;
      case Opcodes.ALOAD:  createVarRead(var, Type.getFreshRef());        break;
      case Opcodes.ISTORE: createVarWrite(var, Type.INT);                 break;
      case Opcodes.LSTORE: createVarWrite(var, Type.LONG);                break;
      case Opcodes.FSTORE: createVarWrite(var, Type.FLOAT);               break;
      case Opcodes.DSTORE: createVarWrite(var, Type.DOUBLE);              break;
      case Opcodes.ASTORE: createVarWrite(var, Type.getFreshRef());       break;
      
      // TODO: RET (paired with JSR)
      case Opcodes.RET: throw new RuntimeException("visitVarInsn: RET");
    }
  }

  /**
   * Imports variable increment instructions (and decrements by specifying
   * negative increment values).
   *
   * @param var       Variable index.
   * @param increment Amount of increment.
   */
  @Override
  public void visitIincInsn(final int var, final int increment) {
    Increment i = new Increment(getVariable(current, var, Type.INT), increment);

    current.getStateful().add(i);
    ordered.add(i);
  }

  /**
   * Imports field instructions (put and get), both for static and instance
   * fields.
   *
   * @param opcode Opcode.
   * @param owner  Class containing the field.
   * @param name   Name of field.
   * @param desc   Type descriptor of field.
   */
  @Override
  public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
    Field f = ClassNode.getClass(owner).getField(name, desc);

    if(((opcode == Opcodes.GETSTATIC) || (opcode == Opcodes.PUTSTATIC)) !=
            f.getModifiers().contains(Modifier.STATIC)) {
      throw new RuntimeException("Field staticness conflicts with instruction");
    }

    switch(opcode) {
      // Loads
      case Opcodes.GETSTATIC:
      case Opcodes.GETFIELD:  createFieldRead(f);  break;
      // Stores
      case Opcodes.PUTSTATIC:
      case Opcodes.PUTFIELD:  createFieldWrite(f); break;
    }
  }

  /**
   * Imports method calls, storing the type of invokation (virtual, static,
   * special and interface).
   *
   * @param opcode     Opcode.
   * @param owner      Class containing the method.
   * @param name       Name of the method.
   * @param descriptor Descriptor of the method.
   */
  @Override
  public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor) {
    // Find relevant class and method.
    ClassNode clazz = ClassNode.getClass(owner);
    Method    m     = clazz.getMethod(name, descriptor);

    int thisOffset = (opcode == Opcodes.INVOKESTATIC) ? 0 : 1;

    // Pop arguments off stack (remembering to pass 'this' for non-statics).
    Producer[] arguments = new Producer[m.getParameterCount() + thisOffset];
    List<Type> argTypes = m.getParameters();
    
    for(int i = m.getParameterCount() - 1; i >= 0; i--) {
      Producer arg = stack.pop();

      arg.getType().unify(argTypes.get(i), false);
      arguments[i + thisOffset] = arg;
    }

    // 'this' Argument
    if(thisOffset == 1) {
      Producer arg = stack.pop();
      arg.getType().unify(Type.getObjectType(owner));
      arguments[0] = arg;
    }

    // Call type
    Call.Sort sort = null;

    switch(opcode) {
      case Opcodes.INVOKEINTERFACE: sort = Call.Sort.INTERFACE; break;
      case Opcodes.INVOKESPECIAL:   sort = Call.Sort.SPECIAL;   break;
      case Opcodes.INVOKESTATIC:    sort = Call.Sort.STATIC;    break;
      case Opcodes.INVOKEVIRTUAL:   sort = Call.Sort.VIRTUAL;   break;
    }
    
    // Create instruction and update stack etc.
    Call c = new Call(arguments, m, sort);
    
    if(Type.getReturnType(descriptor).getSort() != null)
      stack.push(c);
      
    current.getStateful().add(c);
    ordered.add(c);
  }

  /**
   * Imports branch instructions, both conditional and unconditional.
   *
   * @param opcode Opcode.
   * @param label  Destination of branch.
   */
  @Override
  public void visitJumpInsn(final int opcode, final Label label) {
    BasicBlock section = getBasicBlock(label);
      
    switch(opcode) {
      // Unconditional Branch
      case Opcodes.GOTO: finishBlock(section); setCurrent(null); break;
      
      // Zero Test
      case Opcodes.IFEQ: createZeroCondition(Condition.Operator.EQ, section); break;
      case Opcodes.IFNE: createZeroCondition(Condition.Operator.NE, section); break;
      case Opcodes.IFLT: createZeroCondition(Condition.Operator.LT, section); break;
      case Opcodes.IFGE: createZeroCondition(Condition.Operator.GE, section); break;
      case Opcodes.IFGT: createZeroCondition(Condition.Operator.GT, section); break;
      case Opcodes.IFLE: createZeroCondition(Condition.Operator.LE, section); break;
      
      // Integer Comparison
      case Opcodes.IF_ICMPEQ: createCondition(Condition.Operator.EQ, section); break;
      case Opcodes.IF_ICMPNE: createCondition(Condition.Operator.NE, section); break;
      case Opcodes.IF_ICMPLT: createCondition(Condition.Operator.LT, section); break;
      case Opcodes.IF_ICMPGE: createCondition(Condition.Operator.GE, section); break;
      case Opcodes.IF_ICMPGT: createCondition(Condition.Operator.GT, section); break;
      case Opcodes.IF_ICMPLE: createCondition(Condition.Operator.LE, section); break;

      // Reference Comparisons
      case Opcodes.IF_ACMPEQ: createCondition(Condition.Operator.EQ, section); break;
      case Opcodes.IF_ACMPNE: createCondition(Condition.Operator.NE, section); break;
      case Opcodes.IFNULL:    createNullCondition(Condition.Operator.EQ, section); break;
      case Opcodes.IFNONNULL: createNullCondition(Condition.Operator.NE, section); break;
      
      // TODO: JSR (paired with RET)
      case Opcodes.JSR: System.err.println("visitJumpInsn: JSR");
    }
  }

  /**
   * Imports 'table' switch instructions (i.e. those where the integer cases
   * are contiguous from a minimum to a maximum).
   *
   * @param min    Minimum case key.
   * @param max    Maximum case key.
   * @param dflt   Default case destination label.
   * @param labels Destination labels for the cases.
   */
  @Override
  public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label[] labels) {
    Map<Integer, Block> map = new HashMap<Integer, Block>();
    
    // Construct Mapping
    for(int i = min; i <= max; i++) {
      map.put(new Integer(i), getBasicBlock(labels[i - min]));
    }

    createSwitch(map, getBasicBlock(dflt));
  }

  /**
   * Imports 'lookup' switch instructions (i.e. those where the integer cases
   * are explicitly named).
   *
   * @param dflt   Default case destination label.
   * @param keys   Keys for cases.
   * @param labels Destination labels for the cases.
   */
  @Override
  public void visitLookupSwitchInsn(final Label dflt, final int keys[], final Label labels[]) {
    Map<Integer, Block> map = new HashMap<Integer, Block>();
    
    // Construct Mapping
    for(int i = 0; i < keys.length; i++) {
      map.put(new Integer(keys[i]), getBasicBlock(labels[i]));
    }

    createSwitch(map, getBasicBlock(dflt));
  }

  /**
   * Imports 'try ... catch blocks'. These are represented in the object graph
   * as branch instructions, and therefore appear at the end of a basic block.
   *
   * @param start   Start of try region.
   * @param end     End of try region.
   * @param handler Exception handler (catch block).
   * @param type    Name of the exception caught.
   */
  @Override
  public void visitTryCatchBlock(final Label start, final Label end,
                                       final Label handler, final String type) {
    TryCatch t = new TryCatch(
      getBasicBlock(start),
      getBasicBlock(end),
      getBasicBlock(handler),
      Type.getObjectType(type)
    );

    current.setBranch(t);
    ordered.add(t);

    finishBlock(null);
  }

  /**
   * Visited for each label defined in the bytecode. This is used to switch
   * to the new basic block, so instructions are correctly allocated.
   *
   * @param label  New label that is starting
   */
  @Override
  public void visitLabel(final Label label) {
    BasicBlock next = getBasicBlock(label);

    if(current != null) {
      finishBlock(next);
    } else {
      setCurrent(next);
    }
  }

  /**
   * Visits a new frame, giving details about the `delta' (including some
   * limited type information). This is not used by our importer.
   *
   * @param type   Type of delta (i.e. how it is described).
   * @param nLocal Change for local variable stack.
   * @param local  Types for the change.
   * @param nStack Change for operand stack.
   * @param stack  Types for the change.
   */
  @Override
  public void visitFrame(int type, int nLocal, Object[] local, int nStack,
                                                               Object[] stack) {
    // Nothing
  }

  /**
   * Visited when the 'method' is part of an annotation class, to allow the
   * default value of the annotation method to be considered. This is not needed
   * here since annotation classes are never imported.
   *
   * @return       Annotation visitor to consider the default value of this
   *               method. Here <code>null</code>.
   */
  @Override
  public AnnotationVisitor visitAnnotationDefault() {
    return null;
  }

  /**
   * Visited for attributes. Unclear what these are, and aren't needed.
   *
   * @param attr   Attribute.
   */
  @Override
  public void visitAttribute(final Attribute attr) {
    // Nothing
  }

  /**
   * Gives analysis of operand and local variable stacks, not used for import.
   *
   * @param maxStack  Maximum size of stack.
   * @param maxLocals Maximum number of local variables.
   */
  @Override
  public void visitMaxs(final int maxStack, final int maxLocals) {
    // Nothing
  }

  /**
   * Called at end of method visit. Used to perform live variable analysis which
   * propogates variable types between blocks.
   */
  @Override
  public void visitEnd() {
    // Only do stuff for methods with code (not abstract or native).
    if(method.getImplementation() != null) {
      method.getImplementation().accept(new LinePropagator());
      //Perform live variable analysis
      new LiveVariable(method.getImplementation());
    }
  }

  /**
   * Uses line number debug information to mark BasicBlocks. This is solely for
   * debugging purposes.
   *
   * @param line       Line number in source file.
   * @param label      Corresponding label.
   */
  @Override
  public void visitLineNumber(final int line, final Label label) {
    getBasicBlock(label).setLineNumber(line);
  }

  /**
   * Uses debug information regarding names of local variables to update the
   * relevant object with this information. This is solely for debugging
   * purposes.
   *
   * @param name       Name of the variable.
   * @param descriptor Type descriptor of the variable.
   * @param signature
   * @param start
   * @param end
   * @param index      Index of the variable.
   */
  @Override
  public void visitLocalVariable(
    final String name,
    final String descriptor,
    final String signature,
    final Label start,
    final Label end,
    final int index
  ) {
    Queue<Block> blocks = new LinkedList<Block>();
    Set<Block> visited = new HashSet<Block>();
    Block last = getBasicBlock(end);

    // Start
    blocks.add(getBasicBlock(start));

    // Update variable name for all blocks.
    while(!blocks.isEmpty()) {
      Block b = blocks.remove();

      visited.add(b);

      getVariable(b, index, null).setName(name);

      // Update successors until end label.
      for(Block s : b.getSuccessors()) {
        if((s != last) && (s != null) && !visited.contains(s)) {
          blocks.add(s);
        }
      }
    }
  }

  /**
   * Returns a unique variable per index per block. This allows variable names
   * to be retrospectively added.
   *
   * @param block      Block
   * @param index      Index of the variable.
   * @param type       Type of the variable in the given context (this will be
   *                   adjusted to equal the variable type, provided the
   *                   variable type is specialisation of it). For no context,
   *                   this can be supplied as <code>null</code>.
   * @return           Variable.
   */
  public Variable getVariable(Block block, int index, Type type) {
    // Block must exist before getting variables for it.
    if(!variables.containsKey(block)) {
      throw new RuntimeException(
        "getVariable called before getBasicBlock for " + block + "."
      );
    }

    // Get variable.
    Variable var = variables.get(block).get(new Integer(index));

    if(var == null) {
      var = new Variable(index, type);
      variables.get(block).put(new Integer(index), var);
    }

    // Update context type if supplied.
    if(type != null) {
      type.unify(var.getType());
    }

    return var;
  }

  /**
   * Returns a basic block corresponding to a raw label in the bytecode.
   *
   * @param  label Label to look up.
   * @return       Corresponding basic block.
   */
  public BasicBlock getBasicBlock(final Label label) {
    BasicBlock section = labels.get(label);

    if(section == null) {
      section = new BasicBlock();
      labels.put(label, section);
      variables.put(section, new HashMap<Integer, Variable>());
    }

    return section;
  }
// adi, we should reverse branch here
  /**
   * Finishes the current block.
   *
   * @param next   Next block to chain on (<code>null</code> for none).
   */
  private void finishBlock(BasicBlock next) {
    // Cache instruction ordering and reset.
    BlockExporter.cacheCode(current, ordered);
    ordered = new LinkedList<Instruction>();

    // Set final element of basic block.
    current.setNext(next);

    // Declare values that will be emitted and expected by successors.
    current.setValuesOut(stack);
    
    for(Block b : current.getSuccessors()) {
      if(b instanceof BasicBlock) {
        ((BasicBlock) b).setTypesIn(stack);
      } else {
        throw new RuntimeException("Non-basic block not expected at import-time.");
      }
    }
    
    // Move onto next basic block.
    setCurrent(next);
  }

  /**
   * Sets the current block to be that given.
   *
   * @param block  Block to set.
   */
  private void setCurrent(BasicBlock block) {
    if(block == null) {
      stack = null;
      current = null;
    } else {
      stack = new Stack<Producer>();
      current = block;

      // Add restore stack instructions.
      int index = 0;

      for(Type t : current.getTypesIn()) {
        stack.push(new RestoreStack(index++, t));
      }
    }
  }

  /**
   * Creation of arithmetic instruction.
   */
  private void createArithmetic(Arithmetic.Operator operator, Type type) {
    Producer operandB = stack.pop();
    Producer operandA = stack.pop();

    assert((type == operandA.getType()) && (operandA.getType() == operandB.getType()));

    ordered.add(stack.push(new Arithmetic(operator, operandA, operandB)));
  }

  /**
   * Creation of floating point compare instruction.
   */
  private void createCompare(boolean variant, Type type) {
    Producer operandB = stack.pop();
    Producer operandA = stack.pop();

    ordered.add(stack.push(new Compare(variant, type, operandA, operandB)));
  }

  /**
   * Creation of constant instruction
   */
  private void createConstant(Object object) {
    ordered.add(stack.push(new Constant(object)));
  }

  /**
   * Creation of negation operator.
   */
  private void createNegate() {
    ordered.add(stack.push(new Negate(stack.pop())));
  }

  /**
   * Creation of primitive type conversion instruction.
   */
  private void createConvert(Type type) {
    ordered.add(stack.push(new Convert(stack.pop(), type)));
  }

  /**
   * Creation of array read instruction.
   */
  private void createArrayRead(Type type) {
    Producer index = stack.pop();
    Producer array = stack.pop();

    Read r = new Read(new ArrayElement(array, index, type));

    current.getStateful().add(r);
    stack.push(r);
    ordered.add(r);
  }

  /**
   * Creation of array write instruction.
   */
  private void createArrayWrite(Type type) {
    Producer value = stack.pop();
    Producer index = stack.pop();
    Producer array = stack.pop();

    Write w = new Write(new ArrayElement(array, index, type), value);

    current.getStateful().add(w);
    ordered.add(w);
  }

  /**
   * Creation of return instruction.
   */
  private void createReturn(Type type) {
    Branch b = (type == null) ? new Return() : new ValueReturn(stack.pop());

    current.setBranch(b);
    ordered.add(b);

    finishBlock(null);
  }

  /**
   * Creation of throw instruction.
   */
  private void createThrow() {
    Throw t = new Throw(stack.pop());

    current.setBranch(t);
    ordered.add(t);

    finishBlock(null);
  }

  /**
   * Creation of variable read instruction.
   */
  private void createVarRead(int var, Type type) {
    Read r = new Read(getVariable(current, var, type));

    current.getStateful().add(r);
    stack.push(r);
    ordered.add(r);
  }

  /**
   * Creation of variable write instruction.
   */
  private void createVarWrite(int var, Type type) {

    Write r = new Write(getVariable(current, var, type), stack.pop());

    current.getStateful().add(r);
    ordered.add(r);
  }

  /**
   * Creation of field read instruction.
   */
  private void createFieldRead(Field field) {
    Read r;

    if(field.getModifiers().contains(Modifier.STATIC)) {
      r = new Read(field);
    } else {
      r = new Read(new InstanceField(stack.pop(), field));
    }

    current.getStateful().add(r);
    stack.push(r);
    ordered.add(r);
  }

  /**
   * Creation of field write instruction.
   */
  private void createFieldWrite(Field field) {
    Write w;

    if(field.getModifiers().contains(Modifier.STATIC)) {
      w = new Write(field, stack.pop());
    } else {
      Producer value  = stack.pop();
      Producer object = stack.pop();
      
      w = new Write(new InstanceField(object, field), value);
    }

    current.getStateful().add(w);
    ordered.add(w);
  }

  /**
   * Creation of comparison with zero.
   */
  private void createZeroCondition(Condition.Operator operator, BasicBlock destination) {
    Producer operandB = new Constant(new Integer(0));
    Producer operandA = stack.pop();
    Condition c = new Condition(operator, destination, operandA, operandB);

    current.setBranch(c);
    ordered.add(c);

    // New basic block, and copy across variables.
    BasicBlock next = new BasicBlock();
    variables.put(next, variables.get(current));
    finishBlock(next);
  }

  /**
   * Creation of comparison with NULL.
   */
  private void createNullCondition(Condition.Operator operator, BasicBlock destination) {
    Producer operandB = new Constant(null);
    Producer operandA = stack.pop();
    Condition c = new Condition(operator, destination, operandA, operandB);

    current.setBranch(c);
    ordered.add(c);

    // New basic block, and copy across variables.
    BasicBlock next = new BasicBlock();
    variables.put(next, variables.get(current));
    finishBlock(next);
  }

  /**
   * Creation of comparison.
   */
  private void createCondition(Condition.Operator operator, BasicBlock destination) {
    Producer operandB = stack.pop();
    Producer operandA = stack.pop();
    Condition c = new Condition(operator, destination, operandA, operandB);

    current.setBranch(c);
    ordered.add(c);

    // New basic block, and copy across variables.
    BasicBlock next = new BasicBlock();
    variables.put(next, variables.get(current));
    finishBlock(next);
  }

  /**
   * Creation of a switch branch.
   */
  private void createSwitch(Map<Integer, Block> destinations, BasicBlock dflt) {
    Switch s = new Switch(stack.pop(), destinations, dflt);

    current.setBranch(s);
    ordered.add(s);

    finishBlock(null);
  }
}
