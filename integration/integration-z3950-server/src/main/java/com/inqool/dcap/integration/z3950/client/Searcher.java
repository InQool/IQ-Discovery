package com.inqool.dcap.integration.z3950.client;

import com.inqool.dcap.config.Zdo;
import org.jzkit.search.provider.iface.IRQuery;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.provider.iface.Searchable;
import org.jzkit.search.provider.z3950.Z3950ServiceFactory;
import org.jzkit.search.util.QueryModel.CQLString.CQLString;
import org.jzkit.search.util.RecordModel.InformationFragment;
import org.jzkit.search.util.ResultSet.IRResultSet;
import org.jzkit.search.util.ResultSet.IRResultSetException;
import org.jzkit.search.util.ResultSet.IRResultSetStatus;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

/**
 * @author Lukas Jane (inQool) 5. 1. 2015.
 */
@Dependent
public class Searcher {

    @Inject
    @Zdo
    private Logger logger;

    private String server;
    private int port;
    private String database;
    private String encoding;

    public void init(String server, int port, String database, String encoding) {
        this.server = server;
        this.port = port;
        this.database = database;
        this.encoding = encoding;
    }

    public String search(String cqlQuery) throws SearchException, IRResultSetException {
        if(server == null) {
            throw new RuntimeException("Must run init before search");
        }
        ApplicationContext ctx = new ClassPathXmlApplicationContext("TestApplicationContext.xml");

        //logger.debug("Setting up Z3950 factory");
        Z3950ServiceFactory factory = new Z3950ServiceFactory(server, port);
        factory.setApplicationContext(ctx);
        factory.setDefaultRecordSyntax("usmarc");
        factory.setDefaultElementSetName("F");

        Searchable s = factory.newSearchable();

        //IRQuery query = new IRQuery(new PrefixString(prefixQuery), database);
        IRQuery query = new IRQuery(new CQLString(cqlQuery), database);
        IRResultSet result = s.evaluate(query);
        result.waitForStatus(IRResultSetStatus.COMPLETE|IRResultSetStatus.FAILURE,0);

        System.err.println("Iterate over results (status="+result.getStatus()+"), count="+result.getFragmentCount());

        Enumeration en = new org.jzkit.search.util.ResultSet.ReadAheadEnumeration(result);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final MarcWriter writer = new MarcXmlWriter(baos, true);

/*        if(!"UTF-8".equals(encoding)) {
            final AnselToUnicode converter = new AnselToUnicode();
            writer.setConverter(converter);
        }*/

        for ( int i=0; ( ( en.hasMoreElements() ) && ( i < 60 ) ); i++) {
            System.err.println("Processing z3950 result "+i);
            Object o = en.nextElement();
            InformationFragment f = (InformationFragment)o;

            MarcReader reader = new MarcStreamReader(new ByteArrayInputStream((byte[]) f.getOriginalObject()), encoding);
            while (reader.hasNext()) {
                final Record record = reader.next();
                writer.write(record);
            }
        }
        writer.close();
        System.err.println("All done - testLOC()");

        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
