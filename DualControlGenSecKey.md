<h1>Dual Control Key Generation</h1>

We hereby start the new 2013 "Dual Control" quadrilogy, part of the <a href='http://code.google.com/p/vellum/wiki/EnigmaPosts'>Enigma Posts</a>.

<h4>Problem overview</h4>

Encryption is great for information security and all that. But the problem with encryption is... key management. An analogy often bandied about is that we lock the door but leave the key in the lock. <i>Or under the mat, but that's my personal favourite so ssh-ssh.</i>

The "Payment Card Industry Data Security Standard" (<a href='http://en.wikipedia.org/wiki/Payment_Card_Industry_Data_Security_Standard'>PCI DSS</a>) advocates common sense policies for building a secure network and protecting our data. Actually every enterprise should adopt PCI DSS because it's the only and best such thing we got. Although it focusses on credit card numbers (aka <i>Primary Account Numbers</i>, or PANs), it goes without saying that companies in other industries also have sensitive data that people might want to steal, governments even ;)

<img src='http://jroller.com/evanx/resource/Gnome-application-certificate-250-crop.png' align='left'>

PCI DSS suggests encrypting our data-encryption key (DEK) in order to protect it. Great, we now have a "key-encryption key" (KEK) that requires even more protection ;)<br>
<br>
PCI DSS mandates that manual key management requires "split knowledge and dual control" e.g. for key generation and loading. The intent is that no single person can extract the clear-text data.<br>
<br>
The glaring problem is that sysadmins are a single person, with god-like access to all our data, and de facto custodian of the proverbial keys to the kindgom. <i>Consequently sysadmins have root access ;)</i>

<h4>Solution overview</h4>

We'll split the knowledge of the key password between two admins, so it's known to no single person. Clearly dual control by those two admins is then required to load the key.<br>
<br>
We propose keeping at least three copies of the same key in our keystore, where each copy is password-protected by a different "split password" pairing. Then if one admin is on vacation or otherwise indisposed, that's OK because we only require two admins to load the key when we restart our app.<br>
<br>
<h4><tt>DualControlGenSecKey</tt></h4>

Step 1 for any data security endeavour is to generate an encryption key, which preferrably no one can pwn, not even root. Whereas <tt>keytool</tt> prompts for a password entered by a single admin, we introduce <tt>DualControlGenSecKey</tt> to handle multiple password submissions via SSL.<br>
<br>
<pre><code>public class DualControlGenSecKey {<br>
    private int submissionCount;<br>
    private String keyAlias;<br>
    private String keyStoreLocation;<br>
    private String keyStoreType;<br>
    private String keyAlg;<br>
    private int keySize;<br>
    private char[] keyStorePassword;<br>
    private Map&lt;String, char[]&gt; dualPasswordMap;<br>
    private SSLContext sslContext;<br>
<br>
    public DualControlGenSecKey(VellumProperties properties, MockableConsole console) {<br>
        this.properties = properties;<br>
        this.console = console;<br>
        submissionCount = properties.getInt("dualcontrol.submissions", 3);<br>
        keyStorePassword = properties.getPassword("storepass", null);<br>
        keyAlias = properties.getString("alias");<br>
    }<br>
<br>
    public void init() throws Exception {<br>
        sslContext = DualControlSSLContextFactory.createSSLContext(properties, console);<br>
    }<br>
    ...<br>
}<br>
</code></pre>

where we choose property names similar to <tt>keytool</tt>.<br>
<br>
<pre><code>    public static void main(String[] args) throws Exception {<br>
        DualControlGenSecKey instance = new DualControlGenSecKey(<br>
                new VellumProperties(System.getProperties()), <br>
                new ConsoleAdapter(System.console()));<br>
        try {<br>
            instance.init();<br>
            instance.call();<br>
        } catch (DualControlException e) {<br>
            instance.console.println(e.getMessage());<br>
        } finally {<br>
            instance.clear();<br>
        }<br>
    }<br>
    ...<br>
}<br>
</code></pre>
where our <tt>main()</tt> method passes <tt>System</tt> properties i.e. <tt>-D</tt> options, and the <tt>System</tt> console for entering the SSL keystore password.<br>
<br>
<pre><code>    public void call() throws Exception {<br>
        keyStoreLocation = properties.getString("keystore");<br>
        if (new File(keyStoreLocation).exists()) {<br>
            throw new Exception("Keystore file already exists: " + keyStoreLocation);<br>
        }<br>
        if (keyStorePassword == null) {<br>
            keyStorePassword = console.readPassword(<br>
                    "Enter passphrase for keystore (%s): ", keyStoreLocation);<br>
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
        return buildKeyStore(<br>
                new DaulControlManager(properties, submissionCount, purpose).<br>
                readDualMap(sslContext));<br>
    }<br>
</code></pre>

where <tt>DaulControlManager</tt> provides a map of aliases and passwords, composed from submissions via SSL. We pass this <tt>dualPasswordMap</tt> to the <tt>buildKeyStore()</tt> method below.<br>
<pre><code>    public KeyStore buildKeyStore(Map&lt;String, char[]&gt; dualPasswordMap) throws Exception {<br>
        keyAlias = properties.getString("alias");<br>
        keyStoreType = properties.getString("storetype");<br>
        keyAlg = properties.getString("keyalg");<br>
        keySize = properties.getInt("keysize");<br>
        KeyGenerator keyGenerator = KeyGenerator.getInstance(keyAlg);<br>
        keyGenerator.init(keySize);<br>
        SecretKey secretKey = keyGenerator.generateKey();<br>
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);<br>
        keyStore.load(null, null);<br>
        setEntry(keyStore, secretKey, keyAlias, dualPasswordMap);<br>
        return keyStore;<br>
    }<br>
<br>
    private static void setEntry(KeyStore keyStore, SecretKey secretKey,<br>
            String keyAlias, Map&lt;String, char[]&gt; dualPasswordMap) throws Exception {<br>
        KeyStore.Entry entry = new KeyStore.SecretKeyEntry(secretKey);<br>
        for (String dualAlias : dualPasswordMap.keySet()) {<br>
            char[] dualPassword = dualPasswordMap.get(dualAlias);<br>
            String alias = keyAlias + "-" + dualAlias;<br>
            KeyStore.ProtectionParameter prot =<br>
                    new KeyStore.PasswordProtection(dualPassword);<br>
            keyStore.setEntry(alias, entry, prot);<br>
        }<br>
    }<br>
</code></pre>

where for each duo we programmatically create a <tt>KeyStore</tt> entry containing the same key, but protected by a different key password, which is split between two people i.e. known to no single person :)<br>
<br>
In general one might argue that we should not write code per se, but rather tests with accompanying code, hand in glove. Well, we've done that in this case, for a change ;)<br>
<pre><code>    @Test<br>
    public void testGenKeyStore() throws Exception {<br>
        dualPasswordMap.put("brent-evanx", "bbbb+eeee".toCharArray());<br>
        dualPasswordMap.put("brent-henty", "bbbb+hhhh".toCharArray());<br>
        dualPasswordMap.put("evanx-henty", "eeee+hhhh".toCharArray());<br>
        properties.put("alias", "dek2013");<br>
        properties.put("storetype", "JCEKS");<br>
        properties.put("keyalg", "AES");<br>
        properties.put("keysize", "192");<br>
        DualControlGenSecKey instance = new DualControlGenSecKey();<br>
        KeyStore keyStore = instance.buildKeyStore(properties, dualPasswordMap);<br>
        assertEquals(3, Collections.list(keyStore.aliases()).size());<br>
        assertEquals("dek2013-brent-evanx", Lists.asSortedSet(keyStore.aliases()).first());<br>
        SecretKey key = getSecretKey(keyStore, "dek2013-brent-evanx", "bbbb+eeee".toCharArray());<br>
        assertEquals("AES", key.getAlgorithm());<br>
        assertTrue(Arrays.equals(key.getEncoded(), getSecretKey(keyStore, <br>
                "dek2013-brent-henty", "bbbb+hhhh".toCharArray()).getEncoded()));<br>
    }<br>
</code></pre>

where we inspect the <tt>KeyStore</tt> returned by the <tt>buildKeyStore()</tt> method, which is exposed especially for this unit test.<br>
<br>
<h4>Usage demo</h4>

Let's run <tt>DualControlGenSecKey</tt> from the command-line.<br>
<br>
<pre><code>  java -Ddualcontrol.submissions=3 -Ddualcontrol.minPasswordLength=8 \<br>
     -Dkeystore=$keystore -Dstoretype=JCEKS -Dstorepass=$storepass \<br>
     -Dalias=dek2013 -Dkeyalg=AES -Dkeysize=256 \<br>
     dualcontrol.DualControlGenSecKey<br>
</code></pre>

where we use a <tt>JCEKS</tt>-type keystore for our symmetric secret key, generated as 256bit AES, and aliased as "dek2013."<br>
<br>
For this example, three admins submit their passwords via SSL sockets where their client cert's <tt>CN</tt> identifies them as <tt>evanx</tt>, <tt>henty</tt> and <tt>brent</tt>.<br>
<br>
<pre><code>INFO [DaulControlManager] readDualMap submissionCount: 3<br>
INFO [DaulControlManager] readDualMap purpose: new key dek2013<br>
INFO [DaulControlManager] readSubmissions SSL port 4444<br>
INFO [DaulControlManager] Received evanx<br>
INFO [DaulControlManager] Received henty<br>
INFO [DaulControlManager] Received brent<br>
INFO [DaulControlManager] readDualMap dualAlias: brent-evanx<br>
INFO [DaulControlManager] readDualMap dualAlias: brent-henty<br>
INFO [DaulControlManager] readDualMap dualAlias: evanx-henty<br>
INFO [DualControlGenSecKey] alias dek2013-brent-evanx<br>
INFO [DualControlGenSecKey] alias dek2013-brent-henty<br>
INFO [DualControlGenSecKey] alias dek2013-evanx-henty<br>
</code></pre>

We see that <tt>DualControlGenSecKey</tt> creates secret key entries under the following "dual aliases."<br>
<br>
<pre><code>$ keytool -keystore $keystore -storetype JCEKS -storepass $storepass -list | grep Entry<br>
dek2013-brent-henty, 18 Aug 2013, SecretKeyEntry, <br>
dek2013-evanx-henty, 18 Aug 2013, SecretKeyEntry, <br>
dek2013-brent-evanx, 18 Aug 2013, SecretKeyEntry,<br>
</code></pre>

Actually these three keys are one and the same! However each copy has a different "split password," which is a combination of a pair of personal passwords. Consequently the key password is "known to no single person" as per PCI DSS requirements :)<br>
<br>
<h4>Furthermore</h4>

In the next article in this series, we will present <tt>DualControlConsole</tt> for submitting split passwords via SSL, as required for the above demo.<br>
<br>
Thereafter we will present <tt>DualControlManager</tt>, as used by <tt>DualControlGenSecKey</tt> to receive and combine these passwords. We will demonstrate how <tt>DualControlManager</tt> is similarly used by our app to load a key, with dual control.<br>
<br>
<h4>Conclusion</h4>

The problem with encryption is secure key management. We shouldn't leave the key under the mat.<br>
<br>
<img src='http://jroller.com/evanx/resource/gnome-keys-250.png' align='right' />

PCI requires that "split knowledge and dual control" be used to protect our data-encryption key so that no single person can extract the data in clear-text, not even our most trustworthy employee today, rogue tomorrow.<br>
<br>
We present a <tt>DualControlGenSecKey</tt> utility for generating secret keys that are really secret. We protect the data-encryption keys using password-based encryption, courtesy of <tt>JCEKeyStore</tt>. We enable split knowledge of the key password, so that dual control is required to load the key.<br>
<br>
We propose keeping at least three copies of the same key, but where each copy is password-protected by a different pair of admins. Then if one admin is on vacation or otherwise indisposed, that's OK because we only require two admins to load the key when we restart our app.<br>
<br>
<h4>Further reading</h4>

See an extended preview article: <a href='https://code.google.com/p/vellum/wiki/DualControl'>code.google.com/p/vellum/wiki/DualControl</a>.<br>
<br>
<h4>Resources</h4>

You can browse the code for this exercise at <a href='http://code.google.com/p/vellum'>code.google.com/vellum</a> in the <a href='https://code.google.com/p/vellum/source/browse/#svn%2Ftrunk%2Fsrc%2Fdualcontrol'><tt>dualcontrol</tt></a> package.<br>
<br>
<tt><a href='https://twitter.com/evanxsummers'>@evanxsummers</a></tt>