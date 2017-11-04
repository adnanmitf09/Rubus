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

package graph;

import java.util.Set;

import analysis.BlockCollector;

/**
 * Represents natural loops with in the control flow graph. There is also the
 * added constraint that the loop must only have a single exit destination.
 */
public class Loop extends Block {
  /**
   * Start of main loop body.
   */
  private Block start;

  /**
   * End of main loop body.
   */
  private Block end;
  
  /**
   * Constructor
   */
  public Loop(Block start, Block end) {
    this.start = start;
    this.end   = end;
  }

  /**
   * Sets the start of main loop body.
   *
   * @param start  First block in body.
   */
  public void setStart(Block start) {
    this.start = start;
  }
  
  /**
   * Returns start of main loop body.
   *
   * @return       First block in body.
   */
  public Block getStart() {
    return start;
  }

  /**
   * Sets the end of main loop body.
   *
   * @param end    Last block in body.
   */
  public void setEnd(Block end) {
    this.end = end;
  }

  /**
   * Returns end of main loop body.
   *
   * @return       Last block in body.
   */
  public Block getEnd() {
    return end;
  }

  /**
   * Returns the set of blocks within the loop body.
   *
   * @return       Set of blocks in loop body.
   */
  public Set<Block> getBody() {
    return BlockCollector.collect(start, getNext(), false, false);
  }
  
  /**
   * Acceptor for the BlockVisitor pattern.
   */
  public <T> T accept(BlockVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
