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
import graph.BlockVisitor;
import graph.Loop;
import graph.TrivialLoop;
import graph.Type;
import graph.instructions.Instruction;
import graph.instructions.Producer;
import graph.instructions.RestoreStack;
import graph.state.Variable;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cl.Config.KernelExportOption;



/**
 *
 */
class CLBlockExporter extends BlockVisitor<Void> {
	/**
	 * Mapping from graph blocks to labels.
	 */
	static private Map<Block, String> labels = new HashMap<Block, String>();

	/**
	 * Blocks that have already been exported.
	 */
	private Set<Block> visited = new HashSet<Block>();

	/**
	 * Print stream for block export
	 */
	private PrintStream ps;

	/**
	 * Default destination (in case of null jumps).
	 */
	private Block defaultDestination = null;

	/**
	 * Constructs an exporter for the given export destination.
	 * 
	 * @param ps
	 *            Printstream to which the method should be exported.
	 */
	CLBlockExporter(PrintStream ps) {
		this.ps = ps;
	}

	/**
	 * Constructs an exporter for the given export destination, along with a
	 * default destination for null jumps. This is useful for loop exports where
	 * the end of the loop
	 * 
	 * @param ps
	 *            Printstream to which the method should be exported.
	 */
	CLBlockExporter(PrintStream ps, Block dflt) {
		this.ps = ps;
		this.defaultDestination = dflt;
	}

	/**
	 * Returns a label for a block, creating a new one if one hasn't yet been
	 * allocated.
	 * 
	 * @param b
	 *            Block to get label for.
	 * @return C label.
	 */
	static public String getLabel(Block b) {
		if (labels.containsKey(b))
			return labels.get(b);

		// Create Label
		  String l = new String("l" + (labels.size() - (labels.containsKey(null) ? 1 : 0)));
		  labels.put(b, l);

		return l;
	}

	private void printBlockStart(String blockStart) {

		switch (Config.exportBlockOption) {
		case EXPORT:
			break;
		case SKIP:
			blockStart = "";
			break;
		case COMMENT:
			blockStart = "//" + blockStart;
			break;

		default:

			break;
		}
		ps.println(blockStart);
	}

	private void printBlockEnd(String blockEnd) {

		switch (Config.exportBlockOption) {
		case EXPORT:
			break;
		case SKIP:
			blockEnd = "";
			break;
		case COMMENT:
			blockEnd = "//" + blockEnd;
			break;

		default:

			break;
		}
		ps.println(blockEnd);
	}

	
	/**
	 * Exports the given basic block to the export destination. Where possible a
	 * cached flattening of the block code is used. Otherwise a depth first
	 * traversal of the code is used (blindly!).
	 * 
	 * @param bb
	 *            Basic block to export.
	 * @return <code>null</code>
	 */
	@Override
	public Void visit(BasicBlock bb) {
		// Ensure we haven't already exported this block.
		if (!shouldExport(bb))
			return null;
		visited.add(bb);

		// Label name

		printBlockStart(getLabel(bb) + ": { // " + bb);

		if (defaultDestination != null)
			labels.put(null, getLabel(defaultDestination));

		// Generate code by traversal.
		CLCppGenerator cg = new CLCppGenerator(this,ps);

		// First stack restorations.
		int index = 0;

		for (Type t : bb.getTypesIn()) {
			(new RestoreStack(index++, t)).accept(cg);
		}

		// Stateful timeline.
		for (Instruction i : bb.getStateful()) {
			i.accept(cg);
		}

		// Stack saving.
		index = 0;

		for (Producer value : bb.getValuesOut()) {
			ps.println("s"
					+ CLHelper.getName(new Variable(index++, value.getType()))
					+ " = " + value.accept(cg) + ";");
		}

		// Branch.
		if (bb.getBranch() != null) {
			bb.getBranch().accept(cg);
		}

		printBlockEnd("}");

		labels.remove(null);

		// Generate jump to next label.

		if (Config.kernelExportOptions == KernelExportOption.GOTO_LABEL) {

			if (bb.getNext() != null) {
				if (visited.contains(bb.getNext())) {
					ps.println("goto " + getLabel(bb.getNext()) + ";");
				} else {
					bb.getNext().accept(this);
				}
			} else if (defaultDestination != null) {
				ps.println("goto " + getLabel(defaultDestination) + ";");
			} else {
				// TODO: Assert branch is RETURN, SWITCH, THROW, ...
			}
		} else if (Config.kernelExportOptions == KernelExportOption.TRANSFORM_TO_CONDITIONS) {

			if (bb.getNext() != null) {
				/*
				 * if (visited.contains(bb.getNext())) { ps.println("goto " +
				 * getLabel(bb.getNext()) + ";"); } else {
				 */
				bb.getNext().accept(this);
				// }
			} else if (defaultDestination != null) {
				defaultDestination.accept(this);
				// ps.println("goto " + getLabel(defaultDestination) + ";");
			} else {
				// TODO: Assert branch is RETURN, SWITCH, THROW, ...
			}
		}

		// Ensure successors are exported.
		visit(bb.getSuccessors());

		return null;
	}

	/**
	 * Exports the given loop to the export destination. This simply exports the
	 * loop body, looping its end back to the beginning of the loop.
	 * 
	 * @param l
	 *            Loop to export
	 * @return <code>null</code>
	 */
	@Override
	public Void visit(Loop l) {
		// Ensure we haven't already exported this block.
		if (!shouldExport(l))
			return null;
		visited.add(l);

		// Label name
		printBlockStart(getLabel(l) + ": // " + l);

		// Export loop body, looping back to here at the end (default
		// destination).
		CLBlockExporter ke = new CLBlockExporter(ps, l);
		ke.visited.addAll(l.getSuccessors());
		l.getStart().accept(ke);

		// Ensure successors are exported.
		visit(l.getSuccessors());

		return null;
	}

	private boolean shouldExport(BasicBlock block){
		//return Config.gotoOptions == GotoOptopn.TRANSFORM_TO_CONDITIONS?true:!visited.contains(block);
	return !visited.contains(block);
	}
	private boolean shouldExport(Loop l) {
	//	return Config.gotoOptions == GotoOptopn.TRANSFORM_TO_CONDITIONS?true:!visited.contains(l);
		return !visited.contains(l);

	}

	/**
	 * Exports the given trivial loop to the export destination. This simply
	 * exports the loop body within a <code>while</code> loop for the required
	 * condition.
	 * 
	 * @param l
	 *            Trivial loop to export
	 * @return <code>null</code>
	 */
	@Override
	public Void visit(TrivialLoop l) {
		// Ensure we haven't already exported this block.
		if (!shouldExport(l))
			return null;
		visited.add(l);

		// Label name
		printBlockStart(getLabel(l) + ": { // " + l);

		// Calculate loop bound.
		String limit = l.getLimit().accept(new CLCppGenerator(this,ps));

		// Produce loop condition.
		if (l.getIncrements().get(l.getIndex()) > 0) {
			ps.println("while(" + CLHelper.getName(l.getIndex()) + " < "
					+ limit + ") {");
		} else {
			ps.println("while(" + CLHelper.getName(l.getIndex()) + " > "
					+ limit + ") {");
		}

		// Export loop body.
		CLBlockExporter ke = new CLBlockExporter(ps);
		l.getStart().accept(ke);

		ps.println("}");
		printBlockEnd("}");

		// Ensure the next block is exported.
		if (Config.kernelExportOptions == KernelExportOption.GOTO_LABEL) {
			if (l.getNext() != null) {
				if (visited.contains(l.getNext())) {
					ps.println("goto " + getLabel(l.getNext()) + ";");
				} else {
					l.getNext().accept(this);
				}
			} else if (defaultDestination != null) {
				ps.println("goto " + getLabel(defaultDestination) + ";");
			}
		} else if (Config.kernelExportOptions == KernelExportOption.TRANSFORM_TO_CONDITIONS) {

			if (l.getNext() != null) {
				/*
				 * if (visited.contains(l.getNext())) { ps.println("goto " +
				 * getLabel(l.getNext()) + ";");
				 * 
				 * } else {
				 */
				l.getNext().accept(this);
				// }
			} else if (defaultDestination != null) {
				defaultDestination.accept(this);
				// ps.println("goto " + getLabel(defaultDestination) + ";");
			}

		}

		return null;
	}
}
