
package com.rosetta.metrics.architectural.java;

import com.rosetta.engine.AnalysisContext;
import com.rosetta.engine.PerFileExtractor;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Collects per-file Java method call edges and stores them in AnalysisContext bag "java.calls".
 * Returns an empty metric map (relations are not numeric per-file metrics).
 */
public class JavaCallsExtractor implements PerFileExtractor {

    public static final String BAG_KEY = "java.calls";

    static class Edge { public String caller; public String callee; public int count; }

    @Override public boolean supports(String language, Path file) {
        return "java".equalsIgnoreCase(language) && file.toString().endsWith(".java");
    }

    @Override public Map<String, Double> extract(Path file, AnalysisContext ctx) throws Exception {
        Map<String, Map<String, Integer>> perCaller = new HashMap<>();
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            cu.findAll(MethodDeclaration.class).forEach(md -> perCaller.computeIfAbsent(buildSig(cu, md), k -> new HashMap<>()));
            cu.findAll(ConstructorDeclaration.class).forEach(cd -> perCaller.computeIfAbsent(buildSig(cu, cd), k -> new HashMap<>()));
            cu.findAll(MethodCallExpr.class).forEach(mce -> {
                String caller = mce.findAncestor(CallableDeclaration.class).map(cd -> buildSig(cu, cd)).orElse(null);
                if (caller == null) return;
                String name = mce.getNameAsString();
                int arity = mce.getArguments().size();
                String callee = name + "(" + arity + ")"; // light signature; disambiguation can be added later
                perCaller.computeIfAbsent(caller, k -> new HashMap<>()).merge(callee, 1, Integer::sum);
            });
        } catch (Exception ignore) {}

        // Store in bag
        @SuppressWarnings("unchecked")
        List<Edge> bag = (List<Edge>) ctx.getBag().computeIfAbsent(BAG_KEY, k -> new ArrayList<>());
        for (Map.Entry<String, Map<String, Integer>> e : perCaller.entrySet()) {
            for (Map.Entry<String, Integer> ce : e.getValue().entrySet()) {
                Edge ed = new Edge();
                ed.caller = e.getKey(); ed.callee = ce.getKey(); ed.count = ce.getValue();
                bag.add(ed);
            }
        }
        return java.util.Collections.emptyMap();
    }

    private static String buildSig(CompilationUnit cu, CallableDeclaration<?> cd) {
        try {
            String pkg = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
            Optional<String> className = cd.findAncestor(ClassOrInterfaceDeclaration.class).map(c -> c.getNameAsString());
            String owner = className.orElse("");
            String fqnOwner = (pkg.isEmpty() ? "" : pkg + ".") + owner;
            String params = cd.getParameters().stream().map(p -> p.getType().toString()).collect(Collectors.joining(","));
            String name = (cd instanceof ConstructorDeclaration) ? owner : cd.getNameAsString();
            return fqnOwner + "#" + name + "(" + params + ")";
        } catch (Exception ex) { return "unknown#method()"; }
    }
}
