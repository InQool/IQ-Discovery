package com.inqool.dcap.discovery.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
* @author Lukas Jane (inQool) 17. 8. 2015.
*/
@Getter
@Setter
public class PwdResetKeyNewPassword {
    private String pwdResetKey;
    private String newPassword;
}
