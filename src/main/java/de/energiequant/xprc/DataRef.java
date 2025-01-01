package de.energiequant.xprc;

import de.energiequant.xprc.types.ValueType;

public class DataRef<T> {
    // FIXME: draft/WIP
    
    private ValueType<T> type;
    private String name;

    public static <U> DataRef<U> of(ValueType<U> type, String name) {
        return null;
    }

    public static DataRef<?> withUnknownType(String name) {
        return null;
    }
}
