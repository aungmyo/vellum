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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;
import sun.security.pkcs.PKCS10;

/**
 *
 * @author evan
 */
public class LocalCaTest {

    private final static Logger logger = Logger.getLogger(LocalCaTest.class);
    private final int port = 4446;
    private char[] pass = "test1234".toCharArray();
    private SSLParams ca = new SSLParams("ca");
    private SSLParams server = new SSLParams("server");
    private SSLParams client = new SSLParams("client");
            
    class SSLParams {
        String alias;
        GenRsaPair pair;
        KeyStore keyStore;
        KeyStore trustStore;
        SSLContext sslContext;
        X509Certificate cert;
        PKCS10 certRequest;
        KeyStore signedKeyStore;
        KeyStore signedTrustStore;
        X509Certificate signedCert;
        SSLContext signedContext;
        
        SSLParams(String alias) {
            this.alias = alias;
        }
        
        void init() throws Exception {
            pair = new GenRsaPair();
            pair.generate("CN=" + alias, new Date(), 365);
            cert = pair.getCertificate();
            keyStore = createKeyStore(alias, pair);
            sslContext = SSLContexts.create(keyStore, pass, keyStore);
            certRequest = pair.getCertRequest("CN=" + alias);
        }

        void sign(SSLParams signer) throws Exception {
            signedCert = RsaSigner.signCert(signer.pair.getPrivateKey(),
                    signer.pair.getCertificate(), certRequest, new Date(), 365, 1234);
            signedKeyStore = createKeyStore(alias, pair.getPrivateKey(),
                    signedCert, signer.cert);
            signedContext = SSLContexts.create(signedKeyStore, pass,
                    signer.trustStore);
            signedTrustStore = createTrustStore(alias, signedCert);
            signedKeyStore.store(createOutputStream(alias), pass);
            signedTrustStore.store(createOutputStream(alias + ".trust"), pass);
        }        
    }
    
    public LocalCaTest() {
    }

    @Test
    public void test() throws Exception {
        init();
        testRevocation(server.keyStore, server.trustStore, client.signedKeyStore, 
                client.signedTrustStore, client.cert);
        
    }

    private void init() throws Exception {
        ca.init();
        server.init();
        server.sign(ca);
        client.init();
        client.sign(server);
    }
    private FileOutputStream createOutputStream(String alias) throws IOException {
        return new FileOutputStream(File.createTempFile(alias, "jks"));
    }

    private void testRevocation(KeyStore serverKeyStore, KeyStore serverTrustStore, 
            KeyStore clientKeyStore, KeyStore clientTrustStore, 
            X509Certificate revokedCert) throws GeneralSecurityException {
        Set<BigInteger> revokedSerialNumbers = new ConcurrentSkipListSet();
        SSLContext serverSSLContext = RevocableSSLContexts.createRevokedSerialNumbers(
                serverKeyStore, pass, serverTrustStore, revokedSerialNumbers);
        SSLContext clientSSLContext = SSLContexts.create(clientKeyStore, pass, clientTrustStore);
        new ServerThread(serverSSLContext, port).start();
        Assert.assertTrue(testClientConnection(clientSSLContext));
        revokedSerialNumbers.add(revokedCert.getSerialNumber());
        Assert.assertFalse(testClientConnection(clientSSLContext));        
    }
    
    private boolean testClientConnection(SSLContext sslContext) {
        try {
            connect(sslContext, "localhost", port);
            return true;
        } catch (Exception e) {
            logger.warn(e);
            return false;
        }
    }
    
    private void accept(SSLContext sslContext, int port) 
            throws GeneralSecurityException, IOException {
        SSLServerSocket serverSocket = (SSLServerSocket) sslContext.
                getServerSocketFactory().createServerSocket(port);
        serverSocket.setNeedClientAuth(true);
        Socket socket = serverSocket.accept();
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            Assert.assertEquals("clienthello", dis.readUTF());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("serverhello");
        } finally {
            socket.close();
        }
    }

    private void connect(SSLContext context, String host, int port) 
            throws GeneralSecurityException, IOException {
        SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket(host, port);
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("clienthello");
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            Assert.assertEquals("serverhello", dis.readUTF());
        } finally {
            socket.close();            
        }
    }
    
    private KeyStore createKeyStore(String keyAlias, GenRsaPair keyPair) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        X509Certificate[] chain = new X509Certificate[]{keyPair.getCertificate()};
        keyStore.setKeyEntry(keyAlias, keyPair.getPrivateKey(), pass, chain);
        return keyStore;
    }

    private KeyStore createTrustStore(String alias, X509Certificate cert) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry(alias, cert);
        return keyStore;
    }

    private KeyStore createKeyStore(String alias, PrivateKey privateKey,
            X509Certificate signed, X509Certificate ca) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);        
        keyStore.setCertificateEntry("ca", ca);
        X509Certificate[] chain = new X509Certificate[] {signed, ca};
        keyStore.setKeyEntry(alias, privateKey, pass, chain);
        return keyStore;
    }
}
