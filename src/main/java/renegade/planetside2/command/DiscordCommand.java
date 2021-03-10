package renegade.planetside2.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.exception.DiscordPermissionException;
import renegade.planetside2.storage.Configuration;
import renegade.planetside2.tracker.Rank;
import renegade.planetside2.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscordCommand {
    private List<Rank> required;
    private final String description;
    private final DiscordCommandExecutor executor;
    private final String commandUsage;

    private DiscordCommand(List<Rank> required, DiscordCommandExecutor executor, String commandUsage, String description){
        this.required = required;
        this.executor = executor;
        this.commandUsage = commandUsage;
        this.description = description;
    }

    public boolean hasPermission(Member member){
        Configuration configuration = RenegadeTracker.INSTANCE.getConfig();
        List<Role> userRoles = member.getRoles();
        return required.stream()
                .map(configuration::getRole)
                .allMatch(userRoles::contains);
    }

    public static Builder builder()         {
        return new Builder();
    }

    public final void process(TextChannel source, Member author, String command, List<String> args) {
        try {
            executor.execute(source, author, command, args);
        } catch (DiscordPermissionException e) {
            Utility.sendPermissionError(source, author);
        } catch (Exception e) {
            Utility.sendCommandError(source, author, e.getMessage() != null ? e.getMessage() : "an error occurred while executing the command.");
        }
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return commandUsage;
    }

    public static class Builder{
        private DiscordCommandExecutor executor;
        private String commandUsage;
        private String description;
        private List<Rank> required;

        private Builder(){}

        public final Builder setRequiredRanks(Rank... ranks){
            this.required = Arrays.asList(ranks);
            return this;
        }

        public final Builder setCommandExecutor(DiscordCommandExecutor executor){
            this.executor = executor;
            return this;
        }

        public final Builder setDescription(String description){
            this.description = description;
            return this;
        }

        public final Builder setCommandUsage(String commandUsage){
            this.commandUsage = commandUsage;
            return this;
        }

        public DiscordCommand build(){
            return new DiscordCommand(
                    required     != null ? required     : new ArrayList<>(),
                    executor     != null ? executor     : (src, member, command, event)->{},
                    commandUsage != null ? commandUsage : "",
                    description  != null ? description  : ""
            );
        }

    }
}