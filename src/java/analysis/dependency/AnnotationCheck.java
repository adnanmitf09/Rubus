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

import graph.Annotation;
import graph.Method;
import graph.TrivialLoop;
import graph.Type;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Checks dependencies based on annotations on the containing method.
 */
public class AnnotationCheck implements DependencyCheck {
  /**
   * Names of loop indicies that should be run in parallel in the current
   * context.
   */
  private List<String> loopIndices;

  /**
   * Sets the context in which loops should be considered.
   *
   * @param method Method in which loops that follow are contained.
   */
  @Override
  public void setContext(Method method) {
    Annotation annotation = method.getAnnotation(
      Type.getObjectType("annotation/Transform")
    );

    if(annotation == null) {
      loopIndices = Collections.emptyList();
    } else {
      loopIndices = (List<String>) annotation.get("loops");
    }
  }

  /**
   * Checks whether it is safe to execute the given <code>TrivialLoop</code> in
   * parallel based on the name of the loop index.
   *
   * @param  loop  Trivial loop to check.
   * @return       <code>true</code> if safe to run in parallel,
   *               <code>false</code> otherwise.
   */
  @Override
  public boolean check(TrivialLoop loop) {
    if(loopIndices.contains(loop.getIndex().getName())) {
      Logger.getLogger("annotation").info("Accepted " + loop + " for parallelisation.");
      return true;
    } else {
      Logger.getLogger("annotation").info("Rejected " + loop + " for parallelisation.");
      return false;
    }
  }
}
