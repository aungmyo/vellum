# Cryptonomical #

## Introduction ##

The [PasswordHash](PasswordHash.md) prequel started our cryptographic foray.

Let's now investigate the [Java Cryptography Architecture](http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html) further, considering both symmetric (secret key) and asymmetric (public/private) algorithms.

Let's implement a client and server that mimic how SSL might work, where the client uses the server's public key to encrypt and transfer a secret key, which is then used to encrypt messages.


## Crypto Concepts ##

First let's review the important crypto concepts. Cos i keep forgetting them.

### Ciphers ###

Symmetric ciphers (eg. DES, AES) use the same shared secret key to encrypt and decrypt data.

Asymmetric ciphers (eg. RSA, DSA) use a public key to encrypt, and a private key to decrypt.


### Hash functions ###

Hash functions (eg. MD5, SHA-1) can produce a "message digest" or "digital fingerprint" of some data. The data cannot be reconstructed from the hash, i.e. it is one-way operation, to generate a fixed length code e.g. 160 bits for SHA-1, irrespective of the length of the input data. Check out PasswordHash.

### Digital signature ###

Creating a [digital signature](http://en.wikipedia.org/wiki/Image:Digital_signature_schema.gif) involves creating a hashcode of the data to be transmitted e.g. an important document to be emailed, and encrypting that hashcode using your private key. This encrypted hashcode is the digital signature. For example, MD5 or SHA-1 might be used to calculate the hashcode, and a RSA private key used to encrypt the hashcode.

The recipient can then decrypt the hashcode using your public key, calculate the actual hashcode of the document they have received, and verify that the hashcodes match.

### Digital certificate ###

A digital certificate, or [public key certificate](http://en.wikipedia.org/wiki/Public_key_certificate), is an artifact that uses a digital signature to bind together a public key with an identity e.g. the name, address et al of a person or organisation.

The signature might be that of a trusted Certificate Authority, i.e. it is signed by them using their private key. Alternatively, it might be self-signed i.e. signed by ourselves using our private key e.g. generated using [keytool](http://java.sun.com/j2se/1.5.0/docs/tooldocs/solaris/keytool.html).

```
$ keytool -keystore myKeystore -alias myself -genkey
$ keytool -keystore myKeystore -alias myself -export -rfc -file mycert.cer
$ keytool -printcert -file mycert.cer
&nbsp;
Owner: CN=evan summers, OU=aptframework, O=aptframework.net, L=south africa, ST=wp, C=za
Issuer: CN=evan summers, OU=aptframework, O=aptframework.net, L=south africa, ST=wp, C=za
Serial number: 4535ee4f
Valid from: Wed Oct 18 11:05:19 CAT 2006 until: Tue Jan 16 11:05:19 CAT 2007
Certificate fingerprints:
         MD5:  90:B1:1C:60:59:43:65:7D:A5:A9:ED:DA:B9:D4:7D:C4
         SHA1: A2:36:AA:87:0A:AA:AD:2B:69:81:F2:17:2C:07:3C:69:F8:E1:9A:81
         Signature algorithm name: SHA1withDSA
         Version: 1
```

### X509 ###

According to [Wikipedia](http://en.wikipedia.org/wiki/X.509),

<blockquote>
X.509 is an ITU-T standard for public key infrastructure (PKI).<br>
X.509 specifies, amongst other things, standard formats for public key certificates<br>
and a certification path validation algorithm.<br>
</blockquote>

### Message Authentication Code ###

According to [Wikipedia](http://en.wikipedia.org/wiki/HMAC),

<blockquote>
A cryptographic message authentication code (MAC) is a short piece of information used to authenticate a message. A MAC algorithm accepts as input a secret key and an arbitrary-length message to be authenticated, and outputs a MAC (sometimes known as a tag). The MAC value protects both a message's integrity as well as its authenticity, by allowing verifiers (who also possess the secret key) to detect any changes to the message content.<br>
<br>
MACs differ from digital signatures, as MAC values are both generated and verified using the same secret key. This implies that the sender and receiver of a message must agree on keys before initiating communications, as is the case with symmetric encryption.<br>
<br>
A keyed-hash message authentication code, or HMAC, is a type of message authentication code (MAC) calculated using a cryptographic hash function in combination with a secret key. As with any MAC, it may be used to simultaneously verify both the data integrity and the authenticity of a message. Any iterative cryptographic hash function, such as MD5 or SHA-1, may be used in the calculation of an HMAC; the resulting MAC algorithm is termed HMAC-MD5 or HMAC-SHA-1 accordingly.<br>
</blockquote>

### Diffie-Hellman ###

According to [Wikipedia](http://en.wikipedia.org/wiki/Diffie-Hellman),

<blockquote>
Diffie-Hellman (D-H) key exchange is a cryptographic protocol that allows two parties that have no prior knowledge of each other to jointly establish a shared secret key over an insecure communications channel. This key can then be used to encrypt subsequent communications using a symmetric key cipher.<br>
</blockquote>

For example, a shared secret key established using Diffie-Hellman might be used to symmetrically encrypt messages, or to create MACs.

[OpenID's](http://openid.net/specs/openid-authentication-2_0-11.html) "DH-SHA256 association" uses the Diffie-Hellman secret value to encrypt a random MAC key (for secure transmission) for subsequent message authentification e.g. using HMAC-SHA256.

## Crash Test Dummy ##

As an exercise, we create a client and server that are gonna try to communicate securely by encrypting messages, after some kinda key exchange agreement.

```
public class CryptonomicalDemo {
    static CryptonomicalProperties properties = new CryptonomicalProperties();
    
    CryptonomicalServer server = new CryptonomicalServer();
    CryptonomicalClient client = new CryptonomicalClient();
    
    protected void test() throws Exception {
        server.bind(properties.serverPort);
        server.start();
        client.connect(properties.serverHost, properties.serverPort);
        client.start();
    }
    
    public static void main(String[] args) {
        try {
            new CryptonomicalDemo().test();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

where we bind the server to a `ServerSocket`, and then get the client to
connect to the server.

## Client ##

The client connects to the server port, negotiates a key exchange, and then communicates securely.

```
public class CryptonomicalClient extends Thread {
    CryptonomicalSocket cryptoSocket;
    
    public void connect(String host, int port) throws UnknownHostException,
            IOException, NoSuchAlgorithmException {
        Socket clientSocket = new Socket(host, port);
        cryptoSocket = new CryptonomicalSocket(clientSocket);
        cryptoSocket.init();
    }
    
    public void run() {
        try {
            String publicKey = sendRequest(cryptoRequest);
            cryptoSocket.setEncodedPublicKey(publicKey);
            cryptoSocket.generateSecretKey();
            String encryptedSecretKey = cryptoSocket.encryptSecretKey();
            String response = sendRequest(encryptedSecretKey);
            if (!response.equals(cryptoAcknowledge)) throw new RuntimeException();
            cryptoSocket.setEncrypt(true);
            process();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cryptoSocket.close();            
        }
    }
    
    public <T> T sendRequest(Object request) throws Exception {
        cryptoSocket.writeObject(request);
        return (T) cryptoSocket.readObject();
    }

    protected void process() throws Exception {
        String response = sendRequest("ALL YOUR BASE ARE BELONG TO US.");
        logger.info(response);
    }        
}
```

where `cryptoRequest` and `cryptoAcknowledge` are just `static` strings as follows.

```
    public static final String cryptoRequest = "we get signal (v1)";
    public static final String cryptoAcknowledge = "main screen turn on";    
```

Once key exchange has been accomplished, we can securely send the server a test [message](http://en.wikipedia.org/wiki/All_your_base_are_belong_to_us).

## Server ##

Our server waits to `accept()` incoming connections, which are handled by
a `CryptonomicalThread`.

```
public class CryptonomicalServer extends Thread {
    ServerSocket serverSocket;
    boolean isRunning = true; 
    
    public void bind(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    
    public void run() {
        while (isRunning) {
            try {
                new CryptonomicalThread(serverSocket.accept()).start();
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }
    }        
}
```


## Server Thread ##

Our server thread negotiates a key agreement, and then responds to a secure message from the client.

```
public class CryptonomicalThread extends Thread {
    CryptonomicalSocket cryptoSocket;
    
    public CryptonomicalThread(Socket clientSocket) {
        this.cryptoSocket = new CryptonomicalSocket(clientSocket);
    }
    
    public void run() {
        try {
            cryptoSocket.init();
            cryptoSocket.generateKeyPair();
            String string = cryptoSocket.readObject();
            if (!string.equals(cryptoRequest)) throw new RuntimeException(string);
            String publicKey = cryptoSocket.getEncodedPublicKey();
            cryptoSocket.writeObject(publicKey);
            String encryptedSecretKey = cryptoSocket.readObject();
            cryptoSocket.setEncodedSecretKey(encryptedSecretKey);
            cryptoSocket.writeObject(cryptoAcknowledge);
            cryptoSocket.setEncrypt(true);
            process();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cryptoSocket.close();
        }
    }
    
    protected void process() throws Exception {
        Object request = cryptoSocket.readObject();
        logger.info(request);
        cryptoSocket.writeObject("WHAT YOU SAY !!");
    }
}
```

where we generate a new key pair for every connection, which is not necessary. We should create a static key pair for the server beforehand, and use this for all connections. In order to protect against a [man-in-the-middle attack](http://en.wikipedia.org/wiki/Man_in_the_middle_attack), we need to authenticate the server's public key, e.g. via a third party
such as a Certificate Authority. Alternatively, we could install the server's public key in the client beforehand, e.g. encoded into a <tt>static</tt> base-64 string, or resource file.

In the above implementation we send the server's public key to the client, and receive back a secret key encrypted with that public key. The server decrypts the secret key using its private key, and now the client and server share the secret key to use for symmetric encryption of the traffic between them.

An alternative approach is to use [Diffie-Hellman](http://www.exampledepot.com/egs/javax.crypto/KeyAgree.html) to generate a shared secret key.

## Cryptonomical Socket ##

Both the client and server instantiate a `CryptonomicalSocket`. This wraps the `Socket` to support a key agreement, and subsequent symmetric encryption of messages.

```
public class CryptonomicalSocket {
    Socket socket;
    InputStream inputStream;
    OutputStream outputStream;
    AsymmetricCipher asymmetricCipher = new AsymmetricCipher();    
    SymmetricCipher symmetricCipher = new SymmetricCipher();    
    boolean encrypt = false;
    
    public CryptonomicalSocket(Socket socket) {
        this.socket = socket;
    }
    
    public void init() throws IOException {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }
    ...
}    
```

where we create an `asymmetricCipher` and `symmetricCipher`.

### Writing and Reading Objects ###

We write and read objects from the socket streams as follows.

```
    public void writeObject(Object object) throws Exception {
        String text = encodeXml(object);
        if (encrypt) text = Base64.encode(symmetricCipher.encrypt(text.getBytes()));
        text = text + "\n\n";
        outputStream.write(text.getBytes());
    }

    public <T> T readObject() throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        while (true) {
            String string = reader.readLine();
            if (string == null) break;
            if (string.trim().length() == 0) break;
            builder.append(string);
        }
        String text = builder.toString().trim();
        if (encrypt) text = new String(symmetricCipher.decrypt(Base64.decode(text)));
        return (T) decodeXml(text);
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;        
    }
```

where we perform encryption using the symmetric cipher if the `encrypt` property is `true`.

We write the object as text followed by a blank line, and so use `readLine()` to read in the text, until we reach the blank line.

### XML Encoding for Objects ###

Objects are encoded into XML text using `java.beans.XMLEncoder` and `XMLDecoder` as follows.

```
    protected String encodeXml(Object object) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encodeXml(object, outputStream);
        return new String(outputStream.toByteArray());
    }
    
    protected void encodeXml(Object object, OutputStream outputStream) {
        XMLEncoder encoder = new XMLEncoder(outputStream);
        encoder.writeObject(object);
        encoder.close();
    }        

    protected Object decodeXml(String string) {
        return decodeXml(new ByteArrayInputStream(string.getBytes()));
    }
    
    protected Object decodeXml(InputStream inputStream) {
        XMLDecoder decoder = new XMLDecoder(inputStream);
        return decoder.readObject();
    }
```

In our example, we write and read `String` instances only, which are trivially encoded to XML.

### Crypto ###

The symmetric secret key is encrypted by the client using the asymmetric cipher i.e. the public key of the server.

```
    public String encryptSecretKey() throws Exception {
        byte[] encodedKey = symmetricCipher.getSecretKey().getEncoded();
        encodedKey = asymmetricCipher.encrypt(encodedKey);
        String keyString = Base64.encode(encodedKey);
        return keyString;
    }
```

When the server receives the secret key, it decrypts it using the asymmetric cipher i.e. its private RSA key.

```
    public void setEncodedSecretKey(String key) throws Exception {
        byte[] encodedKey = Base64.decode(key);
        encodedKey = asymmetricCipher.decrypt(encodedKey);
        symmetricCipher.setEncodedSecretKey(encodedKey);
    }
```

The following methods delegate to its `asymmetricCiper` and `symmetricCipher` instances.

```
    public void generateKeyPair() throws NoSuchAlgorithmException {
        asymmetricCipher.generateKeyPair();
    }

    public String getEncodedPublicKey() {
        return asymmetricCipher.getEncodedPublicKey();
    }

    public void setEncodedPublicKey(String key) 
    throws NoSuchAlgorithmException, InvalidKeySpecException {
        asymmetricCipher.setEncodedPublicKey(key);
    }   

    public void generateSecretKey() throws NoSuchAlgorithmException {
        symmetricCipher.generateSecretKey();
    }
        
    public String getEncodedSecretKey() {
        return symmetricCipher.getEncodedSecretKey();
    }
    
```

where the secret key relates to the symmetric cipher, and the public key to the asymmetric one.

## Asymmetric Cipher ##

We support asymmetric ciphers as follows.

```
public class AsymmetricCipher {
    static final String asymmetricAlgorithm = "RSA";
    static final String asymmetricAlgorithmModePadding = "RSA/ECB/PKCS1Padding";
    static final int keySize = 1024;
    
    KeyPair keyPair;
    PublicKey publicKey;
    
    public void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = 
                KeyPairGenerator.getInstance(asymmetricAlgorithm);
        keyPairGenerator.initialize(keySize);
        keyPair = keyPairGenerator.generateKeyPair();
        publicKey = keyPair.getPublic();
    }
    
    public String getEncodedPublicKey() {
        byte[] encodedKey = publicKey.getEncoded();
        return Base64.encode(encodedKey);
    }
    
    public void setEncodedPublicKey(String key)
    throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encodedKey = Base64.decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
        publicKey = KeyFactory.getInstance(asymmetricAlgorithm).generatePublic(keySpec);
    }
    ...
}    
```

The server invokes `generateKeyPair()`, and sends the public key to the client. In `getEncodedPublicKey()` we encode the public key into a byte array using `publicKey.getEncoded()`. This produces a byte array in which the key is encoded in a format conforming to the X509 spec. We then use using base-64 encoding to convert the byte array into a string for transmission to the client.

The client invokes `setEncodedPublicKey()` with the encoded public key it receives from the server. This is decoded using `X509EncodedKeySpec` and a `PublicKey` object is created by a `KeyFactory` instance.

### Ciphering ###

The following methods support encryption and decryption using this asymmetric cipher.

```
    public Cipher createEncryptCipher() 
    throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException  {
        Cipher encryptCipher = Cipher.getInstance(asymmetricAlgorithmModePadding);
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return encryptCipher;
    }

    public Cipher createDecryptCipher() 
    throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException  {
        Cipher decryptCipher = Cipher.getInstance(asymmetricAlgorithmModePadding);
        decryptCipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        return decryptCipher;
    }

    public byte[] encrypt(byte[] bytes) throws Exception {
        return createEncryptCipher().doFinal(bytes);
    }
    
    public byte[] decrypt(byte[] bytes) throws Exception {
        return createDecryptCipher().doFinal(bytes);
    }    
```

where the `publicKey` is used for encryption, and the private key for decryption.

### Silent Failure ###

Incidently, the asymmetric ciphers in Sun's default crypto implementation do not appear to support multiple chunks of data (to handle data larger than the key size), and do not throw an exception when this is attempted. Consequently and inconsequently, `CipherOutputStream` and `CipherInputStream` might fail silently too. As explained in their JDK API docs,

<blockquote>
<code>CipherInputStream</code> adheres strictly to the semantics, especially the failure semantics, of its ancestor classes <code>java.io.FilterInputStream</code> and <code>java.io.InputStream</code>. This class has exactly those methods specified in its ancestor classes, and overrides them all. Moreover, this class catches all exceptions that are not thrown by its ancestor classes.<br>
</blockquote>

So crypto exceptions thrown by the ciphers (eg. data chunk larger than key size) are swallowed by the cipher streams e.g. not rethrown even as wrapped runtime exceptions.

### Asymmetric Cipher Test ###

We test the above asymmetric cipher as follows.

```
public class AsymmetricCipherTest {
    AsymmetricCipher cipher = new AsymmetricCipher();
    
    protected void test() throws Exception {
        cipher.generateKeyPair();
        String text = "Let's test this baby...";
        byte[] bytes = text.getBytes();
        bytes = cipher.encrypt(bytes);
        bytes = cipher.decrypt(bytes);
        text = new String(bytes);
        System.out.println(text);
    }
    
    public static void main(String[] args) {
        try {
            new AsymmetricCipherTest().test();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## Symmetric Cipher ##

We support symmetric ciphers as follows.

```
public class SymmetricCipher {
    static final String symmetricAlgorithm = "AES";
    static final int symmetricKeySize = 128;
    
    SecretKey secretKey;
    
    public SymmetricCipher() {
    }
    
    public void generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(symmetricAlgorithm);
        keyGenerator.init(symmetricKeySize);
        secretKey = keyGenerator.generateKey();
    }
        
    public void setEncodedSecretKey(byte[] encodedKey) 
    throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(encodedKey, symmetricAlgorithm);
        secretKey = secretKeySpec;
    }
    ...
}    
```

The client invokes `generateSecretKey()` and will encrypt the secret key using
the asymmetric cipher's public key, for secure transmission to the server.

The server invokes `setEncodedSecretKey()` once it has decrypted the encoded
secret key from the client i.e. `secretKey.getEncoded()`,
using the asymmetric cipher's private key.

### Ciphering ###

Similarly to `AsymmetricCipher`, the following methods support encryption and decryption.

```
    public Cipher createEncryptCipher() 
    throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException  {
        Cipher encryptCipher = Cipher.getInstance(symmetricAlgorithm);
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return encryptCipher;
    }

    public Cipher createDecryptCipher() 
    throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException  {
        Cipher decryptCipher = Cipher.getInstance(symmetricAlgorithm);
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
        return decryptCipher;
    }

    public byte[] encrypt(byte[] bytes) throws Exception {
        return createEncryptCipher().doFinal(bytes);
    }
    
    public byte[] decrypt(byte[] bytes) throws Exception {
        return createDecryptCipher().doFinal(bytes);
    }
```

where the same shared `secretKey` is used for both encryption and decryption, in this symmetric case.

### Symmetric Cipher Test ###

We test the above symmetric cipher as follows.

```
public class SymmetricCipherTest {
    SymmetricCipher cipher = new SymmetricCipher();
    ...    
    protected void test() throws Exception {
        cipher.generateSecretKey();
        String text = "Let's test this baby...";
        byte[] bytes = text.getBytes();
        bytes = cipher.encrypt(bytes);
        bytes = cipher.decrypt(bytes);
        text = new String(bytes);
        System.out.println(text);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream outputStream = new CipherOutputStream(byteArrayOutputStream, 
                cipher.createEncryptCipher());
        outputStream.write(bytes);
        outputStream.flush();
        InputStream inputStream = new ByteArrayInputStream(
                byteArrayOutputStream.toByteArray());
        inputStream = new CipherInputStream(inputStream, 
                cipher.createDecryptCipher());
        inputStream.read(bytes);
        text = new String(bytes);
        System.out.println(text);        
    }
}
```

where additionally we test with `CipherOutputStream` and `CipherInputStream`.

## Conclusion ##

We consider both symmetric and asymmetric ciphers by way of implementing a trivial client and server that mimic how SSL might work. The client uses the server's public key to asymmetrically encrypt and transfer a secret key, which is then used by both sides to encrypt messages using a symmetric cipher.

## Resources ##

You can browse the code for this exercise at [here](http://code.google.com/p/vellum/source/browse/#svn/trunk/src/vellumdemo/cryptonomicaldemo).