package renegade.planetside2.tracker;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.data.OutfitPlayer;
import renegade.planetside2.storage.Configuration;
import renegade.planetside2.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OutfitMember extends WrappedMember {
    private OutfitPlayer player;
    public OutfitMember(Member member, OutfitPlayer player) {
        super(member);
        this.player = player;
    }

    public OutfitPlayer getPlayer(){
        return player;
    }

    public boolean isRenegade(){
        Configuration configuration = RenegadeTracker.INSTANCE.getConfig();
        return member.getRoles().contains(configuration.getRole(Rank.RENEGADE));
    }

    public boolean hasCamo(){
        List<Long> camo = RenegadeTracker.INSTANCE.getAcceptableCamos();
        return player.hasAny(camo);
    }

    public void assignRenegade(){
        Configuration configuration = RenegadeTracker.INSTANCE.getConfig();
        assignRenegade(configuration.getCommandChannel());
    }

    public void assignRenegade(TextChannel channel){
        Utility.setRoleIfAbsent(this, Rank.RENEGADE);
        String message = String.format("%s has been identified as a true renegade.\n" +
                "The Renegade role has been assigned.", player.getActualName());
        channel.sendMessage(Utility.embed("Renegade", message)).queue();
    }

    public boolean hasRank(Rank rank){
        Configuration configuration = RenegadeTracker.INSTANCE.getConfig();
        switch (rank) {
            case OFFICER:
            case SENIOR_LEADER:
            case JUNIOR_LEADER:
            case ROUTER_BUILDER:
            case OUTFIT_WARS: return player.getRank().equals(configuration.getRank(rank));
            case MEMBER: return !player.getRank().equals(configuration.getRank(Rank.PLEB));
            default: return false;
        }
    }

    public boolean shouldManage(Rank rank) {
        Configuration configuration = RenegadeTracker.INSTANCE.getConfig();
        String admin = configuration.getRank(Rank.OFFICER);
        String outfitWars = configuration.getRank(Rank.OUTFIT_WARS);
        String srLead = configuration.getRank(Rank.SENIOR_LEADER);
        String jrLead = configuration.getRank(Rank.JUNIOR_LEADER);
        switch (rank) {
            case SENIOR_LEADER:
                return  !player.getRank().equals(admin);
            case OUTFIT_WARS:
                return  !player.getRank().equals(admin) &&
                        !player.getRank().equals(srLead);
            case JUNIOR_LEADER:
                return  !player.getRank().equals(admin) &&
                        !player.getRank().equals(outfitWars);
            case ROUTER_BUILDER:
                return  !player.getRank().equals(admin) &&
                        !player.getRank().equals(outfitWars) &&
                        !player.getRank().equals(srLead) &&
                        !player.getRank().equals(jrLead);
            case RENEGADE: return false;
            default: return true;
        }
    }

    public void configureRanks() {
        try {
            Configuration configuration = RenegadeTracker.INSTANCE.getConfig();
            boolean shouldAssign = Arrays.stream(Rank.values())
                    .map(configuration::getRank)
                    .anyMatch(s -> s.equals(getPlayer().getRank()));
            if (!shouldAssign) return;
            List<Rank> possibleRanks = Arrays.stream(Rank.values())
                    .filter(configuration::shouldAssign)
                    .filter(this::shouldManage)
                    .collect(Collectors.toList());
            List<Role> toRemove = new ArrayList<>();
            List<Role> toAdd = new ArrayList<>();
            possibleRanks.forEach(rank -> {
                if (hasRank(rank)) {
                    toAdd.add(configuration.getRole(rank));
                } else {
                    toRemove.add(configuration.getRole(rank));
                }
            });
            if (!configuration.shouldAssignRoles()) return;
            getGuild().modifyMemberRoles(member, toAdd, toRemove)
                    .queue(s -> {}, ex -> {});
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
