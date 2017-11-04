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
import graph.instructions.Read;

import java.util.Collections;
import java.util.List;

import util.ConsList;

public class ArrayElement implements State {
  /**
   * Instruction producing array that element is from.
   */
  private Producer array;

  /**
   * Instruction producing index into array.
   */
  private Producer index;
  
  /**
   * Constructs an array element representation.
   *
   * @param array  Instruction producing array that element is from.
   * @param index  Instruction producing index into array.
   * @param type   Type that is expected in the context, this is updated to be
   *               more specialised if the further type information is known.
   */
  public ArrayElement(Producer array, Producer index, Type type) {
    // If the array has an element type, we can use this to update the context.
    if(array.getType().getElementType() != null) {
      // Byte and Boolean arrays are the same, so we have to deal with this.
      if((array.getType().getElementType().getSort() == Type.Sort.BOOL)
                                                   && type.equals(Type.BYTE)) {
        type = Type.BOOL;
      }

      type.unify(array.getType().getElementType());
    }

    this.array = array;
    this.index = index;

    array.getType().unify(Type.getArrayOf(type));
  }
  
  @Override
  public Type getType() {
    return array.getType().getElementType();
  }

  public Producer getArray() {
    return array;
  }

  public Producer getIndex() {
    return index;
  }

  @Override
  public State getBase() {
    if(array instanceof Read) {
      return ((Read) array).getState().getBase();
    } else {
      return null;
    }
  }
  
  @Override
  public Producer[] getOperands() {
    return new Producer[] {array, index};
  }

  @Override
  public List<Producer> getIndicies() {
    if(array instanceof Read) {
      return new ConsList<Producer>(index, ((Read) array).getState().getIndicies());
    } else {
      return Collections.singletonList(index);
    }
  }
  
  @Override
  public String toString() {
    return array.toString() + "[" + index.toString() + "]";
  }
}
