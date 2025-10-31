package com.rosetta.services;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

class JavaIndexEntry {
    Path file;
    String packageName;                 // e.g. org.example.foo
    Set<String> declaredTypes = new HashSet<>(); // simple names declared in this file
    Set<String> imports = new HashSet<>();       // fully-qualified imports
}
