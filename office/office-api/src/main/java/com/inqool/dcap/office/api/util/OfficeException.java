package com.inqool.dcap.office.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 3. 6. 2015.
 */
public class OfficeException extends Exception {

    private int errorCode;
    private List<Object> parameters = new ArrayList<>();

    public OfficeException() {
    }

    public OfficeException(String message) {
        super(message);
    }

    public OfficeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OfficeException(Throwable cause) {
        super(cause);
    }

    public OfficeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void addParameter(Object parameter) {
        this.parameters.add(parameter);
    }
    public void addParameters(List<Object> parameters) {
        this.parameters.addAll(parameters);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("errorCode", errorCode);
        resultMap.put("parameters", parameters);
        return resultMap;
    }
}