package top.ourisland.creepersiarena.game.player;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.job.JobId;

import java.util.UUID;

@Data
public final class PlayerSession {
    private final UUID playerId;

    private PlayerState state = PlayerState.HUB;

    private JobId selectedJob;
    private Integer selectedTeam;
    private int lobbyJobPage = 0;

    private int respawnSecondsRemaining = 0;
    private Location battleSpawnOverride;

    // --- STEAL mode fields ---
    private String selectedTeamKey;      // "red" / "blue"
    private boolean stealReady;
    private boolean stealParticipant;
    private boolean stealAlive = true;

    public PlayerSession(Player player) {
        this.playerId = player.getUniqueId();
    }
}
