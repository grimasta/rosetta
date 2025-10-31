
package com.rosetta.services;

public class LanguageDetector {
    public static String detectFromFilename(String filename) {
        String f = filename.toLowerCase();
        if (f.endsWith(".java")) return "java";
        if (f.endsWith(".py")) return "python";
        if (f.endsWith(".js")) return "javascript";
        if (f.endsWith(".ts")) return "typescript";
        if (f.endsWith(".c")) return "c";
        if (f.endsWith(".cpp") || f.endsWith(".cc") || f.endsWith(".cxx")) return "cpp";
        if (f.endsWith(".cs")) return "csharp";
        if (f.endsWith(".rb")) return "ruby";
        if (f.endsWith(".go")) return "go";
        if (f.endsWith(".kt")) return "kotlin";
        if (f.endsWith(".rs")) return "rust";
        if (f.endsWith(".php")) return "php";
        return "text";
    }
}
