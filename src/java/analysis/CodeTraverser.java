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

package analysis;

import graph.CodeVisitor;
import graph.instructions.Instruction;

/**
 * Causes a child code visitor to be visit all nodes of a data-flow graph in a
 * depth first order.
 */
public class CodeTraverser extends CodeVisitor<Void> {
  /**
   * Child code visitor.
   */
  private CodeVisitor<?> child;

  /**
   * Constructs the traversal for the given child.
   *
   * @param c      Child code visitor.
   */
  public CodeTraverser(CodeVisitor<?> c) {
    child = c;
  }

  /**
   * Passes the visit of an instruction onto a child, first visiting each of its
   * operands in order.
   *
   * @param i      Instruction to traverse.
   * @return       <code>null</code>
   */
  @Override
  public Void visit(Instruction i) {
    for(Instruction o : i.getOperands()) {
      visit(o);
    }

    i.accept(child);

    return null;
  }
}
