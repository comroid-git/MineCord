package org.comroid.minecord;

import org.bukkit.configuration.MemoryConfiguration;
import org.comroid.javacord.util.commands.CommandHandler;
import org.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import org.comroid.minecord.dc_cmd.DiscordCommands;
import org.comroid.minecord.mc_cmd.SpigotCommands;
import org.comroid.minecord.validator.Validator;
import org.comroid.spiroid.api.AbstractPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;

public final class MineCord extends AbstractPlugin {
    public static DiscordApi bot;
    public static CommandHandler cmd;

    private Optional<String> getBotToken() {
        return Optional.ofNullable(getConfig().getString("discord-token"));
    }

    public MineCord() {
        super(SpigotCommands.values(), "config");
    }

    @Override
    public void enable() {
        getServer().getPluginManager()
                .registerEvents(MinecraftHandler.INSTANCE, this);
        getLogger().log(Level.INFO, "Attached ChatHandler");

        getLogger().log(Level.INFO, "Loading Discord Subservice...");
        final Optional<String> token = getBotToken();
        if (!token.isPresent())
            throw new NoSuchElementException("No Bot token defined in config.yml");
        if (bot != null)
            throw new IllegalStateException("Bot is not null!");
        bot = new DiscordApiBuilder()
                .setToken(token.get())
                .login()
                .join();
        getLogger().log(Level.INFO, "Logged in to Discord as user " + bot.getYourself().getDiscriminatedName());
        getLogger().log(Level.INFO, "Initializing Discord command Framework");
        cmd = new CommandHandler(bot, true);
        cmd.prefixes = new String[]{"minecord!"};
        cmd.withUnknownCommandResponseStatus(true);
        cmd.useDefaultHelp(DefaultEmbedFactory.INSTANCE);
        cmd.registerCommandTarget(DiscordCommands.INSTANCE);
        getLogger().log(Level.INFO, "Command Framework running");

        bot.addListener(BotHandler.INSTANCE);
        getLogger().log(Level.INFO, "Attached Bot Listener");

        Validator.initialize();
        getLogger().log(Level.INFO, String.format("Initialized Validator; %d registered users loaded", Validator.count()));

        MinecraftHandler.INSTANCE.initialize();
        getLogger().log(Level.INFO, String.format("Initialized %d ChatHandler connections", MinecraftHandler.postToChannels.size()));
    }

    @Override
    public void disable() {
        MinecraftHandler.INSTANCE.deinitialize();
        getLogger().log(Level.INFO, String.format("Stored %d ChatHandler connections", MinecraftHandler.postToChannels.size()));

        // shutdown bot
        getLogger().log(Level.INFO, "Shutting down discord bot...");
        bot.disconnect();
        bot = null;
        getLogger().log(Level.INFO, "Discord bot shut down");

        Validator.deinitialize();
        getLogger().log(Level.INFO, String.format("Deinitialized Validator, stored %d registered users", Validator.count()));
    }

    @Override
    protected Optional<MemoryConfiguration> getConfigDefaults(String name) {
        final MemoryConfiguration config = new MemoryConfiguration();

        switch (name) {
            case "config":
                config.set("discord-token", "<token here>");
                config.set("connections", new ArrayList<>());
                config.createSection("registered");

                break;
            default:
                return Optional.empty();
        }

        return Optional.of(config);
    }
}
