package de.energiequant.xprc.commands;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import de.energiequant.xprc.Command;
import de.energiequant.xprc.Interval;
import de.energiequant.xprc.XPRCClient;

public class CMTRCommandBuilder<SELF extends CMTRCommandBuilder<SELF, CH, CFB, C>, CH extends CMTRChannel<CH, CFB, C>, CFB extends CMTRChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMTRMessage>> extends Command.Builder<SELF, C, CH, CFB, CMTRMessage> {
    // FIXME: draft/WIP

    public enum MonitorMode {
        HOLD_RELEASE("holdRelease"),
        CYCLE("cycle"),
        NONE("none");

        private final String encoding;

        MonitorMode(String encoding) {
            this.encoding = encoding;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CMTRCommandBuilder(XPRCClient client) {
        super(client, "CMTR", (BiFunction<XPRCClient, Supplier<C>, CFB>) (BiFunction) (BiFunction<XPRCClient, Supplier<C>, CMTRChannel.FactoryBuilder>) CMTRChannel.FactoryBuilder::new);
    }

    public CMTRCommandBuilder(XPRCClient client, Collection<String> commandName) {
        this(client);
        triggeringCommands(commandName);
    }

    public SELF repeatForever() {
        return setOption("times", "infinite");
    }

    public SELF repeatCycles(int times) {
        if (times < 1) {
            throw new IllegalArgumentException("invalid number of repetitions: " + times);
        }

        return setOption("times", Integer.toString(times));
    }

    public SELF repeatEvery(Interval interval) {
        return setOption("repeatFreq", interval.encode());
    }

    public SELF repeatEvery(Duration duration) {
        return repeatEvery(Interval.of(duration));
    }

    public SELF releasingImmediately() {
        return setOption("hold", "0");
    }

    public SELF holdingFor(Interval interval) {
        if (interval.isSpecialMode()) {
            throw new IllegalArgumentException("special interval modes cannot be used as CMTR hold time");
        }

        return setOption("hold", interval.encode());
    }

    public SELF holdingFor(Duration duration) {
        return holdingFor(Interval.of(duration));
    }

    public SELF monitoring(MonitorMode mode) {
        return setOption("monitor", mode.encoding);
    }

    public SELF triggeringCommand(String name) {
        return triggeringCommands(Collections.singleton(name));
    }

    public SELF triggeringCommands(String... names) {
        return triggeringCommands(Arrays.asList(names));
    }

    public SELF triggeringCommands(Collection<String> names) {
        return addParameters(names);
    }

    @Override
    public C build() {
        if (countParameters() == 0) {
            throw new IllegalArgumentException("at least one command name must be specified");
        }

        return super.build();
    }
}
