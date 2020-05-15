package org.prototype.deepclone;

import java.io.Serializable;

public class DeepCloneableTarget implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private String name;

    public DeepCloneableTarget(String name) {
    	super();
    	this.name = name;
    }

    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new DeepCloneableTarget(this.name);
    }
}
