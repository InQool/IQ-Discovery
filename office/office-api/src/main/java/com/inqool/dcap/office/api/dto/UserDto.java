package com.inqool.dcap.office.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 21. 5. 2015.
 */
@Getter
@Setter
public class UserDto {
    private String idmId;
    private String firstName;
    private String lastName;
    private String mail;
    private List<String> roles = new ArrayList<>();
}
