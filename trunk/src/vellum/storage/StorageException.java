/*
 * Apache Software License 2.0, (c) Copyright 2012, Evan Summers
 * 
 */
package vellum.storage;

import java.sql.SQLException;
import vellum.util.Args;
import vellum.util.Lists;

/**
 *
 * @author evan
 */
public class StorageException extends SQLException {
    StorageExceptionType exceptionType;
    
    public StorageException(StorageExceptionType exceptionType) {
        super(Args.format(exceptionType));
        this.exceptionType = exceptionType;
    }
    
    public StorageException(StorageExceptionType exceptionType, Object ... args) {
        super(Args.format(exceptionType, Lists.format(args)));
        this.exceptionType = exceptionType;
    }

    public StorageExceptionType getExceptionType() {
        return exceptionType;
    }

}