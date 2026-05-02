package top.ourisland.creepersiarena.api.game.mode;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class GameModeType {

    private static final Map<String, GameModeType> CACHE = new ConcurrentHashMap<>();

    public static final GameModeType BATTLE = of("battle");
    public static final GameModeType STEAL = of("steal");

    private final String id;

    public static GameModeType fromId(String raw) {
        if (raw == null) return null;
        String normalized = normalize(raw);
        if (normalized.isBlank()) return null;
        return of(normalized);
    }

    private static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static GameModeType of(String raw) {
        String normalized = normalize(raw);
        if (normalized.isBlank()) throw new IllegalArgumentException("mode id is blank");
        return CACHE.computeIfAbsent(normalized, GameModeType::new);
    }

    private GameModeType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean isBattle() {
        return equals(BATTLE);
    }

    public boolean isSteal() {
        return equals(STEAL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameModeType that)) return false;
        return id.equals(that.id);
    }

    @Override
    public String toString() {
        return id;
    }

}
