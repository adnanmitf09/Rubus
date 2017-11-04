package codeexport;
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

import java.io.File;
import java.io.FileNotFoundException;

import cl.CLBeautifier;

public class SourceExporter implements SClassFileVisitor{
	private SClassFile classFile;
	private CLBeautifier out=null;
	private File outputFile;
	public SourceExporter() {
	}

	@Override
	public void visit(SClassFile file) {

		classFile = file;
		if(out == null)
		try {
		
			this.outputFile = this.classFile.getDestinationFile();
				out = new CLBeautifier(this.outputFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
		
			return;
		}
		
	if(classFile.getPackageName()!=null){
		out.print("package "+classFile.getPackageName());
		out.println();
	}
	
		out.print(classFile.getStart());
		out.println();
		for (SField field : file.getFields()) {
			field.accept(this);
		}
		
		
		for (SMethod method : file.getMethods()) {
			method.accept(this);
		}
		
		
		out.print(classFile.getEnd());
		out.println();
		
		out.flush();
		out.close();

	}

	@Override
	public void visit(SMethod method) {
		out.print(method.getStart());
		out.println();
		for (SInstruction ins : method.getInstructions()) {
			ins.accept(this);
		}
		
		out.print(method.getEnd());
		out.println();
	}

	@Override
	public void visit(SField field) {
		out.print(field);
		out.println();

	}

	@Override
	public void visit(SInstruction instruction) {
		out.print(instruction);
		out.println();
	}

}
