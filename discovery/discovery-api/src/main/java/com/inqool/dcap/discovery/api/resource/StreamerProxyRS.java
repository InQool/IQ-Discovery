package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.service.DataStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool)
 */
@RequestScoped
@WebServlet(value = "/data/stream/*", name = "StreamerProxyRS")
public class StreamerProxyRS extends javax.servlet.http.HttpServlet {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private DataStore store;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Reconstruct Fedora url of the file requested
        String path = req.getPathInfo();
        String id = path.substring(path.lastIndexOf("/") + 1, path.length());
        String url = store.createUrl(id);

        //Start building Fedora request
        Invocation.Builder builder = ClientBuilder.newClient().target(url).request();

        //Copy request headers to Fedora request
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Collection<String> headers = resp.getHeaders(headerName);
            for (String header : headers) {
                builder.header(headerName, header);
            }
        }

        //Call Fedora
        Response fedoraResponse = builder.get();

        //Copy response status
        resp.setStatus(fedoraResponse.getStatusInfo().getStatusCode());

        //Copy response headers
        for (Map.Entry<String, List<Object>> stringListEntry : fedoraResponse.getHeaders().entrySet()) {
            String headerName = stringListEntry.getKey();
            List<Object> headers = stringListEntry.getValue();
            for (Object header : headers) {
                resp.addHeader(headerName, String.valueOf(header));
            }
        }

        //Copy response data
        if (fedoraResponse.hasEntity()) {
            try(InputStream is = fedoraResponse.readEntity(InputStream.class)) {
                OutputStream os = resp.getOutputStream();
                IOUtils.copy(is, os);
//                ByteStreams.copy(is, os); //practically the same
            } catch (Exception e) {
                logger.error("Exception while streaming.", e);
            }
        }
    }
}
