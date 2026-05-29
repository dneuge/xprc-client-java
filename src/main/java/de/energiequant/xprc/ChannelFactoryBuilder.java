package de.energiequant.xprc;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.energiequant.xprc.Channel.StateChangeCallback;

/**
 * Builds factories to create {@link Channel}s with configured callbacks/behaviour.
 * <p>
 * The weird name of this class actually describes what it is doing gives a hint on why it is needed this way:
 * Channels are created on dispatch. All handlers/behaviours must have been already defined at that point (and should
 * no longer change) as they could be called at any time. Attaching handlers after channel creation means that some
 * events/messages would already be lost or would need to be cached which would confuse causality. So what we want is
 * something that has all behaviour already configured and can be requested to create a channel - a "channel factory".
 * This comes in handy when we should have something like auto-reconnect/retry mechanisms as well, as the factory can be
 * called by the XPRC client as needed to create new channel instances. While we could create factories that get
 * modified after creation it is probably a better idea to have a builder creating immutable factory instances - which
 * is how we end up with a "factory builder".
 * </p>
 *
 * @param <SELF> the specific {@link ChannelFactoryBuilder} implementation of a {@link Command}
 * @param <CH>   the specific {@link Channel} implementation for a {@link Command}
 * @param <C>    the specific {@link Command}
 * @param <M>    the specific {@link ChannelMessage} of a {@link Command}
 */
public abstract class ChannelFactoryBuilder<SELF extends ChannelFactoryBuilder<SELF, CH, C, M>, CH extends Channel<CH, C, M>, C extends Command<SELF, CH, C, M>, M extends ChannelMessage> {
    private final XPRCClient client;
    private final Supplier<C> commandFactory;
    private final Channel.Callbacks.Builder<CH, C, M> callbackBuilder = Channel.Callbacks.builder();

    protected ChannelFactoryBuilder(XPRCClient client, Supplier<C> commandFactory) {
        this.client = client;
        this.commandFactory = commandFactory;
    }

    @SuppressWarnings("unchecked")
    public SELF onStateChanging(StateChangeCallback<CH, C, M> callback) {
        callbackBuilder.onStateChanging(callback);
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF onStateChanged(StateChangeCallback<CH, C, M> callback) {
        callbackBuilder.onStateChanged(callback);
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF onDataMessage(BiConsumer<CH, M> callback) {
        callbackBuilder.onDataMessage(callback);
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF onErrorMessage(BiConsumer<CH, M> callback) {
        callbackBuilder.onErrorMessage(callback);
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF onBlankMessage(BiConsumer<CH, M> callback) {
        callbackBuilder.onBlankMessage(callback);
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF onDispatch(Consumer<CH> callback) {
        callbackBuilder.onDispatch(callback);
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF onConfirmation(Consumer<CH> callback) {
        callbackBuilder.onConfirmation(callback);
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF onTermination(Consumer<CH> callback) {
        callbackBuilder.onTermination(callback);
        return (SELF) this;
    }

    public Optional<CH> submit() {
        return client.submitCommand(this);
    }

    public ChannelFactory<CH, C, M> build() {
        return buildSpecificFactory(commandFactory, callbackBuilder.build());
    }

    protected abstract ChannelFactory<CH, C, M> buildSpecificFactory(Supplier<C> commandFactory, Channel.Callbacks<CH, C, M> externalCallbacks);
}

