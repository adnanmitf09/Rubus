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

import graph.Block;
import graph.BlockVisitor;
import graph.CodeVisitor;
import graph.Type;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class TryCatch implements Branch {
  private Block start;
  private Block end;
  private Block handler;
  private Type  exception;

  public TryCatch(Block start, Block end, Block handler, Type exception) {
    this.start     = start;
    this.end       = end;
    this.handler   = handler;
    this.exception = exception;
  }

  public Block getStart() {
    return start;
  }

  public Block getEnd() {
    return end;
  }

  public Block getHandler() {
    return handler;
  }

  public Type getExceptionType() {
    return exception;
  }

  @Override
  public Set<Block> getDestinations() {
    Set<Block> dests = new HashSet<Block>();

    dests.add(start);
    dests.add(handler);

    return dests;
  }

  @Override
  public Producer[] getOperands() {
    return new Producer[] {};
  }

  @Override
  public <T> T accept(CodeVisitor<T> visitor) {
    if(visitor.getResult(this) != null) {
      return visitor.getResult(this);
    } else {
      T result = visitor.visit(this);

      visitor.putResult(this, result);

      return result;
    }
  }

  @Override
  public int replace(Block a, Block b) {
    int count = 0;

    if(start   == a) { start   = b; count++; }
    if(end     == a) { end     = b; count++; }
    if(handler == a) { handler = b; count++; }

    return count;
  }

  /**
   * Acceptor for the BlockVisitor pattern. Visits each of the blocks that can
   * be reached through the try catch block.
   */
  @Override
  public <T> void accept(BlockVisitor<T> visitor) {
    start.accept(visitor);
    handler.accept(visitor);
  }
}
