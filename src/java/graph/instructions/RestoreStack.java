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
public class RestoreStack implements Producer {
  /**
   * Index of position on stack to restore.
   */
  private int index;

  /**
   * Type of the value.
   */
  private Type type;
  
  /**
   * Simple constructor.
   */
  public RestoreStack(int index, Type type) {
    this.index = index;
    this.type = type;
  }

  public int getIndex() {
    return index;
  }
  
  public Type getType() {
    return type;
  }
  
  public Producer[] getOperands() {
    return new Producer[] {};
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof RestoreStack) {
      RestoreStack ins = (RestoreStack) obj;

      return (index == ins.index) && type.equals(ins.type);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    // TODO: Properly
    return 0;
  }

  @Override
  public String toString() {
    return "RESTORESTACK " + index + " (" + type + ")";
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
