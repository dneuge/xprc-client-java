package de.energiequant.xprc.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import de.energiequant.xprc.Command;
import de.energiequant.xprc.XPRCClient;

public class CMHDCommandBuilder<SELF extends CMHDCommandBuilder<SELF, CH, CFB, C>, CH extends CMHDChannel<CH, CFB, C>, CFB extends CMHDChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMHDMessage>> extends Command.Builder<SELF, C, CH, CFB, CMHDMessage> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CMHDCommandBuilder(XPRCClient client) {
        super(client, "CMHD", (BiFunction<XPRCClient, Supplier<C>, CFB>) (BiFunction) (BiFunction<XPRCClient, Supplier<C>, CMHDChannel.FactoryBuilder>) CMHDChannel.FactoryBuilder::new);
    }

    public CMHDCommandBuilder(XPRCClient client, Collection<String> commandName) {
        this(client);
        holdingCommands(commandName);
    }

    public SELF holdingCommand(String name) {
        return holdingCommands(Collections.singleton(name));
    }

    public SELF holdingCommands(String... names) {
        return holdingCommands(Arrays.asList(names));
    }

    public SELF holdingCommands(Collection<String> names) {
        return addParameters(names);
    }

    @Override
    public C build() {
        if (countParameters() == 0) {
            throw new IllegalArgumentException("at least one command name must be specified");
        }

        return super.build();
    }
}
