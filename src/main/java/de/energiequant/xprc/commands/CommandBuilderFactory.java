package de.energiequant.xprc.commands;

import de.energiequant.xprc.CMRGChannel;
import de.energiequant.xprc.CMRGCommandBuilder;
import de.energiequant.xprc.CMRGMessage;
import de.energiequant.xprc.Command;
import de.energiequant.xprc.DRLSChannel;
import de.energiequant.xprc.DRLSCommandBuilder;
import de.energiequant.xprc.DRLSMessage;
import de.energiequant.xprc.DRMUChannel;
import de.energiequant.xprc.DRMUCommandBuilder;
import de.energiequant.xprc.DRMUMessage;
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

        public <CB extends DRMUCommandBuilder<CB, CH, CFB, C>, CH extends DRMUChannel<CH, CFB, C>, CFB extends DRMUChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRMUMessage>> CB manipulateUncontrolled() {
            return rootFactory.drmu();
        }
    }

    public static class CommandCommandBuilderFactory {
        // TODO: come up with a better name? unfortunately this is about XPRC commands for XP commands

        private final CommandBuilderFactory rootFactory;

        private CommandCommandBuilderFactory(CommandBuilderFactory rootFactory) {
            this.rootFactory = rootFactory;
        }

        public <CB extends CMRGCommandBuilder<CB, CH, CFB, C>, CH extends CMRGChannel<CH, CFB, C>, CFB extends CMRGChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMRGMessage>> CB register() {
            return rootFactory.cmrg();
        }
    }

    public DataRefCommandBuilderFactory dataRefs() {
        return new DataRefCommandBuilderFactory(this);
    }

    public CommandCommandBuilderFactory commands() {
        return new CommandCommandBuilderFactory(this);
    }

    @SuppressWarnings("unchecked")
    public <CB extends CMRGCommandBuilder<CB, CH, CFB, C>, CH extends CMRGChannel<CH, CFB, C>, CFB extends CMRGChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMRGMessage>> CB cmrg() {
        return (CB) new CMRGCommandBuilder<CB, CH, CFB, C>(client);
    }

    @SuppressWarnings("unchecked")
    public <CB extends DRLSCommandBuilder<CB, CH, CFB, C>, CH extends DRLSChannel<CH, CFB, C>, CFB extends DRLSChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRLSMessage>> CB drls() {
        return (CB) new DRLSCommandBuilder<CB, CH, CFB, C>(client);
    }

    @SuppressWarnings("unchecked")
    public <CB extends DRMUCommandBuilder<CB, CH, CFB, C>, CH extends DRMUChannel<CH, CFB, C>, CFB extends DRMUChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRMUMessage>> CB drmu() {
        return (CB) new DRMUCommandBuilder<CB, CH, CFB, C>(client);
    }
}
