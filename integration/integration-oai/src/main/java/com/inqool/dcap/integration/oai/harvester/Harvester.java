
package com.inqool.dcap.integration.oai.harvester;

import com.rdksys.oai.MyObjectOutputStream;
import com.rdksys.oai.data.RecordIterator;
import org.openarchives.oai._2.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author David Uvalle, david.uvalle@gmail.com
 * @version 0.1
 * 
 */
public class Harvester {

	private String baseUrl;
	private String resumptionToken = null;
	private String identifiersResumptionToken = null;
	private String setResumptionToken = null;

	/**
	 * Constructs the harvester using a repository URL.
	 * @param baseUrl A repository valid URL.
	 */
	public Harvester(String baseUrl) throws Exception {
		if(baseUrl == null || baseUrl.isEmpty())
			throw new Exception("baseUrl cannot be null");
		this.baseUrl = baseUrl;
		deleteTmpFile(getMD5Filename(baseUrl));
	}

	/**
	 * Returns an IdentifyType object containing information about the OAI respository.
	 * @return	A IdentifyType object.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public IdentifyType identify() throws Exception {

        String url = baseUrl + "?verb=Identify";
        OAIPMH oaipmh = oaipmhRequest(url);

        handleOaiErrors(oaipmh);

		return oaipmh.getIdentify();
	}

	/**
	 * Returns a List of HeaderType type, using selective harvesting.
	 * @param from A date.
	 * @param until A date.
	 * @param set A set name supported by the respository.
	 * @return {@link java.util.List}
	 * @throws Exception
	 */
	public List<HeaderType> listIdentifiers(String from, String until, String set, String metadataPrefix) throws Exception {
		List<HeaderType> listIdentifiers = listIdentifiers(null, from, until, set, metadataPrefix);
        while (identifiersResumptionToken != null) {
            listIdentifiers.addAll(listIdentifiers(identifiersResumptionToken, null, null, null));
        }
		return listIdentifiers;
	}

	/**
	 * Returns a List of HeaderType type.
	 * @return A {@link java.util.List} of headers.
	 */
	public List<HeaderType> listIdentifiers() throws Exception {
		return listIdentifiers(null, null, null, null);
	}

	/**
	 * listIdentifiers() auxiliar method.
	 * @param resumptionToken
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<HeaderType> listIdentifiers(String resumptionToken, String from, String until, String set, String metadataPrefix) throws Exception {
		// TODO: handle status attribute
        identifiersResumptionToken = null;
        String url;
        if(resumptionToken == null) {
            url = baseUrl + "?verb=ListIdentifiers&metadataPrefix=" + metadataPrefix;
            if (from != null) {
                url += "&from=" + from;
            }
            if (until != null) {
                url += "&until=" + until;
            }
            if (set != null) {
                url += "&set=" + set;
            }
        }
        else {
            url = baseUrl + "?verb=ListIdentifiers&resumptionToken=" + resumptionToken;
        }
        OAIPMH oaipmh = oaipmhRequest(url);

        handleOaiErrors(oaipmh);

        ListIdentifiersType listIdentifiers = oaipmh.getListIdentifiers();

        //Resumption token
        if(listIdentifiers.getResumptionToken() != null && !"".equals(listIdentifiers.getResumptionToken().getValue())) {
            identifiersResumptionToken = listIdentifiers.getResumptionToken().getValue();
        }

        return listIdentifiers.getHeaders();
	}

	/**
	 * Returns a List of MetadataFormatType supported by the repository.
	 * @return A {@link java.util.List} of MetadataFormats.
	 * @throws Exception
	 */
	public List<MetadataFormatType> listMetadataFormats() throws Exception {
		return listMetadataFormats(null);
	}

	/**
	 * Returns a List of MetadataFormatType from a given identifier.
	 * @param identifier valid OAI document identifier.
	 * @return {@link java.util.List}
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<MetadataFormatType> listMetadataFormats(String identifier) throws Exception {
        String url = baseUrl + "?verb=ListMetadataFormats";
        if(identifier != null) {
            url += "&identifier=" + identifier;
        }
        OAIPMH oaipmh = oaipmhRequest(url);

        handleOaiErrors(oaipmh);

        return oaipmh.getListMetadataFormats().getMetadataFormats();
	}

	/**
	 * Returns a List of SetType.
	 * @return {@link java.util.List}
	 * @throws Exception
	 */
	public List<SetType> listSets() throws Exception {
		List<SetType> listSets = listSets(null);
        while(setResumptionToken != null) {
            listSets.addAll(listSets(setResumptionToken));
        }
		return listSets;
	}

	/**
	 * Returns a List of SetType supported by the repository.
	 * @return {@link java.util.List}
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<SetType> listSets(String resumptionToken) throws Exception {
        String url = baseUrl + "?verb=ListSets";
        if(resumptionToken != null) {
            url += "&resumptionToken=" + resumptionToken;
        }
        OAIPMH oaipmh = oaipmhRequest(url);

        handleOaiErrors(oaipmh);

        if(oaipmh.getListSets().getResumptionToken() != null && !"".equals(oaipmh.getListSets().getResumptionToken().getValue())) {
            setResumptionToken = oaipmh.getListSets().getResumptionToken().getValue();
        }
        else {
            setResumptionToken = null;
        }
        return oaipmh.getListSets().getSets();
	}

	/**
	 * 	Returns a Record object harvested by his identifier
	 * 	using 'oai_dc' as metadataPrefix
	 * @param identifier A OAI document identifier.
	 * @return	Record
	 */
	@SuppressWarnings("unchecked")
	public RecordType getRecord(String identifier, String metadataPrefix) throws Exception {
        String url = baseUrl + "?verb=GetRecord&identifier=" + identifier + "&metadataPrefix=" + metadataPrefix;
        OAIPMH oaipmh = oaipmhRequest(url);

        handleOaiErrors(oaipmh);

		return oaipmh.getGetRecord().getRecord();
	}

	/**
	 * Selective harvest of records from a repository
	 * and store them in a temporal file for later use.
	 * @param from A date.
	 * @param until A date.
	 * @param set A set supported by the repository.
	 * @return {@link com.rdksys.oai.data.RecordIterator}
	 * @throws Exception
	 *
	 */
	public RecordIterator listRecords(String from, String until, String set, String metadataPrefix) throws Exception {
        List<RecordType> listRecords;

        String filename = getMD5Filename(baseUrl);
        File file = new File(filename);
        if(file.exists() && !file.delete()) {
            throw new RuntimeException("Could not delete file from previous harvest.");
        }

        listRecords = listRecords(null, from, until, set, metadataPrefix);
        writeOnDisk(listRecords);
        while(resumptionToken != null) {
            listRecords = listRecords(resumptionToken, from, until, set, metadataPrefix);
            writeOnDisk(listRecords);
        }
        return new RecordIterator(getMD5Filename(baseUrl));
	}

	/**
	 * Harvest all the records from a repository
	 * and store them in a temporal file for later use.
	 * @return {@link com.rdksys.oai.data.RecordIterator}
	 * @throws java.io.FileNotFoundException
	 * @throws Exception
	 * 
	 */
	public RecordIterator listRecords(String metadataPrefix) throws FileNotFoundException, Exception {
        return listRecords(null, null, null, metadataPrefix);
	}
	
	@SuppressWarnings("unchecked")
	private List<RecordType> listRecords(String resumptionToken, String from, String until, String set, String metadataPrefix) throws Exception {
        this.resumptionToken = null;
        String url;
        if(resumptionToken == null) {
            url = baseUrl + "?verb=ListRecords&metadataPrefix=" + metadataPrefix;
            if (from != null) {
                url += "&from=" + from;
            }
            if (until != null) {
                url += "&until=" + until;
            }
            if (set != null) {
                url += "&set=" + set;
            }
        }
        else {
            url = baseUrl + "?verb=ListRecords&resumptionToken=" + resumptionToken;
        }
        OAIPMH oaipmh = oaipmhRequest(url);

        handleOaiErrors(oaipmh);
		
		ListRecordsType listRecords = oaipmh.getListRecords();

        //Resumption token
        if(listRecords.getResumptionToken() != null && !"".equals(listRecords.getResumptionToken().getValue())) {
            this.resumptionToken = listRecords.getResumptionToken().getValue();
        }

        //Records
        return listRecords.getRecords();
	}
	
	private String getMD5Filename(String baseUrl) {
		String md5 = "";
		try {
			MessageDigest mdEnc = MessageDigest.getInstance("MD5");
			mdEnc.update(baseUrl.getBytes(),0,baseUrl.length());
			md5 = new BigInteger(1,mdEnc.digest()).toString(16);
		}
		catch(NoSuchAlgorithmException e) { }
		return md5;
	}
	
	private void writeOnDisk(List<RecordType> recordList) {
		
		String filename = getMD5Filename(baseUrl);
		
		File file = new File(filename);
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		MyObjectOutputStream moos = null;
		
		
		if(file.exists()) {
			try {
				fos = new FileOutputStream(file,true);
				//oos = new ObjectOutputStream(fos);
				moos = new MyObjectOutputStream(fos);
				for(RecordType r:recordList){
					moos.writeObject(r);
				}
				moos.flush(); moos.close();
				
			} catch(Exception e) { 
				System.out.println("Append error "+e);
			}
		}
		else {
			try {
				if(file.createNewFile())
				{
					fos = new FileOutputStream(file);
					oos = new ObjectOutputStream(fos);
					for(RecordType r:recordList) {
						oos.writeObject(r);
					}
					oos.flush();
					oos.close();
				}
				else {
					System.out.println("Cannot create file");
				}
				
			}
			catch(Exception e) {
				System.out.println("Create error "+e);
			}
		}
	}
	
	private boolean fileExists(String filename) {
		File tempFile = new File(filename);
		return tempFile.exists();
	}
	
	private void deleteTmpFile(String filename) {
		File tempFile = new File(filename);
		if(tempFile.exists())
			tempFile.delete();
	}

    private OAIPMH oaipmhRequest(String url) throws JAXBException, MalformedURLException {
        JAXBContext jaxbContext = JAXBContext.newInstance("" +
                "org.openarchives.oai._2" +
                ":org.openarchives.oai._2_0.oai_dc" +
                ":org.purl.dc.elements._1" +
                ":gov.loc.marc21.slim");
        Object unmarshalled = jaxbContext.createUnmarshaller().unmarshal(new URL(url));
        return (OAIPMH) unmarshalled;
    }

    private void handleOaiErrors(OAIPMH oaipmh) throws Exception {
        //Errors
        if(!oaipmh.getErrors().isEmpty()) {
            OAIPMHerrorType error = oaipmh.getErrors().get(0);
            throw new Exception("OAI harvester exception: " + error.getCode() + " - " + error.getValue());
        }
    }
}
