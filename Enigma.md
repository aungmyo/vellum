## Introduction ##

In [PasswordHash](PasswordHash.md) we started our cryptographic foray, which later included Password-Based Encryption.

The [Cryptonomical](Cryptonomical.md) prequel investigated the [Java Cryptography Architecture](http://java.sun.com/j2se/1.5.0/docs/guide/security/CryptoSpec.html) further, considering both symmetric (secret key) and asymmetric (public key) algorithms. We implemented a client and server that mimics how SSL works, where the client uses the server's public key to encrypt and transfer a secret key, which is then used to encrypt messages.

Now for the main event, we use the [Java Secure Socket Extension (JSSE)](http://www.panix.com/~mito/articles/articles/jsse/j-jsse-ltr.pdf), to build a trivial client/server demo using `SSLServerSocket` and `SSLSocket`.

## Crypto Concepts ##

First let's review the relevant crypto concepts again, cos i keep forgetting them.

### Digital signature ###

A [digital signature](http://en.wikipedia.org/wiki/Image:Digital_signature_schema.gif) for a document is created by generating a hashcode of the document, and then encrypting that hashcode using your private key. This encrypted hashcode is the digital signature.

The recipient of the document and its digital signature can then decrypt the hashcode using your public key, calculate the actual hashcode of the document, and verify that the hashcodes match, in which case the document is considered authentic.

### Digital certificate ###

A digital certificate, or [public key certificate](http://en.wikipedia.org/wiki/Public_key_certificate) is an artifact that uses a digital signature to bind together a public key with an identity document e.g. specifying the name, URL et al of a person or organisation.

The signature might be that of a trusted [Certificate Authority](http://en.wikipedia.org/wiki/Certificate_authority) i.e. it is signed by them using their private key. Alternatively, it might be self-signed i.e. signed by ourselves using our private key e.g. generated using [keytool](http://java.sun.com/j2se/1.5.0/docs/tooldocs/windows/keytool.html).

### X.509 ###

According to [Wikipedia](http://en.wikipedia.org/wiki/X.509),

<blockquote>
X.509 is an ITU-T standard for public key infrastructure (PKI).<br>
X.509 specifies, amongst other things, standard formats for public key certificates<br>
and a certification path validation algorithm.<br>
</blockquote>


## Java Key Store ##

Consider that we wish to build a client/server application, using SSL to encrypt communications over an insecure network e.g. the Internet. We expect that we'll have to create at least a self-signed digital certificate for the server.

We use [keytool](http://java.sun.com/j2se/1.5.0/docs/tooldocs/windows/keytool.html) to create a Java Key Store (JKS), which we'll refer to as a "keystore file," and to generate private key entries therein, and also to export and import public key certificate entries.

### Private Key Entry ###

First we create a public/private key pair for our server, (see [keytool](http://java.sun.com/j2se/1.5.0/docs/tooldocs/windows/keytool.html)) i.e. a "private key entry" in a keystore file, which it creates automatically.

```
$ keytool -genkey -alias serverprivate -keystore serverprivate \
          -storepass storepassword -keypass storepassword \
          -dname "CN=EnigmaDemo, OU=ditto, O=ditto, L=ct, S=wp, C=za" 
&nbsp;
$ keytool -list -keystore serverprivate -storepass storepassword
&nbsp;
Keystore type: JKS
Keystore provider: SUN
Your keystore contains 1 entry
serverprivate, 2007/05/23, PrivateKeyEntry,
Certificate fingerprint (MD5): F6:8D:A9:6F:9D:54:E8:CC:ED:E6:7B:11:B2:4D:24:D1
```

where the `-alias` is used to refer to a key entry in the keystore.

The keystore is created as a file named `serverprivate` in the current directory. In order to recreate it, we delete the existing file before running the `keytool` command.

### Digital Certificate File ###

We can export the above "private key entry" as a self-signed X.509 public key certificate file, as follows.

```
$ keytool -export -rfc -file servercert -alias serverprivate -keystore serverprivate \
          -storepass storepassword  
&nbsp;
$ keytool -printcert -file servercert
&nbsp;
Owner: CN=EnigmaDemo, OU=ditto, O=ditto, L=ct, ST=wp, C=za
Issuer: CN=EnigmaDemo, OU=ditto, O=ditto, L=ct, ST=wp, C=za
Serial number: 45eaa1dc
Valid from: Sun Mar 04 12:39:24 CAT 2007 until: Sat Jun 02 12:39:24 CAT 2007
Certificate fingerprints:
         MD5:  5E:5A:60:BF:A7:33:DB:79:AC:C0:A1:76:F4:55:3B:60
         SHA1: 9E:CC:8F:B1:62:A7:B7:72:78:83:65:9E:55:F4:B4:EA:ED:44:02:23
         Signature algorithm name: SHA1withDSA
         Version: 3
```

where the `-rfc` option requests printable encoding format, rather than binary.

The X.509 public key certificate is created as a file named `servercert` in the current directory.

### Trusted Certificate Entry ###

We can import a digital certificate into its own keystore, as a "trusted certificate entry,"
as follows.

```
$ keytool -import -file servercert -alias serverpublic -keystore serverpublic \
          -noprompt -storepass publicstorepassword
&nbsp;
$ keytool -list -keystore serverpublic -storepass publicstorepassword
&nbsp;
Keystore type: JKS
Keystore provider: SUN
Your keystore contains 1 entry
serverpublic, 2007/03/04, trustedCertEntry,
Certificate fingerprint (MD5): 9A:05:FE:39:85:79:BA:54:06:C5:F9:64:42:6D:FD:E6
```

where the JKS file named `serverpublic` is created automatically (if it doesn't already exist), and the certificate is imported into this keystore.

This entry is a "trusted certificate entry" rather than a "private key entry" as created using `-genkey`. In our example, it is a digital certificate providing the server's public key.

We might copy the certificate keystore file into a `resource` subdirectory in the client project `src`, in which case our Java Webstart client can read the server's public key from its jar using `getResourceAsStream()`. Alternatively, we copy the file to the client's file system.

### Client Trust Store ###

According to the [Java Developers Almanac](http://www.exampledepot.com/egs/javax.net.ssl/Client.html),
<blockquote>
When an SSL client socket connects to an SSL server, it receives a certificate of authentication from the server. The client socket then validates the certificate against a set of certificates in its truststore.<br>
<br>
The default truststore is <code>$JAVAHOME/lib/security/cacerts</code>. If the server's certificate cannot be validated with the certificates in the truststore, the server's certificate must be added to the truststore before the connection can be established.<br>
</blockquote>

Incidently, we can supplement the [Certificate Authority](http://en.wikipedia.org/wiki/Certificate_authority) certificates in the `cacerts` keystore, with a new keystore file named `jre/lib/security/jssecacerts`, e.g. copy `serverpublic` to the client JRE's `lib/security` directory and rename it to `jssecacerts`.

Alternatively, we can specify our server's public key certificate keystore as the default trust store for the client, using the following VM options.

```
-Djavax.net.ssl.trustStore=/path/serverpublic
-Djavax.net.ssl.trustStorePassword=publicstorepassword
```

where we specify the path to our `serverpublic` file.

Then we can create an `SSLSocket` as follows.

```
 public class SimpleClient extends Thread { 
    SSLSocket clientSocket;
    
    public void connect(String host, int port) throws Exception {
        SocketFactory socketFactory = SSLSocketFactory.getDefault();
        clientSocket = (SSLSocket) socketFactory.createSocket(host, port);
    }
    ...
}    
```

(See the [Java Developers Almanac](http://www.exampledepot.com/egs/javax.net.ssl/Client.html).)

### Server Key Store ###

According to the [Java Developers Almanac](http://www.exampledepot.com/egs/javax.net.ssl/Server.html)
<blockquote>
An SSL server socket requires certificates that it will send to clients for authentication. The certificates must be contained in a keystore whose location must be explicitly specified.<br>
</blockquote>

The keystore location is specified using the following VM options.

```
-Djavax.net.ssl.keyStore=/path/serverprivate
-Djavax.net.ssl.keyStorePassword=storepassword
```

Then we can create an `SSLServerSocket` as follows.

```
public class SimpleServer extends Thread {
    SSLServerSocket serverSocket;
    
    public void bind(int port) throws Exception {
        ServerSocketFactory serverSocketFactory = SSLServerSocketFactory.getDefault();
        serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
    }
    ...
}    
```

(See the [Java Developers Almanac](http://www.exampledepot.com/egs/javax.net.ssl/Server.html).)


## Crash Test Dummy ##

As an exercise, we create a client and server that are gonna communicate securely using SSL.

```
public class EnigmaDemo {
    static EnigmaCommonProperties properties = new EnigmaCommonProperties();
        
    EnigmaServer server = new EnigmaServer();
    EnigmaClient client = new EnigmaClient();
    
    protected void test() throws Exception {
        server.init();
        server.bind(properties.sslPort);
        server.start();
        client.init();
        client.connect(properties.host, properties.sslPort);
        client.start();
    }
    
    public static void main(String[] args) throws Exception {
        try {
            new EnigmaDemo().test();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(1000);
        System.exit(0);
    }
}
```

where we will bind the server to an `SSLServerSocket` on the given `sslPort` e.g. 443, and then get the client to connect to the server socket using a client `SSLSocket`.

We exit the VM after enough time to run the demo, to make sure we don't leave any processes hanging around, e.g. in the event of the client or server thread encountering an exception, and the other thread still waiting endlessly.

## Public Key Store ##

Let's assume that we are not using the `-Djavax.net.ssl.trustStore` and `trustStorePassword` VM options.

We specify the public keystore location and password in `EnigmaCommonProperties`.

```
public class EnigmaCommonProperties {
    String host = "localhost";
    int port = 80;
    int sslPort = 443;    
    String serverPublicKeyStorePassword = "publicstorepassword";
    String serverPublicKeyStoreResource = "/ssldemo/resource/serverpublic";
}
```

where we have copied the `serverpublic` keystore file as resource in our jar. This keystore contains the server's trusted digital certificate entry, as created below.

```
$ keytool -import -file servercert -alias serverpublic -keystore serverpublic \
          -noprompt -storepass publicstorepassword
```

where the `servercert` file was generated further above using `-export`, from a private key entry in the `serverprivate` keystore, which was created using `-genkey`.

## Client ##

We now implement the client thread as follows, disregarding the `-Djavax.net.ssl.trustStore` option.

```
public class EnigmaClient extends Thread {
    static EnigmaCommonProperties properties = new EnigmaCommonProperties();
    static QLogger logger = QLogger.getLogger(EnigmaClient.class);
    
    SSLSocket clientSocket;
    EnigmaSocket enigmaSocket;
    KeyStore keyStore;
    KeyManager[] keyManagers;
    TrustManager[] trustManagers;
    SecureRandom secureRandom;
    SSLContext sslContext;
    
    public EnigmaClient() {
    }
    
    public void init() throws Exception {
        initKeyManagers();
        initTrustManagers();
        initSSLContext();
    }
    
    public void connect(String host, int port) throws Exception {
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        clientSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);
        inspectCertificates();
        this.enigmaSocket = new EnigmaSocket(clientSocket);
    }
    ...
}    
```

where we need to initialise `SSLContext` before attempting to `connect()` using SSL.

In `EnigmaDemo` further above, we invoke the above `init()`, then `connect()`, and then start the client thread, which will invoke `process()` as seen below.

```
    public void run() {
        try {
            enigmaSocket.init();            
            process();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            enigmaSocket.close();            
        }
    }
    
    protected void process() throws Exception {
        String response = enigmaSocket.sendRequest(
                "How could they cut the power? They're animals!");
        logger.info(response);
    }
```

where we send the server a test [http://www.imdb.com/title/tt0090605/quotes](message.md) in `process()`.

We can inspect the server's certificate as follows.

```
    protected void inspectCertificates() throws IOException {
        clientSocket.startHandshake();
        X509Certificate[] serverCertificates = (X509Certificate[])
                clientSocket.getSession().getPeerCertificates();
        for (X509Certificate certificate : serverCertificates) {
            logger.info(certificate.getIssuerDN().toString());
        }
    }
```

### Client SSL Context ###

We instantiate `keyManagers` and `trustManagers`, and then initialise `SSLContext`, as follows.

```
    protected void initKeyManagers() throws Exception {
        this.keyStore = KeyStore.getInstance("JKS");
        InputStream inputStream = getClass().getResourceAsStream(
                properties.serverPublicKeyStoreResource);
        keyStore.load(inputStream, 
                properties.serverPublicKeyStorePassword.toCharArray());
        KeyManagerFactory keyManagerFactory = 
                KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, null); 
        this.keyManagers = keyManagerFactory.getKeyManagers();
    }

    protected void initTrustManagers() throws Exception {
        TrustManagerFactory trustManagerFactory = 
                TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(keyStore);        
        this.trustManagers = trustManagerFactory.getTrustManagers();
    }
    
    protected void initSSLContext() throws Exception {
        sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagers, trustManagers, secureRandom);
    }        
```

where the `trustManagerFactory` is initialised with the `keyStore`. We access the keystore using `getResourceAsStream()`, and specify the keystore password.

### Empty Trust Manager ###

Incidently, if the server's certificate cannot be verified from `cacerts` e.g. it is self-signed, and you do not wish to add the certificate to `jssecacerts` or a specified trust store, and you wish to connect anyway e.g. to enjoy encryption if not URL authentification, one can create an "empty" trust manager for the client as follows.

```
    protected void initEmptyTrustManagers() throws Exception {
        trustManagers = new TrustManager[] {new EmptyTrustManager()};
    }
```

where `EmptyTrustManager` implements `X509TrustManager` as follows.

```
public class EmptyTrustManager implements X509TrustManager {
    
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
    
    public void checkClientTrusted(X509Certificate[] certs, String authType) {
    }
    
    public void checkServerTrusted(X509Certificate[] certs, String authType) {
    }
}
```

where `checkServerTrusted()` is empty i.e. does not actually check certificates
and so never throws an exception.

(See the [Java Developers Almanac's example](http://www.exampledepot.com/egs/javax.net.ssl/TrustAll.html).)

(See [swingx-ws' various TrustManager implementations](https://swingx-ws.dev.java.net/source/browse/swingx-ws/src/java/org/jdesktop/http).)

### Default Trust Store ###

Alternatively, we can specify our server's public key certificate
keystore as the default trust store for the client, using the following VM options.

```
-Djavax.net.ssl.trustStore=/path/serverpublic 
-Djavax.net.ssl.trustStorePassword=publicstorepassword
```

In this case, our client is simpler, as below.

```
public class SimpleClient extends Thread {
    static Logger logger = Logger.getLogger(SimpleClient.class.getName());
    
    SSLSocket clientSocket;
    EnigmaSocket enigmaSocket;
    
    public void connect(String host, int port) throws Exception {
        SocketFactory socketFactory = SSLSocketFactory.getDefault();
        clientSocket = (SSLSocket) socketFactory.createSocket(host, port);
        inspectCertificates();
        this.enigmaSocket = new EnigmaSocket(clientSocket);
    }
    ...
}
```

where we use the `SSLSocketFactory.getDefault()` rather than `SSLContext`, and need not instantiate any `KeyStore`, `KeyManager` or `TrustManager` instances.

## Server ##

Our server waits to `accept()` incoming connections, which are handled by an `EnigmaThread`.

```
public class EnigmaServer extends Thread {
    EnigmaServerProperties properties = new EnigmaServerProperties();
    
    KeyStore keyStore;
    SSLContext sslContext;
    KeyManager[] keyManagers;
    TrustManager[] trustManagers;
    SecureRandom secureRandom;
    SSLServerSocket serverSocket;
    boolean isRunning = true;
    ...        
    public void bind(int port) throws Exception {
        SSLServerSocketFactory sslServerSocketFactory =
                sslContext.getServerSocketFactory();
        this.serverSocket = (SSLServerSocket) sslServerSocketFactory.
                createServerSocket(port);
    }
    
    public void run() {
        while (isRunning) {
            try {
                new EnigmaThread(serverSocket.accept()).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }
    }
}
```

where we bind to an `SSLServerSocket` using the `SSLContext` to create a `SSLServerSocketFactory`.

### Private Key Store ###

The details of the server's private keystore location and passwords are specified in `EnigmaServerProperties` below.

```
public class EnigmaServerProperties extends EnigmaCommonProperties {
    String serverKeyStorePassword = "storepassword";
    String serverKeyPassword = "storepassword";
    String serverKeyStoreFileName = System.getProperty("javax.net.ssl.keyStore");
}
```

where we specify the path to the `serverprivate` keystore file on the command-line.
This keystore contains the server's private key entry, as generated below.

```
$ keytool -genkey -alias serverprivate -keystore serverprivate \
          -storepass storepassword -keypass storepassword \
          -dname "CN=EnigmaDemo, OU=ditto, O=ditto, L=ct, S=wp, C=za" 
```

### Server SSL Context ###

As in the case of the client, we might instantiate `keyManagers` and `trustManagers`, with which to initialise `SSLContext`.

```
    public void init() throws Exception {
        initKeyManagers();
        initTrustManagers();
        initSSLContext();
    }
    
    protected void initKeyManagers() throws Exception {
        keyStore = KeyStore.getInstance("JKS");
        InputStream inputStream = new FileInputStream(
                properties.serverKeyStoreFileName);
        keyStore.load(inputStream,
                properties.serverKeyStorePassword.toCharArray());
        KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore,
                properties.serverKeyPassword.toCharArray());
        keyManagers = keyManagerFactory.getKeyManagers();
    }
```

where in this case the `inputStream` for the keystore is a `FileInputStream` for the specified `serverprivate` file.

We initialise `KeyManagerFactory` with the `keyStore`, and also the private key password, i.e. the `-keypass` specified with the `-genkey` command. In `EnigmaClient`, this password was `null` since the keystore contained a trusted public certificate entry i.e. no private key entry.

Similarly to `EnigmaClient`, we initialise `trustManagers` and finally `SSLContext`, as follows.

```
    protected void initTrustManagers() throws Exception {
        TrustManagerFactory trustManagerFactory = 
                TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(keyStore);        
        this.trustManagers = trustManagerFactory.getTrustManagers();
    }
    
    protected void initSSLContext() throws Exception {
        sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagers, trustManagers, secureRandom);
    }
```

where the `trustManagerFactory` is initialised with the `keyStore`.

## Key Store VM Option ##

Alternatively, we specify the private keystore location and it's password as VM options for the server.

```
-Djavax.net.ssl.keyStore=/path/serverprivate 
-Djavax.net.ssl.keyStorePassword=storepassword
```

Here, the `-storepass` and `-keypass` specified with `-genkey` below, need to be the same.

```
$ keytool -genkey -alias serverprivate -keystore serverprivate \
          -storepass storepassword -keypass storepassword \
          -dname "CN=EnigmaDemo, OU=ditto, O=ditto, L=ct, S=wp, C=za" 
```

In this case, our server can be implemented as follows, <i>sans</i> any `KeyStore`, `KeyManager` or `TrustManager` instances.

```
public class SimpleServer extends Thread {
    static Logger logger = Logger.getLogger(SimpleServer.class.getName());
    
    SSLServerSocket serverSocket;
    boolean isRunning = true;
        
    public void bind(int port) throws Exception {
        ServerSocketFactory serverSocketFactory = SSLServerSocketFactory.getDefault();
        serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
    }
    
    public void run() {
        while (isRunning) {
            try {
                new EnigmaThread(serverSocket.accept()).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }
    }
}
```

## Server Thread ##

Our server thread responds to an incoming message from the client.

```
public class EnigmaThread extends Thread {
    static QLogger logger = QLogger.getLogger(EnigmaThread.class);    
    EnigmaSocket enigmaSocket;
    
    public EnigmaThread(Socket clientSocket) {
        this.enigmaSocket = new EnigmaSocket(clientSocket);
    }
    
    public void run() {
        try {
            enigmaSocket.init();
            process();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            enigmaSocket.close();
        }
    }
    
    protected void process() throws Exception {
        String request = enigmaSocket.readObject();
        logger.info(request);
        enigmaSocket.writeObject("That's it man, game over man, game over!");
    }
}
```

where in `process()` we respond to the incoming [message](http://www.imdb.com/title/tt0090605/quotes).

## Enigma Socket ##

Both the client and server instantiate an `EnigmaSocket`, which wraps
a `Socket` instance, to support XML message encoding.

```
public class EnigmaSocket {
    static QLogger logger = QLogger.getLogger(EnigmaSocket.class);
    
    Socket socket;
    InputStream inputStream;
    OutputStream outputStream;

    public EnigmaSocket(Socket socket) {
        this.socket = socket;
    }
            
    public void init() throws IOException {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }
    
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }    
    ...    
}
```

where we reference the input and output streams for convenience.

### Send and Receive ###

We send a request, and wait for a response using the following `sendRequest()` method.

```
    public <T> T sendRequest(Object request) throws Exception {
        writeObject(request);
        T response = (T) readObject();
        return response;
    }
```

### Input and Output ###

As in [Cryptonomical](Cryptonomical.md), we write and read objects from the socket streams as follows.

```
    public void writeObject(Object object) throws Exception {
        String text = escapeXml(encodeXml(object));
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
        return (T) decodeXml(unescapeXml(text));
    }
```

where we write the object as XML text followed by a blank line, and so use `readLine()` to read in the text, until we reach the blank line.

We escape and unescape new line characters in the XML message as follows.

```
    protected String escapeXml(String text) {
        return text.replaceAll("\n", "&newline;");
    }

    protected String unescapeXml(String text) {
        return text.replaceAll("&newline;", "\n");
    }
```

which is fine unless any string in the message object contains our arbitrarily chosen `"&amp;newline;"` placeholder.

### XML Encoding ###

Objects are XML encoded into text as follows.

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


## Conclusion ##

We build a trivial client/server demo using `SSLServerSocket` and `SSLSocket`, provided by the [Java Secure Socket Extension (JSSE)](http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/JSSERefGuide.html).

We use [keytool](http://java.sun.com/j2se/1.5.0/docs/tooldocs/windows/keytool.html) to create keystores with private key entries, and public key certificate entries. The `-genkey` command is used to create a keystore with a "private key entry"
ie. a public/private key pair for our server. We export the "private key entry" as a self-signed X.509 public key certificate file using `-export`. Finally, we import the certificate into it's own keystore, as a "trusted certificate entry," to install on the client.

In the application code, the `KeyStore` is loaded from a resource or file, and a `KeyManagerFactory` and `TrustManagerFactory` are initialised with the `keyStore` instance. Finally, an `SSLContext` is initialised with key managers and trust managers, and this is used to create SSL socket connections.

Alternatively, the server's keystore location and password is specified using `-Djavax.net.ssl` VM options, and we override the client's default `cacerts` trust store with the server's trusted public key certificate keystore.


## Resources ##

You can browse the code for this exercise [here](http://code.google.com/p/vellum/source/browse/#svn/trunk/src/vellumdemo/enigmademo).

See blogs...

[Richard Bair's SSL blog entry ](http://weblogs.java.net/blog/rbair/archive/2006/10/ssl_and_self_si_1.html)

[swingx-ws org.jdesktop.http package](https://swingx-ws.dev.java.net/source/browse/swingx-ws/src/java/org/jdesktop/http)

[Andreas Sterbenz's SSL blog entry ](http://blogs.sun.com/andreas/entry/no_more_unable_to_find)
and [InstallCert.java](http://blogs.sun.com/andreas/resource/InstallCert.java)
to add a server's certificate to `jssecerts`.

See the Java Developers Almanac...

[Creating an SSL Server Socket](http://www.exampledepot.com/egs/javax.net.ssl/Server.html)

[Creating an SSL Client Socket](http://www.exampledepot.com/egs/javax.net.ssl/Client.html)

[Disabling Certificate Validation in an HTTPS Connection ](http://www.exampledepot.com/egs/javax.net.ssl/TrustAll.html?l=rel)

See references...

[keytool](http://java.sun.com/j2se/1.5.0/docs/tooldocs/windows/keytool.html)

[JSSE Reference Guide](http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/JSSERefGuide.html)

[ONJava JSSE Tutorial](http://www.onjava.com/pub/a/onjava/2001/05/03/java_security.html)

Especially see...

[IBM developerWorks JSSE Tutorial (PDF)](http://www.ibm.com/developerworks/java/tutorials/j-jsse/section2.html)