package com.inqool.dcap.office.api.resource;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.inqool.dcap.config.Zdo;
import org.apache.commons.io.FileUtils;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/gatoken")
@RequestScoped
public class GoogleAnalyticsRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "tmp.folder")
    private String tmpFolderName;

    @Path("/")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response retrieveGoogleApiToken() {
        try {
            String clientId = "46458516498-m7nrgk3sfutekff8lgt5mg1rj15imio6@developer.gserviceaccount.com";
            JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            //Copy the key out from war to a file, if it is not there already
            File googleApiKeyFile = new File(tmpFolderName + "googleAnalyticsApiKey.p12");
            if(!googleApiKeyFile.exists()) {
                FileUtils.copyInputStreamToFile(this.getClass().getClassLoader().getResourceAsStream("googleAnalyticsApiKey.p12"), googleApiKeyFile);
            }

            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountId(clientId)
                    .setServiceAccountPrivateKeyFromP12File(googleApiKeyFile)
                    .setServiceAccountScopes(Collections.singleton("https://www.googleapis.com/auth/analytics.readonly"))
                    /*.setServiceAccountUser("user@example.com")*/
                    .build();
            credential.refreshToken();
            return Response.ok(credential.getAccessToken()).build();

            //Not quite working this way
/*            GoogleTokenResponse response =
                    new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                            clientId, clientSecret,
                            code, redirectUri)
                            .execute();
            return Response.ok(response.getAccessToken()).build();*/

        } catch (TokenResponseException e) {
            if (e.getDetails() != null) {
                String error = "Google analytics token request error: " + e.getDetails().getError();
                if (e.getDetails().getErrorDescription() != null) {
                    error += (e.getDetails().getErrorDescription());
                }
                if (e.getDetails().getErrorUri() != null) {
                    error += e.getDetails().getErrorUri();
                }
                logger.error(error, e);
            } else {
                logger.error("Google analytics token request ended badly.", e);
            }
        } catch (IOException | GeneralSecurityException e) {
            logger.error("Google analytics token request failed.", e);
        }
        return Response.serverError().build();
    }
}
