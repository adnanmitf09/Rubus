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

package debug;

import graph.BasicBlock;
import graph.Block;
import graph.BlockVisitor;
import graph.CodeVisitor;
import graph.Loop;
import graph.Method;
import graph.TrivialLoop;
import graph.instructions.Instruction;
import graph.instructions.Stateful;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import util.TransformIterable;
import util.Utils;

/**
 * Outputs a Graphviz format code graph.
 */
public class GraphOutput extends BlockVisitor<Void> {
  /**
   * Height of stateful timeline entry.
   */
  private final double TIMELINE = 0.15;

  /**
   * Set of blocks that have been outputed.
   */
  private Set<Block> visited = new HashSet<Block>();

  /**
   * Map of instructions that have been outputed.
   */
  private Map<Instruction, String> instructions = new HashMap<Instruction, String>();

  /**
   * Output stream to write to.
   */
  private PrintStream ps;

  /**
   * Convenience method for outputting graph for a method to a given file.
   *
   * @param method  Method to output.
   * @param file    File for output.
   */
  public static void outputMethod(Method method, File file) {
    if(method.getImplementation() != null) {
      try {
        PrintStream outPS = new PrintStream(file);
        GraphOutput cfo = new GraphOutput(outPS);


        DebugUtil.printProperties(outPS);
//        DebugUtil.printNodeDiscription(outPS);

        method.getImplementation().accept(cfo);
        cfo.end();

        Logger.getLogger("diagrams").info("Diagram for " + method + " outputed to " + file + ".");
      } catch(IOException io) {
        Logger.getLogger("diagrams").warn("Could not output diagram to " + file + ".");
      }
    }
  }

  /**
   * Constructs an 'outputter' onto the given print stream.
   */
  public GraphOutput(PrintStream ps) {
    this.ps = ps;
    this.ps.println("digraph g {");
    }

  /**
   * Outputs a block, along with relevant edges.
   *
   * @param b      Block to output.
   * @return       <code>null</code>
   */
  @Override
  public Void visit(final BasicBlock b) {
    // Never visit a block twice.
    if(visited.contains(b)) return null;
    visited.add(b);

    // Output timeline.
    String label = Utils.join(new TransformIterable<Stateful, String>(b.getStateful()) {
      @Override
      protected String transform(Stateful obj) {
        instructions.put(obj, b.getID() + "\":\"" + System.identityHashCode(obj));

        return "<" + System.identityHashCode(obj) + "> " + obj.toString().replace(" ", "\\ ").replace("->", ".");
      }
    }, "|");

    double height = TIMELINE * b.getStateful().size();

    if(b.getBranch() != null) {
      if(label.length() == 0) {
        label = " <branch> " + b.getBranch().toString().replace(" ", "\\ ");
      } else {
        label += "| <branch> " + b.getBranch().toString().replace(" ", "\\ ");
      }

      instructions.put(b.getBranch(), b.getID() + "\":\"branch");
      height += TIMELINE;
    }

    // Output a node for the block.
    ps.println("\"" + b.getID() + "\" [shape=record, height="+height+", label=\"{" + label + "}\"];");

    // Output all instructions.
    for(Stateful s : b.getStateful()) {
      s.accept(new CodeVisitor<String>() {
        @Override
        public String visit(Instruction instruction) {
          if(!instructions.containsKey(instruction)) {
        
        	  ps.println("\"" + System.identityHashCode(instruction) + "\" [shape=circle , width=\"0.2\", label=\"" + instruction + "\"];");
            instructions.put(instruction, Integer.toString(System.identityHashCode(instruction)));
          }

          for(Instruction i : instruction.getOperands()) {
            ps.println("\"" + i.accept(this) +"\" -> \"" + instructions.get(instruction) + "\";");
          }

          return instructions.get(instruction);
        }
      });
    }

    // Output edges from block to its successors.
    for(Block p : b.getPredecessors()) {
      ps.println("\"" + p.getID() + "\" -> \"" + b.getID() + "\";");
    }

    // Output any successors.
    visit(b.getSuccessors());
    
    return null;
  }

  /**
   * Outputs a loop, along with relevant edges. The loop node is shaded.
   *
   * @param b      Loop to output.
   * @return       <code>null</code>
   */
  @Override
  public Void visit(Loop b) {
    // Never visit a loop twice.
    if(visited.contains(b)) return null;
    visited.add(b);

    // Output a node for the loop, shaded.
    DebugUtil.printNode(b,ps);


    // Output edges from block to its successors.
    for(Block p : b.getPredecessors()) {
      ps.println("\"" + p.getID() + "\" -> \"" + b.getID() + "\";");
    }

    // Output the loop body, connected by a dotted line.
    ps.println("\"" + b.getID() + "\" -> \"" + b.getStart().getID() + "\" [style=\"dotted\"];");
    b.getStart().accept(this);

    // Output any successors.
    visit(b.getSuccessors());
    
    return null;
  }

  /**
   * Outputs a trivial loop, along with relevant edges. The loop node is shaded.
   *
   * @param b      Loop to output.
   * @return       <code>null</code>
   */
  @Override
  public Void visit(TrivialLoop b) {
    // Never visit a loop twice.
    if(visited.contains(b)) return null;
    visited.add(b);

    // Output a node for the loop, shaded.
    DebugUtil.printNode(b,ps);

    // Output edges from block to its successors.
    for(Block p : b.getPredecessors()) {
      ps.println("\"" + p.getID() + "\" -> \"" + b.getID() + "\";");
    }

    // Output the loop body, connected by a dotted line.
    ps.println("\"" + b.getID() + "\" -> \"" + b.getStart().getID() + "\" [style=\"dotted\"];");
    b.getStart().accept(this);

    // Output any successors.
    visit(b.getSuccessors());

    return null;
  }

  /**
   * Ends the output.
   */
  public void end() {
    ps.println("}");
  }
}
