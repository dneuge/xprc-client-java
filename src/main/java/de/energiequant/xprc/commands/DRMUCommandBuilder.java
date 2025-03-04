package de.energiequant.xprc.commands;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import de.energiequant.xprc.Command;
import de.energiequant.xprc.DataRef;
import de.energiequant.xprc.Interval;
import de.energiequant.xprc.XPRCClient;
import de.energiequant.xprc.types.ValueType;

public class DRMUCommandBuilder<SELF extends DRMUCommandBuilder<SELF, CH, CFB, C>, CH extends DRMUChannel<CH, CFB, C>, CFB extends DRMUChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRMUMessage>> extends Command.Builder<SELF, C, CH, CFB, DRMUMessage> {
    // FIXME: draft/WIP

    // TODO: support updating parameters? (change values on later calls)

    private static final String SUB_FIELD_SEPARATOR = ":";

    public enum Method {
        IMMEDIATE("immediate"), LINEAR("linear");

        private final String encoding;

        Method(String encoding) {
            this.encoding = encoding;
        }
    }

    public enum MonitorMode {
        SET("set"),
        GET("get"),
        NONE("none");

        private final String encoding;

        MonitorMode(String encoding) {
            this.encoding = encoding;
        }
    }

    public enum Phase {
        BEFORE_FLIGHT_MODEL("0"),
        AFTER_FLIGHT_MODEL("1");

        private final String encoding;

        Phase(String encoding) {
            this.encoding = encoding;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public DRMUCommandBuilder(XPRCClient client) {
        super(client, "DRMU", (BiFunction<XPRCClient, Supplier<C>, CFB>) (BiFunction) (BiFunction<XPRCClient, Supplier<C>, DRMUChannel.FactoryBuilder>) DRMUChannel.FactoryBuilder::new);
    }

    public SELF repeatEvery(Interval interval) {
        setOption("repeatFreq", interval.encode());
        return (SELF) this;
    }

    public SELF repeatEvery(Duration duration) {
        return repeatEvery(Interval.of(duration));
    }

    public SELF forDuration(Interval interval) {
        if (interval.isSpecialMode()) {
            throw new IllegalArgumentException("special interval modes cannot be used as DRMU durations");
        }

        setOption("duration", interval.encode());

        return (SELF) this;
    }

    public SELF forDuration(Duration duration) {
        return forDuration(Interval.of(duration));
    }

    public SELF withMethod(Method method) {
        setOption("method", method.encoding);
        return (SELF) this;
    }

    public SELF monitoring(MonitorMode mode) {
        setOption("monitor", mode.encoding);
        return (SELF) this;
    }

    public SELF duringPhase(Phase phase) {
        setOption("phase", phase.encoding);
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, int value) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, int start, int end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, float value) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, Object value) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, float start, float end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, double value) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, double start, double end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, int[] value) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, int[] start, int[] end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, float[] value) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, float[] start, float[] end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRef(DataRef<?> dataRef, byte[] bytes) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName())
                + SUB_FIELD_SEPARATOR
                + type.serialize(bytes)
        );
        return (SELF) this;
    }

    public SELF setDataRefAtIndex(DataRef<?> dataRef, int index, int value) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRefAtIndex(DataRef<?> dataRef, int index, int start, int end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRefAtIndex(DataRef<?> dataRef, int index, float value) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRefAtIndex(DataRef<?> dataRef, int index, float start, float end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRefAtIndex(DataRef<?> dataRef, int index, double value) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRefAtIndex(DataRef<?> dataRef, int index, double start, double end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRefAtIndex(DataRef<?> dataRef, int index, Object value) {
        ValueType<?> type = dataRef.getType();
        if (type.isArray()) {
            throw new IllegalArgumentException("Tried to set " + dataRef + " at index " + index + " to an array; you may want to use setDataRefFromIndex instead");
        }

        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(value)
        );
        return (SELF) this;
    }

    public SELF setDataRefFromIndex(DataRef<?> dataRef, int index, int[] values) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(values)
        );
        return (SELF) this;
    }

    public SELF setDataRefFromIndex(DataRef<?> dataRef, int index, int[] start, int[] end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRefFromIndex(DataRef<?> dataRef, int index, float[] values) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(values)
        );
        return (SELF) this;
    }

    public SELF setDataRefFromIndex(DataRef<?> dataRef, int index, float[] start, float[] end) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(end)
                + SUB_FIELD_SEPARATOR
                + type.serialize(start)
        );
        return (SELF) this;
    }

    public SELF setDataRefFromIndex(DataRef<?> dataRef, int index, byte[] bytes) {
        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + SUB_FIELD_SEPARATOR
                + encodeDataRefName(dataRef.getName(), index)
                + SUB_FIELD_SEPARATOR
                + type.serialize(bytes)
        );
        return (SELF) this;
    }

    private static String encodeDataRefName(String original) {
        // FIXME: implement (escape sequences)
        return original;
    }

    private static String encodeDataRefName(String original, int index) {
        // FIXME: implement (escape sequences)
        return original + "[" + index + "]";
    }
}
