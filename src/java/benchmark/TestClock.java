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
 * along with Rubus. If not, see <http://www.gnu.org/licenses/>.
 */

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.Clock;
import com.carrotsearch.junitbenchmarks.IResultsConsumer;
import com.carrotsearch.junitbenchmarks.Result;

public class TestClock
{
    private static Map<String, Result> results = new HashMap<String,Result>();

    private static IResultsConsumer resultsConsumer = new IResultsConsumer()
    {
        public void accept(Result result)
        {
            results.put(result.getTestMethodName(), result);
        }
    };

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(resultsConsumer);


    @Test
    @BenchmarkOptions(clock = Clock.USER_TIME)
    public void testUserTime() throws Exception
    {
        Thread.sleep(100);
    }

    @Test
    @BenchmarkOptions(clock = Clock.CPU_TIME)
    public void testCpuTime() throws Exception
    {
        Thread.sleep(100);
    }


    @Test
    @BenchmarkOptions(clock = Clock.REAL_TIME)
    public void testRealTime() throws Exception
    {
        Thread.sleep(100);
    }


    @AfterClass
    public static void verify()
    {
        final double delta = 0.02;
        assertEquals(3, results.size());
/*
        final double avg1 = results.get("testUserTime").roundAverage.avg;
        final double avg2 = results.get("testCpuTime").roundAverage.avg;
        final double avg3 = results.get("testRealTime").roundAverage.avg;

        assertTrue(avg1 > -delta && avg1 < delta);
        assertTrue(avg2 > -delta && avg2 < delta);
        assertTrue(avg3 > 0.1 - delta && avg3 < 0.1 + delta);
  */  }
}