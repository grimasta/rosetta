
package com.rosetta.metrics.basics.text;

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

@MetricCategory({"basics","loc","text"})
@MetricInfo(names = {"LOC"}, description = "Lines of code for text files")
public class TextLocExtractor implements PerFileExtractor {

    private static boolean looksBinary(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buf = new byte[4096];
            int n = in.read(buf);
            if (n == -1) return false;
            for (int i = 0; i < n; i++) if (buf[i] == 0) return true;
        } catch (Exception ignore) {}
        return false;
    }

    @Override
    public boolean supports(String language, Path file) {
        return true;
    }

    @Override
    public Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception {
        Map<String, Double> out = new HashMap<>();
        if (looksBinary(file)) return out;
        long loc = 0L;
        try (InputStream in = Files.newInputStream(file)) {
            var decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, decoder))) {
                while (br.readLine() != null) loc++;
            }
        } catch (Exception ignore) {}
        out.put("LOC", (double) loc);
        return out;
    }
}
