package top.ourisland.creepersiarena.api.game.mutation;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stable mutation identifier. It is intentionally not an enum so extensions can add effects.
 */
@Getter
public final class MutationType {

    private static final Map<String, MutationType> CACHE = new ConcurrentHashMap<>();

    public static final MutationType NONE = MutationType.of("none");

    private final String id;

    public static @Nullable MutationType fromId(@Nullable String raw) {
        String normalized = normalize(raw);
        if (normalized.isBlank()) return null;
        return of(normalized);
    }

    private static @NonNull String normalize(@Nullable String raw) {
        return raw == null
                ? ""
                : raw.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    public static MutationType of(@lombok.NonNull String raw) {
        String normalized = normalize(raw);
        if (normalized.isBlank()) throw new IllegalArgumentException("mutation id is blank");
        return CACHE.computeIfAbsent(normalized, MutationType::new);
    }

    private MutationType(String id) {
        this.id = id;
    }

    public boolean isNone() {
        return equals(NONE);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MutationType that)) return false;
        return id.equals(that.id());
    }

    @Override
    public String toString() {
        return id;
    }

}
