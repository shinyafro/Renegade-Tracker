package renegade.planetside2.storage;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import renegade.planetside2.data.PS2API;

import java.util.ArrayList;
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

    public long getInGameRenegade(){
        return planetside.renegade;
    }

    public long getInGameJoinDiscord(){
        return planetside.joinDiscord;
    }

    @ConfigSerializable
    public static class Planetside {
        @Setting(value = "Verified-Banana-Camo", comment = "Camouflage fitting for the banana people")
        private final List<String> bananaCamo = Arrays.asList(
                "Shatter Camo",
                "Solid Yellow Camo",
                "Solid Metallic Yellow Camo"
        );

        @Setting("Join-Discord-Rank")
        private long joinDiscord;

        @Setting("Renegade-Rank")
        private long renegade;

    }

    @ConfigSerializable
    public static class Discord {

        @Setting(value = "Discord-API-Token", comment = "The discord bot token.")
        private String jdaToken = "";

        @Setting("Renegade-Discord")
        private long guild = 508534801339252744L;
    }

    @ConfigSerializable
    public static class Roles {
        @Setting("Exo-Role")
        private long exo = 0L;

        @Setting("Renegade-Role")
        private long baseBuilder = 0L;

        @Setting("Renegade-Role")
        private long renegade = 0L;

        @Setting("Member-Role")
        private long member = 0L;

        @Setting("Join-Discord-Role")
        private long joinDiscord = 0L;
    }
}
