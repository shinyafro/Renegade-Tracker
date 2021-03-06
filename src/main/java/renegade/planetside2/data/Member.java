package renegade.planetside2.data;

import renegade.planetside2.Utility;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Member {

    long character_id;
    long member_since;
    String member_since_date;
    String rank;
    long rank_ordinal;
    Name name;

    public HashSet<Long> getItems() {
        String query = String.format("%s/characters_item/?character_id=%s", PS2API.API_BASE, character_id);
        ItemListing listing = Utility.getGsonFromUrl(query, ItemListing.class);
        if (listing == null) return new HashSet<>();
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
