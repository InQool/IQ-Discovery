package com.inqool.dcap.discovery.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.solr.common.SolrDocumentList;

import java.util.List;

/**
 * @author Lukas Jane (inQool) 21. 7. 2015.
 */
@Getter
@Setter
public class SearchResponse {
    private SolrDocumentList searchResults;
    private long numResults;
    private List<Facet> facets;
}
