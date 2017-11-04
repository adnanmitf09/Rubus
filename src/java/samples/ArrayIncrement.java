package samples;
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
 * along with Rubus.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Arrays;

import annotation.Transform;
/**
 * This simple program increment each index of an array. 
 *
 */
public class ArrayIncrement {
/**
 * This method will be accepted in both auto and manual analysis
 */
	@Transform(loops = { "i" })
	public static void doIncrement() {
		int array[] = new int[1000];
		Arrays.fill(array, 45);
		for (int i = 0; i < array.length; i++) {
			int a = array[i];
			a++;
			array[i] = a;
		}
	}
	
/**
 * This method will not be accepted in auto analysis, because the array is passed as argument. Thus Rubus would not accept this because 
 * array might be global and being modified in some other method 	
 */
	@Transform(loops = { "i" })
	public static void doIncrement(int array[]) {
		
		for (int i = 0; i < array.length; i++) {
			int a = array[i];
			a++;
			array[i] = a;
		}
	}
}
