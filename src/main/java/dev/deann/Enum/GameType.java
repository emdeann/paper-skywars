package dev.deann.Enum;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public enum GameType {
    SKYWARS("skywars", new String[] {"sw"}),
    SURVIVAL_GAMES("survival_games", new String[] {"sg", "survivalgames"});

    private final String name;
    private final ArrayList<String> aliases;
    GameType(String s, String[] aliases) {
        name = s.toLowerCase();
        this.aliases = new ArrayList<>(List.of(aliases));
    }

    public String getTitleName() {
        return StringUtils.capitalize(name.replace('_', ' '));
    }

    public String getName() {
        return name;
    }

    public static GameType getTypeByString(String type) {
        for (GameType t : GameType.values()) {
            if (t.name.equals(type.toLowerCase()) || t.aliases.contains(type.toLowerCase())) {
                return t;
            }
        }
        return null;
    }
}
