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

import graph.state.State;
import graph.state.Variable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.ObjectUtil;
import cl.Config;

/**
 *
 */
public class Kernel extends Method {
  public static class Parameter {
    private final boolean copyOut;
    private final State   state;

    public Parameter(State state, boolean copyOut) {
      this.state   = state;
      this.copyOut = copyOut;
    }

    public State getState() {
      return state;
    }

    public boolean getCopyOut() {
      return copyOut;
    }
  }

  private final List<Parameter> paramDetails;
  private final List<Variable> indices;
  private final List<Map<Variable, Integer>> increments;

  public Kernel(String name, List<Variable> indices, List<Map<Variable, Integer>> increments, List<Parameter> parameters) {
    super(name, fullParameters(indices.size(), parameters), Type.getType("V"));
    
    this.indices      = indices;
    this.increments   = increments;
    this.paramDetails = parameters;
  }

  public int getDimensions() {
    return indices.size();
  }

  public Variable getIndex(int dimension) {
    return indices.get(dimension);
  }

  public Map<Variable, Integer> getIncrements(int dimension) {
    return increments.get(dimension);
  }

  public List<Parameter> getRealParameters() {
    return paramDetails;
  }

  @Override
  public List<Variable> getParameterVariables() {
    List<Variable> result = new LinkedList<Variable>();

    for(Parameter param : paramDetails) {
      if(param.getState() instanceof Variable) {
        result.add((Variable) param.getState());
      }
    }

    return Collections.unmodifiableList(result);
  }

  public int getRealParameterCount() {
    return paramDetails.size();
  }

  @Override
  public String toString() {if(Config.printObjectDescriptionInToString) ObjectUtil.println(this);
    return getName() + "/" + getDimensions() + indices + ":" + getDescriptor();
  }

  private static Type[] fullParameters(int dimensions, List<Parameter> parameters) {
    Type[] fullParams = new Type[dimensions + parameters.size()];

    // Limit parameters for each dimension
    for(int d = 0; d < dimensions; d++) {
      fullParams[d] = Type.INT;
    }

    // Copy standard parameters
    for(int i = 0; i < parameters.size(); i++) {
      fullParams[i + dimensions] = parameters.get(i).getState().getType();
    }

    return fullParams;
  }
}
