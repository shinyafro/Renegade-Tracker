package renegade.planetside2.command.ps2;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.command.DiscordCommandExecutor;
import renegade.planetside2.exception.DiscordCommandException;
import renegade.planetside2.util.Utility;

import java.util.List;

public class CheckCamo implements DiscordCommandExecutor {
    @Override
    public void execute(TextChannel source, Member author, String command, List<String> args) throws DiscordCommandException {
        if (args.size() < 1){
            source.sendMessage(Utility.embed("Error", "Incorrect usage.")).queue();
        }
        RenegadeTracker.INSTANCE.calculateRanks(true);
    }
}
