package top.ourisland.creepersiarena.job;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class JobId {

    private static final Map<String, JobId> CACHE = new ConcurrentHashMap<>();

    public static final JobId CREEPER = of("creeper");
    public static final JobId MOISON = of("moison");
    public static final JobId AVENGER = of("avenger");
    public static final JobId BLOODLINE = of("bloodline");
    public static final JobId GOLEM = of("golem");
    public static final JobId WOLONG = of("wolong");
    public static final JobId YSAHAN = of("ysahan");

    private final String id;

    private JobId(String id) {
        this.id = id;
    }

    public static JobId of(String raw) {
        String normalized = normalize(raw);
        if (normalized.isBlank()) throw new IllegalArgumentException("job id is blank");
        return CACHE.computeIfAbsent(normalized, JobId::new);
    }

    public static JobId fromId(String raw) {
        if (raw == null) return null;
        String normalized = normalize(raw);
        if (normalized.isBlank()) return null;
        return of(normalized);
    }

    private static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobId jobId)) return false;
        return id.equals(jobId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
