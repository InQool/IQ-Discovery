package com.inqool.dcap.integration.desa2.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SuppressWarnings("unused")
@ApplicationScoped
public class Data {
    @PersistenceContext(unitName = "dcapPersistenceUnit")
    private EntityManager entityManager;

    @Produces
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
