package com.inqool.dcap.integration.desa2.loader;

import com.inqool.dcap.config.Zdo;
import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * @author Lukas Jane (inQool) 1. 12. 2015.
 */
@RequestScoped
@Path("/museum")
public class XlsUploadRS {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "bach.data.dir")
    private String BACH_DATA_DIR;
    @Inject
    @ConfigProperty(name = "demus.data.dir")
    private String DEMUS_DATA_DIR;

    @Path("/upload")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadMuseumFile(MultipartFormDataInput input, @QueryParam("ico") String ico, @QueryParam("extension") String extension) {
        try {
            //Extract the file from multipart
            List<InputPart> fileInputParts = input.getFormDataMap().get("file");
            if(fileInputParts == null || fileInputParts.isEmpty()) {
                throw new RuntimeException("No file received.");
            }
            InputPart fileInputPart = fileInputParts.get(0);
            try(InputStream fileInputStream = fileInputPart.getBody(InputStream.class, null)) {
                File mainFolder;
                if(extension.equals("mdb")) {
                    mainFolder = new File(DEMUS_DATA_DIR);
                }
                else if(extension.equals("xls") || extension.equals("xlsx")) {
                    mainFolder = new File(BACH_DATA_DIR);
                }
                else {
                    throw new RuntimeException("Unknown file extension.");
                }

                if (!mainFolder.exists()) {
                    throw new RuntimeException("Main data input folder does not exist.");
                }
                FileFilter fileFilter = file -> (!file.isHidden() && file.isDirectory() && file.getName().startsWith(ico));
                File[] orgFolders = mainFolder.listFiles(fileFilter);
                if (orgFolders == null || orgFolders.length != 1) {
                    throw new RuntimeException("No matching organization folder found for ico " + ico + ".");
                }
                File orgFolder = orgFolders[0];

                try (FileOutputStream fileOutputStream = new FileOutputStream(new File(orgFolder, UUID.randomUUID().toString() + "." + extension))) {
                    IOUtils.copy(fileInputStream, fileOutputStream);
                }
            }
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Could not upload the file.", e);
            return Response.serverError().build();
        }
    }

}
