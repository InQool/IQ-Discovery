package com.inqool.dcap.integration.z3950.server;

import junit.framework.TestCase;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jzkit.search.provider.iface.IRQuery;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.provider.iface.Searchable;
import org.jzkit.search.provider.z3950.Z3950ServiceFactory;
import org.jzkit.search.util.RecordBuilder.RecordBuilder;
import org.jzkit.search.util.RecordBuilder.RecordBuilderException;
import org.jzkit.search.util.RecordBuilder.RecordBuilderService;
import org.jzkit.search.util.RecordBuilder.iso2709RecordFactory;
import org.jzkit.search.util.RecordModel.ArchetypeRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.InformationFragmentImpl;
import org.jzkit.search.util.ResultSet.IRResultSet;
import org.jzkit.search.util.ResultSet.IRResultSetException;
import org.jzkit.search.util.ResultSet.IRResultSetStatus;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Lukas Jane (inQool) 16. 1. 2015.
 */
@ApplicationScoped
@Singleton
@Startup
public class ServerTester extends TestCase {

    @Inject
    private Logger LOG;

    @PostConstruct
    public void testZ3950Server() throws SearchException, IRResultSetException, RecordBuilderException, IOException {

        iso2709RecordFactory isoFac = new iso2709RecordFactory();
        String src = "Příliš žluťoučký kůň úpěl ďábelské ódy.";

        //Document x = isoFac.getCanonicalXML(bytes);

        ApplicationContext app_context = new ClassPathXmlApplicationContext( "Z3950ServerTestContext.xml" );
        System.err.println("Setting up Z3950 factory");
        Z3950ServiceFactory factory = new Z3950ServiceFactory("localhost",9999);
        factory.setApplicationContext(app_context);
        factory.setDefaultRecordSyntax("xml");
        factory.setDefaultRecordSchema("solr");
        factory.setDefaultElementSetName("F");
//    factory.getRecordArchetypes().put("Default","xml::F");
        factory.getRecordArchetypes().put("Default","iso2709:marc21:F");
        factory.getRecordArchetypes().put("FullDisplay","xml::F");
        factory.getRecordArchetypes().put("BriefDisplay","xml::B");
        factory.getRecordArchetypes().put("Holdings","xml::F");

        System.err.println("Build IR Query");
        IRQuery query = new IRQuery();
        query.collections = new Vector();
        // query.collections.add("Default");
        query.collections.add("Test:one");
        query.query = new org.jzkit.search.util.QueryModel.PrefixString.PrefixString("@attrset bib-1 @attr 1=4 \"bitch\"");

        System.err.println("Obtain instance from factory");
        Searchable s = factory.newSearchable();

        System.err.println("Evaluate query...");
        IRResultSet result = s.evaluate(query);

        System.err.println("Waiting for result set to complete, current status = "+result.getStatus());
        // Wait without timeout until result set is complete or failure
        result.waitForStatus(IRResultSetStatus.COMPLETE|IRResultSetStatus.FAILURE,0);

        System.err.println("Iterate over results (status="+result.getStatus()+"), count="+result.getFragmentCount());

        // Enumeration e = new org.jzkit.search.util.ResultSet.ReadAheadEnumeration(result);
        Enumeration e = new org.jzkit.search.util.ResultSet.ReadAheadEnumeration(result, new ArchetypeRecordFormatSpecification("Default"));

        for ( int i=0; ( ( e.hasMoreElements() ) && ( i < 10 ) ); i++) {
            System.err.println("Processing z3950 server result "+i);
            Object o = e.nextElement();
            System.err.println(o);
            InformationFragmentImpl resultRecord = (InformationFragmentImpl) o;
            RecordBuilderService recordBuilderService = new RecordBuilderService();
            RecordBuilder rb = new iso2709RecordFactory();
            Document transformedDocument = rb.getCanonicalXML(resultRecord.getOriginalObject());
            //Document transformedDocument = recordBuilderService.getCanonicalXML(resultRecord);
            dumpResponseRecord(transformedDocument);
        }

        System.err.println("All done - Z3950 Server Unit Test");

        result.close();
        s.close();
    }

    private static void dumpResponseRecord(Document d) throws IOException {
       // log.debug("dumpResponseRecord");
        OutputFormat format  = new OutputFormat( "xml","utf-8",false );
        format.setOmitXMLDeclaration(true);
        java.io.StringWriter  stringOut = new java.io.StringWriter();
        XMLSerializer serial = new XMLSerializer( stringOut,format );
        serial.setNamespaces(true);
        serial.asDOMSerializer();
        serial.serialize( d.getDocumentElement() );
        System.out.println(stringOut.toString());
       // log.debug("Result: "+stringOut.toString());
    }

}
