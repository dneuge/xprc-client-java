package de.energiequant.xprc;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class DRLSChannel<SELF extends DRLSChannel<SELF, CFB, C>, CFB extends DRLSChannel.FactoryBuilder<CFB, SELF, C>, C extends Command<CFB, SELF, C, DRLSMessage>> extends Channel<SELF, C, DRLSMessage> {
    public DRLSChannel(ChannelId id, Session session, C command) {
        super(id, session, command);
    }

    public List<DataRef<?>> getReceivedDataRefs() {
        // FIXME: implement
        return Collections.emptyList();
    }

    public static class FactoryBuilder<SELF extends FactoryBuilder<SELF, CH, C>, CH extends DRLSChannel<CH, SELF, C>, C extends Command<SELF, CH, C, DRLSMessage>> extends ChannelFactoryBuilder<SELF, CH, C, DRLSMessage> {
        public FactoryBuilder(XPRCClient client, C command) {
            super(client, command);
        }

        @SuppressWarnings("unchecked")
        public SELF onDataRef(Consumer<DataRef<?>> dataRef) {
            return (SELF) this;
        }
    }
}
