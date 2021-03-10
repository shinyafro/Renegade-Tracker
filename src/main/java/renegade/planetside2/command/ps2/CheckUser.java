package renegade.planetside2.command.ps2;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.command.DiscordCommandExecutor;
import renegade.planetside2.exception.DiscordCommandException;
import renegade.planetside2.exception.UserNotLinkedException;
import renegade.planetside2.tracker.OutfitMember;
import renegade.planetside2.tracker.UserManager;
import renegade.planetside2.util.Utility;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CheckUser implements DiscordCommandExecutor {
    @Override
    public void execute(TextChannel source, Member author, String command, List<String> args) throws DiscordCommandException {
        Optional<CompletableFuture<Member>> optTarget = Utility.parseMember(args);
        if (optTarget.isPresent()){
            optTarget.get().thenAccept(m->withMemberTarget(source, author, m, command, args));
        } else {
            throw new DiscordCommandException("Failed to fetch user.");
        }
    }

    public void withMemberTarget(TextChannel source, Member author, Member target, String command, List<String> args){
        UserManager manager = RenegadeTracker.INSTANCE.getManager();
        try {
            OutfitMember member = manager.getMember(target);
            if (member.isRenegade()) {
                source.sendMessage(Utility.embed("Error", member.getPlayer().getActualName() + " is already a renegade.")).queue();
            } else if (member.hasCamo()) {
                member.assignRenegade(source);
            } else {
                source.sendMessage(Utility.embed("False", member.getPlayer().getActualName() + " is not a true renegade.")).queue();
            }
        } catch (UserNotLinkedException e){
            source.sendMessage("The user specified was not linked.").submit()
                    .thenApply(Message::delete)
                    .thenCompose(ra->ra.submitAfter(30, TimeUnit.SECONDS));
        }
    }
}
