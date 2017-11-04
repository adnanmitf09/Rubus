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

/**
 * Class representing all stack operations (i.e. DUP, SWAP etc.). This is not
 * used for any analysis, but is important when recreating stack based bytecode.
 */
public class StackOperation implements Instruction {
  public enum Sort {
    SWAP, POP, POP2, DUP, DUP2, DUP_X1, DUP_X2, DUP2_X1, DUP2_X2
  }

  private Sort sort;

  public StackOperation(Sort sort) {
    this.sort = sort;
  }

  public Sort getSort() {
    return sort;
  }

  public <T> T accept(CodeVisitor<T> v) {
    return v.visit(this);
  }

  public Producer[] getOperands() {
    return new Producer[]{};
  }

  public String toString() {
    return sort.toString();
  }
}
