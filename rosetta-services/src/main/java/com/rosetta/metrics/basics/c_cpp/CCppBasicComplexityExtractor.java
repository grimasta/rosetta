
package com.rosetta.metrics.basics.c_cpp;

import com.rosetta.engine.AnalysisContext;
import com.rosetta.engine.PerFileExtractor;

import java.io.BufferedReader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CCppBasicComplexityExtractor implements PerFileExtractor {
    @Override public boolean supports(String language, Path file) {
        String fn = file.toString().toLowerCase();
        boolean ext = fn.endsWith(".c") || fn.endsWith(".cc") || fn.endsWith(".cpp") || fn.endsWith(".cxx") || fn.endsWith(".c++") || fn.endsWith(".h") || fn.endsWith(".hh") || fn.endsWith(".hpp") || fn.endsWith(".hxx") || fn.endsWith(".h++");
        return ext && ("c".equalsIgnoreCase(language) || "cpp".equalsIgnoreCase(language) || "c-cpp".equalsIgnoreCase(language));
    }

    @Override
    public Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception {
        Map<String, Double> out = new HashMap<>();
        long loc = 0, complexity = 0;
        try (var in = Files.newInputStream(file)) {
            var dec = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
            try (var br = new BufferedReader(new java.io.InputStreamReader(in, dec))) {
                String line;
                while ((line = br.readLine()) != null) {
                    loc++;
                    String s = " " + line + " ";
                    if (s.contains(" if ")) complexity++;
                    if (s.contains(" for ")) complexity++;
                    if (s.contains(" while ")) complexity++;
                    if (s.contains(" case ")) complexity++;
                    if (s.contains(" else if ")) complexity++; // common pattern
                }
            }
        } catch (Exception ignore) {}
        out.put("LOC", (double) loc);
        out.put("naive_complexity", (double) complexity);
        return out;
    }
}
