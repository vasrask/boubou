package com.vasrask.boubou.entities;

import android.content.Context;

import com.vasrask.boubou.R;

import java.util.HashMap;
import java.util.Map;

public enum BabyActivityType {
    SLEEP(R.string.sleep),
    FEEDING(R.string.feeding),
    DIAPER_CHANGE(R.string.diaper_change),
    MEDICINE(R.string.medicine),
    PLAYTIME(R.string.playtime),
    OTHER(R.string.other);

    private final int id;

    BabyActivityType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    private static final Map<String, BabyActivityType> lookup = new HashMap<>();

    public static void init(Context context) {
        lookup.clear();
        for (BabyActivityType babyActivityType : values()) {
            lookup.put(context.getString(babyActivityType.id), babyActivityType);
        }
    }

    public static BabyActivityType fromString(String s) {
        return lookup.getOrDefault(s, null);
    }

}
