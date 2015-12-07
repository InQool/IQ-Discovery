package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.core.NkListsAccess;
import com.inqool.dcap.office.api.entity.nklists.ChronologicalAuthority;
import com.inqool.dcap.office.api.entity.nklists.GenreAuthority;
import com.inqool.dcap.office.api.entity.nklists.GeographicalAuthority;
import com.inqool.dcap.office.api.entity.nklists.TopicAuthority;
import com.inqool.dcap.security.ZdoRoles;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */

@Path("/loadNkListsToDb")
@RequestScoped
public class NkListsDbLoaderRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    @ConfigProperty(name = "nk.lists.path")
    private String NK_LISTS_PATH;

    @Inject
    private NkListsAccess nkListsAccess;

    @Path("/")
    @POST
    @RolesAllowed(ZdoRoles.ADMIN_SYS)
    public Response loadNkListsToDb() {
        return loadNkListsToDbInner();
    }

    public Response loadNkListsToDbInner() {
        try {
            nkListsAccess.wipeAuthorities();

            SAXParserFactory factory = SAXParserFactory.newInstance();

            logger.info("Parsing geographical autorities");
            List<Object> authoritiesList = new ArrayList<>();
            NkAuthoritiesSaxParserHandler parserHandler = new NkAuthoritiesSaxParserHandler(GeographicalAuthority.class, authoritiesList);
            factory.newSAXParser().parse(new File(NK_LISTS_PATH + "aut_ge.xml"), parserHandler);
            authoritiesList.add(new GeographicalAuthority("Neznámé", ""));
            nkListsAccess.saveAuthorityList(authoritiesList);

            logger.info("Parsing chronological autorities");
            authoritiesList.clear();
            parserHandler = new NkAuthoritiesSaxParserHandler(ChronologicalAuthority.class, authoritiesList);
            factory.newSAXParser().parse(new File(NK_LISTS_PATH + "aut_ch.xml"), parserHandler);
            authoritiesList.add(new ChronologicalAuthority("Neznámé", ""));
            nkListsAccess.saveAuthorityList(authoritiesList);

            logger.info("Parsing genre autorities");
            authoritiesList.clear();
            parserHandler = new NkAuthoritiesSaxParserHandler(GenreAuthority.class, authoritiesList);
            factory.newSAXParser().parse(new File(NK_LISTS_PATH + "aut_fd.xml"), parserHandler);
            authoritiesList.add(new GenreAuthority("Neznámý", ""));
            nkListsAccess.saveAuthorityList(authoritiesList);

            logger.info("Parsing topic autorities");
            authoritiesList.clear();
            parserHandler = new NkAuthoritiesSaxParserHandler(TopicAuthority.class, authoritiesList);
            factory.newSAXParser().parse(new File(NK_LISTS_PATH + "aut_ph.xml"), parserHandler);
            authoritiesList.add(new TopicAuthority("Neznámé", ""));
            nkListsAccess.saveAuthorityList(authoritiesList);

            logger.info("Authorities loaded.");
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed loading NK lists.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    //Handler for SAX parsing of the xml file
    class NkAuthoritiesSaxParserHandler extends DefaultHandler {
        List<Object> resultList;
        Class clazz;
        String tagToGoFor;

        boolean insideRightDatafield = false;
        boolean insideNameDataSubfield = false;
        boolean insideCodeDataSubfield = false;

        String name = "";
        String code = "";

        public NkAuthoritiesSaxParserHandler(Class clazz, List<Object> resultList) {
            this.resultList = resultList;
            this.clazz = clazz;
            if(clazz.equals(GeographicalAuthority.class)) {
                this.tagToGoFor = "151";
            } else if(clazz.equals(ChronologicalAuthority.class)) {
                this.tagToGoFor = "148";
            } else if(clazz.equals(TopicAuthority.class)) {
                this.tagToGoFor = "150";
            } else if(clazz.equals(GenreAuthority.class)) {
                this.tagToGoFor = "155";
            }
        }

        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            //We enter datafield
            if("datafield".equals(qName) && tagToGoFor.equals(attributes.getValue("tag"))) {
                insideRightDatafield = true;
                return;
            }
            //We enter subfield of datafield
            if(insideRightDatafield) {
                if("subfield".equals(qName)) {
                    if("a".equals(attributes.getValue("code"))) {
                        insideNameDataSubfield = true;
                    }
                    else if("7".equals(attributes.getValue("code"))) {
                        insideCodeDataSubfield = true;
                    }
                }
            }
        }

        public void endElement(String uri, String localName,
                               String qName) throws SAXException {
            //We jump out of the field
            if(insideRightDatafield) {
                if ("datafield".equals(qName)) {
                    insideRightDatafield = false;
                    if("".equals(name) || "".equals(code)) {
                        logger.debug("Some record didn't contain required data.");
                        return;
                    }
                    //Store found data
                    if(clazz.equals(GeographicalAuthority.class)) {
                        resultList.add(new GeographicalAuthority(name, code));
                    } else if(clazz.equals(ChronologicalAuthority.class)) {
                        resultList.add(new ChronologicalAuthority(name, code));
                    } else if(clazz.equals(TopicAuthority.class)) {
                        resultList.add(new TopicAuthority(name, code));
                    } else if(clazz.equals(GenreAuthority.class)) {
                        resultList.add(new GenreAuthority(name, code));
                    }
                    name = "";
                    code = "";
                    return;
                }
                //We jump out of the subfield
                if (insideRightDatafield) {
                    if ("subfield".equals(qName)) {
                        insideNameDataSubfield = false;
                        insideCodeDataSubfield = false;
                    }
                }
            }
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if(insideRightDatafield && insideNameDataSubfield) {
                name += new String(ch, start, length);
            }

            if(insideRightDatafield && insideCodeDataSubfield) {
                code += new String(ch, start, length);
            }
        }
    }
}
