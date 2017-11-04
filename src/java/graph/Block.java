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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.ObjectUtil;
import util.WeakList;
import cl.Config;

/**
 * Abstract class representing control flow blocks with a method.
 */
public abstract class Block {
	/**
	 * List of preceeding blocks in the control graph.
	 */
	protected List<Block> predecessors = new WeakList<Block>();

	/**
	 * Block that immediately follows in the control flow graph.
	 */
	private Block next;

	/**
	 * First line number that is associated with the code section (for
	 * debugging).
	 */
	private Integer lineNumber;

	/**
	 * Static counter for allocating identifiers to blocks. Used purely for
	 * debugging as a friendlier number than hash codes.
	 */
	static private int gID = 0;

	/**
	 * Identifier allocated to the block.
	 */
	private int id = gID++;

	/**
	 * Returns the unique ID for the block.
	 */
	public int getID() {
		return id;
	}

	/**
	 * Set control flow successor.
	 */
	public void setNext(Block block) {
		// Update predecessor sets for the old and new successor.
		if (next != null) {
			next.predecessors.remove(this);
		}

		// Update internal reference.
		next = block;

		if (next != null) {
			block.predecessors.add(this);
		}
	}

	/**
	 * Get control flow successor.
	 */
	public Block getNext() {
		return next;
	}

	/**
	 * Get predecessors.
	 */
	public Set<Block> getPredecessors() {
		return Collections.unmodifiableSet(new HashSet<Block>(predecessors));
	}

	/**
	 * Get successors.
	 */
	public Set<Block> getSuccessors() {
		if (next != null) {
			return Collections.singleton(next);
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Replaces any occurence of <code>a</code> among the block's successors
	 * with <code>b</code>. For the abstract Block, this just checks the
	 * <code>next</code> reference.
	 * 
	 * @param a
	 *            Block to be replaced.
	 * @param b
	 *            Replacement block.
	 */
	public void replace(Block a, Block b) {
		if (next == a) {
			setNext(b);
		}
	}

	/**
	 * Replaces this block with the given block (altering all predecessors and
	 * successors).
	 */
	public void replace(Block n) {
		for (Block p : getPredecessors()) {
			p.replace(this, n);
		}

		n.setNext(getNext());
	}

	/**
	 * Set line number for debugging.
	 */
	public void setLineNumber(int line) {
		lineNumber = new Integer(line);
	}

	/**
	 * Set line number for debugging.
	 */
	public void setLineNumber(Integer line) {
		lineNumber = line;
	}

	/**
	 * Get line number.
	 */
	public Integer getLineNumber() {
		return lineNumber;
	}

	/**
	 * Acceptor for the BlockVisitor pattern. This must be implemented in each
	 * implementing class so that the most specific method within the visitor is
	 * used.
	 */
	public abstract <T> T accept(BlockVisitor<T> visitor);

	// by adi, for experiment - block export if already export
	/**
	 * should force block if it is already exported
	 */
	public boolean forceExport;

	public boolean isForceExport() {
		return forceExport;
	}

	public void setForceExport(boolean forceExport) {
		this.forceExport = forceExport;
	}
	public boolean forceSkip;
	

	public boolean isForceSkip() {
		return forceSkip;
	}

	public void setForceSkip(boolean forceSkip) {
		this.forceSkip = forceSkip;
	}

	/**
	 * String representation.
	 */
	@Override
	public String toString() {
		if (Config.printObjectDescriptionInToString)
			ObjectUtil.println(this);
		if (lineNumber != null) {
			return getClass().getSimpleName() + " #" + id + " (line "
					+ lineNumber + ")";
		} else {
			return getClass().getSimpleName() + " #" + id;
		}
	}
}
