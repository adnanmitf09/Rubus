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
 * along with Rubus. If not, see <http://www.gnu.org/licenses/>.
 */

import annotation.Transform;



/**
 * Matrix multiplication using 1D arrays
 */

public class MatrixMultiplicationOneD {
/**
 * Loop k would be selected for parallelism because most inner loop would have most potential 
 * to achieve performance by exploiting parallelism
 * @param inA
 * @param inB
 * @param n
 * @return out
 */
    @Transform(loops={"k"})
	public static float[] multiplyMatrices(float[] inA, float[] inB, int n) {
		float[] out = new float[n * n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				float sum = 0;
				for (int k = 0; k < n; k++)
					sum = sum + inA[i * n + k] * inB[k * n + j];
				out[i * n + j] = sum;

			}
		}
		return out;
	}
}
