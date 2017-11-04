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
import graph.CodeVisitor;

import java.util.Collections;
import java.util.Set;

/**
 * 
 */
public class Condition implements Branch {
  /**
   * Represents possible conditional operators.
   */
  public enum Operator {
    EQ, NE, LT, GE, GT, LE;

    /**
     * Returns the reverse operator.
     *
     * @return     Reverse operator.
     */
    public Operator reverse() {
      switch(this) {
        case LT: return GT;
        case GE: return LE;
        case GT: return LT;
        case LE: return GE;
        default: return this;
      }
    }

    /**
     * Returns the negation of the operator.
     *
     * @return     Negated operator.
     */
    public Operator not() {
      switch(this) {
        case EQ: return NE;
        case NE: return EQ;
        case LT: return GE;
        case GE: return LT;
        case GT: return LE;
        case LE: return GT;
        default: return null; // Should never occur.
      }
    }
  };

  private Block destination;
  private Operator operator;
  private Producer operandA;
  private Producer operandB;
  
  public Condition(Operator operator, Block dest, Producer a, Producer b) {
    this.destination = dest;
    this.operator    = operator;
    this.operandA    = a;
    this.operandB    = b;
  }
  
  public Operator getOperator() {
    return operator;
  }

  public Producer getOperandA() {
    return operandA;
  }

  public Producer getOperandB() {
    return operandB;
  }
  
  public Producer[] getOperands() {
    return new Producer[] {operandA, operandB};
  }

  public Block getDestination() {
    return destination;
  }

  public Set<Block> getDestinations() {
    return Collections.singleton(destination);
  }
  
  public String toString() {
    return "IF A " + operator + " B THEN " + destination;
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
    if(destination == a) {
      destination = b;
      return 1;
    } else {
      return 0;
    }
  }
  
  /**
   * Acceptor for the BlockVisitor pattern. Visits the block that can be
   * conditionally reached.
   */
  public <T> void accept(BlockVisitor<T> visitor) {
    destination.accept(visitor);
  }
  /**
   * Block which contains this branch
   */
  private BasicBlock parent;

public BasicBlock getParent() {
	return parent;
}

public void setParent(BasicBlock parent) {
	this.parent = parent;
} 

}
