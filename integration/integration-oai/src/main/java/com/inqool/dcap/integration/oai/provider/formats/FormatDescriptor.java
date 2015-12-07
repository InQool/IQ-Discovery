package com.inqool.dcap.integration.oai.provider.formats;

import com.inqool.dcap.integration.oai.provider.exception.CannotDisseminateFormatPmhException;
import com.inqool.dcap.integration.oai.provider.exception.IdDoesNotExistPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoRecordsMatchPmhException;
import com.inqool.dcap.integration.oai.provider.exception.NoSetHierarchyPmhException;
import org.openarchives.oai._2.GetRecordType;
import org.openarchives.oai._2.ListIdentifiersType;
import org.openarchives.oai._2.ListRecordsType;

import java.time.OffsetDateTime;

/**
 * Interface that represents a metadata format (for example OAI Dublin Core) and provides methods for OAI-PMH to be able to respond with records in this format.
 * You should implement this interface for every format you wish to support.
 * DcFormatDescriptor is the default example implementation.
 * @author Lukas Jane (inQool) 1. 5. 2015.
 */
public interface FormatDescriptor {
    /**
     * Get short metadata prefix that is used to represent this metadata format
     * example: "oai_dc"
     */
    String getMetadataPrefix();

    /**
     * Get namespace of this metadata format
     * example: "http://www.openarchives.org/OAI/2.0/oai_dc/"
     */
    String getMetadataNamespace();

    /**
     * Get schema location of this metadata format
     * example: "http://www.openarchives.org/OAI/2.0/oai_dc.xsd"
     */
    String getSchema();

    /**
     * Find record by its identifier and return its header and metadata in format that this descriptor represents.
     *  @param identifier Id of the record
     *  @return Header and Metadata in this descriptors format.
     *  @throws CannotDisseminateFormatPmhException The item identified by the value of the identifier argument can't be exported in this format.
     *  @throws IdDoesNotExistPmhException The value of the identifier argument is unknown or illegal in this repository.
     */
    GetRecordType getRecord(String identifier) throws CannotDisseminateFormatPmhException, IdDoesNotExistPmhException;

    /**
     * Find records last modified between from and until belonging to the set.
     * Only returns records supported by this format.
     * If there is too many records, they can be retrieved in groups page by page.
     *  @param from null, or lower boundary on last modified time of records to be returned
     *  @param until null, or upper (inclusive) boundary on last modified time of records to be returned
     *  @param set null if all records are to be returned regardless of set. Otherwise name of the set from which the records are returned.
     *  @param page which page of records to return. Pages can be for example 1000 records, then first 1000 records belong to page 0.
     *  @return list of records
     *  @throws NoRecordsMatchPmhException The combination of the values of the from, until, and set arguments results in an empty list.
     *  @throws NoSetHierarchyPmhException The repository does not support sets.
     */
    ListRecordsType listRecords(OffsetDateTime from, OffsetDateTime until, String set, int page) throws NoRecordsMatchPmhException, CannotDisseminateFormatPmhException, NoSetHierarchyPmhException;

    /**
     * Same as listRecords, but returns only headers with identifiers, not body of the record with metadata itself.
     * Only returns records supported by this format.
     * If there is too many records, they can be retrieved in groups page by page.
     *  @param from null, or lower boundary on last modified time of records to be returned
     *  @param until null, or upper (inclusive) boundary on last modified time of records to be returned
     *  @param set null if all records are to be returned regardless of set. Otherwise name of the set from which the records are returned.
     *  @param page offset in pages, from which to start. Pages can be for example 1000 records, then first 1000 records belong to page 0.
     *  @return list of records
     *  @throws NoRecordsMatchPmhException The combination of the values of the from, until, and set arguments results in an empty list.
     *  @throws NoSetHierarchyPmhException The repository does not support sets.
     */
    ListIdentifiersType listIdentifiers(OffsetDateTime from, OffsetDateTime until, String set, int page) throws NoRecordsMatchPmhException, CannotDisseminateFormatPmhException, NoSetHierarchyPmhException;
}
