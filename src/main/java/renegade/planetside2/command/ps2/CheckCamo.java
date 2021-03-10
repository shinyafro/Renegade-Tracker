package renegade.planetside2.command.ps2;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.command.DiscordCommandExecutor;
import renegade.planetside2.exception.DiscordCommandException;

import java.util.List;

public class CheckCamo implements DiscordCommandExecutor {
    @Override
    public void execute(TextChannel source, Member author, String command, List<String> args) throws DiscordCommandException {
        RenegadeTracker.INSTANCE.calculateRanks(true);
    }
}
