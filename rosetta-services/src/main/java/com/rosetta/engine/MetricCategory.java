package com.rosetta.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Category tags for metric extractors/post-processors.
 * Example: @MetricCategory({"complexity"})
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetricCategory {
    String[] value();
}
