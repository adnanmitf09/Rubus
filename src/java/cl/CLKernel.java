package cl;
/*
 * Rubus: A Compiler for Seamless and Extensible Parallelism
 * 
 * Copyright (C) 2017 Muhammad Adnan - University of the Punjab
 * 
 * This file is part of Rubus.
 * Rubus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.

 * Rubus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Rubus. If not, see <http://www.gnu.org/licenses/>.
 */

import graph.BasicBlock;
import graph.Method;
import graph.Type;
import graph.instructions.Stateful;
import graph.state.State;
import graph.state.Variable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.ObjectUtil;

public class CLKernel extends Method {
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
//	    @Override
//	    public String toString() {
//	    	if(Config.printObjectDescriptionInToString) ObjectUtil.println(this);
//	    	return ObjectUtil.toString(Parameter.this);
//	    }
	  }

	  private final List<Parameter> paramDetails;
	  private final List<Variable> indices;
	  private final List<Map<Variable, Integer>> increments;

	  public CLKernel(String name, List<Variable> indices, List<Map<Variable, Integer>> increments, List<Parameter> parameters) {
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
	  public String toString() {
		  if(Config.printKernel) ObjectUtil.println(this);
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
	  
	  /**
	    * Javac optimizes some branches to avoid goto->goto, branch->goto etc.  
	    * 
	    * This method specifically deals with reverse branches which are the result of such optimisations. 
	    * 
	    * <code><pre>
	    * 
	    * </pre></code>
	    * 
	    * Read this function: Adnan
	    * 
	    */
//	   public void deoptimizeReverseBranches() {
//       
//		 BasicBlock bb = (BasicBlock) getImplementation(); 
//		 List<Stateful> instructions = bb.getStateful();
//		   
//	      for (Block instruction = getImplementation(); instruction != null; instruction = instruction.getNext()) {
//	          if(instruction instanceof)
//	    	  if (instruction.isBranch()) {
//	            final Branch branch = instruction.asBranch();
//	            if (branch.isReverse()) {
//	               final Instruction target = branch.getTarget();
//	               final LinkedList<Branch> list = target.getReverseUnconditionalBranches();
//	               if ((list != null) && (list.size() > 0) && (list.get(list.size() - 1) != branch)) {
//	                  final Branch unconditional = list.get(list.size() - 1).asBranch();
//	                  branch.retarget(unconditional);
//
//	               }
//	            }
//	         }
//	      }
//	   }
 public void print(){
	 BasicBlock bb = (BasicBlock) getImplementation(); 
	 for (BasicBlock instruction = bb; instruction != null; instruction = (BasicBlock) instruction.getNext()) {
	 List<Stateful> instructions = instruction.getStateful();
	 for (Stateful stateful : instructions) {
		System.out.println(stateful.getState());
	}
	 }
 }
	  
	  
	}