package com.rosetta.services;

import com.rosetta.domain.Project;
import com.rosetta.storage.HibernateUtil;
import jakarta.persistence.EntityManager;

public class ProjectService {
    public Project getOrCreateProject(String name) {
        EntityManager em = HibernateUtil.emf().createEntityManager();
        try {
            em.getTransaction().begin();
            Project p = em.createQuery("select p from Project p where p.name = :n", Project.class)
                    .setParameter("n", name)
                    .getResultStream().findFirst().orElse(null);
            if (p == null) {
                p = new Project();
                p.setName(name);
                em.persist(p);
            }
            em.getTransaction().commit();
            return p;
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }
}
