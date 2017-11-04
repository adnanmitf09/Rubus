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

import graph.Block;
import graph.ClassNode;
import graph.CodeVisitor;
import graph.Method;
import graph.Type;
import graph.instructions.Arithmetic;
import graph.instructions.ArrayLength;
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

import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Converts graph node instructions into visits on a ASM method visitor.
 */
public class InstructionExporter extends CodeVisitor<Void> {
  /**
   * ASM method visitor.
   */
  private MethodVisitor mv;

  /**
   * Constructs an instance for exporting graph node instructions to the given
   * method visitor.
   *
   * @param mv     ASM method visitor.
   */
  public InstructionExporter(MethodVisitor mv) {
    this.mv = mv;
  }
  @Override
public Void visit(Instruction i) {System.err.println("FAIL OUT: " + i); return null;}

  /**
   * Ignores stack restoration instructions. This relies on this case being
   * handled correctly by a cached instruction order.
   *
   * @param instruction Stack restoration instruction.
   * @return            <code>null</code>
   */
  public Void visit(RestoreStack instruction) {
    // Nothing
    return null;
  }

  /**
   * Outputs stack operations (SWAP, DUP, etc.).
   *
   * @param instruction Stack operation.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(StackOperation instruction) {
    switch(instruction.getSort()) {
      case SWAP:    mv.visitInsn(Opcodes.SWAP);    break;
      case POP:     mv.visitInsn(Opcodes.POP);     break;
      case POP2:    mv.visitInsn(Opcodes.POP2);    break;
      case DUP:     mv.visitInsn(Opcodes.DUP);     break;
      case DUP2:    mv.visitInsn(Opcodes.DUP2);    break;
      case DUP_X1:  mv.visitInsn(Opcodes.DUP_X1);  break;
      case DUP_X2:  mv.visitInsn(Opcodes.DUP_X2);  break;
      case DUP2_X1: mv.visitInsn(Opcodes.DUP2_X1); break;
      case DUP2_X2: mv.visitInsn(Opcodes.DUP2_X2); break;
    }

    return null;
  }

  /**
   * Outputs constants into stack operations. This will use different JVM
   * instructions depending on whether there are short-hands for the given
   * constant etc.
   *
   * @param instruction Constant.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Constant instruction) {
    // NULL Pointer
    if(instruction.getConstant() == null) {
      mv.visitInsn(Opcodes.ACONST_NULL);
    // Integers
    } else if(instruction.getConstant() instanceof Integer) {
      Integer i = (Integer) instruction.getConstant();

      switch(i.intValue()) {
        case -1: mv.visitInsn(Opcodes.ICONST_M1); break;
        case  0: mv.visitInsn(Opcodes.ICONST_0);  break;
        case  1: mv.visitInsn(Opcodes.ICONST_1);  break;
        case  2: mv.visitInsn(Opcodes.ICONST_2);  break;
        case  3: mv.visitInsn(Opcodes.ICONST_3);  break;
        case  4: mv.visitInsn(Opcodes.ICONST_4);  break;
        case  5: mv.visitInsn(Opcodes.ICONST_5);  break;
        default: mv.visitLdcInsn(i);
      }
    // Longs
    } else if(instruction.getConstant() instanceof Long) {
      Long l = (Long) instruction.getConstant();

      if     (l.longValue() == 0) mv.visitInsn(Opcodes.LCONST_0);
      else if(l.longValue() == 1) mv.visitInsn(Opcodes.ICONST_1);
      else                        mv.visitLdcInsn(l);
    // Floats
    } else if(instruction.getConstant() instanceof Float) {
      Float f = (Float) instruction.getConstant();

      // Should be safe since 0 and 1 are both exactly representable in FP.
      if     (f.floatValue() == 0.0f) mv.visitInsn(Opcodes.FCONST_0);
      else if(f.floatValue() == 1.0f) mv.visitInsn(Opcodes.FCONST_1);
      else if(f.floatValue() == 2.0f) mv.visitInsn(Opcodes.FCONST_2);
      else                            mv.visitLdcInsn(f);
    // Doubles
    } else if(instruction.getConstant() instanceof Double) {
      Double d = (Double) instruction.getConstant();

      // Should be safe since 0 and 1 are both exactly representable in FP.
      if     (d.doubleValue() == 0.0f) mv.visitInsn(Opcodes.DCONST_0);
      else if(d.doubleValue() == 1.0f) mv.visitInsn(Opcodes.DCONST_1);
      else                             mv.visitLdcInsn(d);
    // Byte Pushes
    } else if(instruction.getConstant() instanceof Byte) {
      Byte b = (Byte) instruction.getConstant();

      mv.visitIntInsn(Opcodes.BIPUSH, b.intValue());
    // Short Pushes
    } else if(instruction.getConstant() instanceof Short) {
      Short s = (Short) instruction.getConstant();

      mv.visitIntInsn(Opcodes.SIPUSH, s.intValue());
    // Otherwise a constant pool load.
    } else {
      mv.visitLdcInsn(instruction.getConstant());
    }

    return null;
  }

  /**
   * Outputs array length instruction.
   *
   * @param instruction Array length instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(ArrayLength instruction) {
    mv.visitInsn(Opcodes.ARRAYLENGTH);
    return null;
  }

  /**
   * Outputs all arithmetic instructions, selecting the correct opcode for the
   * required type etc.
   *
   * @param instruction Arithmetic instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Arithmetic instruction) {
    switch(instruction.getOperator()) {
      case ADD: switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.IADD); break;
                  case LONG:   mv.visitInsn(Opcodes.LADD); break;
                  case FLOAT:  mv.visitInsn(Opcodes.FADD); break;
                  case DOUBLE: mv.visitInsn(Opcodes.DADD); break;
                }
                break;
      case SUB: switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.ISUB); break;
                  case LONG:   mv.visitInsn(Opcodes.LSUB); break;
                  case FLOAT:  mv.visitInsn(Opcodes.FSUB); break;
                  case DOUBLE: mv.visitInsn(Opcodes.DSUB); break;
                }
                break;
      case MUL: switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.IMUL); break;
                  case LONG:   mv.visitInsn(Opcodes.LMUL); break;
                  case FLOAT:  mv.visitInsn(Opcodes.FMUL); break;
                  case DOUBLE: mv.visitInsn(Opcodes.DMUL); break;
                }
                break;
      case DIV: switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.IDIV); break;
                  case LONG:   mv.visitInsn(Opcodes.LDIV); break;
                  case FLOAT:  mv.visitInsn(Opcodes.FDIV); break;
                  case DOUBLE: mv.visitInsn(Opcodes.DDIV); break;
                }
                break;
      case REM: switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.IREM); break;
                  case LONG:   mv.visitInsn(Opcodes.LREM); break;
                  case FLOAT:  mv.visitInsn(Opcodes.FREM); break;
                  case DOUBLE: mv.visitInsn(Opcodes.DREM); break;
                }
                break;
      case AND: switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.IAND); break;
                  case LONG:   mv.visitInsn(Opcodes.LAND); break;
                }
                break;
      case OR:  switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.IOR);  break;
                  case LONG:   mv.visitInsn(Opcodes.LOR);  break;
                }
                break;
      case XOR: switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.IXOR); break;
                  case LONG:   mv.visitInsn(Opcodes.LXOR); break;
                }
                break;
      case SHL: switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.ISHL); break;
                  case LONG:   mv.visitInsn(Opcodes.LSHL); break;
                }
                break;
      case SHR: switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.ISHR); break;
                  case LONG:   mv.visitInsn(Opcodes.LSHR); break;
                }
                break;
      case USHR:switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.IUSHR);break;
                  case LONG:   mv.visitInsn(Opcodes.LUSHR);break;
                }
                break;
    }
    
    return null;
  }

  /**
   * Outputs a comparison operation, selecting the opcode for longs, floats or
   * doubles, and also selecting the correct variant for floating-point
   * comparisons.
   *
   * @param instruction Compare instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Compare instruction) {
    if(!instruction.getVariant()) {
      switch(instruction.getCompareType().getSort()) {
        case LONG:   mv.visitInsn(Opcodes.LCMP);  break;
        case FLOAT:  mv.visitInsn(Opcodes.FCMPL); break;
        case DOUBLE: mv.visitInsn(Opcodes.DCMPL); break;
      }
    } else {
      switch(instruction.getCompareType().getSort()) {
        case LONG:   mv.visitInsn(Opcodes.LCMP);  break;
        case FLOAT:  mv.visitInsn(Opcodes.FCMPG); break;
        case DOUBLE: mv.visitInsn(Opcodes.DCMPG); break;
      }
    }

    return null;
  }

  /**
   * Outputs a conversion instruction, choosing the correct opcode for the two
   * types concerned.
   *
   * @param instruction Conversion instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Convert instruction) {
    switch(instruction.getOperand().getType().getSort()) {
      case INT: switch(instruction.getType().getSort()) {
                  case LONG:   mv.visitInsn(Opcodes.I2L); break;
                  case FLOAT:  mv.visitInsn(Opcodes.I2F); break;
                  case DOUBLE: mv.visitInsn(Opcodes.I2D); break;
                  case BYTE:   mv.visitInsn(Opcodes.I2B); break;
                  case CHAR:   mv.visitInsn(Opcodes.I2C); break;
                  case SHORT:  mv.visitInsn(Opcodes.I2S); break;
                }
                break;
      case LONG:switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.L2I); break;
                  case FLOAT:  mv.visitInsn(Opcodes.L2F); break;
                  case DOUBLE: mv.visitInsn(Opcodes.L2D); break;
                }
                break;
      case FLOAT:
                switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.F2I); break;
                  case LONG:   mv.visitInsn(Opcodes.F2L); break;
                  case DOUBLE: mv.visitInsn(Opcodes.F2D); break;
                }
                break;
      case DOUBLE:
                switch(instruction.getType().getSort()) {
                  case INT:    mv.visitInsn(Opcodes.D2I); break;
                  case LONG:   mv.visitInsn(Opcodes.D2L); break;
                  case FLOAT:  mv.visitInsn(Opcodes.D2F); break;
                }
                break;
    }
    
    return null;
  }

  /**
   * Outputs negation instruction, selecting correct variant for the type.
   *
   * @param instruction Negation instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Negate instruction) {
    switch(instruction.getType().getSort()) {
      case INT:    mv.visitInsn(Opcodes.INEG); break;
      case LONG:   mv.visitInsn(Opcodes.LNEG); break;
      case FLOAT:  mv.visitInsn(Opcodes.FNEG); break;
      case DOUBLE: mv.visitInsn(Opcodes.DNEG); break;
    }

    return null;
  }

  /**
   * Outputs a void return instruction.
   *
   * @param instruction Void return instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Return instruction) {
    mv.visitInsn(Opcodes.RETURN);
    return null;
  }

  /**
   * Outputs a return instruction, selecting the correct type variant.
   *
   * @param instruction Return instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(ValueReturn instruction) {
    switch(instruction.getType().getSort()) {
      case LONG:   mv.visitInsn(Opcodes.LRETURN); break;
      case FLOAT:  mv.visitInsn(Opcodes.FRETURN); break;
      case DOUBLE: mv.visitInsn(Opcodes.DRETURN); break;
      case REF:    mv.visitInsn(Opcodes.ARETURN); break;
      default:     mv.visitInsn(Opcodes.IRETURN); break;
    }

    return null;
  }

  /**
   * Outputs a read operation, choosing both the correct opcode family (variable
   * read, array load, field get, etc.) and type.
   *
   * @param instruction Read instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Read instruction) {
    // Variable Reads
    if(instruction.getState() instanceof Variable) {
      Variable v = (Variable) instruction.getState();

      switch(v.getType().getSort()) {
        case LONG:   mv.visitVarInsn(Opcodes.LLOAD, v.getIndex()); break;
        case FLOAT:  mv.visitVarInsn(Opcodes.FLOAD, v.getIndex()); break;
        case DOUBLE: mv.visitVarInsn(Opcodes.DLOAD, v.getIndex()); break;
        case REF:    mv.visitVarInsn(Opcodes.ALOAD, v.getIndex()); break;
        default:     mv.visitVarInsn(Opcodes.ILOAD, v.getIndex()); break;
      }
    // Array Loads
    } else if(instruction.getState() instanceof ArrayElement) {
      switch(instruction.getState().getType().getSort()) {
        case INT:    mv.visitInsn(Opcodes.IALOAD); break;
        case LONG:   mv.visitInsn(Opcodes.LALOAD); break;
        case FLOAT:  mv.visitInsn(Opcodes.FALOAD); break;
        case DOUBLE: mv.visitInsn(Opcodes.DALOAD); break;
        case REF:    mv.visitInsn(Opcodes.AALOAD); break;
        case BYTE:   mv.visitInsn(Opcodes.BALOAD); break;
        case BOOL:   mv.visitInsn(Opcodes.BALOAD); break;
        case CHAR:   mv.visitInsn(Opcodes.CALOAD); break;
        case SHORT:  mv.visitInsn(Opcodes.SALOAD); break;
      }
    // Static Reads
    } else if(instruction.getState() instanceof Field) {
      Field f = (Field) instruction.getState();

      mv.visitFieldInsn(
        Opcodes.GETSTATIC,
        f.getOwner().getName(),
        f.getName(),
        f.getType().getDescriptor()
      );
    // Field Reads
    } else if(instruction.getState() instanceof InstanceField) {
      Field f = (Field) ((InstanceField) instruction.getState()).getField();

      mv.visitFieldInsn(
        Opcodes.GETFIELD,
        f.getOwner().getName(),
        f.getName(),
        f.getType().getDescriptor()
      );
    }

    return null;
  }

  /**
   * Outputs a write operation, choosing both the correct opcode family
   * (variable write, array store, field put, etc.) and type.
   *
   * @param instruction Write instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Write instruction) {
    // Variable Stores
    if(instruction.getState() instanceof Variable) {
      Variable v = (Variable) instruction.getState();

      switch(v.getType().getSort()) {
        case LONG:   mv.visitVarInsn(Opcodes.LSTORE, v.getIndex()); break;
        case FLOAT:  mv.visitVarInsn(Opcodes.FSTORE, v.getIndex()); break;
        case DOUBLE: mv.visitVarInsn(Opcodes.DSTORE, v.getIndex()); break;
        case REF:    mv.visitVarInsn(Opcodes.ASTORE, v.getIndex()); break;
        default:     mv.visitVarInsn(Opcodes.ISTORE, v.getIndex()); break;
      }
    // Array Stores
    } else if(instruction.getState() instanceof ArrayElement) {
      switch(instruction.getState().getType().getSort()) {
        case INT:    mv.visitInsn(Opcodes.IASTORE); break;
        case LONG:   mv.visitInsn(Opcodes.LASTORE); break;
        case FLOAT:  mv.visitInsn(Opcodes.FASTORE); break;
        case DOUBLE: mv.visitInsn(Opcodes.DASTORE); break;
        case REF:    mv.visitInsn(Opcodes.AASTORE); break;
        case BYTE:   mv.visitInsn(Opcodes.BASTORE); break;
        case BOOL:   mv.visitInsn(Opcodes.BASTORE); break;
        case CHAR:   mv.visitInsn(Opcodes.CASTORE); break;
        case SHORT:  mv.visitInsn(Opcodes.SASTORE); break;
      }
    // Static Writes
    } else if(instruction.getState() instanceof Field) {
      Field f = (Field) instruction.getState();

      mv.visitFieldInsn(
        Opcodes.PUTSTATIC,
        f.getOwner().getName(),
        f.getName(),
        f.getType().getDescriptor()
      );
    // Field Writes
    } else if(instruction.getState() instanceof InstanceField) {
      Field f = (Field) ((InstanceField) instruction.getState()).getField();

      mv.visitFieldInsn(
        Opcodes.PUTFIELD,
        f.getOwner().getName(),
        f.getName(),
        f.getType().getDescriptor()
      );
    }

    return null;
  }

  /**
   * Outputs increment instruction (acts on variables).
   *
   * @param instruction Increment instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Increment instruction) {
    mv.visitIincInsn(
      ((Variable) instruction.getState()).getIndex(),
      instruction.getIncrement()
    );

    return null;
  }

  /**
   * Output conditional branch instructions, choosing between integer and
   * reference comparisons, and also abbreviating comparisons with 0 or
   * <code>null</code>.
   *
   * @param instruction Comparison instruction.
   * @return .          <code>null</code>
   */
  @Override
  public Void visit(Condition instruction) {
    Label label = BlockExporter.getLabel(instruction.getDestination());
    
    // Produce integer instructions
    if(instruction.getOperandA().getType().isIntBased()) {
      // Comparisons with zero
      if(instruction.getOperandB().equals(new Constant(new Integer(0)))) {
        switch(instruction.getOperator()) {
          case EQ: mv.visitJumpInsn(Opcodes.IFEQ, label); break;
          case NE: mv.visitJumpInsn(Opcodes.IFNE, label); break;
          case LT: mv.visitJumpInsn(Opcodes.IFLT, label); break;
          case GE: mv.visitJumpInsn(Opcodes.IFGE, label); break;
          case GT: mv.visitJumpInsn(Opcodes.IFGT, label); break;
          case LE: mv.visitJumpInsn(Opcodes.IFLE, label); break;
        }
      // Direct comparisons
      } else {
        switch(instruction.getOperator()) {
          case EQ: mv.visitJumpInsn(Opcodes.IF_ICMPEQ, label); break;
          case NE: mv.visitJumpInsn(Opcodes.IF_ICMPNE, label); break;
          case LT: mv.visitJumpInsn(Opcodes.IF_ICMPLT, label); break;
          case GE: mv.visitJumpInsn(Opcodes.IF_ICMPGE, label); break;
          case GT: mv.visitJumpInsn(Opcodes.IF_ICMPGT, label); break;
          case LE: mv.visitJumpInsn(Opcodes.IF_ICMPLE, label); break;
        }
      }
    // Produce reference instructions
    } else if(instruction.getOperandA().getType().getSort() == Type.Sort.REF) {
      // Comparisons with null
      if(instruction.getOperandB().equals(new Constant(null))) {
        switch(instruction.getOperator()) {
          case EQ: mv.visitJumpInsn(Opcodes.IFNULL, label); break;
          case NE: mv.visitJumpInsn(Opcodes.IFNONNULL, label); break;
        }
      // Direct comparisons
      } else {
        switch(instruction.getOperator()) {
          case EQ: mv.visitJumpInsn(Opcodes.IF_ACMPEQ, label); break;
          case NE: mv.visitJumpInsn(Opcodes.IF_ACMPNE, label); break;
        }
      }
    // Flag up others.
    } else {
      throw new RuntimeException("Attempt to export condition of unknown type.");
    }

    return null;
  }

  /**
   * Output call instructions, outputing the correct opcode for the type of
   * call.
   *
   * @param instruction Call instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Call instruction) {
    Method    m = instruction.getMethod();
    ClassNode c = m.getOwner();
    
    int opcode;

    switch(instruction.getSort()) {
      case INTERFACE: opcode = Opcodes.INVOKEINTERFACE; break;
      case VIRTUAL:   opcode = Opcodes.INVOKEVIRTUAL;   break;
      case SPECIAL:   opcode = Opcodes.INVOKESPECIAL;   break;
      case STATIC:    opcode = Opcodes.INVOKESTATIC;    break;
      default:        throw new RuntimeException("Unknown call type");
    }

    mv.visitMethodInsn(opcode, c.getName(), m.getName(), m.getDescriptor());
    
    return null;
  }

  /**
   * Output instructions for allocating arrays, both for primitive and
   * reference types.
   *
   * @param instruction Array allocation instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(NewArray instruction) {
    if(instruction.getElementType().getSort() == Type.Sort.REF) {
      mv.visitTypeInsn(
        Opcodes.ANEWARRAY,
        instruction.getElementType().getInternalName()
      );
    } else {
      int type;

      switch(instruction.getElementType().getSort()) {
        case BOOL:   type = Opcodes.T_BOOLEAN; break;
        case CHAR:   type = Opcodes.T_CHAR;    break;
        case FLOAT:  type = Opcodes.T_FLOAT;   break;
        case DOUBLE: type = Opcodes.T_DOUBLE;  break;
        case BYTE:   type = Opcodes.T_BYTE;    break;
        case SHORT:  type = Opcodes.T_SHORT;   break;
        case INT:    type = Opcodes.T_INT;     break;
        case LONG:   type = Opcodes.T_LONG;    break;
        default: throw new RuntimeException("Unknown array element type");
      }

      mv.visitIntInsn(Opcodes.NEWARRAY, type);
    }

    return null;
  }

  /**
   * Outputs a multidimensional allocation instruction for reference types.
   *
   * @param instruction Array allocation instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(NewMultiArray instruction) {
    mv.visitMultiANewArrayInsn(
      instruction.getType().getInternalName(),
      instruction.getDimensions()
    );

    return null;
  }

  /**
   * Outputs an object allocation instruction.
   *
   * @param instruction Object allocation instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(NewObject instruction) {
    mv.visitTypeInsn(Opcodes.NEW, instruction.getType().getInternalName());

    return null;
  }

  /**
   * Outputs switch conditional branches, using a simpler table switch if
   * possible.
   *
   * @param instruction Switch instruction.
   * @return            <code>null</code>
   */
  @Override
  public Void visit(Switch instruction) {
    Map<Integer, Block> mapping = instruction.getMapping();
    int[] keys = new int[mapping.size()];
    Label[] destinations = new Label[mapping.size()];

    int index = 0;
    for(Map.Entry<Integer, Block> m : instruction.getMapping().entrySet()) {
      keys[index] = m.getKey().intValue();
      destinations[index] = BlockExporter.getLabel(m.getValue());
      index++;
    }

    // TODO: Use table switch if possible.

    mv.visitLookupSwitchInsn(
      BlockExporter.getLabel(instruction.getDefault()),
      keys,
      destinations
    );

    return null;
  }
  
  /**
   * Outputs type casting instructions.
   * 
   * @param instruction Check cast instruction.
   * @return            <code>null</code>.
   */
  @Override
  public Void visit(CheckCast instruction) {
    mv.visitTypeInsn(Opcodes.CHECKCAST, instruction.getType().getDescriptor());

    return null;
  }

  /**
   * Outputs <code>instanceof</code> comparison instructions.
   *
   * @param instruction <code>instanceof</code> instruction.
   * @return            <code>null</code>.
   */
  @Override
  public Void visit(InstanceOf instruction) {
    mv.visitTypeInsn(Opcodes.INSTANCEOF, instruction.getType().getDescriptor());

    return null;
  }

  /**
   * Outputs <code>try ... catch ...</code> blocks for exception handling.
   *
   * @param instruction Instruction introducing block.
   * @return            <code>null</code>.
   */
  @Override
  public Void visit(TryCatch instruction) {
    mv.visitTryCatchBlock(
      BlockExporter.getLabel(instruction.getStart()),
      BlockExporter.getLabel(instruction.getEnd()),
      BlockExporter.getLabel(instruction.getHandler()),
      instruction.getExceptionType().getInternalName()
    );

    return null;
  }

  /**
   * Outputs exception <code>throw</code>ing instructions.
   *
   * @param instruction <code>throw</code> instruction.
   * @return            <code>null</code>.
   */
  @Override
  public Void visit(Throw instruction) {
    mv.visitInsn(Opcodes.ATHROW);

    return null;
  }
}
