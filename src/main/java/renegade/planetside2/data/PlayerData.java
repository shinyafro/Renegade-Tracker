package renegade.planetside2.data;

import renegade.planetside2.util.Utility;

import java.util.List;

public class PlayerData {

    public static PlayerData getPlayerData(String name){
        return getPlayerData(name, 5);
    }

    private static PlayerData getPlayerData(String name, int attempts) {
        String query = PS2API.getNameQuery(name);
        for (int i = 0; i < attempts; i++) {
            try {
                PlayerDataList listing = Utility.getGsonFromUrl(query, PlayerDataList.class);
                if (listing == null || listing.character_list == null || listing.character_list.isEmpty()) {
                    System.out.printf("Unable to retrieve %s! Re-trying!\n", name);
                    continue;
                }
                return listing.character_list.get(0);
            } catch (Exception ignored) {

            }
        }
        System.out.printf("Unable to retrieve %s!\n", name);
        return null;
    }

    private long character_id;

    public long getCharacterId(){
        return character_id;
    }

    public static class PlayerDataList{
        List<PlayerData> character_list;
    }
}
