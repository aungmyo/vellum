/*
 * Source https://code.google.com/p/vellum by @evanxsummers

       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements. See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.  
 */
package dualcontrol;

import java.math.BigInteger;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evan.summers
 */
public class RevocableClientTrustManager implements X509TrustManager {
    static Logger logger = LoggerFactory.getLogger(RevocableClientTrustManager.class);

    X509Certificate serverCertificate;
    X509TrustManager delegate;
    Set<String> revokedCommonNames;
    Set<BigInteger> revokedSerialNumbers;
    
    public RevocableClientTrustManager(X509Certificate serverCertificate, 
            X509TrustManager delegate, Set<String> revokedCommonNames,
            Set<BigInteger> revokedSerialNumbers) {
        this.delegate = delegate;
        this.serverCertificate = serverCertificate;
        this.revokedCommonNames = revokedCommonNames;
        this.revokedSerialNumbers = revokedSerialNumbers;
    }
    
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[] {serverCertificate};
    }
    
    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) 
            throws CertificateException {
        if (certs.length != 2) {
            throw new CertificateException("Invalid cert chain length");
        }
        logger.debug(String.format("checkClientTrusted %s, issuer %s, root %s", 
                certs[0].getSubjectDN().getName(), certs[0].getIssuerDN().getName(),
                certs[1].getSubjectDN().getName()));
        if (!certs[0].getIssuerX500Principal().equals(
                serverCertificate.getSubjectX500Principal())) {
            throw new CertificateException("Untrusted issuer");
        }
        if (!Arrays.equals(certs[1].getPublicKey().getEncoded(),
                serverCertificate.getPublicKey().getEncoded())) {
            throw new CertificateException("Invalid server certificate");
        }
        if (revokedCommonNames != null && 
                revokedCommonNames.contains(getCN(certs[0].getSubjectDN()))) {
            throw new CertificateException("Certificate CN revoked");
        }
        if (revokedSerialNumbers != null && 
                revokedSerialNumbers.contains(certs[0].getSerialNumber())) {
            throw new CertificateException("Certificate serial number revoked");
        }
        delegate.checkClientTrusted(certs, authType);
    }
    
    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) 
            throws CertificateException {
        delegate.checkServerTrusted(certs, authType);
    }        
        
    public static String getCN(Principal principal) throws CertificateException {
        String dname = principal.getName();
        try {
            LdapName ln = new LdapName(dname);
            for (Rdn rdn : ln.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    return rdn.getValue().toString();
                }
            }
            throw new InvalidNameException("no CN: " + dname);
        } catch (Exception e) {
            throw new CertificateException(e.getMessage());
        }
    }        
    
}