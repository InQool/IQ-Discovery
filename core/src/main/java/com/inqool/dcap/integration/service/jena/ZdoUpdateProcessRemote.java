/*
 * ZdoUpdateProcessRemote.java
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

package com.inqool.dcap.integration.service.jena;

import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.modify.UpdateProcessRemote;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.UpdateRequest;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpOp;


/**
 * Overrides default execute method by adding UTF-8 charset at the end of content type.
 *
 * @author Matus Zamborsky (inQool)
 */
@SuppressWarnings("unused")
public class ZdoUpdateProcessRemote extends UpdateProcessRemote {
    public ZdoUpdateProcessRemote(UpdateRequest request, String endpoint, Context context) {
        super(request, endpoint, context);
    }

    public ZdoUpdateProcessRemote(UpdateRequest request, String endpoint, Context context, HttpAuthenticator authenticator) {
        super(request, endpoint, context, authenticator);
    }

    @Override
    public void execute()
    {
        // Validation
        if ( this.getEndpoint() == null )
            throw new ARQException("Null endpoint for remote update") ;
        if ( this.getUpdateRequest() == null )
            throw new ARQException("Null update request for remote update") ;

        // Build endpoint URL
        String endpoint = this.getEndpoint();
        final String querystring = this.getQueryString();
        if (querystring != null && !querystring.equals("")) {
            endpoint = endpoint.contains("?") ? endpoint + "&" + querystring : endpoint + "?" + querystring;
        }

        // Execution
        final String reqStr = this.getUpdateRequest().toString() ;
        HttpOp.execHttpPost(endpoint, WebContent.contentTypeSPARQLUpdate+";charset=UTF-8", reqStr, null, getHttpContext(), getAuthenticator()) ;
    }
}
