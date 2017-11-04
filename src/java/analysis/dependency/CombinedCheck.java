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

import org.apache.log4j.Logger;

/**
 * Chains together other dependency checks.
 */
public class CombinedCheck implements DependencyCheck {
  /**
   * Dependency checks that are considered in returning a judgement.
   */
  final private DependencyCheck[] contained;

  /**
   * Accuracy statistics.
   */
  final private int[] correct;

  /**
   * Safety flags.
   */
  final private boolean[] safe;

  /**
   * Constructor for a combined check. The result of the combined check is the
   * disjunction of these (i.e. it is assumed all checks are sound but not
   * complete). When accuracy statistics are calculated, it is assumed that the
   * first check will detect all parallelisable loops that the others detect.
   *
   * @param contained All checks to be considered.
   */
  public CombinedCheck(DependencyCheck ... contained) {
    this.contained = contained;
    this.correct   = new int[contained.length];
    this.safe      = new boolean[contained.length];

    for(int i = 0; i < contained.length; i++) {
      this.safe[i] = true;
      this.correct[i] = 0;
    }

    if(Logger.getLogger("check.Combined").isInfoEnabled()) {
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          for(int i = 1; i < CombinedCheck.this.contained.length; i++) {
//           Logger.getLogger("check.Combined").info(
//              CombinedCheck.this.contained[i].getClass().getSimpleName()
//              + ": " + correct[i] + "/" + correct[0] + "="
//              + (100 * correct[i] / correct[0]) + "% (safe = " + safe[i] + ")"
//            );
          }
        }
      });
    }
  }

  /**
   * Sets the context in which following loops are contained. This is simply
   * passed onto the contained dependency checks.
   *
   * @param method Method in which loops that follow are contained.
   */
  @Override
  public void setContext(Method method) {
    for(DependencyCheck check : contained) {
      check.setContext(method);
    }
  }

  /**
   * Used the contained checks to produce a judgement as to whether it is safe
   * to run the given trivial loop in parallel.
   *
   * @param  loop  Trivial loop to check.
   * @return       <code>true</code> if safe to run in parallel,
   *               <code>false</code> otherwise.
   */
  @Override
  public boolean check(TrivialLoop loop) {
    Logger logger = Logger.getLogger("check.Combined");

    if(logger.isInfoEnabled()) {
      boolean answer = contained[0].check(loop);

      correct[0]++;

      for(int i = 1; i < contained.length; i++) {
        if(answer == contained[i].check(loop)) {
          correct[i]++;
        } else if(answer == false) {
          logger.info(
            contained[i].getClass().getSimpleName()
            + " is unsafe for loop on line " + loop.getLineNumber()
          );

          safe[i] = false;
        }
      }

      return answer;
    } else {
      for(DependencyCheck check : contained) {
        if(check.check(loop))
          return true;
      }
    }

    return false;
  }
}
