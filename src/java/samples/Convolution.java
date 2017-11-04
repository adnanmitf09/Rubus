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
 Convolution filtering is an important function of image processing to modify the characteristics of an image.
  It takes an image and a matrix of numbers called , which acts like a filter on the input image and produce a 
  second image as output. It works on each pixel by adding the weighted values of pixels around it.
   Different patterns of kernel produce different results under convolution. The kernel can be of different sizes
    but often it is of a 3x3 matrix. Convolution is often used to smooth, sharpen, enhance or blur the image.
 */
import annotation.Transform;

public class Convolution   {

	@Transform(loops = "i")
	public static void applyConvolution(int width, int height, byte[] imageIn, byte[] imageOut, float[] convMatrix3x32) {
		int factor = 3;
		int n = factor * width * height;
		int w = width * factor;
		int success = 1;

		for (int i = 0; i < n; i++) {

			int x = i % (width * factor);
			int y = i / (width * factor);

			int b = checkbound(x, y, width, height);
			if (b == success)
				processPixel(x, y, w, height, imageIn, imageOut, convMatrix3x32);
		}
	}

	public static int checkbound(int x, int y, int width, int height) {
		int factor = 3;
		if (x > factor && x < (width * factor - factor) && y > 1 && y < (height - 1))
			return 1;
		return 0;
	}

	public static void processPixel(int x, int y, int w, int h, byte[] imageIn, byte[] imageOut, float convMatrix3x3[]) {
		float accum = 0f;
		int count = 0;
		int st = -3, ed = 6, st2 = -1, ed2 = 2;
		for (int dx = st; dx < ed; dx += 3) {
			for (int dy = st2; dy < ed2; dy++) {
				byte px = imageIn[((y + dy) * w) + (x + dx)];
				int rgb = 0xff & px;
				float weight = convMatrix3x3[count++];
				accum = accum + (rgb * weight);
			}
		}
		int colorRange = 255;
		byte value = (byte) (Math.max(0, Math.min((int) accum, colorRange)));
		imageOut[y * w + x] = value;

	}

	
}