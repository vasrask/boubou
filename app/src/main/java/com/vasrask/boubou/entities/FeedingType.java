package com.vasrask.boubou.entities;

import android.content.Context;

import com.vasrask.boubou.R;

import java.util.HashMap;
import java.util.Map;

public enum FeedingType {
    BREASTFEEDING(R.string.breastfeeding),
    PUMPED_BREAST_MILK(R.string.pumped_breast_milk),
    FORMULA(R.string.formula),
    NO_FEEDING(R.string.no_feeding);

    private final int id;

    FeedingType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    private static final Map<String, FeedingType> lookup = new HashMap<>();

    public static void init(Context context) {
        lookup.clear();
        for (FeedingType feedingType : values()) {
            lookup.put(context.getString(feedingType.id), feedingType);
        }
    }

    public static FeedingType fromString(String s) {
        return lookup.getOrDefault(s, null);
    }
}
