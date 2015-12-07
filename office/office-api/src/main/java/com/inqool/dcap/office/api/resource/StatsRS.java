package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.common.StatsAccessCommon;
import com.inqool.dcap.common.dto.StatsDocsDto;
import com.inqool.dcap.common.dto.StatsWeeklyDto;
import com.inqool.dcap.common.entity.StatsDocs;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.TriplestoreStuff;
import com.inqool.dcap.office.api.core.StatsAccess;
import com.inqool.dcap.office.api.dto.StatsOrganizationDto;
import com.inqool.dcap.office.api.dto.StatsUserDto;
import org.picketlink.authorization.annotations.LoggedIn;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/stats")
@LoggedIn
@RequestScoped
public class StatsRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private StatsAccess statsAccess;

    @Inject
    private StatsAccessCommon statsAccessCommon;

    @Inject
    private DataStore store;

    @Inject
    private TriplestoreStuff triplestoreStuff;

    @Path("/topCurators/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopCurators(@QueryParam("limit") int limit) {
        try {
            List<StatsUserDto> statsUserList = statsAccess.getTopCurators(limit);
            return Response.ok(statsUserList).build();
        } catch (Exception e) {
            logger.error("Failed while getting top curators list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/topOrganizations/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopOrgs(@QueryParam("limit") int limit) {
        try {
            List<StatsOrganizationDto> statsOrgList = statsAccess.getTopOrgs(limit);
            return Response.ok(statsOrgList).build();
        } catch (Exception e) {
            logger.error("Failed while getting top organizations list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/topDocTypes/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopDocTypes(@QueryParam("limit") int limit, @QueryParam("organization") String org) {
        try {
            Map<String, Integer> resultMap = statsAccess.getTopDocTypes(limit, org);
            return Response.ok(resultMap).build();
        } catch(Exception e) {
            logger.error("Could not get top doc types.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/topDocSubTypes/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopDocSubTypes(@QueryParam("limit") int limit, @QueryParam("organization") String org) {
        try {
            Map<String, Integer> resultMap = statsAccess.getTopDocSubTypes(limit, org);
            return Response.ok(resultMap).build();
        } catch(Exception e) {
            logger.error("Could not get top doc subtypes.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/topZdoTypes/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopZdoTypes(@QueryParam("limit") int limit, @QueryParam("organization") String org) {
        try {
            Map<String, Integer> resultMap = statsAccess.getTopZdoTypes(limit, org);
            return Response.ok(resultMap).build();
        } catch(Exception e) {
            logger.error("Could not get top zdo types.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/topViewedDocs/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopViewedDocs(@QueryParam("limit") int limit) {
        try {
            List<StatsDocs> statsDocsList = statsAccessCommon.getTopViewedDocs(limit);
            List<StatsDocsDto> resultList = new ArrayList<>();
            for (StatsDocs statsDocs : statsDocsList) {
                String title = triplestoreStuff.findPublishedDocTitle(statsDocs.getDocInvId());
                StatsDocsDto statsDocsDto = new StatsDocsDto();
                statsDocsDto.setDocInvId(statsDocs.getDocInvId());
                statsDocsDto.setFavorites(statsDocs.getFavorites());
                statsDocsDto.setViews(statsDocs.getViews());
                statsDocsDto.setTitle(title);
                resultList.add(statsDocsDto);
            }
            return Response.ok(resultList).build();
        } catch(Exception e) {
            logger.error("Could not get top viewed docs.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/topFavoriteDocs/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopFavoriteDocs(@QueryParam("limit") int limit) {
        try {
            List<StatsDocs> statsDocsList = statsAccessCommon.getTopFavoriteDocs(limit);
            List<StatsDocsDto> resultList = new ArrayList<>();
            for (StatsDocs statsDocs : statsDocsList) {
                String title = triplestoreStuff.findPublishedDocTitle(statsDocs.getDocInvId());
                StatsDocsDto statsDocsDto = new StatsDocsDto();
                statsDocsDto.setDocInvId(statsDocs.getDocInvId());
                statsDocsDto.setFavorites(statsDocs.getFavorites());
                statsDocsDto.setViews(statsDocs.getViews());
                statsDocsDto.setTitle(title);
                resultList.add(statsDocsDto);
            }
            return Response.ok(resultList).build();
        } catch(Exception e) {
            logger.error("Could not get top favorite docs.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/weeklyDocStats/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeeklyDocStats(@QueryParam("limit") int limit, @QueryParam("organization") String org, @QueryParam("user") String user) {
        try {
            List<StatsWeeklyDto> statsDocsList = statsAccessCommon.getWeeklyStats(limit, org, user);
            return Response.ok(statsDocsList).build();
        } catch(Exception e) {
            logger.error("Could not get weekly doc stats.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    /*
    @Path("/weeklyConceptedDocCount/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeeklyConceptedDocCount(@QueryParam("limit") int limit, @QueryParam("organization") String org, @QueryParam("user") String user) {
        try {
            List<Tuple> statsDocsList = statsAccessCommon.getWeeklyConceptedDocCount(limit, org, user);
            return Response.ok(statsDocsList).build();
        } catch(Exception e) {
            logger.error("Could not get weekly concepted doc count stats.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/weeklyPublishedDocCount/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeeklyPublishedDocCount(@QueryParam("limit") int limit, @QueryParam("organization") String org, @QueryParam("user") String user) {
        try {
            List<StatsDocs> statsDocsList = statsAccessCommon.getTopFavoriteDocs(limit);
            return Response.ok(statsDocsList).build();
        } catch(Exception e) {
            logger.error("Could not get weekly published doc count stats.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/weeklyDocReservationCount/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeeklyDocReservationCount(@QueryParam("limit") int limit, @QueryParam("organization") String org, @QueryParam("user") String user) {
        try {
            List<StatsDocs> statsDocsList = statsAccessCommon.getTopFavoriteDocs(limit);
            return Response.ok(statsDocsList).build();
        } catch(Exception e) {
            logger.error("Could not get weekly doc reservation count stats.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }*/
}
