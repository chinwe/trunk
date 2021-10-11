package buildingblocks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * ConcurrentHashMap
 * CopyOnWriteArrayList
 *
 * @author chinwe
 * 2021/10/10
 */
public class Container {
    public static void main(String[] args) {
        final ConcurrentHashMap<Object, Object> concurrentHashMap = new ConcurrentHashMap<>();

        final CopyOnWriteArrayList<Object> copyOnWriteArrayList = new CopyOnWriteArrayList<>();

    }
}
