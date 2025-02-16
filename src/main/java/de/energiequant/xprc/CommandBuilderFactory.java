package de.energiequant.xprc;

import de.energiequant.xprc.commands.CMRGChannel;
import de.energiequant.xprc.commands.CMRGCommandBuilder;
import de.energiequant.xprc.commands.CMRGMessage;
import de.energiequant.xprc.commands.DRCIChannel;
import de.energiequant.xprc.commands.DRCICommandBuilder;
import de.energiequant.xprc.commands.DRCIMessage;
import de.energiequant.xprc.commands.DRLSChannel;
import de.energiequant.xprc.commands.DRLSCommandBuilder;
import de.energiequant.xprc.commands.DRLSMessage;
import de.energiequant.xprc.commands.DRMUChannel;
import de.energiequant.xprc.commands.DRMUCommandBuilder;
import de.energiequant.xprc.commands.DRMUMessage;

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

        public <CB extends DRCICommandBuilder<CB, CH, CFB, C>, CH extends DRCIChannel<CH, CFB, C>, CFB extends DRCIChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRCIMessage>> CB claimImmediate() {
            return rootFactory.drci();
        }

        public <CB extends DRCICommandBuilder<CB, CH, CFB, C>, CH extends DRCIChannel<CH, CFB, C>, CFB extends DRCIChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRCIMessage>> CB claimImmediate(DataRef<?> dataRef) {
            return rootFactory.drci(dataRef);
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
    public <CB extends DRCICommandBuilder<CB, CH, CFB, C>, CH extends DRCIChannel<CH, CFB, C>, CFB extends DRCIChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRCIMessage>> CB drci() {
        return (CB) new DRCICommandBuilder<CB, CH, CFB, C>(client);
    }

    @SuppressWarnings("unchecked")
    public <CB extends DRCICommandBuilder<CB, CH, CFB, C>, CH extends DRCIChannel<CH, CFB, C>, CFB extends DRCIChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRCIMessage>> CB drci(DataRef<?> dataRef) {
        return (CB) new DRCICommandBuilder<CB, CH, CFB, C>(client, dataRef);
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
