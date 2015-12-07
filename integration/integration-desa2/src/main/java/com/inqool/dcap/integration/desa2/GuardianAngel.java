package com.inqool.dcap.integration.desa2;

import com.inqool.dcap.config.Zdo;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

/**
 * @author Lukas Jane (inQool) 16. 9. 2015.
 */
@RequestScoped
public class GuardianAngel {
    @Inject
    @Zdo
    private Logger logger;

    private  int numOpened = 0;
    private  int numClosed = 0;
    public int getScore() {
        return numOpened - numClosed;
    }

    public int getNumOpened() {
        return numOpened;
    }

    public void setNumOpened(int numOpened) {
        this.numOpened = numOpened;
    }

    public int getNumClosed() {
        return numClosed;
    }

    public void setNumClosed(int numClosed) {
        this.numClosed = numClosed;
    }

    public void open() {
        numOpened++;
    }

    public void close() {
        numClosed++;
    }

    public void dump() {
        if(numOpened != numClosed) {
            logger.warn("Your guardian angel: opened " + numOpened + " closed " + numClosed + " score " + getScore());
        }
    }
}
