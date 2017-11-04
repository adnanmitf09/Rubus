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

/**
 * Matrix multiplication using 2D arrays
 */

public class MatrixMultiplication {
/**
 * This method taking input 2 matrixes, however, none of them is being written inside the method,
 * Thus loops i, j will be selected during auto analysis.  
 * @param a input
 * @param b input
 * @return resultant matrix
 */
   public static int[][] multiply(int[][] a, int[][] b) {
       int rowsInA = a.length;
       int columnsInA = a[0].length; // same as rows in B
       int columnsInB = b.length;
       int[][] c = new int[rowsInA][columnsInB];
       for (int i = 0; i < rowsInA; i++) {
           for (int j = 0; j < columnsInB; j++) {
               for (int k = 0; k < columnsInA; k++) {
                   c[i][j] = c[i][j] + a[i][k] * b[k][j];
               }
           }
       }
       return c;
   }
 
}
