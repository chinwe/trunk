package org.exapmle.jvm;

import java.util.ArrayList;

/**
 * VM Args: -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError
 *
 * @author mozixun
 * @description
 * @date 2022/5/3 - 12:11
 */
public class HeapOOM {

    private static class OOMObject {
    }

    public static void main(String[] args) {
        ArrayList<OOMObject> oomObjects = new ArrayList<>();

        while (true) {
            oomObjects.add(new OOMObject());
        }
    }
}
