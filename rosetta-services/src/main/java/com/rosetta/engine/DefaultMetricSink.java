
package com.rosetta.engine;

import com.rosetta.domain.*;
import jakarta.persistence.EntityManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMetricSink implements MetricSink {
    private final EntityManager em;
    private final Repository repo;
    private final Commit commit;
    private final Map<String, Metric> metricCache = new ConcurrentHashMap<>();

    public DefaultMetricSink(EntityManager em, Repository repo, Commit commit) {
        this.em = em;
        this.repo = repo;
        this.commit = commit;
    }

    @Override
    public void emit(String fileRelPath, String key, double value) {
        FileEntity fe = em.createQuery(
                "select f from FileEntity f where f.repository = :r and f.path = :p", FileEntity.class)
            .setParameter("r", repo)
            .setParameter("p", fileRelPath)
            .getResultStream().findFirst().orElse(null);
        if (fe == null) {
            fe = new FileEntity();
            fe.setRepository(repo);
            fe.setPath(fileRelPath);
            fe.setLanguage(com.rosetta.services.LanguageDetector.detectFromFilename(fileRelPath));
            em.persist(fe);
        }

        Metric metric = metricCache.computeIfAbsent(key, k -> {
            Metric m = em.createQuery("select m from Metric m where m.key = :k", Metric.class)
                    .setParameter("k", k).getResultStream().findFirst().orElse(null);
            if (m == null) {
                m = new Metric();
                m.setKey(k);
                m.setUnit("count");
                em.persist(m);
            }
            return m;
        });

        MetricValue mv = new MetricValue();
        mv.setMetric(metric);
        mv.setFile(fe);
        mv.setCommit(commit);
        mv.setValue(value);
        em.persist(mv);
    }
}
