package vellum.enigma;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import vellum.logger.Logr;
import vellum.logger.LogrFactory;

public class SimpleServer extends Thread {
    static Logr logger = LogrFactory.getLogger(SimpleServer.class.getName());
    
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
