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

import org.apache.log4j.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class ClassMerger implements SClassFileVisitor{
	private CtClass classFile;
	private String outDir;
	public ClassMerger(String mergeTo) {
		try {
			classFile = ClassPool.getDefault().get(mergeTo);
			classFile.stopPruning(true);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public ClassMerger(CtClass mergeTo, String outDir) {
		classFile = mergeTo;
		this.outDir = outDir;
	}

	@Override
	public void visit(SClassFile file) {
		
		
		for (SField field : file.getFields()) {
			field.accept(this);
		}
		
		for (SMethod method : file.getMethods()) {
			method.accept(this);
		}
	
		try {
			classFile.defrost();
			classFile.writeFile(this.outDir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void visit(SMethod method) {
		classFile.defrost();

		CtMethod ctMethod;
		try {
			ctMethod = CtMethod.make(method.toString(), classFile);
			classFile.addMethod(ctMethod);
		} catch (CannotCompileException e) {
			 Logger.getLogger(getClass().getName()).warn(
				        "Unable to compile method " +e.getMessage()
				      );
			 e.printStackTrace();
		}
		
		
	}

	@Override
	public void visit(SField field) {
		try {
			classFile.defrost();

	CtField ctField = CtField.make(field.toString(), classFile);
			classFile.addField(ctField);
		} catch (CannotCompileException e) {
		
				 Logger.getLogger(getClass().getName()).warn(
					        "Unable to compile field " +e.getMessage()
					      );
			e.printStackTrace();
		}
		
	}

	@Override
	public void visit(SInstruction instruction) {
		
	}

}
