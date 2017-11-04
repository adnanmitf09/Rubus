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

package graph.instructions;

import graph.BasicBlock;
import graph.Block;
import graph.BlockVisitor;

import java.util.Set;

public interface Branch extends Instruction {
  /**
   * Returns a set of any destination blocks used by the branch.
   */
  public Set<Block> getDestinations();

  /**
   * Replace any occurances of <code>a</code> within the branch's destinations
   * with <code>b</code>, returning the number of replacements made.
   *
   * @param a      Block to be replaced.
   * @param b      Replacement block.
   * @return       Number of replacements made.
   */
  public int replace(Block a, Block b);
  
  /**
   * Acceptor for the BlockVisitor pattern. This should cause visits to any
   * blocks referenced by the branch.
   */
  public abstract <T> void accept(BlockVisitor<T> visitor);


}
