package com.inqool.dcap.discovery.api.request;

import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 23. 2. 2015.
 */
public class SearchRequest {

    @QueryParam("query")
    private String query;

    @QueryParam("start")
    private Integer start;

    @QueryParam("maxCount")
    private Integer maxRecords;

    @QueryParam("orderBy")
    private String sort;

    @QueryParam("orderDir")
    private String sortDir;

    @QueryParam("parentId")
    private String parentId;

    @QueryParam("facets")
    private Boolean facets;

    @QueryParam("facf")
    private List<String> facetFields = new ArrayList<>();

    @QueryParam("facq")
    private List<String> facetQueries = new ArrayList<>();

    @QueryParam("fq")
    private List<String> filterQueries = new ArrayList<>();

    public String getQuery() {
        if(query == null || query.equals("")) query = "*:*";
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getStart() {
        if(start == null) {
            return 0;
        }
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public int getMaxRecords() {
        if(maxRecords == null) {
            return 30;
        }
        return maxRecords;
    }

    public void setMaxRecords(Integer maxRecords) {
        this.maxRecords = maxRecords;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getSortDir() {
        if(!"desc".equals(sortDir.toLowerCase())) {
            return "asc";
        }
        return "desc";
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<String> getFilterQueries() {
        return filterQueries;
    }

    public void setFilterQueries(List<String> filterQueries) {
        this.filterQueries = filterQueries;
    }

    public Boolean isFacets() {
        if(facets == null) {
            return true;
        }
        return facets;
    }

    public void setFacets(Boolean facets) {
        this.facets = facets;
    }

    public List<String> getFacetFields() {
        return facetFields;
    }

    public void setFacetFields(List<String> facetFields) {
        this.facetFields = facetFields;
    }

    public List<String> getFacetQueries() {
        return facetQueries;
    }

    public void setFacetQueries(List<String> facetQueries) {
        this.facetQueries = facetQueries;
    }
}
