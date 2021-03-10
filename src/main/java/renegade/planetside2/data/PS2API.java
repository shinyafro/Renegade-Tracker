package renegade.planetside2.data;

import java.util.Locale;

public class PS2API {
    public static final String API_BASE = "http://census.daybreakgames.com/s:RenegadeTracker/get/ps2";
    public static String getOutfitQuery(String name){
        return String.format("%s/outfit/?name=^%s&c:resolve=member_character(name,type.faction)", API_BASE, name);
    }

    public static long getItemId(String englishName){
        Item item = Item.getItem(englishName);
        return item == null? 0 : item.item_id;
    }

    public static String getNameQuery(String name){
        name = name.toLowerCase(Locale.ENGLISH);
        return String.format("%s/character/?name.first_lower=%s", API_BASE, name);
    }
}
