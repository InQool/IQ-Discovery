package org.jzkit.search.provider.solr.test;

import junit.framework.TestCase;
import org.jzkit.search.provider.iface.IRQuery;
import org.jzkit.search.provider.iface.IRServiceDescriptor;
import org.jzkit.search.provider.iface.Searchable;
import org.jzkit.search.provider.iface.SearchableFactory;
import org.jzkit.search.util.ResultSet.IRResultSet;
import org.jzkit.search.util.ResultSet.IRResultSetStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Enumeration;

public class SOLRTest extends TestCase {

  private static ApplicationContext app_context = null;

  public SOLRTest(String name) {
    super (name);

  }
  
  public static void main(String[] args) {
  }

  public void testSOLR() throws Exception {
    app_context = new ClassPathXmlApplicationContext( "TestApplicationContext.xml" );
    SearchableFactory sf = (org.jzkit.search.provider.iface.SearchableFactory) app_context.getBean("SearchableFactory");

    IRServiceDescriptor descriptor = new IRServiceDescriptor("proto=SOLR,code=LL-solr,baseURL=http://localhost:8180/solr/new_core/select,shortname=SOLR,longname=LL SOLR Test,defaultRecordSyntax=usmarc,defaultElementSetName=F,recordArchetypes(Default)=solr::F,QueryType=SOLR-STD,fieldList(F)='id,title',fieldList(B)='id,title',fieldList(Default)='id,title'");

    Searchable s = sf.create(descriptor);

    if ( s != null ) {
      org.jzkit.search.util.QueryModel.CQLString.CQLString qm = new org.jzkit.search.util.QueryModel.CQLString.CQLString("dc.title=revue or dc.author = dick");

      // Create a query
      IRQuery query = new IRQuery(qm,"Default");

      IRResultSet result = s.evaluate(query);
      result.waitForStatus(IRResultSetStatus.COMPLETE|IRResultSetStatus.FAILURE,0);
      Enumeration e = new org.jzkit.search.util.ResultSet.ReadAheadEnumeration(result, null /*new ArchetypeRecordFormatSpecification("Default")*/);
      for ( int i=0; ( ( e.hasMoreElements() ) && ( i < 20 ) ); i++) {
        Object o = e.nextElement();
        System.err.println(o);
      }
    }
    else {
      System.err.println("No search created by factory");
    }
  }
}
