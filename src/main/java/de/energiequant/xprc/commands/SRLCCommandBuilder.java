package de.energiequant.xprc.commands;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import de.energiequant.xprc.Command;
import de.energiequant.xprc.XPRCClient;

public class SRLCCommandBuilder<SELF extends SRLCCommandBuilder<SELF, CH, CFB, C>, CH extends SRLCChannel<CH, CFB, C>, CFB extends SRLCChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, SRLCMessage>> extends Command.Builder<SELF, C, CH, CFB, SRLCMessage> {
    // SRLC does not accept any options or parameters at this time; CommandBuilder is still left open for extension in case of future changes

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SRLCCommandBuilder(XPRCClient client) {
        super(client, "SRLC", (BiFunction<XPRCClient, Supplier<C>, CFB>) (BiFunction) (BiFunction<XPRCClient, Supplier<C>, SRLCChannel.FactoryBuilder>) SRLCChannel.FactoryBuilder::new);
    }

    public CompletableFuture<SRLCAggregator.SRLCResult> submitAndAggregate() {
        return SRLCAggregator.submitCommand(this);
    }
}
