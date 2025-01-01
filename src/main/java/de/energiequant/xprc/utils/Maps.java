package de.energiequant.xprc.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Maps {
    private Maps() {
        // utility class; hide constructor
    }

    @SafeVarargs
    public static <K, V> Map<K, V> createUnmodifiableHashMap(Map.Entry<K, V>... entries) {
        return Collections.unmodifiableMap(createHashMap(entries));
    }

    @SafeVarargs
    public static <K, V> Map<K, V> createUnmodifiableLinkedHashMap(Map.Entry<K, V>... entries) {
        return Collections.unmodifiableMap(createLinkedHashMap(entries));
    }

    @SafeVarargs
    public static <K, V> HashMap<K, V> createHashMap(Map.Entry<K, V>... entries) {
        return putAll(new HashMap<>(), entries);
    }

    @SafeVarargs
    public static <K, V> LinkedHashMap<K, V> createLinkedHashMap(Map.Entry<K, V>... entries) {
        return putAll(new LinkedHashMap<>(), entries);
    }

    private static <K, V, M extends Map<K, V>> M putAll(M dest, Map.Entry<K, V>... entries) {
        for (Map.Entry<K, V> entry : entries) {
            K key = entry.getKey();
            if (dest.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate key: " + key);
            }

            dest.put(key, entry.getValue());
        }

        return dest;
    }

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new Map.Entry<K, V>() {
            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public V setValue(V value) {
                throw new UnsupportedOperationException("Maps.entry is a helper method for constructing statically defined maps, setter calls are not supported.");
            }
        };
    }
}
