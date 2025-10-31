package com.rosetta.services;

import java.nio.file.Path;
import java.util.Map;

public interface MetricExtractor {
    /** Return a map of metricKey -> value for the given source file. */
    Map<String, Double> extract(Path file);
    /** Return true if this extractor can handle the given language string. */
    boolean supportsLanguage(String language);
}
