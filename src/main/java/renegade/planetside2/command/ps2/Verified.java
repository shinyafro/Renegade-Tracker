package renegade.planetside2.command.ps2;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.command.DiscordCommandExecutor;
import renegade.planetside2.data.PlayerData;
import renegade.planetside2.exception.DiscordCommandException;
import renegade.planetside2.storage.Database;
import renegade.planetside2.util.Utility;

import java.util.List;

public class Verified implements DiscordCommandExecutor {
    @Override
    public void execute(TextChannel source, Member author, String command, List<String> args) throws DiscordCommandException {
        List<Database.VerifiedData> verifiedUsers = RenegadeTracker.INSTANCE.getDatabase().getEntries();
        StringBuilder builder = new StringBuilder();
        int page = 1;
        for (Database.VerifiedData data : verifiedUsers) {
            PlayerData p = PlayerData.getPlayerData(data.ps2);
            String name = p == null? "Unknown" : p.getNameActual();
            String response = String.format("<@%s> was linked to %s at %s", data.discord, name, data.verified.toString());
            if (builder.length() + response.length() > 1900) {
                String title = String.format("Verified Users (Pg.%d)", page++);
                source.sendMessage(Utility.embed(title, builder.toString())).queue();
                builder = new StringBuilder();
            } else if (builder.length() != 0){
                builder.append("\n");
            }
            builder.append(response);
        }
        if (builder.length() > 1){
            String title = String.format("Verified Users (Pg.%d)", page);
            source.sendMessage(Utility.embed(title, builder.toString())).queue();
        }
    }
}
