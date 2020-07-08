package org.comroid.minecord;

import com.google.common.flogger.FluentLogger;
import org.bukkit.configuration.MemoryConfiguration;
import org.comroid.minecord.cmd.dc.ChatHandler;
import org.comroid.minecord.cmd.mc.SpigotCommands;
import org.comroid.spiroid.api.AbstractPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;

public final class MineCord extends AbstractPlugin {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    public static DiscordApi bot;

    private Optional<String> getBotToken() {
        return Optional.ofNullable(getConfig().getString("discord-token"));
    }

    public MineCord() {
        super(SpigotCommands.values(), "config", "users");
    }

    @Override
    public void onLoad() {
        getConfig();
        getConfig("users");
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        logger.at(Level.INFO).log("Loading Discord Subservice...");

        getServer().getPluginManager()
                .registerEvents(ChatHandler.INSTANCE, this);

        final Optional<String> token = getBotToken();
        if (!token.isPresent())
            throw new NoSuchElementException("No Bot token defined in config.yml");
        if (bot != null)
            throw new IllegalStateException("Bot is not null!");
        bot = new DiscordApiBuilder()
                .setToken(token.get())
                .login()
                .join();

        logger.at(Level.INFO).log(
                "Logged in to Discord as user %s",
                bot.getYourself().getDiscriminatedName()
        );
    }

    @Override
    public void onDisable() {
        // shutdown bot
        logger.at(Level.INFO).log("Shutting down discord bot...");
        bot.disconnect();
        bot = null;
        logger.at(Level.INFO).log("Discord bot shut down");
    }

    @Override
    protected Optional<MemoryConfiguration> getConfigDefaults(String name) {
        final MemoryConfiguration config = new MemoryConfiguration();

        switch (name) {
            case "config":
                config.set("discord-token", "<token here>");

                break;
            case "users":
                config.set("registered", new ArrayList<>());

                break;
            default:
                return Optional.empty();
        }

        return Optional.of(config);
    }
}
