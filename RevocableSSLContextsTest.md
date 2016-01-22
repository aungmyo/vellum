<h1>Simple Local CA</h1>

<h4>Overview</h4>

Imagine we have innumerable clients connecting to a Java server over SSL sockets.

Naturally, we generate certificates for each client, and can import these self-signed into our server truststore.  Alternatively we can sign the client certificates using a local CA cert.

<h4>Unit test</h4>

Our unit test creates keystores, <tt>SSLContext</tt>'s for client and server, and tests an SSL connection.
```
public class RevocableClientTrustManagerTest {
    ...
    @Test
    public void test() throws Exception {
        serverPair = new GenRsaPair();
        serverPair.generate("CN=server", new Date(), 365);
        serverCert = serverPair.getCertificate();
        serverKeyStore = createKeyStore("server", serverPair);        
        clientPair = new GenRsaPair();
        clientPair.generate("CN=client", new Date(), 365);
        clientKeyStore = createKeyStore("client", clientPair);
        clientCert = (X509Certificate) clientKeyStore.getCertificate("client");
        serverContext = SSLContexts.create(serverKeyStore, pass, clientKeyStore);
        clientContext = SSLContexts.create(clientKeyStore, pass, serverKeyStore);
        testConnection(serverContext, clientContext);
    }
```


```
   private Exception testConnection(SSLContext serverContext, SSLContext clientContext)
            throws Exception {
        ServerThread serverThread = new ServerThread(serverContext);
        ClientThread clientThread = new ClientThread(clientContext);
        serverThread.start();
        clientThread.start();
        clientThread.join(1000);
        serverThread.join(1000);
        if (serverThread.exception != null) {
            return serverThread.exception;
        }
        if (clientThread.exception != null) {
            return clientThread.exception;
        }
        return null;
    }
}
```

The server thread creates an <tt>SSLServerSocket</tt> as follows.
```
    class ServerThread extends Thread {
        SSLContext sslContext;
        Exception exception;

        public ServerThread(SSLContext sslContext) {
            this.sslContext = sslContext;
        }

        @Override
        public void run() {
            SSLServerSocket serverSocket = null;
            SSLSocket clientSocket = null;
            try {
                serverSocket = (SSLServerSocket) sslContext.getServerSocketFactory().
                        createServerSocket(port);
                serverSocket.setNeedClientAuth(true);
                clientSocket = (SSLSocket) serverSocket.accept();
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                Assert.assertEquals("clienthello", dis.readUTF());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                dos.writeUTF("serverhello");
                clientSocket.close();
                serverSocket.close();
                Thread.sleep(500);
            } catch (Exception e) {
                exception = e;
                close(clientSocket);
                close(serverSocket);
            }
        }
    }

```

The client thread connects to our server socket as follows.
```
    class ClientThread extends Thread {
        SSLContext sslContext;
        Exception exception;

        public ClientThread(SSLContext sslContext) {
            this.sslContext = sslContext;
        }

        @Override
        public void run() {
            SSLSocket clientSocket = null;
            try {
                Thread.sleep(500);
                clientSocket = (SSLSocket) sslContext.getSocketFactory().
                        createSocket("localhost", port);
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                dos.writeUTF("clienthello");
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                Assert.assertEquals("serverhello", dis.readUTF());
                clientSocket.close();
            } catch (Exception e) {
                exception = e;
                close(clientSocket);
            }
        }
    }
```

Similarly to the standard <tt>-Dnet.javax.ssl.keyStore</tt> <i>et al</i> command-line options used to specify the default keystore and truststore for SSL sockets, and in order to avoid any potential conflict with those, we use <tt>-Dcrocserver.ssl.keyStore</tt> <i>et al</i>. These properties are used to create an <tt>SSLContext</tt> as follows.
```
public class SSLContexts {    

    public static SSLContext create(boolean strict, String sslPrefix, Properties properties,
            MockableConsole console) throws Exception {
        ExtendedProperties props = new ExtendedProperties(properties);
        sslPrefix = props.getString(sslPrefix, sslPrefix);
        String keyStoreLocation = props.getString(sslPrefix + ".keyStore");
        if (keyStoreLocation == null) {
            throw new Exception("Missing keystore property for " + sslPrefix);
        }
        char[] pass = props.getPassword(sslPrefix + ".pass", null);
        if (pass == null) {
            pass = console.readPassword("Enter passphrase for %s: ", sslPrefix);
        }
        String trustStoreLocation = props.getString(sslPrefix + ".trustStore", 
                keyStoreLocation);
        if (strict && keyStoreLocation.equals(trustStoreLocation)) {
            throw new KeyStoreException("Require separate truststore");
        }
        SSLContext sslContext = create(keyStoreLocation, pass, trustStoreLocation);
        Arrays.fill(pass, (char) 0);
        return sslContext;
    }
    ...
}
```
where we reuse the same password for the SSL keystore, its private key, and the truststore. This password can specified on the command-line e.g. for automated test scripts, but otherwise we prompt for the SSL keystore password to be entered on the console. Moreover, we have introduced a <tt>MockableConsole</tt> for the benefit of automated unit testing.

Note that the truststore is defaulted to the specified private keystore, which is usually only OK on the client side, when using self-signed certificates. When using CA-signed certificates, our keystore must contain the certificate chain including the CA certs e.g. intermediate and root. However, these cannot be in our truststore, otherwise we trust any cert signed by that CA!

Ordinarily we create the <tt>SSLContext</tt> as follows.
```
    public static SSLContext create(KeyStore keyStore, char[] keyPassword,
            KeyStore trustStore) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyPassword);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
```

<h4><tt>RevocableSSLContexts</tt></h4>

On a related note, in a further article we'll implement a custom <tt>X509TrustManager</tt> to support local certificate revocation.

```
public class RevocableSSLContexts {
    ...
    public static SSLContext create(KeyStore keyStore, char[] keyPass,
            KeyStore trustStore, List<String> revocationList) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyPass);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager revocableTrustManager = new RevocableClientTrustManager(
                getPrivateKeyCertificate(keyStore),
                getX509TrustManager(trustStore),
                revocationList);
        sslContext.init(keyManagerFactory.getKeyManagers(),
                new TrustManager[]{revocableTrustManager},
                new SecureRandom());
        return sslContext;
    }
}
```

This enables a simple "local CA" where the server cert is also our root CA cert used to sign client certs. However, we do not recommend such an approach here, where we want tightly managed access control, which is easy to review.
```
public class RevocableClientTrustManager implements X509TrustManager {
    X509Certificate serverCertificate;
    X509TrustManager delegate;
    Collection<String> revokedCNList;
    ...
    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) 
            throws CertificateException {
        if (!certs[0].getIssuerX500Principal().equals(
                serverCertificate.getSubjectX500Principal())) {
            throw new CertificateException("Untrusted issuer");
        }
        if (!Arrays.equals(certs[1].getPublicKey().getEncoded(),
                serverCertificate.getPublicKey().getEncoded())) {
            throw new CertificateException("Invalid server certificate");
        }
        if (revokedCNList.contains(getCN(certs[0].getSubjectDN()))) {
            throw new CertificateException("Certificate CN revoked");
        }
        delegate.checkClientTrusted(certs, authType);
    }
}
```
where we initialise this trust manager with a collection of revoked certificates' CNs or serial numbers. We check that the client certificate is signed by our server certificate, and not revoked. Finally, we delegate to the standard <tt>X509TrustManager</tt> for good measure.

<h4><tt>keytool</tt></h4>

Naturally we use <tt>keytool</tt> to create our private SSL keystore, e.g. as required by <tt>DualControlConsole</tt>, specified by our <tt>crocserver.ssl.keyStore</tt> property.
```
$ keytool -keystore evanx.jks -genkeypair -keyalg rsa -keysize 2048 -validity 365 -alias evanx \
    -dname "CN=evanx, OU=test"
```
We export our certificate as follows.
```
$ keytool -keystore evanx.jks -alias evanx -exportcert -rfc
```
We cut and paste the exported PEM text into a file, which we can inspect using <tt>openssl</tt> as follows.
```
$ openssl x509 -text -in evanx.pem
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number: 1380030508 (0x5241982c)
    Signature Algorithm: sha1WithRSAEncryption
        Issuer: CN=evanx, OU=test
        Validity
            Not Before: Sep 24 13:48:28 2013 GMT
            Not After : Sep 24 13:48:28 2014 GMT
        Subject: CN=evanx, OU=test
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
                Public-Key: (2048 bit)
```
We import the cert into the server SSL truststore as required by <tt>DualControlManager</tt> on behalf of <tt>DualControlGenSecKey</tt> and our app.
```
$ keytool -keystore crocserver.trust.jks -alias evanx -importcert -file evanx.pem
```

Similarly, the server cert is imported into the custodians' truststores as specified by <tt>crocserver.ssl.trustStore</tt> for <tt>DualControlConsole</tt>.

<h4>Client certificate signing</h4>

Incidently, we could export an CSR, and sign this with the server cert. In this case our server is a local CA, which must support certificate revocation e.g. using that <tt>RecovableClientTrustManager</tt> introduced further above.
```
$ keytool -keystore evanx.jks -alias evanx -certreq
```
We use Java7's keytool to sign the CSR.
```
$ keytool -keystore crocserver.jks -gencert -validity 365 -rfc \
    -dname "CN=evanx, OU=test" -infile evanx.csr -outfile evanx.signed.pem
```
Note that <tt>-gencert</tt> is not available in Java6's keytool.

We inspect the cert using <tt>openssl</tt>.
```
$ openssl x509 -text -in evanx.signed.pem | grep CN
        Issuer: CN=crocserver, OU=test
        Subject: CN=evanx, OU=test
```
Since our keystore requires our cert chain to be imported in the correct order starting with the root cert, we import the server cert first, and then our signed cert.
```
$ keytool -keystore evanx.jks -importcert -noprompt \
    -file crocserver.pem -alias crocserver 
Enter keystore password:  
Certificate was added to keystore

$ keytool -keystore evanx.jks -importcert -noprompt \
    -file evanx.signed.pem  -alias evanx
Enter keystore password:  
Certificate reply was installed in keystore
```

Our client keystore can double up as our truststore since it contains the server cert as the root of its certificate chain. If our server cert is CA-signed, we can't do that, since our keystore certificate chain would include the CA's root cert, and so we would trust <i>any</i> cert signed by that CA.

Our server keystore can also double up as its truststore, since it contains its cert which signs our client certs.

<h4>Client certificate management</h4>

Clearly a rogue certificate with a duplicate CN can impersonate a valid certificate. So when issuing a certificate, we must take care to ensure the uniqueness of the CN, and add the certificate, or at least its unique identifier, to a registry of some sort.

If a certificate is signed but not recorded, or its record is deleted, our server is forever vulnerable to that rogue certificate. In order to regularly review access, we require a perfect record of all issued certificates. We might record our signed certs into a keystore file as follows.
```
$ keytool -keystore crocserver.issued.jks -importcert -alias evanx -file evanx.pem 
Certificate was added to keystore
```
where this is not a truststore per se, but just a database of issued certificates.

Interestingly, in trying to avoid a truststore containing all our client certificates, we have nevertheless ended up with one!

All told, given the risk of a rogue certificate, we recommend sticking with self-signed client certificates, explicitly imported into our server truststore. Crucially, its keystore must not be used as its truststore, since the keystore naturally contains the server certificate, and so a rogue certificate can be created by signing it with the server key, e.g. using <tt>keytool -gencert</tt>, as illustrated above.

<h4>Conclusion</h4>


<h4>Furthermore</h4>


<tt><a href='https://twitter.com/evanxsummers'>@evanxsummers</a></tt>