/*
 * MetsParser2.java
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
package com.inqool.dcap.integration.desa2.parser;

import com.inqool.dcap.MarcToDcConverter;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoModel;
import com.inqool.dcap.integration.service.DataStore;
import gov.loc.marc21.slim.CollectionType;
import gov.loc.marc21.slim.RecordType;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Parses Marc XML document and outputs list of RDF models.
 *
 * @author Lukas Jane (inQool)
 */
@ApplicationScoped
public class MarcXmlParser {
    @Inject
    private DataStore store;

    @Inject
    @Zdo
    private Logger logger;

    public List<ZdoModel> parse(final CollectionType collectionType, final String orgShortcut) {
        if(collectionType.getRecords().size() != 1) {
            throw new RuntimeException("Marc collection contained more than one record.");
        }
        RecordType record = collectionType.getRecords().get(0);
        MarcToDcConverter marcToDcConverter = new MarcToDcConverter();
        ZdoModel zdoModel = new ZdoModel(store.createUrl(UUID.randomUUID().toString()));
        marcToDcConverter.convert(record, orgShortcut, zdoModel);
//        String inventoryId = zdoModel.get(ZdoTerms.inventoryId);
        List<ZdoModel> resultList = new ArrayList<>();
        resultList.add(zdoModel);
        return resultList;
    }
}
