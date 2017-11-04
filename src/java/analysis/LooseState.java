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

package analysis;

import graph.instructions.Producer;
import graph.instructions.Read;
import graph.state.ArrayElement;
import graph.state.Field;
import graph.state.InstanceField;
import graph.state.State;
import graph.state.Variable;
import util.ObjectUtil;
import cl.Config;

/**
 * Allows states to be compared without them appearing different due to reads
 * occuring at different times, and also discounts differences in array
 * indicies.
 */
public class LooseState {
  private State internal;

  public LooseState(State internal) {
    this.internal = internal;
  }

  public State getInternal() {
    return internal;
  }

  public CanonicalState toCanonical() {
    return new CanonicalState(internal);
  }

  @Override
  public boolean equals(Object obj) {
    // Object must be CanonicalState
    if(obj.getClass() != getClass()) {
      return false;
    }

    return compare(internal, ((LooseState) obj).internal);
  }

  @Override
  public int hashCode() {
    return 0;
  }

  private boolean compare(State state1, State state2) {
    // Must be same type.
    if(state1.getClass() != state2.getClass()) {
      return false;
    }

    // Variables or statics.
    if((state1 instanceof Variable) || (state1 instanceof Field)) {
      return state1.equals(state2);
    // Array Elements
    } else if(state1 instanceof ArrayElement) {
      ArrayElement element1 = (ArrayElement) state1;
      ArrayElement element2 = (ArrayElement) state2;

      return compare(element1.getArray(), element2.getArray());
    // Object Fields
    } else if(state1 instanceof InstanceField) {
      InstanceField field1 = (InstanceField) state1;
      InstanceField field2 = (InstanceField) state2;

      return field1.getField().equals(field2.getField())
                             && compare(field1.getObject(), field2.getObject());
    // Any others
    } else {
      return state1.equals(state2);
    }
  }

  private boolean compare(Producer producer1, Producer producer2) {
    State state1, state2;

    // Same
    if(producer1 == producer2) {
      return true;
    }

    // First state
    if(producer1 instanceof Read) {
      state1 = ((Read) producer1).getState();
    } else {
      return false;
    }

    // Second state
    if(producer2 instanceof Read) {
      state2 = ((Read) producer2).getState();
    } else {
      return false;
    }

    // Compare states
    return compare(state1, state2);
  }

  @Override
  public String toString() {
	  if(Config.printObjectDescriptionInToString) ObjectUtil.println(this);
    return internal.toString();
  }
}
