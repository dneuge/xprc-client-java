package de.energiequant.xprc;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class DRLSBuilder extends Command.Builder<DRLSBuilder, DRLSMessage, DRLSDecoder> {
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

    public DRLSBuilder() {
        super("DRLS", DRLSDecoder::new);
    }

    public DRLSBuilder withReadWriteCheck(ReadWriteCheck check) {
        return setOption(READWRITE_OPTION, check.serialization);
    }

    public DRLSBuilder forDataRefs(DataRef<?>... dataRefs) {
        return forDataRefs(Arrays.stream(dataRefs));
    }

    public DRLSBuilder forDataRefs(Collection<DataRef<?>> dataRefs) {
        return forDataRefs(dataRefs.stream());
    }

    public DRLSBuilder forDataRefs(Stream<DataRef<?>> dataRefs) {
        return forDataRefsNamed(dataRefs.map(DataRef::getName));
    }

    public DRLSBuilder forDataRefsNamed(String... dataRefs) {
        return forDataRefsNamed(Arrays.stream(dataRefs));
    }

    public DRLSBuilder forDataRefsNamed(Collection<String> dataRefs) {
        return forDataRefsNamed(dataRefs.stream());
    }

    public DRLSBuilder forDataRefsNamed(Stream<String> dataRefs) {
        // TODO: catch empty strings
        // TODO: deduplicate
        dataRefs.forEach(this::addParameter);
        return this;
    }
}
