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
 * The Black Scholes model, also known as the Black-Scholes-Merton model,
 *  is a model of price variation over time of financial instruments such as stocks that can, 
 *  among other things, be used to determine the price of a European call option.
 *  www.investopedia.com/terms/b/blackscholes.asp
 */



public class BlackScholes{

	/**
	 * Computer will be selected in auto analysis and both loops will be transformed, as compute use
	 *  phi method, it will also be transformed. 
	 */
	
	private static void compute() {
		float[] randArray;
		float[] call_java;
		float[] put_java;

		int size = 4096;
		randArray = new float[size];
		call_java = new float[size];
		put_java = new float[size];


		for (int i = 0; i < size; i++) {
			randArray[i] = i * 1.0f / size;
		}


		float S_LOWER_LIMIT = 10.0f;

		float S_UPPER_LIMIT = 100.0f;

		float K_LOWER_LIMIT = 10.0f;

		float K_UPPER_LIMIT = 100.0f;

		float T_LOWER_LIMIT = 1.0f;

		float T_UPPER_LIMIT = 10.0f;

		float R_LOWER_LIMIT = 0.01f;

		float R_UPPER_LIMIT = 0.05f;

		float SIGMA_LOWER_LIMIT = 0.01f;

		float SIGMA_UPPER_LIMIT = 0.10f;


		for (int gid = 0; gid < size; gid++) {

			float two = 2.0f;
			float inRand = randArray[gid];
			float S = S_LOWER_LIMIT * inRand + S_UPPER_LIMIT * (1.0f - inRand);
			float K = K_LOWER_LIMIT * inRand + K_UPPER_LIMIT * (1.0f - inRand);
			float T = T_LOWER_LIMIT * inRand + T_UPPER_LIMIT * (1.0f - inRand);
			float R = R_LOWER_LIMIT * inRand + R_UPPER_LIMIT * (1.0f - inRand);
			float sigmaVal = SIGMA_LOWER_LIMIT * inRand + SIGMA_UPPER_LIMIT * (1.0f - inRand);

			float sigmaSqrtT = (float) (sigmaVal * Math.sqrt(T));

			float d1 = (float) ((Math.log(S / K) + (R + sigmaVal * sigmaVal / two) * T) / sigmaSqrtT);
			float d2 = d1 - sigmaSqrtT;

			float KexpMinusRT = (float) (K * Math.exp(-R * T));

			float phiD1 = phi(d1);
			float phiD2 = phi(d2);

			call_java[gid] = S * phiD1 - KexpMinusRT * phiD2;

			phiD1 = phi(-d1);
			phiD2 = phi(-d2);

			put_java[gid] = KexpMinusRT * phiD2 - S * phiD1;
		}
	}


	static float  phi(float X) {
		float c1 = 0.319381530f;
		float c2 = -0.356563782f;
		float c3 = 1.781477937f;
		float c4 = -1.821255978f;
		float c5 = 1.330274429f;

		float temp4 = 0.2316419f;

		float oneBySqrt2pi = 0.398942280f;

		float absX = Math.abs(X);
		float t = 1f / (1f + temp4 * absX);

		float y = (float) (1f - oneBySqrt2pi * Math.exp(-X * X / 2f) * t * (c1 + t * (c2 + t * (c3 + t * (c4 + t * c5)))));

		if (X < 0f) return  1f - y;


		return y;
	}


}
