package de.energiequant.xprc.commands;

import de.energiequant.xprc.CMRGBuilder;
import de.energiequant.xprc.Command;
import de.energiequant.xprc.DRLSChannel;
import de.energiequant.xprc.DRLSCommandBuilder;
import de.energiequant.xprc.DRLSMessage;
import de.energiequant.xprc.DRMUBuilder;
import de.energiequant.xprc.XPRCClient;

public class CommandBuilderFactory {
    private final XPRCClient client;

    public CommandBuilderFactory(XPRCClient client) {
        this.client = client;
    }

    public static class DataRefCommandBuilderFactory {
        private final CommandBuilderFactory rootFactory;

        private DataRefCommandBuilderFactory(CommandBuilderFactory rootFactory) {
            this.rootFactory = rootFactory;
        }

        public <CB extends DRLSCommandBuilder<CB, CH, CFB, C>, CH extends DRLSChannel<CH, CFB, C>, CFB extends DRLSChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRLSMessage>> CB list() {
            return rootFactory.drls();
        }

        public DRMUBuilder manipulateUncontrolled() {
            return rootFactory.drmu();
        }
    }

    public static class CommandCommandBuilderFactory {
        // TODO: come up with a better name? unfortunately this is about XPRC commands for XP commands

        private final CommandBuilderFactory rootFactory;

        private CommandCommandBuilderFactory(CommandBuilderFactory rootFactory) {
            this.rootFactory = rootFactory;
        }

        public CMRGBuilder register() {
            return rootFactory.cmrg();
        }
    }

    public DataRefCommandBuilderFactory dataRefs() {
        return new DataRefCommandBuilderFactory(this);
    }

    public CommandCommandBuilderFactory commands() {
        return new CommandCommandBuilderFactory(this);
    }

    public CMRGBuilder cmrg() {
        return new CMRGBuilder(client);
    }

    @SuppressWarnings("unchecked")
    public <CB extends DRLSCommandBuilder<CB, CH, CFB, C>, CH extends DRLSChannel<CH, CFB, C>, CFB extends DRLSChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRLSMessage>> CB drls() {
        return (CB) new DRLSCommandBuilder<CB, CH, CFB, C>(client);
    }

    public DRMUBuilder drmu() {
        return new DRMUBuilder(client);
    }
}
