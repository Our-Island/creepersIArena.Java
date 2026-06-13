package top.ourisland.creepersiarena.api.ability;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.arena.ArenaId;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.identity.ContextAttributeKey;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime facts used when deciding whether an ability is active.
 */
public record AbilityContext(
        @Nullable GameRuntime runtime,
        @Nullable GameSession game,
        @Nullable Player player,
        @Nullable GameModeId modeId,
        @Nullable ArenaId arenaId,
        @Nullable String phase,
        @Nullable String reason,
        Map<ContextAttributeKey<?>, Object> attributes
) {

    public AbilityContext {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    public <T> @Nullable T attribute(@lombok.NonNull ContextAttributeKey<T> key) {
        Object value = attributes.get(key);
        if (value == null) return null;
        if (!key.type().isInstance(value)) {
            throw new IllegalStateException("Ability context attribute %s contains %s, expected %s".formatted(
                    key.asString(),
                    value.getClass().getName(),
                    key.type().getName()
            ));
        }
        return key.type().cast(value);
    }

    public static final class Builder {

        private final Map<ContextAttributeKey<?>, Object> attributes = new LinkedHashMap<>();
        private GameRuntime runtime;
        private GameSession game;
        private Player player;
        private GameModeId modeId;
        private ArenaId arenaId;
        private String phase;
        private String reason;

        public Builder runtime(@Nullable GameRuntime runtime) {
            this.runtime = runtime;
            return this;
        }

        public Builder game(@Nullable GameSession game) {
            this.game = game;
            if (game != null) {
                this.modeId = game.mode();
                if (game.arena() != null) this.arenaId = game.arena().id();
            }
            return this;
        }

        public Builder player(@Nullable Player player) {
            this.player = player;
            return this;
        }

        public Builder modeId(@Nullable GameModeId modeId) {
            this.modeId = modeId;
            return this;
        }

        public Builder arenaId(@Nullable ArenaId arenaId) {
            this.arenaId = arenaId;
            return this;
        }

        public Builder phase(@Nullable String phase) {
            this.phase = phase;
            return this;
        }

        public Builder reason(@Nullable String reason) {
            this.reason = reason;
            return this;
        }

        public <T> Builder attribute(@lombok.NonNull ContextAttributeKey<T> key, @Nullable T value) {
            if (value == null) {
                attributes.remove(key);
            } else {
                if (!key.type().isInstance(value)) {
                    throw new IllegalArgumentException(
                            "Value for %s must be %s".formatted(key.asString(), key.type().getName())
                    );
                }
                attributes.put(key, value);
            }
            return this;
        }

        public Builder attributes(@Nullable Map<ContextAttributeKey<?>, Object> values) {
            if (values != null) attributes.putAll(values);
            return this;
        }

        public AbilityContext build() {
            return new AbilityContext(runtime, game, player, modeId, arenaId, phase, reason, attributes);
        }

    }

}
