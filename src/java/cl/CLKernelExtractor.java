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
import graph.Block;
import graph.ClassNode;
import graph.Modifier;
import graph.TrivialLoop;
import graph.instructions.Call;
import graph.instructions.Constant;
import graph.instructions.Producer;
import graph.instructions.Read;
import graph.instructions.Write;
import graph.state.State;
import graph.state.Variable;

import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import util.ObjectUtil;
import util.Tree;
import util.Utils;
import analysis.BlockCollector;
import analysis.dataflow.AliasUsed;
import analysis.dataflow.LiveVariable;
import analysis.dataflow.ReachingConstants;
import analysis.dataflow.SimpleUsed;
import analysis.dependency.DependencyCheck;

	/**
	 *
	 */
	public class CLKernelExtractor {
	  /**
	   * Attempts to extract all kernels from the given nested loop tree, according
	   * to judgements made by the given dependency checker.
	   *
	   * @param clazz  Class concerned.
	   * @param loops  Root level set of loop nestings.
	   * @param check  Dependency checker.
	   * @return       Number of kernels successfully extracted.
	   */
	  public static int extract(ClassNode clazz, Set<Tree<TrivialLoop>> loops, DependencyCheck check) {
 	    Deque<Tree<TrivialLoop>> consider = new LinkedList<Tree<TrivialLoop>>();

	    int count = 0;

	    consider.addAll(loops);

	    while(!consider.isEmpty()) {
	      Tree<TrivialLoop> level = consider.remove();

	      if(extract(clazz, level, check)) {
	        count++;
	      } else {
	        consider.addAll(level.getChildren());
	      }
	    }

	    return count;
	  }

	  /**
	   * Attempts to extract the given loop level as a kernel, including any nested
	   * loops in the kernel if possible (i.e. multiple dimensions).
	   * 
	   * @param  clazz Class concerned.
	   * @param  level Outer loop to first consider.
	   * @param  check Dependency checker.
	   * @return       <code>true</code> if the extraction succeeded,
	   *               <code>false</code> otherwise.
	   */
	  public static boolean extract(ClassNode clazz, Tree<TrivialLoop> level, DependencyCheck check) {
	    Deque<TrivialLoop> loops = new LinkedList<TrivialLoop>();

	    // Various lists for creating kernel.
	    List<Variable>               indices    = new LinkedList<Variable>();
	    List<Producer>               limits     = new LinkedList<Producer>();
	    List<Map<Variable, Constant>>constants  = new LinkedList<Map<Variable, Constant>>();
	    List<Map<Variable, Integer>> increments = new LinkedList<Map<Variable, Integer>>();
	    List<CLKernel.Parameter>       parameters = new LinkedList<CLKernel.Parameter>();
	    
	    
	    
	    // First check that one level is possible.
 	    if(!check.check(level.getValue())) {
	       return false;
	    }
	    
	    // Add first level.
	    constants.add(Collections.EMPTY_MAP);
	    indices.add(level.getValue().getIndex());
	    limits.add(level.getValue().getLimit());
	    increments.add(level.getValue().getIncrements());

	    loops.add(level.getValue());
	    level = Utils.getSingleElement(level.getChildren());

	    // Determine the number of dimensions.
	    while(level != null) {
	      // Check inner loop (includes checking limit doesn't depend on loop).
	      if(!check.check(level.getValue())) {
	        break;
	      }

	      // Pre-inner loop region.
	      Set<Block> preRegion = BlockCollector.collect(
	        loops.peekLast().getStart(),
	        level.getValue(),
	        true,
	        false
	      );

	      SimpleUsed pre = new SimpleUsed(preRegion);

	      // Post-inner loop region.
	      SimpleUsed post = new SimpleUsed(
	        BlockCollector.collect(level.getValue().getNext())
	      );

	      // Ensure that pre-inner loop region defines exactly the increment set.
	      Map<Variable, Constant> loopConstants = new ReachingConstants(preRegion)
	                                            .getResultAtStart(level.getValue());

	      if(!loopConstants.keySet().equals(level.getValue().getIncrements().keySet())) {
	        break;
	      }

	      // Neither pre or post region should not have any reference writes.
	      if(pre.containsReferenceWrites() || post.containsReferenceWrites()) {
	        break;
	      }

	      // Post region should just have direct writes to increment variables.
	      if(!loops.peekLast().getIncrements().keySet().containsAll(post.getDirectWrites())) {
	        break;
	      }

	      // Collect constants, indices, increment and limit lists.
	      constants.add(loopConstants);
	      indices.add(level.getValue().getIndex());
	      limits.add(level.getValue().getLimit());
	      increments.add(level.getValue().getIncrements());

	      
	      System.out.println(/*************    extract   ***************/);
		    ObjectUtil.println(level);
		    System.out.println(/*************    extract   ***************/);

	      
	      
	      // Get next level.
	      loops.add(level.getValue());
	      level = Utils.getSingleElement(level.getChildren());
	    }

	    // Reverse Lists (dimension 0 should be inner most).
	    Collections.reverse(constants);
	    Collections.reverse(indices);
	    Collections.reverse(limits);
	    Collections.reverse(increments);
	    Collections.reverse((LinkedList) loops);

	    // Kernel body.
	    Block body = loops.peekFirst().getStart();
	    // Line Number String
	    String line = (body.getLineNumber() == null)
	                                  ? "" : " (line " + body.getLineNumber() + ")";

	    // Kernel Parameters: Copy-In Variables
	    SimpleUsed used = new SimpleUsed(body);
	    Set<State> copyIn = new LiveVariable(body).getLive(body);

	    copyIn.addAll(used.getStatics());

	    // Determine 'copy-out' state.
	    AliasUsed alias = new AliasUsed(body, copyIn);

	    // Note: used.getWrites() should be disjoint with LIVE after loop - unless
	    //       dependency checks have gone wrong.

	    // Kernel Parameters
	    for(State s : copyIn) {
	      parameters.add(
	        new CLKernel.Parameter(
	          s,
	          alias.getBaseWrites().contains(s) || !alias.isAccurate()
	        )
	      );
	    }

	   // System.out.println("Kernel Body=>"+ObjectUtil.toString(body));
	   
	    
	    
	    // Create kernel.
	    
	    CLKernel kernel = new CLKernel(
	      "kernel_" + (loops.hashCode() > 0 ? loops.hashCode() : "M" + -loops.hashCode()),
	      indices,
	      increments,
	      parameters
	    );

	    kernel.setImplementation(body);
	   // kernel.print();
	    kernel.getModifiers().addAll(EnumSet.of(Modifier.STATIC, Modifier.PUBLIC));
      
	    
	    // set the owner of the method (package and class name)
		   
//		 kernel.setOwner(ClassImporter.getClass(CLExporter.getGeneratedSourceFileNameWithPackage()));
	    // Now method is being add in same class so same class is owner of this new method
	    kernel.setOwner(clazz);
	    
	   // call methods from static class, new method would be merged in same class on next step
	   //clazz.addMethod(kernel);

	 
	    
	   
	   
	    /***
	     * 
	     *   export in openCL C
	     */
	    try {
	      CLExporter.export(kernel);
	    } catch(CLUnsupportedInstruction e) {
	      Logger.getLogger("extract").warn(
	        "CLKernel" + line + " could not be export into OpenCL (" + e + ")."
	      );
	
	     clazz.removeMethod(kernel);
	      return false;
	    }
	    
	    // we don't want this method in class while export because it would be called from generated source
	   // clazz.removeMethod(kernel);
	    

	  
	    
	    BasicBlock invoke = new BasicBlock();

	    // Constant initialisation.
	    for(Map<Variable, Constant> map : constants) {
	      for(Map.Entry<Variable, Constant> pair : map.entrySet()) {
	        invoke.getStateful().add(new Write(pair.getKey(), pair.getValue()));
	      }
	    }

	    // Actual Call
	    Producer[] arguments = new Producer[kernel.getParameterCount()];

	    int i = 0;

	    for(Producer limit : limits) {
	      arguments[i++] = limit;
	    }
	    
	    for(CLKernel.Parameter p : kernel.getRealParameters()) {
	      arguments[i++] = new Read(p.getState());
	    }
	    
	   
      
	    invoke.getStateful().add(
	      new Call(arguments, kernel, Call.Sort.STATIC)
	    );

	    // Replace outer loop with invocation.
	    loops.peekLast().replace(invoke);
	   // Field field = new Field("test_field", Type.BOOL);
	   // clazz.addField(field);
	    // User Feedback
	       
	    	    Logger.getLogger("extract").info(
	      "Kernel of " + indices.size() + " dimensions extracted" + line + "."
	    );

	    Logger.getLogger("extract").info("   Copy In: " + copyIn);
	    Logger.getLogger("extract").info("   Copy Out: " + alias.getBaseWrites());
	    
	    return true;
	  }
	}
