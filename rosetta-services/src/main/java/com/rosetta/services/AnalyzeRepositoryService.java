
package com.rosetta.services;

import com.rosetta.domain.*;
import com.rosetta.storage.HibernateUtil;
import jakarta.persistence.EntityManager;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.rosetta.engine.*;
import com.rosetta.metrics.basics.java.JavaBasicComplexityExtractor;
import com.rosetta.metrics.basics.text.TextLocExtractor;
import com.rosetta.metrics.complexity.java.JavaMcCabeExtractor;
import com.rosetta.metrics.structural.java.JavaFanInOutPostProcessor;
import com.rosetta.metrics.structural.java.JavaIndexCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyzeRepositoryService {

    public void analyze(String projectName, String repoUrl, String authUser, String authToken) throws Exception {
        File workDir = new File("work");
        if (!workDir.exists()) workDir.mkdirs();

        String envUser = System.getenv("GITHUB_USER");
        String envToken = System.getenv("GITHUB_TOKEN");
        CredentialsProvider creds = null;
        if (authUser != null && !authUser.isBlank() && authToken != null && !authToken.isBlank()) {
            creds = new UsernamePasswordCredentialsProvider(authUser, authToken);
        } else if (envUser != null && !envUser.isBlank() && envToken != null && !envToken.isBlank()) {
            creds = new UsernamePasswordCredentialsProvider(envUser, envToken);
        }

        String repoName = repoUrl.substring(repoUrl.lastIndexOf('/') + 1).replace(".git", "");
        File localPath = new File(workDir, repoName);

        Git git;
        if (localPath.exists() && new File(localPath, ".git").exists()) {
            git = Git.open(localPath);
            if (creds != null) git.pull().setCredentialsProvider(creds).call();
            else git.pull().call();
        } else {
            CloneCommand clone = Git.cloneRepository().setURI(repoUrl).setDirectory(localPath);
            if (creds != null) clone.setCredentialsProvider(creds);
            git = clone.call();
        }

        EntityManager em = HibernateUtil.emf().createEntityManager();
        try {
            em.getTransaction().begin();

            Project project = em.createQuery("select p from Project p where p.name = :n", Project.class)
                    .setParameter("n", projectName)
                    .getResultStream().findFirst().orElse(null);
            if (project == null) {
                project = new Project();
                project.setName(projectName);
                em.persist(project);
            }

            Repository repo = em.createQuery("select r from Repository r where r.url = :u", Repository.class)
                    .setParameter("u", repoUrl)
                    .getResultStream().findFirst().orElse(null);
            if (repo == null) {
                repo = new Repository();
                repo.setUrl(repoUrl);
                repo.setDefaultBranch(git.getRepository().getBranch());
                repo.setProject(project);
                em.persist(repo);
            }

            Commit scanCommit = new Commit();
            scanCommit.setCommitId("INITIAL_SCAN");
            scanCommit.setCommitDate(Instant.now());
            scanCommit.setAuthor("Rosetta");
            scanCommit.setMessage("Initial repository scan");
            em.persist(scanCommit);

            final Path basePath = localPath.toPath();
            final java.util.List<Path> files;
            try (var stream = Files.walk(basePath)) {
//            	TODO: create documentation on how to exclude files (e.g. via .rosettaignore or system props)
                files = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.toString().contains(File.separator + ".git" + File.separator))
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                            name.endsWith(".gif") || name.endsWith(".class") || name.endsWith(".jar") ||
                            name.endsWith(".zip") || name.endsWith(".exe") || name.endsWith(".dll") ||
                            name.endsWith(".so")  || name.endsWith(".pdf")) return false;
                        try { return Files.size(p) <= 2_000_000; } catch (IOException e) { return false; }
                    })
                    .collect(Collectors.toList());
            }

            AnalysisContext currentAnalysisContext = new AnalysisContext(basePath, files, em, repo, scanCommit);
            MetricSink currentMetricSink = new DefaultMetricSink(em, repo, scanCommit);
//        	TODO: explain what happens in the next two lines - how the engine discovers and loads metric extractors and post-processors, and how to include/exclude them via tags and system properties
            var perFile = new java.util.ArrayList<com.rosetta.engine.PerFileExtractor>();
            java.util.ServiceLoader.load(com.rosetta.engine.PerFileExtractor.class)
                    .forEach(perFile::add);

            var post = new java.util.ArrayList<com.rosetta.engine.PostProcessor>();
         // Accept comma-separated include/exclude via system props or CLI options
            var include = System.getProperty("metrics.include"); // e.g. "complexity,structural"
            var exclude = System.getProperty("metrics.exclude"); // e.g. "basics"

            java.util.function.Predicate<Object> byTag = o -> {
                // Prefer annotation-based tagging (MetricCategory) when present. Fall back to package-name matching.
                var cls = o.getClass();
                var cat = cls.getAnnotation(com.rosetta.engine.MetricCategory.class);
                if (include != null && !include.isBlank()) {
                    boolean ok = false;
                    for (var tag : include.split(",")) {
                        String t = tag.trim();
                        if (t.isEmpty()) continue;
                        if (cat != null) {
                            for (var cval : cat.value()) if (cval.equalsIgnoreCase(t)) { ok = true; break; }
                            if (ok) break;
                        }
                        // fallback: package name contains
                        if (cls.getPackageName().contains(t)) { ok = true; break; }
                    }
                    if (!ok) return false;
                }
                if (exclude != null && !exclude.isBlank()) {
                    for (var tag : exclude.split(",")) {
                        String t = tag.trim();
                        if (t.isEmpty()) continue;
                        if (cat != null) {
                            for (var cval : cat.value()) if (cval.equalsIgnoreCase(t)) return false;
                        }
                        if (cls.getPackageName().contains(t)) return false;
                    }
                }
                return true;
            };

//            how to run:
//            java -Dmetrics.include=complexity,structural -jar rosetta-cli/target/... analyze-repo https://github.com/...
            java.util.ServiceLoader.load(com.rosetta.engine.PostProcessor.class)
            .forEach(post::add);
            perFile.removeIf(e -> !byTag.test(e));
            post.removeIf(e -> !byTag.test(e));


            // (optionally) print what was loaded
            System.out.println("Loaded PerFile extractors: " +
                    perFile.stream().map(Object::getClass).map(Class::getSimpleName).toList());
            System.out.println("Loaded PostProcessors: " +
                    post.stream().map(Object::getClass).map(Class::getSimpleName).toList());

            var engine = new com.rosetta.engine.AnalysisEngine(perFile, post);
            engine.run(currentAnalysisContext, currentMetricSink);

            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
            try { git.getRepository().close(); } catch (Exception ignore) {}
            try { git.close(); } catch (Exception ignore) {}
        }
    }
}
