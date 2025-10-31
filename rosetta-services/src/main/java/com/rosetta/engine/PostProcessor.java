
package com.rosetta.engine;

public interface PostProcessor {
    void afterAllFiles(AnalysisContext ctx, MetricSink sink) throws Exception;
    default String name() { return getClass().getSimpleName(); }
}
