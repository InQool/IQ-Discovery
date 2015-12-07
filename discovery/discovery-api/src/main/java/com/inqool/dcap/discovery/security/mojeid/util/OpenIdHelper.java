package com.inqool.dcap.discovery.security.mojeid.util;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.html.HtmlResolver;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.server.RealmVerifierFactory;
import org.openid4java.util.HttpFetcherFactory;

import javax.net.ssl.SSLContext;

/**
 * Copyright Keith D Swenson, Creative Commons Share-Alike
 * Feel free to use this code where you like, but keep this copyright
 * statement in it, and understand that there is no guarantee to be
 * suitable for any purpose, so read it carefully.
 */
public class OpenIdHelper {

    /**
     * Constructs a new openID4Java Consumer Manager object, properly
     * initialized so that it does not validate certificates.
     */
    public static ConsumerManager newConsumerManager() throws Exception {
        // Install the all-trusting trust manager SSL Context
        SSLContext sc = SSLPatch.disableSSLCertValidation();

        HttpFetcherFactory hff = new HttpFetcherFactory(sc,
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        YadisResolver yr = new YadisResolver(hff);
        RealmVerifierFactory rvf = new RealmVerifierFactory(yr);
        Discovery d = new Discovery(new HtmlResolver(hff),yr,
                Discovery.getXriResolver());

        ConsumerManager manager = new ConsumerManager(rvf, d, hff);
        manager.setAssociations(new InMemoryConsumerAssociationStore());
        manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
        return manager;
    }
}