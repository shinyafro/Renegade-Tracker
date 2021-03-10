package renegade.planetside2.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.command.DiscordCommandManager;
import renegade.planetside2.storage.Configuration;
import renegade.planetside2.tracker.UserManager;
import renegade.planetside2.util.Utility;

import static renegade.planetside2.util.Utility.embed;

public class DiscordEvents {
    final UserManager manager;
    final DiscordCommandManager commandBase = new DiscordCommandManager();

    public DiscordEvents(UserManager manager){
        this.manager = manager;
    }
    @SubscribeEvent
    public void onMemberJoin(GuildMemberJoinEvent event){
        Configuration cfg = RenegadeTracker.INSTANCE.getConfig();
        if (event.getUser().isBot() || !cfg.sendJoinMessage()) return;
        MessageEmbed embed = Utility.embed()
                .addField("Welcome", "Welcome to the official Renegade-18 discord server.\n" +
                        "To access the rest of the server, please click <#695926293426077717>\n" +
                        "for further instructions.", false)
                .addField("If you are a member", "Please also type your in-game name below.", false)
                .addField("If you need any help", "Please contact any of the following admins:\n" +
                        "<@248056002274918400>, <@178370119213252608>, <@272173309850943488>", false)
                .build();
        event.getUser().openPrivateChannel()
                .flatMap(ch->ch.sendMessage(embed))
                .queue();
    }

    @SubscribeEvent
    public void onMemberLeave(GuildMemberRemoveEvent event){

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
            final String pre = conf.getCommandPrefix();
            final String message = event.getMessage().getContentRaw();
            final String args;
            if (message.startsWith(pre)) args = message.substring(pre.length());
            else return;
            event.getGuild().retrieveMember(event.getAuthor()).submit()
                    .thenAccept(m->commandBase.process(event.getChannel(), m, args));
        }
    }
}
