package com.inqool.dcap.office.config;

import org.picketlink.annotations.PicketLink;

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
    @PicketLink
    public EntityManager getPicketLinkEntityManager() {
        return entityManager;
    }

    @Produces
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
