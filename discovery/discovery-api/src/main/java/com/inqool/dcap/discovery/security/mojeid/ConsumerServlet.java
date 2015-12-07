package com.inqool.dcap.discovery.security.mojeid;

import com.inqool.dcap.discovery.api.core.DiscoveryUserAccess;
import com.inqool.dcap.discovery.security.mojeid.data.CustomerRequest;
import com.inqool.dcap.discovery.security.mojeid.util.OpenIdHelper;
import com.inqool.dcap.discovery.security.mojeid.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.openid4java.OpenIDException;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.*;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;


/**
 * Servlet vyuzivajici knihovnu OpenId4Java pro realizaci prihlaseni pomoci standardu OpenID.
 * (Servlet je napsan pro sluzbu mojeID, lze ho vsak snadno upravit i pro jine implementace OpenID.)
 *
 * @author ACTIVE24, s.r.o.
 */
@WebServlet(value = "/consumer", name = "ConsumerServlet")
public class ConsumerServlet extends javax.servlet.http.HttpServlet {

    @Inject
    private DiscoveryUserAccess discoveryUserAccess;

    @Inject
    @ConfigProperty(name = "mojeid.realm")
    private String REALM;
//    private final static String REALM = "http://localhost:8080/dcap/discovery/";									//realm - oblast URL ve které musí ležet URL, na které poslouchá servlet. Dale na teto URL je vystaven xrds.xml dokument. Navratova URL (returnToUrl), musi lezet v tomto realm. napr. http://www.vasserver.cz/OpenIdClientSample/
//    private final static String REALM = "http://inqool.cz:8084/dcap/discovery/";									//realm - oblast URL ve které musí ležet URL, na které poslouchá servlet. Dale na teto URL je vystaven xrds.xml dokument. Navratova URL (returnToUrl), musi lezet v tomto realm. napr. http://www.vasserver.cz/OpenIdClientSample/
//    private final static String REALM = "https://zdo.inqool.cz/";									//realm - oblast URL ve které musí ležet URL, na které poslouchá servlet. Dale na teto URL je vystaven xrds.xml dokument. Navratova URL (returnToUrl), musi lezet v tomto realm. napr. http://www.vasserver.cz/OpenIdClientSample/
    //returnUrl, ktera musi lezet v tomto realm je nutno nastavit take v souboru WEB-INF/xrds.xml

    //provozni konstanty pro mojeID
//    	private final static String DEFAULT_MOJEID_ENDPOINT = "http://mojeid.fred.nic.cz/endpoint/";	//koncovy bod poskytovatele OpenID (test)
    private final static String DEFAULT_MOJEID_ENDPOINT = "https://mojeid.cz/endpoint/";    //koncovy bod poskytovatele OpenID


    //=============== ATRIBUTY ==================================================
    private final static long serialVersionUID = 6825667150249962740L;
    private final static Log log = LogFactory.getLog(ConsumerServlet.class);

    private final static String OPENID_DISC_INFO = "openid-disc";                    //klic pod kterym je ulozen objekt DiscoveryInformation v session
    private final static String HC_PARAM_NAME = "hc";                                //nazev URL parametru, ve kterem je predavan hash kod, pod kterym je v session ulozena prepravka CustomerRequest se vstupnimi daty

    //nazvy atributu ziskanych z mojeid
    public final static String MOJE_ID = "mojeId";
    public final static String FIRST_NAME = "firstName";
    public final static String SURNAME = "surname";
    public final static String STREET = "street";
    public final static String CITY = "city";
    public final static String POSTAL_CODE = "postalCode";
    public final static String COUNTRY = "country";
    public final static String PHONE = "phone";
    public final static String EMAIL_PRIMARY = "emailPrimary";
    public final static String COMPANY_NAME = "companyName";
    public final static String ICO = "ico";
    public final static String VAT = "vat";
    public final static String VALID = "valid";
    public final static String OP_NUMBER = "opNumber";
    public final static String STATUS = "status";

    private ConsumerManager manager;                                                        //consumer manager z openid4java


    //============== KOSTRUKTORY A TOVARNI METODY ===============================

    //============== METODY TRIDY ===============================================

    //============== VEREJNE METODY INSTANCE ====================================
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // --- nastaveni proxy (pokud je potreba) ---
        ProxyProperties proxyProps = getProxyProperties(config);
        if (proxyProps != null) {
            log.debug("ProxyProperties: " + proxyProps);
            HttpClientFactory.setProxyProperties(proxyProps);
        }

        try {
            this.manager = OpenIdHelper.newConsumerManager();
        } catch (Exception e) {
            throw new RuntimeException("Helper for creating openid Consumermanager Failed", e);
        }
//		this.manager = new ConsumerManager();
        manager.setAssociations(new InMemoryConsumerAssociationStore());
        manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
        manager.setMinAssocSessEnc(AssociationSessionType.DH_SHA256);

        log.debug(ConsumerServlet.class.getName() + " initialization ok");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Pri zpracovani mohou nastat dve situace:

        //A. ZPRACOVANI ODPOVEDI OD OPENID PROVIDERA.
        //   Pokud se v URL nachazi parametr is_return=true, pozadavek na servlet prichazi od poskytovatele OpenID tj. od serveru mojeID.
        if ("true".equals(req.getParameter("is_return"))) {
            try {
                log.debug("performing the response from OpenId authentication server ...");
                processReturn(req, resp);

            } catch (Exception e) {
                //presmerujeme na chybovou stranku a zalogujeme
                this.redirectToErrorPage("chyba pri zpracovani odpovedi po autentizaci na mojeID serveru",
                        "Error during performing of the OpenID/mojeID server response after authentification",
                        e, req, resp);
                return;
            }


            //B. INICIACE PROCESU PRIHLASENI, VYTVORI POZADAVEK NA OPENID POSKYTOVATELE (na mojeID server).
            //   Servlet navaze spojeni s mojeID serverem a provede presmerovani uzivatele na
            //	 autentizacni stranku mojeID.
        } else {
            String inputParHashCode = null;
            try {
                log.debug("start performing login and standard pairing request ...");
                //nacteni parametru z URL do prepravky
                CustomerRequest inputData = this.readInputParams(req);
                //ulozeni prepravky s nactenymi parametry do session pod vygenerovanym klicem
                inputParHashCode = this.saveInputParamsToSession(inputData, req.getSession());

            } catch (MissingParamException e) {
                //presmerujeme na chybovou stranku a zalogujeme
                this.redirectToErrorPage("chyba pri zpracovani pozadavku na prihlaseni pres mojeID, strance nebyly predany vsechny pozadovane parametry",
                        "Error during OpenID authentification - standard pairing",
                        e, req, resp);
                return;
            } catch (Exception e) {
                //presmerujeme na chybovou stranku a zalogujeme
                this.redirectToErrorPage("obecna chyba pri zpracovani pozadavku na prihlaseni pres mojeID",
                        "Error during OpenID authentification - standard pairing",
                        e, req, resp);
                return;
            }

            //Sestaveni navratove URL adresy, ktera bude zpracovavat odpoved od OpenID poskytovatele (od mojeID serveru).
            //Tato url musi lezet ve zvolenem realm. Jde o adresu na tento servlet,
            //URL adresa obsahuje parametr is_return=true, tak aby doslo ke zpracovani pozadavku podle bodu A.
            //Dale obsahuje klic, pod kterym je v session ulozena prepravka se vstupnimi parametry.
            //Tato adresa je mojeID serverem kontrolovana proti hodnote v tagu URI v xrds.xml dokumentu, proto se musi
            //shodovat (na parametrech nezalezi).
            String returnToUrl = new UrlBuilder(REALM + "/dcap/discovery/consumer")
                    .appendParam("is_return", "true")
                    .appendParam(HC_PARAM_NAME, inputParHashCode)
                    .build();

            //vytvoreni pozadavku na prihlaseni pomoci knihovny openid4java
            this.authOpenIdRequest(req, resp, returnToUrl);
        }
    }


    //============== SOUKROME METODY INSTANCE ===================================

    /**
     * Ulozi vstupni data v podobe prepravky customerRequest do session pod vygenerovanym hash
     * klicem, ktery tato metoda vraci.
     *
     * @param customerRequest
     * @return klic pod kterym jsou v session ulozeny vstupni data(cela prepravka)
     */
    private String saveInputParamsToSession(CustomerRequest customerRequest, HttpSession session) {
        //vygenerovani hash kodu, ktery bude identifikovat vstupni data ulozena v session
        String inputParHashCode = String.valueOf(customerRequest.hashCode());

        //ulozeni parametru do session (CustomerRequest)
        session.setAttribute(inputParHashCode, customerRequest);
        log.debug("Input data saved to session");

        return inputParHashCode;
    }


    /**
     * Nacte parametry predane frontend aplikaci z URL do prepravky.
     *
     * @param req
     * @return vraci prepravku s naplnenymi parametry
     * @throws MissingParamException pokud nektery z parametru chybi
     */
    private CustomerRequest readInputParams(HttpServletRequest req) throws MissingParamException {
        String frontendUrl = req.getParameter("frontendUrl");

        //pokud nejsou vsechny pozadovane parametry vyplnene vraci null;
        if (frontendUrl == null) {
            log.error("some of URL values is null:" + "frontendUrl=" + frontendUrl);
            throw new MissingParamException("some of URL values is null:" + "frontendUrl=" + frontendUrl);
        }

        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setFrontendUrl(frontendUrl);                //puvodni url, na kterou mel byt uzivatel navracen
        return customerRequest;
    }


    /**
     * Zpracovani odpovedi od mojeid serveru.
     *
     * @param req
     * @param resp
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     * @throws OpenIDException
     */
    private void processReturn(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, OpenIDException {
        //zpracovani odpovedi od mojeid serveru + overeni jeji pravosti
        VerificationResult verificationResult = this.verifyResponse(req, resp);

        //OVERENI, ZE PRIHLASENI PROBEHLO USPESNE
        //pokud ne je uzivatel presmerovan na chybovou stranku
        if (verificationResult == null || verificationResult.getVerifiedId() == null) {
            //neuspesne prihlaseni, presmerovavame na chybovou stranku
            redirectToFailure(req, resp, null);
            return;            //----------------------------------------------------------------------------
        }

        //USPESNE PRIHLASENI, pokracujeme kdyz prihlaseni probehlo uspesne
        //ziskani mojeid identifier
        final String mojeidIdentifier = verificationResult.getVerifiedId().getIdentifier();                        //prohlaseny a jedinecny identifikator mojeid uzivatele
        log.debug("OpenID authorization successful, claimed identifier: " + mojeidIdentifier);

        //ziskani puvodnich vstupnich parametru ze session, parametry z puvodniho webu, ze ktereho uzivatel prisel se prihlasit
        final CustomerRequest customerRequest = (CustomerRequest) req.getSession().getAttribute(req.getParameter(HC_PARAM_NAME));                                        //pro bezne parovani

        //ziskani osobnich udaju uzivatele z uctu mojeid jako asociativni pole (mapa hodnot)
        Map<String, String> attributes = this.extractMojeidData(verificationResult);


        //ZPRACOVANI po autentizaci uzivatele
        // -----------------------------------------------------------------------
        // Pro standardni parovani a dalsi mozne nasledne kroky (prihlaseni uid v backendu, synchronizace)
        if (customerRequest != null) {
            //vykonani akci po uspesne autentizaci pres mojeID, treba sparovani mojeID uctu s firemnim uctem
            /*this.doSomething();*/
            //Login user by mojeId to database
            Map<String, String> returnData = discoveryUserAccess.mojeIdLogin(attributes);

            if(returnData.size() == 1) {
                redirectToFailure(req, resp, returnData);
            }

            //presmerovani na puvodni web odkud uzivatel prisel, POSTem odesilame ziskane udaje z mojeID
            log.debug("mojeID authentication successful, Customer will be redirected to url: " + customerRequest.getFrontendUrl());

/*            resp.sendRedirect("http://www.google.com");
            return;*/

            /*RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
			req.setAttribute("targetURL", customerRequest.getFrontendUrl() );
			req.setAttribute("message", returnData);*/
            /*dispatcher.forward(req, resp);*/

            //Redirect user's browser back to frontend, by an auto-submitting form
            resp.setCharacterEncoding("UTF-8");
            String redirectHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "\n" +
                    "            <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                    "            <html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "            <head>\n" +
                    "            <title>OpenID HTML FORM Redirection</title>\n" +
                    "            <meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml; charset=UTF-8\" />\n" +
                    "            </head>\n" +
                    "            <body onload=\"document.getElementById('openid-form-redirection').submit();\">\n" +
                    "            <form id=\"openid-form-redirection\" name=\"openid-form-redirection\" action=\"";
            redirectHtml += customerRequest.getFrontendUrl();
            redirectHtml += "\" method=\"GET\" accept-charset=\"utf-8\">\n";
            for (Map.Entry<String, String> entry : returnData.entrySet()) {
                if(entry.getValue() == null) {
                    continue;
                }
                redirectHtml += "<input type=\"hidden\" name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>\n";
            }
            redirectHtml += "            <noscript>\n" +
                    "                    Probíhá přesměrování...<br/>\n" +
                    "                    Pokud nedošlo k automatickému přesměrování pokračujte prosím kliknutím na tlačítko \"Pokračovat\"\n" +
                    "                    <input type=\"submit\" name=\"continueButton\" value=\"Pokračovat\"/>\n" +
                    "            </noscript>\n" +
                    "            </form>\n" +
                    "            </body>\n" +
                    "            </html>";

            resp.getWriter().print(redirectHtml);
            return;            //----------------------------------------------------------------------------
        }


        //jinak presmerujeme na chybovou stranku
        redirectToFailure(req, resp, null);
        return;            //----------------------------------------------------------------------------
    }

    private void redirectToFailure(HttpServletRequest req, HttpServletResponse resp, Map<String, String> returnData) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        String redirectHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "            <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "            <html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "            <head>\n" +
                "            <title>OpenID HTML FORM Redirection</title>\n" +
                "            <meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml; charset=UTF-8\" />\n" +
                "            </head>\n" +
                "            <body onload=\"document.getElementById('openid-form-redirection').submit();\">\n" +
                "            <form id=\"openid-form-redirection\" name=\"openid-form-redirection\" action=\"";
        final CustomerRequest customerRequest = (CustomerRequest) req.getSession().getAttribute(req.getParameter(HC_PARAM_NAME));
        redirectHtml += customerRequest.getFrontendUrl();
        redirectHtml += "/failure\" method=\"GET\" accept-charset=\"utf-8\">\n";
        if(returnData != null) {
            for (Map.Entry<String, String> entry : returnData.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                redirectHtml += "<input type=\"hidden\" name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>\n";
            }
        }
        redirectHtml += "            <noscript>\n" +
                "                    Probíhá přesměrování...<br/>\n" +
                "                    Pokud nedošlo k automatickému přesměrování pokračujte prosím kliknutím na tlačítko \"Pokračovat\"\n" +
                "                    <input type=\"submit\" name=\"continueButton\" value=\"Pokračovat\"/>\n" +
                "            </noscript>\n" +
                "            </form>\n" +
                "            </body>\n" +
                "            </html>";

        resp.getWriter().print(redirectHtml);
    }

    /**
     * Vrati ziskana mojeid data uzivatele po prihlaseni jako prepravku.
     *
     * @param verificationResult
     * @return
     * @throws MessageException
     */
    private Map<String, String> extractMojeidData(VerificationResult verificationResult) throws MessageException {
        if (verificationResult == null) throw new IllegalArgumentException("VerificationResult cannot be null ");
        final Map<String, String> attributes = new LinkedHashMap<String, String>();
        final AuthSuccess authSuccess = (AuthSuccess) verificationResult.getAuthResponse();

        attributes.put(MOJE_ID, verificationResult.getVerifiedId().getIdentifier());                //pridani mojeid identifikatoru mezi nactene atributy

        if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
            FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);

            @SuppressWarnings("unchecked")
            List<String> aliases = (List<String>) fetchResp.getAttributeAliases();
            for (Iterator<String> iter = aliases.iterator(); iter.hasNext(); ) {
                String alias = iter.next();
                @SuppressWarnings("unchecked")
                List<String> values = (List<String>) fetchResp.getAttributeValues(alias);
                if (values.size() > 0) {
                    attributes.put(alias, String.join("", values));
                } else {
                    //hodnota atributu je prazdna
                }
            }
        }

        return attributes;
    }


    /**
     * Presmeruje uzivatele na chybovou stranku (/error.jsp), chybu zaloguje(backendLogMessage+frontendMessage)
     * a vypise na chybove strance (frontendMessage). Po volani teto metody by mel nasledovat prikaz return.
     *
     * @param frontendMessage   text chyby na webovou chybovou stranku
     * @param backendLogMessage zprava zapsana do logu
     * @param cause             vyjimka, ktera zpusobila chybu
     * @param req
     * @param resp
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    private void redirectToErrorPage(String frontendMessage, String backendLogMessage, Throwable cause, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.error(backendLogMessage + ", [" + frontendMessage + "], from IP:" + req.getRemoteAddr(), cause);
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("errorMessage", frontendMessage);
        redirectToFailure(req, resp, msgMap);
    }


    /**
     * Zpracovani po uspesnem prihlaseni.
     */
    private void doSomething() {
        //.....
    }


    // --- metody pro praci s OPENID4JAVA knihovnou  ---

    /**
     * Vytvori autentizacni pozadavek na prihlaseni pomoci knihovny openid4java.
     * Presmeruje zakaznika na urceny endpoint.
     * Pokud dojde k vyjimce pri autentizaci, je uzivatel presmerovan na chybovou stranku.
     *
     * @param httpReq
     * @param httpResp
     * @param returnToUrl url kam bude uzivatel presmerovan po uspesne autentizaci na cizim OPENID webu.
     * @throws java.io.IOException            pokud se spravne nepodari redirect na autentizacni server
     * @throws javax.servlet.ServletException pokud se spravne nepodari redirect na autentizacni server
     */
    private void authOpenIdRequest(HttpServletRequest httpReq, HttpServletResponse httpResp, String returnToUrl) throws IOException, ServletException {
        try {
            //Pro iniciaci je nutne vyplnit bud identifikator uzivatele (uzivatelskejmeno.mojeid.cz) nebo
            //endpoint OpenID poskytovatele.
            //Tyto dve moznosti odpovidaji zvolene taktice pri prihlasovani uzivatele:
            // 1. Na webu poskytovatele sluzeb(nase index.jsp) je vstupni pole pro vyplneni identifikatoru
            //    uzivatele a tlacitko Prihlasit pomoci mojeID.
            // 2. Na webu poskytovatele sluzeb(nase index.jsp) je pouze tlacitko Prihlasit pomoci mojeID.
            //
            //V tomto priklade je pouzita varianta 1 a tak zde do promenne identifier vyplnime primo
            //endpoint mojeID, cimz take urcime, ze budeme pouzivat jen sluzeb tohoto poskytovatele
            //OpenID.
            final String identifier = DEFAULT_MOJEID_ENDPOINT;

            //spusteni vyhledavani endpoint poskytovatele podle zadaneho identifikatoru
            @SuppressWarnings("rawtypes")
            List discoveries = manager.discover(identifier);

            //pokus o spojeni s OpenID poskytovatelem a ziskani
            //jednoho endpoint pro autentizaci
            DiscoveryInformation discovered = manager.associate(discoveries);

            //ulozeni DiscoveryInformation do session
            httpReq.getSession().setAttribute(OPENID_DISC_INFO, discovered);

            //nastaveni realm, ve kterem musi lezet navratova url a na teto url take musi byt vystaven soubor xrds.xml
            final String realm = REALM;

            //ziskani AuthRequest zpravy odesilane OpenID providerovi
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl, realm);

            //definovani seznamu pozadovanych osobnich udaju, ktere chceme od uzivatele z mojeID ziskat
            //jde napr. o jmeno prijmeni, adresu apod., uzivatel po autentizaci musi schvalit jejich odeslani
            authReq.addExtension(this.getFetchRequestValues());

            //presmerovani uzivatelova prohlizece na web mojeID, kde provede autentizaci
            httpResp.sendRedirect(authReq.getDestinationUrl(true));
            return;

        } catch (OpenIDException e) {
            String message = "chyba pri vytvareni pozadavku na prihlaseni pres mojeID";
            log.error("Error during OpenID authentification, [" + message + "]", e);
            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("errorMessage", message);
            redirectToFailure(httpReq, httpResp, msgMap);
            return;
        }
    }


    /**
     * Provede overeni odpovedi od poskytovatele OpenID, jelikoz je predavana neprimo pomoci
     * presmerovani uzivatelova prohlizece.
     *
     * @param httpReq
     * @param httpResp
     * @return
     * @throws OpenIDException
     */
    private VerificationResult verifyResponse(HttpServletRequest httpReq, HttpServletResponse httpResp) throws OpenIDException {
        //ziska parametry z autentizacni odpovedi, ktera prisla v HTTP pozadavku od OpenID poskytovatele
        ParameterList response = new ParameterList(httpReq.getParameterMap());

        //nacteni drive ulozenych discovery information ze session
        DiscoveryInformation discovered = (DiscoveryInformation) httpReq.getSession().getAttribute(OPENID_DISC_INFO);

        //extrahovani prichozi URL s HTTP pozadavku vcetne parametru
        StringBuffer receivingURL = httpReq.getRequestURL();

        String queryString = httpReq.getQueryString();
        if (queryString != null && queryString.length() > 0)
            receivingURL.append("?").append(httpReq.getQueryString());

        log.debug("verifying response...");

        //overeni odpovedi, ConsumerManager musi byt ta stejna instance pouzita pro
        //vytvoreni autentizacniho pozadavku
        //VerificationResult obsahuje vysledky overeni odpovedi i samotnou puvodni odpoved od poskytovatele OpenID
        VerificationResult verification = manager.verify(receivingURL.toString(), response, discovered);

        //pokud VerificationResult obsahuje overeny identifikator, overeni odpovedi i cela autentizace byla uspesna
        if (verification.getVerifiedId() != null) {
            log.info("verifying response successful, verifiedId: " + verification.getVerifiedId());        //uspech
            return verification;
        }
        log.warn("verifying response failed");
        return null;
    }


    /**
     * Definice povinnych/nepovinnych udaju, ktere chceme ziskat z uctu mojeID.
     * Uplny vycet polozek i s prislusnymi URI schematy je uveden v dokumentu 01-mojeid__technicky_popis.pdf
     *
     * @return
     * @throws MessageException
     */
    private MessageExtension getFetchRequestValues() throws MessageException {
        FetchRequest fetch = FetchRequest.createFetchRequest();

        String typeUri = "http://axschema.org/namePerson/first";    //uri schema, oznacuje typ polozky
        boolean required = true;                                    //urcuje zda je udaj vyzadovan jako povinny nebo nepovinny
        int count = 1;                                                //pocet vracenych hodnot v pripade mojeID je rovno 1
        fetch.addAttribute(FIRST_NAME, typeUri, required, count);    //pridani pozadavku na atribut do seznamu pozadovanych atributu

        typeUri = "http://axschema.org/namePerson/last";
        required = true;
        count = 1;
        fetch.addAttribute(SURNAME, typeUri, required, count);

        typeUri = "http://axschema.org/contact/email";
        required = true;
        count = 1;
        fetch.addAttribute(EMAIL_PRIMARY, typeUri, required, count);

/*		typeUri = "http://axschema.org/company/name";
		required = true;
		count = 1;
		fetch.addAttribute(COMPANY_NAME, typeUri, required, count);
		
		typeUri = "http://specs.nic.cz/attr/contact/vat";		
		required = true;
		count = 1;
		fetch.addAttribute(VAT, typeUri, required, count);
		
		typeUri = "http://specs.nic.cz/attr/contact/ident/vat_id";		
		required = true;
		count = 1;
		fetch.addAttribute(ICO, typeUri, required, count);
		
		typeUri = "http://axschema.org/contact/phone/default";
		required = true;
		count = 1;
		fetch.addAttribute(PHONE, typeUri, required, count);*/

        typeUri = "http://specs.nic.cz/attr/contact/valid";
        required = true;
        count = 1;
        fetch.addAttribute(VALID, typeUri, required, count);

        typeUri = "http://specs.nic.cz/attr/contact/status";
        required = true;
        count = 1;
        fetch.addAttribute(STATUS, typeUri, required, count);

        typeUri = "http://axschema.org/contact/postalAddress/home";
        required = true;
        count = 1;
        fetch.addAttribute(STREET, typeUri, required, count);

        typeUri = "http://axschema.org/contact/city/home";
        required = true;
        count = 1;
        fetch.addAttribute(CITY, typeUri, required, count);

/*		typeUri = "http://axschema.org/contact/country/home";
		required = true;
		count = 1;
		fetch.addAttribute(COUNTRY, typeUri, required, count);*/

        typeUri = "http://axschema.org/contact/postalCode/home";
        required = true;
        count = 1;
        fetch.addAttribute(POSTAL_CODE, typeUri, required, count);

        typeUri = "http://specs.nic.cz/attr/contact/ident/card";
        required = true;
        count = 1;
        fetch.addAttribute(OP_NUMBER, typeUri, required, count);
        return fetch;
    }


    /**
     * Ziska parametry pro nastaveni proxy z init parametru servletu.
     *
     * @return proxy properties
     */
    private static ProxyProperties getProxyProperties(ServletConfig config) {
        ProxyProperties proxyProps;
        String host = config.getInitParameter("proxy.host");
        log.debug("proxy.host: " + host);
        if (host == null) {
            proxyProps = null;
        } else {
            proxyProps = new ProxyProperties();
            String port = config.getInitParameter("proxy.port");
            String username = config.getInitParameter("proxy.username");
            String password = config.getInitParameter("proxy.password");
            String domain = config.getInitParameter("proxy.domain");
            proxyProps.setProxyHostName(host);
            proxyProps.setProxyPort(Integer.parseInt(port));
            proxyProps.setUserName(username);
            proxyProps.setPassword(password);
            proxyProps.setDomain(domain);
        }
        return proxyProps;
    }
    // --- konec metod pro praci s OPENID4JAVA knihovnou  ---


    //============== VNORENE A VNITRNI TRIDY ====================================

    /**
     * Vyjimka vyhozena pokud chybi nektery z ocekavanych parametru.
     *
     * @author ACTIVE24, s.r.o.
     */
    private static class MissingParamException extends Exception {
        private static final long serialVersionUID = 6772038564563188981L;

        public MissingParamException(String message) {
            super(message);

        }
    }


    //============== OSTATNÍ (MAIN A AUTOMATICKY GENEROVANE METODY) =============

}


	