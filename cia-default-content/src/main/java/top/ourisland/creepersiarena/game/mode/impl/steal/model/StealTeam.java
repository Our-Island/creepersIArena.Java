package top.ourisland.creepersiarena.game.mode.impl.steal.model;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Locale;

public enum StealTeam {

    RED("red", 1, NamedTextColor.RED),
    BLUE("blue", 2, NamedTextColor.BLUE);

    private final String key;
    private final int numericId;
    private final NamedTextColor color;

    StealTeam(String key, int numericId, NamedTextColor color) {
        this.key = key;
        this.numericId = numericId;
        this.color = color;
    }

    public static StealTeam fromNumericId(Integer id) {
        if (id == null) return null;
        return switch (id) {
            case 1 -> RED;
            case 2 -> BLUE;
            default -> null;
        };
    }

    public static StealTeam fromKey(String key) {
        if (key == null || key.isBlank()) return null;
        return switch (key.trim().toLowerCase(Locale.ROOT)) {
            case "red", "r", "1" -> RED;
            case "blue", "b", "2" -> BLUE;
            default -> null;
        };
    }

    public String key() {
        return key;
    }

    public int numericId() {
        return numericId;
    }

    public NamedTextColor color() {
        return color;
    }

    public String displayNameZh() {
        return this == RED ? "红队" : "蓝队";
    }

    public StealTeam opposite() {
        return this == RED ? BLUE : RED;
    }

}
