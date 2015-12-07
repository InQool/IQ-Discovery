package com.inqool.dcap.integration.z3950.sru.server;/*
package com.inqool.dcap.integration.z3950;

import org.oasis_open.docs.ns.search_ws.diagnostic.DiagnosticComplexType;
import org.z3950.zing.cql.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

*/
/**
* Use to test converters
* @author Lukas Jane (inQool) 2. 12. 2014.
*//*

@Dependent
public class Main {
    @Inject
    private CqlToSolr cqlToSolr;

    public void main(String[] args) {
        try {
            Properties props = new Properties();
            props.setProperty("complexQuery", "0.3");
            props.setProperty("complexClause", "0.2");
            props.setProperty("proxOp", "0.05");
            props.setProperty("equalsRelation", "0.2");
            props.setProperty("numericRelation", "0.05");
            CQLGenerator generator = new CQLGenerator(props);
            while(true) {
                CQLNode root = generator.generate();
                CqlToSparql cqlToSparql = new CqlToSparql();
                String result = cqlToSparql.convert(root.toCQL());
                //CqlToSolr cqlToSolr = new CqlToSolr();
                List<DiagnosticComplexType> diagnostics = new ArrayList<>();
                String result2 = null;
                try {
                    result2 = cqlToSolr.convert(root.toCQL(), diagnostics);
                } catch (CouldNotParseCqlException e) {
                    e.printStackTrace();
                }
                System.out.println(result);
                System.out.println(result2);
                System.out.println("");//put breakpoint here
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CQLParseException e) {
            e.printStackTrace();
        } catch (MissingParameterException e) {
            e.printStackTrace();
        }
    }
}
*/
