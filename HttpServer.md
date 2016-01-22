# JRE http server #

```
public class Server {

    Logr logger = LogrFactory.getLogger(getClass());
    int serverPort = Integer.getInteger("serverPort", 8099);
    int shutdownPort = Integer.getInteger("shutdownPort", 8098);
    HttpServer server;

    protected void start() throws Exception {
        requestShutdown();
        server = HttpServer.create(new InetSocketAddress(serverPort), 4);
        server.createContext("/echo", new EchoHandler());
        server.createContext("/", new GenericPageHandler(HomePageHandler.class));
        createContext();
        server.setExecutor(new ThreadPoolExecutor(4, 8, 0, TimeUnit.MILLISECONDS, 
            new ArrayBlockingQueue<Runnable>(4)));
        server.start();
        new ServerSocket(shutdownPort).accept();
        logger.info("bizmon restarted");
        System.exit(0);
    }

    protected void requestShutdown() {
        try {
            URL url = new URL("http://localhost:" + shutdownPort);
            URLConnection connection = url.openConnection();
            connection.connect();
            connection.getContentLength();
            logger.info("restarting server");
        } catch (Exception e) {
            logger.info("starting server");
        }
    }

    protected void createContext(String context, Class type) {
        logger.info(context, type);
        server.createContext(context, new GenericPageHandler(type));
    }

    protected void createContext() {
        for (PageHandlerInfo info : PageHandlerInfoManager.getInstance().getHandlerInfoList()) {
            createContext("/" + info.getName(), info.getType());
        }
    }

    public static void main(String[] args) throws Exception {
        new Server().start();
    }
}
```


# JRE https secure server #


while i got the `com.sun.net.httpserver.HttpsServer` builtin JRE https server to work,
we were suprised that `-Djavax.net.ssl.keyStore` options didn't seem to take,
and so we had to instantiate and load keystore manually
(couldn't avoid having the `init()` method to manually init SSL context key and trust managers even
with` -Djavax.ssl.net` options i tried)

We tested with client auth - where generate private key and public cert using openssl,
and package into browser-friendly pcks12 format like `certificate.pfx`,
and import that into web-browser as client-side cert,
and import the client cert into the server keystore as trusted cert (truststore) using keytool.

At that time, it worked with Firefox but not with Chrome.

```
import com.sun.net.httpserver.HttpsServer;

public class SecureServer {

    SSLContext sslContext;
    
    public SecureServer() {
    }
    
    protected void init() throws Exception {
        sslContext = SSLContext.getInstance("TLS");
        char[] password = System.getProperty("javax.net.ssl.keyStorePassword").toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream(System.getProperty("javax.net.ssl.keyStore"));
        ks.load(fis, password);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());        
    }
    
    protected void start() throws Exception {
        init();
        HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext) {

            @Override
            public void configure(HttpsParameters httpsParameters) {
                SSLContext sslContext = getSSLContext();
                InetSocketAddress remote = httpsParameters.getClientAddress();
                if (remote.getHostName().equals("localhost")) {
                }
                SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
                defaultSSLParameters.setNeedClientAuth(true);
                httpsParameters.setSSLParameters(defaultSSLParameters);
            }
        };
        HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(443), 8);
        httpsServer.setHttpsConfigurator(httpsConfigurator);
        httpsServer.setExecutor(new ThreadPoolExecutor(4, 8, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(4)));
        httpsServer.createContext("/", new EchoHandler());
        httpsServer.start();
    }

    public static final void main(String[] args) {
        try {
            new SecureServer().start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
```


For client-side auth disabled,
```
 defaultSSLParameters.setNeedClientAuth(false);
```

and we generate client private key (for browser) and public cert (for server truststore) as follows
```
 openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout privateKey.key -out certificate.crt
 openssl pkcs12 -export -out certificate.pfx -inkey privateKey.key -in certificate.crt -certfilecertificate.crt
```

http://emo.sourceforge.net/cert-login-howto.html

http://download.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html