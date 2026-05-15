package de.energiequant.xprc.commands;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import de.energiequant.xprc.Command;
import de.energiequant.xprc.XPRCClient;
import de.energiequant.xprc.commands.SRIDAggregator.SRIDResult;

public class SRIDCommandBuilder<SELF extends SRIDCommandBuilder<SELF, CH, CFB, C>, CH extends SRIDChannel<CH, CFB, C>, CFB extends SRIDChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, SRIDMessage>> extends Command.Builder<SELF, C, CH, CFB, SRIDMessage> {
    // SRID does not accept any options or parameters at this time; CommandBuilder is still left open for extension in case of future changes

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SRIDCommandBuilder(XPRCClient client) {
        super(client, "SRID", (BiFunction<XPRCClient, Supplier<C>, CFB>) (BiFunction) (BiFunction<XPRCClient, Supplier<C>, SRIDChannel.FactoryBuilder>) SRIDChannel.FactoryBuilder::new);
    }

    public CompletableFuture<SRIDResult> submitAndAggregate() {
        return SRIDAggregator.submitCommand(this);
    }
}
