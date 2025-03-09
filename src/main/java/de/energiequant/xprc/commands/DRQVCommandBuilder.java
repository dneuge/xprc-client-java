package de.energiequant.xprc.commands;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.energiequant.xprc.Command;
import de.energiequant.xprc.DataRef;
import de.energiequant.xprc.Interval;
import de.energiequant.xprc.XPRCClient;
import de.energiequant.xprc.types.ValueType;

public class DRQVCommandBuilder<SELF extends DRQVCommandBuilder<SELF, CH, CFB, C>, CH extends DRQVChannel<CH, CFB, C>, CFB extends DRQVChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRQVMessage>> extends Command.Builder<SELF, C, CH, CFB, DRQVMessage> {
    // FIXME: draft/WIP

    private final List<DataRef<?>> dataRefs = new ArrayList<>();

    // TODO: extract Phase enum (used by other commands as well)
    public enum Phase {
        BEFORE_FLIGHT_MODEL("0"),
        AFTER_FLIGHT_MODEL("1");

        private final String encoding;

        Phase(String encoding) {
            this.encoding = encoding;
        }
    }

    public DRQVCommandBuilder(XPRCClient client) {
        super(client, "DRQV");
    }

    public DRQVCommandBuilder(XPRCClient client, List<DataRef<?>> dataRefs) {
        super(client, "DRQV");
        dataRefs.forEach(this::readingDataRef);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CFB createChannelFactoryBuilder(XPRCClient client, Supplier<C> commandSupplier) {
        return (CFB) new DRQVChannel.FactoryBuilder<>(client, commandSupplier, dataRefs);
    }

    public SELF repeatEvery(Interval interval) {
        setOption("freq", interval.encode());
        return (SELF) this;
    }

    public SELF repeatEvery(Duration duration) {
        return repeatEvery(Interval.of(duration));
    }

    public SELF stopAfterIterations(int times) {
        if (times <= 0) {
            throw new IllegalArgumentException("number of times must be positive, non-zero; got: " + times);
        }

        setOption("times", Integer.toString(times));
        return (SELF) this;
    }

    public SELF duringPhase(Phase phase) {
        setOption("phase", phase.encoding);
        return (SELF) this;
    }

    public SELF readingDataRef(DataRef<?> dataRef) {
        // TODO: check for duplicates?
        // TODO: check implementation/protocol for array addressing; not documented, is it supported?

        ValueType<?> type = dataRef.getType();
        addParameter(
            type.getEncodedTypeName()
                + ':'
                + dataRef.getName() // TODO: do we need to escape anything?
        );

        dataRefs.add(dataRef.withoutArrayLength());

        return (SELF) this;
    }

    @Override
    public C build() {
        if (dataRefs.isEmpty()) {
            throw new IllegalArgumentException("no DataRefs have been specified (query nothing?)");
        }

        return super.build();
    }
}
