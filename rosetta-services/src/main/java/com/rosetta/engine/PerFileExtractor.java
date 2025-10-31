
package com.rosetta.engine;

import java.nio.file.Path;
import java.util.Map;

public interface PerFileExtractor {
    boolean supports(String language, Path file);
    Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception;
    default String name() { return getClass().getSimpleName(); }
}
