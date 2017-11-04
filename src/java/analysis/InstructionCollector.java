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

import java.util.HashSet;
import java.util.Set;

/**
 * Traverses a instruction graph, and adds instructions of the required type to
 * a set.
 */
public class InstructionCollector<T> extends CodeVisitor<Void> {
  /**
   * Class type to be collected.
   */
  private Class<T> clazz;

  /**
   * Set of instructions.
   */
  private Set<T> instructions = new HashSet<T>();

  /**
   * Collects instructions from the given root node and of the given type (or
   * subclasses).
   *
   * @param root   Root instruction of graph.
   * @param clazz  Class object for type required.
   * @return       Set of instructions.
   */
  public static <T> Set<T> collect(Instruction root, Class<T> clazz) {
    InstructionCollector<T> collector = new InstructionCollector<T>(clazz);

    root.accept(new CodeTraverser(collector));

    return collector.instructions;
  }

  /**
   * Private constructor to only allow specific use as in <code>collect</code>.
   *
   * @param clazz  Class object for type required.
   */
  private InstructionCollector(Class<T> clazz) {
    this.clazz = clazz;
  }

  /**
   * Adds the given instruction to the set if it is of the required type.
   *
   * @param instruction Instruction.
   * @return            <code>null</code>.
   */
  @Override
  @SuppressWarnings("unchecked")
  public Void visit(Instruction instruction) {
    if(clazz.isAssignableFrom(instruction.getClass())) {
      instructions.add((T) instruction);
    }

    return null;
  }
}
