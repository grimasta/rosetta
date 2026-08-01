
package com.rosetta.metrics.structural.c_cpp;

import com.rosetta.engine.AnalysisContext;
import com.rosetta.engine.PerFileExtractor;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CCppIndexCollector implements PerFileExtractor {
    public static final String BAG_KEY_INDEX = "c_cpp.index";

    public static class Entry {
        public String rel; // relative path
        public Set<String> includes = new HashSet<>();
        public Set<String> declaredTypes = new HashSet<>();
    }

    @Override public boolean supports(String language, Path file) {
        String fn = file.toString().toLowerCase();
        boolean ext = fn.endsWith(".c") || fn.endsWith(".cc") || fn.endsWith(".cpp") || fn.endsWith(".cxx") || fn.endsWith(".c++") || fn.endsWith(".h") || fn.endsWith(".hh") || fn.endsWith(".hpp") || fn.endsWith(".hxx") || fn.endsWith(".h++");
        return ext && ("c".equalsIgnoreCase(language) || "cpp".equalsIgnoreCase(language) || "c-cpp".equalsIgnoreCase(language));
    }
    
    private static String rel(Path base, Path file) {
        try {
            return base.relativize(file).toString();
        } catch (Exception e) {
            return file.toString();
        }
    }
    
    @Override public Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception {
    	String rel = rel(ctx.getBasePath(), file);
        @SuppressWarnings("unchecked")
        Map<String, Entry> map = (Map<String, Entry>) ctx.getBag().computeIfAbsent(BAG_KEY_INDEX, k -> new HashMap<>());
        Entry e = new Entry(); e.rel = rel;
        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.trim();
                if (s.startsWith("#include")) {
                    // naive include capture
                    e.includes.add(s.replace("#include", "").trim());
                } else if (s.startsWith("struct ") || s.startsWith("class ") || s.startsWith("enum ")) {
                    String[] toks = s.split("\\s+");
                    if (toks.length >= 2) e.declaredTypes.add(toks[1].replace("{",""));
                }
            }
        } catch (Exception ignore) {}
        map.put(rel, e);
        return java.util.Collections.emptyMap();
    }
}
