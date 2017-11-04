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

package graph;

import graph.instructions.Producer;
import graph.state.Variable;

import java.util.Map;

/**
 *
 */
public class TrivialLoop extends Loop {
  /**
   * Loop index variable.
   */
  private Variable index;

  /**
   * Variables incremented over the loop.
   */
  private Map<Variable, Integer> increments;

  /**
   * Limit (exclusive). When reconstructing an exit condition, use &lt; for
   * positive increments and &gt; for negative increments. The producer stored
   * must be of type sort INT.
   */
  private Producer limit;

  /**
   * Constructor
   */
  public TrivialLoop(Block start, Block end, Variable index, Map<Variable, Integer> increments, Producer limit) {
    super(start, end);

    this.index      = index;
    this.increments = increments;
    this.limit      = limit;
  }

  /**
   * Returns the loop variable.
   */
  public Variable getIndex() {
    return index;
  }

  /**
   * Returns the loop increment variables.
   */
  public Map<Variable, Integer> getIncrements() {
    return increments;
  }

  /**
   * Returns the loop limit (exclusive).
   */
  public Producer getLimit() {
    return limit;
  }

  /**
   * Acceptor for the BlockVisitor pattern.
   */
  @Override
  public <T> T accept(BlockVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() { 
    return super.toString() + ": " + index + ";" + increments + ";" + limit;
  }
}
