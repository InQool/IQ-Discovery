package com.inqool.dcap.office.api.core;

import com.inqool.dcap.common.entity.DocumentSubType;
import com.inqool.dcap.common.entity.DocumentType;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoType;
import com.inqool.dcap.office.api.dto.StatsOrganizationDto;
import com.inqool.dcap.office.api.dto.StatsUserDto;
import com.inqool.dcap.office.api.entity.*;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.model.ZdoUser;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 28. 9. 2015.
 */
@ApplicationScoped
@Transactional
public class StatsAccess {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    @Inject
    private PicketLinkAccess plAccess;

    public void incrementZdoTypeUsage(ZdoType zdoType, String organization, int number) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsZdoType qStatsZdoType = QStatsZdoType.statsZdoType;
        StatsZdoType statsZdoType = jpaQuery
                .from(qStatsZdoType)
                .where(qStatsZdoType.organization.eq(organization).and(qStatsZdoType.zdoType.eq(zdoType)))
                .singleResult(qStatsZdoType);
        if(statsZdoType == null) {
            statsZdoType = new StatsZdoType();
            statsZdoType.setZdoType(zdoType);
            statsZdoType.setOrganization(organization);
        }
        statsZdoType.setNumPublished(statsZdoType.getNumPublished() + number);
        if(!em.contains(statsZdoType)) {
            em.persist(statsZdoType);
        }
    }

    public void incrementDocTypeUsage(int docTypeId, String organization, int number) {
        DocumentType docType = em.find(DocumentType.class, docTypeId);

        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsDocType qStatsDocType = QStatsDocType.statsDocType;
        StatsDocType statsDocType = jpaQuery
                .from(qStatsDocType)
                .where(qStatsDocType.organization.eq(organization).and(qStatsDocType.docType.eq(docType)))
                .singleResult(qStatsDocType);
        if(statsDocType == null) {
            statsDocType = new StatsDocType();
            statsDocType.setDocType(docType);
            statsDocType.setOrganization(organization);
        }
        statsDocType.setNumPublished(statsDocType.getNumPublished() + number);
        if(!em.contains(statsDocType)) {
            em.persist(statsDocType);
        }
    }

    public void incrementDocSubTypeUsage(int docSubTypeId, String organization, int number) {
        DocumentSubType docSubType = em.find(DocumentSubType.class, docSubTypeId);

        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsDocSubType qStatsDocSubType = QStatsDocSubType.statsDocSubType;
        StatsDocSubType statsDocSubType = jpaQuery
                .from(qStatsDocSubType)
                .where(qStatsDocSubType.organization.eq(organization).and(qStatsDocSubType.docSubType.eq(docSubType)))
                .singleResult(qStatsDocSubType);
        if(statsDocSubType == null) {
            statsDocSubType = new StatsDocSubType();
            statsDocSubType.setDocSubType(docSubType);
            statsDocSubType.setOrganization(organization);
        }
        statsDocSubType.setNumPublished(statsDocSubType.getNumPublished() + number);
        if(!em.contains(statsDocSubType)) {
            em.persist(statsDocSubType);
        }
    }

    public void incrementOrganizationDocsPublished(String organization, int number) {
        StatsOrganization statsOrganization = em.find(StatsOrganization.class, organization);
        if(statsOrganization == null) {
            statsOrganization = new StatsOrganization();
            statsOrganization.setOrganization(organization);
            em.persist(statsOrganization);
        }
        statsOrganization.setDocsPublished(statsOrganization.getDocsPublished() + number);
    }

    public void decrementOrganizationDocsPublished(String organization, int number) {
        StatsOrganization statsOrganization = em.find(StatsOrganization.class, organization);
        if(statsOrganization == null) {
            return;
        }
        statsOrganization.setDocsPublished(statsOrganization.getDocsPublished() - number);
    }

    public void incrementUserDocsPublished(String user, int number) {
        StatsUser statsUser = em.find(StatsUser.class, user);
        if(statsUser == null) {
            statsUser = new StatsUser();
            statsUser.setUserId(user);
            em.persist(statsUser);
        }
        statsUser.setDocsPublished(statsUser.getDocsPublished() + number);
    }

    public void decrementUserDocsPublished(String user, int number) {
        StatsUser statsUser = em.find(StatsUser.class, user);
        if(statsUser == null) {
            return;
        }
        statsUser.setDocsPublished(statsUser.getDocsPublished() - number);
    }

    public List<StatsUserDto> getTopCurators(int limit) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsUser qStatsUser = QStatsUser.statsUser;
        List<StatsUser> statsUserList = jpaQuery
                .from(qStatsUser)
                .orderBy(qStatsUser.docsPublished.desc())
                .limit(limit)
                .list(qStatsUser);
        List<StatsUserDto> resultList = new ArrayList<>();
        for (StatsUser statsUser : statsUserList) {
            ZdoUser zdoUser = plAccess.getUser(statsUser.getUserId());
            StatsUserDto statsUserDto = new StatsUserDto();
            statsUserDto.setDocsPublished(statsUser.getDocsPublished());
            statsUserDto.setUserId(statsUser.getUserId());
            if(zdoUser != null) {
                statsUserDto.setFirstName(zdoUser.getFirstName());
                statsUserDto.setLastName(zdoUser.getLastName());
            }
            resultList.add(statsUserDto);

        }
        return resultList;
    }

    public List<StatsOrganizationDto> getTopOrgs(int limit) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsOrganization qStatsOrganization = QStatsOrganization.statsOrganization;
        List<StatsOrganization> statsOrganizationList = jpaQuery
                .from(qStatsOrganization)
                .orderBy(qStatsOrganization.docsPublished.desc())
                .limit(limit)
                .list(qStatsOrganization);
        List<StatsOrganizationDto> resultList = new ArrayList<>();
        for (StatsOrganization statsOrganization : statsOrganizationList) {
            String orgName = plAccess.getOrganizationName(statsOrganization.getOrganization());
            StatsOrganizationDto statsOrganizationDto = new StatsOrganizationDto();
            statsOrganizationDto.setDocsPublished(statsOrganization.getDocsPublished());
            statsOrganizationDto.setOrganization(orgName);
            resultList.add(statsOrganizationDto);
        }
        return resultList;
    }

    public Map<String, Integer> getTopDocTypes(int limit, String organization) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsDocType qStatsDocType = QStatsDocType.statsDocType;
        List<Tuple> tupleList;
        if(organization == null) {
            tupleList = jpaQuery
                    .from(qStatsDocType)
                    .groupBy(qStatsDocType.docType.name)
                    .orderBy(qStatsDocType.numPublished.sum().desc())
                    .limit(limit)
                    .list(qStatsDocType.docType.name, qStatsDocType.numPublished.sum());
            Map<String, Integer> resultMap = new LinkedHashMap<>(); //Linked to maintain order
            for (Tuple tuple : tupleList) {
                resultMap.put(tuple.get(qStatsDocType.docType.name), tuple.get(qStatsDocType.numPublished.sum()));
            }
            return resultMap;
        }
        else {
            tupleList = jpaQuery
                    .from(qStatsDocType)
                    .where(qStatsDocType.organization.eq(organization))
                    .orderBy(qStatsDocType.numPublished.desc())
                    .limit(limit)
                    .list(qStatsDocType.docType.name, qStatsDocType.numPublished);
            Map<String, Integer> resultMap = new LinkedHashMap<>(); //Linked to maintain order
            for (Tuple tuple : tupleList) {
                resultMap.put(tuple.get(qStatsDocType.docType.name), tuple.get(qStatsDocType.numPublished));
            }
            return resultMap;
        }
    }

    public Map<String, Integer> getTopDocSubTypes(int limit, String organization) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsDocSubType qStatsDocSubType = QStatsDocSubType.statsDocSubType;
        List<Tuple> tupleList;
        if(organization == null) {
            tupleList = jpaQuery
                    .from(qStatsDocSubType)
                    .groupBy(qStatsDocSubType.docSubType.name)
                    .orderBy(qStatsDocSubType.numPublished.sum().desc())
                    .limit(limit)
                    .list(qStatsDocSubType.docSubType.name, qStatsDocSubType.numPublished.sum());
            Map<String, Integer> resultMap = new LinkedHashMap<>(); //Linked to maintain order
            for (Tuple tuple : tupleList) {
                resultMap.put(tuple.get(qStatsDocSubType.docSubType.name), tuple.get(qStatsDocSubType.numPublished.sum()));
            }
            return resultMap;
        }
        else {
            tupleList = jpaQuery
                    .from(qStatsDocSubType)
                    .where(qStatsDocSubType.organization.eq(organization))
                    .orderBy(qStatsDocSubType.numPublished.desc())
                    .limit(limit)
                    .list(qStatsDocSubType.docSubType.name, qStatsDocSubType.numPublished);
            Map<String, Integer> resultMap = new LinkedHashMap<>(); //Linked to maintain order
            for (Tuple tuple : tupleList) {
                resultMap.put(tuple.get(qStatsDocSubType.docSubType.name), tuple.get(qStatsDocSubType.numPublished));
            }
            return resultMap;
        }
    }

    public Map<String, Integer> getTopZdoTypes(int limit, String organization) {
        JPAQuery jpaQuery = new JPAQuery(em);
        QStatsZdoType qStatsZdoType = QStatsZdoType.statsZdoType;
        List<Tuple> tupleList;
        if(organization == null) {
            tupleList = jpaQuery
                    .from(qStatsZdoType)
                    .groupBy(qStatsZdoType.zdoType)
                    .orderBy(qStatsZdoType.numPublished.sum().desc())
                    .limit(limit)
                    .list(qStatsZdoType.zdoType, qStatsZdoType.numPublished.sum());
            Map<String, Integer> resultMap = new LinkedHashMap<>(); //Linked to maintain order
            for (Tuple tuple : tupleList) {
                resultMap.put(tuple.get(qStatsZdoType.zdoType).name(), tuple.get(qStatsZdoType.numPublished.sum()));
            }
            return resultMap;
        }
        else {
            tupleList = jpaQuery
                    .from(qStatsZdoType)
                    .where(qStatsZdoType.organization.eq(organization))
                    .orderBy(qStatsZdoType.numPublished.desc())
                    .limit(limit)
                    .list(qStatsZdoType.zdoType, qStatsZdoType.numPublished);
            Map<String, Integer> resultMap = new LinkedHashMap<>(); //Linked to maintain order
            for (Tuple tuple : tupleList) {
                resultMap.put(tuple.get(qStatsZdoType.zdoType).name(), tuple.get(qStatsZdoType.numPublished));
            }
            return resultMap;
        }
    }
}
