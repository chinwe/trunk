package org.prototype.deepclone;

import java.io.Serializable;

public class DeepProtoType implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    public DeepCloneableTarget deepCloneableTarget;

	@Override
    protected Object clone() throws CloneNotSupportedException {
		DeepProtoType deepProtoType = (DeepProtoType)super.clone();
		
		deepProtoType.deepCloneableTarget = (DeepCloneableTarget)deepProtoType.deepCloneableTarget.clone();
		return deepProtoType;
    }

}
