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

import graph.ClassNode;
import graph.Method;
import graph.TrivialLoop;
import graph.Type;
import graph.instructions.Producer;
import graph.instructions.Read;
import graph.state.Field;
import graph.state.State;
import graph.state.Variable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import analysis.AliasMap;
import analysis.CanonicalState;
import analysis.LooseState;
import analysis.dataflow.AliasUsed;
import analysis.dataflow.IncrementVariables;
import analysis.dataflow.LiveVariable;
import analysis.dataflow.SimpleUsed;

/**
 * Very simple automatic check. Requires that variables live at the start of the
 * loop body are not written to by the body (read-write and write-read
 * dependencies), and that variables that are live after the loop are not
 * written to by the loop (write-write dependencies).
 */
public class BasicCheck implements DependencyCheck {
  /**
   * Annotation type for <code>restrict</code>.
   */
  final Type RESTRICT = Type.getObjectType("annotation/Restrict");
  
  /**
   * Live variable analysis, updated whenever the context changes.
   */
  private LiveVariable lva = null;

  /**
   * Alias analysis, updated whenever the context changes.
   */
  private AliasUsed alias = null;

  /**
   * Updates the context to the new method by computing various analyses across
   * the method (LVA and may-alias).
   *
   * @param method Method that forms context.
   */
  @Override
  public void setContext(Method method) {
    // Perform Live Variable Analysis.
    lva = new LiveVariable(method.getImplementation());

    // Construct interest map for alias analysis.
    AliasMap interest = new AliasMap();

    for(Field field : new SimpleUsed(method.getImplementation()).getStatics()) {
      LooseState ls = new LooseState(field);
      CanonicalState cs = new CanonicalState(field);

      if(field.getOwner().getAnnotation(RESTRICT) == null) {
        interest.put(
          ls,
          Collections.singleton(new CanonicalState(new Variable(-1, Type.REF)))
        );
      } else {
        interest.put(ls, Collections.singleton(cs));
      }
    }

    int param = 0;

    for(Variable var : method.getParameterVariables()) {
      LooseState ls = new LooseState(var);
      CanonicalState cs = new CanonicalState(var);
      ClassNode clazz = var.getType().getClassNode();

      if((method.getParameterAnnotation(param, RESTRICT) == null) &&
                 ((clazz == null) || (clazz.getAnnotation(RESTRICT) == null))) {
        interest.put(
          ls,
          Collections.singleton(
            new CanonicalState(new Variable(-1, Type.getFreshRef()))
          )
        );
      } else {
        interest.put(ls, Collections.singleton(cs));
      }

      param++;
    }
    
    // Perform alias analysis on whole method.
    alias = new AliasUsed(method.getImplementation(), interest);
  }

  
  @Override
  public boolean check(TrivialLoop loop) {
    String line = (loop.getLineNumber() == null) ? ""
                                       : " (line " + loop.getLineNumber() + ")";

    // Check we have a context.
    if((lva == null) || (alias == null)) {
      throw new RuntimeException("Can't do dependency check without context.");
    }

    // Fetch relevant liveness information.
    Set<State> liveAfter = lva.getLive(loop.getNext());
    Set<State> liveIn    = new LiveVariable(loop.getStart()).getLive(loop.getStart());

    // Fetch usage information.
    SimpleUsed used = new SimpleUsed(loop.getStart());

    // Direct writes can't conflict with variables read in or live afterwards.
    for(State state : used.getDirectWrites()) {
      if(state instanceof Variable) {
        if(!loop.getIncrements().containsKey(state)) {
          // FIXME: Also consider whether used by limit...
          if(liveIn.contains(state) || liveAfter.contains(state)) {
            Logger.getLogger("check.Basic").info(
              "Rejecting loop" + line + ", write to non-local variable " + state + "."
            );

            return false;
          }
        }
      // No writes to statics.
      } else {
        Logger.getLogger("check.Basic").info(
          "Rejecting loop" + line + ", write to static field " + state + "."
        );

        return false;
      }
    }

    // Assemble interest mapping where possible (accurate aliasing).
    AliasMap interest = new AliasMap();

    // Lookup initial alias information from method wide analysis.
    for(State s : liveIn) {
      interest.put(new LooseState(s), alias.getAliasSet(loop, s));
    }

    for(State s : used.getStatics()) {
      interest.put(new LooseState(s), alias.getAliasSet(loop, s));
    }

    Logger.getLogger("check.Basic").trace(
      "Loop " + line + " interest map " + interest
    );
    
    // Compute increment information for loop body.
    IncrementVariables inc = new IncrementVariables(loop.getStart());

    // Compute alias information for loop body.
    AliasUsed bodyAlias = new AliasUsed(loop.getStart(), interest);

    // If loop alias analysis is inaccurate, cannot proceed.
    if(!bodyAlias.isAccurate()) {
      Logger.getLogger("check.Basic").info(
        "Alias analysis not accurate enough to judge loop" + line + "."
      );

      return false;
    }

    for(CanonicalState w : bodyAlias.getWrites()) {
      // Check not to unrestricted state.
      if(w.getInternal().getBase().equals(new Variable(-1, Type.REF))) {
        Logger.getLogger("check.Basic").info(
          "Rejecting loop" + line + ", write to unrestricted variable."
        );

        return false;
      }

      // Indicies of write.
      List<Producer> outer = w.getInternal().getIndicies();

      // Check for output dependencies (write-after-write)
      for(CanonicalState s : bodyAlias.getWrites()) {
        if(s.toLoose().equals(w.toLoose())) {
          List<Producer> inner = s.getInternal().getIndicies();
          int i;

          for(i = 0; i < outer.size(); i++) {
            if((outer.get(i) instanceof Read) && (inner.get(i) instanceof Read)) {
              Read readOuter = (Read) outer.get(i);
              Read readInner = (Read) inner.get(i);

              if(readOuter.getState().equals(readInner.getState())) {
                State state = readOuter.getState();

                // Can't cope with method calls (i.e. if read was during call).
                if((inc.getIncrements(readOuter) == null)
                                    || (inc.getIncrements(readInner) == null)) {
                  continue;
                }

                // Check if it's based on loop index.
                if(loop.getIncrements().get(state) != null) {
                  Integer incOuter = inc.getIncrements(readOuter).get(state);
                  Integer incInner = inc.getIncrements(readInner).get(state);

                  if(((incOuter == null) && (incInner == null))
                                               || (incInner.equals(incOuter))) {
                    break;
                  }
                }
              }
            }
          }

          // Writes must differ in a dimension.
          if(i == outer.size()) {
            Logger.getLogger("check.Basic").info(
              "Rejecting loop" + line + ", writes to array may conflict."
            );

            return false;
          }
        }
      }

      // Check for true and anti dependencies (read-after-write/write-after-read)
      for(CanonicalState s : bodyAlias.getReads()) {
        if(s.toLoose().equals(w.toLoose())) {
          List<Producer> inner = s.getInternal().getIndicies();
          int i;

          for(i = 0; i < outer.size(); i++) {
            if((outer.get(i) instanceof Read) && (inner.get(i) instanceof Read)) {
              Read readOuter = (Read) outer.get(i);
              Read readInner = (Read) inner.get(i);

              if(readOuter.getState().equals(readInner.getState())) {
                State state = readOuter.getState();

                // Can't cope with method calls.
                if((inc.getIncrements(readOuter) == null)
                                    || (inc.getIncrements(readInner) == null)) {
                  continue;
                }

                // Check if it's based on loop index.
                if(loop.getIncrements().get(state) != null) {
                  Integer incOuter = inc.getIncrements(readOuter).get(state);
                  Integer incInner = inc.getIncrements(readInner).get(state);

                  if(((incOuter == null) && (incInner == null))
                                               || (incInner.equals(incOuter))) {
                    break;
                  }
                }
              }
            }
          }

          // Writes must differ in a dimension.
          if(i == outer.size()) {
            Logger.getLogger("check.Basic").info(
              "Rejecting loop" + line + ", writes to array may conflict."
            );
            
            return false;
          }
        }
      }
    }

    Logger.getLogger("check.Basic").info(
      "Accepted loop" + line + " based on basic test."
    );

    return true;
  }
}
