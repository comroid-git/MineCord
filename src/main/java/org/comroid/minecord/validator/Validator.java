package org.comroid.minecord.validator;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.comroid.minecord.MineCord;
import org.comroid.trie.TrieMap;
import org.javacord.api.entity.user.User;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class Validator {
    private static final Map<Long, UUID> registeredByDiscord = TrieMap.parsingCached(Long::parseLong);
    private static final Map<UUID, Long> registeredByMinecraft = TrieMap.parsing(UUID::fromString);
    private static final Map<UUID, Validator> instances = TrieMap.parsing(UUID::fromString);

    private final UUID mcID;
    private final String secret;

    public UUID getUUID() {
        return mcID;
    }

    public String getSecret() {
        return secret;
    }

    private Validator(UUID mcID) {
        this.mcID = mcID;
        this.secret = Base64.getEncoder()
                .encodeToString((mcID.toString() + System.nanoTime()).getBytes());
    }

    public static Validator create(CommandSender forSender) {
        if (forSender instanceof Entity)
            return instances.computeIfAbsent(((Entity) forSender).getUniqueId(), Validator::new);
        else throw new IllegalArgumentException(forSender.getClass().getName());
    }

    public static Optional<Validator> findBySecret(String secret) {
        return instances.values()
                .stream()
                .filter(val -> val.secret.equals(secret))
                .findFirst();
    }

    public static void register(UUID uuid, User user) {
        registeredByMinecraft.put(uuid, user.getId());
        MineCord.instance.getConfig().createSection("registered", registeredByMinecraft);
        instances.remove(uuid);
    }

    public static void initialize() {
        final Configuration cfg = MineCord.instance.getConfig();
        final ConfigurationSection registered = cfg.getConfigurationSection("registered");

        MineCord.instance.getLogger()
                .log(Level.INFO, "Loading registered users...");

        if (registered != null)
            registered.getKeys(false)
                    .forEach(key -> {
                        final long id = registered.getLong(key);

                        if (MineCord.bot.getUserById(id).join().getId() == id)
                            Validator.registeredByMinecraft.put(UUID.fromString(key), id);
                    });

        MineCord.instance.getLogger()
                .log(Level.INFO, String.format("Loaded %d registered Discord users", Validator.registeredByMinecraft.size()));
    }

    public static void deinitialize() {
        final Configuration cfg = MineCord.instance.getConfig();
        MineCord.instance.getConfig().createSection("registered", registeredByMinecraft);
    }

    public static int count() {
        return registeredByMinecraft.size();
    }

    public static boolean isRegistered(UUID uuid) {
        return registeredByMinecraft.containsKey(uuid);
    }

    public static User getDiscordUser(UUID uuid) {
        return MineCord.bot.getUserById(registeredByMinecraft.get(uuid)).join();
    }
}
