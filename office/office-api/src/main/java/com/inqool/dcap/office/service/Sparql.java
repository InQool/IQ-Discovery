/*
 * Sparql.java
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

package com.inqool.dcap.office.service;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.exception.SparqlException;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class Sparql {
    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "fedora.endpoint")
    private String FEDORA_ENDPOINT;

    @Inject
    @ConfigProperty(name = "sparql.endpoint")
    private String SPARQL_ENDPOINT;


    private String sparqlQuery(String query) {
        Client client = ClientBuilder.newClient();

        Response response = client.target(SPARQL_ENDPOINT)
                .request()
                .post(Entity.entity(query, "application/sparql-query"));

        if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            String result = response.readEntity(String.class);
            logger.debug("Sparql Query result: ", result);
            return result;
        } else {
            logger.error("Sparql Query: ", query);
            logger.error("Result: ", response.getStatus());
            throw new SparqlException("Error executing SPARQL Query.");
        }
    }

//    private void sparqlUpdate(String path, String content) {
//        Client client = ClientBuilder.newClient();
//        client.target(FEDORA_ENDPOINT + path).request().method()
//    }
}
