package de.energiequant.xprc;

import java.util.Optional;
import java.util.function.BiConsumer;

public class ChannelFactoryBuilder<SELF extends ChannelFactoryBuilder<SELF, CH, C, M>, CH extends Channel<CH, C, M>, C extends Command<SELF, CH, C, M>, M extends ChannelMessage> {
    private final XPRCClient client;
    private final C command;

    protected ChannelFactoryBuilder(XPRCClient client, C command) {
        this.client = client;
        this.command = command;
    }

    @SuppressWarnings("unchecked")
    public SELF onDataMessage(BiConsumer<CH, M> msg) {
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public Optional<CH> submit() {
        return client.submitCommand(this);
    }

    public CH submit(ChannelId channelId) {
        return null;
    }
}
