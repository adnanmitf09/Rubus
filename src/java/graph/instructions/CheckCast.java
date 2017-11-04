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
 * Represents casting in Java. As a data flow graph node, this simply changes
 * the type of the producer. However, when executed a
 * <code>ClassCastException</code> maybe thrown if the cast is not valid.
 */
public class CheckCast implements Producer {
  private Producer object;
  private Type cast;

  public CheckCast(Producer object, Type cast) {
    this.object = object;
    this.cast   = cast;
  }

  public Type getType() {
    return cast;
  }

  @Override
  public String toString() {
    return "CAST " + cast;
  }

  @Override
  public <T> T accept(CodeVisitor<T> visitor) {
    if(visitor.getResult(this) != null) {
      return visitor.getResult(this);
    } else {
      T result = visitor.visit(this);

      visitor.putResult(this, result);

      return result;
    }
  }

  public Producer[] getOperands() {
    return new Producer[] {object};
  }
}
