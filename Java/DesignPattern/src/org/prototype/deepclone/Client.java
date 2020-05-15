package org.prototype.deepclone;

public class Client {
    public static void main(String[] args) throws CloneNotSupportedException {
        
    	DeepProtoType deepProtoType = new DeepProtoType();
    	deepProtoType.deepCloneableTarget = new DeepCloneableTarget("test");
    	
    	DeepProtoType deepProtoType2 = (DeepProtoType)deepProtoType.clone();

    	System.out.println(deepProtoType.deepCloneableTarget.hashCode());
    	System.out.println(deepProtoType2.deepCloneableTarget.hashCode());

    }
}
