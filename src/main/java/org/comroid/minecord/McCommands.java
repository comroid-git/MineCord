package org.comroid.minecord;

import org.bukkit.command.CommandSender;
import org.comroid.common.info.MessageSupplier;
import org.comroid.spiroid.api.command.SpiroidCommand;
import org.jetbrains.annotations.Nullable;

public enum McCommands implements SpiroidCommand {
    MAIN_SETUP("minecord") {
        @Override
        public Object execute(CommandSender sender, boolean simulate, String arg) {
            final class Connector implements SpiroidCommand {
                @Override
                public Object execute(CommandSender sender, boolean simulate, @Nullable String arg) {
                    if (arg == null || arg.isEmpty())
                        return "No ID Found";

                    return MineCord.bot.getServerTextChannelById(arg)
                            .map(stc -> {
                                ChatHandler.postToChannels.add(stc.getId());
                                return String.format("Successfully connected chat to Discord Channel %s", stc);
                            })
                            .orElseGet(MessageSupplier.format("Could not find channel with ID %s", arg));
                }

                @Override
                public String getName() {
                    return "connect";
                }
            }

            if (arg == null)
                return MineCord.instance.toString();

            switch (arg) {
                case "connect":
                    return new Connector();
            }

            return MineCord.instance.toString();
        }
    };

    private final String name;

    @Override
    public String getName() {
        return name;
    }

    McCommands(String name) {
        this.name = name;
    }

    @Override
    public Object execute(CommandSender sender, boolean simulate, String arg) {
        throw new AbstractMethodError();
    }
}
