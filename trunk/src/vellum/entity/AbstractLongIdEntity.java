/*
 * Apache Software License 2.0, (c) Copyright 2012, Evan Summers
 * 
 */
package vellum.entity;

/**
 *
 * @author evan
 */
public abstract class AbstractLongIdEntity extends AbstractIdEntity {
    protected Long id;
    
    @Override
    public Comparable getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
       
}
