package de.energiequant.xprc;

import java.util.function.Supplier;

public abstract class ChannelFactory<CH extends Channel<CH, C, M>, C extends Command<?, CH, C, M>, M extends ChannelMessage> {
    private final Supplier<C> commandFactory;

    protected ChannelFactory(Supplier<C> commandFactory) {
        this.commandFactory = commandFactory;
    }

    public C getCommand() {
        return commandFactory.get();
    }

    protected abstract CH createChannel(ChannelId channelId, Session session, C command);
}
