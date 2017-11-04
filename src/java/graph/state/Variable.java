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

package graph.state;

import graph.Type;
import graph.instructions.Producer;

import java.util.Collections;
import java.util.List;

/**
 * Represents a local variable within a method.
 */
public class Variable implements State {
  /**
   * Index of the variable.
   */
  private int index;

  /**
   * Type of the variable. This maybe particularly general for reference types,
   * since full type descriptors are only reliable for parameters, or if full
   * debugging information is turned on.
   */
  private Type type;

  /**
   * Original name of the variable in the source code. This information is only
   * sometimes available, and is purely used for debugging.
   */
  private String name;

  /**
   * Constructs a object for the given variable index and type.
   *
   * @param number Variable index.
   * @param type   Type descriptor.
   */
  public Variable(int number, Type type) {
    this.index = number;
    this.type  = type;
  }

  /**
   * Returns the index of the variable.
   *
   * @return       Variable index.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Returns the type of the variable.
   *
   * @return       Variable type.
   */
  @Override
  public Type getType() {
    return type;
  }

  /**
   * Returns the variable itself.
   *
   * @return       The variable (<code>this</code>).
   */
  @Override
  public State getBase() {
    return this;
  }

  /**
   * Any operands taken by the state, this is an empty array for local
   * variables.
   *
   * @return       Empty array.
   */
  @Override
  public Producer[] getOperands() {
    return new Producer[] {};
  }

  /**
   * Returns an empty list, since local variables neither allow any 'index' nor
   * rely on any other state.
   *
   * @return       Empty list.
   */
  @Override
  public List<Producer> getIndicies() {
    return Collections.emptyList();
  }

  /**
   * Sets the original name used for the variable.
   *
   * @param n      Original name of the variable.
   */
  public void setName(String n) {
    name = n;
  }

  /**
   * Returns the original name used for the variable.
   *
   * @return       Original name of the variable.
   */
  public String getName() {
    return name;
  }

  /**
   * Checks for equality with another object. Requires that this other object is
   * also a variable with the same index and type sort.
   *
   * @param  obj   Object for comparison.
   * @return       <code>true</code> if considered equals; <code>false</code>
   *               otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    }

    if(getClass() != obj.getClass()) {
      return false;
    }

    final Variable other = (Variable) obj;
    
    if((index == other.index) && type.getSort().equals(other.type.getSort())) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns a hashcode for the local variable, based on its index and type
   * sort.
   *
   * @return       Hashcode.
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + index;
    hash = 97 * hash + type.getSort().hashCode();
    return hash;
  }

  /**
   * Returns a textual represetation of the variable. If available this gives
   * the original name of the variable, otherwise the variable index is used.
   *
   * @return       Textual representation of variable.
   */
  @Override
  public String toString() {
    if(name != null) {
      return name + "#" + index + " (" + type + ")";
    } else {
      return "Var#" + index + " (" + type + ")";
    }
  }
}
