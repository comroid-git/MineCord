package org.comroid.minecord;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.span.Span;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.function.BiConsumer;

public enum ChatHandler implements Listener {
    INSTANCE;

    public static final Span<Long> postToChannels = new Span<>();
    private static final Span<ServerTextChannel> channels = postToChannels.pipe()
            .map(id -> MineCord.bot.getServerTextChannelById(id))
            .flatMap(opt -> opt.map(Reference::constant).orElseGet(Reference::empty))
            .span();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        channels.pipe()
                .bi(ServerChannel::getServer)
                .mapSecond(DefaultEmbedFactory::create)
                .peek((stc, embed) -> embed.addField(event.getPlayer().getDisplayName(), event.getMessage()))
                .forEach((BiConsumer<ServerTextChannel, EmbedBuilder>) Messageable::sendMessage);
    }
}
