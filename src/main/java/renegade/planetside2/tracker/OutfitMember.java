package renegade.planetside2.tracker;

import net.dv8tion.jda.api.entities.Member;
import renegade.planetside2.data.Player;

public class OutfitMember extends WrappedMember {
    private Player player;
    public OutfitMember(Member member, Player player) {
        super(member);
        this.player = player;
    }

    public Player getPlayer(){
        return player;
    }
}
