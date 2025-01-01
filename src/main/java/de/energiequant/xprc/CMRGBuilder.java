package de.energiequant.xprc;

public class CMRGBuilder extends Command.Builder<CMRGBuilder, CMRGMessage, CMRGDecoder> {
    // FIXME: draft/WIP
    
    public enum MonitorMode {
        ALL, TRIGGER, HOLD_RELEASE;
    }

    public enum Phase {
        BEFORE_XPLANE_HANDLER, AFTER_XPLANE_HANDLER;
    }

    public CMRGBuilder() {
        super("CMRG", CMRGDecoder::new);
    }

    public CMRGBuilder monitoring(MonitorMode mode) {
        return this;
    }

    public CMRGBuilder propagatingEvents(boolean propagate) {
        return this;
    }

    public CMRGBuilder duringPhase(Phase phase) {
        return this;
    }

    public CMRGBuilder withCommandName(String name) {
        return this;
    }

    public CMRGBuilder withDescription(String description) {
        return this;
    }
}
