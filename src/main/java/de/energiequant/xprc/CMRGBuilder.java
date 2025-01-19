package de.energiequant.xprc;

public class CMRGBuilder extends Command.Builder {
    // FIXME: draft/WIP

    public enum MonitorMode {
        ALL, TRIGGER, HOLD_RELEASE;
    }

    public enum Phase {
        BEFORE_XPLANE_HANDLER, AFTER_XPLANE_HANDLER;
    }

    public CMRGBuilder(XPRCClient client) {
        super(client, "CMRG", null); // FIXME: stubbed (null factory)
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
