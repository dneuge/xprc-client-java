package de.energiequant.xprc.utils;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper methods for {@link Map}s.
 */
public class Maps {
    private Maps() {
        // utility class; hide constructor
    }

    /**
     * Creates a new unmodifiable {@link HashMap} holding the given entries.
     *
     * @param entries map entries
     * @param <K>     key type
     * @param <V>     value type
     * @return unmodifiable {@link HashMap} holding the given entries
     * @see #entry(Object, Object)
     */
    @SafeVarargs
    public static <K, V> Map<K, V> createUnmodifiableHashMap(Map.Entry<K, V>... entries) {
        return Collections.unmodifiableMap(createHashMap(entries));
    }

    /**
     * Creates a new unmodifiable {@link LinkedHashMap} holding the given entries, maintaining the same order.
     *
     * @param entries map entries
     * @param <K>     key type
     * @param <V>     value type
     * @return unmodifiable {@link LinkedHashMap} holding the given entries in order
     * @see #entry(Object, Object)
     */
    @SafeVarargs
    public static <K, V> Map<K, V> createUnmodifiableLinkedHashMap(Map.Entry<K, V>... entries) {
        return Collections.unmodifiableMap(createLinkedHashMap(entries));
    }

    /**
     * Creates a new unmodifiable {@link EnumMap} holding the given entries.
     *
     * @param clazz   enum type to be used as key
     * @param entries map entries
     * @param <K>     key type
     * @param <V>     value type
     * @return unmodifiable {@link EnumMap} holding the given entries
     * @see #entry(Object, Object)
     */
    @SafeVarargs
    public static <K extends Enum<K>, V> Map<K, V> createUnmodifiableEnumMap(Class<K> clazz, Map.Entry<K, V>... entries) {
        return Collections.unmodifiableMap(createEnumMap(clazz, entries));
    }

    /**
     * Creates a new {@link HashMap} initially holding the given entries.
     *
     * @param entries initial map entries
     * @param <K>     key type
     * @param <V>     value type
     * @return {@link HashMap} holding the given entries
     * @see #entry(Object, Object)
     */
    @SafeVarargs
    public static <K, V> HashMap<K, V> createHashMap(Map.Entry<K, V>... entries) {
        return putAll(new HashMap<>(), entries);
    }

    /**
     * Creates a new {@link LinkedHashMap} initially holding the given entries, maintaining the same order.
     *
     * @param entries initial map entries
     * @param <K>     key type
     * @param <V>     value type
     * @return {@link LinkedHashMap} holding the given entries in order
     * @see #entry(Object, Object)
     */
    @SafeVarargs
    public static <K, V> LinkedHashMap<K, V> createLinkedHashMap(Map.Entry<K, V>... entries) {
        return putAll(new LinkedHashMap<>(), entries);
    }

    /**
     * Creates a new {@link EnumMap} initially holding the given entries.
     *
     * @param clazz   enum type to be used as key
     * @param entries initial map entries
     * @param <K>     key type
     * @param <V>     value type
     * @return {@link EnumMap} holding the given entries
     * @see #entry(Object, Object)
     */
    @SafeVarargs
    public static <K extends Enum<K>, V> EnumMap<K, V> createEnumMap(Class<K> clazz, Map.Entry<K, V>... entries) {
        return putAll(new EnumMap<>(clazz), entries);
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

    /**
     * Creates a {@link Map.Entry} to be used in other helper methods provided by {@link Maps}.
     *
     * @param key   entry key
     * @param value entry value
     * @param <K>   key type
     * @param <V>   value type
     * @return {@link Map.Entry} to be used in other helper methods provided by {@link Maps}
     */
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
