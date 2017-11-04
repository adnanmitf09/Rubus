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
import graph.Loop;
import graph.TrivialLoop;
import graph.Type;
import graph.instructions.Call;
import graph.instructions.NewArray;
import graph.instructions.NewMultiArray;
import graph.instructions.NewObject;
import graph.instructions.Producer;
import graph.instructions.Read;
import graph.instructions.Stateful;
import graph.instructions.ValueReturn;
import graph.instructions.Write;
import graph.state.ArrayElement;
import graph.state.Field;
import graph.state.InstanceField;
import graph.state.State;
import graph.state.Variable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import analysis.AliasMap;
import analysis.BlockCollector;
import analysis.CanonicalState;
import analysis.InstructionCollector;
import analysis.LooseState;

/**
 * Collects information on usage of state within a graph, or subset of graph.
 * This includes across calls etc. and performs basic may-alias analysis.
 */
public class AliasUsed extends BlockVisitor<AliasMap> {
  /**
   * Running set of 'raw' writes.
   */
  private Set<CanonicalState> rawWrites = new HashSet<CanonicalState>();

  /**
   * Running set of 'raw' reads.
   */
  private Set<CanonicalState> rawReads = new HashSet<CanonicalState>();

  /**
   * Running set of state to which reads are made (indirect).
   */
  private Set<State> refReads = null;

  /**
   * Running set of state to which writes are made (indirect).
   */
  private Set<State> refWrites = null;

  /**
   * Running set of state that may be returned by the blocks.
   */
  private Set<CanonicalState> returnState = new HashSet<CanonicalState>();

  /**
   * Map of may-alias information.
   */
  private Map<Block, AliasMap> mayAlias = new HashMap<Block, AliasMap>();

  /**
   * Initial may-alias information passed in.
   */
  private AliasMap initial;

  /**
   * Loop predecessors (linking start of body to loop node).
   */
  private Map<Block, Loop> loops = new HashMap<Block, Loop>();

  /**
   * Call alias information.
   */
  private Map<Call, Set<CanonicalState>> callAlias =
                                       new HashMap<Call, Set<CanonicalState>>();

  /**
   * Accuracy flag - cleared if an out-of-scope method is called.
   */
  private boolean accurate = true;

  /**
   * Empty set for NEW instructions.
   */
  private static class New implements State {
    private Producer instruction;

    public New(Producer i) {
      instruction = i;
    }

    @Override
    public boolean equals(Object obj) {
      if(obj instanceof New) {
        return ((New) obj).instruction.equals(instruction);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return instruction.hashCode();
    }

    @Override
    public String toString() {
    	//if(Config.printObjectInToString) ObjectUtil.println(this);
      return instruction.toString();
    }

    @Override
    public Type getType() {
      return instruction.getType();
    }

    @Override
    public State getBase() {
      return this;
    }

    @Override
    public Producer[] getOperands() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Producer> getIndicies() {
      return Collections.emptyList();
    }
  }

  /**
   * Gathers state reference usage information across all blocks given.
   *
   * @param blocks   Blocks to consider..
   * @param interest Map of alias situation at entry to code graph.
   */
  public AliasUsed(Set<Block> blocks, AliasMap interest) {
    initial = interest;

    // Initialise block results.
    for(Block block : blocks) {
      addBlock(block);
    }

    // Calculate Result
    calculate();
  }

  /**
   * Gathers state reference usage information across all blocks reachable from
   * that given.
   *
   * @param graph    Starting block.
   * @param interest Map of alias situation at entry to code graph.
   */
  public AliasUsed(Block graph, AliasMap interest) {
    this(BlockCollector.collect(graph), interest);
  }

  /**
   * Gathers state reference usage information across all blocks given.
   *
   * @param blocks   Blocks to consider..
   * @param interest State to consider of interest.
   */
  public AliasUsed(Set<Block> blocks, Set<? extends State> interest) {
    // Form initial may-alias map from interest.
    initial = new AliasMap();

    for(State s : interest) {
      initial.put(
        new LooseState(s),
        Collections.singleton(new CanonicalState(s))
      );
    }

    // Initialise block results.
    for(Block block : blocks) {
      addBlock(block);
    }

    // Calculate Result
    calculate();
  }

  /**
   * Gathers state reference usage information across all blocks reachable from
   * that given.
   *
   * @param graph    Starting block.
   * @param interest State to consider of interest.
   */
  public AliasUsed(Block graph, Set<? extends State> interest) {
    this(BlockCollector.collect(graph), interest);
  }

  /**
   * Adds the given block to the analysis.
   */
  private void addBlock(Block block) {
    mayAlias.put(block, new AliasMap());

    if(block instanceof Loop) {
      Loop loop = (Loop) block;

      loops.put(loop.getStart(), loop);

      for(Block b : loop.getBody()) {
        addBlock(b);
      }
    }
  }

  /**
   * Causes the analysis to be recomputed.
   */
  public void calculate() {
    boolean changed = true;

    // Invalidate cached
    refReads =  null;
    refWrites = null;

    // Iterate while things change.
    for(int i = 0; (i < 100) && changed; i++) {
      changed = false;

      for(Block block : mayAlias.keySet()) {
        AliasMap map = block.accept(this);

        if(!map.equals(mayAlias.get(block))) {
          mayAlias.put(block, map);
          changed = true;
        }
      }
    }

    if(changed == true) {
      Logger.getLogger("dataflow.Alias").info(
        "May-alias analysis failed to converge."
      );
      
      accurate = false;
    }
  }

  /**
   * Returns whether the analysis can be considered accurate.
   *
   * @return
   */
  public boolean isAccurate() {
    return accurate;
  }

  /**
   * Returns the raw set of state which was read.
   *
   * @return       Set of relevant state.
   */
  public Set<CanonicalState> getReads() {
    return Collections.unmodifiableSet(rawReads);
  }

  /**
   * Returns the raw set of state which was written.
   *
   * @return       Set of relevant state.
   */
  public Set<CanonicalState> getWrites() {
    return Collections.unmodifiableSet(rawWrites);
  }

  /**
   * Returns the raw set of state that could be returned by the code region.
   *
   * @return       Set of relevant state.
   */
  public Set<CanonicalState> getReturns() {
    return Collections.unmodifiableSet(returnState);
  }

  /**
   * Returns the set of base states from which indirect reads were made (i.e.
   * reads from fields of an object or elements of an array).
   *
   * @return       Set of relevant base states (variables or statics).
   */
  public Set<State> getBaseReads() {
    // Calculate first time
    if(refReads == null) {
      refReads = new HashSet<State>();

      for(CanonicalState s : rawReads) {
        refReads.add(s.getInternal().getBase());
      }
    }

    return Collections.unmodifiableSet(refReads);
  }

  /**
   * Returns the set of base states to which indirect writes were made (i.e.
   * writes to fields of an object or elements of an array).
   *
   * @return       Set of relevant base states (variables or statics).
   */
  public Set<State> getBaseWrites() {
    // Calculate first time
    if(refWrites == null) {
      refWrites = new HashSet<State>();

      for(CanonicalState s : rawWrites) {
        refWrites.add(s.getInternal().getBase());
      }
    }

    return Collections.unmodifiableSet(refWrites);
  }

  /**
   * Forms the union of the mappings for all predecessors of the given block.
   *
   * @param  block Block whose predecessors to consider.
   * @return       Resultant mapping from union.
   */
  private AliasMap unionPredecessors(Block block) {
    AliasMap map = null;

    // Loops (link to end of loop).
    if(block instanceof Loop) {
      map = new AliasMap(mayAlias.get(((Loop) block).getEnd()));
    }

    // Union predecessors
    for(Block pred : block.getPredecessors()) {
      if(!mayAlias.containsKey(pred))
        continue;

      if(map == null) {
        map = new AliasMap(mayAlias.get(pred));
      } else {
        for(Map.Entry<LooseState, Set<CanonicalState>> entry :
                                                mayAlias.get(pred).entrySet()) {
          // Union the two may-alias sets.
          if(map.containsKey(entry.getKey())) {
            map.get(entry.getKey()).addAll(entry.getValue());
          // Copy the new may-alias set.
          } else {
            map.put(
              entry.getKey(),
              new HashSet<CanonicalState>(entry.getValue())
            );
          }
        }
      }
    }

    if(map == null) {
      // Loop start, needs to be linked to the outside.
      if(loops.containsKey(block)) {
        map = new AliasMap(mayAlias.get(loops.get(block)));
      // Initial may-alias set.
      } else {
        map = new AliasMap(initial);
      }
    }
    
    return map;
  }

  /**
   * Returns the set of state that the reference returned by the given
   * instruction could possibly point to.
   *
   * @param  mapping     May-alias mapping to use.
   * @param  instruction Instruction.
   * @return             Set of state that may-alias.
   */
  private Set<CanonicalState> getAliasSet(AliasMap mapping, Producer instruction) {
    // Reads
    if(instruction instanceof Read) {
      return getAliasSet(mapping, ((Read) instruction).getState());
    // Calls
    } else if(instruction instanceof Call) {
      if(callAlias.containsKey((Call) instruction)) {
        return callAlias.get((Call) instruction);
      } else {
        return Collections.emptySet();
      }
    // Allocations (i.e. NEW*)
    } else if((instruction instanceof NewObject) || (instruction instanceof
            NewArray) || (instruction instanceof NewMultiArray)) {
      Set<CanonicalState> set = new HashSet<CanonicalState>();

      set.add(new CanonicalState(new New(instruction)));
      return set;
    // Other
    } else {
      return Collections.emptySet();
    }
  }

  /**
   * Returns the set of state that the given state could possibly alias with.
   *
   * @param  mapping May-alias mapping to use.
   * @param  state   State.
   * @return         Set of state that may-alias.
   */
  private Set<CanonicalState> getAliasSet(AliasMap mapping, State state) {
    Set<CanonicalState> set = new HashSet<CanonicalState>();

    // Recurse for array elements.
    if(state instanceof ArrayElement) {
      ArrayElement element = (ArrayElement) state;

      for(CanonicalState s : getAliasSet(mapping, element.getArray())) {
        set.add(new CanonicalState(
          new ArrayElement(
            new Read(s.getInternal()),
            element.getIndex(),
            element.getType()
          )
        ));
      }
    // And also for instance fields
    } else if(state instanceof InstanceField) {
      InstanceField field = (InstanceField) state;

      for(CanonicalState s : getAliasSet(mapping, field.getObject())) {
        set.add(new CanonicalState(
          new InstanceField(new Read(s.getInternal()), field.getField())
        ));
      }
    }

    // Add any aliasing from recursion
    for(CanonicalState s : new HashSet<CanonicalState>(set)) {
      if(mapping.containsKey(s.toLoose())) {
        set.addAll(mapping.get(s.toLoose()));
      }
    }

    // Add trivial aliasing.
    if(mapping.containsKey(new LooseState(state))) {
      set.addAll(mapping.get(new LooseState(state)));
    }
    
    return set;
  }

  /**
   * Returns the may-alias set for a given state after the given block.
   *
   * @param  block Block.
   * @param  state State.
   * @return       May-alias set for the state.
   */
  public Set<CanonicalState> getAliasSet(Block block, State state) {
    return getAliasSet(mayAlias.get(block), state);
  }

  /**
   * Considers the effect of a stateful instruction on the usage information.
   * This also recurses in the case of any method calls.
   *
   * @param stateful  The stateful instruction.
   */
  private void consider(AliasMap result, Stateful stateful) {
    // Recurse on calls
    if(stateful instanceof Call) {
      Call call = (Call) stateful;
      Producer[] arguments = call.getOperands();

      // Calls to out-of-scope method results in inaccurate flag being set if it
      // could affect aliasing.
      if(call.getMethod().getImplementation() == null) {
        if(call.getType().getSort() == Type.Sort.REF) {
          accurate = false;
        }

        for(int i = 0; i < arguments.length; i++) {
          if(arguments[i].getType().getSort() == Type.Sort.REF) {
            accurate = false;
          }
        }
        return;
      }

      // Mapping of interest
      AliasMap recurseMap = new AliasMap();

      for(int i = 0; i < arguments.length; i++) {
        LooseState ls = new LooseState(new Variable(i, arguments[i].getType()));

        recurseMap.put(ls, getAliasSet(result, arguments[i]));
      }

      Logger.getLogger("dataflow.Alias").trace(
        "Call to " + call.getMethod().getName() + " with interest map " + recurseMap + "."
      );

      AliasUsed recurse = new AliasUsed(
        call.getMethod().getImplementation(),
        recurseMap
      );

      Logger.getLogger("dataflow.Alias").trace(
        "Call to " + call.getMethod().getName() + " gave writes " + recurse.getWrites() + "."
      );

      // If indirect writes of reference type, then must flag as inaccurate.
      for(CanonicalState cs : recurse.getWrites()) {
        if(cs.getInternal().getType().getSort() == Type.Sort.REF) {
          accurate = false;
        }
      }

      // Update CALL mapping for return values.
      callAlias.put(call, recurse.getReturns());

      // Add in recursion stuff.
      rawReads.addAll(recurse.getReads());
      rawWrites.addAll(recurse.getWrites());
    // Write / Reads
    } else {
      // State is a Variable or Static
      if((stateful.getState() instanceof Variable)
                                    || (stateful.getState() instanceof Field)) {
        if(stateful instanceof Write) {
          Write write = (Write) stateful;

          result.put(
            new LooseState(write.getState()),
            getAliasSet(result, write.getValue())
          );
        }
      // State is a array element.
      } else if(stateful.getState() instanceof ArrayElement) {
        ArrayElement element = (ArrayElement) stateful.getState();

        // Update alias sets.
        if(stateful instanceof Write) {
          Write write = (Write) stateful;

          if(write.getValue().getType().getSort() == Type.Sort.REF) {
            LooseState ls  = new LooseState(element);
            
            if(result.containsKey(ls)) {
              result.get(ls).addAll(getAliasSet(result, write.getValue()));
            } else {
              result.put(ls, getAliasSet(result, write.getValue()));
            }
          }
        }

        // Use exiting alias knowledge.
        for(CanonicalState s : getAliasSet(result, element.getArray())) {
          LooseState ls = new LooseState(
            new ArrayElement(
              new Read(s.getInternal()),
              element.getIndex(),
              element.getType()
            )
          );
          
          // Writes
          if(stateful instanceof Write) {
            Write write = (Write) stateful;

            // TODO: Unclear if needed.
            if(write.getValue().getType().getSort() == Type.Sort.REF) {
              if(result.containsKey(ls)) {
                result.get(ls).addAll(getAliasSet(result, write.getValue()));
              } else {
                result.put(ls, getAliasSet(result, write.getValue()));
              }
            }
            
            rawWrites.add(ls.toCanonical());
          // Reads
          } else if(stateful instanceof Read) {
            rawReads.add(ls.toCanonical());
          }
        }
      // State is an instance field.
      } else if(stateful.getState() instanceof InstanceField) {
        InstanceField field = (InstanceField) stateful.getState();

        // Update alias sets.
        if(stateful instanceof Write) {
          Write write = (Write) stateful;

          if(write.getValue().getType().getSort() == Type.Sort.REF) {
            LooseState ls  = new LooseState(field);

            if(result.containsKey(ls)) {
              result.get(ls).addAll(getAliasSet(result, write.getValue()));
            } else {
              result.put(ls, getAliasSet(result, write.getValue()));
            }
          }
        }

        // Use exiting alias knowledge.
        for(CanonicalState s : getAliasSet(result, field.getObject())) {
          LooseState ls = new LooseState(
            new InstanceField(new Read(s.getInternal()), field.getField())
          );

          // Writes
          if(stateful instanceof Write) {
            Write write = (Write) stateful;

            // TODO: Unclear if needed.
            if(write.getValue().getType().getSort() == Type.Sort.REF) {
              if(result.containsKey(ls)) {
                result.get(ls).addAll(getAliasSet(result, write.getValue()));
              } else {
                result.put(ls, getAliasSet(result, write.getValue()));
              }
            }

            rawWrites.add(ls.toCanonical());
          // Reads
          } else if(stateful instanceof Read) {
            rawReads.add(ls.toCanonical());
          }
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
  public AliasMap visit(BasicBlock bb) {
    AliasMap result = unionPredecessors(bb);

    // Effect of stateful instructions in the block.
    for(Stateful s : bb.getStateful()) {
      consider(result, s);
    }

    // Effect of value returns.
    if(bb.getBranch() instanceof ValueReturn) {
      ValueReturn ret = (ValueReturn) bb.getBranch();

      if(ret.getType().getSort() == Type.Sort.REF) {
        returnState.addAll(getAliasSet(result, ret.getOperand()));
      }
    }

    return result;
  }

  /**
   * Considers the effect of a loop on the state usage information.
   *
   * @param  loop  Loop.
   * @return       May-alias result.
   */
  @Override
  public AliasMap visit(Loop loop) {
    return unionPredecessors(loop);
  }

  /**
   * Considers the effect of a trivial loop on the state usage information.
   * This differs from a standard loop in that it considers the instructions in
   * the limit calculation.
   *
   * @param  loop  Trivial loop.
   * @return       May-alias result.
   */
  @Override
  public AliasMap visit(TrivialLoop loop) {
    AliasMap result = unionPredecessors(loop);

    // Consider of reads/calls in TrivialLoop limits.
    for(Stateful s : InstructionCollector.collect(loop.getLimit(),
                                                              Stateful.class)) {
      consider(result, s);
    }

    return result;
  }
}
