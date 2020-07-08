package org.comroid.minecord.cmd.dc;

import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import org.comroid.minecord.MineCord;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.span.Span;
import org.comroid.spiroid.api.model.BiInitializable;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

public enum ChatHandler implements Listener, BiInitializable {
    INSTANCE;

    public static final Span<Long> postToChannels = new Span<>();
    public static final Span<ServerTextChannel> channels = postToChannels.pipe()
            .map(id -> MineCord.bot.getServerTextChannelById(id))
            .flatMap(opt -> opt.map(Reference::constant).orElseGet(Reference::empty))
            .span();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        channels.pipe()
                .bi(ServerChannel::getServer)
                .mapSecond(DefaultEmbedFactory::create)
                .peek((stc, embed) -> embed.addField(event.getPlayer().getDisplayName(), event.getMessage())
                        .setFooter("Minecraft Server Chat", "https://www.minecraft.net/etc.clientlibs/minecraft/clientlibs/main/resources/android-icon-192x192.png"))
                .forEach((BiConsumer<ServerTextChannel, EmbedBuilder>) Messageable::sendMessage);
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
    }

    @Override
    public void deinitialize() {
        final Configuration cfg = MineCord.instance.getConfig();

        cfg.set("connections", postToChannels.unwrap());
    }
}
