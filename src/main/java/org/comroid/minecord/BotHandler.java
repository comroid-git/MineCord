package org.comroid.minecord;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.comroid.minecord.cmd.dc.ChatHandler;
import org.javacord.api.entity.Nameable;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public enum BotHandler implements MessageCreateListener {
    INSTANCE;

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (ChatHandler.postToChannels.contains(event.getChannel().getId())
                && event.getMessageContent().toLowerCase().contains("mc:")) {
            Bukkit.broadcastMessage(
                    String.format(
                            "%sDiscord %s- %s%s%s: %s%s",
                            ChatColor.DARK_AQUA,
                            ChatColor.RESET,
                            ChatColor.AQUA,
                            event.getServerTextChannel()
                                    .map(Nameable::getName)
                                    .orElse("unknown channel"),
                            ChatColor.RESET,
                            ChatColor.GRAY,
                            event.getReadableMessageContent()
                    )
            );
        }
    }
}
