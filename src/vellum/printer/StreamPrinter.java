/*
 * Apache Software License 2.0
 * Supported by iPay (Pty) Ltd, BizSwitch.net
 * Vellum by Evan Summers under Apache Software License 2.0 from ASF.
 */

package vellum.printer;

import java.io.PrintStream;

/**
 *
 * @author evan.summers
 */
public interface StreamPrinter extends Printer {

    public PrintStream getPrintStream();

}