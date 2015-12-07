package com.inqool.dcap.integration.oai.provider;

import com.inqool.dcap.integration.oai.provider.exception.NoSetHierarchyPmhException;
import com.inqool.dcap.integration.oai.provider.formats.DcFormatDescriptor;
import com.inqool.dcap.integration.oai.provider.formats.EseFormatDescriptor;
import com.inqool.dcap.integration.oai.provider.formats.FormatDescriptor;
import org.openarchives.oai._2.DeletedRecordType;
import org.openarchives.oai._2.GranularityType;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.ListSetsType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class providing all the custom configuration for the OAI-PMH provider.
 *
 * If you are looking to use the OAI-PMH provider, you should:
 *
 * 1) fill in your repository information to the identify() method
 * 2) implement FormatDescriptor for all metadata formats you wish to support, or just complete the default DcFormatDescriptor template
 * 3) add all supported formats in this class constructor
 *
 * @author Lukas Jane (inQool) 1. 5. 2015.
 */
@ApplicationScoped
public class OaiPmhConfiguration {
    private Map<String, FormatDescriptor> supportedFormats = new HashMap<>();

    public OaiPmhConfiguration() {

    }
    /**
     * Add all supported metadata formats here.
     * When request comes, Resource classes use these format descriptors to load data and build the response.
     */
    @Inject
    public OaiPmhConfiguration(DcFormatDescriptor dcFormatDescriptor, EseFormatDescriptor eseFormatDescriptor) {
        addSupportedFormat(dcFormatDescriptor);
        addSupportedFormat(eseFormatDescriptor);
        //addSupportedFormat();
    }

    /**
     * Provides basic info about the repository when identify verb is requested
     */
    public IdentifyType identify() {
        IdentifyType identifyType = new IdentifyType();
        //TODO fill actual info
        identifyType.setRepositoryName("IQ ZDO Repository");
        identifyType.getAdminEmails().add("admin@example.com");
        identifyType.setEarliestDatestamp("2015-08-01T00:00:00Z");    //find earliest modified record and return the time
        identifyType.setDeletedRecord(DeletedRecordType.NO);
        identifyType.setGranularity(GranularityType.YYYY_MM_DD);
        identifyType.setProtocolVersion("2.0");
        return identifyType;
    }

    /**
     * List sets, groups into which records are divided in this repository
     * @throws NoSetHierarchyPmhException
     */
    public ListSetsType listSets() throws NoSetHierarchyPmhException {
        throw new NoSetHierarchyPmhException();
    }

    /* Following methods do not need to be changed */

    public void addSupportedFormat(FormatDescriptor formatDescriptor) {
        supportedFormats.put(formatDescriptor.getMetadataPrefix(), formatDescriptor);
    }

    public FormatDescriptor getFormatDescriptor(String name) {
        return supportedFormats.get(name);
    }

    public Collection<FormatDescriptor> listAllFormats() {
        return supportedFormats.values();
    }
}
