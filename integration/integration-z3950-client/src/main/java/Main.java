import com.inqool.dcap.integration.z3950.client.Searcher;
import com.inqool.dcap.integration.Utils;
import gov.loc.marc21.slim.CollectionType;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.util.ResultSet.IRResultSetException;
import org.openarchives.oai._2_0.oai_dc.DcCollection;

/**
 * @author Matus Zamborsky (inQool)
 * @author Lukas Jane (inQool)
 */
public class Main {
    public static void main(String[] args) throws IRResultSetException, SearchException {

//        CQLParser parser = new CQLParser();
//        try {
//            CQLNode cqlNode = parser.parse("utah");
//            String pqf = cqlNode.toPQF(new Properties());
//            System.out.println(pqf);
//        } catch (CQLParseException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (PQFTranslationException e) {
//            e.printStackTrace();
//        }

        Searcher searcher = new Searcher();
        String result;
/*
        searcher.init("localhost", 9999, "Default", "UTF-8");
        String result = searcher.search("dc.title = bitch");
        System.out.println(result);*/

/*        searcher.init("aleph.mzk.cz", 9991, "MZK01-UTF", "UTF-8");
        //String result = searcher.search("@attrset bib-1 @attr 1=4 \"utah\"");
        result = searcher.search("dc.title = morava");*/

        searcher.init("131.117.208.130", 210, "Katalog", "CP1250");
        //String result = searcher.search("@attrset bib-1 @attr 1=4 \"utah\"");
        result = searcher.search("komensky");

        System.out.println(result);

        CollectionType marcXmlCollection = Utils.unmarshallMarcXml(result);

        String dcXmlCollection = Utils.marcXmlToDcCollectionXml(result);

        DcCollection dcCollection = Utils.unmarshallDcCollectionXml(dcXmlCollection);
        System.out.println("cdrap");
    }
}
