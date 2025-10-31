
package com.rosetta.engine;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AnalysisEngine {
    private final List<PerFileExtractor> perFile;
    private final List<PostProcessor> post;

    public AnalysisEngine(List<PerFileExtractor> perFile, List<PostProcessor> post) {
        this.perFile = perFile;
        this.post = post;
    }

    public void run(AnalysisContext ctx, MetricSink sink) throws Exception {
        for (Path file : ctx.getFiles()) {
            String rel = ctx.getBasePath().relativize(file).toString();
            String lang = com.rosetta.services.LanguageDetector.detectFromFilename(rel);
            for (PerFileExtractor ex : perFile) {
                if (!ex.supports(lang, file)) continue;
                Map<String, Double> out = ex.extract(file, ctx);
                if (out == null || out.isEmpty()) continue;
                for (var e : out.entrySet()) {
                    sink.emit(rel, e.getKey(), e.getValue() == null ? 0.0 : e.getValue());
                }
            }
        }
        for (PostProcessor p : post) p.afterAllFiles(ctx, sink);
    }
}
