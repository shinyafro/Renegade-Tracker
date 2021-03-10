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
import java.util.Optional;

public class Unlink implements DiscordCommandExecutor {
    @Override
    public void execute(TextChannel source, Member author, String command, List<String> args) throws DiscordCommandException {
        Database database = RenegadeTracker.INSTANCE.getDatabase();
        boolean discord = args.get(0).equalsIgnoreCase("d") || args.get(0).equalsIgnoreCase("discord");
        if (args.size() < 2){
            source.sendMessage(Utility.embed("Error", "Incorrect usage.")).queue();
        } else if (discord) {
            long id = Utility.parseUserId(args).orElseThrow(() -> new DiscordCommandException("Please specify a discord user!"));
            Optional<Database.VerifiedData> entry = database.getWithDiscordId(id);
            database.deleteRecordDiscord(id);
            if (entry.isPresent()) {
                source.sendMessage(Utility.embed("Success", "Deleted the entry from the database."))
                        .queue();
            } else {
                source.sendMessage(Utility.embed("Failure", "Could not find the user in the database."))
                        .queue();
            }
        } else {
            long id = args.get(1).matches("\\d+")? Long.parseLong(args.get(1)) : fetchPlayerId(args.get(1));
            if (id == -1) throw new DiscordCommandException("Failed to retrieve user ID from name.");
            Optional<Database.VerifiedData> entry = database.getWithPlanetsideId(id);
            database.deleteRecordPlanetside(id);
            if (entry.isPresent()) {
                source.sendMessage(Utility.embed("Success", "Deleted the entry from the database."))
                        .queue();
            } else {
                source.sendMessage(Utility.embed("Failure", "Could not find the user in the database."))
                        .queue();
            }
        }
    }

    public Long fetchPlayerId(String name){
        PlayerData data = PlayerData.getPlayerData(name);
        return data == null? -1 : data.getCharacterId();
    }
}
