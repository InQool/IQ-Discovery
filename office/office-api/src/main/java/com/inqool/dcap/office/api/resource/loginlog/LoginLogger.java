package com.inqool.dcap.office.api.resource.loginlog;

import com.inqool.dcap.common.dto.PersonInfoHolder;
import com.inqool.dcap.config.Zdo;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Lukas Jane (inQool) 19. 11. 2015.
 */
@ApplicationScoped
public class LoginLogger {
    @Inject
    @Zdo
    private Logger logger;

    public void logLogin(PersonInfoHolder personInfoHolder) {
        logger.debug("User " + personInfoHolder.getIdmUsername() + " (" + personInfoHolder.getFirstName() + " " + personInfoHolder.getLastName() + ") from " + personInfoHolder.getOrganizationName() + " logged in.");
    }
}
