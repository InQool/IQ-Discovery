/*
 * Model2.java
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

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.inqool.dcap.integration.Constants;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * Extends basic RDF model from Jena by adding some shortcut methods, which assume that only one subject
 * is used in the model.
 *
 * @author Matus Zamborsky (inQool)
 */
public class ZdoModel extends ModelCom {
    private String url;
    private InputStream content;
    private ZipFile fileToClose;    //If inputstream comes from resource that needs to be closed, it's here, don't forget to close it once inputstream is no longer needed

    public ZdoModel() {
        super(com.hp.hpl.jena.graph.Factory.createGraphMem());
    }

    public ZdoModel(String url) {
        this();

        this.url = url;
    }

    public ZdoModel(String url, InputStream content) {
        this();

        this.url = url;
        this.content = content;
        this.add(ZdoTerms.zdoType, ZdoType.binary.name());
    }

    public ZdoModel(String url, ZdoType type) {
        this();

        this.url = url;
        this.add(ZdoTerms.zdoType, type.name());
    }

    public boolean isIndexable() {
        return this.contains(getSubject(), RDF.type, Constants.INDEXABLE_MIXIN);
    }

    public void setIndexable(boolean indexable) {
        this.removeAll(getSubject(), RDF.type, Constants.INDEXABLE_MIXIN);

        if (indexable) {
            this.add(getSubject(), RDF.type, Constants.INDEXABLE_MIXIN);
        }
    }

    public ZdoModel add(Property property, String value) {
        //We no longer save URLs as resources because Fedora likes to mess with resources, inserting transactions into them or crashing when removing resource that points at other fedora object
        /*if(value != null && value.startsWith("http")) { //Limited support for saving url values as resources
            Resource valueUrl = new ResourceImpl(value);
            return (ZdoModel)this.add(this.getSubject(), property, valueUrl);
        }*/
        return (ZdoModel)this.add(getSubject(), property, value, XSDDatatype.XSDstring);
    }

    public ZdoModel add(Property property, RDFNode value) {
        return (ZdoModel)this.add(getSubject(), property, value);
    }

    public ZdoModel addAll(Property property, List<String> values) {
        values.forEach(value -> this.add(getSubject(), property, value));
        return this;
    }

    public ZdoModel addAllRdfNodes(Property property, List<RDFNode> values) {
        values.forEach(value -> this.add(getSubject(), property, value));
        return this;
    }

    public ZdoModel addURI(Property property, String value) {
        return (ZdoModel)this.add(getSubject(), property, ResourceFactory.createResource(value));
    }

    public String get(Property property) {
        Statement statement = this.getProperty(getSubject(), property);
        if(statement == null) return null;
        RDFNode resultNode = statement.getObject();
        if(resultNode instanceof Literal) return ((Literal) resultNode).getString();
        if(resultNode instanceof Resource) return ((Resource) resultNode).getURI();
        else return resultNode.toString();
    }

    public List<String> getAll(Property property) {
        StmtIterator iter = listStatements( getSubject(), property, (RDFNode) null );
        try {
            List<String> result = new ArrayList<>();
            while (iter.hasNext()) {
                Statement statement = iter.nextStatement();
                RDFNode resultNode = statement.getObject();
                if(resultNode instanceof Literal) result.add(((Literal) resultNode).getString());
                else if(resultNode instanceof Resource) result.add(((Resource) resultNode).getURI());
                else result.add(resultNode.toString());
            }
            return result;
        }
        finally { iter.close(); }
    }

    public String getParent() {
        return get(DCTerms.isPartOf);
    }

    public LocalDate getDate(Property property) {
        String value = this.get(property);

        if (value != null) {
            return LocalDate.parse(value);
        } else {
            return null;
        }
    }

    public Boolean getBoolean(Property property) {
        Statement statement = this.getProperty(this.getSubject(), property);
        return statement != null && statement.getBoolean();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream inputStream) {
        this.content = inputStream;
    }

    public Resource getSubject() {
        return this.createResource(url);
    }

    public void replaceValueOfProperty(Property property, String value) {
        removeAllValuesOfProperty(property);
        this.add(property, value);
    }

    public void removeAllValuesOfProperty(Property property) {
        remove(listStatements(getSubject(), property, (RDFNode) null));
    }

    public void resubjectTo(String newSubject) {
        StmtIterator stmtIterator = this.listStatements();
        List<Statement> statementsToAdd = new ArrayList<>();
        while(stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();
            Statement newStatement = new StatementImpl(new ResourceImpl(newSubject), statement.getPredicate(), statement.getObject());
            stmtIterator.remove();
            statementsToAdd.add(newStatement);
        }
        this.add(statementsToAdd);
    }

    public void clearFedoraInsertedTriplets() {
        StmtIterator stmtIterator = this.listStatements();
        while(stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();
            String propNamepsace = statement.getPredicate().getNameSpace();
            if("http://fedora.info/definitions/v4/repository#".equals(propNamepsace)) {
                stmtIterator.remove();
            }
        }
    }

    public String extractKdrUuid() {
        NodeIterator identifiers = listObjectsOfProperty(DCTerms.identifier);
        while(identifiers.hasNext()) {
            Literal identLiteral = identifiers.next().asLiteral();
            if("http://inqool.cz/zdo/1.0/UUID".equals(identLiteral.getDatatypeURI())) {
                return identLiteral.getString();
            }
        }
        return null;
    }

    public String extractInvId() {
        NodeIterator identifiers = listObjectsOfProperty(DCTerms.identifier);
        while(identifiers.hasNext()) {
            Literal identLiteral = identifiers.next().asLiteral();
            if("http://inqool.cz/zdo/1.0/InvId".equals(identLiteral.getDatatypeURI())) {
                return identLiteral.getString();
            }
        }
        return null;
    }

    public Map<String, Map<String, String>> getModelContentsAsMap() {
        StmtIterator iter = this.listStatements();
        Map<String, Map<String, String>> map = new HashMap<>();
        while(iter.hasNext()) {
            Statement statement = iter.next();
            String predicate = statement.getPredicate().getURI();
            RDFNode valNode = statement.getObject();
            String value;
            if(valNode.isLiteral()) {
                value = valNode.asLiteral().getString();
            }
            else {
                value = valNode.asResource().getURI();
            }
            if(!map.containsKey(statement.getSubject().getURI())) {
                map.put(statement.getSubject().getURI(), new HashMap<>());
            }
            map.get(statement.getSubject().getURI()).put(predicate, value);
        }
        return map;
    }

    public void stripPossibleBadUrlEnding() {
        if(url.contains("?")) { //remove query parameters
            url = url.substring(0, url.indexOf("?"));
        }
        if((url.length() - url.lastIndexOf("/")) < 20 ) {   //if url does not end with UUID, strip the foreign last part
            url = url.substring(0, url.lastIndexOf("/"));
        }
    }

    public void increaseLockCount() {
        this.replaceValueOfProperty(ZdoTerms.lockCount, String.valueOf(Integer.valueOf(this.get(ZdoTerms.lockCount))+1));
    }

    public void decreaseLockCount() {
        int lockCount = Integer.valueOf(this.get(ZdoTerms.lockCount));
        if(lockCount >= 1) {
            this.replaceValueOfProperty(ZdoTerms.lockCount, String.valueOf(--lockCount));
        }
        if(lockCount == 0) {
            this.removeAllValuesOfProperty(ZdoTerms.owner);
        }
    }

    public int getLockCount() {
        return Integer.valueOf(this.get(ZdoTerms.lockCount));
    }

    public ZipFile getFileToClose() {
        return fileToClose;
    }

    public void setFileToClose(ZipFile fileToClose) {
        this.fileToClose = fileToClose;
    }
}
