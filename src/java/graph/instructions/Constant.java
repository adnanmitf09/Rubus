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

import graph.CodeVisitor;
import graph.Type;

/**
 * 
 */
public class Constant implements Producer {
  private Object constant;
  
  public Constant(Object constant) {
    this.constant = constant;
  }
  
  @Override
  public Type getType() {
    if(constant == null) {
      return Type.REF;
    } else if(constant.getClass() == Boolean.class) {
      return Type.BOOL;
    } else if(constant.getClass() == Byte.class) {
      return Type.BYTE;
    } else if(constant.getClass() == Character.class) {
      return Type.CHAR;
    } else if(constant.getClass() == Short.class) {
      return Type.SHORT;
    } else if(constant.getClass() == Integer.class) {
      return Type.INT;
    } else if(constant.getClass() == Float.class) {
      return Type.FLOAT;
    } else if(constant.getClass() == String.class) {
      return Type.getType("Ljava/lang/String;");
    } /*else if(constant.getClass() == Type.class) {
      return (Type) constant;
    }*/ else if(constant.getClass() == Long.class) {
      return Type.LONG;
    } else if(constant.getClass() == Double.class) {
      return Type.DOUBLE;
    } else {
      return null;
    }
  }

  public Object getConstant() {
    return constant;
  }
  
  @Override
  public Producer[] getOperands() {
    return new Producer[] {};
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Constant) {
      Constant c = (Constant) obj;

      return (constant == c.constant) || (constant.equals(c.constant));
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + (this.constant != null ? this.constant.hashCode() : 0);
    return hash;
  }
  
  @Override
  public String toString() {
    return "CONST " + constant + " (" + getType() + ")";
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
}
