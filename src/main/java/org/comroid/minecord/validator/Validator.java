package org.comroid.minecord.validator;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.comroid.minecord.MineCord;
import org.comroid.mutatio.ref.Processor;
import org.javacord.api.entity.user.User;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class Validator {
    private static final Map<UUID, Long> registeredByMinecraft = new ConcurrentHashMap<>();
    private static final Map<String, Validator> instances = new ConcurrentHashMap<>();

    private final String counterpart;
    private final String secret;

    public String getCounterpart() {
        return counterpart;
    }

    public String getSecret() {
        return secret;
    }

    private Validator(String counterpart) {
        this.counterpart = counterpart;

        String encoded = Base64.getEncoder().encodeToString((counterpart + System.nanoTime()).getBytes());
        this.secret = Integer.toHexString(encoded.hashCode());
    }

    public static Validator create(CommandSender forSender) {
        if (forSender instanceof Entity) {
            final UUID uuid = ((Entity) forSender).getUniqueId();
            return instances.computeIfAbsent(uuid.toString(), key -> new Validator(uuid.toString()));
        } else throw new IllegalArgumentException(forSender.getClass().getName());
    }

    public static Validator create(User user) {
        return instances.computeIfAbsent(user.getIdAsString(), key -> new Validator(user.getIdAsString()));
    }

    public static Processor<Validator> findBySecret(String secret) {
        return Processor.ofOptional(instances.values()
                .stream()
                .filter(val -> val.secret.equals(secret))
                .findFirst());
    }

    public static void register(UUID uuid, User user) {
        if (instances.remove(uuid.toString()) != null) {
            registeredByMinecraft.put(uuid, user.getId());
            MineCord.instance.getConfig().createSection("registered", registeredByMinecraft);
        }
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

    public static boolean isRegistered(User user) {
        return registeredByMinecraft.containsValue(user.getId());
    }

    public static User getDiscordUser(UUID uuid) {
        return MineCord.bot.getUserById(registeredByMinecraft.get(uuid)).join();
    }
}
