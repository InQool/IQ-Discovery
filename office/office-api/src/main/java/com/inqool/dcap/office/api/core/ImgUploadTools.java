package com.inqool.dcap.office.api.core;

import com.inqool.dcap.integration.model.ZdoFileType;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.model.ZdoTerms;
import com.inqool.dcap.integration.service.DataStore;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * @author Lukas Jane (inQool) 27. 7. 2015.
 */
@ApplicationScoped
public class ImgUploadTools {
    @Inject
    private DataStore store;

    public String uploadedFileToFedora(MultipartFormDataInput input, ZdoFileType fileType) throws IOException {
        //Extract the file from multipart
        List<InputPart> fileInputParts = input.getFormDataMap().get("file");
        if(fileInputParts == null || fileInputParts.isEmpty()) {
            throw new RuntimeException("No file received.");
        }
        InputPart fileInputPart = fileInputParts.get(0);
        InputStream fileStream = fileInputPart.getBody(InputStream.class, null);

        //Upload to Fedora
        String imageUuid = UUID.randomUUID().toString();
        ZdoModel model = new ZdoModel(store.createUrl(imageUuid), fileStream);
        model.add(ZdoTerms.mimeType, fileInputPart.getMediaType().toString());
        model.add(ZdoTerms.fileType, fileType.toString());
        store.update(model);
        return imageUuid;
    }
}
