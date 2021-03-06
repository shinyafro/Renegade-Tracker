package renegade.planetside2.data;

import renegade.planetside2.Utility;

import java.util.HashMap;
import java.util.List;

public class Item {

    public static Item getItem(String itemName) {
        String query = String.format("%s/item/?name.en=%s", PS2API.API_BASE, itemName);
        Item_Listing listing = Utility.getGsonFromUrl(query, Item_Listing.class);
        if (listing == null || listing.item_list.isEmpty()) {
            System.out.printf("ERROR: \"%s\" was unable to be parsed into an ID. Is it spelled correctly?", itemName);
            return null;
        }
        else return listing.item_list.get(0);
    }

    HashMap<String, String> name;
    long item_id;

    public String getEnglishName(){
        return name.get("en");
    }
    public static class Item_Listing {
        List<Item> item_list;
    }
}
