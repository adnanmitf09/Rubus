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

package graph;

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
import graph.instructions.Read;
import graph.instructions.RestoreStack;
import graph.instructions.Return;
import graph.instructions.StackOperation;
import graph.instructions.Stateful;
import graph.instructions.Switch;
import graph.instructions.Throw;
import graph.instructions.TryCatch;
import graph.instructions.ValueReturn;
import graph.instructions.Write;

import java.util.HashMap;
import java.util.Map;

/**
 * Template class for visitors to the dataflow graph. Methods in this class
 * delegate to the instruction's parent class, so that extensions of this class
 * can choose to implement either a general or specific visitor.
 */
public abstract class CodeVisitor<T> {
  private Map<Instruction, T> results = new HashMap<Instruction, T>();

  public void putResult(Instruction i, T r) {
    results.put(i, r);
  }

  public T getResult(Instruction i) {
    return results.get(i);
  }

  public T visit(Instruction instruction)    { return null;                             }
  public T visit(Branch instruction)         { return visit((Instruction) instruction); }
  public T visit(Stateful instruction)       { return visit((Instruction) instruction); }

  public T visit(Condition instruction)      { return visit((Branch) instruction);      }
  public T visit(Return instruction)         { return visit((Branch) instruction);      }
  public T visit(Throw instruction)          { return visit((Branch) instruction);      }
  public T visit(TryCatch instruction)       { return visit((TryCatch) instruction);    }
  public T visit(Switch instruction)         { return visit((Branch) instruction);      }
  public T visit(ValueReturn instruction)    { return visit((Return) instruction);      }

  public T visit(Call instruction)           { return visit((Stateful) instruction);    }
  public T visit(Increment instruction)      { return visit((Stateful) instruction);    }
  public T visit(Read instruction)           { return visit((Stateful) instruction);    }
  public T visit(Write instruction)          { return visit((Stateful) instruction);    }

  public T visit(ArrayLength instruction)    { return visit((Instruction) instruction); }
  public T visit(Arithmetic instruction)     { return visit((Instruction) instruction); }
  public T visit(CheckCast instruction)      { return visit((Instruction) instruction); }
  public T visit(Compare instruction)        { return visit((Instruction) instruction); }
  public T visit(Constant instruction)       { return visit((Instruction) instruction); }
  public T visit(Convert instruction)        { return visit((Instruction) instruction); }
  public T visit(InstanceOf instruction)     { return visit((Instruction) instruction); }
  public T visit(NewArray instruction)       { return visit((Instruction) instruction); }
  public T visit(NewMultiArray instruction)  { return visit((Instruction) instruction); }
  public T visit(NewObject instruction)      { return visit((Instruction) instruction); }
  public T visit(Negate instruction)         { return visit((Instruction) instruction); }
  public T visit(RestoreStack instruction)   { return visit((Instruction) instruction); }
  public T visit(StackOperation instruction) { return visit((Instruction) instruction); }
}
