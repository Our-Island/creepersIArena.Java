package top.ourisland.creepersiarena.core.command.model;

import java.util.Arrays;
import java.util.Locale;

/**
 * Target configuration file for /ciaa config.
 */
public enum ConfigTarget {

    CONFIG("config"),
    ARENA("arena"),
    SKILL("skill");

    private final String id;

    ConfigTarget(String id) {
        this.id = id;
    }

    public static ConfigTarget parse(String raw) {
        if (raw == null) return null;
        var normalized = raw.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(target -> target.id.equals(normalized))
                .findFirst()
                .orElse(null);
    }

    public String id() {
        return id;
    }

    public String fileName() {
        return id + ".yml";
    }

}
