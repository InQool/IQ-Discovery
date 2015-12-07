package com.inqool.dcap.office.api.request;

import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.office.api.resource.Document;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.QueryParam;

/**
 * @author Lukas Jane (inQool) 3. 3. 2015.
 */
@Getter
@Setter
public class DocumentListRequest {
    @QueryParam("state")
    private String state;

    @QueryParam("batch")
    private String batch;

    @QueryParam("inventoryId")
    private String inventoryId;

    @QueryParam("limit")
    private Integer limit;

    @QueryParam("offset")
    private Integer offset;

    @QueryParam("orderBy")
    private String orderBy;

    @QueryParam("orderDir")
    private String orderDir;

    @Inject
    @Zdo
    private Logger logger;

    public String getState() {
        if(state == null) state = "all";
        else {
            try {
                state = Document.DocumentState.valueOf(state).name();
            } catch(IllegalArgumentException e) {
                logger.error("Document state from request not recognized, defaulting to all.");
                state = "all";
            }
        }
        return state;
    }

    public Integer getBatch() {
        if(batch == null || !batch.matches("\\d+")) {
            return null;
        }
        try {
            return Integer.parseInt(batch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getOrderDir() {
        if("DESC".equals(orderDir)) {
            return orderDir;
        }
        return "ASC";
    }

    public Integer getOffset() {
        if(offset == null) {
            return 0;
        }
        return offset;
    }

    public Integer getLimit() {
        if(limit == null || limit == 0) {
            return 100000;
        }
        return limit;
    }
}
