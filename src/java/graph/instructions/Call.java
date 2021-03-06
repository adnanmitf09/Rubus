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
import graph.Method;
import graph.Type;
import graph.state.State;

public class Call implements Stateful, Producer {
  /**
   * Possible types of call (SPECIAL = non-virtual).
   */
  public enum Sort {
    VIRTUAL, INTERFACE, STATIC, SPECIAL
  }

  /**
   * Method being called.
   */
  private Method method;
  
  /**
   * Arguments to be passed to the method.
   */
  private Producer[] arguments;

  /**
   * Sort of call.
   */
  private Sort sort;
  
  /**
   * Standard constructor.
   */
  public Call(Producer[] args, Method method, Sort sort) {
    this.arguments  = args;
    this.method     = method;
    this.sort       = sort;
  }

  /**
   * Returns method that is called.
   */
  public Method getMethod() {
    return method;
  }

  /**
   * Returns the type of call.
   */
  public Sort getSort() {
    return sort;
  }

  /**
   * Returns the return type.
   */
  public Type getType() {
    return method.getReturnType();
  }

  /**
   * Unimplemented (throws exception) since calls don't affect a single state.
   */
  public State getState() {
    // TODO: Is this correct?
    return null;
  }

  /**
   * Returns arguments passed to the function.
   */
  public Producer[] getOperands() {
    return arguments;
  }
  
  /**
   * Output the textual name for the method.
   */
  @Override
  public String toString() {
    return "CALL " + method.toString();
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
}
