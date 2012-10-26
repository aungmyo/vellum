/*
 */
package vellum.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import crocserver.httpserver.HttpServerConfig;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import vellum.lifecycle.Startable;
import vellum.logr.Logr;
import vellum.logr.LogrFactory;
import vellum.util.Keystores;
import vellum.util.Sockets;

/**
 *
 * @author evans
 */
public class VellumHttpsServer implements Startable {
    Logr logger = LogrFactory.getLogger(VellumHttpsServer.class);
    SSLContext sslContext;
    HttpsServer httpsServer;
    HttpServerConfig config;     
    ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 8, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(4));
    
    public VellumHttpsServer(HttpServerConfig config) {
        this.config = config;
    }    

    public void init() throws Exception {
        sslContext = Keystores.createSSLContext();
    }
    
    public void init(SSLContext sslContext) throws Exception {
        this.sslContext = sslContext;
    }    
    
    @Override
    public void start() throws Exception {
        if (sslContext == null) {
            sslContext = Keystores.createSSLContext();
        }
        Sockets.waitPort(config.getPort(), 4000, 500);
        InetSocketAddress socketAddress = new InetSocketAddress(config.getPort());
        httpsServer = HttpsServer.create(socketAddress, 4);
        httpsServer.setHttpsConfigurator(Keystores.createHttpsConfigurator(sslContext, config.isClientAuth()));
        httpsServer.setExecutor(executor);
        httpsServer.start();
        logger.info("start", config.getPort());
    }

    public void startContext(String contextName, HttpHandler httpHandler) {
        httpsServer.createContext(contextName, httpHandler);
    }

    @Override
    public boolean stop() {
        if (httpsServer != null) {
            httpsServer.stop(0); 
            executor.shutdown();
            return true;
        }        
        return false;
    }    
}
