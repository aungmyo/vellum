<h4>Overview</h4>

Say we have a customer that deploys hundreds of managed point-of-sale devices that connect to our private Java server to perform online transactions. You propose client-authenticated SSL sockets. The question is, should we buy CA-signed certificates for each device, or use self-signed certificates?

<img src='http://jroller.com/evanx/resource/gnome-keys-250.png' align='left' />

CA-signed certificates are typically used for server authentication. Certainly we'll buy a CA certificate for our production server. However, we are primarily concerned with client authentication. Down the line, we want to automate the deployment and enrollment of new devices, without any intervening manual process, such as stopping to buy a certificate on the company's credit card!

Naturally each client generates a private key. We could import all each client's self-signed certificate into our server "truststore." But as an experiment, let's setup our own CA, and sign the client certificates with our local CA key.

We'll examine the security implications, and propose measures to mitigate risks. In the course of this exercise, we'll learn about <tt>keytool</tt>, and understand Java SSL better. Finally, we must decide if local CA signing is preferrable to self-signed certificates for client authentication.


<h4>SSLContext</h4>

Naturally an <tt>SSLServerSocket</tt> is created using an <tt>SSLContext</tt>, which we create with a keystore and a truststore, as follows.

```
public class LocalCaTest {
    ...
    static void accept(KeyStore keyStore, char[] keyPassword, KeyStore trustStore,
            int port) throws GeneralSecurityException, IOException {
        SSLContext sslContext = SSLContexts.create(keyStore, keyPassword, trustStore);
        SSLServerSocket serverSocket = (SSLServerSocket) sslContext.
                getServerSocketFactory().createServerSocket(port);
        try {
            serverSocket.setNeedClientAuth(true);
            handle(serverSocket.accept());
        } finally {
            serverSocket.close();
        }
    }    
}
```

where we create an SSL context for our server socket, and accept a client connection. We'll present our <tt>SSLContexts</tt> utility further below.

For the purpose of SSL, the keystore must contain an asymmetric private key, which is paired with its public key certificate. Moreover, the keystore must contain the certificate chain of that key certificate, through to its root certificate (which is self-signed by definition). In fact, <tt>keytool</tt> will not allow a signed certificate to be imported unless its parent certificate chain is already present in the keystore.

The truststore contains peer or CA certificates that we trust. By definition we trust any peer certificate chain that includes a certificate in our truststore. That is to say, if our truststore contains a CA certificate, then we trust all certificates issued by that CA.

Note that since the keystore must contain the certificate chain of the key certificate, and the truststore needn't include the certificate chains of trusted certificates, they differ critically in this respect, and so the keystore should not be misused as the truststore.


<h4>Root CA certificate</h4>

Let's first inspect a <a href='https://certs.godaddy.com/anonymous/repository.pki'>GoDaddy</a> root certificate, for example.
```
$ openssl x509 -text -in godaddy-root.pem 
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number: 0 (0x0)
    Signature Algorithm: sha256WithRSAEncryption
        Issuer: C=US, ..., CN=Go Daddy Root Certificate Authority - G2
        Validity
            Not Before: Sep  1 00:00:00 2009 GMT
            Not After : Dec 31 23:59:59 2037 GMT
        Subject: C=US, ..., CN=Go Daddy Root Certificate Authority - G2
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
                Public-Key: (2048 bit)
```
We observe that the following "Basic Constraints" and "Key Usage" extensions are set.
```
        X509v3 extensions:
            X509v3 Basic Constraints: critical
                CA:TRUE
            X509v3 Key Usage: critical
                Certificate Sign, CRL Sign
```
where the "Basic Constraints" indicates if this a CA certificate, i.e. it is a root or intermediate certificate belonging to a CA. It will therefore likely be used for signing certificates, revocation lists, code, or email. The "Key Usage" of the above certificate indicates that it can be used to sign certificates, and certificate revocation lists. These usage limitations are considered when validating a certificate chain i.e. during an SSL handshake.

Alternatively we import the certificate into a keystore, and the print certificate using <tt>keytool</tt>.
```
$ keytool -keystore godaddy.jks -alias godaddy-root -list -v
...
Extensions: 

#1: ObjectId: 2.5.29.19 Criticality=true
BasicConstraints:[
  CA:true
  PathLen:2147483647
]

#2: ObjectId: 2.5.29.15 Criticality=true
KeyUsage [
  Key_CertSign
  Crl_Sign
]
```
where the path length of certificate chains that can be validated is set to the maximum for a signed integer. Certificates are stored in <a href='http://en.wikipedia.org/wiki/Distinguished_Encoding_Rules#DER_encoding'>DER</a> format, i.e. using "Distinguished Encoding Rules" where a field can have "Object ID" e.g. "2.5.29.19" is used for "Basic Constraints."


<h4>Local CA key</h4>

While our server naturally resides in a DMZ accessible to the Internet, our CA key should be isolated on a more secure internal machine. In fact, our root CA key could be generated offline, where it can never be compromised. In this case, an intermediate CA key would be used for online signing. We would transfer the intermediate CA's certificate signing request to the offline CA server, and return its signed certificate, using removable USB media.

We create our "local CA" key as follows.
```
$ keytool -keystore ca.jks -genkeypair -alias ca -dname "CN=ca" \
    -keyalg rsa -keysize 2048 -validity 365 -noprompt \
    -ext BasicConstraints:critical=CA:true,pathlen:2 \
    -ext KeyUsage:critical=keyCertSign,cRLSign
```

Incidently, our testing using Java SSL sockets indicates that these CA constraints are ignored for root certificates. For example, even setting <tt>CA:false</tt> on our CA certificate, we are nevertheless able to sign certificates, and use those for SSL connections. This means that self-signed server and client keys can be used to issue rogue certificates, which is somewhat disconcerting.

Having created our keystores and truststores using <tt>keytool</tt>, where both our client and server are signed by our local CA, we test them for SSL sockets as follows.
```
    private void connect(String serverKeyStoreLocation, String serverTrustStoreLocation,
            String clientKeyStoreLocation, String clientTrustStoreLocation,
            char[] pass) throws Exception {
        SSLContext serverSSLContext = SSLContexts.create(
                serverKeyStoreLocation, pass, serverTrustStoreLocation);
        SSLContext clientSSLContext = SSLContexts.create(
                clientKeyStoreLocation, pass, clientTrustStoreLocation);        
        ServerThread serverThread = new ServerThread();
        try {
            serverThread.start(serverSSLContext, PORT);
            Assert.assertNull(connect(clientSSLContext, PORT));
            Assert.assertNull(serverThread.getErrorMessage());
        } finally {
            serverThread.close();
            serverThread.join(1000);
        }        
    }
```
where we create SSL contexts for the server socket and the client, using the provided keystores and truststores, and try to connect. We test the client connection to the server socket as follows.
```
    static String connect(SSLContext context, int port) 
            throws GeneralSecurityException, IOException {
        SSLSocket socket = (SSLSocket) context.getSocketFactory().
                createSocket("localhost", port);
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("clienthello");
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            Assert.assertEquals("serverhello", dis.readUTF());
            return null;
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            socket.close();
        }
    }
```
where we return an error message, otherwise <tt>null</tt> which indicates success.


<h4>Server certificate signing</h4>

We create a keystore containing a private key and its self-signed cert (for starters) using <tt>keytool -genkeypair</tt>.
```
$ keytool -keystore server.jks -genkeypair -alias server -dname "CN=server.com" \
  -keyalg rsa -keysize 2048 -validity 365 -noprompt
```
where we specify a validity period of 365 days.

Naturally the common name of a server certificate is its domain name. This is validated by the client e.g. the browser, that the domain name of the server to which we connect, matches its certificate's common name.

We export a certificate signing request (CSR) as follows.
```
$ keytool -keystore server.jks -alias server -certreq -rfc -file server.csr
```

We can sign the CSR using using Java7's <tt>keytool -gencert</tt>.
```
$ keytool -keystore ca.jks -alias ca -gencert -infile server.csr -dname "CN=server.com" \
    -validity 365 -rfc -outfile server.signed.pem \
    -ext BasicConstraints:critical=ca:false,pathlen:0 \
    -ext KeyUsage:critical=keyEncipherment \
    -ext ExtendedKeyUsage:critical=serverAuth
```
where we set the X509v3 extensions to restrict the key usage for good measure, as we see for certificates we buy from a public CA.

We now wish to import this signed certificate reply into our server keystore. As mentioned earlier, <tt>keytool</tt> will not allow a signed certificate to be imported unless its parent certificate chain is already present in the keystore.
```
$ keytool -keystore server.jks -alias server -importcert -file server.signed.pem
Enter keystore password:
keytool error: java.lang.Exception: Failed to establish chain from reply
```
where the signed certificate is a so-called "reply" to its certificate signing request.

So we must import the certificate chain in order starting with the root certificate.
```
$ keytool -keystore server.jks -alias ca -importcert -file ca.pem
```
An "intermediate" certificate would be next, if we have one. Finally we import the signed certificate reply.
```
$ keytool -keystore server.jks -alias server -importcert -file server.signed.pem
```


<h4>Certificate chain</h4>

We can list the certicate chain as follows.
```
$ keytool -keystore server.jks -alias server -list -v
...
Certificate chain length: 2

Certificate[1]:
Owner: CN=server.com
Issuer: CN=ca

Certificate[2]:
Owner: CN=ca
Issuer: CN=ca
```
where we have signed our server certificate with our local CA root certificate.

Note that the first certificate of the chain is our key certificate, and the last certificate is the root CA certificate. By definition the "root" certificate of a chain is self-signed. Additionally, might have an "intermediate" CA certificate that issues the key certificate.


<h4>Server truststore</h4>

Our server truststore should contain the CA certificate which signs our client certficates.
```
$ keytool -keystore server.trust.jks -alias ca -importcert -noprompt -file ca.pem
```

If we use an intermediate CA certificate to sign our client certificates, then that would be imported into the truststore rather than the root CA certificate.

Note that by definition, any remote peer certificate whose chain contains a certificate in the truststore, is trusted. Therefore since each client certificate chain contains this trusted certificate (as it root certificate), it will be trusted. In fact, any certificate issued by our CA is trusted, according to this truststore.

<h4>openssl</h4>

We can use <tt>openssl</tt> to connect to the <tt>SSLServerSocket</tt> and inspect its key certificate chain as follows.
```
$ openssl s_client -connect localhost:4444
...
Certificate chain
 0 s:/CN=server.com
   i:/CN=ca
 1 s:/CN=ca
   i:/CN=ca
...
Acceptable client certificate CA names
/CN=ca
```
where the subject and issuer of the two certificates in our key certificate chain are listed, and our server will accept client certificates whose chain includes our CA certificate, i.e. as its root certificate.

This demonstrates why the keystore requires a certificate chain, i.e. to send to the peer for validation. The peer validates the chain, and checks it against our trusted certificates. It stops checking as soon as it encounters a certificate in the chain that it trusts. Therefore the chain for a trusted certificate need not be stored in the truststore, and actually must not be, otherwise we trust <i>any</i> certificate issued by that trusted certificate's root, irrespective of the trusted certificate itself. For example, if our clients must trust only our server, whose certificate happens to be issued by GoDaddy, we certainly don't want those clients to trust any server with a certificate issued by GoDaddy.


<h4>Server-specific CA certificate</h4>

In practice we might have multiple servers with different sets of clients, where we generate a CA certificate that is used to sign client certificates for a specific server.

```
$ keytool -keystore server-ca.jks -genkeypair -noprompt -keyalg rsa -keysize 2048 \
    -alias server-ca -dname "CN=server-ca" -validity 365     
```
where the server's truststore would contain this CA certificate.

While it is possible to sign client certificates with the server key as its own CA, this key resides in the DMZ, which is our least secure zone. If compromised, the key can be used to issue rogue certificates. Therefore we choose not to store CA keys in our DMZ.


<h4>Intermediate CA certificate</h4>

We might keep our root CA key offline where it cannot possibly be compromised. In this case, we generate an intermediate CA key, where its CSR and certificate reply is transferred via removable USB media between our online intermediate CA server, and our offline root CA server.

Although the intermediate CA key is then used for certificate signing, our server and client truststores still contain the root CA certificate. However, their keystores contain their key certificate chain, which includes the intermediate CA certificate.

```
$ openssl s_client -connect localhost:4444
...
Certificate chain
 0 s:/CN=server.com
   i:/CN=ca-intermediate-2013-10
 1 s:/CN=ca-intermediate-2013-10
   i:/CN=ca
 2 s:/CN=ca
   i:/CN=ca
...
Acceptable client certificate CA names
/CN=ca
```

In the event that an intermediate keystore is compromised, then it's certificate should be revoked, e.g. where our root CA certificate includes a URL for its certificate revocation list. In this case, our clients can reject servers signed with that compromised intermediate certificate.

Clearly we would have to generate a new intermediate CA key, and re-sign all our certificates. To avoid such a burden, one might prefer self-signed client certificates.


<h4>Serial numbers</h4>

Incidently, <a href='http://www.docjar.org/html/api/sun/security/tools/KeyTool.java.html'><tt>KeyTool.java</tt></a>, as used by the <a href='http://docs.oracle.com/javase/7/docs/technotes/tools/solaris/keytool.html'><tt>keytool</tt></a> command-line utility, generates a random serial number as follows.
```
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(
            new java.util.Random().nextInt() & 0x7fffffff));
```

We see that <tt><a href='http://www.docjar.org/html/api/java/security/cert/X509Certificate.java.html'>X509Certificate</a></tt> (implemented by <a href='http://www.docjar.org/html/api/sun/security/x509/X509CertImpl.java.html'><tt>X509CertImpl</tt></a>) returns a <tt>BigInteger</tt> for the certificate serial number, which should be unique, and can be used for revocation.

In a later article, we might build a local CA management tool, which uses a sequence number for certificates which it generates programmatically and records in an SQL database.


<h4>Local revocation</h4>

Ordinarily we create an <tt>SSLContext</tt> as follows.
```
public class SSLContexts {
    ...
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
}
```
where we provide the keystore and truststore for this SSL context, which can be used to create an <tt>SSLServerSocket</tt>, or client <tt>SSLSocket</tt>.

Since we wish to support a local CA for client certificates, we should enable local certificate revocation in order to retain access control. For convenience, we'll revoke certificates by their common name (CN), which must be unique.

So we create an <tt>SSLContext</tt> with a set of revoked names, as follows.
```
public class RevocableNameSSLContexts {
    ...
    public static SSLContext create(KeyStore keyStore, char[] keyPass,
            KeyStore trustStore, Set<String> revokedNames) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyPass);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager revocableTrustManager = new RevocableTrustManager(
                KeyStores.findSoleTrustedCertificate(trustStore),
                KeyStores.findX509TrustManager(trustStore),
                revokedNames);
        sslContext.init(keyManagerFactory.getKeyManagers(),
                new TrustManager[] {revocableTrustManager},
                new SecureRandom());
        return sslContext;
    }
}
```
where we initialise a custom <tt>RevocableTrustManager</tt> with our client certificate issuer and a set of revoked names.
```
public class RevocableTrustManager implements X509TrustManager {
    X509Certificate issuerCertificate;
    X509TrustManager delegate;
    Set<String> revokedNames;
    ...
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) 
            throws CertificateException {
        if (chain.length < 2) {
            throw new CertificateException("Invalid cert chain length");
        }
        if (!chain[0].getIssuerX500Principal().equals(
                issuerCertificate.getSubjectX500Principal())) {
            throw new CertificateException("Untrusted issuer");
        }
        if (!Arrays.equals(chain[1].getPublicKey().getEncoded(),
                issuerCertificate.getPublicKey().getEncoded())) {
            throw new CertificateException("Untrusted issuer public key");
        }
        if (revokedNames.contains(Certificates.getCN(chain[0].getSubjectDN()))) {
            throw new CertificateException("Certificate CN revoked");
        }
        delegate.checkClientTrusted(chain, authType);
    }
}
```
where we check that the client certificate is issued by our CA, and not revoked. Finally, we delegate to the standard <a href='http://www.docjar.org/html/api/javax/net/ssl/X509TrustManager.java.html'><tt>X509TrustManager</tt></a> (implemented by <a href='http://www.docjar.org/html/api/sun/security/ssl/X509TrustManagerImpl.java.html'><tt>X509TrustManagerImpl</tt></a>) for good measure, e.g. to validate the certificate chain, expiry dates and what not (using <a href='http://www.docjar.org/html/api/sun/security/validator/PKIXValidator.java.html'><tt>PKIXValidator</tt></a>).

Since we use this trust manager on the server to validate clients, we know its <tt>checkServerTrusted()</tt> method is not needed.
```
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) 
            throws CertificateException {
        throw new CertificateException("Server authentication not supported");
    }    
```

<h4>Dynamic revocation</h4>

The set of revoked certificates' identifiers might be read from a file, URL or database. This could be a <tt>synchronized</tt> <tt>Set</tt> that can be updated concurrently, and so enables a dynamic truststore, which we test as follows.
```
public class LocalCaTest {
    ...
    private void testDynamicNameRevocation(KeyStore serverKeyStore, KeyStore serverTrustStore,
            KeyStore clientKeyStore, KeyStore clientTrustStore, String revokedName) 
            throws Exception {
        Set<String> revokedNames = new ConcurrentSkipListSet();
        SSLContext serverSSLContext = RevocableNameSSLContexts.create(
                serverKeyStore, pass, serverTrustStore, revokedNames);
        SSLContext clientSSLContext = SSLContexts.create(clientKeyStore, pass, clientTrustStore);
        ServerThread serverThread = new ServerThread();
        try {
            serverThread.start(serverSSLContext, port, 2);
            Assert.assertNull(connect(clientSSLContext, port));
            Assert.assertNull(serverThread.getErrorMessage());
            revokedNames.add(revokedName);
            Thread.sleep(1000);
            Assert.assertNotNull(connect(clientSSLContext, port));
            Assert.assertNotNull(serverThread.getErrorMessage());
        } finally {
            serverThread.close();
            serverThread.join(1000);
        }
    }
    ...
```
where we create a concurrent <tt>Set</tt> for the revoked certificates, and revoke a client certificate after the server has been started. Before the certificate is revoked, the connection should succeed, and afterwards it should fail.

Incidently, we find that the SSL session is cached, and so we must either recreate the client <tt>SSLContext</tt> instance, or set a session timeout on the client socket. We set the minimum timeout of 1 second, and we sleep for the same. This forces re-authentication by the server, so that our trust manager is actually invoked and rejects our newly revoked certificate.

```
public class ServerThread extends Thread {
    ...
    static void handle(SSLSocket socket) throws IOException {
        logger.info("handle: " + socket.getSession().getPeerPrincipal());
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            Assert.assertEquals("clienthello", dis.readUTF());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("serverhello");
            logger.info("ok");
        } finally {
            socket.getSession().getSessionContext().setSessionTimeout(1);
            socket.close();
        }
    }
}
```


<h4>Client keystore</h4>

Let's create the private keystore for a test client..
```
$ keytool -keystore client.jks -genkeypair -keyalg rsa -keysize 2048 -validity 365 \
    -alias client -dname "CN=client"
```
We print our certificate as PEM text using <tt>-rfc</tt>.
```
$ keytool -keystore client.jks -alias client -exportcert -rfc
----BEGIN CERTIFICATE-----
MIIDxTCCAq2gAwIBAgIBADANBgkqhkiG9w0BAQsFADCBgzELMAkGA1UEBhMCVVMx
...
```
We cut and paste the exported PEM text into a file, which we inspect using <tt>openssl</tt>.
```
$ openssl x509 -text -in client.pem
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number: 1380030508 (0x5241982c)
    Signature Algorithm: sha1WithRSAEncryption
        Issuer: CN=client
        Validity
            Not Before: Sep 24 13:48:28 2013 GMT
            Not After : Sep 24 13:48:28 2014 GMT
        Subject: CN=client
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
                Public-Key: (2048 bit)
...
```
We might import the client's self-signed certificate into our server truststore.
```
$ keytool -keystore server.trust.jks -alias client -importcert -file client.pem
```
Similarly, our server's certificate is imported into this client's truststore.


<h4>Client certificate signing</h4>

We export a certificate signing request (CSR) to be signed by our local CA.
```
$ keytool -keystore client.jks -alias client -certreq
```
We cut and paste the output PEM text into a CSR file, and use Java7's keytool to sign this CSR, using its <tt>-gencert</tt> option, which by the way is not available in earlier JRE's.
```
$ keytool -keystore ca.jks -gencert -validity 365 -rfc \
    -dname "CN=client" -infile client.csr -outfile client.signed.pem \
    -ext BasicConstraints:critical=ca:false,pathlen:0 \
    -ext KeyUsage:critical=digitalSignature \
    -ext ExtendedKeyUsage:critical=clientAuth
```
where we specify an unchanged name and validity period for the newly signed certificate. We might add some extensions for good measure, i.e. to restrict key usage.

We inspect the certificate using <tt>openssl</tt>.
```
$ openssl x509 -text -in client.signed.pem
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number: 448957773 (0x1ac28d4d)
    Signature Algorithm: sha256WithRSAEncryption
        Issuer: CN=ca
        Validity
            Not Before: Oct 16 20:02:24 2013 GMT
            Not After : Jul 11 20:02:24 2016 GMT
        Subject: CN=client
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
                Public-Key: (2048 bit)
...
```
We see that the X509v3 extensions are set as follows.
```

            X509v3 Basic Constraints: critical
                CA:FALSE
            X509v3 Extended Key Usage: critical
                TLS Web Client Authentication
            X509v3 Key Usage: critical
                Digital Signature
```
We compare these settings to a certificate bought from GoDaddy.com, for example.
```
        X509v3 extensions:
            X509v3 Basic Constraints: critical
                CA:FALSE
            X509v3 Extended Key Usage: 
                TLS Web Server Authentication, TLS Web Client Authentication
            X509v3 Key Usage: critical
                Digital Signature, Key Encipherment
```
where our tests indicate that the "Digital Signature" usage is required for client authentication, and "Key Encipherment" is required for server authentication.

Since the keystore requires its key certificate chain to be imported in the correct order starting with the root cert, we import the CA cert first, and then our signed cert.
```
$ keytool -keystore client.jks -importcert -noprompt \
    -file ca.pem -alias ca
Enter keystore password: 
Certificate was added to keystore

$ keytool -keystore client.jks -importcert -noprompt \
    -file client.signed.pem -alias client
Enter keystore password: 
Certificate reply was installed in keystore
```
Otherwise if the parent chain is not present, we're balked by the following <tt>keytool</tt> error.
```
keytool error: java.lang.Exception: Failed to establish chain from reply
```

<img src='http://jroller.com/evanx/resource/wooden-shield200.png' align='right' />

Incidently, in a follow-up article we will create keystores programmatically for our unit tests, taking cues from <a href='http://www.docjar.org/html/api/sun/security/tools/KeyTool.java.html'><tt>KeyTool.java</tt></a>, to emulate the manual procedure presented here.


<h4>Client certificate management</h4>

Clearly a certificate with a duplicate CN impersonates the original certificate with that CN. So when issuing a client certificate, we must take care to ensure the uniqueness of its CN, and should add the certificate (or at least its unique identifier) to a registry of some sort.

In order to review access, we clearly require a perfect record of all issued certificates. If a certificate is signed but not recorded, or its record is deleted, our server is forever vulnerable to that rogue certificate.

We might record our signed certificates into a keystore file as follows.
```
$ keytool -keystore server.issued.jks -importcert -alias client -file client.pem 
Certificate was added to keystore
```
where this is not a truststore per se, but just a database of issued certificates.

Interestingly, we sign our client certificates to avoid having such a truststore containing all our client certificates, but nevertheless end up with one, which is telling.


<h4>Self-signed client certificates</h4>

If our local CA key is compromised by a breach, or abused by an administrator, rogue certificates can might be created. So we prefer self-signed client certificates which are explicitly imported into our server truststore where they can be reviewed.

However, self-signed client keys are effectively CA keys, and so rogue certificates can be created using compromised client keys, e.g. using <tt>keytool -gencert</tt>. So we use an explicit trust manager as below, where the key certificate must be in the truststore.
```
public class ExplicitTrustManager implements X509TrustManager {
    Map<String, X509Certificate> certificateMap = new HashMap();
    X509TrustManager delegate;
    
    public ExplicitTrustManager(KeyStore trustStore) 
        throws GeneralSecurityException {
        this.delegate = KeyStores.findX509TrustManager(trustStore);
        for (String alias : Collections.list(trustStore.aliases())) {
            certificateMap.put(alias, (X509Certificate) 
                    trustStore.getCertificate(alias));
        }
    }
    ...
```
where we build a map of the certificates in our truststore.

We validate peer certificate chains as follows.
```
    private void checkTrusted(X509Certificate[] chain) 
            throws CertificateException {
        X509Certificate trustedCertificate = certificateMap.get(
                X509Certificates.getCN(chain[0].getSubjectDN()));
        if (trustedCertificate == null) {
            throw new CertificateException("Untrusted peer certificate");
        }
        if (!Arrays.equals(chain[0].getPublicKey().getEncoded(),
                trustedCertificate.getPublicKey().getEncoded())) {
            throw new CertificateException("Invalid peer certificate");
        }
    }
```
where we check that the first cert in the chain sent by the peer, i.e. the key certificate, is contained in our map. It's chain is disregarded, and so our certificates can be self-signed.

We could use this trust manager equally on a client, as well as our server.
```
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) 
            throws CertificateException {
        checkTrusted(chain);
        delegate.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) 
            throws CertificateException {
        checkTrusted(chain);
        delegate.checkServerTrusted(chain, authType);
    }    
```


<h4>Furthermore</h4>

We'll delve into the unit testing of our custom trust manager in a follow-up article.

Thereafter, we might implement a local CA management tool that records issued certificates, and supports standard CRLs, if not a local CA server that supports the <a href='http://en.wikipedia.org/wiki/Online_Certificate_Status_Protocol'>Online Certificate Status Protocol</a>.


<h4>Conclusion</h4>

Naturally, the keystore must contain the certificate chain of the key certificate, including root CA certificate, and so we note that if the keystore is misused as a truststore, then <i>any</i> certificate issued by that CA would be trusted. If we are using a public CA, then we would trust any certiticate issued by that CA, not just our own.

We can sign client certificates using a local CA root certificate. However, we should then support certificate revocation, e.g. to disable certificates for devices that have gone amiss. So we introduce a custom SSL trust manager to support a local revocation list.

<img src='http://jroller.com/evanx/resource/Gnome-application-certificate-250-crop.png' align='right'>

Naturally, our SSL server resides in our DMZ, whereas we want to isolate our local CA on a secure internal server. Even so, we discuss the risk of rogue certificates signed by our CA, and argue that explicitly importing each client certificate into the truststore enables us to review access. In this case our client certificates needn't be signed by our local CA.<br>
<br>
If our CA certificate is compromised, we would need to re-sign all our client certificates. In order to avoid such a burden, one might prefer to use self-signed client certificates.<br>
<br>
We note that self-signed client certificates are effectively CA certificates, and so compromised client keys can be used to create rogue certificates. We present an explicit trust manager to check that the peer's key certificate is in the truststore, and disregard its chain.<br>
<br>
Public CA certificates are typically used for server authentication. However, we are primarily concerned with client authentication. We find that using a local CA to sign client certificates introduces operational burdens and security risks, and so we would prefer to use self-signed certificates, with the above-mentioned truststore.<br>
<br>
<br>
<h4>Moreover</h4>

In an upcoming article, We'll introduce a trust manager to automate certificate enrollment, e.g. new clients' certificates are imported into a dynamic truststore when they connect for the first time.<br>
<br>
<br>
<h4>Resources</h4>

You can browse the code for this exercise on <a href='https://code.google.com/p/vellum'>code.google.com/vellum</a>.<br>
<br>
<h4>Other reading</h4>

Also relating to Java crypto in this blog: <a href='https://weblogs.java.net/blog/evanx/archive/2013/01/24/password-salt'>Password Salt</a> for secure passwords; and leveraging the <a href='https://weblogs.java.net/blog/evanx/archive/2012/11/07/google-authenticator-thus-enabled'>Google Authenticator</a> mobile app for multi-factor authentication for your own sites.<br>
