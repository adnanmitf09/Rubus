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

package debug;

import graph.Block;
import graph.BlockVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * Ensures that all blocks are labelled with a line number, propogating this
 * from a predecessor if necessary.
 */
public class LinePropagator extends BlockVisitor<Void> {
  private Set<Block> visited = new HashSet<Block>();

  /**
   * Visits a block and copies its line number to its successor.
   *
   * @param  block Block to visit.
   * @return       <code>null</code>.
   */
  @Override
  public Void visit(Block block) {
    // Only visit once.
    if(visited.contains(block)) {
      return null;
    }

    visited.add(block);

    // Propagate line number if appropriate.
    if((block.getNext() != null) && (block.getNext().getLineNumber() == null)) {
      block.getNext().setLineNumber(block.getLineNumber());
    }

    // Visit Successors
    visit(block.getSuccessors());

    return null;
  }
}
