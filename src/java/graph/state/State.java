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

import java.util.List;

/**
 * Interface which all types of state should implement.
 */
public interface State {
  /**
   * Type of the state.
   *
   * @return       Relevant type.
   */
  public Type getType();

  /**
   * 'Base' state of the state. This will either be a local variable or static
   * field. In some cases, this may return <code>null</code> if the state is
   * based on an allocation, or call result.
   *
   * @return       Base state, or <code>null</code> if none exists.
   */
  public State getBase();

  /**
   * Operands for the state, such as objects, arrays and indicies.
   *
   * @return       Array of producer instructions.
   */
  public Producer[] getOperands();

  /**
   * Indicies from the whole state.
   *
   * @return       List of producer instructions for each index.
   */
  public List<Producer> getIndicies();
}
