package renegade.planetside2.data;

import com.google.common.reflect.TypeToken;
import renegade.planetside2.Pair;
import renegade.planetside2.Utility;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PS2API {
    @SuppressWarnings("UnstableApiUsage")
    public static Outfit getOutfit(){
        String query = "http://census.daybreakgames.com/s:RenegadeTracker/get/ps2/outfit/?name=^Renegade%2018&c:resolve=member_character(name,type.faction)";
        OutfitListing outfits = Utility.getGsonFromUrl(query, OutfitListing.class);
        return outfits.outfit_list.get(0);
    }

    public static Map<String, Member> getMembers(){
        Outfit outfit = getOutfit();
        if (outfit == null) return null;
        return outfit.members.stream()
                .map(m->new Pair<>(m.name.first_lower, m))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
}
