package top.ourisland.creepersiarena.api.game.player;

import lombok.Data;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.api.identity.ExtensionSessionData;
import top.ourisland.creepersiarena.api.identity.SessionDataKey;
import top.ourisland.creepersiarena.api.job.JobId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public final class PlayerSession {

    private final UUID playerId;
    private final Map<SessionDataKey<?>, Object> modeData = new HashMap<>();
    private PlayerState state = PlayerState.HUB;
    private JobId selectedJob;
    private TeamId selectedTeam;
    private int lobbyJobPage;
    private int respawnSecondsRemaining;

    public PlayerSession(@NonNull Player player) {
        this.playerId = player.getUniqueId();
    }

    public <T> T getOrDefault(SessionDataKey<T> key, T defaultValue) {
        T value = get(key);
        return value == null ? defaultValue : value;
    }

    public <T> @Nullable T get(
            @lombok.NonNull SessionDataKey<T> key
    ) {
        var value = modeData.get(key);
        if (value == null) return null;

        if (!key.type().isInstance(value)) {
            throw new IllegalStateException("Session data %s contains %s, expected %s".formatted(
                    key.asString(),
                    value.getClass().getName(),
                    key.type().getName()
            ));
        }
        return key.type().cast(value);
    }

    public <T> void set(
            @lombok.NonNull SessionDataKey<T> key,
            @Nullable T value
    ) {
        if (value == null) {
            modeData.remove(key);
            return;
        }
        if (!key.type().isInstance(value)) {
            throw new IllegalArgumentException("Value for %s must be %s".formatted(
                    key.asString(),
                    key.type().getName()
            ));
        }
        modeData.put(key, value);
    }

    public void remove(@lombok.NonNull SessionDataKey<?> key) {
        modeData.remove(key);
    }

    public void clear(@lombok.NonNull ExtensionSessionData scope) {
        modeData.keySet().removeIf(scope::owns);
    }

}
