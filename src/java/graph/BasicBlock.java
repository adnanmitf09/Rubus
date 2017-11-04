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

import graph.instructions.Branch;
import graph.instructions.Condition;
import graph.instructions.Producer;
import graph.instructions.Stateful;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Represents basic blocks in the control flow graph. A basic block contains no
 * internal control flow except for at the last instruction, which may branch.
 * All instructions within the block that may be affect or be affected by state
 * are held in a timeline. On top of this timeline, a dataflow graph is kept.
 */
public class BasicBlock extends Block {
  /**
   * Values emitted by the basic block.
   */
  private List<Producer> valuesOut = Collections.emptyList();// Producer is super interface of instructions - adi

  /**
   * Types expected by the basic block.
   */
  private List<Type> typesIn = Collections.emptyList();

  /**
   * Flag as to whether expected types have been defined.
   */
  private boolean typesDefined = false;

  /**
   * Final branch on exit from the block (can be null). This will be either
   * a conditional, a switch or a return.
   */
  private Branch branch = null;
  
  /**
   * List of instructions related to state. These are listed in order.
   */
  private List<Stateful> stateful = new LinkedList<Stateful>();
  
  /**
   * Sets values emitted by the block.
   */
  public void setValuesOut(List<Producer> values) {
    valuesOut = Collections.unmodifiableList(values);
  }

  /**
   * Returns the values emitted by the block.
   */
  public List<Producer> getValuesOut() {
    return valuesOut;
  }

  /**
   * Sets the types expected by the block to those corresponding to the given
   * values. If this is not possible an exception is raised.
   */
  public void setTypesIn(List<Producer> values) {
    if(!typesDefined) {
      List<Type> types = new LinkedList<Type>();

      for(Producer value : values) {
        types.add(value.getType());
      }

      typesIn = Collections.unmodifiableList(types);
      typesDefined = true;
    } else {
      if(typesIn.size() != values.size()) {
        throw new RuntimeException("Number of values at entry to block differs.");
      }

      for(int i = 0; i < values.size(); i++) {
        typesIn.get(i).unify(values.get(i).getType());
      }
    }
  }

  /**
   * Gets the list of types expected by the block.
   */
  public List<Type> getTypesIn() {
    return typesIn;
  }

  /**
   * Gets (mutable) list of stateful instructions.
   */
  public List<Stateful> getStateful() {
    return stateful;
  }
  
  /**
   * Sets the branch for the block, and updates the relevant predecessor set for
   * the relevant destination block.
   */
  public void setBranch(Branch br) {
    // Remove this block from the predecessor sets of the old branch.
    if(branch != null) {
      for(Block b : branch.getDestinations()) {
        b.predecessors.remove(this);
      }
    }
  //adi
    if(br instanceof  Condition){
    	((Condition)br).setParent(this);
    }
    
    // Update to new branch.
    branch = br;
    
    if(branch != null) {
      for(Block b : branch.getDestinations()) {
        b.predecessors.add(this);
      }
    }
  }

  /**
   * Returns the optional branch at the end of the block.
   *
   * @return       Branch for the block.
   */
  public Branch getBranch() {
    return branch;
  }
  
  /**
   * Get successors.
   */
  @Override
  public Set<Block> getSuccessors() {
    Set<Block> set = new HashSet<Block>();
    
    if(branch != null) {
      set.addAll(branch.getDestinations());
      set.remove(null);
    }
    
    set.addAll(super.getSuccessors());
    
    return set;
  }

  /**
   * Replaces any occurence of <code>a</code> among the block's successors with
   * <code>b</code>. For a basic block, this requires that any replacements in
   * the conditional branch are checked.
   *
   * @param a      Block to be replaced.
   * @param b      Replacement block.
   */
  @Override
  public void replace(Block a, Block b) {
    // Use superclass for simple 'next' case.
    super.replace(a, b);

    // Deal with optional branch.
    if(branch != null) {
      int occurances = branch.replace(a, b);

      for(int i = 0; i < occurances; i++) {
        if(a != null) a.predecessors.remove(this);
        if(b != null) b.predecessors.add(this);
      }
    }
  }

  /**
   * Acceptor for the BlockVisitor pattern.
   */
  public <T> T accept(BlockVisitor<T> visitor) {
    return visitor.visit(this);
  }
  /**
   * Print block instructions
   */
  public void printBlock(){
	  for (Stateful ins:  stateful) {
		  System.out.println(ins);
	}

  }
  
  // adi
  private boolean haveBackEdge= false;

public boolean isHaveBackEdge() {
	return haveBackEdge;
}

public void setHaveBackEdge(boolean haveBackEdge) {
	this.haveBackEdge = haveBackEdge;
}
  
}

