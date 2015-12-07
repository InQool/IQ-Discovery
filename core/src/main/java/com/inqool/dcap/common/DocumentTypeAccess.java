package com.inqool.dcap.common;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.inqool.dcap.common.entity.DocumentSubType;
import com.inqool.dcap.common.entity.DocumentType;
import com.inqool.dcap.common.entity.QDocumentSubType;
import com.inqool.dcap.common.entity.QDocumentType;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.mysema.query.jpa.impl.JPAQuery;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@RequestScoped
@Transactional
public class DocumentTypeAccess {
    @Inject
    private EntityManager em;

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;

    public int createType(String name) {
        DocumentType documentType = new DocumentType();
        documentType.setName(name);
        em.persist(documentType);
        em.flush();
        return documentType.getId();
    }

    public int createSubType(int typeId, String name) {
        DocumentType docType = em.find(DocumentType.class, typeId);
        if(docType == null) {
            throw new RuntimeException("Can't add subtype to that type, type wasn't found.");
        }
        DocumentSubType documentSubType = new DocumentSubType();
        documentSubType.setName(name);
        documentSubType.setOwningType(docType);
        em.persist(documentSubType);
        em.flush();
        return documentSubType.getId();
    }

    public String getTypeNameForId(int id) {
        DocumentType documentType = em.find(DocumentType.class, id);
        return documentType.getName();
    }

    public String getSubTypeNameForId(int id) {
        DocumentSubType documentSubType = em.find(DocumentSubType.class, id);
        return documentSubType.getName();
    }

    public List<Map<String, Object>> listAll() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QDocumentType qDocumentType = QDocumentType.documentType;
        List<DocumentType> fullList = jpaQuery.from(qDocumentType).where(qDocumentType.deleted.eq(false)).list(qDocumentType);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for(DocumentType documentType : fullList) {
            Map<String, Object> docTypeMap = new HashMap<>();
            docTypeMap.put("id", documentType.getId());
            docTypeMap.put("name", documentType.getName());
            docTypeMap.put("subtypeCount", documentType.getSubTypes().size());
            resultList.add(docTypeMap);
        }
        return resultList;
    }

    public DocumentType fetchType(int id) {
        DocumentType documentType = em.find(DocumentType.class, id);
        if(documentType == null || documentType.isDeleted()) {
            throw new RuntimeException("Can't get type, type not found.");
        }
        DocumentType resultDocType = new DocumentType();
        resultDocType.setId(documentType.getId());
        resultDocType.setName(documentType.getName());
        documentType    //Add subtypes that are not deleted
                .getSubTypes()
                .stream()
                .filter(subtype -> !subtype.isDeleted())
                .forEach(resultDocType.getSubTypes()::add);
        return resultDocType;
    }

    public void updateType(int id, String name) {
        DocumentType documentType = em.find(DocumentType.class, id);
        if(documentType == null) {
            throw new RuntimeException("Can't update type, type not found.");
        }
        documentType.setName(name);
    }

    public void updateSubType(int id, String name) {
        DocumentSubType documentSubType = em.find(DocumentSubType.class, id);
        if(documentSubType == null) {
            throw new RuntimeException("Can't update subtype, subtype not found.");
        }
        documentSubType.setName(name);
    }

    public boolean deleteType(int id) {
        DocumentType documentType = em.find(DocumentType.class, id);
        //Find if nobody uses this type
        String queryString = "SELECT ?subject WHERE {" +
                "?subject <" + ZdoTerms.documentType + "> \"" + documentType.getId() + "\"^^<http://www.w3.org/2001/XMLSchema#string>." +
                " }";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
        ResultSet resultSet = queryExecution.execSelect();
        if(resultSet.hasNext()) {
            logger.error(resultSet.next().getResource("subject").getURI() + " is still using this type, cant delete.");
            return false;
        }
        documentType.setDeleted(true);
        documentType.getSubTypes().forEach(x -> x.setDeleted(true));
        return true;
    }

    public boolean deleteSubType(int id) {
        DocumentSubType documentSubType = em.find(DocumentSubType.class, id);
        //Find if nobody uses this subtype
        String queryString = "SELECT ?subject WHERE {" +
                "?subject <" + ZdoTerms.documentSubType + "> \"" + documentSubType.getId() + "\"^^<http://www.w3.org/2001/XMLSchema#string>." +
                " }";
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, queryString);
        ResultSet resultSet = queryExecution.execSelect();
        if(resultSet.hasNext()) {
            logger.error(resultSet.next().getResource("subject").getURI() + " is still using this subtype, cant delete.");
            return false;
        }
        documentSubType.getOwningType().getSubTypes().remove(documentSubType);
        documentSubType.setDeleted(true);
        return true;
    }

    public int findIdForTypeName(String name) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QDocumentType qDocumentType = QDocumentType.documentType;
        Integer typeId = jpaQuery.from(qDocumentType)
                .where(qDocumentType.name.eq(name))
                .singleResult(qDocumentType.id);
        if(typeId == null) {
            throw new RuntimeException("Document type id for name " + name + " was not found.");
        }
        return typeId;
    }

    public int findIdForSubTypeName(String name, int parentTypeId) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QDocumentSubType qDocumentSubType = QDocumentSubType.documentSubType;
        Integer typeId = jpaQuery.from(qDocumentSubType)
                .where(qDocumentSubType.name.eq(name)
                .and(qDocumentSubType.owningType.id.eq(parentTypeId)))
                .singleResult(qDocumentSubType.id);
        if(typeId == null) {
            throw new RuntimeException("Document subtype id for name " + name + " and parent id " + parentTypeId + " was not found.");
        }
        return typeId;
    }
}
