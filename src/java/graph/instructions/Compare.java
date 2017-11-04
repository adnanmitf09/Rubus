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
 * @author pete
 */
public class Compare implements Producer{
  private boolean variant;
  private Type type;

  /**
   * Operands
   */
  private Producer operandA;
  private Producer operandB;

  /**
   * Constructs comparison of <code>a</code> and <code>b</code> for the given
   * type and variant in the case of floating-point comparisons.
   *
   * @param variant Whether to use G variant (rather than L).
   * @param type    Return type of the object (should agree with both operands).
   * @param a       Operand A
   * @param b       Operand B
   */
  public Compare(boolean variant, Type type, Producer a, Producer b) {
    this.variant   = variant;
    this.type      = type;
    this.operandA  = a;
    this.operandB  = b;
  }

  public boolean getVariant() {
    return variant;
  }

  public Type getCompareType() {
    return type;
  }

  public Type getType() {
    return Type.INT;
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
  public String toString() {
    return "COMPARE " + (variant ? "L" : "G");
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
