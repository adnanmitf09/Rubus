/*
 * Parallelising JVM Compiler
 * Part II Project, Computer Science Tripos
 *
 * Copyright (c) 2009, 2010 - Peter Calvert, University of Cambridge
 */

package analysis.dataflow;

import graph.BasicBlock;
import graph.Block;
import graph.BlockVisitor;
import graph.ClassNode;
import graph.Loop;
import graph.TrivialLoop;
import graph.Type;
import graph.instructions.Call;
import graph.instructions.Increment;
import graph.instructions.Stateful;
import graph.instructions.Write;
import graph.state.Field;
import graph.state.InstanceField;
import graph.state.State;
import graph.state.Variable;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import analysis.BlockCollector;
import analysis.InstructionCollector;

/**
 * Collects information on usage of state within a graph, or subset of graph.
 * This is only done for direct writes (not reference/indirect) and also
 * collects information on the classes used.
 */
public class SimpleUsed extends BlockVisitor<Void> {
  /**
   * Running set of state which is written (direct).
   */
  private Set<State> writes = new HashSet<State>();

  /**
   * Running boolean of whether any reference writes have occured (indirect).
   */
  private boolean written = false;

  /**
   * Running set of local variables used.
   */
  private Set<Variable> variables = new HashSet<Variable>();

  /**
   * Running list of sets of types for stack saving variables.
   */
  private List<Set<Type>> stackTypes = new LinkedList<Set<Type>>();

  /**
   * Running set of static variables used.
   */
  private Set<Field> statics = new HashSet<Field>();

  /**
   * Running set of classes used.
   */
  private Set<ClassNode> classes = new HashSet<ClassNode>();

  /**
   * Gathers state usage information across all blocks reachable from that
   * given.
   *
   * @param graph  Starting block.
   */
  public SimpleUsed(Block graph) {
    for(Block block : BlockCollector.collect(graph)) {
      block.accept(this);
    }
  }

  /**
   * Gathers state usage information across all blocks in the given set.
   *
   * @param blocks Blocks.
   */
  public SimpleUsed(Set<Block> blocks) {
    for(Block block : blocks) {
      block.accept(this);
    }
  }

  /**
   * Returns the set of state which was directly written.
   *
   * @return       Set of relevant state.
   */
  public Set<State> getDirectWrites() {
    return Collections.unmodifiableSet(writes);
  }

  /**
   * Returns whether any state has been directly written to within given
   * region.
   *
   * @return       <code>true</code> if state has been written,
   *               <code>false</code> otherwise.
   */
  public boolean containsDirectWrites() {
    return !writes.isEmpty();
  }

  /**
   * Returns whether any state has been indirectly written to within given
   * region.
   *
   * @return       <code>true</code> if state has been written,
   *               <code>false</code> otherwise.
   */
  public boolean containsReferenceWrites() {
    return written;
  }

  /**
   * Returns the set of local variables used.
   *
   * @return       Set of local variables used.
   */
  public Set<Variable> getVariables() {
    return Collections.unmodifiableSet(variables);
  }

  /**
   * Returns the set of types for the given stack variable.
   */
  public Set<Type> getStackTypes(int index) {
    return Collections.unmodifiableSet(stackTypes.get(index));
  }

  /**
   * Returns the number of stack indicies used.
   */
  public int getStackCount() {
    return stackTypes.size();
  }

  /**
   * Returns the set of statics used.
   *
   * @return       Set of statics used.
   */
  public Set<Field> getStatics() {
    return Collections.unmodifiableSet(statics);
  }

  /**
   * Returns the set of classes used.
   *
   * @return       Set of classes used.
   */
  public Set<ClassNode> getClasses() {
    return Collections.unmodifiableSet(classes);
  }

  /**
   * Considers the effect of a stateful instruction on the usage information.
   * This also recurses in the case of any method calls.
   *
   * @param stateful  The stateful instruction.
   */
  private void consider(Stateful stateful) {
    // Recurse on calls
    if(stateful instanceof Call) {
      Call call = (Call) stateful;

      // For out-of-scope methods, we can't know usage information, but can
      // assume that reference writes were made.
      if(call.getMethod().getImplementation() == null) {
        written = true;
        return;
      }

      SimpleUsed recurse = new SimpleUsed(call.getMethod().getImplementation());

      // Merge recursive information (trivial).
      statics.addAll(recurse.getStatics());
      classes.addAll(recurse.getClasses());
      written = written | recurse.containsReferenceWrites();
    // Access
    } else {
      // Class Use
      if(stateful.getState() instanceof InstanceField) {
        classes.add(((InstanceField) stateful.getState()).getField().getOwner());
      }

      // Variable Use
      if(stateful.getState().getBase() instanceof Variable) {
        variables.add((Variable) stateful.getState().getBase());
      // Static Use
      } else if(stateful.getState().getBase() instanceof Field) {
        statics.add((Field) stateful.getState().getBase());
      }

      // Write (or Increment)
      if((stateful instanceof Write) || (stateful instanceof Increment)) {
        // Direct (s will be Variable or Field).
        if(stateful.getState().getBase() == stateful.getState()) {
          writes.add(stateful.getState().getBase());
        // Indirect (s will be ArrayElement or InstanceField).
        } else {
          written = true;
        }
      }
    }
  }

  /**
   * Considers the effect of a basic block on the usage information.
   *
   * @param  bb    Basic block.
   * @return       <code>null</code>.
   */
  @Override
  public Void visit(BasicBlock bb) {
    // Effect of stateful instructions in the block.
    for(Stateful s : bb.getStateful()) {
      consider(s);
    }

    // Update stack types.
    for(int i = bb.getTypesIn().size() - stackTypes.size(); i > 0; i--) {
      stackTypes.add(new HashSet<Type>());
    }

    for(int i = 0; i < bb.getTypesIn().size(); i++) {
      stackTypes.get(i).add(bb.getTypesIn().get(i));
    }

    return null;
  }

  /**
   * Considers the effect of a loop on the state usage information by recursing
   * on the body of the loop.
   *
   * @param  loop  Loop.
   * @return       <code>null</code>.
   */
  @Override
  public Void visit(Loop loop) {
    SimpleUsed recurse = new SimpleUsed(loop.getStart());

    written = written | recurse.containsReferenceWrites();
    writes.addAll(recurse.getDirectWrites());
    variables.addAll(recurse.getVariables());
    statics.addAll(recurse.getStatics());
    classes.addAll(recurse.getClasses());

    // Add in recursive stack types.
    for(int i = recurse.stackTypes.size() - stackTypes.size(); i > 0; i--) {
      stackTypes.add(new HashSet<Type>());
    }

    for(int i = 0; i < recurse.stackTypes.size(); i++) {
      stackTypes.get(i).addAll(recurse.stackTypes.get(i));
    }
    

    return null;
  }

  /**
   * Considers the effect of a trivial loop on the state usage information by
   * recursing on the body of the loop. This differs from a standard loop in
   * that it considers the instructions in the limit calculation.
   *
   * @param  loop  Trivial loop.
   * @return       <code>null</code>.
   */
  @Override
  public Void visit(TrivialLoop loop) {
    // Treat as standard loop.
    visit((Loop) loop);

    // Consider of reads/calls in TrivialLoop limits.
    for(Stateful s : InstructionCollector.collect(loop.getLimit(), Stateful.class)) {
      consider(s);
    }

    return null;
  }
}
