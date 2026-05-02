package top.ourisland.creepersiarena.api.game.player;

import lombok.Data;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.job.JobId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public final class PlayerSession {

    private final UUID playerId;
    private final Map<String, Object> modeData = new HashMap<>();
    private PlayerState state = PlayerState.HUB;
    private JobId selectedJob;
    private Integer selectedTeam;
    private String selectedTeamKey;
    private int lobbyJobPage = 0;
    private int respawnSecondsRemaining = 0;

    public PlayerSession(Player player) {
        this.playerId = player.getUniqueId();
    }

    public boolean modeBoolean(String key, boolean defaultValue) {
        Object value = modeData(key);
        if (value instanceof Boolean bool) return bool;
        if (value instanceof String string) return Boolean.parseBoolean(string);
        return defaultValue;
    }

    public Object modeData(String key) {
        if (key == null || key.isBlank()) return null;
        return modeData.get(key);
    }

    public void setModeBoolean(String key, boolean value) {
        modeData(key, value);
    }

    public void modeData(String key, Object value) {
        if (key == null || key.isBlank()) return;
        if (value == null) {
            modeData.remove(key);
            return;
        }
        modeData.put(key, value);
    }

    public void clearModeData(String prefix) {
        if (prefix == null || prefix.isBlank()) return;
        modeData.keySet().removeIf(key -> key.startsWith(prefix));
    }

}
