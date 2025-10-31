
package com.rosetta.engine;

public interface MetricSink {
    void emit(String fileRelPath, String key, double value);
}
