
package dualcontrol;

import java.util.Arrays;
import javax.crypto.SecretKey;
import org.apache.log4j.Logger;

/**
 *
 * @author evans
 */
public class AppDemo {
    private static final Logger logger = Logger.getLogger(AppDemo.class);
    private SecretKey dek; 
    
    public static void main(String[] args) throws Exception {
        logger.info("main invoked with args: " + Arrays.toString(args));
        if (args.length != 3) {
            System.err.println("usage: keyStorePath storePass alias");
        } else {
            new AppDemo().run(args[0], args[1].toCharArray(), args[2]);
        }
    }    
    
    private void run(String keyStorePath, char[] storePass, String alias) throws Exception {
        DualControl.init();
        dek = DualControl.loadKey(keyStorePath, storePass, alias);
        logger.info("loaded key " + dek.getAlgorithm());
    }
}