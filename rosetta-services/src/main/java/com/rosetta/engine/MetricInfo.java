package com.rosetta.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes metric names produced by an extractor or post-processor.
 * Example: @MetricInfo(names={"LOC","naive_complexity"}, description="Lines of code and a naive complexity heuristic")
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetricInfo {
    String[] names();
    String description() default "";
}
