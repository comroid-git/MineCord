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
    public static DiscordApi bot;

    private Optional<String> getBotToken() {
        return Optional.ofNullable(getConfig().getString("discord-token"));
    }

    public MineCord() {
        super(SpigotCommands.values(), "config", "users");
    }

    @Override
    public void enable() {
        getServer().getPluginManager()
                .registerEvents(ChatHandler.INSTANCE, this);
        getLogger().log(Level.INFO, "Attached Chathandler");

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

        bot.addListener(BotHandler.INSTANCE);
        getLogger().log(Level.INFO,"Attached Bot Listener");

                ChatHandler.INSTANCE.initialize();
        getLogger().log(Level.INFO, String.format("Initialized %d ChatHandler connections", ChatHandler.postToChannels.size()));
    }

    @Override
    public void disable() {
        ChatHandler.INSTANCE.deinitialize();
        getLogger().log(Level.INFO, String.format("Stored %d ChatHandler connections", ChatHandler.postToChannels.size()));

        // shutdown bot
        getLogger().log(Level.INFO, "Shutting down discord bot...");
        bot.disconnect();
        bot = null;
        getLogger().log(Level.INFO, "Discord bot shut down");
    }

    @Override
    protected Optional<MemoryConfiguration> getConfigDefaults(String name) {
        final MemoryConfiguration config = new MemoryConfiguration();

        switch (name) {
            case "config":
                config.set("discord-token", "<token here>");
                config.set("connections", new ArrayList<>());

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
