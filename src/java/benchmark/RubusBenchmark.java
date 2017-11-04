package benchmark;
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

import java.io.File;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkOptionsSystemProperties;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import com.carrotsearch.junitbenchmarks.h2.H2Consumer;
@AxisRange(min = 0)
@BenchmarkOptions(warmupRounds=1,benchmarkRounds=1)
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY)
public abstract class RubusBenchmark{
/*
 This exception mean your GPU function is not working
 
 Exception in thread "Thread-1" java.lang.RuntimeException: Failed to close DB consumer.
	at com.carrotsearch.junitbenchmarks.db.DbConsumer.close(DbConsumer.java:174)
	at com.carrotsearch.junitbenchmarks.AutocloseConsumer$1.run(AutocloseConsumer.java:61)
Caused by: java.lang.IndexOutOfBoundsException: Index: 5, Size: 5
	at java.util.ArrayList.rangeCheck(Unknown Source)
	at java.util.ArrayList.get(Unknown Source)
	at com.carrotsearch.junitbenchmarks.db.HistoryChartGenerator.getData(HistoryChartGenerator.java:301)
	at com.carrotsearch.junitbenchmarks.db.HistoryChartGenerator.generate(HistoryChartGenerator.java:132)
	at com.carrotsearch.junitbenchmarks.db.HistoryChartVisitor.generate(HistoryChartVisitor.java:64)
	at com.carrotsearch.junitbenchmarks.db.DbConsumer.doClose(DbConsumer.java:327)
	at com.carrotsearch.junitbenchmarks.db.DbConsumer.close(DbConsumer.java:167)
	... 1 more
 * 
 */
	public static boolean compareLogEnable = false;
	@Test
	abstract
	public void Java();
	@Test
	abstract
	public void AparAPI();
	@Test
	abstract
	public void Rubus();

	@Rule
	public BenchmarkRule benchmarkRun = new BenchmarkRule(h2consumer);

	public static int[] inputSizes = new int[]{};
	public static File dbFile ;
	public static  File dbFileFull ;
	public static int size;
	public static H2Consumer h2consumer;
	public static void setupDBFile(String fileName){
		dbFile = new File(fileName);
		dbFileFull =  new File(dbFile.getName() + ".h2.db");

	}

	@BeforeClass
	public static void setupConsumer(){
		System.setProperty(BenchmarkOptionsSystemProperties.CUSTOMKEY_PROPERTY,""+size);
		//if(h2consumer!=null)
		h2consumer = new H2Consumer(dbFile);

	}

	public static void deleteDBFileIfExist(){

		if(dbFile!=null && dbFile.exists()){
			dbFile.delete();
		}
	}

	public static void setInput(int [] input){
		inputSizes = input;
	}


	public static void startBenchmark(Class<?> class1) {
		System.out.println("/************* Benchmark Started *************/");
		for (int i = 0; i < inputSizes.length; i++) {

			JUnitCore junit = new JUnitCore();
			size = inputSizes[i];
			System.out.println("Running for input size: "+size);
			junit.run(class1);
			System.out.println("Completed for input size : "+size);

		}	
		System.out.println("/*************  Benchmark End   **************/");


	}


	public static void printArray(float ary[], String name,int size) {
		String line;
		line = name + ": ";
		for (int i = 0; i < size; i++) {
			if (i > 0)
				line += ", ";
			line += ary[i];
		}
		System.out.println(line);
	}
	public static void printArray(long ary[], String name,int size) {
		String line;
		line = name + ": ";
		for (int i = 0; i < size; i++) {
			if (i > 0)
				line += ", ";
			line += ary[i];
		}
		System.out.println(line);
	}
	public static void printArray(short ary[], String name,int size) {
		String line;
		line = name + ": ";
		for (int i = 0; i < size; i++) {
			if (i > 0)
				line += ", ";
			line += ary[i];
		}
		System.out.println(line);
	}
	
	public static void printArray(int ary[], String name,int size) {
		String line;
		line = name + ": ";
		for (int i = 0; i < size; i++) {
			if (i > 0)
				line += ", ";
			line += ary[i];
		}
		System.out.println(line);
	}

	public static void showArray(int ary[], String name, int count) {
		String line;
		line = name + ": ";
		for (int i = 0; i < count; i++) {
			if (i > 0)
				line += ", ";
			line += ary[i];
		}
		System.out.println(line);
	}
	
	public static void sequentialFill(float arr[]){
		if(arr==null){ System.out.println("sequentialFill: Array is null "); return;}
		
		for (int i = 0; i < arr.length; i++) {
			arr[i]=i;
		}
		
	}
	
	public static void sequentialFill(int arr[]){
		if(arr==null){ System.out.println("sequentialFill: Array is null "); return;}
		
		for (int i = 0; i < arr.length; i++) {
			arr[i]=i;
		}
		
	}
	
	public static void randomFill(float arr[]){
		if(arr==null){ System.out.println("sequentialFill: Array is null "); return;}
		
		int n = arr.length;
		for (int i = 0; i <n ; i++) {
			arr[i]= randFloat(i,n);
		}
		
	}
	
	public static void randomFill(int arr[]){
		if(arr==null){ System.out.println("sequentialFill: Array is null "); return;}
		
		int n = arr.length;
		for (int i = 0; i <n ; i++) {
			arr[i]= randInt(i,n);
		}
		
	}
	
	public static float randFloat(float minX, float maxX) {

	 
	    Random rand = new Random();

	 
	    float finalX = rand.nextFloat() * (maxX - minX) + minX;

	    return finalX;
	}
	
	public static int randInt(int minX, int maxX) {

		 
	    Random rand = new Random();

	 
	    int finalX = rand.nextInt((maxX - minX) + 1) + minX;

	    return finalX;
	}
	
	public static boolean compare(float inA[], float inB[], int margen){
		if(inA==null){ System.out.println("compare: inA is null "); return false;}

		if(inB==null){ System.out.println("compare: inB is null "); return false;}
		if(inB.length!=inB.length) return false;
		
		for (int i = 0; i < inA.length; i++) {

			if(inA[i]+margen<inB[i] || inA[i]-margen>inB[i]){return false;}
			
		}
		
		return true;
		
	}

	public static void runBenchmark(Class<?> class1,int input[]) {
		setupDBFile(class1.getName());
		deleteDBFileIfExist();
		setInput(input); //512,1024,2048,4096,8192,16384,32768,65536
	    startBenchmark(class1);
		
	}
}
