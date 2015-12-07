package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.core.DiscoveryUserAccess;
import com.inqool.dcap.office.api.dto.CuratorHolder;
import com.inqool.dcap.office.api.dto.StatsUserDto;
import com.inqool.dcap.office.api.entity.DiscoveryUserFake;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.ZdoRoles;
import com.inqool.dcap.security.model.ZdoUser;
import org.picketlink.Identity;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 26. 3. 2015.
 */
@Path("/people")
@LoggedIn
@RequestScoped
public class People {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private PicketLinkAccess picketLinkAccess;

    @Inject
    private Identity identity;

    @Inject
    private DiscoveryUserAccess discoveryUserAccess;

    @Path("/curators/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        try {
            List<ZdoUser> zdoUserList = picketLinkAccess.getAllUsers();
            List<StatsUserDto> statsUserDtoList = new ArrayList<>();
            if (zdoUserList.isEmpty()) {
                return Response.ok("[]").build();
            }

            zdoUserList.stream()
                    .filter(zdoUser -> picketLinkAccess.getRolesOfUser(zdoUser).contains(ZdoRoles.curator))
                    .forEach(zdoUser -> {
                        StatsUserDto statsUserDto = new StatsUserDto();
                        statsUserDto.setUserId(zdoUser.getLoginName());
                        statsUserDto.setFirstName(zdoUser.getFirstName());
                        statsUserDto.setLastName(zdoUser.getLastName());
                        statsUserDtoList.add(statsUserDto);
                    });

            return Response.ok(statsUserDtoList).build();
        } catch (Exception e) {
            logger.error("Could not return all users.", e);
            return Response.serverError().build();
        }
    }

    @Path("/sameOrgCurators/ofUser/{idmId}")
    @GET
    @RolesAllowed({ZdoRoles.ADMIN_ORG, ZdoRoles.ADMIN_SYS})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSameOrgCurators(@PathParam("idmId") String idmId) {
        try {
            List<CuratorHolder> curatorHolderList = listFellowCurators(idmId);
            if (curatorHolderList.isEmpty()) {
                return Response.ok("[]").build();
            }
            return Response.ok(curatorHolderList).build();
        } catch (Exception e) {
            logger.error("Could not return curators.", e);
            return Response.serverError().build();
        }
    }

    @Path("/sameOrgCurators/")
    @GET
    @RolesAllowed(ZdoRoles.CURATOR)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSameOrgCurators() {
        try {
            String userToSkipIdmId = ((ZdoUser) identity.getAccount()).getLoginName();
            List<CuratorHolder> curatorHolderList = listFellowCurators(userToSkipIdmId);
            if (curatorHolderList.isEmpty()) {
                return Response.ok("[]").build();
            }
            return Response.ok(curatorHolderList).build();
        } catch (Exception e) {
            logger.error("Could not return curators.", e);
            return Response.serverError().build();
        }
    }

    private List<CuratorHolder> listFellowCurators(String ofUser) {
        List<CuratorHolder> curatorHolderList = new ArrayList<>();
        List<ZdoUser> curators = picketLinkAccess.getFellowCurators(ofUser);
        for (ZdoUser curator : curators) {
            if (ofUser.equals(curator.getLoginName())) {  //Don't list this user
                continue;
            }
            CuratorHolder curatorHolder = new CuratorHolder();
            curatorHolder.setIdmId(curator.getLoginName());
            curatorHolder.setFirstName(curator.getFirstName());
            curatorHolder.setLastName(curator.getLastName());
            curatorHolderList.add(curatorHolder);
        }
        return curatorHolderList;
    }

    @Path("/discoveryUser/byCode/{opNumber}")
    @GET
    @RolesAllowed({ZdoRoles.ADMIN_ORG, ZdoRoles.ADMIN_SYS})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryUserByOpNumber(@PathParam("opNumber") String opNumber) {
        try {
            DiscoveryUserFake discoveryUser = discoveryUserAccess.getUserByOpNumber(opNumber);
            if (discoveryUser == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(discoveryUser).build();
        } catch (Exception e) {
            logger.error("Could not return user.", e);
            return Response.serverError().build();
        }
    }

    @Path("/discoveryUser/verify/")
    @POST
    @RolesAllowed({ZdoRoles.ADMIN_ORG, ZdoRoles.ADMIN_SYS})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyUser(DiscoveryUserFake discoveryUser) {
        try {
            if (!discoveryUserAccess.markUserAsVerified(discoveryUser)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not verify user.", e);
            return Response.serverError().build();
        }
    }

}
