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

import java.util.Arrays;

/**
 *
 */
public class NewMultiArray implements Producer {
  private Producer[] counts;
  private Type       elementType;
  private Type       fullType;

  public NewMultiArray(Type type, Producer[] counts) {
    this.elementType = type;
    this.fullType    = type;
    this.counts      = counts;

    // Get element type.
    for(int i = 0; i < getDimensions(); i++) {
      elementType = elementType.getElementType();
    }
  }

  public Type getElementType() {
    return elementType;
  }

  public Type getType() {
    return fullType;
  }

  public Producer[] getCounts() {
    return counts;
  }

  public int getDimensions() {
    return counts.length;
  }

  public Producer[] getOperands() {
    return counts;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof NewMultiArray) {
      NewMultiArray n = (NewMultiArray) obj;

      return elementType.equals(n.elementType) && Arrays.equals(counts, n.counts);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + (this.counts != null ? Arrays.hashCode(counts) : 0);
    hash = 97 * hash + (this.elementType != null ? this.elementType.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "NEWMULTIARRAY (" + elementType + ")";
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
