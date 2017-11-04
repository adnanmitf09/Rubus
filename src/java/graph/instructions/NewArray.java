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
public class NewArray implements Producer {
  private Producer count;
  private Type type;
  
  public NewArray(Type type, Producer count) {
    this.type  = Type.getArrayOf(type);
    this.count = count;
  }

  public Type getElementType() {
    return type.getElementType();
  }

  public Type getType() {
    return type;
  }

  public Producer getCount() {
    return count;
  }
  
  public Producer[] getOperands() {
    return new Producer[] {count};
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof NewArray) {
      NewArray n = (NewArray) obj;

      return type.equals(n.type) && count.equals(n.count);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.count != null ? this.count.hashCode() : 0);
    hash = 67 * hash + (this.type != null ? this.type.hashCode() : 0);
    return hash;
  }
  
  @Override
  public String toString() {
    return "NEWARRAY (" + type + ")";
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
