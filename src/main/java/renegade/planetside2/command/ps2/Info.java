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

public class Info implements DiscordCommandExecutor {
    @Override
    public void execute(TextChannel source, Member author, String command, List<String> args) throws DiscordCommandException {
        Database database = RenegadeTracker.INSTANCE.getDatabase();
        boolean discord = args.get(0).equalsIgnoreCase("d") || args.get(0).equalsIgnoreCase("discord");
        if (args.size() < 2){
            source.sendMessage(Utility.embed("Error", "Incorrect usage.")).queue();
        } else if (discord) {
            long id = Utility.parseUserId(args).orElseThrow(() -> new DiscordCommandException("Please specify a discord user!"));
            Database.VerifiedData entry = database.getWithDiscordId(id).orElseThrow(()-> new DiscordCommandException("Could not find user in database."));
            String name = PlayerData.getPlayerData(entry.ps2).getNameActual();
            String response = String.format("<@%s> was linked to %s at %s", entry.discord, name, entry.verified.toString());
            source.sendMessage(Utility.embed("Info", response)).queue();
        } else {
            long id = args.get(1).matches("\\d+") ? Long.parseLong(args.get(1)) : fetchPlayerId(args.get(1));
            if (id == -1) throw new DiscordCommandException("Failed to retrieve user ID from name.");
            Database.VerifiedData entry = database.getWithPlanetsideId(id).orElseThrow(()-> new DiscordCommandException("Could not find user in database."));
            String name = PlayerData.getPlayerData(entry.ps2).getNameActual();
            String response = String.format("<@%s> was linked to %s at %s", entry.discord, name, entry.verified.toString());
            source.sendMessage(Utility.embed("Info", response)).queue();
        }
    }

    public Long fetchPlayerId(String name) {
        PlayerData data = PlayerData.getPlayerData(name);
        return data == null ? -1 : data.getCharacterId();
    }
}