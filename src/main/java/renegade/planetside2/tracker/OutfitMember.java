package renegade.planetside2.tracker;

import net.dv8tion.jda.api.entities.Member;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.data.Player;
import renegade.planetside2.storage.Configuration;

public class OutfitMember extends WrappedMember {
    private Player player;
    public OutfitMember(Member member, Player player) {
        super(member);
        this.player = player;
    }

    public Player getPlayer(){
        return player;
    }

    public boolean isRenegade(){
        Configuration configuration = RenegadeTracker.INSTANCE.getConfig();
        return member.getRoles().contains(configuration.getRole(Rank.RENEGADE));
    }

    public void assignRenegade(){

    }

    public void configureRanks(Player p) {

    }
}
