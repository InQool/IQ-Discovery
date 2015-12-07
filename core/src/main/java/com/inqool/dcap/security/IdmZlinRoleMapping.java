package com.inqool.dcap.security;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukas Jane (inQool) 2. 7. 2015.
 */
@Getter
public class IdmZlinRoleMapping {
    public static final Map<String, ZdoRoles> idmZlinRoleMapping = new HashMap<String, ZdoRoles>() {
        {
            put("redaktor", ZdoRoles.redactor);
            put("administrator_instituce", ZdoRoles.org_admin);
            put("administrator_systemu", ZdoRoles.sys_admin);
            put("kurator", ZdoRoles.curator);
        }
    };
}
