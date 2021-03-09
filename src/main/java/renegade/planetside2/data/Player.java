package renegade.planetside2.data;

import renegade.planetside2.util.Utility;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Player {

    long character_id;
    long member_since;
    String member_since_date;
    String rank;
    long rank_ordinal;
    Name name;

    public HashSet<Long> getItems() {
        String query = String.format("%s/characters_item/?character_id=%s", PS2API.API_BASE, character_id);
        ItemListing listing = Utility.getGsonFromUrl(query, ItemListing.class);
        if (listing == null || listing.characters_item_list == null) {
            System.out.printf("Unable to retrieve unlocks for %s! Re-trying!", getActualName());
            listing = Utility.getGsonFromUrl(query, ItemListing.class);
            if (listing == null || listing.characters_item_list == null) {
                System.out.printf("Unable to retrieve unlocks for %s! Skipping!", getActualName());
                return new HashSet<>();
            }
        }
        return listing.characters_item_list.stream()
                .map(UnlockedItem::getId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public boolean belowRank(long rank){
        return rank < rank_ordinal;
    }

    public String getActualName(){
        return name.first;
    }

    public String getLowerName(){
        return name.first_lower;
    }

    public long getCharacter_id(){
        return character_id;
    }

    public static class UnlockedItem {
        long character_id;
        long item_id;

        public long getId(){
            return item_id;
        }
    }

    public static class ItemListing {
        List<UnlockedItem> characters_item_list;
    }
}
