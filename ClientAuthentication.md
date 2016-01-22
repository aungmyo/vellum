<h4>Problem overview</h4>

<img src='http://jroller.com/evanx/resource/wooden-shield200.png' align='right' />

Client-authenticated SSL connections require client certificate generation, enrollment, and renewal. Enrollment typically involves a manual process, either certificate signing, or importing into our truststore. Moreover certificate expiry requires this process to be repeated at some stage.

Consequently browser certificates are not widely used for consumer sites i.e. for client-authenticated HTTPS. Typically cookies are used to record authenticated sessions. These might be hijacked using <a href='http://en.wikipedia.org/wiki/Cross-site_scripting'>cross-site scripting</a> attacks, in order to mount a <a href='http://en.wikipedia.org/wiki/Spoofing_attack'>spoofing attack</a> as an authenticated client.

That aside, our application manages client devices connecting via SSL sockets, where we want to automate certificate management for operational reasons.


<h4>Solution overview</h4>

We implement a custom trust manager that authenticates existing clients, but also recognises new clients, and automatically imports their certificate into our truststore.

A new client is be pre-approved by an administrator according to its expected certificate name, and we automatically import its certificate the first time it connects. The risk is that a rogue client connects before the legitimate client. However, this requires timeous knowledge of the names of pending new clients. Moreover, our application might mitigate this risk by limiting the transactions of new clients until they have been manually confirmed.

Alternatively a new client can register without a certificate, where our application generates a key on its behalf. For example, our HTTPS server responds with a content type of <tt>application/x-pkcs12</tt>, to be imported by the client's browser.


<h4>SQL truststore</h4>

Our dynamic truststore might be an SQL database table with columns for the client certificate name, the certificate itself, and its status.
```
CREATE TABLE client_cert (
  cert_name VARCHAR(255) PRIMARY KEY,
  cert VARCHAR(4096), 
  enabled BOOL DEFAULT TRUE,
  expiry_date TIMESTAMP, 
  confirmed_date TIMESTAMP
);
```
where the certificate name is unique. Incidently, the certificate itself would be Base64-encoded for SQL storage. For convenience we might want an expiry date column, even though that information is in the certificate. For example, this might be used by a script for alerts of impending expiry dates.

<img src='http://upload.wikimedia.org/wikipedia/commons/4/40/Crystal_Clear_app_database.png' align='left' />

We might import certificates for new clients where these are disabled until validated by our application, and perhaps their access is restricted pending confirmation by an administrator. In this case, we might be vulnerable to a denial of service attack whereby our server is flooded with new clients. We could counter this limiting the number of new clients that are recorded from the same source IP number, limiting the rate in general, and enforcing a cutoff where we disable any new clients for some period.

We pre-approve a new client by inserting a record with its expected certificate name, but with a <tt>null</tt> entry for the actual certificate. The certificate is then set when the client connects for the first time. Initially, the <tt>expiry_date</tt> column is used to limit the enrollment period. This mitigates the risk of a rogue client, especially if the legitimate client never enrolls for some reason.

Incidently, we can "revoke" a client certificate by setting its <tt>enabled</tt> column to <tt>false</tt>. However in this case the certificate name cannot be reused, because of our uniqueness constraint.


<h4>Certificate storage</h4>

We provide a mockable certificate storage interface for our SQL truststore.
```
public interface CertificateStorage {
    public boolean contains(String commonName) throws CertificateStorageException;
    public void insert(String commonName, X509Certificate cert) throws CertificateStorageException;
    public boolean isNullCert(String commonName) throws CertificateStorageException;
    public void setCert(String commonName, X509Certificate cert) throws CertificateStorageException;
    public boolean isEnabled(String commonName) throws CertificateStorageException;
    public X509Certificate load(String commonName) throws CertificateStorageException;    
    public void update(String commonName, X509Certificate cert) throws CertificateStorageException;
}
```
where if our storage contains a record for that common name, but has a <tt>null</tt> certificate, then <tt>isNullCert()</tt> returns <tt>true</tt>, and we can invoke <tt>setCert()</tt> to set the certificate. If the certificate exists, then we can load it, and update it i.e. if it has expired.


<h4><tt>SSLSession</tt></h4>

When we accept a connection to our <tt>SSLServerSocket</tt>, naturally our application reads and validates the request message. At this stage, our application itself might import the peer certificate e.g. for new clients that send a valid registration message. Our application gets the client certificate via the <tt>SSLSession</tt> of the client <tt>SSLSocket</tt>, as demonstrated in the following test code.
```
    static void accept(KeyStore keyStore, char[] keyPassword, KeyStore trustStore,
            int port) throws GeneralSecurityException, IOException {
        SSLContext sslContext = SSLContexts.create(keyStore, keyPassword, trustStore);
        SSLServerSocket serverSocket = (SSLServerSocket) sslContext.
                getServerSocketFactory().createServerSocket(port);
        try {
            serverSocket.setNeedClientAuth(true);
            SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
            javax.security.cert.X509Certificate peer = 
                    clientSocket.getSession().getPeerCertificateChain()[0];
            logger.info("peer: " + peer.getSubjectDN().getName());
            ServerThread.handle(clientSocket);
        } finally {
            serverSocket.close();
        }
    }
```

In this case, our trust manager would allow connections by new clients whose certificates are not yet in our truststore, where these are imported by our application.

Alternatively our trust manager itself imports new certificates.


<h4>Trust manager</h4>

We implement a trust manager which uses our certificate storage.
```
public class StorageTrustManager implements X509TrustManager {
    final private CertificateStorage certificateStorage;
    private boolean allowWithoutCertificate;
    private boolean allowExpired;
    private boolean insertNew;
    private boolean updateExpired;
    private boolean setNull;
    ...    
    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) 
            throws CertificateException {
        throw new CertificateException("Server authentication not supported");
    }
}
```
where we use this trust manager on the server to validate clients, and so the <tt>checkServerTrusted()</tt> method is not implemented.

We might allow a client without a certificate to connect, e.g. to request a PKCS12 bundle to be generated on its behalf. Naturally in this case the server must not store the generated key, but does import the client certificate into its truststore. However, the certificate is perhaps initially disabled, pending manual confirmation by an administrator.

We check our client certificate chain as follows.
```
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) 
            throws CertificateException {
        if (chain.length == 0) {
            if (!allowWithoutCertificate) {
                throw new CertificateException("No certificate");
            }
        } else {
            try {
                X509Certificate peerCertificate = chain[0];
                if (!validate(Certificates.getCommonName(peerCertificate.getSubjectDN()),
                        peerCertificate)) {
                    throw new CertificateException("Certificate rejected");
                }
            } catch (CertificateStorageException e) {
                throw new CertificateException(e);
            }
        }
    }
```
where we consider only the peer certificate, and disregard its chain. (See the prequel article <a href='https://code.google.com/p/vellum/wiki/LocalCa'>Local CA</a>.)
```
    private boolean validate(String commonName, X509Certificate peerCertificate)
            throws CertificateStorageException, CertificateException {
        if (!certificateStorage.contains(commonName)) {
            if (insertNew) {
                certificateStorage.insert(commonName, peerCertificate);
                return true;
            }
        } else if (!certificateStorage.isEnabled(commonName)) {
            return false;
        } else if (certificateStorage.isNullCert(commonName)) {
            if (setNull) {
                certificateStorage.setCert(commonName, peerCertificate);
                return true;
            }
        } else {
            X509Certificate trustedCertificate = certificateStorage.load(commonName);
            if (Arrays.equals(peerCertificate.getPublicKey().getEncoded(),
                    trustedCertificate.getPublicKey().getEncoded())) {
                return allowExpired || !isExpired(trustedCertificate);
            } else if (updateExpired && isExpired(trustedCertificate)) {
                certificateStorage.update(commonName, peerCertificate);
                return true;
            }
        }
        return false;
    }
```
We check that we have a record for this common name. We ensure that it is <tt>enabled</tt> e.g. it is not pending confirmation or revoked. We import the certificate if we don't have one. Otherwise we check that the peer certificate's public key matches that of the trusted certificate we have on record. If the certificate on record has expired, we automatically import the client's renewed certificate.

```
    private static boolean isExpired(X509Certificate certificate) {
        return new Date().after(certificate.getNotAfter());
    }
```

We might automatically insert that client's certificate into our SQL truststore if it does not exist. In this case, the newly inserted certificate is initially disabled, pending validation of the client's request message by our application, or alternatively requiring manual approval by an administrator.


<h4>To be continued</h4>

We'll delve into the unit testing of our custom trust manager in a follow-up article.


<h4>Conclusion</h4>

We want the enhanced security of client-authenticated SSL, with the convenience of non-authenticated SSL, via automated certificate enrollment.

<img src='http://jroller.com/evanx/resource/Gnome-application-certificate-250-crop.png' align='right'>

Usually a new client cannot connect to our client-authenticated connection, and so also requires a non-authenticated HTTPS server for enrollment. Unfortunately the standard port number is the same for both, i.e. 443, which is widely allowed by firewalls. Consequently two hosts are typically required, i.e. distinguished by their different IP numbers.<br>
<br>
We introduce a custom trust manager to enable non-authenticated connections for enrollment, as well as our client-authenticated sessions, through the same server. New clients' certificates are imported into our truststore when they connect for the first time, and thereafter we authenticate our SSL clients.<br>
<br>
New clients are pre-approved by an administrator according to their expected certificate names. Alternatively we might allow a new client might connect without a certificate in order to request the server to generate a PKCS12 key and certificate bundle on its behalf. We'll demonstrate this is a follow-up article.<br>
<br>
<br>
<h4>Resources</h4>

You can browse the code for this exercise on <a href='https://code.google.com/p/vellum'>code.google.com/vellum</a>.<br>
<br>
<br>
<h4>Other reading</h4>

Also relating to Java crypto in this blog: <a href='https://weblogs.java.net/blog/evanx/archive/2013/01/24/password-salt'>Password Salt</a> for secure passwords; and leveraging the <a href='https://weblogs.java.net/blog/evanx/archive/2012/11/07/google-authenticator-thus-enabled'>Google Authenticator</a> mobile app for multi-factor authentication for your own sites.<br>
