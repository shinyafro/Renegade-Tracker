package renegade.planetside2.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import renegade.planetside2.exception.DiscordCommandException;

import java.util.List;

public interface DiscordCommandExecutor {
    void execute(TextChannel source, Member author, String command, List<String> args) throws DiscordCommandException;
}