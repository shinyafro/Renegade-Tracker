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

import java.util.*;
import java.util.stream.Collectors;

@ConfigSerializable @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class Configuration {

    @Setting("Discord-Settings")
    private Discord discord = new Discord();
    @Setting("Planetside-Settings")
    private Planetside planetside = new Planetside();

    @Setting("Outfit-Ranks")
    private InGameRanks inGameRanks = new InGameRanks();
    @Setting("Discord-Roles")
    private DiscordRoles discordRoles = new DiscordRoles();

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
            case OFFICER: return inGameRanks.admin;
            case SENIOR_LEADER: return inGameRanks.srLeader;
            case JUNIOR_LEADER: return inGameRanks.jrLeader;
            case OUTFIT_WARS: return inGameRanks.outfitWars;
            case ROUTER_BUILDER: return inGameRanks.routerOps;
            case RENEGADE: return inGameRanks.renegade;
            case MEMBER: return inGameRanks.member;
            case PLEB: return inGameRanks.joinDiscord;
            default: throw new AssertionError();
        }
    }

    public Role getRole(Rank rank) {
        JDA jda = RenegadeTracker.INSTANCE.getJda();
        switch (rank) {
            case OFFICER: return jda.getRoleById(discordRoles.admin);
            case SENIOR_LEADER: return jda.getRoleById(discordRoles.srLeader);
            case JUNIOR_LEADER: return jda.getRoleById(discordRoles.jrLeader);
            case OUTFIT_WARS: return jda.getRoleById(discordRoles.outfitWars);
            case ROUTER_BUILDER: return jda.getRoleById(discordRoles.routerOps);
            case RENEGADE: return jda.getRoleById(discordRoles.renegade);
            case MEMBER: return jda.getRoleById(discordRoles.member);
            case PLEB: return jda.getRoleById(discordRoles.joinDiscord);
            default: return null;
        }
    }

    public Set<String> getCheckForRenegade() {
        if (planetside.checkForCamoHashSet == null){
            planetside.checkForCamoHashSet = new HashSet<>(planetside.checkForCamo);
        }
        return planetside.checkForCamoHashSet;
    }

    @ConfigSerializable
    public static class Planetside {
        @Setting(value = "Verified-Banana-Camo", comment = "Camouflage fitting for the banana people")
        private final List<String> bananaCamo = Arrays.asList(
                "Shatter Camo",
                "Solid Yellow Camo",
                "Solid Metallic Yellow Camo",
                "Loyal Hearts Camo"
        );

        @Setting(value = "Check-For-Renegade", comment = "These ranks will be checked for renegade-worthy camo.")
        private List<String> checkForCamo = Arrays.asList(
                "R18 Member",
                "Join Discord"
        );

        private Set<String> checkForCamoHashSet;
    }

    @ConfigSerializable
    public static class InGameRanks{
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
    public static class DiscordRoles {
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

    @ConfigSerializable
    public static class Discord {

        @Setting(value = "Discord-API-Token", comment = "The discord bot token.")
        private String jdaToken = "";

        @Setting(value = "Renegade-Discord", comment = "The discord server ID")
        private long guild = 508534801339252744L;

        @Setting(value = "Linking-Channel", comment = "The channel for people to input their username into")
        private long linkChannel = 0L;

        @Setting(value = "Command-Channel", comment = "The channel for command input / output\nScheduled check output will appear here too.")
        private long botChannel = 0L;
    }
}
