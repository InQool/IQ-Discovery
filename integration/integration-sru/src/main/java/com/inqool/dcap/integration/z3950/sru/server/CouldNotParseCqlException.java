package com.inqool.dcap.integration.z3950.sru.server;

/**
 * @author Lukas Jane (inQool) 11. 12. 2014.
 */
public class CouldNotParseCqlException extends Exception {
    public CouldNotParseCqlException() { super(); }
    public CouldNotParseCqlException(String message) { super(message); }
    public CouldNotParseCqlException(String message, Throwable cause) { super(message, cause); }
    public CouldNotParseCqlException(Throwable cause) { super(cause); }
}
