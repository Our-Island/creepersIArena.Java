package top.ourisland.creepersiarena.job;

import java.util.Locale;

public enum JobId {
    CREEPER("creeper"),
    MOISON("moison"),
    AVENGER("avenger"),
    BLOODLINE("bloodline"),
    GOLEM("golem"),
    WOLONG("wolong"),
    YSAHAN("ysahan");

    private final String id;

    JobId(String id) {
        this.id = id;
    }

    public static JobId fromId(String id) {
        if (id == null) return null;
        String needle = id.trim().toLowerCase(Locale.ROOT);
        for (JobId j : values()) {
            if (j.id.equalsIgnoreCase(needle)) return j;
            if (j.name().equalsIgnoreCase(needle)) return j;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.id;
    }

    public String id() {
        return this.id;
    }
}
