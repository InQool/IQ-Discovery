package com.inqool.dcap.security;

/**
 * @author Lukas Jane (inQool) 26. 1. 2015.
 */
public enum ZdoRoles {
    redactor,
    sys_admin,
    org_admin,
    curator;

    public static final String CURATOR = "curator";
    public static final String REDACTOR = "redactor";
    public static final String ADMIN_SYS = "sys_admin";
    public static final String ADMIN_ORG = "org_admin";
}
