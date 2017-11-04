package samples;
import annotation.Ignore;
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


/**
 * Ignore annotation test
 */
public class IgnoreTest {
	/**
	 * Loop i have potential to be parallelized, however, it will be ignored due to {@Ignore} annotation 
	 */
	@Ignore
	private static void logFill() {
		float[] array = new float[100];
		for (int i = 0; i < array.length; i++) {
			array[i] =(int) Math.log(i);
		}

	}
    
}