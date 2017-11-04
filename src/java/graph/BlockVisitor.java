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

import java.util.Collection;
import java.util.HashSet;

/**
 * Template class for visitors to the controlflow graph. Methods in this class
 * delegate to the block's parent class, so that extensions of this class
 * can choose to implement either a general or specific visitor.
 */
public abstract class BlockVisitor<T> {
  /**
   * Visits each of the blocks in the collection, causing each to be visited by
   * this visitor.
   *
   * TODO: Currently throws away results, possibly should return array?
   * TODO: Copies collection so that no concurrent modification problems, messy!
   */
  public void visit(Collection<Block> blocks) {
    for(Block b : new HashSet<Block>(blocks)) {
      if(b != null) b.accept(this);
    }
  }
  
  /**
   * General method that visits a block (of any type).
   */
  public T visit(Block block) {
    return null;
  }
  
  /**
   * Method for visiting BasicBlocks. This default implementation causes the
   * delegation to the default block visit method. This is required so that
   * implementations of BlockVisitors can opt between implementing specific
   * visit methods or not.
   */
  public T visit(BasicBlock block) {
    return visit((Block) block);
  }
  
  /**
   * Method for visiting Loop. This default implementation causes the
   * delegation to the default block visit method. This is required so that
   * implementations of BlockVisitors can opt between implementing specific
   * visit methods or not.
   */
  public T visit(Loop block) {
    return visit((Block) block);
  }

  /**
   * Method for visiting TrivialLoop. This default implementation causes the
   * delegation to the default Loop visit method. This is required so that
   * implementations of BlockVisitors can opt between implementing specific
   * visit methods or not.
   */
  public T visit(TrivialLoop block) {
    return visit((Loop) block);
  }
}
