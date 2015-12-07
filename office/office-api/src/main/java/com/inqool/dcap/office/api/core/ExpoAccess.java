package com.inqool.dcap.office.api.core;

import com.inqool.dcap.common.entity.Expo;
import com.inqool.dcap.common.entity.QExpo;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoFileType;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.security.model.ZdoUser;
import com.mysema.query.jpa.impl.JPAQuery;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.picketlink.Identity;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Transactional
@RequestScoped
public class ExpoAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    @Inject
    private Identity identity;

    @Inject
    private DataStore store;

    @Inject
    private ImgUploadTools imgUploadTools;

    public List<Expo> listExpos() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QExpo qExpo = QExpo.expo;
        List<Expo> expoList = jpaQuery
                .from(qExpo)
                .where(qExpo.owner.eq(((ZdoUser) identity.getAccount()).getLoginName())
                        .and(qExpo.deleted.eq(false))
                )
                .list(qExpo);
        return expoList;
    }

    public int createExpo(Expo expo) {
        expo.setImageId(null);
        expo.setOwner(((ZdoUser) identity.getAccount()).getLoginName());
        em.persist(expo);
        em.flush();
        return expo.getId();
    }

    public Expo fetchExpo(int expoId) {
        Expo expo = em.find(Expo.class, expoId);
        if(expo == null || expo.isDeleted() || !expo.getOwner().equals(((ZdoUser) identity.getAccount()).getLoginName())) {
            throw new RuntimeException("Can't get expo, expo not found.");
        }
        return expo;
    }

    public void updateExpo(int expoId, Expo updatedExpo) {
        Expo dbExpo = em.find(Expo.class, expoId);
        if(!dbExpo.getOwner().equals(((ZdoUser) identity.getAccount()).getLoginName())) {
            throw new RuntimeException("Can't update expo, expo not found.");
        }
        dbExpo.setTitle(updatedExpo.getTitle());
        dbExpo.setPerex(updatedExpo.getPerex());
        dbExpo.setDocuments(updatedExpo.getDocuments());
        dbExpo.setContent(updatedExpo.getContent());
        dbExpo.setActive(updatedExpo.isActive());
        dbExpo.setPublishedFrom(updatedExpo.getPublishedFrom());
        dbExpo.setPublishedTo(updatedExpo.getPublishedTo());
        /*dbExpo.setCreated(updatedExpo.getCreated());*/
    }

    public void deleteExpo(int expoId) throws IOException {
        Expo expo = em.find(Expo.class, expoId);
        if(!expo.getOwner().equals(((ZdoUser) identity.getAccount()).getLoginName())) {
            throw new RuntimeException("Can't update expo, expo not found.");
        }
        String currentImageId = expo.getImageId();
        if(currentImageId != null) {
            store.delete(store.createUrl(currentImageId));
        }
        expo.setImageId(null);
        expo.setDeleted(true);
    }

    public void deleteExpos(List<Integer> expoIds) throws IOException {
        for (Integer expoId : expoIds) {
            deleteExpo(expoId);
        }
    }

    public void uploadExpoImage(MultipartFormDataInput input, int expoId) throws IOException {
        Expo expo = fetchExpo(expoId);
        if(!expo.getOwner().equals(((ZdoUser) identity.getAccount()).getLoginName())) {
            throw new RuntimeException("Can't update expo, expo not found.");
        }
        String currentImageId = expo.getImageId();
        if(currentImageId != null) {
            store.delete(store.createUrl(currentImageId));
        }

        String uuid = imgUploadTools.uploadedFileToFedora(input, ZdoFileType.expoImage);

        expo.setImageId(uuid);
        //autopersist
    }
}
