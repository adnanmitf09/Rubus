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

import graph.BasicBlock;
import graph.Block;
import graph.BlockVisitor;
import graph.Loop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import util.Utils;
import analysis.BlockCollector;

/**
 * Detects loops in a control flow graph by considering each Block's dominators.
 * Where loops are found, these are replaced with Loop blocks in the control
 * flow graph. The search is restricted to natural loops (i.e. single entry)
 * which only have a single exit.
 *
 * The detection also rules out loops which contain RETURN instructions, since
 * a basic block can't contain both a RETURN and also a condition.
 */
public class LoopDetector {
  /**
   * Public interface for the class. Detects all loops in the given control flow
   * graph, and modifies the graph to instead have the relevant loop nodes.
   */
  public static Set<Loop> detect(Block graph) {
    boolean changed;
    Map<Block, Set<Block>> dominators = new HashMap<Block, Set<Block>>();
    Set<Loop> loops = new HashSet<Loop>();

    // Initialise
    for(Block b : BlockCollector.collect(graph)) {
      dominators.put(b, dominators.keySet());
    }
    
    // Iterate until no changes
    do {
      changed = false;
      
      for(Block s : dominators.keySet()) {
        Set<Block> set = null;
        
        for(Block p : s.getPredecessors()) {
          if(set == null) {
            set = new HashSet<Block>(dominators.get(p));
          } else {
            set.retainAll(dominators.get(p));
          }
        }
        
        if(set == null) {
          set = new HashSet<Block>();
        }
        
        set.add(s);
        
        if(!set.equals(dominators.get(s))) {
          dominators.put(s, set);
          changed = true;
        }
      }
    } while(changed);
    
    // Detect loops
    for(Block end : dominators.keySet()) {
      Set<Block> loopStarts = new HashSet<Block>(dominators.get(end));
      
      // Gives all successors of `end' that are also among its dominators.
      loopStarts.retainAll(end.getSuccessors());
      
      // Natural loops can only have 1 start.
      if(loopStarts.size() != 1) {
        continue;
      }
      
      // Get loop start
      Block start = Utils.getSingleElement(loopStarts);
      
      // Discover loop body and exits
      final Set<Block> body = new HashSet<Block>();
      final Set<Block> exits = new HashSet<Block>();
      body.add(start);
      exits.addAll(start.getSuccessors());
      
      end.accept(new BlockVisitor<Void>() {
        @Override
        public Void visit(Block b) {
          if(!body.contains(b)) {
            body.add(b);
            exits.addAll(b.getSuccessors());
            visit(b.getPredecessors());
          }
          
          return null;
        }

        @Override
        public Void visit(Loop b) {
          if(!body.contains(b)) {
            body.add(b);
            body.addAll(b.getBody());
            exits.addAll(b.getSuccessors());
            visit(b.getPredecessors());
          }

          return null;
        }
      });

      exits.removeAll(body);

      // Only detect loops with a single exit.
      if(exits.size() != 1) {
        if(start.getLineNumber() == null) {
          Logger.getLogger("loops.detect").info("Multiple exits from loop.");
        } else {
          Logger.getLogger("loops.detect").info(
            "Multiple exits from loop on line " + start.getLineNumber() + "."
          );
        }
        continue;
      }

      // Inserts sentinel at end of loop so that end never changes.
      BasicBlock sentinel = new BasicBlock();
      end.replace(start, sentinel);

      // Create Loop block.
      Loop loop = new Loop(start, sentinel);
      loop.setLineNumber(start.getLineNumber());
      loop.setNext(Utils.getSingleElement(exits));
      loops.add(loop);

      // Put loop into graph.
      for(Block b : start.getPredecessors()) {
        b.replace(start, loop);
      }

      // User feedback
      if(loop.getLineNumber() == null) {
        Logger.getLogger("loops.detect").info("Natural loop detected.");
      } else {
        Logger.getLogger("loops.detect").info(
          "Natural loop on line " + loop.getLineNumber() + "."
        );
      }
    }

    return loops;
  }
}
