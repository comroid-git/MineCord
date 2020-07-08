package org.comroid.minecord.cmd.mc;

import org.bukkit.command.CommandSender;
import org.comroid.minecord.MineCord;
import org.comroid.spiroid.api.command.SpiroidCommand;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum SpigotCommands implements SpiroidCommand {
    MAIN_SETUP(
            "minecord",
            MainSetupCommands.values(),
            (sender, args) -> MineCord.instance.toString(),
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
