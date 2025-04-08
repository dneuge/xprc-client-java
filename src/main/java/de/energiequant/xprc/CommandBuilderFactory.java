package de.energiequant.xprc;

import java.util.Arrays;
import java.util.Collection;

import de.energiequant.xprc.commands.CMHDChannel;
import de.energiequant.xprc.commands.CMHDCommandBuilder;
import de.energiequant.xprc.commands.CMHDMessage;
import de.energiequant.xprc.commands.CMRGChannel;
import de.energiequant.xprc.commands.CMRGCommandBuilder;
import de.energiequant.xprc.commands.CMRGMessage;
import de.energiequant.xprc.commands.CMTRChannel;
import de.energiequant.xprc.commands.CMTRCommandBuilder;
import de.energiequant.xprc.commands.CMTRMessage;
import de.energiequant.xprc.commands.DRCIChannel;
import de.energiequant.xprc.commands.DRCICommandBuilder;
import de.energiequant.xprc.commands.DRCIMessage;
import de.energiequant.xprc.commands.DRLSChannel;
import de.energiequant.xprc.commands.DRLSCommandBuilder;
import de.energiequant.xprc.commands.DRLSMessage;
import de.energiequant.xprc.commands.DRMUChannel;
import de.energiequant.xprc.commands.DRMUCommandBuilder;
import de.energiequant.xprc.commands.DRMUMessage;
import de.energiequant.xprc.commands.DRQVChannel;
import de.energiequant.xprc.commands.DRQVCommandBuilder;
import de.energiequant.xprc.commands.DRQVMessage;

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

        public <CB extends DRQVCommandBuilder<CB, CH, CFB, C>, CH extends DRQVChannel<CH, CFB, C>, CFB extends DRQVChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRQVMessage>> CB query() {
            return rootFactory.drqv();
        }

        public <CB extends DRQVCommandBuilder<CB, CH, CFB, C>, CH extends DRQVChannel<CH, CFB, C>, CFB extends DRQVChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRQVMessage>> CB query(DataRef<?>... dataRefs) {
            return rootFactory.drqv(dataRefs);
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

        public <CB extends CMHDCommandBuilder<CB, CH, CFB, C>, CH extends CMHDChannel<CH, CFB, C>, CFB extends CMHDChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMHDMessage>> CB hold() {
            return rootFactory.cmhd();
        }

        public <CB extends CMHDCommandBuilder<CB, CH, CFB, C>, CH extends CMHDChannel<CH, CFB, C>, CFB extends CMHDChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMHDMessage>> CB hold(String... commandNames) {
            return rootFactory.cmhd(commandNames);
        }

        public <CB extends CMHDCommandBuilder<CB, CH, CFB, C>, CH extends CMHDChannel<CH, CFB, C>, CFB extends CMHDChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMHDMessage>> CB hold(Collection<String> commandNames) {
            return rootFactory.cmhd(commandNames);
        }

        public <CB extends CMRGCommandBuilder<CB, CH, CFB, C>, CH extends CMRGChannel<CH, CFB, C>, CFB extends CMRGChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMRGMessage>> CB register() {
            return rootFactory.cmrg();
        }

        public <CB extends CMTRCommandBuilder<CB, CH, CFB, C>, CH extends CMTRChannel<CH, CFB, C>, CFB extends CMTRChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMTRMessage>> CB trigger() {
            return rootFactory.cmtr();
        }

        public <CB extends CMTRCommandBuilder<CB, CH, CFB, C>, CH extends CMTRChannel<CH, CFB, C>, CFB extends CMTRChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMTRMessage>> CB trigger(String... commandNames) {
            return rootFactory.cmtr(commandNames);
        }

        public <CB extends CMTRCommandBuilder<CB, CH, CFB, C>, CH extends CMTRChannel<CH, CFB, C>, CFB extends CMTRChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMTRMessage>> CB trigger(Collection<String> commandNames) {
            return rootFactory.cmtr(commandNames);
        }
    }

    public DataRefCommandBuilderFactory dataRefs() {
        return new DataRefCommandBuilderFactory(this);
    }

    public CommandCommandBuilderFactory commands() {
        return new CommandCommandBuilderFactory(this);
    }

    @SuppressWarnings("unchecked")
    public <CB extends CMHDCommandBuilder<CB, CH, CFB, C>, CH extends CMHDChannel<CH, CFB, C>, CFB extends CMHDChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMHDMessage>> CB cmhd() {
        return (CB) new CMHDCommandBuilder<CB, CH, CFB, C>(client);
    }

    public <CB extends CMHDCommandBuilder<CB, CH, CFB, C>, CH extends CMHDChannel<CH, CFB, C>, CFB extends CMHDChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMHDMessage>> CB cmhd(String... commandNames) {
        return cmhd(Arrays.asList(commandNames));
    }

    @SuppressWarnings("unchecked")
    public <CB extends CMHDCommandBuilder<CB, CH, CFB, C>, CH extends CMHDChannel<CH, CFB, C>, CFB extends CMHDChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMHDMessage>> CB cmhd(Collection<String> commandNames) {
        return (CB) new CMHDCommandBuilder<CB, CH, CFB, C>(client, commandNames);
    }

    @SuppressWarnings("unchecked")
    public <CB extends CMRGCommandBuilder<CB, CH, CFB, C>, CH extends CMRGChannel<CH, CFB, C>, CFB extends CMRGChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMRGMessage>> CB cmrg() {
        return (CB) new CMRGCommandBuilder<CB, CH, CFB, C>(client);
    }

    @SuppressWarnings("unchecked")
    public <CB extends CMTRCommandBuilder<CB, CH, CFB, C>, CH extends CMTRChannel<CH, CFB, C>, CFB extends CMTRChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMTRMessage>> CB cmtr() {
        return (CB) new CMTRCommandBuilder<CB, CH, CFB, C>(client);
    }

    public <CB extends CMTRCommandBuilder<CB, CH, CFB, C>, CH extends CMTRChannel<CH, CFB, C>, CFB extends CMTRChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMTRMessage>> CB cmtr(String... commandNames) {
        return cmtr(Arrays.asList(commandNames));
    }

    @SuppressWarnings("unchecked")
    public <CB extends CMTRCommandBuilder<CB, CH, CFB, C>, CH extends CMTRChannel<CH, CFB, C>, CFB extends CMTRChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, CMTRMessage>> CB cmtr(Collection<String> commandNames) {
        return (CB) new CMTRCommandBuilder<CB, CH, CFB, C>(client, commandNames);
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
    public <CB extends DRQVCommandBuilder<CB, CH, CFB, C>, CH extends DRQVChannel<CH, CFB, C>, CFB extends DRQVChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRQVMessage>> CB drqv() {
        return (CB) new DRQVCommandBuilder<CB, CH, CFB, C>(client);
    }

    @SuppressWarnings("unchecked")
    public <CB extends DRQVCommandBuilder<CB, CH, CFB, C>, CH extends DRQVChannel<CH, CFB, C>, CFB extends DRQVChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRQVMessage>> CB drqv(DataRef<?>... dataRefs) {
        return (CB) new DRQVCommandBuilder<CB, CH, CFB, C>(client, Arrays.asList(dataRefs));
    }

    @SuppressWarnings("unchecked")
    public <CB extends DRMUCommandBuilder<CB, CH, CFB, C>, CH extends DRMUChannel<CH, CFB, C>, CFB extends DRMUChannel.FactoryBuilder<CFB, CH, C>, C extends Command<CFB, CH, C, DRMUMessage>> CB drmu() {
        return (CB) new DRMUCommandBuilder<CB, CH, CFB, C>(client);
    }
}
