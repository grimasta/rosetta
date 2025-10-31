
package com.rosetta.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Session;
import java.io.File;
import java.sql.Statement;

public class HibernateUtil {
    private static final EntityManagerFactory EMF = build();

    private static EntityManagerFactory build() {
        try {
            File dataDir = new File("data");
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                throw new RuntimeException("Failed to create data directory at: " + dataDir.getAbsolutePath());
            }
            File workDir = new File("work");
            if (!workDir.exists()) workDir.mkdirs();

            EntityManagerFactory emfLocal = Persistence.createEntityManagerFactory("rosettaPU");

            EntityManager em = emfLocal.createEntityManager();
            try {
                em.unwrap(Session.class).doWork(conn -> {
                    try (Statement s = conn.createStatement()) {
                        s.execute("PRAGMA journal_mode=WAL");
                        s.execute("PRAGMA synchronous=NORMAL");
                    }
                });
            } catch (Exception ignore) {
            } finally {
                em.close();
            }
            return emfLocal;
        } catch (Exception e) {
            throw new RuntimeException("Failed to init EntityManagerFactory", e);
        }
    }

    public static EntityManagerFactory emf() { return EMF; }
}
