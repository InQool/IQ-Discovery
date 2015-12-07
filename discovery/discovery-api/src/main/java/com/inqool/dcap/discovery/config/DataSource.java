package com.inqool.dcap.discovery.config;

import org.picketlink.annotations.PicketLink;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class DataSource {
    @PersistenceContext(unitName = "dcapPersistenceUnit")
    private EntityManager entityManager;

    @Produces
    @PicketLink
    public EntityManager getPicketLinkEntityManager() {
        return entityManager;
    }

    @Produces
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
