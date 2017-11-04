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
import graph.state.State;
import graph.state.Variable;

/**
 * 
 */
public class Increment implements Stateful {
  private Variable var;
  private int increment;
  
  public Increment(Variable var, int increment) {
    this.var       = var;
    this.increment = increment;
  }

  public int getIncrement() {
    return increment;
  }
  
  public State getState() {
    return var;
  }
  
  public Producer[] getOperands() {
    return new Producer[] {};
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Increment) {
      Increment i = (Increment) obj;

      return var.equals(i.var) && (increment == i.increment);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 83 * hash + (this.var != null ? this.var.hashCode() : 0);
    hash = 83 * hash + this.increment;
    return hash;
  }
  
  @Override
  public String toString() {
    return "INC " + var + " BY " + increment;
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
