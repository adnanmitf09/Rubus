/*
 * Parallelising JVM Compiler
 * Part II Project, Computer Science Tripos
 *
 * Copyright (c) 2009, 2010 - Peter Calvert, University of Cambridge
 */

package analysis.dataflow;

import graph.Block;
import graph.Loop;
import graph.instructions.Increment;
import graph.instructions.Stateful;
import graph.instructions.Write;
import graph.state.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import analysis.BlockCollector;

/**
 *
 */
public class IncrementVariables extends Dataflow<Integer> {
  private Map<Loop, IncrementVariables> children = new HashMap<Loop, IncrementVariables>();

  public IncrementVariables(Block graph) {
    addBlocks(BlockCollector.collect(graph));
    calculate();
  }

  public IncrementVariables(Set<Block> blocks) {
    addBlocks(blocks);
    calculate();
  }

  public Map<Variable, Integer> getIncrements(Block block) {
    Map<Variable, Integer> result = new HashMap<Variable, Integer>();

    for(Map.Entry<Variable, Integer> entry : getResult(block).entrySet()) {
      if((entry.getValue() != null) && (entry.getValue() != 0)) {
        result.put(entry.getKey(), entry.getValue());
      }
    }

    return result;
  }

  public Map<Variable, Integer> getIncrements(Stateful instruction) {
    Map<Variable, Integer> result = new HashMap<Variable, Integer>();

    // Check whether result exists.
    if(getResult(instruction) == null) {
      return null;
    }

    // Convert result.
    for(Map.Entry<Variable, Integer> entry : getResult(instruction).entrySet()) {
      if((entry.getValue() != null) && (entry.getValue() != 0)) {
        result.put(entry.getKey(), entry.getValue());
      }
    }

    return result;
  }

  @Override
  protected void consider(Map<Variable, Integer> map, Stateful stateful) {
    if(stateful instanceof Increment) {
      if(stateful.getState() instanceof Variable) {
        Variable v = (Variable) stateful.getState();

        if(map.containsKey(v)) {
          if(map.get(v) != null) {
            int old = map.get(v).intValue();

            map.put(v, new Integer(old + ((Increment) stateful).getIncrement()));
          }
        } else {
          map.put(v, new Integer(((Increment) stateful).getIncrement()));
        }
      }
    } else if(stateful instanceof Write) {
      if(stateful.getState() instanceof Variable) {
        Variable v = (Variable) stateful.getState();

        map.put(v, null);
      }
    }
  }

  @Override
  protected Dataflow<Integer> recurse(Loop loop) {
    if(!children.containsKey(loop)) {
      children.put(loop, new IncrementVariables(loop.getBody()));
    }

    return children.get(loop);
  }
}
