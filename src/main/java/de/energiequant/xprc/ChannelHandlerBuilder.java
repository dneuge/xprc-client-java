package de.energiequant.xprc;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChannelHandlerBuilder<M extends ChannelMessage, D extends ChannelDecoder<M>> {
    // FIXME: draft/WIP
/*
    public static <T extends ChannelMessage, D extends ChannelDecoder<T>> ChannelHandlerBuilder<T, D> forCommand(
        Command.Builder<?, T, D> commandBuilder) {
        return new ChannelHandlerBuilder<T, D>();
    }*/

    public ChannelHandlerBuilder<M, D> withChannelId(String channelId) {
        return this;
    }

    public ChannelHandlerBuilder<M, D> onMessage(BiConsumer<Channel, M> a) {
        return this;
    }

    public ChannelHandlerBuilder<M, D> onClose(BiConsumer<Channel, M> a) {
        return this;
    }

    public ChannelHandlerBuilder<M, D> onTimeout(Consumer<Channel> a) {
        return this;
    }

    public ChannelHandlerBuilder<M, D> waitUntilCompleted() {
        return null;
    }

    public ChannelHandlerBuilder<M, D> waitUntilCompleted(Duration timeout) {
        return null;
    }

    public ChannelHandlerBuilder<M, D> waitUntilAcknowledged() {
        return null;
    }

    public ChannelHandlerBuilder<M, D> waitUntilAcknowledged(Duration timeout) {
        return null;
    }

    public ChannelHandler build() {
        return null;
    }
}
