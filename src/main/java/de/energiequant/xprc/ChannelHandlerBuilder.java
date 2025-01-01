package de.energiequant.xprc;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChannelHandlerBuilder<T extends ChannelMessage, D extends ChannelDecoder<T>> {
    // FIXME: draft/WIP

    public static <T extends ChannelMessage, D extends ChannelDecoder<T>> ChannelHandlerBuilder<T, D> forCommand(
        Command.Builder<?, T, D> commandBuilder) {
        return new ChannelHandlerBuilder<T, D>();
    }

    public ChannelHandlerBuilder<T, D> withChannelId(String channelId) {
        return this;
    }

    public ChannelHandlerBuilder<T, D> onMessage(BiConsumer<Channel, T> a) {
        return this;
    }

    public ChannelHandlerBuilder<T, D> onClose(BiConsumer<Channel, T> a) {
        return this;
    }

    public ChannelHandlerBuilder<T, D> onTimeout(Consumer<Channel> a) {
        return this;
    }

    public ChannelHandlerBuilder<T, D> waitUntilCompleted() {
        return null;
    }

    public ChannelHandlerBuilder<T, D> waitUntilCompleted(Duration timeout) {
        return null;
    }

    public ChannelHandlerBuilder<T, D> waitUntilAcknowledged() {
        return null;
    }

    public ChannelHandlerBuilder<T, D> waitUntilAcknowledged(Duration timeout) {
        return null;
    }

    public ChannelHandler build() {
        return null;
    }
}
