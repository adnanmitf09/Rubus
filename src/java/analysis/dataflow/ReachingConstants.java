/*
 * Parallelising JVM Compiler
 * Part II Project, Computer Science Tripos
 *
 * Copyright (c) 2009, 2010 - Peter Calvert, University of Cambridge
 */

package analysis.dataflow;

import graph.Block;
import graph.Loop;
import graph.instructions.Constant;
import graph.instructions.Increment;
import graph.instructions.Stateful;
import graph.instructions.Write;
import graph.state.Variable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import analysis.BlockCollector;

/**
 *
 */
public class ReachingConstants extends Dataflow<Constant> {
  private Map<Loop, ReachingConstants> children = new HashMap<Loop, ReachingConstants>();

  public ReachingConstants(Block graph) {
    addBlocks(BlockCollector.collect(graph));
    calculate();
  }

  public ReachingConstants(Set<Block> blocks) {
    addBlocks(blocks);
    calculate();
  }

  /**
   * Returns the result of the analysis for the start of a given block (i.e.
   * before the given block has any effect).
   *
   * @param  block Block to give result for.
   * @return       Result.
   */
  public Map<Variable, Constant> getResultAtStart(Block block) {
    return Collections.unmodifiableMap(intersectPredecessors(block));
  }

  @Override
  protected void consider(Map<Variable, Constant> map, Stateful stateful) {
    if(stateful instanceof Write) {
      Write write = (Write) stateful;

      if(write.getValue() instanceof Constant) {
        map.put((Variable) write.getState(), (Constant) write.getValue());
      } else {
        map.put((Variable) write.getState(), null);
      }
    } else if(stateful instanceof Increment) {
      Increment increment = (Increment) stateful;
      Constant  constant = map.get((Variable) stateful.getState());

      // If we have an integer constant, can simply increment.
      if((constant != null) && constant.getType().isIntBased()) {
        int value = ((Number) constant.getConstant()).intValue();

        map.put(
          (Variable) stateful.getState(),
          new Constant(new Integer(value + increment.getIncrement()))
        );
      // Otherwise have to blacklist.
      } else {
        map.put((Variable) stateful.getState(), null);
      }
    }
  }

  @Override
  protected Dataflow<Constant> recurse(Loop loop) {
    if(!children.containsKey(loop)) {
      children.put(loop, new ReachingConstants(loop.getBody()));
    }

    return children.get(loop);
  }
}
