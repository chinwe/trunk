package org.exapmle.jvm;

import java.util.ArrayList;

/**
 * VM options: -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError
 * -XX:StartFlightRecording=disk=true,maxsize=5000m,maxage=2d,settings=./allocation.jfc,filename=./recording.jfr -XX:FlightRecorderOptions=maxchunksize=128m,repository=./,stackdepth=256
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
