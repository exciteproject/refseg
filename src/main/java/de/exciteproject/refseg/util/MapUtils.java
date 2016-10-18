package de.exciteproject.refseg.util;

import java.util.Map;

public class MapUtils {

    public static <K> void addCount(Map<K, Integer> map, K key) {
        MapUtils.addCount(map, key, 1);
    }

    public static <K> void addCount(Map<K, Integer> map, K key, Integer count) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + count);
        } else {
            map.put(key, count);
        }
    }
}
