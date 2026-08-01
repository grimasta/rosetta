
package com.rosetta.metrics.structural.java;

import com.rosetta.engine.AnalysisContext;
import com.rosetta.engine.MetricSink;
import com.rosetta.engine.PostProcessor;
import com.rosetta.engine.MetricCategory;
import com.rosetta.engine.MetricInfo;
import com.rosetta.metrics.structural.java.JavaIndexCollector.Entry;

import static com.rosetta.metrics.structural.java.JavaIndexCollector.BAG_KEY_INDEX;

import java.util.*;

@MetricCategory({"structural","fan"})
@MetricInfo(names = {"fanout_internal","fanout_external","fanin_internal","fanin_external"}, description = "Fan-in / fan-out metrics per file")
public class JavaFanInOutPostProcessor implements PostProcessor {
    @SuppressWarnings("unchecked")
    @Override
    public void afterAllFiles(AnalysisContext ctx, MetricSink sink) throws Exception {
        Map<String, Entry> index = (Map<String, Entry>) ctx.getBag().get(BAG_KEY_INDEX);
        if (index == null || index.isEmpty()) return;

        Map<String, String> fqnToRel = new HashMap<>();
        for (Entry e : index.values()) {
            for (String t : e.declaredTypes) {
                String fqn = (e.pkg == null || e.pkg.isEmpty()) ? t : (e.pkg + "." + t);
                fqnToRel.put(fqn, e.rel);
            }
        }

        Map<String, Set<String>> outInternal = new HashMap<>();
        Map<String, Integer> outExternal = new HashMap<>();
        index.values().forEach(e -> { outInternal.put(e.rel, new HashSet<>()); outExternal.put(e.rel, 0); });

        for (Entry e : index.values()) {
            for (String imp : e.imports) {
                if (imp.endsWith(".*")) {
                    outExternal.put(e.rel, outExternal.get(e.rel) + 1);
                    continue;
                }
                String targetRel = fqnToRel.get(imp);
                if (targetRel != null) outInternal.get(e.rel).add(targetRel);
                else outExternal.put(e.rel, outExternal.get(e.rel) + 1);
            }
        }

        Map<String, Integer> inInternal = new HashMap<>();
        index.values().forEach(e -> inInternal.put(e.rel, 0));
        for (var kv : outInternal.entrySet()) {
            for (String to : kv.getValue()) inInternal.put(to, inInternal.getOrDefault(to, 0) + 1);
        }

        for (Entry e : index.values()) {
            int foi = outInternal.get(e.rel).size();
            int foe = outExternal.get(e.rel);
            int fii = inInternal.getOrDefault(e.rel, 0);
            int fie = 0;

            sink.emit(e.rel, "fanout_internal", foi);
            sink.emit(e.rel, "fanout_external", foe);
            sink.emit(e.rel, "fanin_internal",  fii);
            sink.emit(e.rel, "fanin_external",  fie);
        }
    }
}
