package codeexport;

import java.util.ArrayList;

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

public class SMethod implements SBlock {

   private String start;
   public String end;

	public String getStart() {
	return start;
}

public void setStart(String start) {
	this.start = start;
}

public String getEnd() {
	return end;
}

public void setEnd(String end) {
	this.end = end;
}

	@Override
	public void accept(SClassFileVisitor visitor) {
		visitor.visit(this);
	}

	private ArrayList<SInstruction> instructions = new ArrayList<SInstruction>();

	public void addInstruction(SInstruction instruction) {
		instructions.add(instruction);
	}

	public void removeInstruction(SInstruction instruction) {
		instructions.remove(instruction);
	}

	public ArrayList<SInstruction> getInstructions() {
		return instructions;
	}

	public void setInstructions(ArrayList<SInstruction> instructions) {
		this.instructions = instructions;
	}
	
	@Override
		public String toString() {
			String toString = start+"\n";
			for (SInstruction instruction : instructions) {
				toString+=instruction+"\n";
			}
			 toString += end+"\n";
			 return toString;
		}
}
