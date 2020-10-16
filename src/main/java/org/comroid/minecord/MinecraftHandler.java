package org.comroid.minecord;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import org.comroid.minecord.validator.Validator;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.span.Span;
import org.comroid.spiroid.api.chat.MessageLevel;
import org.comroid.spiroid.api.chat.PlayerNotifier;
import org.comroid.spiroid.api.model.BiInitializable;
import org.comroid.spiroid.api.util.BukkitUtil;
import org.comroid.util.ReflectionHelper;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.embed.EmbedFooter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public enum MinecraftHandler implements Listener, BiInitializable {
    INSTANCE;

    public static final Span<Long> postToChannels = new Span<>();
    public static final Span<ServerTextChannel> channels = postToChannels.pipe()
            .map(id -> MineCord.bot.getServerTextChannelById(id))
            .flatMap(opt -> opt.map(Reference::constant).orElseGet(Reference::empty))
            .span();

    private static CompletableFuture<Message> appendEmbed(ServerTextChannel stc, String displayName, EmbedBuilder newContent) {
        return stc.getMessages(1)
                .thenApplyAsync(MessageSet::getNewestMessage)
                .thenComposeAsync(opt -> opt
                        .filter(msg -> msg.getAuthor().isYourself())
                        .filter(latest -> latest.getEmbeds()
                                .get(0)
                                .getFooter()
                                .flatMap(EmbedFooter::getText)
                                .orElse("")
                                .equals(displayName))
                        .map(message -> {
                            final Embed old = message.getEmbeds().get(0);
                            final EmbedBuilder builder = old.toBuilder();

                            builder.setDescription(
                                    old.getDescription()
                                            .map(str -> str + '\n' + ReflectionHelper.forceGetField(newContent.getDelegate(), "description"))
                                            .orElseGet(() -> ReflectionHelper.forceGetField(newContent.getDelegate(), "description"))
                            );

                            return message.edit(builder).thenApply(nil -> message);
                        })
                        .orElseGet(() -> stc.sendMessage(newContent))
                );

    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        channels.pipe()
                .bi(ServerChannel::getServer)
                .mapSecond(DefaultEmbedFactory::create)
                .peek((stc, embed) -> embed.setDescription(event.getMessage())
                        .setFooter(
                                event.getPlayer().getDisplayName(),
                                String.format("https://minotar.net/helm/%s/100.png", event.getPlayer().getName())
                        ))
                .forEach((stc, embed) -> appendEmbed(stc, event.getPlayer().getDisplayName(), embed));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final UUID plr = event.getPlayer().getUniqueId();

        if (!Validator.isRegistered(plr)) {
            final PlayerNotifier notifier = MineCord.instance.getPlayerNotifier(event.getPlayer());

            notifier.sendMessage(MineCord.MessageLevel.WARN, "²[³Discord²] ³You are not registered.");
            notifier.sendMessage(MineCord.MessageLevel.WARN, "²Please register using ³/validate², otherwise you will be kicked in ³3 minutes².");

            MineCord.instance.schedule(3, TimeUnit.MINUTES, () -> {
                if (!Validator.isRegistered(plr))
                    event.getPlayer().kickPlayer("Please register using ingame or discord bot commands.");
            });
        }
    }

    public void playerCountStatus() {
        MineCord.bot.updateActivity(String.format("%d online Users", Bukkit.getOnlinePlayers().size()));
    }

    @Override
    public void initialize() {
        final Configuration cfg = MineCord.instance.getConfig();

        cfg.getLongList("connections")
                .stream()
                .map(MineCord.bot::getServerTextChannelById)
                .flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
                .map(DiscordEntity::getId)
                .forEach(postToChannels::add);

        final long time = BukkitUtil.time2tick(10, TimeUnit.SECONDS);
        Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(
                        MineCord.instance,
                        this::playerCountStatus,
                        0,
                        time
                );
    }

    @Override
    public void deinitialize() {
        final Configuration cfg = MineCord.instance.getConfig();

        cfg.set("connections", postToChannels.unwrap());
    }
}
