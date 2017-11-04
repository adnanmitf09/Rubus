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

import java.util.Collections;
import java.util.Set;

/**
 * 
 */
public class Return implements Branch {
  public Set<Block> getDestinations() {
    return Collections.emptySet();
  }
  
  public Producer[] getOperands() {
    return new Producer[] {};
  }
  
  public String toString() {
    return "VOID RETURN";
  }
  
  public <T> T accept(CodeVisitor<T> visitor) {
    if(visitor.getResult(this) != null) {
      return visitor.getResult(this);
    } else {
      T result = visitor.visit(this);
      
      visitor.putResult(this, result);
      
      return result;
    }
  }

  public int replace(Block a, Block b) {
    return 0;
  }

  /**
   * Acceptor for the BlockVisitor pattern. Since returns don't reference any
   * blocks, this needn't do anything.
   */
  public <T> void accept(BlockVisitor<T> visitor) {
    // Nothing
  }
}
