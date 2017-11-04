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

package bytecode;

import graph.BasicBlock;
import graph.Block;
import graph.BlockVisitor;
import graph.CodeVisitor;
import graph.Loop;
import graph.TrivialLoop;
import graph.instructions.Call;
import graph.instructions.Instruction;
import graph.instructions.Read;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import analysis.CodeTraverser;

/**
 * 
 */
public class BlockExporter extends BlockVisitor<Void> {
  /**
   * Cache of instruction flattenings for a basic block.
   *
   * TODO: Change this to be more clever (i.e. map from something other than
   *       blocks). Would remove need for 'invalidate' function.
   */
  static private Map<BasicBlock, List<Instruction>> cached = new WeakHashMap<BasicBlock, List<Instruction>>();

  /**
   * Mapping from graph blocks to labels.
   */
  static private Map<Block, Label> labels = new HashMap<Block, Label>();

  /**
   * Blocks that have already been exported.
   */
  private Set<Block> visited = new HashSet<Block>();

  /**
   * ASM library method visitor to which export should occur.
   */
  private MethodVisitor mv;

  /**
   * Default destination (in case of null jumps).
   */
  private Block defaultDestination = null;

  /**
   * Constructs an exporter for the given export destination.
   *
   * @param mv     ASM method visitor to which the blocks should be exported.
   */
  public BlockExporter(MethodVisitor mv) {
    this.mv = mv;
  }

  /**
   * Constructs an exporter for the given export destination, along with a
   * default destination for null jumps. This is useful for loop exports where
   * the end of the loop
   *
   * @param mv     ASM method visitor to which the blocks should be exported.
   */
  public BlockExporter(MethodVisitor mv, Block dflt) {
    this.mv = mv;
    this.defaultDestination = dflt;
  }

  /**
   * Caches a given flattened order of instructions for a basic block.
   *
   * @param bb     Basic block to cache.
   * @param c      Ordered, flattened code to cache.
   */
  public static void cacheCode(BasicBlock bb, List<Instruction> c) {
    cached.put(bb, c);
  }

  /**
   * Invalidate the cache for a given basic block.
   *
   * @param bb     Basic block to invalidate the cache for.
   */
  public static void invalidate(BasicBlock bb) {
    cached.remove(bb);
  }

  /**
   * Returns a label for a block, creating a new one if one hasn't yet been
   * allocated.
   *
   * @param b      Block to get label for.
   * @return       ASM label.
   */
  static public Label getLabel(Block b) {
    if(labels.containsKey(b)) return labels.get(b);

    // Create Label
    Label l = new Label();
    labels.put(b, l);

    return l;
  }

  /**
   * Exports the given basic block to the export destination. Where possible a
   * cached flattening of the block code is used. Otherwise a depth first
   * traversal of the code is used (blindly!).
   *
   * @param bb     Basic block to export.
   * @return       <code>null</code>
   */
  @Override
  public Void visit(BasicBlock bb) {
    // Ensure we haven't already exported this block.
    if(visited.contains(bb)) return null;
    visited.add(bb);

    // Label name
    mv.visitLabel(getLabel(bb));

    // Debug line number (if available)
    if(bb.getLineNumber() != null) {
      mv.visitLineNumber(bb.getLineNumber().intValue(), getLabel(bb));
    }

    if(defaultDestination != null) labels.put(null, getLabel(defaultDestination));

    // Cached copy available.
    if(cached.containsKey(bb)) {
      for(Instruction i : cached.get(bb)) {
        i.accept(new InstructionExporter(mv));
      }
    // Generate code by traversal.
    } else {
      CodeVisitor<Void> cv = new CodeTraverser(new InstructionExporter(mv));
      
      for(Instruction i : bb.getStateful()) {
        // TODO: Is this reasonable?
        if(!(i instanceof Read) || ((i instanceof Call) && (((Call) i).getType().getSort() == null))) {
          i.accept(cv);
        }
      }

      if(bb.getBranch() != null) {
        bb.getBranch().accept(cv);
      }
    }

    labels.remove(null);

    // Generate jump to next label.
    if(bb.getNext() != null) {
      if(visited.contains(bb.getNext())) {
        mv.visitJumpInsn(Opcodes.GOTO, getLabel(bb.getNext()));
      } else {
        bb.getNext().accept(this);
      }
    } else if(defaultDestination != null) {
      mv.visitJumpInsn(Opcodes.GOTO, getLabel(defaultDestination));
    } else {
      // TODO: Assert branch is RETURN, SWITCH, THROW, ...
    }

    // Ensure successors are exported.
    visit(bb.getSuccessors());

    return null;
  }

  /**
   * Exports the given loop to the export destination. This simply exports the
   * loop body, looping its end back to the beginning of the loop.
   *
   * @param l      Loop to export
   * @return       <code>null</code>
   */
  @Override
  public Void visit(Loop l) {
    // Ensure we haven't already exported this block.
    if(visited.contains(l)) return null;
    visited.add(l);

    // Label name
    mv.visitLabel(getLabel(l));

    // Debug line number (if available)
    if(l.getLineNumber() != null) {
      mv.visitLineNumber(l.getLineNumber().intValue(), getLabel(l));
    }

    // Export loop body, looping back to here at the end (default destination).
    BlockExporter be = new BlockExporter(mv, l);
    be.visited.addAll(l.getSuccessors());
    l.getStart().accept(be);

    // Ensure successors are exported.
    visit(l.getSuccessors());

    return null;
  }

  /**
   * Exports the given loop to the export destination. This simply exports the
   * loop body, looping its end back to the beginning of the loop.
   *
   * @param l      Loop to export
   * @return       <code>null</code>
   */
  @Override
  public Void visit(TrivialLoop t) {
    // Ensure we haven't already exported this block.
    if(visited.contains(t)) return null;
    visited.add(t);

    // Label name
    mv.visitLabel(getLabel(t));

    // Debug line number (if available)
    if(t.getLineNumber() != null) {
      mv.visitLineNumber(t.getLineNumber().intValue(), getLabel(t));
    }

    // Condition code
    mv.visitVarInsn(Opcodes.ILOAD, t.getIndex().getIndex());
    t.getLimit().accept(new CodeTraverser(new InstructionExporter(mv)));

    if(t.getIncrements().get(t.getIndex()) > 0) {
      mv.visitJumpInsn(Opcodes.IF_ICMPGE, getLabel(t.getNext()));
    } else {
      mv.visitJumpInsn(Opcodes.IF_ICMPLE, getLabel(t.getNext()));
    }

    // Export loop body, looping back to here at the end (default destination).
    BlockExporter be = new BlockExporter(mv, t);
    be.visited.addAll(t.getSuccessors());
    t.getStart().accept(be);

    // Ensure successors are exported.
    visit(t.getSuccessors());

    return null;
  }
}
