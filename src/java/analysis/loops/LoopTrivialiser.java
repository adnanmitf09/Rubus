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

package analysis.loops;

import static graph.instructions.Arithmetic.Operator.ADD;
import static graph.instructions.Arithmetic.Operator.SUB;
import static graph.instructions.Condition.Operator.EQ;
import static graph.instructions.Condition.Operator.GE;
import static graph.instructions.Condition.Operator.GT;
import static graph.instructions.Condition.Operator.LE;
import static graph.instructions.Condition.Operator.LT;
import graph.BasicBlock;
import graph.Block;
import graph.Loop;
import graph.TrivialLoop;
import graph.instructions.Arithmetic;
import graph.instructions.Condition;
import graph.instructions.Constant;
import graph.instructions.Producer;
import graph.instructions.Read;
import graph.state.Variable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import util.Utils;
import analysis.BlockCollector;
import analysis.dataflow.IncrementVariables;
import analysis.dataflow.SimpleUsed;

/**
 * Used for supplementing loop nodes with information regarding the loop index
 * and limits. This is done for the case of trivial loops (only one exit branch
 * that occurs before any writes are made by the loop).
 */
public class LoopTrivialiser {
  /**
   * Converts as many of the loop nodes in the given set as possible into
   * trivial loops.
   *
   * @param  loops Set of loop nodes to supplement.
   * @return       Set of trivial loop nodes corresponding to the input.
   */
  public static Set<TrivialLoop> convert(Set<Loop> loops) {
    Set<TrivialLoop> ret = new HashSet<TrivialLoop>();

    for(Loop loop : loops) {
      TrivialLoop trivialLoop = convert(loop);

      if(trivialLoop != null) {
        ret.add(trivialLoop);
      }
    }

    return ret;
  }

  /**
   * Converts the given loop node into a <code>TrivialLoop</code> by detecting
   * the loop index, which variables are incremented and the loop limit. If the
   * given loop is not a trivial loop then <code>null</code> is returned.
   *
   * @param  loop  Loop to be analysed.
   * @return       <code>TrivialLoop</code> if the given loop was trivial,
   *               <code>null</code> otherwise.
   */
  public static TrivialLoop convert(Loop loop) {
    // Line Number String
    String line = (loop.getLineNumber() == null)
                                  ? "" : " (line " + loop.getLineNumber() + ")";

    // Various properties to determine for the trivial loop.
    BasicBlock branch;
    Condition  condition;

    // Find branch that exits loop.
    Set<Block> branches = new HashSet<Block>(loop.getNext().getPredecessors());

    branches.retainAll(loop.getBody());

    if(Utils.getSingleElement(branches) instanceof BasicBlock) {
      branch = (BasicBlock) Utils.getSingleElement(branches);
    } else {
      Logger.getLogger("loops.trivialise").info(
        "Loop has multiple exit points" + line + "."
      );

      return null;
    }

    // Update line number string to be more specific.
    if(branch.getLineNumber() != null) {
      line = " (line " + branch.getLineNumber() + ")";
    }

    // Check that branch is a simple condition.
    if(branch.getBranch() instanceof Condition) {
      condition = (Condition) branch.getBranch();
    } else {
      Logger.getLogger("loops.trivialise").info(
        "Exit from loop is not simple condition " + line + "."
      );
      
      return null;
    }

    // Check that no writes occur before condition.
    SimpleUsed used = new SimpleUsed(BlockCollector.collect(loop.getStart(), branch, true, true));

    if(used.containsDirectWrites() || used.containsReferenceWrites()) {
      Logger.getLogger("loops.trivialise").info(
        "Loop exit is not at start of loop " + line + "."
      );

      return null;
    }

    // Find all increment variables.
    Map<Variable, Integer> increments = new IncrementVariables(
                                   loop.getBody()).getIncrements(loop.getEnd());

    // Loop is of the form "while(index <operator> limit) { ... }".
    Variable           index;
    Condition.Operator operator;
    Producer           limit;

    // Index variable on LHS of operator.
    if((index = checkRead(condition.getOperandA(), increments.keySet())) != null) {
      limit    = condition.getOperandB();
      operator = condition.getOperator();
    // Index variable on RHS of operator.
    } else if((index = checkRead(condition.getOperandB(), increments.keySet())) != null) {
      limit    = condition.getOperandA();
      operator = condition.getOperator().reverse();
    // Condition is more complex than index on one side.
    } else {
      Logger.getLogger("loops.trivialise").info(
        "Condition is not simple comparison of index " + line + "."
      );
     return null;
    }

    // If conditional branch goes out of loop, then negate operator.
    Block start = condition.getDestination();

    if(branch.getNext() != loop.getNext()) {
      operator = operator.not();
      start = branch.getNext();
    }

    // Positive increment.
    if(increments.get(index).intValue() > 0) {
      // Correct inclusive limit for <=.
      if(operator == LE) {
        limit = new Arithmetic(SUB, limit, new Constant(new Integer(1)));
      }

      // Forbidden operators for positive increment
      if((operator == EQ) || (operator == GT) || (operator == GE)) {
        Logger.getLogger("loops.trivialise").warn(
          "Comparison operator suggests non-termination " + line + "."
        );

        return null;
      }
    // Negative increment.
    } else {
      // Correct inclusive limit for >=.
      if(operator == GE) {
        limit = new Arithmetic(ADD, limit, new Constant(new Integer(1)));
      }

      // Forbidden operators for positive increment
      if((operator == EQ) || (operator == LT) || (operator == LE)) {
        Logger.getLogger("loops.trivialise").warn(
          "Comparison operator suggests non-termination " + line + "."
        );
        
        return null;
      }
    }

    // Create trivial loop object.
    TrivialLoop trivialLoop = new TrivialLoop(
      start,
      loop.getEnd(),
      index,
      increments,
      limit
    );

    trivialLoop.setLineNumber(loop.getLineNumber());

    // Replace old loop with supplemented loop.
    loop.replace(trivialLoop);

    // User feedback.
    if(increments.get(index) > 0) {
      Logger.getLogger("loops.trivialise").info(
        "Trivial loop found " + line + ": " + index + " < " + limit + " " + increments
      );
    } else {
      Logger.getLogger("loops.trivialise").info(
        "Trivial loop found " + line + ": " + index + " > " + limit + " " + increments
      );
    }

    return trivialLoop;
  }

  private static Variable checkRead(Producer p, Set<Variable> vars) {
    if(p instanceof Read) {
      Read r = (Read) p;

      if((r.getState() instanceof Variable) && vars.contains((Variable) r.getState())) {
        return (Variable) r.getState();
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
}
