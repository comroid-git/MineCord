package org.comroid.minecord;

import com.google.common.flogger.FluentLogger;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import org.bukkit.configuration.MemoryConfiguration;
import org.comroid.spiroid.api.AbstractPlugin;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;

public final class MineCord extends AbstractPlugin {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    public GatewayDiscordClient bot;

    private Optional<String> getBotToken() {
        return Optional.ofNullable(getConfig().getString("discord-token"));
    }

    @Override
    public void onLoad() {
        getConfig();
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        logger.at(Level.INFO).log("Loading Discord Subservice...");

        final Optional<String> token = getBotToken();
        if (!token.isPresent())
            throw new NoSuchElementException("No Bot token defined in config.yml");
        if (bot != null)
            throw new IllegalStateException("Bot is not null!");
        this.bot = DiscordClient.create(token.get()).login().block();

        logger.at(Level.INFO).log("Logged in to Discord as user %s",
                bot.getSelf()
                        .map(usr -> String.format("%s#%s", usr.getUsername(), usr.getDiscriminator()))
                        .block()
        );
    }

    @Override
    public void onDisable() {
        // shutdown bot
        logger.at(Level.INFO).log("Shutting down discord bot...");
        this.bot.logout().block();
        this.bot = null;
        logger.at(Level.INFO).log("Discord bot shut down");
    }

    @Override
    protected Optional<MemoryConfiguration> getConfigDefaults(String name) {
        final MemoryConfiguration config = new MemoryConfiguration();

        switch (name) {
            case "config":
                config.set("discord-token", null);

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
