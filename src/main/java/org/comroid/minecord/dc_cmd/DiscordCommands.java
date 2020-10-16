package org.comroid.minecord.dc_cmd;

import org.bukkit.Bukkit;
import org.comroid.common.info.MessageSupplier;
import org.comroid.javacord.util.commands.Command;
import org.comroid.javacord.util.commands.CommandGroup;
import org.comroid.minecord.validator.Validator;
import org.comroid.mutatio.proc.Processor;
import org.javacord.api.entity.user.User;

import java.util.Arrays;

@CommandGroup(name = "MineCord Commands", description = "All commands for interacting with MineCord")
public enum DiscordCommands {
    INSTANCE;

    @Command(
            description = "Link your Discord and Minecraft Accounts",
            usage = "validate [MC Username]",
            maximumArguments = 1,
            convertStringResultsToEmbed = true,
            useTypingIndicator = true,
            async = true
    )
    public Object validate(User user, String[] args) {
        if (Validator.isRegistered(user))
            return "You already are registered";

        if (args.length == 1) {
            // validate using username
            return Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(player -> Processor.ofConstant(player.getName()).into(args[0]::equals))
                    .findAny()
                    .map(player -> {
                        Validator.register(player.getUniqueId(), user);
                        return String.format(
                                "Successfully connected with Minecraft Account `%s`!",
                                Processor.ofConstant(player.getName()).orElseGet(() -> player.getUniqueId().toString())
                        );
                    })
                    .orElseGet(MessageSupplier.format("Could not find Minecraft user with name %s", args[0]));
        } else {
            // validate using token
            return user.sendMessage(String.format(
                    "Please execute the following Minecraft Command:\n" +
                            "```\n" +
                            "/validate %s\n" +
                            "```",
                    Validator.create(user).getSecret()
            )).thenApplyAsync(msg -> "Please check your DMs!");
        }
    }
}
