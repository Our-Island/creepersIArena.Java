package top.ourisland.creepersiarena.game.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.game.lobby.inventory.InventorySnapshot;
import top.ourisland.creepersiarena.job.JobId;

import java.util.UUID;

public final class PlayerSession {
    private final UUID playerId;

    private PlayerState state = PlayerState.HUB;

    private JobId selectedJob;
    private Integer selectedTeam;
    private int lobbyJobPage = 0;

    private InventorySnapshot outsideSnapshot;

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

    public UUID playerId() {
        return playerId;
    }

    public PlayerState state() {
        return state;
    }

    public void state(PlayerState s) {
        this.state = s;
    }

    public JobId selectedJob() {
        return selectedJob;
    }

    public void selectedJob(JobId job) {
        this.selectedJob = job;
    }

    public Integer selectedTeam() {
        return selectedTeam;
    }

    public void selectedTeam(Integer team) {
        this.selectedTeam = team;
    }

    public int lobbyJobPage() {
        return lobbyJobPage;
    }

    public void lobbyJobPage(int v) {
        this.lobbyJobPage = v;
    }

    public InventorySnapshot outsideSnapshot() {
        return outsideSnapshot;
    }

    public void outsideSnapshot(InventorySnapshot snap) {
        this.outsideSnapshot = snap;
    }

    public int respawnSecondsRemaining() {
        return respawnSecondsRemaining;
    }

    public void respawnSecondsRemaining(int v) {
        this.respawnSecondsRemaining = v;
    }

    public Location battleSpawnOverride() {
        return battleSpawnOverride;
    }

    public void battleSpawnOverride(Location loc) {
        this.battleSpawnOverride = loc;
    }

    // --- steal getters/setters ---
    public String selectedTeamKey() {
        return selectedTeamKey;
    }

    public void selectedTeamKey(String k) {
        this.selectedTeamKey = k;
    }

    public boolean stealReady() {
        return stealReady;
    }

    public void stealReady(boolean v) {
        this.stealReady = v;
    }

    public boolean stealParticipant() {
        return stealParticipant;
    }

    public void stealParticipant(boolean v) {
        this.stealParticipant = v;
    }

    public boolean stealAlive() {
        return stealAlive;
    }

    public void stealAlive(boolean v) {
        this.stealAlive = v;
    }
}
