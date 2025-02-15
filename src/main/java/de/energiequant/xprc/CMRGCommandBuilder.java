package de.energiequant.xprc;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CMRGCommandBuilder<SELF extends CMRGCommandBuilder<SELF, CH, CFB, C>, CH extends CMRGChannel<CH, CFB, C>, CFB extends CMRGChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMRGMessage>> extends Command.Builder<SELF, C, CH, CFB, CMRGMessage> {
    // FIXME: draft/WIP

    private String commandName;
    private String description;

    private static final String MONITOR_OPTION = "monitor";
    private static final String PROPAGATE_OPTION = "propagate";
    private static final String PHASE_OPTION = "phase";

    public enum MonitorMode {
        ALL("all"),
        TRIGGER("trigger"),
        HOLD_RELEASE("holdRelease");

        private final String optionValue;

        MonitorMode(String optionValue) {
            this.optionValue = optionValue;
        }
    }

    public enum Phase {
        BEFORE_XPLANE_HANDLER("before"),
        AFTER_XPLANE_HANDLER("after");

        private final String optionValue;

        Phase(String optionValue) {
            this.optionValue = optionValue;
        }
    }

    public CMRGCommandBuilder(XPRCClient client) {
        super(client, "CMRG", (BiFunction<XPRCClient, Supplier<C>, CFB>) (BiFunction) (BiFunction<XPRCClient, Supplier<C>, CMRGChannel.FactoryBuilder>) CMRGChannel.FactoryBuilder::new);
    }

    public SELF monitoring(MonitorMode mode) {
        return setOption(MONITOR_OPTION, mode.optionValue);
    }

    public SELF propagatingEvents(boolean propagate) {
        return setOption(PROPAGATE_OPTION, Boolean.toString(propagate));
    }

    public SELF duringPhase(Phase phase) {
        return setOption(PHASE_OPTION, phase.optionValue);
    }

    @SuppressWarnings("unchecked")
    public SELF withCommandName(String name) {
        this.commandName = name;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF withDescription(String description) {
        this.description = description;
        return (SELF) this;
    }

    @Override
    public C build() {
        if (commandName == null) {
            throw new IllegalArgumentException("command name has not been specified");
        }

        // TODO: if possible assign already during setter calls to throw encoding exception to actual source

        setParameter(0, commandName);

        if (description != null) {
            setParameter(1, description);
        } else if (countParameters() > 1) {
            removeParameter(1);
        }

        return super.build();
    }
}
