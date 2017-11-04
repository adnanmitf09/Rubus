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

package graph.instructions;

import graph.CodeVisitor;
import graph.Type;

/**
 * 
 */
public class Arithmetic implements Producer {
  /**
   * Possible arithmetic operations.
   */
  public enum Operator {
    ADD, SUB, MUL, DIV, REM, AND, OR, XOR, SHL, SHR, USHR
  }
  
  /**
   * Internal details
   */
  private Operator operator;

  /**
   * Operands
   */
  private Producer operandA;
  private Producer operandB;
  
  /**
   * Constructs object with given details, representing <code>a [op] b</code>.
   *
   * @param op    Operator that object represents.
   * @param type  Return type of the object (should agree with both operands).
   * @param a     Operand A
   * @param b     Operand B
   */
  public Arithmetic(Operator op, Producer a, Producer b) {
    this.operator  = op;
    this.operandA  = a;
    this.operandB  = b;
  }
  
  public Operator getOperator() {
    return operator;
  }

  private Type ignoreSize(Type t) {
    switch(t.getSort()) {
      case BYTE: return Type.INT;
      case SHORT: return Type.INT;
      default: return t;
    }
  }

  public Type getType() {
    Type a = ignoreSize(operandA.getType());
    Type b = ignoreSize(operandB.getType());

    if(!a.equals(b))
      System.err.println("Arithmetic on non-matching types: " + a + ", " + b);

    return a;
  }

  public Producer getOperandA() {
    return operandA;
  }

  public Producer getOperandB() {
    return operandB;
  }

  public Producer[] getOperands() {
    return new Producer[] {operandA, operandB};
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Arithmetic) {
      Arithmetic a = (Arithmetic) obj;

      return (operator == a.operator) && operandA.equals(a.operandA) &&
                                                    operandB.equals(a.operandB);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + this.operator.hashCode();
    hash = 29 * hash + (this.operandA != null ? this.operandA.hashCode() : 0);
    hash = 29 * hash + (this.operandB != null ? this.operandB.hashCode() : 0);
    return hash;
  }
  
  @Override
  public String toString() {
    return operator + " (" + getType() + ")";
  }
  
  public <T> T accept(CodeVisitor<T> visitor) {
    if(visitor.getResult(this) != null) {
      return visitor.getResult(this);
    } else {
      T result = visitor.visit(this);
      
      visitor.putResult(this, result);
      
      return result;
    }
  }
}
