package com.carrotsearch.junitbenchmarks.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;


/**
 * Generate a graphical summary of the historical and current run of a given
 * set of methods. 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface BenchmarkHistoryChart
{

    /**
     * Chart file prefix override. If empty, class name is used.
     * Any substring <code>CLASSNAME</code> is replaced with a fully qualified 
     * class name of the annotated test's class.
     */
    String filePrefix() default "";

    /**
     * Maximum number of historical runs to take into account.
     */
    int maxRuns() default Integer.MAX_VALUE;

    /**
     * Use custom keys for X-axis label. If <code>false</code>, run ID is used.
     */
    LabelType labelWith() default LabelType.RUN_ID;
    /**
     * Time unit to measure running time
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
   /**
    * custom heading for custom column
    */
    String customLabel() default "N";
    
    boolean showClassName() default false;;
}
