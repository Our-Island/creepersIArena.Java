package top.ourisland.creepersiarena.api.job;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class JobId {

    private static final Map<String, JobId> CACHE = new ConcurrentHashMap<>();

    private final String id;

    public static JobId fromId(String raw) {
        if (raw == null) return null;
        String normalized = normalize(raw);
        if (normalized.isBlank()) return null;
        return of(normalized);
    }

    private static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static JobId of(String raw) {
        String normalized = normalize(raw);
        if (normalized.isBlank()) throw new IllegalArgumentException("job id is blank");
        return CACHE.computeIfAbsent(normalized, JobId::new);
    }

    private JobId(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String path() {
        int colon = id.indexOf(':');
        return colon >= 0 ? id.substring(colon + 1) : id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobId jobId)) return false;
        return id.equals(jobId.id);
    }

    @Override
    public String toString() {
        return id;
    }

}
