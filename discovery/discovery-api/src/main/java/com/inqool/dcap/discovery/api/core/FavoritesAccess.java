package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.StatsAccessCommon;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.dto.DocumentReservationDto;
import com.inqool.dcap.discovery.api.dto.DocumentReserveRequestDto;
import com.inqool.dcap.discovery.api.dto.FavoriteDocumentDto;
import com.inqool.dcap.discovery.api.entity.*;
import com.inqool.dcap.discovery.api.exception.ReservedRecentlyException;
import com.inqool.dcap.discovery.api.resource.Search;
import com.inqool.dcap.discovery.security.PicketLinkAccessDiscovery;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.security.model.DiscoveryUser;
import com.mysema.query.jpa.impl.JPAQuery;
import lombok.Getter;
import lombok.Setter;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author Lukas Jane (inQool) 17. 8. 2015.
 */
@ApplicationScoped
@Transactional
public class FavoritesAccess {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    @Inject
    private PicketLinkAccessDiscovery plAccess;

    @Inject
    private DataStore store;

    @Inject
    private MailNotifier mailNotifier;

    @Inject
    private Search search;

    @Inject
    private StatsAccessCommon statsAccessCommon;

    @Inject
    private StatsAsyncLayer statsAsyncLayer;

    //Favorite documents
    public void addFavDoc(String favDocInvId) {
        FavoriteDocument favoriteDocument = new FavoriteDocument();
        favoriteDocument.setUserId(plAccess.getCurrentUser().getId());
        favoriteDocument.setDocInvId(favDocInvId);
        em.persist(favoriteDocument);
        statsAsyncLayer.incrementDocFavorites(favDocInvId);
    }

    public Collection<FavoriteDocumentDto> listFavDocs() throws SolrServerException {
        QFavoriteDocument qFavoriteDocument = QFavoriteDocument.favoriteDocument;
        JPAQuery jpaQuery = new JPAQuery(em);
        List<FavoriteDocument> resultList = jpaQuery.from(qFavoriteDocument)
                .where(qFavoriteDocument.userId.eq(plAccess.getCurrentUser().getId()))
                .list(qFavoriteDocument);
        Map<String, FavoriteDocumentDto> helperMap = new HashMap<>();
        for (FavoriteDocument favoriteDocument : resultList) {
            FavoriteDocumentDto favDocDto = new FavoriteDocumentDto();
            favDocDto.setId(favoriteDocument.getId());
            favDocDto.setInvId(favoriteDocument.getDocInvId());
            helperMap.put(favoriteDocument.getDocInvId(), favDocDto);
        }
        //Find docs with these inventory ids in Solr and fill in details
        SolrDocumentList solrDocumentList = search.searchByInvIdList(resultList);
        for (SolrDocument solrDocument : solrDocumentList) {
            String invId = (String) solrDocument.getFieldValue("inventoryId");
            FavoriteDocumentDto favDocDto = helperMap.get(invId);
            if(favDocDto == null) {
                logger.error("Solr returned document with inventory id that he wasnt asked for.");
                continue;
            }
            favDocDto.setFedoraId((String) solrDocument.getFieldValue("id"));
            favDocDto.setTitle((String) solrDocument.getFirstValue("title"));
        }
        return helperMap.values();
    }

    public void deleteFavDoc(int id) {
        FavoriteDocument favoriteDocument = em.find(FavoriteDocument.class, id);
        em.remove(favoriteDocument);
        statsAsyncLayer.decrementDocFavorites(favoriteDocument.getDocInvId());
    }

    //Clipboard documents
    public void addClipDoc(String clipDocInvId) {
        ClipboardDocument clipboardDocument = new ClipboardDocument();
        clipboardDocument.setUserId(plAccess.getCurrentUser().getId());
        clipboardDocument.setDocInvId(clipDocInvId);
        em.persist(clipboardDocument);
    }

    public Collection<FavoriteDocumentDto> listClipDocs() throws SolrServerException {
        QClipboardDocument qClipboardDocument = QClipboardDocument.clipboardDocument;
        JPAQuery jpaQuery = new JPAQuery(em);
        List<ClipboardDocument> resultList = jpaQuery.from(qClipboardDocument)
                .where(qClipboardDocument.userId.eq(plAccess.getCurrentUser().getId()))
                .list(qClipboardDocument);
        Map<String, FavoriteDocumentDto> helperMap = new HashMap<>();
        for (ClipboardDocument clipboardDocument : resultList) {
            FavoriteDocumentDto favDocDto = new FavoriteDocumentDto();
            favDocDto.setId(clipboardDocument.getId());
            favDocDto.setInvId(clipboardDocument.getDocInvId());
            helperMap.put(clipboardDocument.getDocInvId(), favDocDto);
        }
        //Find docs with these inventory ids in Solr and fill in details
        SolrDocumentList solrDocumentList = search.searchByInvIdList(resultList);
        for (SolrDocument solrDocument : solrDocumentList) {
            String invId = (String) solrDocument.getFieldValue("inventoryId");
            FavoriteDocumentDto favDocDto = helperMap.get(invId);
            favDocDto.setFedoraId((String) solrDocument.getFieldValue("id"));
            favDocDto.setTitle((String) solrDocument.getFirstValue("title"));
        }
        return helperMap.values();
    }

    public void deleteClipDoc(int id) {
        ClipboardDocument clipboardDocument = em.find(ClipboardDocument.class, id);
        em.remove(clipboardDocument);
    }

    //Favorite queries
    public void addFavQuery(FavoriteQuery favoriteQuery) {
        favoriteQuery.setUserId(plAccess.getCurrentUser().getId());
        em.persist(favoriteQuery);
    }

    public List<FavoriteQuery> listFavQueries() {
        QFavoriteQuery qFavoriteQuery = QFavoriteQuery.favoriteQuery;
        JPAQuery jpaQuery = new JPAQuery(em);
        List<FavoriteQuery> resultList = jpaQuery.from(qFavoriteQuery)
                .where(qFavoriteQuery.userId.eq(plAccess.getCurrentUser().getId()))
                .list(qFavoriteQuery);
        return resultList;
    }

    public void deleteFavQuery(int id) {
        FavoriteQuery favoriteQuery = em.find(FavoriteQuery.class, id);
        em.remove(favoriteQuery);
    }

    //Document reservation
    public void reserveDocs(DocumentReserveRequestDto docReserveRequestDto) throws IOException, ReservedRecentlyException {
        DiscoveryUser discoveryUser = plAccess.getCurrentUser();
        if(!discoveryUser.isVerified()) {
            throw new RuntimeException("Unverified user tried to reserve documents.");
        }
        List<String> invIds = new ArrayList<>();
        Map<String, DocReservationHolder> ownerToDocsMap = new HashMap<>(); //For every document owner, stores invIds of his documents reserved
        for (String fedoraId : docReserveRequestDto.getFedoraIds()) {
            ZdoModel model = store.get(store.createUrl(fedoraId));
            String invId = model.get(ZdoTerms.inventoryId);
            invIds.add(invId);

            //Verify there wasn't the same reservation request recently
            QDocumentReservation qDocumentReservation = QDocumentReservation.documentReservation;
            JPAQuery jpaQuery = new JPAQuery(em);
            DocumentReservation oldDocumentReservation = jpaQuery.from(qDocumentReservation)
                    .where(qDocumentReservation.userId.eq(discoveryUser.getId())
                            .and(qDocumentReservation.docInvId.eq(invId)))
                    .orderBy(qDocumentReservation.date.desc())
                    .singleResult(qDocumentReservation);
            if(oldDocumentReservation != null && oldDocumentReservation.getDate().until(LocalDateTime.now(), ChronoUnit.DAYS) < 90) {
                throw new ReservedRecentlyException("Can't reserve document because of recent reservation.");
            }

            //Store info for later actions (notifications and statistics)
            String owner = model.get(ZdoTerms.owner);
            DocReservationHolder docReservationHolder = ownerToDocsMap.get(owner);
            if(docReservationHolder == null) {
                docReservationHolder = new DocReservationHolder();
                docReservationHolder.setOrganization(model.get(ZdoTerms.organization));
                ownerToDocsMap.put(owner, docReservationHolder);
            }
            docReservationHolder.getDocInvIds().add(invId);

            //Store to db
            DocumentReservation documentReservation = new DocumentReservation();
            documentReservation.setUserId(discoveryUser.getLoginName());
            documentReservation.setDocInvId(invId);
            documentReservation.setDate(LocalDateTime.now());
            documentReservation.setReason(docReserveRequestDto.getReason());
            em.persist(documentReservation);
        }

        //For every owner, send notification mail with reserved documents ids
        for (Map.Entry<String, DocReservationHolder> entry : ownerToDocsMap.entrySet()) {
            Query query = em.createNativeQuery("SELECT email FROM dcap_account WHERE login_name = '" + entry.getKey() + "';");
            String email = (String) query.getSingleResult();
            mailNotifier.notifyOwnerDocumentReserved(
                    discoveryUser,
                    entry.getValue().getDocInvIds(),
                    docReserveRequestDto.getReason(),
                    email);
            statsAccessCommon.documentReserved(entry.getKey(), entry.getValue().getOrganization(), entry.getValue().getDocInvIds().size());
        }

        //And notify the client that requested the reservation
        mailNotifier.notifyClientDocumentsReserved(
                discoveryUser,
                invIds,
                docReserveRequestDto.getReason());
    }

    @Getter
    @Setter
    private class DocReservationHolder {
        List<String> docInvIds = new ArrayList<>();
        String organization;
    }

    public Collection<DocumentReservationDto> listReservedDocs() throws SolrServerException {
        String userId = plAccess.getCurrentUser().getId();
        QDocumentReservation qDocumentReservation = QDocumentReservation.documentReservation;
        JPAQuery jpaQuery = new JPAQuery(em);
        List<DocumentReservation> documentReservationList = jpaQuery.from(qDocumentReservation)
                .where(qDocumentReservation.userId.eq(userId))
                .orderBy(qDocumentReservation.date.desc())
                .list(qDocumentReservation);
        Map<String, DocumentReservationDto> helperMap = new HashMap<>();
        for (DocumentReservation documentReservation : documentReservationList) {
            DocumentReservationDto docReservationDto = new DocumentReservationDto();
            docReservationDto.setId(documentReservation.getId());
            docReservationDto.setInvId(documentReservation.getDocInvId());
            docReservationDto.setReason(documentReservation.getReason());
            docReservationDto.setDate(documentReservation.getDate());
            helperMap.put(documentReservation.getDocInvId(), docReservationDto);
        }
        //Find docs with these inventory ids in Solr and fill in details
        SolrDocumentList solrDocumentList = search.searchByInvIdList(documentReservationList);
        for (SolrDocument solrDocument : solrDocumentList) {
            String invId = (String) solrDocument.getFieldValue("inventoryId");
            DocumentReservationDto docReservationDto = helperMap.get(invId);
            docReservationDto.setFedoraId((String) solrDocument.getFieldValue("id"));
            docReservationDto.setTitle((String) solrDocument.getFirstValue("title"));
        }
        return helperMap.values();
    }


    //Query about a document
    public void queryDoc(String fedoraId, String docQuery) throws IOException {
        ZdoModel model = store.get(store.createUrl(fedoraId));
        String invId = model.get(ZdoTerms.inventoryId);

        //Find doc owner mail and notify him
        String owner = model.get(ZdoTerms.owner);

        Query query = em.createNativeQuery("SELECT email FROM dcap_account WHERE login_name = '" + owner + "';");
        String ownerEmail = (String) query.getSingleResult();
        DiscoveryUser discoveryUser = plAccess.getCurrentUser();
        mailNotifier.notifyOwnerDocumentQuery(
                discoveryUser,
                invId,
                docQuery,
                ownerEmail);
    }
}
