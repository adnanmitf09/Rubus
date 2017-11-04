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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import annotation.Ignore;
import annotation.Transform;

/**
 * Mandelbrot Benchmark. Adapted from the Computer Language Benchmarks Game
 * (http://shootout.alioth.debian.org/) so that all cells are calculated before
 * outputing the complete image.
 *
 * Note that <code>float</code>s are used since not all GPUs support double
 * precision, and also that exceptions are just thrown out of <code>main</code>
 * to avoid <code>try { ... } catch { ... }</code> blocks.
 *
 */
public class Mandelbrot {
	final static float LIMIT = 4.0f;	
	static short data[];
	static int   width = 18000, height=18000;
	static int   iterations = 250;
	static float spacing = 4.0f;



	@Transform(loops = {"y", "x"})
	public static void compute() {
		data = new short[height*width];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				float Zr = 0.0f;
				float Zi = 0.0f;
				float Cr = (x * spacing - 1.5f);
				float Ci = (y * spacing - 1.0f);

				float ZrN = 0;
				float ZiN = 0;
				int i;

				for(i = 0; (i < iterations) && (ZiN + ZrN <= LIMIT); i++) {
					Zi = 2.0f * Zr * Zi + Ci;
					Zr = ZrN - ZiN + Cr;
					ZiN = Zi * Zi;
					ZrN = Zr * Zr;
				}

				data[y * height + x] = (short)((i * 255) / iterations);
			}
		}
	}

	@Ignore
	public void output(File out) throws IOException {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r = img.getRaster();

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				r.setSample(x, y, 0, data[y * height + x]);
			}
		}

		ImageIO.write(img, "png", out);
	}

}
