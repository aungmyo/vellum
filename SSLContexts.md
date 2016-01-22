<h1>Dual Control Key Management</h1>

We hereby start the new 2013 "Dual Control" quadrilogy, part of the <a href='http://code.google.com/p/vellum/wiki/EnigmaPosts'>Enigma Posts</a>.

<h4>Problem overview</h4>

Encryption is great for information security and all that. But the problem with encryption is... key management. An analogy often bandied about is that we lock the door but leave the key in the lock. <i>Or under the mat, but that's my personal favourite so ssh-ssh.</i>

<h4>PCI DSS</h4>

The "Payment Card Industry Data Security Standard" (<a href='http://en.wikipedia.org/wiki/Payment_Card_Industry_Data_Security_Standard'>PCI DSS</a>) advocates common sense policies for building a secure network and protecting our data. Actually every enterprise should adopt PCI DSS because it's the only and best such thing we got. Where it refers to "cardholder data," replace that with "identity data" or "access credentials" or what-have-you.

If we want information security, then PCI DSS is our new best friend forever, albeit a high-maintenance one.

<img src='http://jroller.com/evanx/resource/Gnome-application-certificate-250-crop.png' align='left'>

PCI DSS suggests encrypting our data-encryption key (DEK) in order to protect it. Great, we now have a "key-encryption key" (KEK) that requires even more protection ;)<br>
<br>
Furthermore, PCI DSS decrees that manual key management requires "split knowledge and dual control" e.g. for key generation and loading. The intent is that no single person can extract the clear-text data.<br>
<br>
The glaring problem is that sysadmins are a single person, with god-like access to all our data, and de facto custodian of the proverbial keys to the kindgom. <i>Consequently sysadmins have root access ;)</i>

<h4>Solution overview</h4>

We'll store our key in a standard <a href='http://www.docjar.com/html/api/com/sun/crypto/provider/JceKeyStore.java.html'><tt>JceKeyStore</tt></a>, whose <a href='http://www.docjar.com/html/api/com/sun/crypto/provider/KeyProtector.java.html'><tt>KeyProtector</tt></a> uses a <a href='http://www.docjar.com/html/api/com/sun/crypto/provider/PBEWithMD5AndTripleDESCipher.java.html'>Triple DES cipher</a> for password-based encryption of the key.<br>
<br>
We'll split the knowledge of the key-protection password between two custodians, so it's known to no single person. Clearly dual control by those two custodians is then required to load the key, and PCI DSS compliance is thus enabled.<br>
<br>
We propose enrolling at least three custodians, so that if one is unavailable, we're still good to go with two others. For each duo, we'll store a copy of the key protected by the concatenation of their two personal passwords.<br>
<br>
Since the split password is used for password-based encryption of the key, it is effectively a key-encryption key, which is clearly well protected by virtue of being known to no single person, and not stored anywhere.<br>
<br>
<h4><tt>DualControlGenSecKey</tt> demo</h4>

Step 1 for any data security endeavour is to generate an encryption key. Whereas <tt>keytool</tt> prompts for a password entered by a single custodian, we introduce <tt>DualControlGenSecKey</tt> to handle multiple password submissions via SSL.<br>
<br>
<pre><code>$ java -Ddualcontrol.submissions=3 -Ddualcontrol.minPassphraseLength=8 \<br>
    -Ddualcontrol.ssl.keyStore=keystores/dualcontrolserver.jks \<br>
    -Ddualcontrol.ssl.trustStore=keystores/dualcontrolserver.trust.jks \<br>
    -Dkeystore=keystores/dek2013.jceks -Dstoretype=JCEKS \<br>
    -Dalias=dek2013 -Dkeyalg=AES -Dkeysize=256 \<br>
    dualcontrol.DualControlGenSecKey<br>
<br>
Enter passphrase for dualcontrol.ssl:<br>
Enter passphrase for keystore for new key dek2013: <br>
</code></pre>

where we have requested a new 256bit AES secret key, aliased as <tt>dek2013</tt>.<br>
<br>
Note that the <tt>-Ddualcontrol.ssl.keyStore</tt> property is the location of the "private keystore" for SSL. This contains the <i>private</i> key and its public certificate for RSA assymmetric encryption to establish an SSL connection. This should not be confused with our "secret keystore" which contains an AES or DESede <i>secret</i> key for symmetric encryption of our data.<br>
<br>
The logs produced by <tt>DualControlGenSecKey</tt> are as follows.<br>
<br>
<pre><code>INFO [DualControlManager] purpose: new key dek2013<br>
INFO [DualControlManager] accept: 3<br>
INFO [DualControlManager] Received evanx<br>
INFO [DualControlManager] Received henty<br>
INFO [DualControlManager] Received brent<br>
INFO [DualControlManager] dualAlias: brent-evanx<br>
INFO [DualControlManager] dualAlias: brent-henty<br>
INFO [DualControlManager] dualAlias: evanx-henty<br>
INFO [DualControlGenSecKey] alias: dek2013-brent-evanx<br>
INFO [DualControlGenSecKey] alias: dek2013-brent-henty<br>
INFO [DualControlGenSecKey] alias: dek2013-evanx-henty<br>
</code></pre>

where <tt>DualControlManager</tt> does the leg work of receiving passwords from custodians.<br>
<br>
For this demo, three custodians submit their passwords via SSL sockets, using <tt>DualControlConsole</tt> as shown below, where they are identified as evanx, henty and brent by their SSL client certs' CN field.<br>
<br>
<pre><code>evanx$ java -Ddualcontrol.ssl.keyStore=keystores/evanx.jks dualcontrol.DualControlConsole<br>
Enter passphrase for dualcontrol.ssl:<br>
Connected evanx<br>
Enter passphrase for new key dek2013:<br>
Re-enter passphrase:<br>
Received evanx<br>
</code></pre>

We see that <tt>DualControlGenSecKey</tt> creates secret key entries under the following "dual aliases."<br>
<br>
<pre><code>$ keytool -keystore keystores/dek2013.jceks -storetype JCEKS -list<br>
Enter keystore password:  <br>
<br>
Keystore type: JCEKS<br>
Keystore provider: SunJCE<br>
<br>
Your keystore contains 3 entries<br>
<br>
dek2013-brent-henty, 27 Sep 2013, SecretKeyEntry, <br>
dek2013-brent-evanx, 27 Sep 2013, SecretKeyEntry, <br>
dek2013-evanx-henty, 27 Sep 2013, SecretKeyEntry, <br>
</code></pre>

Actually these three keys are one and the same! However each copy has a different "split password," which is a combination of a pair of personal passwords. Consequently the key password is known to no single person. The keystore password is shared, but that's just extra.<br>
<br>
<h4><tt>DualControlGenSecKey</tt> implementation</h4>

Let's walk through the code.<br>
<br>
<pre><code>public class DualControlGenSecKey {<br>
    private int submissionCount;<br>
    private String keyAlias;<br>
    private String keyStoreLocation;<br>
    private String keyStoreType;<br>
    private String keyAlg;<br>
    private int keySize;<br>
    private char[] keyStorePassword;<br>
    private Map&lt;String, char[]&gt; dualMap;<br>
    private SSLContext sslContext;<br>
<br>
    public DualControlGenSecKey(Properties properties, MockableConsole console) {<br>
        this.props = new ExtendedProperties(properties);<br>
        this.console = console;<br>
        submissionCount = props.getInt("dualcontrol.submissions", 3);<br>
        keyAlias = props.getString("alias");<br>
    }<br>
    ...<br>
}<br>
</code></pre>
where we configure via <tt>Properties</tt> and provide a <tt>MockableConsole</tt> for entering keystore passwords.<br>
<br>
Note that <tt>ExtendedProperties</tt> is a utility wrapper for <tt>java.util.Properties</tt> that will throw an exception if the property is not found and no default is provided.<br>
<br>
We initialise the instance with an <tt>SSLContext</tt> as follows.<br>
<pre><code>    public void init() throws Exception {<br>
        sslContext = SSLContexts.create("dualcontrol.ssl", props, console);<br>
    }<br>
</code></pre>

where we'll see further below that <tt>SSLContexts</tt> uses a keystore and truststore configured by properties e.g. <tt>dualcontrol.ssl.keyStore</tt> and <tt>dualcontrol.ssl.trustStore</tt>. These SSL keystores should not be confused with the keystore for our new secret key which we are generating.<br>
<br>
Our <tt>main()</tt> method below passes <tt>System</tt> properties i.e. <tt>-D</tt> options, and the <tt>System</tt> console.<br>
<pre><code>    public static void main(String[] args) throws Exception {<br>
        DualControlGenSecKey instance = new DualControlGenSecKey(System.getProperties(),<br>
                new MockableConsoleAdapter(System.console()));<br>
        try {<br>
            instance.init();<br>
            instance.call();<br>
        } catch (DualControlException e) {<br>
            instance.console.println(e.getMessage());<br>
        } finally {<br>
            instance.clear();<br>
        }<br>
    }<br>
</code></pre>
where we instantiate, initialise, and then call this class.<br>
<br>
<pre><code>    public void call() throws Exception {<br>
        keyStoreLocation = props.getString("keystore");<br>
        if (new File(keyStoreLocation).exists()) {<br>
            throw new Exception("Keystore file already exists: " + keyStoreLocation);<br>
        }<br>
        keyStorePassword = props.getPassword("storepass", null);<br>
        if (keyStorePassword == null) {<br>
            keyStorePassword = console.readPassword(<br>
                    "Enter passphrase for keystore for new key %s: ", keyAlias);<br>
            if (keyStorePassword == null) {<br>
                throw new Exception("No keystore passphrase from console");<br>
            }<br>
        }<br>
        KeyStore keyStore = createKeyStore();<br>
        keyStore.store(new FileOutputStream(keyStoreLocation), keyStorePassword);<br>
    }<br>
<br>
    public KeyStore createKeyStore() throws Exception {<br>
        String purpose = "new key " + keyAlias;<br>
        DualControlManager manager = new DualControlManager(properties, <br>
                submissionCount, purpose);<br>
        manager.init(sslContext);<br>
        manager.call();<br>
        return buildKeyStore(manager.getDualMap());<br>
    }<br>
</code></pre>
where <tt>DualControlManager</tt> provides a map of dual aliases and passwords, composed from submissions via SSL. We pass this map to the <tt>buildKeyStore()</tt> method below.<br>
<pre><code>    public KeyStore buildKeyStore(Map&lt;String, char[]&gt; dualMap) throws Exception {<br>
        keyAlias = props.getString("alias");<br>
        keyStoreType = props.getString("storetype");<br>
        keyAlg = props.getString("keyalg");<br>
        keySize = props.getInt("keysize");<br>
        KeyGenerator keyGenerator = KeyGenerator.getInstance(keyAlg);<br>
        keyGenerator.init(keySize);<br>
        SecretKey secretKey = keyGenerator.generateKey();<br>
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);<br>
        keyStore.load(null, null);<br>
        setEntries(keyStore, secretKey, keyAlias, dualMap);<br>
        return keyStore;<br>
    }<br>
<br>
    private static void setEntries(KeyStore keyStore, SecretKey secretKey,<br>
            String keyAlias, Map&lt;String, char[]&gt; dualMap) throws Exception {<br>
        KeyStore.Entry entry = new KeyStore.SecretKeyEntry(secretKey);<br>
        for (String dualAlias : dualMap.keySet()) {<br>
            char[] dualPassword = dualMap.get(dualAlias);<br>
            String alias = keyAlias + "-" + dualAlias;<br>
            logger.info("alias: " + alias);<br>
            KeyStore.PasswordProtection passwordProtection =<br>
                    new KeyStore.PasswordProtection(dualPassword);<br>
            keyStore.setEntry(alias, entry, passwordProtection);<br>
            passwordProtection.destroy();<br>
            Arrays.fill(dualPassword, (char) 0);<br>
        }<br>
    }<br>
</code></pre>
where for each duo in the map we program a keystore entry containing the same key, but protected by a different split password.<br>
<br>
Incidently, we invariably use <tt>char</tt> arrays for passwords, and clear these as soon as possible. <tt>String</tt>'s are immutable and will be garbage-collected and overwritten in memory at some stage, but that is too indeterminate to alleviate our paranoia. Having said that, <a href='http://www.docjar.com/html/api/javax/crypto/spec/SecretKeySpec.java.html'><tt>SecretKeySpec</tt></a><tt>.getEncoded()</tt> clones the byte array, which is somewhat disconcerting. So hopefully no one can scan or "debug" our JVM memory and thereby extract our passwords, or indeed the key itself.<br>
<br>
Some argue that one should not write code per se, but rather tests with accompanying code, hand in glove. Indeed we observe that our implementation is well defined by the unit tests.<br>
<pre><code>    @Test<br>
    public void testGenKeyStore() throws Exception {<br>
        dualMap.put("brent-evanx", "bbbb|eeee".toCharArray());<br>
        dualMap.put("brent-henty", "bbbb|hhhh".toCharArray());<br>
        dualMap.put("evanx-henty", "eeee|hhhh".toCharArray());<br>
        MockConsole appConsole = new MockConsole("app", keyStorePass);<br>
        DualControlGenSecKey instance = new DualControlGenSecKey(properties, appConsole);<br>
        KeyStore keyStore = instance.buildKeyStore(dualMap);<br>
        Assert.assertEquals(3, Collections.list(keyStore.aliases()).size());<br>
        Assert.assertEquals("dek2013-brent-evanx", asSortedSet(keyStore.aliases()).first());<br>
        SecretKey key = getSecretKey(keyStore, "dek2013-brent-evanx", "bbbb|eeee".toCharArray());<br>
        Assert.assertEquals("AES", key.getAlgorithm());<br>
        Assert.assertTrue(Arrays.equals(key.getEncoded(), getSecretKey(keyStore, <br>
                "dek2013-brent-henty", "bbbb|hhhh".toCharArray()).getEncoded()));<br>
    }<br>
</code></pre>
where we build a map of sample dual submissions, and inspect the <tt>KeyStore</tt> returned by <tt>buildKeyStore()</tt>.<br>
<br>
Note that the key passwords are concatenations of personal passwords, where we arbitrarily choose the vertical bar character as a delimiter. This is our so-called split password, which can be used to recover the key in clear-text using the following utility method.<br>
<pre><code>    private static SecretKey getSecretKey(KeyStore keyStore, String keyAlias, char[] keyPass) <br>
            throws GeneralSecurityException {<br>
        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(<br>
                keyAlias, new KeyStore.PasswordProtection(keyPass));<br>
        return entry.getSecretKey();<br>
    }<br>
</code></pre>
which invokes <tt>KeyStore.getEntry()</tt>.<br>
<br>
<h4><tt>SSLContexts</tt></h4>

Similarly to the standard <tt>-Dnet.javax.ssl.keyStore</tt> <i>et al</i> command-line options used to specify the default keystore and truststore for SSL sockets, and in order to avoid any potential conflict with those, we use <tt>-Ddualcontrol.ssl.keyStore</tt> <i>et al</i>. These properties are used to create an <tt>SSLContext</tt> as follows.<br>
<pre><code>public class SSLContexts {    <br>
<br>
    public static SSLContext create(String sslPrefix, Properties properties, <br>
            MockableConsole console) throws Exception {<br>
        ExtendedProperties props = new ExtendedProperties(properties);<br>
        sslPrefix = props.getString(sslPrefix, sslPrefix);<br>
        String keyStoreLocation = props.getString(sslPrefix + ".keyStore");<br>
        if (keyStoreLocation == null) {<br>
            throw new Exception("Missing -D property: " + sslPrefix + ".keyStore");<br>
        }<br>
        char[] pass = props.getPassword(sslPrefix + ".pass", null);<br>
        if (pass == null) {<br>
            pass = console.readPassword("Enter passphrase for %s: ", sslPrefix);<br>
        }<br>
        String trustStoreLocation = props.getString(sslPrefix + ".trustStore", <br>
                keyStoreLocation);<br>
        SSLContext sslContext = create(keyStoreLocation, trustStoreLocation, pass);<br>
        String crlFile = props.getString(sslPrefix + ".crlFile", null);<br>
        if (crlFile != null) {<br>
            sslContext = create(keyStoreLocation, pass, trustStoreLocation,<br>
                    readRevocationList(crlFile));<br>
        }<br>
        Arrays.fill(pass, (char) 0);<br>
        return sslContext;<br>
    }<br>
    ...<br>
}<br>
</code></pre>
where we reuse the same password for the SSL keystore, its private key, and the truststore. This password can specified on the command-line e.g. for automated test scripts, but otherwise we prompt for the SSL keystore password to be entered on the console. Moreover, we have introduced a <tt>MockableConsole</tt> for the benefit of automated unit testing.<br>
<br>
Note that the truststore is defaulted to the specified private keystore, which is OK for self-signed certificates. When using CA-signed certificates, our keystore must contain the certificate chain including the CA certs e.g. intermediate and root. However, these cannot be in our truststore, otherwise we trust any cert signed by that CA!<br>
<br>
Ordinarily we create the <tt>SSLContext</tt> as follows.<br>
<pre><code>    public static SSLContext create(KeyStore keyStore, char[] keyPassword,<br>
            KeyStore trustStore) throws Exception {<br>
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");<br>
        keyManagerFactory.init(keyStore, keyPassword);<br>
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");<br>
        trustManagerFactory.init(trustStore);<br>
        SSLContext sslContext = SSLContext.getInstance("TLS");<br>
        sslContext.init(keyManagerFactory.getKeyManagers(),<br>
                trustManagerFactory.getTrustManagers(), new SecureRandom());<br>
        return sslContext;<br>
    }<br>
</code></pre>

Furthermore, we enable a local certificate revocation list (CRL) specified by our <tt>crlFile</tt> property.<br>
<pre><code>    public static SSLContext create(KeyStore keyStore, char[] keyPass,<br>
            KeyStore trustStore, List&lt;BigInteger&gt; revocationList) throws Exception {<br>
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");<br>
        keyManagerFactory.init(keyStore, keyPass);<br>
        SSLContext sslContext = SSLContext.getInstance("TLS");<br>
        TrustManager revocableTrustManager = new RevocableClientTrustManager(<br>
                getPrivateKeyCertificate(keyStore),<br>
                getX509TrustManager(trustStore),<br>
                revocationList);<br>
        sslContext.init(keyManagerFactory.getKeyManagers(),<br>
                new TrustManager[] {revocableTrustManager},<br>
                new SecureRandom());<br>
        return sslContext;<br>
    }<br>
</code></pre>
where this uses a custom <tt>RevocableClientTrustManager</tt> instantiated with a <tt>List</tt> of revoked certificates' names. These are read from a local text file, as follows.<br>
<pre><code>    private static List&lt;String&gt; readRevocationList(String crlFile)<br>
            throws FileNotFoundException, IOException {<br>
        List&lt;String&gt; revocationList = new ArrayList();<br>
        BufferedReader reader = new BufferedReader(new FileReader(crlFile));<br>
        while (true) {<br>
            String line = reader.readLine();<br>
            if (line == null) {<br>
                return revocationList;<br>
            }<br>
            revocationList.add(line.trim());<br>
        }        <br>
    }<br>
</code></pre>

<h4><tt>RevocableClientTrustManager</tt></h4>

We implement a custom <tt>X509TrustManager</tt> to support local certificate revocation. This enables a simple "local CA" via a server keystore/truststore with a single entry, where the server cert is also our root CA cert used to sign client certs.<br>
<pre><code>public class RevocableClientTrustManager implements X509TrustManager {<br>
    X509Certificate serverCertificate;<br>
    X509TrustManager delegate;<br>
    Collection&lt;String&gt; revokedCNList;<br>
    Collection&lt;BigInteger&gt; revokedSerialNumberList;<br>
    ...<br>
    @Override<br>
    public void checkClientTrusted(X509Certificate[] certs, String authType) <br>
            throws CertificateException {<br>
        if (certs.length != 2) {<br>
            throw new CertificateException("Invalid cert chain length");<br>
        }<br>
        if (!certs[0].getIssuerX500Principal().equals(<br>
                serverCertificate.getSubjectX500Principal())) {<br>
            throw new CertificateException("Untrusted issuer");<br>
        }<br>
        if (!Arrays.equals(certs[1].getPublicKey().getEncoded(),<br>
                serverCertificate.getPublicKey().getEncoded())) {<br>
            throw new CertificateException("Invalid server certificate");<br>
        }<br>
        if (revokedCNList.contains(getCN(certs[0].getSubjectDN()))) {<br>
            throw new CertificateException("Certificate CN revoked");<br>
        }<br>
        if (revokedSerialNumberList.contains(certs[0].getSerialNumber())) {<br>
            throw new CertificateException("Certificate serial number revoked");<br>
        }<br>
        delegate.checkClientTrusted(certs, authType);<br>
    }<br>
}<br>
</code></pre>
where we initialise this trust manager with a collection of revoked certificates' CNs or serial numbers. We check that the client certificate is signed by our server certificate, and not revoked. Finally, we delegate to the standard <tt>X509TrustManager</tt> for good measure.<br>
<br>
We'll elaborate on <a href='https://code.google.com/p/vellum/source/browse/trunk/src/dualcontrol/RevocableClientTrustManager.java'><tt>RevocableClientTrustManager</tt></a> and its <a href='https://code.google.com/p/vellum/source/browse/trunk/test/dualcontrol/RevocableClientTrustManagerTest.java'>unit tests</a> in a follow-up article.<br>
<br>
<h4><tt>keytool</tt></h4>

Naturally we use <tt>keytool</tt> to create our private SSL keystore, e.g. as required by <tt>DualControlConsole</tt>, specified by our <tt>dualcontrol.ssl.keyStore</tt> property.<br>
<pre><code>$ keytool -keystore evanx.jks -genkeypair -keyalg rsa -keysize 2048 -validity 365 -alias evanx \<br>
    -dname "CN=evanx, OU=test"<br>
</code></pre>
We export our certificate as follows.<br>
<pre><code>$ keytool -keystore evanx.jks -alias evanx -exportcert -rfc<br>
</code></pre>
We cut and paste the exported PEM text into a file, which we can inspect using <tt>openssl</tt> as follows.<br>
<pre><code>$ openssl x509 -text -in evanx.pem<br>
Certificate:<br>
    Data:<br>
        Version: 3 (0x2)<br>
        Serial Number: 1380030508 (0x5241982c)<br>
    Signature Algorithm: sha1WithRSAEncryption<br>
        Issuer: CN=evanx, OU=test<br>
        Validity<br>
            Not Before: Sep 24 13:48:28 2013 GMT<br>
            Not After : Sep 24 13:48:28 2014 GMT<br>
        Subject: CN=evanx, OU=test<br>
        Subject Public Key Info:<br>
            Public Key Algorithm: rsaEncryption<br>
                Public-Key: (2048 bit)<br>
</code></pre>
We import the cert into the server SSL truststore as required by <tt>DualControlManager</tt> on behalf of <tt>DualControlGenSecKey</tt> and our app.<br>
<pre><code>$ keytool -keystore dualcontrolserver.trust.jks -alias evanx -importcert -file evanx.pem<br>
</code></pre>

Similarly, the server cert is imported into the custodians' truststores as specified by <tt>dualcontrol.ssl.trustStore</tt> for <tt>DualControlConsole</tt>.<br>
<br>
<h4>Client certificate signing</h4>

Alternatively, we could export an CSR, and sign this with the server cert, which is then our local CA cert, and we must support certificate revocation.<br>
<pre><code>$ keytool -keystore evanx.jks -alias evanx -certreq<br>
</code></pre>
We use Java7's keytool to sign the CSR.<br>
<pre><code>$ keytool -keystore dualcontrolserver.jks -gencert -validity 365 -rfc \<br>
    -dname "CN=evanx, OU=test" -infile evanx.csr -outfile evanx.signed.pem<br>
</code></pre>
Note that <tt>-gencert</tt> is not available in Java6's keytool.<br>
<br>
We inspect the cert using <tt>openssl</tt>.<br>
<pre><code>$ openssl x509 -text -in evanx.signed.pem | grep CN<br>
        Issuer: CN=dualcontrolserver, OU=test<br>
        Subject: CN=evanx, OU=test<br>
</code></pre>
Since our keystore requires our cert chain to be imported in the correct order starting with the root cert, we import the server cert first, and then our signed cert.<br>
<pre><code>$ keytool -keystore evanx.jks -importcert -noprompt \<br>
    -file dualcontrolserver.pem -alias dualcontrolserver <br>
Enter keystore password:  <br>
Certificate was added to keystore<br>
<br>
$ keytool -keystore evanx.jks -importcert -noprompt \<br>
    -file evanx.signed.pem  -alias evanx<br>
Enter keystore password:  <br>
Certificate reply was installed in keystore<br>
</code></pre>

Our client keystore can double up as our truststore since it contains the server cert as the root of its certificate chain. If our server cert is CA-signed, we can't do that, since our keystore certificate chain would include the CA's root cert, and so we would trust <i>any</i> cert signed by that CA.<br>
<br>
Our server keystore can also double up as its truststore, since it contains its cert which signs our client certs.<br>
<br>
<h4>Client certificate management</h4>

Our <tt>RevocableClientTrustManager</tt> handles a revocation list of CN's, where we assume these are unique. Alternatively we could use the cert's serial number.<br>
<br>
If a certificate is signed but not recorded, or its record is deleted, our server is forever vulnerable to that rogue certificate. We cannot revoke it when we have no record that it was even issued!<br>
<br>
We might record our signed certs into a keystore file as follows.<br>
<pre><code>$ keytool -keystore dualcontrolserver.issued.jks -importcert -file evanx.pem<br>
</code></pre>
where this is not a truststore per se, but just a database of issued certs. However, it might as well be a truststore, from which we delete certificates in order to revoke them.<br>
<br>
Actually given the risk of a rogue certificate, we recommend keeping it simple, with a separate server truststore which explicitly contains the self-signed client certs which we accept.<br>
<br>
Note that in this case, the server keystore must not be used as its truststore, into which we load self-signed client certificates. Naturally the keystore contains the server certificate, and so if it configured as the truststore, then consequently any client certificate signed by that server certificate would be trusted. As such, a rogue certificate can be created by signing it using the server keystore. Even if those that certificate is not included in the "truststore," it would be equally trusted to the self-signed client certificates.<br>
<br>
<h4><tt>DualControlConsole</tt></h4>

Let's try our <tt>DualControlConsole</tt> app, e.g. to submit a password to <tt>DualControlGenSecKey</tt>.<br>
<br>
<pre><code>evanx$ java -Ddualcontrol.ssl.keyStore=keystores/evanx.jks dualcontrol.DualControlConsole<br>
Enter passphrase for dualcontrol.ssl:<br>
Connected evanx<br>
Enter passphrase for new key dek2013: <br>
Re-enter passphrase: <br>
Received evanx<br>
</code></pre>
where <tt>SSLContexts</tt> prompts for the SSL keystore password first. We then enter a passphrase for the new key.<br>
<br>
The importance of not echoing passwords to the console, so that they are not cut and pasted by mistake, is illustrated above ;)<br>
<br>
We implement the <tt>main()</tt> as follows.<br>
<pre><code>public class DualControlConsole {<br>
    Properties properties;<br>
    MockableConsole console;<br>
    SSLContext sslContext;<br>
<br>
    public static void main(String[] args) throws Exception {<br>
        DualControlConsole instance = new DualControlConsole(System.getProperties(), <br>
                new MockableConsoleAdapter(System.console()));<br>
        try {<br>
            instance.init();<br>
            instance.call();<br>
        } finally {            <br>
            instance.clear();<br>
        }<br>
    }<br>
<br>
    public void init() throws Exception {<br>
        init(SSLContexts.create("dualcontrol.ssl", properties, console));<br>
    }<br>
    ...<br>
}<br>
</code></pre>

where we create an <tt>SSLContext</tt> from the provided properties, which must specify the requisite keystore and truststore. (Later we'll see that our unit test provides an <tt>SSLContext</tt> from <tt>KeyStore</tt>'s which it creates programmatically.)<br>
<br>
We submit the password via SSL in the <tt>call()</tt> method below.<br>
<br>
<pre><code>    private final static int PORT = 4444;<br>
    private final static String HOST = "127.0.0.1";<br>
    <br>
    public void call() throws Exception {<br>
        Socket socket = sslContext.getSocketFactory().createSocket(HOST, PORT);<br>
        DataInputStream dis = new DataInputStream(socket.getInputStream());<br>
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());<br>
        String purpose = dis.readUTF();<br>
        char[] password = console.readPassword(<br>
                "Enter passphrase for " + purpose + ": ");<br>
        String invalidMessage = new DualControlPassphraseVerifier(properties).<br>
                getInvalidMessage(password);<br>
        if (invalidMessage != null) {<br>
            console.println(invalidMessage);<br>
            dos.writeShort(0);<br>
        } else {<br>
            char[] pass = console.readPassword(<br>
                    "Re-enter passphrase: ");<br>
            if (!Arrays.equals(password, pass)) {<br>
                console.println("Passwords don't match.");<br>
                dos.writeShort(0);<br>
            } else {<br>
                writeChars(dos, password);<br>
                String message = dis.readUTF();<br>
                console.println(message);<br>
            }<br>
            Arrays.fill(pass, (char) 0);<br>
        }<br>
        Arrays.fill(password, (char) 0);<br>
        socket.close();<br>
    }<br>
</code></pre>

where if the re-entered password does not match, we politely send an empty password to the server, which indicates an aborted attempt.<br>
<br>
Note that have hard-coded the host to <tt>localhost</tt> to enforce ssh port forwarding for remote access i.e. SSL over ssh :)<br>
<br>
A known "bug" is that more godly sysadmins can socially-engineer other less godly admins to submit their passwords whilst a malicious socket server has been installed by the most godly one onto that frikking port! Other naughty tricks that come to mind is the creation of phantom custodians, or the impersonation of other custodians e.g. during a key generation procedure, by abusing root access to SSL keystores. So we must ensure that our logs and alerts prevent such events from going unnoticed.<br>
<br>
<h4>Multi-factor authentication</h4>

On a positive note, we can setup "<i>double</i>-two-factor" authentication whereby the client requires a password-protected <tt>ssh</tt> key for port forwarding, and a password-protected <tt>KeyStore</tt> for the client-authenticated SSL connection.<br>
<br>
<img src='http://jroller.com/evanx/resource/Gnome-preferences-desktop-personal-250-crop.png' align='right' />

For both the underlying <tt>ssh</tt> access, and the <tt>SSL</tt> connection over that, the custodian needs to <i>have</i> the private key, and <i>know</i> the password that is protecting it, and so arguably both require <a href='http://en.wikipedia.org/wiki/Multi-factor_authentication'>multi-factor authentication</a>.<br>
<br>
However root can also "have" the files containing the keys, but at least doesn't know other custodians' passwords protecting the keys therein.<br>
<br>
Some argue that to really "have" something, it should not be so copyable as a key in a file, but rather it should be a hardware token or smartcard. However, <a href='https://www.pcisecuritystandards.org/documents/navigating_dss_v20.pdf'><i>Navigating PCI DSS 2.0</i></a> provides the following guidance:<br>
<blockquote>
<i>A digital certificate is a valid option as a form of the authentication type "something<br>
you have" as long as it is unique.</i>
</blockquote>

A further challenge is that the existence of a password which protects an ssh or SSL key, cannot be verified by the server. So at least we double up to mitigate this ;)<br>
<br>
<h4>Password complexity vs length</h4>

We ensure that the complexity and/or length of the password is sufficient to counter brute-force attacks.<br>
<br>
<pre><code>public class DualControlPassphraseVerifier {<br>
    private final boolean verifyPassphrase;<br>
    private final boolean verifyPassphraseComplexity;<br>
    private final int minPassphraseLength;<br>
    private final int minWordCount;<br>
<br>
    public DualControlPassphraseVerifier(Properties properties) {<br>
        ExtendedProperties props = new ExtendedProperties(properties);<br>
        verifyPassphrase = props.getBoolean(<br>
                "dualcontrol.verifyPassphrase", true);<br>
        verifyPassphraseComplexity = props.getBoolean(<br>
                "dualcontrol.verifyPassphraseComplexity", true);<br>
        minPassphraseLength = props.getInt(<br>
                "dualcontrol.minPassphraseLength", 12);<br>
        minWordCount = props.getInt(<br>
                "dualcontrol.minWordCount", 4);<br>
    }<br>
    ...<br>
</code></pre>

We note that PCI DSS mandates alphanumeric passwords, whereas <a href='http://en.wikipedia.org/wiki/Sarbanes%E2%80%93Oxley_Act'>SOX</a> requires uppercase and lowercase, and we should cover all bases.<br>
<pre><code>    public String getInvalidMessage(char[] password) {<br>
        if (verifyPassphrase) {<br>
            if (password.length &lt; minPassphraseLength) {<br>
                return "Passphrase too short";<br>
            }<br>
            if (countWords(password) &lt; minWordCount) {<br>
                return "Too few words in passphrase";<br>
            }<br>
            if (verifyPassphraseComplexity) {<br>
                if (!containsUpperCase(password) || !containsLowerCase(password) || <br>
                        !containsDigit(password) || !containsPunctuation(password)) {<br>
                    return "Insufficient password complexity";<br>
                }<br>
            }<br>
        }<br>
        return null;<br>
    }<br>
</code></pre>

Clearly passphrases are easier to remember than complex passwords, and so perhaps we should enforce passphrases with a minimum word count, rather than complexity per se. Then we don't have to write down our complex password and stick it on our monitor.<br>
<br>
<img src='http://jroller.com/evanx/resource/wooden-shield200.png' align='right' />

Having said that, we can easily capitalise the first letter of our passphrase, add a punctuation mark at the end at least, and somewhere replace an 'o' with a zero or an 'e' with 3. <i>Yeah b4by! (Darn, now I can't use that one anymore.)</i>

Incidently, when we are submitting existing passwords using <tt>DualControlConsole</tt> to start our app, we might have to disable passphrase verification if our old password falls short of revised complexity requirements. However in that case, we should rather change our password accordingly, e.g. together with a key rotation for good measure.<br>
<br>
We note that PCI DSS mandates that passwords be changed every 90 days. However, we hope that this applies to remote access passwords, and not to key-protection passwords. We might argue that key-protection passwords are key-encryption keys, and not remote access credentials per se, even when they protect remote access keys. Actually we argue the opposite in favour of multi-factor authentication, so it depends on the context ;)<br>
<br>
<h4>Brutish Key Protector</h4>

Our keystore is unfortunately forever vulnerable to theft and/or brute-force attacks. Even when we have deleted it, someone else might have ill-gotten it earlier. On the upside, we might assume that after say 30 years the data is no longer of any value to anyone.<br>
<br>
We observe that an Intel i5 can manage about 30 guesses per millisecond on a JCE keystore, using 4 threads to utilise its quad-cores.<br>
<br>
<pre><code>evanx@beethoven:~$ java dualcontrol.JCEKSBruteForceTimer 4 1000000 \<br>
  keystores/dek2013.jceks "$pass" dek2013-evanx-henty eeeehhhh<br>
<br>
threads 4, count 1000000, time 128s, avg 0.032ms<br>
31 guesses per millisecond<br>
</code></pre>

If we assume an average of 10 guesses per core per millisecond, and consider a botnet with 1 million cores, then by my backroom calculations, 12 days are required to try all possible passwords up to 10 characters in length using a lazy subset of 40 characters.<br>
<br>
<pre><code>guess=10<br>
mach=1000*1000<br>
40^10/(mach*guess*1000*60*60*24)<br>
12 days<br>
</code></pre>

Considering that the most common 100 words make up a half of all written material (cit. <a href='http://en.wikipedia.org/wiki/Most_common_words_in_English'>Wikipedia</a>), if we guess combinations of the 1500 most common words, then 8 days are required for such passphrases with 5 words.<br>
<br>
<pre><code>(1500^5)/(mach*guess*1000*60*60*24)<br>
8 days<br>
</code></pre>

Note that our mandatory minimums apply to each custodian's passphrase, to protect against a rogue custodian perpetrating the brute-force attack on the other half of the split password.<br>
<br>
A follow-up article will discuss this further, and conclude that we might want an even stronger <tt>KeyStore</tt> implementation than JCEKS, in particular with a stronger <tt>KeyProtector</tt> using <tt>PBKDF2</tt> or even <tt>scrypt</tt>. Then we would specify huge number of iterations e.g. 500k, so that it takes a few seconds to load the key. That is still a tolerable startup delay in our production environment, but would thwart brute-force attacks.<br>
<br>
<h4><tt>DualControlDemoApp</tt></h4>

The downside of all this malarkey, is that we have to re-engineer our application to be dual-controlled, in order to load the key it so desperately needs to cipher our data.<br>
<br>
<pre><code>public class DualControlDemoApp {<br>
    private SecretKey dek;     <br>
    ...<br>
    public void loadKey(String keyStoreLocation, String alias) throws Exception {<br>
        char[] storePass = System.console().readPassword(<br>
                "Enter keystore password for %s: ", alias);<br>
        dek = DualControlSessions.loadKey(keyStoreLocation, "JCEKS", storePass, alias,<br>
                "DualControlDemoApp");<br>
        logger.info("loaded key {}: alg {}", alias, dek.getAlgorithm());<br>
    }<br>
}<br>
</code></pre>
where we prompt for the shared keystore password to be entered on the console.<br>
<pre><code>$ java -Ddualcontrol.ssl.keyStore=keystores/dualcontrolserver.jks \<br>
    -Ddualcontrol.ssl.trustStore=keystores/dualcontrolserver.trust.jks \<br>
    dualcontrol.DualControlDemoApp keystores/dek2013.jceks dek2013<br>
Enter passphrase for dualcontrol.ssl:<br>
Enter keystore password for dek2013:<br>
INFO [DualControlManager] purpose: key dek2013 for DualControlDemoApp<br>
INFO [DualControlManager] accept: 2<br>
</code></pre>

The application's execution is then blocked whilst we are waiting on the <tt>SSLServerSocket</tt> for two custodians to submit their passwords using <tt>DualControlConsole</tt>.<br>
<pre><code>evanx$ java -Ddualcontrol.ssl.keyStore=evanx.jks dualcontrol.DualControlConsole<br>
Enter passphrase for dualcontrol.ssl:<br>
Connected evanx<br>
Enter passphrase for key dek2013 for DualControlDemoApp: <br>
Re-enter passphrase:<br>
Received evanx<br>
</code></pre>
where we specify the keystore for the SSL socket, which can double up as our truststore.<br>
<pre><code>henty$ java -Ddualcontrol.ssl.keyStore=henty.jks dualcontrol.DualControlConsole<br>
Enter passphrase for dualcontrol.ssl:<br>
Connected henty<br>
Enter passphrase for key dek2013 for DualControlDemoApp: <br>
Re-enter passphrase:<br>
Received henty<br>
</code></pre>

We observe the following logs from <tt>DualControlDemoApp</tt>.<br>
<pre><code>INFO [DualControlManager] Received evanx<br>
INFO [DualControlManager] Received henty<br>
INFO [DualControlManager] dualAlias: evanx-henty<br>
INFO [DualControlSessions] dek2013-evanx-henty<br>
INFO [DualControlDemoApp] loaded key dek2013: alg AES<br>
</code></pre>
where it accepts submissions from evanx and henty courtesy of <tt>DualControlManager</tt>, and can then load the key.<br>
<br>
<h4><tt>DualControlSessions</tt></h4>

Our demo app above invokes the <tt>loadKey()</tt> method below to do the legwork.<br>
<br>
<pre><code>public class DualControlSessions {<br>
<br>
    public static SecretKey loadKey(String keyStoreLocation, String keyStoreType, <br>
            char[] keyStorePass, String keyAlias, String purpose) throws Exception {<br>
        KeyStore keyStore = DualControlKeyStores.loadKeyStore(keyStoreLocation, <br>
                keyStoreType, keyStorePass);<br>
        purpose = "key " + keyAlias + " for " + purpose;<br>
        Map.Entry&lt;String, char[]&gt; entry = DualControlManager.readDualEntry(purpose);<br>
        String dualAlias = entry.getKey();<br>
        char[] splitPassphrase = entry.getValue();<br>
        keyAlias = keyAlias + "-" + dualAlias;<br>
        SecretKey key = (SecretKey) keyStore.getKey(keyAlias, splitPassword);<br>
        Arrays.fill(splitPassword, (char) 0);<br>
        return key;<br>
    }<br>
}<br>
</code></pre>

where we read the dual info using <tt>DualControlManager</tt>, which opens an <tt>SSLServerSocket</tt> and waits for split password submissions from any two custodians.<br>
<br>
Note that we append the dual alias e.g. so that <tt>dek2013</tt> becomes <tt>dek2013-evanx-henty</tt>, and get that copy of the key from the keystore.<br>
<br>
Once we have used the split password to load the key, we clear that password. However the key itself is now in memory in clear-text, and we must be wary of it being compromised by our application, or extracted by attaching a debugger to the JVM, or by a malicious memory scanner.<br>
<br>
We instantiate, initialise and call <tt>DualControlManager</tt> as follows.<br>
<pre><code>    public static Map.Entry&lt;String, char[]&gt; readDualEntry(String purpose) throws Exception {<br>
        DualControlManager manager = new DualControlManager(System.getProperties(), 2, purpose);<br>
        manager.setVerifyPassphrase(false);<br>
        manager.init(new MockableConsoleAdapter(System.console()));<br>
        manager.call();<br>
        return manager.getDualMap().entrySet().iterator().next();<br>
    }   <br>
</code></pre>

where we read the first entry in <tt>dualMap</tt>. Actually this is the only entry in the map since we have required only two submissions.<br>
<br>
Note that we are accepting custodians' existing passwords to load the key, and so we don't verify their complexity as we would for <tt>DualControlGenSecKey</tt>.<br>
<br>
<h4>Remote keystore</h4>

We might wish to store our keystore centrally, or internally on a more secure server, and load it via SSL as follows.<br>
<br>
<pre><code>public class DualControlKeyStores {<br>
<br>
    public static KeyStore loadKeyStore(String keyStoreLocation, String keyStoreType,<br>
            char[] keyStorePassword) <br>
            throws Exception {<br>
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);<br>
        if (keyStoreLocation.contains(":")) {<br>
            String[] array = keyStoreLocation.split(":");<br>
            String keyStoreHost = array[0];<br>
            int keyStorePort = Integer.parseInt(array[1]);<br>
            SSLContext sslContext = SSLContexts.create("fileclient.ssl", <br>
                    System.getProperties(), <br>
                    new MockableConsoleAdapter(System.console()));<br>
            Socket socket = sslContext.getSocketFactory().createSocket(<br>
                    keyStoreHost, keyStorePort);<br>
            keyStore.load(socket.getInputStream(), keyStorePassword);<br>
            socket.close();<br>
        } else if (new File(keyStoreLocation).exists()) {<br>
            FileInputStream fis = new FileInputStream(keyStoreLocation);<br>
            keyStore.load(fis, keyStorePassword);<br>
            fis.close();<br>
        } else {<br>
            keyStore.load(null, null);<br>
        }<br>
        return keyStore;<br>
    }<br>
</code></pre>

where if the keystore location is formatted as <tt>host:port</tt>, then we open an <tt>SSLSocket</tt> from which to read a remote keystore file.<br>
<br>
Note that we use the properties <tt>fileclient.ssl.keyStore</tt> <i>et al</i> for <tt>SSLContexts</tt> to configure this client SSL connection.<br>
<br>
The following utility demonstrates a trivial server for a remote keystore.<br>
<br>
<pre><code>public class FileServer {<br>
    private static Logger logger = Logger.getLogger(FileServer.class);<br>
    private InetAddress localAddress;<br>
    private int port;<br>
    private int backlog;<br>
    private Set&lt;String&gt; allowedHosts = new TreeSet();<br>
    private String fileName;<br>
    ...<br>
    public void call() throws Exception {<br>
        SSLContext sslContext = SSLContexts.create("fileserver.ssl", <br>
                System.getProperties(), new MockableConsoleAdapter(System.console()));<br>
        SSLServerSocket serverSocket = (SSLServerSocket) sslContext.getServerSocketFactory().<br>
                createServerSocket(port, backlog, localAddress);<br>
        serverSocket.setNeedClientAuth(true);<br>
        FileInputStream stream = new FileInputStream(fileName);<br>
        int length = (int) new File(fileName).length();<br>
        byte[] bytes = new byte[length];<br>
        stream.read(bytes);<br>
        while (true) {<br>
            Socket socket = serverSocket.accept();<br>
            logger.info("hostAddress " + socket.getInetAddress().getHostAddress());<br>
            if (allowedHosts.contains(socket.getInetAddress().getHostAddress())) {<br>
                socket.getOutputStream().write(bytes);<br>
            }<br>
            socket.close();<br>
        }        <br>
    }    <br>
}<br>
</code></pre>

where we create an <tt>SSLServerSocket</tt> requiring client authentication, and write out the keystore file to client connections from allowed remote hosts.<br>
<br>
<h4><tt>DualControlManager</tt></h4>

Finally, we present <tt>DualControlManager</tt> which accepts the split password submissions.<br>
<br>
<pre><code>public class DualControlManager {<br>
    private Properties properties;<br>
    private String purpose;<br>
    private int submissionCount;<br>
    private SSLContext sslContext;<br>
    private Map&lt;String, char[]&gt; submissions = new TreeMap();<br>
    <br>
    public DualControlManager(Properties properties, int submissionCount, String purpose) {<br>
        this.properties = properties;<br>
        this.submissionCount = submissionCount;<br>
        this.purpose = purpose;<br>
    }<br>
<br>
    public void init(SSLContext sslContent) {<br>
        this.sslContext = sslContent;<br>
    }<br>
    ...<br>
}<br>
</code></pre>

where we specify the required number of password submissions for some purpose e.g. the generate a key or load a key, and provide an <tt>SSLContext</tt> for the <tt>SSLServerSocket</tt>.<br>
<br>
Our app calls <tt>DualControlManager</tt> to accept submissions via an <tt>SSLServerSocket</tt> as follows.<br>
<br>
<pre><code>    private final static int PORT = 4444;<br>
    private final static String HOST = "127.0.0.1";<br>
    private final static String REMOTE_ADDRESS = "127.0.0.1";<br>
    ...<br>
    public void call() throws Exception {<br>
        logger.info("purpose: " + purpose);<br>
        SSLServerSocket serverSocket = (SSLServerSocket) sslContext.<br>
                getServerSocketFactory().createServerSocket(PORT, submissionCount,<br>
                InetAddress.getByName(HOST));<br>
        try {<br>
            serverSocket.setNeedClientAuth(true);<br>
            accept(serverSocket);<br>
        } finally {<br>
            serverSocket.close();<br>
        }<br>
        buildDualMap();<br>
    }<br>
</code></pre>

where we create a client-authenticated SSL server socket.<br>
<br>
Note that we have hard-wired the <tt>SSLServerSocket</tt> to <tt>localhost</tt>. Therefore <tt>DualControlConsole</tt> must be invoked either in a local <tt>ssh</tt> session, or a remote session using <tt>ssh</tt> port forwarding.<br>
<br>
<pre><code>    private void accept(SSLServerSocket serverSocket) throws Exception {<br>
        logger.info("accept: " + submissionCount);        <br>
        while (submissions.size() &lt; submissionCount) {<br>
            SSLSocket socket = (SSLSocket) serverSocket.accept();<br>
            try {<br>
                if (!socket.getInetAddress().getHostAddress().equals(REMOTE_ADDRESS)) {<br>
                    throw new Exception("Invalid remote address: "<br>
                            + socket.getInetAddress().getHostAddress());<br>
                }<br>
                read(socket);<br>
            } finally {<br>
                socket.close();<br>
            }<br>
        }<br>
    }<br>
</code></pre>

where we accumulate the required number of submissions in a loop, handing each socket connection as follows.<br>
<br>
<pre><code>    private void read(SSLSocket socket) throws Exception {<br>
        String name = getCN(socket.getSession().getPeerPrincipal().getName());<br>
        if (submissions.keySet().contains(name)) {<br>
            throw new Exception("Duplicate submission from " + name);<br>
        }<br>
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());<br>
        dos.writeUTF(purpose);<br>
        DataInputStream dis = new DataInputStream(socket.getInputStream());<br>
        char[] passphrase = readChars(dis);<br>
        try {<br>
            String resultMessage = verify(name, passphrase);<br>
            dos.writeUTF(resultMessage);<br>
            logger.info(resultMessage);<br>
        } catch (Exception e) {<br>
            dos.writeUTF(e.getMessage());<br>
            logger.warn(e.getMessage());<br>
            throw e;<br>
        }<br>
    }<br>
</code></pre>

where the custodian's username is determined from their SSL cert, in particular the <tt>CN</tt> field.<br>
<br>
<tt>DualControlGenSecKey</tt> will require <tt>DualControlManager</tt> to verify the length and complexity of new passphrases, but otherwise we'll set <tt>verifyPassphrase</tt> to <tt>false</tt> e.g. for existing passphrases as required to load the key into our app.<br>
<br>
<pre><code>    private String verify(String name, char[] passphrase) throws Exception {<br>
        if (passphrase.length == 0) {<br>
            return "Empty submission from " + name;<br>
        }<br>
        String responseMessage = "Received " + name;<br>
        if (verifyPassphrase &amp;&amp; !verifiedNames.contains(name)) {<br>
            String invalidMessage = new DualControlPassphraseVerifier(properties).<br>
                    getInvalidMessage(passphrase);<br>
            if (invalidMessage != null) {<br>
                throw new Exception(responseMessage + ": " + invalidMessage);<br>
            }<br>
        }<br>
        submissions.put(name, passphrase);<br>
        return responseMessage;<br>
    }<br>
</code></pre>

where an empty passhrase is sent by <tt>DualControlConsole</tt> to abort if re-entered passphrase doesn't match, and we'll let the custodian retry in that case.<br>
<br>
Finally, we compose a map of dual aliases and split passwords as follows.<br>
<br>
<pre><code>    private void buildDualMap() {<br>
        for (String name : submissions.keySet()) {<br>
            for (String otherName : submissions.keySet()) {<br>
                if (name.compareTo(otherName) &lt; 0) {<br>
                    String dualAlias = String.format("%s-%s", name, otherName);<br>
                    char[] dualPassword = combineSplitPassword(<br>
                            submissions.get(name), submissions.get(otherName));<br>
                    dualMap.put(dualAlias, dualPassword);<br>
                    logger.info("dualAlias: " + dualAlias);<br>
                }<br>
            }<br>
        }<br>
        for (char[] password : submissions.values()) {<br>
            Arrays.fill(password, (char) 0);<br>
        }<br>
    }<br>
</code></pre>

where the <tt>compareTo()</tt> in the nested loop above ensures that we exclude tehe alphabetically-challenged <tt>evanx-brent</tt> in favour of <tt>brent-evanx</tt>, and the likes of <tt>evanx-evanx</tt>, which is just silly.<br>
<br>
Incidently, we combine the split passwords by simply concatenating them.<br>
<pre><code>    public static char[] combineSplitPassword(char[] password, char[] other) {<br>
        char[] splitPassword = new char[password.length + other.length + 1];<br>
        int index = 0;<br>
        for (char ch : password) {<br>
            splitPassword[index++] = ch;<br>
        }<br>
        splitPassword[index++] = '|';<br>
        for (char ch : other) {<br>
            splitPassword[index++] = ch;<br>
        }<br>
        return splitPassword;<br>
    }<br>
</code></pre>

where we arbitrarily decided to delimit the two personal passwords with the vertical bar character.<br>
<br>
<h4>Unit test</h4>

In <a href='https://code.google.com/p/vellum/source/browse/trunk/test/dualcontrol/DualControlTest.java'><tt>DualControlTest</tt></a>, we test <tt>DualControlManager</tt> and <tt>DualControlConsole</tt> in concert, using threads, mock consoles and what-not. This will be presented in a follow-on article, as this article is already too long.<br>
<br>
<h4>Crypto server</h4>

We might wish to create a central crypto server which is dual-controlled, rather than burden our app. In this case, we can restart our application without dual control, and indeed have any number of apps using this server. This simplifies key management, and enables us to isolate our keys to improve security.<br>
<br>
<img src='http://jroller.com/evanx/resource/gnome-keys-250.png' align='right' />

We'll implement the crypto server in a subsequent article. <i>Then we can say, "Dual control? We have an app for that." ;)</i>

<h4>Conclusion</h4>

The problem with encryption is secure key management. We shouldn't leave the key under the mat.<br>
<br>
PCI requires that "split knowledge and dual control" be used to protect our data-encryption key so that no single person can extract the data in clear-text, not even our most trustworthy employee today, rogue tomorrow. Or victim of blackmail, or government coercion ;)<br>
<br>
We introduce our <tt>DualControlGenSecKey</tt> utility for generating a new secret key. We protect this key using password-based encryption, courtesy of <tt>JceKeyStore</tt>. We enforce split knowledge of the key password, so that dual control is required to load the key. Each split password is effectively a key-encryption key split between two custodians, and so known to no single person.<br>
<br>
We propose keeping at least three copies of the same key, but where each copy is password-protected by a different duo of custodians. Then when any one custodian is on vacation or otherwise indisposed, the other two custodians can restart our app.<br>
<br>
We note that we require two shared passwords (server SSL keystore/truststore and secret keystore), and as well two private passwords per custodian (client SSL keystore/truststore and secret key split password), and thereby hopefully keep our secret key, erm, secret.<br>
<br>
<h4>Furthermore</h4>

In "Dual Control Mock Console" we will present a unit test orchestrating <tt>DualControlManager</tt> and <tt>DualControlConsole</tt> threads - preview <a href='https://code.google.com/p/vellum/source/browse/trunk/test/dualcontrol/DualControlTest.java'><tt>DualControlTest.java</tt></a>

<img src='http://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Gnome-applications-office.svg/200px-Gnome-applications-office.svg.png' align='left' />

In "Dual Control Enroll" we will present tools to enroll and revoke custodians - preview <a href='https://code.google.com/p/vellum/source/browse/trunk/src/dualcontrol/DualControlEnroll.java'><tt>DualControlEnroll.java</tt></a> and <a href='https://code.google.com/p/vellum/source/browse/trunk/src/dualcontrol/DualControlRevoke.java'><tt>DualControlRevoke.java</tt></a>

In "Dual Control Crypto Server" we implement a dual-controlled crypto server to unburden our apps, simplify key management and enhance security - preview <a href='https://code.google.com/p/vellum/source/browse/trunk/src/dualcontrol/CryptoServer.java'><tt>CryptoServer.java</tt></a> and its <a href='https://code.google.com/p/vellum/source/browse/trunk/src/dualcontrol/CryptoHandler.java'><tt>CryptoHandler.java</tt></a>

In "Dual Control Key Protection" we address increased protection against brute-force password attacks e.g. via PBE of the keystore using <tt>PBKDF2</tt> with a high number of iterations, so that the key takes a second or two to recover, rather than half a millisecond - preview <a href='https://code.google.com/p/vellum/source/browse/trunk/src/dualcontrol/JceksBruteForceTimer.java'><tt>JceksBruteForceTimer.java</tt></a>, <a href='https://code.google.com/p/vellum/source/browse/trunk/src/dualcontrol/RecryptedKeyStore.java'><tt>RecryptedKeyStore.java</tt></a> and <a href='https://code.google.com/p/vellum/source/browse/trunk/src/dualcontrol/AesPbeStore.java'><tt>AesPbeStore.java</tt></a>

In "Dual Control Key Rotation" we'll address periodic key revision e.g. migrating from an older "dek2013" key to a new "dek2014" key.<br>
<br>
You can browse the code for this exercise at <a href='http://code.google.com/p/vellum'>code.google.com/vellum</a> in <a href='https://code.google.com/p/vellum/source/browse/#svn%2Ftrunk%2Fsrc%2Fdualcontrol'><tt>src/dualcontrol</tt></a> and <a href='https://code.google.com/p/vellum/source/browse/trunk/test/dualcontrol/'><tt>test/dualcontrol</tt></a>.<br>
<br>
<tt><a href='https://twitter.com/evanxsummers'>@evanxsummers</a></tt>