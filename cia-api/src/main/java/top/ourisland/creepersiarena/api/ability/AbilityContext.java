package top.ourisland.creepersiarena.api.ability;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime facts used when deciding whether an ability is active.
 */
public record AbilityContext(
        @Nullable GameRuntime runtime,
        @Nullable GameSession game,
        @Nullable Player player,
        @Nullable String modeId,
        @Nullable String arenaId,
        @Nullable String phase,
        @Nullable String reason,
        Map<String, Object> attributes
) {

    public AbilityContext {
        attributes = attributes == null
                ? Map.of()
                : Map.copyOf(attributes);
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .runtime(runtime)
                .game(game)
                .player(player)
                .modeId(modeId)
                .arenaId(arenaId)
                .phase(phase)
                .reason(reason)
                .attributes(attributes);
    }

    public static final class Builder {

        private final Map<String, Object> attributes = new LinkedHashMap<>();
        private GameRuntime runtime;
        private GameSession game;
        private Player player;
        private String modeId;
        private String arenaId;
        private String phase;
        private String reason;

        public Builder runtime(@Nullable GameRuntime runtime) {
            this.runtime = runtime;
            return this;
        }

        public Builder game(@Nullable GameSession game) {
            this.game = game;
            if (game != null) {
                if (game.mode() != null) this.modeId = game.mode().id();
                if (game.arena() != null) this.arenaId = game.arena().id();
            }
            return this;
        }

        public Builder player(@Nullable Player player) {
            this.player = player;
            return this;
        }

        public Builder modeId(@Nullable String modeId) {
            this.modeId = modeId;
            return this;
        }

        public Builder arenaId(@Nullable String arenaId) {
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

        public Builder attribute(String key, Object value) {
            if (key != null && !key.isBlank() && value != null) attributes.put(key, value);
            return this;
        }

        public Builder attributes(@Nullable Map<String, Object> values) {
            if (values != null) attributes.putAll(values);
            return this;
        }

        public AbilityContext build() {
            return new AbilityContext(runtime, game, player, modeId, arenaId, phase, reason, attributes);
        }

    }

}
