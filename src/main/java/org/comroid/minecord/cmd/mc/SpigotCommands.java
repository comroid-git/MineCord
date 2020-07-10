package org.comroid.minecord.cmd.mc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.comroid.common.exception.AssertionException;
import org.comroid.common.info.MessageSupplier;
import org.comroid.minecord.MineCord;
import org.comroid.minecord.validator.Validator;
import org.comroid.spiroid.api.command.SpiroidCommand;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum SpigotCommands implements SpiroidCommand {
    MAIN_SETUP(
            "minecord",
            MainSetupCommands.values(),
            (sender, args) -> MineCord.instance.toString(),
            filter -> new String[0]
    ),
    VALIDATION(
            "validate",
            new SpiroidCommand[0],
            (sender, args) -> {
                AssertionException.expect(true, sender instanceof Entity, "sender instanceof Entity");

                if (Validator.isRegistered(((Entity) sender).getUniqueId()))
                    return ChatColor.BLUE + "You already are registered!";

                return String.format(
                        "%sPlease send a DM containing the following text to %s%s%s in order to validate your identity:" +
                                "\n%s%s" +
                                "\n%sLink to DM: https://discord.com/channels/@me/%s",
                        ChatColor.AQUA,
                        ChatColor.DARK_AQUA,
                        MineCord.bot.getYourself().getDiscriminatedName(),
                        ChatColor.AQUA,
                        ChatColor.RED,
                        Validator.create(sender).getSecret(),
                        ChatColor.AQUA,
                        MineCord.bot.getYourself().getIdAsString()
                );
            },
            filter -> new String[0]
    ),
    DISCORD_WHOIS(
            "discord",
            new SpiroidCommand[0],
            (sender, args) -> {
                if (sender instanceof Entity) {
                    final UUID uuid = ((Entity) sender).getUniqueId();

                    if (!Validator.isRegistered(uuid))
                        return ChatColor.RED + "You are not registered and cannot use this command";

                    return Arrays.stream(Bukkit.getOfflinePlayers())
                            .filter(plr -> Objects.equals(plr.getName(), args[0]))
                            .findAny()
                            .map(OfflinePlayer::getUniqueId)
                            .map(Validator::getDiscordUser)
                            .map(User::getDiscriminatedName)
                            .map(name -> String.format("The discord username of %s is %s", args[0], name))
                            .orElseGet(MessageSupplier.format("No user found with name %s", args[0]));
                }
                throw new AssertionError();
            },
            filter -> Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getDisplayName)
                    .toArray(String[]::new)
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

    SpigotCommands(
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
