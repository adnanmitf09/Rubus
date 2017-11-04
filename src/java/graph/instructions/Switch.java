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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class Switch implements Branch {
  private Map<Integer, Block> destinations;
  private Block               dflt;
  
  private Producer operand;
  
  public Switch(Producer op, Map<Integer, Block> dests, Block dflt) {
    this.operand      = op;
    this.destinations = dests;
    this.dflt         = dflt;
  }

  public Map<Integer, Block> getMapping() {
    return Collections.unmodifiableMap(destinations);
  }

  public Block getDefault() {
    return dflt;
  }
  
  public Set<Block> getDestinations() {
    Set<Block> dests = new HashSet<Block>();
    
    for(Map.Entry<Integer, Block> e : destinations.entrySet()) {
      dests.add(e.getValue());
    }
    
    dests.add(dflt);
    
    return dests;
  }

  public Producer getOperand() {
    return operand;
  }

  public Producer[] getOperands() {
    return new Producer[] {operand};
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
    int count = 0;

    // Switch Destinations
    for(Map.Entry<Integer, Block> entry : destinations.entrySet()) {
      if(entry.getValue() == a) {
        entry.setValue(b);
        count++;
      }
    }

    // Default Destination
    if(dflt == a) {
      dflt = b;
      count++;
    }
    
    return count;
  }
  
  /**
   * Acceptor for the BlockVisitor pattern. Visits each of the blocks that can
   * be reached through the switch.
   */
  public <T> void accept(BlockVisitor<T> visitor) {
    // Default Destination
    dflt.accept(visitor);
    
    // Other Destinations
    for(Map.Entry<Integer, Block> e : destinations.entrySet()) {
      e.getValue().accept(visitor);
    }
  }
}
