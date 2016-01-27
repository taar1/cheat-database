package com.cheatdatabase.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class CheatContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<CheatItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<Integer, CheatItem> ITEM_MAP = new HashMap<>();

    static {
        addItem(new CheatItem(9876, "3ds Xl And Wii U Easter Eggs", 10));
        addItem(new CheatItem(12345, "Catching The Legendary Bird Pokemon", 8));
        addItem(new CheatItem(4424, "Earn Easy Money", 5));
        addItem(new CheatItem(843, "Gym Battle Preparation", 9));
    }

    private static void addItem(CheatItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.cheatId, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class CheatItem {
        public int cheatId;
        public String cheatTitle;
        public int rating;

        public CheatItem(int cheatId, String cheatTitle, int rating) {
            this.cheatId = cheatId;
            this.cheatTitle = cheatTitle;
            this.rating = rating;
        }

        @Override
        public String toString() {
            return cheatTitle;
        }

        public int getCheatId() {
            return cheatId;
        }

        public void setCheatId(int cheatId) {
            this.cheatId = cheatId;
        }

        public String getCheatTitle() {
            return cheatTitle;
        }

        public void setCheatTitle(String cheatTitle) {
            this.cheatTitle = cheatTitle;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }
    }
}
