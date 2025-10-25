package com.vasrask.boubou.entities;

import java.util.HashMap;
import java.util.Map;

public enum Language {
    ENGLISH(0, "en", "English"),
    GREEK(1, "el", "Ελληνικά");

    private final String code;
    private final String name;
    private final int id;

    Language(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public int getId() {
        return id;
    }

    private static final Map<Integer, Language> lookupById = new HashMap<>();
    private static final Map<String, Language> lookupByCode = new HashMap<>();
    private static final Map<String, Language> lookupByName = new HashMap<>();

    public static void init() {
        lookupByCode.clear();
        lookupByName.clear();
        for (Language lang : values()) {
            lookupById.put(lang.id, lang);
            lookupByCode.put(lang.code, lang);
            lookupByName.put(lang.name, lang);
        }
    }

    public static Language fromId(int id) {
        return lookupById.getOrDefault(id, ENGLISH);
    }

    public static Language fromCode(String code) {
        return lookupByCode.getOrDefault(code, ENGLISH);
    }

    public static Language fromName(String name) {
        return lookupByName.getOrDefault(name, ENGLISH);
    }

    public static String[] getLanguageList() {
        Language[] values = values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].getName();
        }
        return names;
    }
}

