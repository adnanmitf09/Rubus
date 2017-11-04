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

package analysis;

import graph.Block;
import graph.BlockVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows all blocks in a graph to be collected into a set.
 */
public class BlockCollector extends BlockVisitor<Set<Block>> {
  /**
   * Running set of collected blocks.
   */
  private Set<Block> blocks = new HashSet<Block>();

  /**
   * Limit of collection (exclusive).
   */
  private Block limit;

  /**
   * Should collection converge.
   */
  private boolean converge;

  /**
   * Collects all blocks from a given starting point. This will include all
   * those until the end of a function, loop, etc.
   *
   * @param  start Starting point (included in result).
   * @return       Set of blocks.
   */
  public static Set<Block> collect(Block start) {
    return start.accept(new BlockCollector(null, false));
  }

  /**
   * Collects all blocks from a given starting point to a given end point.
   *
   * @param  start Starting point (included in result).
   * @param  end   End point (included in result).
   * @return       Set of blocks.
   */
  public static Set<Block> collect(Block start, Block end, boolean converge, boolean inclusive) {
    Set<Block> result = start.accept(new BlockCollector(end, converge));

    if(inclusive) {
      result.add(end);
    }

    return result;
  }

  /**
   * Private constructor to prevent usage from outside the class. Initialises
   * the block that should be considered the limit of the collection, and also
   * whether the collection must converge to this limit.
   *
   * @param  limit Set of blocks which mark end of collection.
   *
   */
  private BlockCollector(Block limit, boolean converge) {
    this.limit    = limit;
    this.converge = converge;
  }

  /**
   * Adds the block to the running set, and recurses on its successors if it is
   * yet to be visited.
   * 
   * @param  b     Block.
   * @return       The running set.
   */
  @Override
  public Set<Block> visit(Block b) {
    if(!blocks.contains(b) && (b != limit)) {
      blocks.add(b);

      // Disallow dead ends in convergence case.
      if((b.getSuccessors().size() == 0) && converge) {
        throw new RuntimeException("BlockCollector: Collection not properly bounded.");
      }

      // Recurse
      visit(b.getSuccessors());
    }

    return blocks;
  }
}
