package renegade.planetside2.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.exception.DiscordCommandException;
import renegade.planetside2.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class DiscordCommandManager extends DiscordCommandTree {

    private final HashSet<String> defaultAliases = new HashSet<>(Arrays.asList("", "help"));

    public DiscordCommandManager() {

    }

    public void process(TextChannel source, Member author, String args){
        try {
            String[] command = args == null || defaultAliases.contains(args)? new String[0] : args.split(" ");
            execute(source, author, null, new ArrayList<>(Arrays.asList(command)));
        } catch (Exception e){
            String message = e.getMessage() != null? e.getMessage() : "an error occurred while executing the command.";
            Utility.sendCommandError(source, author, message);
        }
    }

    @Override
    public void defaultResponse(TextChannel source, Member author, String command, java.util.List<String> args) throws DiscordCommandException {
        EmbedBuilder embed = Utility.embed();
        String pre = RenegadeTracker.INSTANCE.getConfig().getCommandPrefix();
        getCommandMap().forEach((alias, cmd)->{
            if (!cmd.hasPermission(author)) return;
            String title = pre + alias + " " + cmd.getUsage();
            embed.addField(title, cmd.getDescription(), false);
            embed.setFooter("Requested By: " + author.getUser().getAsTag(), author.getUser().getAvatarUrl());
        });
        source.sendMessage(embed.build()).queue();
    }
}