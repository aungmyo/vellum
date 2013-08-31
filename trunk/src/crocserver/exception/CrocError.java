/*
 * Vellum by Evan Summers under Apache Software License 2.0 from ASF.
 * 
 */
package crocserver.exception;

import vellum.exception.DisplayMessage;
import vellum.exception.EnumExceptions;

/**
 *
 * @author evan.summers
 */
public class CrocError implements DisplayMessage {
    CrocExceptionType exceptionType;
    Object[] args;
    String displayMessage;
    
    public CrocError(CrocExceptionType exceptionType) {
        this.exceptionType = exceptionType;
        this.displayMessage = EnumExceptions.formatMessage(exceptionType);
    }
    
    public CrocError(CrocExceptionType exceptionType, Object ... args) {
        this.exceptionType = exceptionType;
        this.args = args;
        this.displayMessage = EnumExceptions.formatMessage(exceptionType, args);
    }   

    public CrocExceptionType getExceptionType() {
        return exceptionType;
    }

    public Object[] getArgs() {
        return args;
    }
    
    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }
}
