package renegade.planetside2.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import renegade.planetside2.exception.DiscordCommandException;

import java.util.*;

public class DiscordCommandTree implements DiscordCommandExecutor {
    protected final HashMap<String, DiscordCommand> commandMap = new HashMap<>();
    protected final HashSet<String> defaults = new HashSet<>(Arrays.asList("help", "?"));

    public void register(DiscordCommand command, String... alias){
        for (String name : alias) {
            commandMap.put(name.toLowerCase(), command);
        }
    }

    @Override
    public void execute(TextChannel source, Member member, String command, List<String> args) throws DiscordCommandException {
        if (args.size() == 0 || defaults.contains(args.get(0))) {
            defaultResponse(source, member, command, args);
            return;
        }

        final String base = args.get(0);
        final DiscordCommand discordCommand = commandMap.get(base.toLowerCase());
        if (discordCommand != null) {
            args.remove(0);
            discordCommand.process(source, member, command, args);
        } else defaultResponse(source, member, command, args);
    }

    public Map<String, DiscordCommand> getCommandMap(){
        Map<String, DiscordCommand> result = new HashMap<>();
        commandMap.forEach((alias, cmd) -> {
            if (result.containsValue(cmd)) return;
            result.put(alias, cmd);
        });
        return result;
    }

    public void defaultResponse(TextChannel source, Member member, String command, List<String> args) throws DiscordCommandException{
        throw new DiscordCommandException("Command not found");
    }
}