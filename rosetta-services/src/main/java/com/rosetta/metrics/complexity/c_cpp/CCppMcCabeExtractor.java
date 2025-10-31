
package com.rosetta.metrics.complexity.c_cpp;

import com.rosetta.engine.AnalysisContext;
import com.rosetta.engine.PerFileExtractor;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Approximate McCabe by counting decision points. */
public class CCppMcCabeExtractor implements PerFileExtractor {
    @Override public boolean supports(String language, Path file) {
        String fn = file.toString().toLowerCase();
        boolean ext = fn.endsWith(".c") || fn.endsWith(".cc") || fn.endsWith(".cpp") || fn.endsWith(".cxx") || fn.endsWith(".c++") || fn.endsWith(".h") || fn.endsWith(".hh") || fn.endsWith(".hpp") || fn.endsWith(".hxx") || fn.endsWith(".h++");
        return ext && ("c".equalsIgnoreCase(language) || "cpp".equalsIgnoreCase(language) || "c-cpp".equalsIgnoreCase(language));
    }

    @Override public Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception {
        Map<String, Double> out = new HashMap<>();
        long cyclo = 0;
        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                String s = " " + line + " ";
                if (s.contains(" if ")) cyclo++;
                if (s.contains(" for ")) cyclo++;
                if (s.contains(" while ")) cyclo++;
                if (s.contains(" case ")) cyclo++;
                if (s.contains("&&")) cyclo++;
                if (s.contains("||")) cyclo++;
                if (s.contains("?")) cyclo++;
            }
        } catch (Exception ignore) {}
        // McCabe baseline is decisions+1 per function; we emit per-file sum approximation
        out.put("McCabe", (double) (cyclo + 1));
        return out;
    }
}
