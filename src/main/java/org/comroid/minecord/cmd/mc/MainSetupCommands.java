package org.comroid.minecord.cmd.mc;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import org.comroid.minecord.MineCord;
import org.comroid.minecord.cmd.dc.MinecraftHandler;
import org.comroid.spiroid.api.command.SpiroidCommand;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum MainSetupCommands implements SpiroidCommand {
    CONNECT(
            "connect",
            new SpiroidCommand[0],
            (sender, args) -> {
                Optional<ServerTextChannel> opt = MineCord.bot.getServerTextChannelById(args[1]);

                if (!opt.isPresent())
                    return "No Channel found with ID " + args[1];
                final ServerTextChannel stc = opt.get();

                MinecraftHandler.postToChannels.add(stc.getId());

                return stc.sendMessage(DefaultEmbedFactory.create(stc.getServer())
                        .setDescription(String.format(
                                "Connection to %s established!",
                                MineCord.instance.getServer().getName()
                        )))
                        .thenApply(msg -> String.format(
                                "Connection to %s in %s established!",
                                stc, stc.getServer()
                        ))
                        .join();
            },
            filter -> filter.matches("\\d{6,20}")
                    ? new String[]{filter}
                    : new String[]{"<discord server text channel ID, bot must see the channel>"}
    ),
    UNCONNECT(
            "unconnect",
            new SpiroidCommand[0],
            (sender, args) -> {
                Optional<ServerTextChannel> opt = MineCord.bot.getServerTextChannelById(args[1]);

                if (!opt.isPresent())
                    return "No Channel found with ID " + args[1];
                final ServerTextChannel stc = opt.get();

                MinecraftHandler.postToChannels.remove(stc.getId());

                return stc.sendMessage(DefaultEmbedFactory.create(stc.getServer())
                        .setDescription(String.format(
                                "Connection to %s removed!",
                                MineCord.instance.getServer().getName()
                        )))
                        .thenApply(msg -> String.format(
                                "Connection to %s in %s removed!",
                                stc, stc.getServer()
                        ))
                        .join();
            },
            filter -> MinecraftHandler.postToChannels.stream()
                    .map(String::valueOf)
                    .filter(str -> str.startsWith(filter))
                    .toArray(String[]::new)
    ),
    LIST(
            "list",
            new SpiroidCommand[0],
            (sender, args) -> MinecraftHandler.channels.stream()
                    .map(stc -> String.format(
                            "%s%d %s- %s%s %sin %s%s",
                            ChatColor.GRAY,
                            stc.getId(),
                            ChatColor.RESET,
                            ChatColor.AQUA,
                            stc.getName(),
                            ChatColor.RESET,
                            ChatColor.DARK_AQUA,
                            stc.getServer().getName()
                    ))
                    .collect(Collectors.joining("\n", String.format("%sChat connections in this Server:\n", ChatColor.BLUE), "")),
            filter -> new String[0]
    );

    private final String name;
    private final SpiroidCommand[] subcommands;
    private final BiFunction<CommandSender, String[], @Nullable String> function;
    private final Function<String, String[]> completer;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SpiroidCommand[] getSubcommands() {
        return subcommands;
    }

    MainSetupCommands(
            String name,
            SpiroidCommand[] subcommands,
            BiFunction<CommandSender, String[], @Nullable String> function,
            Function<String, String[]> completer
    ) {
        this.name = name;
        this.subcommands = subcommands;
        this.function = function;
        this.completer = completer;
    }

    @Override
    public String[] tabComplete(String startsWith) {
        return completer.apply(startsWith);
    }

    @Override
    public @Nullable String execute(CommandSender sender, String[] args) {
        return function.apply(sender, args);
    }
}
