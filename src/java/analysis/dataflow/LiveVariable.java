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
import graph.TrivialLoop;
import graph.Type;
import graph.instructions.Call;
import graph.instructions.Increment;
import graph.instructions.Read;
import graph.instructions.Stateful;
import graph.instructions.Write;
import graph.state.State;
import graph.state.Variable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analysis.InstructionCollector;
// Some improvements needed - Adnan
/**
 * Class for performing live variable analysis on sections of code. This only
 * considers liveness of local variables (not statics) and does not go below
 * the granularity of the whole variable (i.e. fields / array elements).
 */
public class LiveVariable extends BlockVisitor<Map<Integer, Type>> {
  /**
   * Map of results.
   */
  private Map<Block, Map<Integer, Type>> live = new HashMap<Block, Map<Integer, Type>>();

  /**
   * Default successors for use with end of loops.
   */
  private Map<Block, Block> defaultSuccessors = new HashMap<Block, Block>();

  /**
   * Blocks `in-flight' - used to prevent infinite loops.
   */
  private Set<Block> inFlight = new HashSet<Block>();

  /**
   * Indicates whether changes have been made on an iteration.
   */
  private boolean changed = false;

  /**
   * Start block for analysis.
   */
  private Block start;

  /**
   * Constructs analysis object for the given graph.
   *
   * @param graph  Graph to analyse.
   */
  public LiveVariable(Block graph) {
    // Start block.
    start = graph;

    // Perform LVA
    calculate();
  }

  /**
   * Causes the analysis to be recomputed.
   */
  public void calculate() {
    // Iterate while things change.
    do {
      changed = false;

      start.accept(this);
    } while(changed);
  }

  /**
   * Performs union of successors for a given block, and also does the required
   * type unification to keep things consistent.
   *
   * @param  block Block whose successors should be unioned.
   * @return       Union map with unified types.
   */
  private Map<Integer, Type> unionSuccessors(Block block) {
    Map<Integer, Type> map = new HashMap<Integer, Type>();

    // Default successor.
    if(defaultSuccessors.containsKey(block)) {
      for(Map.Entry<Integer, Type> e : defaultSuccessors.get(block).accept(this).entrySet()) {
        if(map.containsKey(e.getKey())) {
          map.get(e.getKey()).unify(e.getValue());
        } else {
          map.put(e.getKey(), e.getValue());
        }
      }
    }

    // Normal successors.
    for(Block b : block.getSuccessors()) {
      for(Map.Entry<Integer, Type> e : b.accept(this).entrySet()) {
        if(map.containsKey(e.getKey())) {
          map.get(e.getKey()).unify(e.getValue());
        } else {
          map.put(e.getKey(), e.getValue());
        }
      }
    }

    return map;
  }

  /**
   * Stores the given result for the given block and updates the
   * <code>changed</code> flag as appropriate.
   *
   * @param block  Block.
   * @param map    New result.
   */
  private void storeResult(Block block, Map<Integer, Type> map) {
    if(!map.equals(live.get(block))) {
      changed = true;
      live.put(block, map);
    }
  }

  /**
   * Performs analysis on a basic block.
   *
   * @param  bb    Basic block.
   * @return       Result for the block.
   */
  @Override
  public Map<Integer, Type> visit(BasicBlock bb) {
    // Check not already in flight.
    if(inFlight.contains(bb)) {
      return Collections.emptyMap();
    }

    inFlight.add(bb);

    // Get stateful instructions in correct order.
    List<Stateful> stateful = new LinkedList<Stateful>(bb.getStateful());
    Collections.reverse(stateful);

    Map<Integer, Type> map = unionSuccessors(bb);

    // Effect of stateful instructions in the block.
    for(Stateful s : stateful) {
      // Calls can't affect local variables.
      if(s instanceof Call)
        continue;

      if(s.getState().getBase() instanceof Variable) {
        Variable var = (Variable) s.getState().getBase();

        if((s instanceof Read) || (s instanceof Increment)) {
          if(map.containsKey(var.getIndex())) {
            map.get(var.getIndex()).unify(var.getType());
          } else {
            map.put(var.getIndex(), var.getType());
          }
        } else if(s instanceof Write) {
          if(s.getState() == s.getState().getBase()) {
            if(map.containsKey(var.getIndex())) {
              map.get(var.getIndex()).unify(var.getType());
            }

            map.remove(var.getIndex());
          }
        }
      }
    }

    storeResult(bb, map);

    return map;
  }

  /**
   * Performs analysis of simple natural loop.
   *
   * @param  loop  Natural Loop.
   * @return       Result for loop.
   */
  @Override
  public Map<Integer, Type> visit(Loop loop) {
    // Check not already in flight.
    if(inFlight.contains(loop)) {
      return Collections.emptyMap();
    }

    inFlight.add(loop);

    Map<Integer, Type> map = unionSuccessors(loop);

    // Store default successor for loop end.
    defaultSuccessors.put(loop.getEnd(), loop);

    // Union in loop body.
    for(Map.Entry<Integer, Type> e : loop.getStart().accept(this).entrySet()) {
      if(map.containsKey(e.getKey())) {
        map.get(e.getKey()).unify(e.getValue());
      } else {
        map.put(e.getKey(), e.getValue());
      }
    }

    storeResult(loop, map);

    return map;
  }


  /**
   * Performs analysis of trivial loop.
   *
   * @param  loop  Trivial Loop.
   * @return       Result for trivial loop.
   */
  @Override
  public Map<Integer, Type> visit(TrivialLoop loop) {
    // Check not already in flight.
    if(inFlight.contains(loop)) {
      return Collections.emptyMap();
    }

    inFlight.add(loop);
    
    Map<Integer, Type> map = unionSuccessors(loop);

    // Store default successor for loop end.
    defaultSuccessors.put(loop.getEnd(), loop);

    // Union in loop body.
    for(Map.Entry<Integer, Type> e : loop.getStart().accept(this).entrySet()) {
      if(map.containsKey(e.getKey())) {
        map.get(e.getKey()).unify(e.getValue());
      } else {
        map.put(e.getKey(), e.getValue());
      }
    }

    // Effect of limit (only need Reads since calls don't effect variable
    // liveness, and Writes/Increments don't return values so won't be in tree).
    for(Read read : InstructionCollector.collect(loop.getLimit(), Read.class)) {
      if(read.getState().getBase() instanceof Variable) {
        Variable var = (Variable) read.getState().getBase();

        if(map.containsKey(var.getIndex())) {
          map.get(var.getIndex()).unify(var.getType());
        } else {
          map.put(var.getIndex(), var.getType());
        }
      }
    }

    storeResult(loop, map);

    return map;
  }

  /**
   * Returns the set of live variables at the start of the given block.
   *
   * @param  block Block.
   * @return       Set of live variables.
   */
  public Set<State> getLive(Block block) {
    Set<State> result = new HashSet<State>();

    for(Map.Entry<Integer, Type> e : live.get(block).entrySet()) {
      result.add(new Variable(e.getKey().intValue(), e.getValue()));
    }

    return result;
  }
}
