
package com.rosetta.metrics.architectural.c_cpp;

import com.rosetta.engine.AnalysisContext;
import com.rosetta.engine.PerFileExtractor;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Naive function call edge collector for C/C++. Stores edges in bag "c_cpp.calls". */
public class CCppCallsExtractor implements PerFileExtractor {

    public static final String BAG_KEY = "c_cpp.calls";
    private static final Pattern CALL = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "if","for","while","switch","return","sizeof","catch","new","delete"
    ));

    @Override public boolean supports(String language, Path file) {
        String fn = file.toString().toLowerCase();
        boolean ext = fn.endsWith(".c") || fn.endsWith(".cc") || fn.endsWith(".cpp") || fn.endsWith(".cxx") || fn.endsWith(".c++");
        return ext && ("c".equalsIgnoreCase(language) || "cpp".equalsIgnoreCase(language) || "c-cpp".equalsIgnoreCase(language));
    }

    private static String rel(java.nio.file.Path base, java.nio.file.Path file) {
        try { return base.relativize(file).toString(); }
        catch (Exception e) { return file.toString(); }
    }
    
    @Override public Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception {
    	String rel = rel(ctx.getBasePath(), file);
        Map<String, Integer> calls = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = CALL.matcher(line);
                while (m.find()) {
                    String name = m.group(1);
                    if (KEYWORDS.contains(name)) continue;
                    calls.merge(name, 1, Integer::sum);
                }
            }
        } catch (Exception ignore) {}

        @SuppressWarnings("unchecked")
        List<Map<String,Object>> bag = (List<Map<String,Object>>) ctx.getBag().computeIfAbsent(BAG_KEY, k -> new ArrayList<>());
        for (Map.Entry<String,Integer> e : calls.entrySet()) {
            Map<String,Object> edge = new HashMap<>();
            edge.put("file", rel);
            edge.put("callee", e.getKey());
            edge.put("count", e.getValue());
            bag.add(edge);
        }
        return java.util.Collections.emptyMap();
    }
}
