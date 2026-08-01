package com.rosetta.metrics.complexity.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ConditionalExpr;         // <-- IMPORTANT
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.rosetta.engine.AnalysisContext;
import com.rosetta.engine.PerFileExtractor;
import com.rosetta.engine.MetricCategory;
import com.rosetta.engine.MetricInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@MetricCategory({"complexity"})
@MetricInfo(names = {"mccabe_cyclomatic"}, description = "Cyclomatic complexity (McCabe)")
public class JavaMcCabeExtractor implements PerFileExtractor {

    @Override
    public boolean supports(String language, Path file) {
        return "java".equalsIgnoreCase(language);
    }

    @Override
    public Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception {
        Map<String, Double> out = new HashMap<>();
        try (var in = Files.newInputStream(file)) {
            var dec = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            var reader = new BufferedReader(new InputStreamReader(in, dec));
            CompilationUnit cu = StaticJavaParser.parse(reader);

            final int[] decisions = {0};
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override public void visit(IfStmt n, Void a)         { decisions[0]++; super.visit(n, a); }
                @Override public void visit(ForStmt n, Void a)        { decisions[0]++; super.visit(n, a); }
                @Override public void visit(ForEachStmt n, Void a)    { decisions[0]++; super.visit(n, a); }
                @Override public void visit(WhileStmt n, Void a)      { decisions[0]++; super.visit(n, a); }
                @Override public void visit(DoStmt n, Void a)         { decisions[0]++; super.visit(n, a); }
                @Override public void visit(SwitchEntry n, Void a)    {
                    if (!n.getLabels().isEmpty()) decisions[0]++;     // each 'case' adds a path
                    super.visit(n, a);
                }
                @Override public void visit(CatchClause n, Void a)    { decisions[0]++; super.visit(n, a); }
                @Override public void visit(ConditionalExpr n, Void a){ decisions[0]++; super.visit(n, a); } // ternary ?: operator
            }, null);

            // McCabe = #decision points + 1
            out.put("mccabe_cyclomatic", (double) decisions[0] + 1.0);
        } catch (Exception ignore) {
            // swallow parse errors; emit nothing for this file
        }
        return out;
    }
}
