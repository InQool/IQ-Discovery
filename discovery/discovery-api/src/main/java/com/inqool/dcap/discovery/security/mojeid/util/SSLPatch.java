package com.inqool.dcap.discovery.security.mojeid.util;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Copyright Keith D Swenson, Creative Commons Share-Alike
 * Feel free to use this code where you like, but keep this copyright
 * statement in it, and understand that there is no guarantee to be
 * suitable for any purpose, so read it carefully.
 */
public class SSLPatch
{

    //Added by Lukess
    public static KeyStore getKeyStore(String resource, String password) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        try (InputStream stream = SSLPatch.class.getClassLoader().getResourceAsStream(resource)) {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(stream,password.toCharArray());

            return keyStore;
        }
    }
    //End added by Lukess

    /**
     * Java proides a standard "trust manager" interface.  This trust manager
     * essentially disables the rejection of certificates by trusting anyone and everyone.
     */
    public static X509TrustManager getDummyTrustManager() {
        return new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        };
    }

    /**
     * Returns a hostname verifiers that always returns true, always positively verifies a host.
     */
    public static HostnameVerifier getAllHostVerifier() {
        return new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    /**
     * a call to disableSSLCertValidation will disable certificate validation
     * for SSL connection made after this call.   This is installed as the
     * default in the JVM for future calls.  Returns the SSLContext in case
     * you need it for something else.
     */
    public static SSLContext disableSSLCertValidation() throws Exception {

        //Added by Lukess
        KeyStore store = getKeyStore("mojeIdKeystore.jks", "wocu87fopa");

        final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(store);

        /*TrustManager[] tma = factory.getTrustManagers();*/
        //End added by Lukess

        // Create a trust manager that does not validate certificate chains
        TrustManager[] tma = new TrustManager[] {getDummyTrustManager()};   //disabled by Lukess

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, tma, new java.security.SecureRandom());
        /*HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());*/   //disabled by Lukess

        // Install the all-trusting host verifier
        /*HttpsURLConnection.setDefaultHostnameVerifier(getAllHostVerifier());*/   //disabled by Lukess
        return sc;
    }

}