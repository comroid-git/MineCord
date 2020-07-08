package org.comroid.minecord;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.comroid.minecord.cmd.dc.ChatHandler;
import org.comroid.minecord.validator.Validator;
import org.javacord.api.entity.Nameable;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.user.UserStartTypingEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.user.UserStartTypingListener;

import java.util.NoSuchElementException;

public enum BotHandler implements MessageCreateListener {
    INSTANCE;

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.isPrivateMessage()) {
            Validator.findBySecret(event.getMessageContent())
                    .ifPresent(validator -> {
                        Validator.register(validator.getUUID(), event.getMessageAuthor()
                                .asUser()
                                .orElseThrow(NoSuchElementException::new));
                        event.getChannel().sendMessage("You are now registered!");
                    });
        }

        if (event.getReadableMessageContent().length() > 0
                && ChatHandler.postToChannels.contains(event.getChannel().getId())) {
            Bukkit.broadcastMessage(
                    String.format(
                            "%sDiscord %s- %s%s%s %s- %s%s%s: %s%s",
                            ChatColor.DARK_AQUA,
                            ChatColor.RESET,
                            ChatColor.AQUA,
                            event.getServerTextChannel()
                                    .map(Nameable::getName)
                                    .orElse("unknown channel"),
                            ChatColor.RESET,
                            ChatColor.RESET,
                            ChatColor.AQUA,
                            event.getMessageAuthor()
                                    .asUser()
                                    .map(Nameable::getName)
                                    .orElse("unknown user"),
                            ChatColor.RESET,
                            ChatColor.GRAY,
                            event.getReadableMessageContent()
                    )
            );
        }
    }
}
