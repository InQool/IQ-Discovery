/*
 * Constants.java
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
package com.inqool.dcap.integration;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class Constants {
    public static final String REPOSITORY_NAMESPACE = "http://fedora.info/definitions/v4/repository#";
    public static final String INDEXER_NAMESPACE = "http://fedora.info/definitions/v4/indexing#";

    /*
        Replaces all non-legal java identifier letters, so the HornetQ JMS do not make problems.
     */
    public static final String JMS_NAMESPACE = "org.fcrepo.jms.";
    public static final String JMS_NAMESPACE_SAFE = JMS_NAMESPACE
            .replace("://", "_")
            .replace("/", "_")
            .replace(".", "_")
            .replace("#", "_");

    public static final String IDENTIFIER_HEADER_NAME = JMS_NAMESPACE_SAFE + "identifier";
    public static final String PROPERTIES_HEADER_NAME = JMS_NAMESPACE_SAFE + "properties";
    public static final String BASE_URL_HEADER_NAME = JMS_NAMESPACE_SAFE + "baseURL";
    public static final String EVENT_TYPE_HEADER_NAME = JMS_NAMESPACE_SAFE + "eventType";

    public static final Resource INDEXABLE_MIXIN = ResourceFactory.createResource(INDEXER_NAMESPACE + "indexable");

    public static final String FEDORA_EVENT_REMOVED = REPOSITORY_NAMESPACE + "NODE_REMOVED";

    public static final String IP_SRC_ID_HEADER_NAME = "sourceId";
}
