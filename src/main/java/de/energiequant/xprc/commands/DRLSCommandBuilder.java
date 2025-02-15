package de.energiequant.xprc.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.energiequant.xprc.Command;
import de.energiequant.xprc.DataRef;
import de.energiequant.xprc.XPRCClient;

public class DRLSCommandBuilder<SELF extends DRLSCommandBuilder<SELF, CH, CFB, C>, CH extends DRLSChannel<CH, CFB, C>, CFB extends DRLSChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRLSMessage>> extends Command.Builder<SELF, C, CH, CFB, DRLSMessage> {
    // FIXME: draft/WIP

    private static final String READWRITE_OPTION = "rwCheck";

    public enum ReadWriteCheck {
        SIMULATOR("simulator"),
        SERVER("server"),
        SESSION("session");

        private final String serialization;

        ReadWriteCheck(String serialization) {
            this.serialization = serialization;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public DRLSCommandBuilder(XPRCClient client) {
        super(client, "DRLS", (BiFunction<XPRCClient, Supplier<C>, CFB>) (BiFunction) (BiFunction<XPRCClient, Supplier<C>, DRLSChannel.FactoryBuilder>) DRLSChannel.FactoryBuilder::new);
    }

    public SELF withReadWriteCheck(ReadWriteCheck check) {
        return setOption(READWRITE_OPTION, check.serialization);
    }

    public SELF forDataRefs(DataRef<?>... dataRefs) {
        return forDataRefs(Arrays.stream(dataRefs));
    }

    public SELF forDataRefs(Collection<DataRef<?>> dataRefs) {
        return forDataRefs(dataRefs.stream());
    }

    public SELF forDataRefs(Stream<DataRef<?>> dataRefs) {
        return forDataRefsNamed(dataRefs.map(DataRef::getName));
    }

    public SELF forDataRefsNamed(String... dataRefs) {
        return forDataRefsNamed(Arrays.stream(dataRefs));
    }

    public SELF forDataRefsNamed(Collection<String> dataRefs) {
        return forDataRefsNamed(dataRefs.stream());
    }

    @SuppressWarnings("unchecked")
    public SELF forDataRefsNamed(Stream<String> dataRefs) {
        // TODO: catch empty strings
        // TODO: deduplicate
        dataRefs.forEach(this::addParameter);
        return (SELF) this;
    }
}
