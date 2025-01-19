package de.energiequant.xprc;

import java.time.Duration;

public class DRMUBuilder extends Command.Builder {
    // FIXME: draft/WIP

    public enum Method {
        IMMEDIATE, LINEAR;
    }

    public enum MonitorMode {
        SET, GET, NONE;
    }

    public enum Phase {
        BEFORE_FLIGHT_MODEL, AFTER_FLIGHT_MODEL;
    }

    public DRMUBuilder(XPRCClient client) {
        super(client, "DRMU", null); // FIXME: stubbed (null factory)
    }

    public DRMUBuilder repeatEvery(Interval interval) {
        return this;
    }

    public DRMUBuilder forDuration(Interval interval) {
        return this;
    }

    public DRMUBuilder forDuration(Duration duration) {
        return this;
    }

    public DRMUBuilder withMethod(Method method) {
        return this;
    }

    public DRMUBuilder monitoring(MonitorMode mode) {
        return this;
    }

    public DRMUBuilder duringPhase(Phase phase) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, int value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, float value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, double value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, int[] value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, float[] value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, byte[] value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, int index, int value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, int index, float value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, int index, double value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, int index, int[] value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, int index, float[] value) {
        return this;
    }

    public DRMUBuilder setDataRef(DataRef dataRef, int index, byte[] value) {
        return this;
    }
}
