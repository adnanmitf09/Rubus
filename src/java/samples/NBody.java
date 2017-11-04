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

import annotation.Ignore;
import annotation.Transform;

/**
 N-body Simulation is an astronomy and physics problem, simulated on a system of particles. 
 The particles are influenced by physical forces. N-body simulations are widely used in astrophysics. 
 Influenced by dark matter, structure formations such as galaxy filaments processes are studied under 
 the umbrella of N-body. Star cluster's dynamic evolution is also studied where there is the involvement of direct N-body. 
 More formally, the N-body simulation computes new positions of N-Body influenced by some physical forces like gravity.
 */

public class NBody{



	@Ignore
	public static void compute(){
		float positionsCPU[];
		float velocitiesCPU[];	
		float delT = .005f;
		float espSqr = 1.0f;
		float mass = 5f;
		int size = 9999999;
		int bodies = size;
		positionsCPU = new float[bodies * 3];
		velocitiesCPU = new float[bodies * 3];


		final float maxDist = 20f;
		for (int body = 0; body < (bodies * 3); body += 3) {

			final float theta = (float) (Math.random() * Math.PI * 2);
			final float phi = (float) (Math.random() * Math.PI * 2);
			final float radius = (float) (Math.random() * maxDist);

			// get the 3D dimensional coordinates
			positionsCPU[body + 0] = (float) (radius * Math.cos(theta) * Math.sin(phi));
			positionsCPU[body + 1] = (float) (radius * Math.sin(theta) * Math.sin(phi));
			positionsCPU[body + 2] = (float) (radius * Math.cos(phi));

			// divide into two 'spheres of bodies' by adjusting x

			if ((body % 2) == 0) {
				positionsCPU[body + 0] += maxDist * 1.5;
			} else {
				positionsCPU[body + 0] -= maxDist * 1.5;
			}
		}


			compute(size, positionsCPU, delT, espSqr, mass, velocitiesCPU);
		}

	/**
	 *Here is where we calculate the position of each body
	 */
	@Transform(loops = { "i" })
	public static void compute(int bodies, float xyz[],float delT,float espSqr, float mass,float vxyz[]) {
		final int count = bodies * 3;
		for (int body = 0; body < bodies; body++){
			final int globalId = body * 3;

			float accx = 0.f;
			float accy = 0.f;
			float accz = 0.f;

			final float myPosx = xyz[globalId + 0];
			final float myPosy = xyz[globalId + 1];
			final float myPosz = xyz[globalId + 2];
			for (int i = 0; i < count; i += 3) {
				final float dx = xyz[i + 0] - myPosx;
				final float dy = xyz[i + 1] - myPosy;
				final float dz = xyz[i + 2] - myPosz;
				final float invDist = 1f/((float)Math.sqrt((dx * dx) + (dy * dy) + (dz * dz) + espSqr));
				final float s = mass * invDist * invDist * invDist;
				accx = accx + (s * dx);
				accy = accy + (s * dy);
				accz = accz + (s * dz);
			}
			accx = accx * delT;
			accy = accy * delT;
			accz = accz * delT;
			xyz[globalId + 0] = myPosx + (vxyz[globalId + 0] * delT) + (accx * .5f * delT);
			xyz[globalId + 1] = myPosy + (vxyz[globalId + 1] * delT) + (accy * .5f * delT);
			xyz[globalId + 2] = myPosz + (vxyz[globalId + 2] * delT) + (accz * .5f * delT);

			vxyz[globalId + 0] = vxyz[globalId + 0] + accx;
			vxyz[globalId + 1] = vxyz[globalId + 1] + accy;
			vxyz[globalId + 2] = vxyz[globalId + 2] + accz;
		}
	}



	



}
