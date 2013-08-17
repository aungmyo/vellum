
package dualcontrol;

import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author evans
 */
public abstract class DualControlClient {
    private static int PORT = 4444;
    private static String HOST = "127.0.0.1";

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            write(args[0]);
        } else {
            String username = System.console().readLine("DualControl username: ");
            char[] passwd = System.console().readPassword("DualControl password: ");
            write(username + ':'+ new String(passwd));
        }
    }

    public static void write(String data) throws Exception {
        System.err.println("DualControlClient write " + data);
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket socket = sf.createSocket(HOST, PORT);
        socket.getOutputStream().write(data.getBytes());
        socket.close();
    }
    
}