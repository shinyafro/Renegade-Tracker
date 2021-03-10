package renegade.planetside2.handlers;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.storage.Configuration;
import renegade.planetside2.tracker.UserManager;

import static renegade.planetside2.util.Utility.*;

public class DiscordEvents {
    final UserManager manager;

    public DiscordEvents(UserManager manager){
        this.manager = manager;
    }
    @SubscribeEvent
    public void onMemberJoin(GuildMemberJoinEvent event){
        if (event.getUser().isBot()) return;
        event.getUser().openPrivateChannel()
                .flatMap(ch->ch.sendMessage(embed("Welcome!", "Welcome to renegade 18. Please input your in-game name.\n" +
                        "If you are not a member you are not required to do this.")))
                .queue();
    }

    @SubscribeEvent
    public void onPrivateMessage(PrivateMessageReceivedEvent event){
        if (event.getAuthor().isBot()) return;
        String username = event.getMessage()
                .getContentRaw();
        User user = event.getAuthor();
        manager.linkAccount(user, user, username);
    }

    @SubscribeEvent
    public void onGuildMessageEvent(GuildMessageReceivedEvent event){
        if (event.getAuthor().isBot()) return;
        long channel = event.getChannel().getIdLong();
        Configuration conf = RenegadeTracker.INSTANCE.getConfig();
        if (channel == conf.getLinkingChannelId()){
            String username = event.getMessage()
                    .getContentRaw();
            User user = event.getAuthor();
            manager.linkAccount(user, user, username);
        } else if (channel == conf.getCommandChannelId()) {
            //run commands and shit
        }
    }
}
