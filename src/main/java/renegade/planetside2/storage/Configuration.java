package renegade.planetside2.storage;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.data.PS2API;
import renegade.planetside2.tracker.Rank;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ConfigSerializable @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class Configuration {

    @Setting private Discord discord = new Discord();
    @Setting private Planetside planetside = new Planetside();

    public List<Long> getBananaIds(){
        return planetside.bananaCamo.stream()
                .map(s->s.replace(" ", "%20"))
                .map(PS2API::getItemId)
                .collect(Collectors.toList());
    }

    public String getJdaToken(){
        return discord.jdaToken;
    }

    public Guild getGuild(JDA jda){
        return jda.getGuildById(discord.guild);
    }

    public long getLinkingChannelId(){
        return discord.linkChannel;
    }

    public long getCommandChannelId(){
        return discord.botChannel;
    }

    public TextChannel getCommandChannel(){
        return RenegadeTracker.INSTANCE.getJda()
                .getTextChannelById(discord.botChannel);
    }

    public String getRank(Rank rank){
        switch (rank){
            case OFFICER: return planetside.admin;
            case SENIOR_LEADER: return planetside.srLeader;
            case JUNIOR_LEADER: return planetside.jrLeader;
            case OUTFIT_WARS: return planetside.outfitWars;
            case ROUTER_BUILDER: return planetside.routerOps;
            case RENEGADE: return planetside.renegade;
            case MEMBER: return planetside.member;
            case PLEB: return planetside.joinDiscord;
            default: throw new AssertionError();
        }
    }

    public Role getRole(Rank rank) {
        JDA jda = RenegadeTracker.INSTANCE.getJda();
        switch (rank) {
            case OFFICER: return jda.getRoleById(discord.admin);
            case SENIOR_LEADER: return jda.getRoleById(discord.srLeader);
            case JUNIOR_LEADER: return jda.getRoleById(discord.jrLeader);
            case OUTFIT_WARS: return jda.getRoleById(discord.outfitWars);
            case ROUTER_BUILDER: return jda.getRoleById(discord.routerOps);
            case RENEGADE: return jda.getRoleById(discord.renegade);
            case MEMBER: return jda.getRoleById(discord.member);
            case PLEB: return jda.getRoleById(discord.joinDiscord);
            default: throw new AssertionError();
        }
    }

    @ConfigSerializable
    public static class Planetside {
        @Setting(value = "Verified-Banana-Camo", comment = "Camouflage fitting for the banana people")
        private final List<String> bananaCamo = Arrays.asList(
                "Shatter Camo",
                "Solid Yellow Camo",
                "Solid Metallic Yellow Camo"
        );

        @Setting("Admin-Rank")
        private String admin = "R18 CEO Owner";

        @Setting("Senior-Leader-Rank")
        private String srLeader = "R18 Bastion Officer";

        @Setting("Junior-Leader-Rank")
        private String jrLeader = "R18 Jr Officer";

        @Setting("Outfit-Wars-Rank")
        private String outfitWars = "R18 Vanguards";

        @Setting("Router-Ops-Rank")
        private String routerOps = "R18 Router Builder";

        @Setting("Renegade-Rank")
        private String renegade = "R18 Renegade";

        @Setting("Member-Rank")
        private String member = "R18 Member";

        @Setting("Join-Discord-Rank")
        private String joinDiscord = "Join Discord";

    }

    @ConfigSerializable
    public static class Discord {

        @Setting(value = "Discord-API-Token", comment = "The discord bot token.")
        private String jdaToken = "";

        @Setting("Renegade-Discord")
        private long guild = 508534801339252744L;

        @Setting("Linking-Channel")
        private long linkChannel = 0L;

        @Setting("Command-Channel")
        private long botChannel = 0L;

        @Setting("Admin-Role")
        private long admin = 695922688731906048L;

        @Setting("Senior-Leader-Role")
        private long srLeader= 695922691307208746L;

        @Setting("Junior-Leader-Role")
        private long jrLeader = 714259997961486369L;

        @Setting("Outfit-Wars-Role")
        private long outfitWars = 817667884355420180L;

        @Setting("Router-Ops-Role")
        private long routerOps = 695922691521249352L;

        @Setting("Renegade-Role")
        private long renegade = 730753718131097610L;

        @Setting("Member-Role")
        private long member = 622397700872077322L;

        @Setting("Join-Discord-Role")
        private long joinDiscord = 702303877172559923L;
    }
}
