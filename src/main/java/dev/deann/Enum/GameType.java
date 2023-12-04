package dev.deann.Enum;

import org.apache.commons.lang3.StringUtils;

public enum GameType {
    SKYWARS("skywars"),
    SURVIVAL_GAMES("survivalgames");

    private final String name;
    GameType(String s) {
        name = s.toLowerCase();
    }

    public String getTitleName() {
        return StringUtils.capitalize(name);
    }

    public String getName() {
        return name;
    }

    public static GameType getTypeByString(String type) {
        for (GameType t : GameType.values()) {
            if (t.name.equals(type.toLowerCase())) {
                return t;
            }
        }
        return null;
    }
}
