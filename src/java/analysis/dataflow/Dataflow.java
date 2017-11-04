/*
 * Parallelising JVM Compiler
 * Part II Project, Computer Science Tripos
 *
 * Copyright (c) 2009, 2010 - Peter Calvert, University of Cambridge
 */

package analysis.dataflow;

import graph.BasicBlock;
import graph.Block;
import graph.BlockVisitor;
import graph.Loop;
import graph.instructions.Call;
import graph.instructions.Stateful;
import graph.state.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public abstract class Dataflow<T> extends BlockVisitor<Map<Variable, T>> {
  /**
   * Results of the analysis (after each block).
   */
  private Map<Block, Map<Variable, T>> blockResults = new HashMap<Block, Map<Variable, T>>();

  /**
   * Results of the analysis (after each stateful instruction).
   */
  private Map<Stateful, Map<Variable, T>> insResults = new HashMap<Stateful, Map<Variable, T>>();

  /**
   * Adds blocks to the analysis.
   *
   * @param blocks Blocks to add.
   */
  protected void addBlocks(Collection<Block> blocks) {
    for(Block block : blocks) {
      blockResults.put(block, new HashMap<Variable, T>());
    }
  }

  /**
   * Recalculates the analysis.
   */
  public void calculate() {
    boolean change;

    do {
      change = false;

      for(Block block : blockResults.keySet()) {
        Map<Variable, T> map = block.accept(this);

        if(!map.equals(blockResults.get(block))) {
          blockResults.put(block, map);
          change = true;
        }
      }
    } while(change);
  }

  /**
   * Returns the result of the analysis for a given block.
   *
   * @param  block Block to give result for.
   * @return       Result.
   */
  public Map<Variable, T> getResult(Block block) {
    if(blockResults.containsKey(block)) {
      return Collections.unmodifiableMap(blockResults.get(block));
    } else {
      return null;
    }
  }

  /**
   * Returns the result of the analysis after a given stateful instruction.
   *
   * @param  instruction Instruction to give result for.
   * @return             Result.
   */
  public Map<Variable, T> getResult(Stateful instruction) {
    if(insResults.containsKey(instruction)) {
      return Collections.unmodifiableMap(insResults.get(instruction));
    } else {
      return null;
    }
  }

  /**
   * Considers the given instruction, and applies its effect to
   * <code>map</code>.
   *
   * @param map      Result map before instruction.
   * @param stateful Stateful instruction to consider.
   */
  protected abstract void consider(Map<Variable, T> map, Stateful stateful);

  /**
   * Performs the analysis recursively on the given loop.
   *
   * @param loop     Loop to recurse on.
   */
  protected abstract Dataflow<T> recurse(Loop loop);

  /**
   * Returns the map that results from 'intersecting' the result maps of the
   * block's predecessors.
   *
   * @param  block Block whose predecessors should be intersected.
   * @return       Resultant map.
   */
  protected Map<Variable, T> intersectPredecessors(Block block) {
    Map<Variable, T> map = null;
    Set<Map.Entry<Variable, T>> set = null;

    // Intersect predecessors
    for(Block pred : block.getPredecessors()) {
      if(!blockResults.containsKey(pred))
        continue;

      if(map == null) {
        map = new HashMap<Variable, T>(blockResults.get(pred));
        set = map.entrySet();
      } else {
        set.retainAll(blockResults.get(pred).entrySet());
      }
    }

    // Insert 'black-list' mappings.
    for(Block pred : block.getPredecessors()) {
      if(blockResults.get(pred) != null) {
        for(Map.Entry<Variable, T> pair : blockResults.get(pred).entrySet()) {
          if(!set.contains(pair)) {
            map.put(pair.getKey(), null);
          }
        }
      }
    }

    if(map == null) {
      map = new HashMap<Variable, T>();
    }

    return map;
  }

  /**
   * Causes the analysis to visit a basic block, passing consideration of each
   * stateful instruction onto the abstract <code>consider</code> method.
   *
   * @param  block Block to visit.
   * @return       Result for the block.
   */
  @Override
  public Map<Variable, T> visit(BasicBlock block) {
    Map<Variable, T> result = intersectPredecessors(block);

    // Effect of stateful instructions in the block.
    for(Stateful s : block.getStateful()) {
      // Ignore Calls
      if(s instanceof Call) {
        insResults.put(s, new HashMap<Variable, T>(result));
        continue;
      }

      // Only interested in variables.
      if(s.getState() instanceof Variable) {
        consider(result, s);
      }
      
      insResults.put(s, new HashMap<Variable, T>(result));
    }

    return result;
  }

  /**
   * Causes the analysis to visit a loop, passing consideration of the loop onto
   * the abstract <code>recurse</code> method.
   *
   * @param  loop  Loop to visit.
   * @return       Result for the loop.
   */
  @Override
  public Map<Variable, T> visit(Loop loop) {
    Map<Variable, T> result = intersectPredecessors(loop);
    Dataflow<T> child = recurse(loop);

    // Black list all variables affected by the loop, since the loop may run
    // zero or many times.
    for(Variable variable : child.getResult(loop.getEnd()).keySet()) {
      result.put(variable, null);
    }

    // Update results for all stateful instructions within the loop.
    for(Stateful stateful : child.insResults.keySet()) {
      insResults.put(stateful, result);
    }

    return result;
  }
}
