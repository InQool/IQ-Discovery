/*
 * ZdoTerms.java
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

package com.inqool.dcap.integration.model;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

import java.util.Arrays;
import java.util.List;

public class ZdoTerms {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static final Model m_model = ModelFactory.createDefaultModel();

    public static final String ns = "http://inqool.cz/zdo/1.0/";

    /** <p>A name given to the resource.</p> */
    public static final Property zdoType = m_model.createProperty( "http://inqool.cz/zdo/1.0/zdoType" );

    /** <p>A name given to the resource.</p> */
    public static final Property group = m_model.createProperty( "http://inqool.cz/zdo/1.0/group" );

    /** <p>A name given to the resource.</p> */
    public static final Property fileType = m_model.createProperty( "http://inqool.cz/zdo/1.0/fileType" );

    /** <p>A name given to the resource.</p> */
    public static final Property source = m_model.createProperty( "http://inqool.cz/zdo/1.0/source" );

    /** <p>A name given to the resource.</p> */
    public static final Property organization = m_model.createProperty( "http://inqool.cz/zdo/1.0/organization" );

    /** <p>A name given to the resource.</p> */
    public static final Property owner = m_model.createProperty( "http://inqool.cz/zdo/1.0/owner" );
    /** <p>A name given to the resource.</p> */
    public static final Property lockCount = m_model.createProperty( "http://inqool.cz/zdo/1.0/lockCount" );

    /** <p>A name given to the resource.</p> */
    public static final Property published = m_model.createProperty( "http://inqool.cz/zdo/1.0/published" );

    /** <p>A name given to the resource.</p> */
    public static final Property publishFrom = m_model.createProperty( "http://inqool.cz/zdo/1.0/publishFrom" );

    /** <p>A name given to the resource.</p> */
    public static final Property publishTo = m_model.createProperty( "http://inqool.cz/zdo/1.0/publishTo" );

    /** <p>A name given to the resource.</p> */
    public static final Property inventoryId = m_model.createProperty( "http://inqool.cz/zdo/1.0/inventoryId" );

    public static final Property validToPublish = m_model.createProperty( "http://inqool.cz/zdo/1.0/validToPublish" );

//    /** <p>A name given to the resource.</p> */
    public static final Property batchId = m_model.createProperty( "http://inqool.cz/zdo/1.0/batchId" );
    public static final Property kdrObject = m_model.createProperty( "http://inqool.cz/zdo/1.0/kdrObject" );
    public static final Property newestPublished = m_model.createProperty( "http://inqool.cz/zdo/1.0/newestPublished" );
    public static final Property mimeType = m_model.createProperty( "http://fedora.info/definitions/v4/repository#mimeType" );
    public static final Property documentType = m_model.createProperty( "http://inqool.cz/zdo/1.0/documentType" );
    public static final Property documentSubType = m_model.createProperty( "http://inqool.cz/zdo/1.0/documentSubType" );

    public static final Property allowContentPublicly = m_model.createProperty( "http://inqool.cz/zdo/1.0/allowContentPublicly" );
    public static final Property allowPdfExport = m_model.createProperty( "http://inqool.cz/zdo/1.0/allowPdfExport" );
    public static final Property allowEpubExport = m_model.createProperty( "http://inqool.cz/zdo/1.0/allowEpubExport" );
    public static final Property pdfUrl = m_model.createProperty( "http://inqool.cz/zdo/1.0/pdfUrl" );
    public static final Property epubUrl = m_model.createProperty( "http://inqool.cz/zdo/1.0/epubUrl" );
    public static final Property datePublished = m_model.createProperty( "http://inqool.cz/zdo/1.0/datePublished" );


    public static final Property watermark = m_model.createProperty( "http://inqool.cz/zdo/1.0/watermark" );
    public static final Property watermarkPosition = m_model.createProperty( "http://inqool.cz/zdo/1.0/watermarkPosition" );
    public static final Property imgThumb = m_model.createProperty( "http://inqool.cz/zdo/1.0/imgThumb" );
    public static final Property imgNormal = m_model.createProperty( "http://inqool.cz/zdo/1.0/imgNormal" );
    public static final Property pageIndex = m_model.createProperty( "http://inqool.cz/zdo/1.0/pageIndex" );

    public static final Property additionalMetadata = m_model.createProperty( "http://inqool.cz/zdo/1.0/additionalMetadata" );

    public static final Property oaiIdentifier = m_model.createProperty( "http://inqool.cz/zdo/1.0/oaiIdentifier" );

    //Helper property used to mark spine pages so that their thumbnail is not added as a thumbnail of whole book
    public static final Property cannotBeCoverPage = m_model.createProperty( "http://inqool.cz/zdo/1.0/cannotBeCoverPage" );

    //Sck defines what to display publicly in KDR already, we store it here and derive other info when needed (allowContentPublicly, allowPdfExport, autopublish)
    public static final Property publishHint = m_model.createProperty( "http://inqool.cz/zdo/1.0/publishHint" );

    //These are not of Zdo, but for convenience they are also placed here
    public static final Property fedoraCreated = m_model.createProperty( "http://fedora.info/definitions/v4/repository#created" );
    public static final Property fedoraLastModified = m_model.createProperty( "http://fedora.info/definitions/v4/repository#lastModified" );

    public static final String stringType = "^^<http://www.w3.org/2001/XMLSchema#string>";

    public static String stringConstantOf(String value) {
        return "\"" + value + "\"" + stringType;
    }

    private static final List<String> zdoPropNames = Arrays.asList(
            "inventoryId",  //todo fill
            "documentType",
            "documentSubType",
            "allowContentPublicly",
            "allowPdfExport",
            "allowEpubExport",
            "watermark",
            "watermarkPosition",
            "imgThumb",
            "imgNormal",
            "publishFrom",
            "publishTo"
    );

    public static List<String> getZdoPropNames() {
        return zdoPropNames;
    }
}
