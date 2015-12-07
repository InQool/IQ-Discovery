package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.exception.FailedToLoadException;
import com.inqool.dcap.integration.exception.FailedToParseException;
import com.inqool.dcap.security.ZdoRoles;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;

@ApplicationScoped
@RolesAllowed({ZdoRoles.ADMIN_SYS, ZdoRoles.ADMIN_ORG})
@Path("/dataLoad/")
public class DataLoadProxy {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "desa2.endpoint")
    private String DESA2_ENDPOINT;

    @Path("/kdr/")
    @POST
    public Response loadKdrData() throws FailedToLoadException, FailedToParseException, IOException {
        return ClientBuilder.newClient().target(DESA2_ENDPOINT + "kdr/load").request().get();
    }

    @Path("/bach/")
    @POST
    public Response loadBachData() throws FailedToLoadException, FailedToParseException, IOException {
        return ClientBuilder.newClient().target(DESA2_ENDPOINT + "bach/load").request().get();
    }

    @Path("/demus/")
    @POST
    public Response loadDemusData() throws FailedToLoadException, FailedToParseException, IOException {
        return ClientBuilder.newClient().target(DESA2_ENDPOINT + "demus/load").request().get();
    }
}
