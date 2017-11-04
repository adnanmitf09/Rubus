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
public class Convert implements Producer {
  private Producer operand;
  private Type     type;
  
  public Convert(Producer operand, Type type) {
    this.operand = operand;
    this.type    = type;
  }
  
  public Type getType() {
    return type;
  }

  public Producer getOperand() {
    return operand;
  }
  
  public Producer[] getOperands() {
    return new Producer[] {operand};
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Convert) {
      Convert c = (Convert) obj;

      return (type == c.getType()) && operand.equals(c.operand);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 37 * hash + (this.operand != null ? this.operand.hashCode() : 0);
    hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
    return hash;
  }
  
  public String toString() {
    return "CONVERT TO " + type;
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
