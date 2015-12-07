/*
 * TestResource.java
 *
 * Copyright (c) 2014  inQool a.s.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.common.OrgMappingAccess;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.security.PicketLinkAccess;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.picketlink.authorization.annotations.LoggedIn;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

@RequestScoped
@LoggedIn
@Path("/museum")
public class MuseumUploadProxy {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "desa2.endpoint")
    private String DESA2_ENDPOINT;

    @Inject
    private PicketLinkAccess picketLinkAccess;
    @Inject
    private OrgMappingAccess orgMappingAccess;

    @Inject
    private ProjectStage projectStage;

    @Path("/upload")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFileToMuseumDataLoadFolders(MultipartFormDataInput input) {
        if(!(ProjectStage.Staging.equals(projectStage) || ProjectStage.Production.equals(projectStage))) {
            logger.error("Museum file upload not usable outside of Zlin.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            //Determine user's organization ico so that we can put file into the right folder
            String orgIdmId = picketLinkAccess.getUsersOrganization().getName();
            String ico = orgMappingAccess.getOrgIco(orgIdmId);
            if(ico == null) {
                logger.error("Could not determine user's organization ICO.");
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            //Extract the file from multipart
            List<InputPart> fileInputParts = input.getFormDataMap().get("file");
            if(fileInputParts == null || fileInputParts.isEmpty()) {
                throw new RuntimeException("No file received.");
            }
            InputPart fileInputPart = fileInputParts.get(0);
            InputStream fileStream = fileInputPart.getBody(InputStream.class, null);

            //Determine file extension
            String fileName = getFileName(fileInputPart.getHeaders());
            if(!fileName.contains(".")) {
                throw new RuntimeException("Could not determine file extension for file " + fileName);
            }
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();

            //Create multipart with attachment file
            MultipartFormDataOutput mdo = new MultipartFormDataOutput();
            mdo.addFormData("file", fileStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(mdo) {};
            Response upstreamResponse = ClientBuilder
                    .newBuilder()
                    .build()
                    .target(DESA2_ENDPOINT + "museum/upload")
                    .queryParam("ico", ico)
                    .queryParam("extension", extension)
                    .request()
                    .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
            if(!upstreamResponse.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                logger.error("Could not upload the museum file - received " + upstreamResponse.getStatus() + " from loader server.");
                return Response.serverError().build();
            }
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not upload the museum file.", e);
            return Response.serverError().build();
        }
    }

    private String getFileName(MultivaluedMap<String, String> header) {

        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }
}
