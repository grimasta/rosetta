
package com.rosetta.metrics.basics.java;

import com.rosetta.engine.AnalysisContext;
import com.rosetta.engine.PerFileExtractor;
import com.rosetta.engine.MetricCategory;
import com.rosetta.engine.MetricInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@MetricCategory({"basics","complexity"})
@MetricInfo(names = {"LOC","naive_complexity"}, description = "Line count and a naive complexity heuristic")
public class JavaBasicComplexityExtractor implements PerFileExtractor {
    @Override
    public boolean supports(String language, Path file) {
        return "java".equalsIgnoreCase(language);
    }

    @Override
    public Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception {
        Map<String, Double> out = new HashMap<>();
        long loc = 0;
        long complexity = 0;
        try (InputStream in = Files.newInputStream(file)) {
            var decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, decoder))) {
                String line;
                while ((line = br.readLine()) != null) {
                    loc++;
                    String s = " " + line + " ";
                    if (s.contains(" if ")) complexity++;
                    if (s.contains(" for ")) complexity++;
                    if (s.contains(" while ")) complexity++;
                    if (s.contains(" case ")) complexity++;
                }
            }
        } catch (Exception ignore) {}
        out.put("LOC", (double) loc);
        out.put("naive_complexity", (double) complexity);
        return out;
    }
}
