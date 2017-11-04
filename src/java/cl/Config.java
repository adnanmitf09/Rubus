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

public interface Config {
	// boolean isDetailedToStringEnabled = true;

	boolean printObjectDescriptionInToString = false; // be sure that it is false when using compiler to export actual classes, result might be wrong otherwise
	boolean useCLKernelExporter = true;
	boolean printKernel = false;
	boolean useCudaKernelExporter = false;
	boolean deleteGeneratedSourceFilesAfterCompilation = false;
	boolean openSrcFileAfterExport = true;
	int spacesBeforePlusSignInKernel = 60;
	
	// Merge code in a single class after generation (bytecode)
	boolean mergeCode = true;
	
	enum ExportKernelBlockOption{
		COMMENT,
		SKIP,
		EXPORT
		
	}
	
	
	ExportKernelBlockOption exportBlockOption = ExportKernelBlockOption.EXPORT;
/*
	enum GotoOption{
		DONOTHING, // work
		TRANSFORM_TO_CONDITIONS // don't work yet
		
	}
	
	GotoOption gotoOptions = GotoOption.DONOTHING;
	
	*/
	enum KernelExportOption{
		GOTO_LABEL, // don't work
		METHODS, // don't work
		TRANSFORM_TO_CONDITIONS,
		SIMPLE // Work
		,CUDA_TEST
		
		
	}
	
	KernelExportOption kernelExportOptions = KernelExportOption.TRANSFORM_TO_CONDITIONS
			;
	
	String generatedSourceDirectory = "./generated";//"rubus.generated";
	
	boolean clearOutputDirOnNextRun= true;
}
