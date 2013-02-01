/*
 * Apache Software License 2.0, (c) Copyright 2012 Evan Summers
 */
package keystoremanager.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import keystoremanager.httphandler.AdminHandler;
import keystoremanager.httphandler.ShutdownHandler;
import saltserver.app.VaultStorage;
import vellum.logr.Logr;
import vellum.logr.LogrFactory;

/**
 *
 * @author evans
 */
public class MantraHttpHandler implements HttpHandler {
    Logr logger = LogrFactory.getLogger(MantraHttpHandler.class);
    MantraApp app;
    VaultStorage storage;
    
    public MantraHttpHandler(MantraApp app) {
        this.app = app;
        this.storage = app.getStorage();
    }
    
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HttpHandler handler = getHandler(httpExchange);
        if (handler != null) {
            handler.handle(httpExchange);
        } else {
            
        }
    }
    
    public HttpHandler getHandler(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        logger.info("path", path);
        if (httpExchange.getRemoteAddress().getHostName().equals("127.0.0.1"))  {
            if (path.equals("/shutdown")) {
                return new ShutdownHandler(app);
            } else if (path.startsWith("/local/")) {
                return null;
            }
        }
        if (path.equals("/admin")) {
            return new AdminHandler(app);
        }
        return null;
    }
}
