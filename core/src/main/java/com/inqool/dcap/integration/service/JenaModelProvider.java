/*
 * JenaModelProvider.java
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
package com.inqool.dcap.integration.service;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoModel;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes("text/rdf+n3")
@Produces("text/rdf+n3")
public class JenaModelProvider implements MessageBodyReader<ZdoModel>, MessageBodyWriter<ZdoModel> {
    @Inject
    @Zdo
    private Logger logger;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == ZdoModel.class;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == ZdoModel.class;
    }

    @Override
    public ZdoModel readFrom(Class<ZdoModel> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                          MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        final ZdoModel model = new ZdoModel();

        model.read(entityStream, "", "N3");


        final ResIterator it = model.listSubjects();
        if (it.hasNext()) {
            final Resource subject = it.next();
            final String uri = subject.getURI();
            model.setUrl(uri);
        } else {
            logger.warn("No subject was found in model. You need to call setUrl.");
        }

        return model;
    }

    @Override
    public void writeTo(ZdoModel model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        model.write(entityStream, "N3", "");
    }

    @Override
    @Deprecated
    public long getSize(ZdoModel model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }
}
