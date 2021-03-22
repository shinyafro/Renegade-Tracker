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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static renegade.planetside2.util.Utility.HOUR;
import static renegade.planetside2.util.Utility.MINUTE;

@ConfigSerializable @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class Configuration {

    @Setting("Discord-Settings")
    private Discord discord = new Discord();
    @Setting("Planetside-Settings")
    private Planetside planetside = new Planetside();

    @Setting("Outfit-Ranks")
    private InGameRanks inGameRanks = new InGameRanks();
    @Setting(value = "Discord-Roles", comment = "Roles can be set to -1 in order to not be assigned or removed.\n" +
            "Note that this also means they cannot be used for command-perms.\n" +
            "The admin role in particular is used for administrative commands.")
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

    public Role getGuestRole(){
        JDA jda = RenegadeTracker.INSTANCE.getJda();
        return jda.getRoleById(discordRoles.guest);
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

    public boolean shouldAssign(Rank rank){
        switch (rank) {
            case OFFICER: return discord.assignAdmin;
            case OUTFIT_WARS: return discord.assignWars;
            case PLEB: return discord.assignWaiting;
            default: return true;
        }
    }

    public Set<String> getCheckForRenegade() {
        if (planetside.checkForCamoHashSet == null){
            planetside.checkForCamoHashSet = new HashSet<>(planetside.checkForCamo);
        }
        return planetside.checkForCamoHashSet;
    }

    public boolean shouldBeInDiscord(String rank) {
        if (planetside.notInDiscordHashSet == null){
            planetside.notInDiscordHashSet = new HashSet<>(planetside.notInDiscord);
        }
        return planetside.notInDiscordHashSet.contains(rank);
    }

    public long getScheduleInterval(){
        if (planetside.interval < 0) return -1;
        long val = (long) (planetside.interval * HOUR);
        return Math.max(val, MINUTE * 15);
    }

    public boolean sendJoinMessage() {
        return discord.sendJoinMessage;
    }

    public boolean shouldAssignRoles() {
        return discord.assignRoles;
    }

    public boolean shouldStripLeaving(){
        return discord.stripLeaving;
    }

    public boolean shouldAssignMember() {
        return discord.assignMember;
    }

    public String getCommandPrefix() {
        return discord.commandPrefix;
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

        @Setting(value = "Not-In-Discord", comment = "Ranks for members not in discord.")
        private List<String> notInDiscord = Arrays.asList(
                "Join Discord Shatter",
                "Join Discord"
        );

        @Setting(value = "Scheduler-Interval", comment = "How long (In hours) the bot will check verified users ranks,\n" +
                "and for renegade camo, etc. Default: 2.0 - Minimum of 15 minutes.\n" +
                "any values below 0 will result in the tasks not being ran.\n" +
                "NOTE: If it is disabled, you will need to restart the bot to re-enable it.")
        private double interval = 2.0;

        private Set<String> checkForCamoHashSet;

        private Set<String> notInDiscordHashSet;
    }

    @ConfigSerializable
    public static class InGameRanks{
        @Setting("Admin-Rank")
        private String admin = "R18 CEO Owner";

        @Setting("Senior-Leader-Rank")
        private String srLeader = "R18 Bastion Officer";

        @Setting("Outfit-Wars-Rank")
        private String outfitWars = "R18 Vanguards";

        @Setting("Junior-Leader-Rank")
        private String jrLeader = "R18 Jr Leader";

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

        @Setting("Outfit-Wars-Role")
        private long outfitWars = 817667884355420180L;

        @Setting("Junior-Leader-Role")
        private long jrLeader = 714259997961486369L;

        @Setting("Router-Ops-Role")
        private long routerOps = 695922691521249352L;

        @Setting("Renegade-Role")
        private long renegade = 730753718131097610L;

        @Setting("Member-Role")
        private long member = 622397700872077322L;

        @Setting("Join-Discord-Role")
        private long joinDiscord = 702303877172559923L;

        @Setting("Guest-Role")
        private long guest = 695992100155818104L;
    }

    @ConfigSerializable
    public static class Discord {
        @Setting(value = "Command-Prefix", comment = "The prefix to use bot commands")
        private String commandPrefix = "/";

        @Setting(value = "Discord-API-Token", comment = "The discord bot token.")
        private String jdaToken = "";

        @Setting(value = "Renegade-Discord", comment = "The discord server ID")
        private long guild = 508534801339252744L;

        @Setting(value = "Linking-Channel", comment = "The channel for people to input their username into")
        private long linkChannel = 819180331814486048L;

        @Setting(value = "Command-Channel", comment = "The channel for command input / output\nScheduled check output will appear here too.")
        private long botChannel = 819180387422699571L;

        @Setting(value = "Join-Message", comment = "Send a message when someone joins the discord?")
        private boolean sendJoinMessage = true;

        @Setting(value = "Manage-Admin", comment = "Should the bot manage the admin role? (Assign/Remove based on in-game data)")
        private boolean assignAdmin = true;

        @Setting(value = "Manage-Outfit-Wars", comment = "Should the bot manage the outfit-wars role? (Assign/Remove based on in-game data)")
        private boolean assignWars = true;

        @Setting(value = "Assign-Member", comment =
                "Assign member rank automatically when an account has been linked.\n" +
                "Note: This will not remove the waiting verification role.")
        private boolean assignMember = true;

        @Setting(value = "Assign-Waiting-Verification", comment =
                "Assign/Remove role for awaiting verification.")
        private boolean assignWaiting = false;

        @Setting(value = "Assign-Roles", comment = "Should the bot try to assign roles?")
        private boolean assignRoles = true;

        @Setting(value = "Remove-Leaving-Roles", comment = "Remove roles from leaving members.")
        private boolean stripLeaving = true;
    }
}
