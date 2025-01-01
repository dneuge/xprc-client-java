package de.energiequant.xprc;

public class DRLSBuilder extends Command.Builder<DRLSBuilder, DRLSMessage, DRLSDecoder> {
    // FIXME: draft/WIP
    
    public enum ReadWriteCheck {
        SIMULATOR, SERVER, SESSION;
    }

    public DRLSBuilder() {
        super("DRLS", DRLSDecoder::new);
    }

    public DRLSBuilder withReadWriteCheck(ReadWriteCheck check) {
        return this;
    }

    public DRLSBuilder forDataRefs(String... dataRefs) {
        return this;
    }

    public DRLSBuilder forDataRefs(DataRef... dataRefs) {
        return this;
    }
}
