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

package analysis.dependency;

import graph.Method;
import graph.TrivialLoop;

/**
 * General interface for all dependency checking routines.
 */
public interface DependencyCheck {
  /**
   * Sets the context in which loops should be considered.
   *
   * @param method Method in which loops that follow are contained.
   */
  public void setContext(Method method);

  /**
   * Checks whether it is safe to execute the given <code>TrivialLoop</code> in
   * parallel.
   *
   * @param  loop  Trivial loop to check.
   * @return       <code>true</code> if safe to run in parallel,
   *               <code>false</code> otherwise.
   */
  public boolean check(TrivialLoop loop);
}
