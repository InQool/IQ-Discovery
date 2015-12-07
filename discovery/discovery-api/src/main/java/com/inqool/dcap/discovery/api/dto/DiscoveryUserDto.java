package com.inqool.dcap.discovery.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Lukas Jane (inQool) 29. 1. 2015.
 */
@Getter
@Setter
public class DiscoveryUserDto {
    private String userName;
    private String password;
    private String firstName;
    private String lastName;
    private String street;
    private String streetNumber;
    private String city;
    private String zip;
    private String opNumber;
    private boolean verified;
}
