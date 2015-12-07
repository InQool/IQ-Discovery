package com.inqool.dcap.integration;

import org.oasis_open.docs.ns.search_ws.sruresponse.RecordDefinition;
import org.oasis_open.docs.ns.search_ws.sruresponse.SearchRetrieveResponseDefinition;
import org.openarchives.oai._2_0.oai_dc.Dc;
import org.openarchives.oai._2_0.oai_dc.DcCollection;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 21. 1. 2015.
 */
@RequestScoped
@Path("/sru-client")
public class SruClient {

    public boolean testSruSource(String sourceUrl) {
        try {
            Response response = ClientBuilder.newClient()
                    .target(sourceUrl + "?operation=explain")
                    .request()
                    .get();
            return response.getStatusInfo()
                            .getFamily()
                            .equals(Response.Status.Family.SUCCESSFUL);
        } catch (Exception e) {
            return false;
        }
    }

    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public DcCollection performSru(@QueryParam("endpoint") String endpoint, @QueryParam("query") String cqlQuery) {
        Response response = ClientBuilder.newClient()
                .target(endpoint + "?operation=searchRetrieve&maximumRecords=10&query="+cqlQuery)
                .request()
                .get();
        System.out.println(response.getStatusInfo()
                        .getFamily()
                        .equals(Response.Status.Family.SUCCESSFUL)
        );
        String responseString = response.readEntity(String.class);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("" +
                            "org.oasis_open.docs.ns.search_ws.sruresponse:" +
                            "org.openarchives.oai._2_0.oai_dc:" +
                            "gov.loc.marc21.slim:" +
                            /*"info.srw.schema._1.dc_schema:" +*/
                            "org.purl.dc.elements._1"
            );
            //Convert MarcXML records to DC records
            responseString = Utils.marcSrwXmlToDcCollectionSrwXml(responseString);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement responseJAXBElement = (JAXBElement) unmarshaller.unmarshal(new StringReader(responseString));
            SearchRetrieveResponseDefinition responseDefinition = (SearchRetrieveResponseDefinition) responseJAXBElement.getValue();
            List<RecordDefinition> recordsList = responseDefinition.getRecords().getRecord();
            DcCollection result = new DcCollection();
            for(RecordDefinition recordDefinition : recordsList) {
                if(/*"info:srw/schema/1/dc-v1.1".equals(recordDefinition.getRecordSchema())
                        && */recordDefinition.getRecordPacking() == null || "xml".equals(recordDefinition.getRecordPacking())) {
                    for(Object obj : recordDefinition.getRecordData().getContent()) {
                        Dc record;
                        if(obj instanceof JAXBElement) {
                            record = (Dc) ((JAXBElement) obj).getValue();
                        }
                        else if (obj instanceof Dc) {
                            record = (Dc) obj;
                        }
                        else {
                            continue;
                        }
                        result.getDcs().add(record);
                        //Do we need to shovel elements elsewhere?
//                    List<JAXBElement<ElementType>> recordFields = record.getTitleOrCreatorOrSubject();
//                    for(JAXBElement<ElementType> recordField : recordFields) {
//
//                    }
                    }
                }
                else if("info:srw/schema/1/marcxml-v1.1".equals(recordDefinition.getRecordSchema()) || "".equals(recordDefinition.getRecordSchema())
                        && "xml".equals(recordDefinition.getRecordPacking())) {
                    //nooooooooooooooooooo
                } else {
                    throw new RuntimeException("Unsupported metadata format received."); //wrong record format received
                }
            }
            return result;
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
}

