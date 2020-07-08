package org.comroid.minecord.validator;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.comroid.minecord.MineCord;
import org.comroid.spiroid.api.model.BiInitializable;
import org.comroid.trie.TrieMap;
import org.javacord.api.entity.user.User;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class Validator implements BiInitializable {
    private static final Map<UUID, Long> registered = TrieMap.parsing(UUID::fromString);
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
                .encodeToString((mcID.toString() + MineCord.instance.toString()).getBytes());
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
        registered.put(uuid, user.getId());
        MineCord.instance.getConfig().createSection("registered", registered);
        instances.remove(uuid);
    }

    @Override
    public void initialize() {
        final Configuration cfg = MineCord.instance.getConfig();
        final ConfigurationSection registered = cfg.getConfigurationSection("registered");

        MineCord.instance.getLogger()
                .log(Level.INFO, "Loading registered users...");

        if (registered != null)
            registered.getKeys(false)
                    .forEach(key -> {
                        final long id = registered.getLong(key);

                        if (MineCord.bot.getUserById(id).join().getId() == id)
                            Validator.registered.put(UUID.fromString(key), id);
                    });

        MineCord.instance.getLogger()
                .log(Level.INFO, String.format("Loaded %d registered Discord users", Validator.registered.size()));
    }

    @Override
    public void deinitialize() {
        final Configuration cfg = MineCord.instance.getConfig();
        MineCord.instance.getConfig().createSection("registered", registered);
    }
}
