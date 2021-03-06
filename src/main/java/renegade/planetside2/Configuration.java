package renegade.planetside2;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;

@ConfigSerializable
public class Configuration {
    @Setting(value = "Verified-Banana-Camo", comment = "Camouflage fitting for the banana people")
    private final ArrayList<String> bananaCamo = new ArrayList<>();

    @Setting(value = "Discord-API-Token", comment = "The discord bot token.")
    private String jdaToken = "";

    @Setting("Renegade-Discord")
    private long guild = 508534801339252744L;

    public String getJdaToken(){
        return jdaToken;
    }
}
