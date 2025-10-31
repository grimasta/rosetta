
package com.rosetta.engine;

import com.rosetta.domain.Commit;
import com.rosetta.domain.Repository;
import jakarta.persistence.EntityManager;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AnalysisContext {
    private final Path basePath;
    private final java.util.List<Path> files;
    private final EntityManager em;
    private final Repository repo;
    private final Commit commit;
    private final Map<String,Object> bag = new ConcurrentHashMap<>();

    public AnalysisContext(Path basePath, java.util.List<Path> files, EntityManager em, Repository repo, Commit commit) {
        this.basePath = basePath;
        this.files = files;
        this.em = em;
        this.repo = repo;
        this.commit = commit;
    }

    public Path getBasePath() { return basePath; }
    public java.util.List<Path> getFiles() { return files; }
    public EntityManager getEm() { return em; }
    public Repository getRepo() { return repo; }
    public Commit getCommit() { return commit; }
    public Map<String,Object> getBag() { return bag; }
}
