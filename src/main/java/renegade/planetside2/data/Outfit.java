package renegade.planetside2.data;

import renegade.planetside2.util.Utility;

import java.util.List;

public class Outfit {
    List<Member> members;

    public static Outfit getR18() {
        String query = PS2API.getOutfitQuery("Renegade%2018");
        OutfitListing outfits = Utility.getGsonFromUrl(query, OutfitListing.class);
        if (outfits == null) return null;
        return outfits.outfit_list.get(0);
    }

    public List<Member> getMembers() {
        return members;
    }

    public static class OutfitListing {
        List<Outfit> outfit_list;
    }
}
