
package com.rosetta.metrics.structural.java;

import com.rosetta.engine.AnalysisContext;
import com.rosetta.engine.PerFileExtractor;
import com.rosetta.engine.MetricCategory;
import com.rosetta.engine.MetricInfo;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@MetricCategory({"structural","index"})
@MetricInfo(names = {}, description = "Index collector (stores structural index in analysis context)")
public class JavaIndexCollector implements PerFileExtractor {
    public static final String BAG_KEY_INDEX = "java.index";

    public static class Entry {
        public String rel;
        public String pkg = "";
        public Set<String> imports = new HashSet<>();
        public Set<String> declaredTypes = new HashSet<>();
    }

    @Override
    public boolean supports(String language, Path file) {
        return "java".equalsIgnoreCase(language);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception {
        var map = (Map<String, Entry>) ctx.getBag().computeIfAbsent(BAG_KEY_INDEX, k -> new HashMap<String, Entry>());
        String rel = ctx.getBasePath().relativize(file).toString();
        Entry e = new Entry();
        e.rel = rel;

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.trim();
                if (s.startsWith("package ")) {
                    e.pkg = s.substring(8).replace(";", "").trim();
                } else if (s.startsWith("import ")) {
                    String imp = s.substring(7).replace(";", "").trim();
                    if (imp.startsWith("static ")) imp = imp.substring(7).trim();
                    e.imports.add(imp);
                } else if (s.startsWith("public class ") || s.startsWith("class ") ||
                           s.startsWith("public interface ") || s.startsWith("interface ") ||
                           s.startsWith("public enum ") || s.startsWith("enum ")) {
                    String[] toks = s.split("\s+");
                    for (int i = 0; i < toks.length - 1; i++) {
                        if (toks[i].equals("class") || toks[i].equals("interface") || toks[i].equals("enum")) {
                            e.declaredTypes.add(toks[i+1].replace("{",""));
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignore) {}
        map.put(rel, e);
        return java.util.Collections.emptyMap();
    }
}
