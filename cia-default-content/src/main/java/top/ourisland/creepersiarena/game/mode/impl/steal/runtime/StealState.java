package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import top.ourisland.creepersiarena.game.mode.impl.steal.config.StealArenaConfig;
import top.ourisland.creepersiarena.game.mode.impl.steal.config.StealModeConfig;
import top.ourisland.creepersiarena.game.mode.impl.steal.model.StealTeam;

import java.util.*;

final class StealState {

    final StealBossBars bossBars = new StealBossBars();
    final Set<UUID> participants = new LinkedHashSet<>();
    final Set<UUID> alive = new LinkedHashSet<>();
    final Map<UUID, StealTeam> teams = new LinkedHashMap<>();
    final Map<UUID, Integer> mineCooldowns = new LinkedHashMap<>();
    private final StealModeConfig modeConfig;
    private final StealArenaConfig arenaConfig;
    StealPhase phase = StealPhase.LOBBY;
    int remainingSeconds;
    int roundIndex;
    int redWins;
    int blueWins;
    int minedBlocks;
    int targetMineCount = 10;
    int tourStep = -1;
    boolean closing;

    StealState(StealModeConfig modeConfig, StealArenaConfig arenaConfig) {
        this.modeConfig = Objects.requireNonNull(modeConfig, "modeConfig");
        this.arenaConfig = Objects.requireNonNull(arenaConfig, "arenaConfig");
        this.targetMineCount = Math.max(1, modeConfig.targetMineCount());
    }

    StealModeConfig modeConfig() {
        return modeConfig;
    }

    StealArenaConfig arenaConfig() {
        return arenaConfig;
    }

    void resetWholeGame() {
        phase = StealPhase.LOBBY;
        remainingSeconds = 0;
        roundIndex = 0;
        redWins = 0;
        blueWins = 0;
        minedBlocks = 0;
        targetMineCount = Math.max(1, modeConfig.targetMineCount());
        tourStep = -1;
        closing = false;
        participants.clear();
        alive.clear();
        teams.clear();
        mineCooldowns.clear();
    }

    void resetRoundCounters(int targetMineCount) {
        minedBlocks = 0;
        this.targetMineCount = Math.max(1, targetMineCount);
        alive.clear();
        alive.addAll(participants);
        mineCooldowns.clear();
    }

    boolean isParticipant(UUID id) {
        return id != null && participants.contains(id);
    }

    boolean isAlive(UUID id) {
        return id != null && alive.contains(id);
    }

    void markDead(UUID id) {
        if (id != null) alive.remove(id);
    }

    void removeParticipant(UUID id) {
        if (id == null) return;
        participants.remove(id);
        alive.remove(id);
        teams.remove(id);
        mineCooldowns.remove(id);
    }

    void setTeam(UUID id, StealTeam team) {
        if (id == null || team == null) return;
        teams.put(id, team);
    }

    StealTeam team(UUID id) {
        return id == null ? null : teams.get(id);
    }

    int wins(StealTeam team) {
        return team == StealTeam.RED ? redWins : blueWins;
    }

    void addWin(StealTeam team) {
        if (team == StealTeam.RED) redWins++;
        if (team == StealTeam.BLUE) blueWins++;
    }

    int livingOn(StealTeam team) {
        int count = 0;
        for (UUID id : alive) {
            if (teams.get(id) == team) count++;
        }
        return count;
    }

    int participantsOn(StealTeam team) {
        int count = 0;
        for (UUID id : participants) {
            if (teams.get(id) == team) count++;
        }
        return count;
    }

    Set<UUID> participantIds() {
        return Collections.unmodifiableSet(participants);
    }

    int mineCooldown(UUID id) {
        if (id == null) return 0;
        return Math.max(0, mineCooldowns.getOrDefault(id, 0));
    }

    void startMineCooldown(UUID id, int seconds) {
        if (id != null && seconds > 0) mineCooldowns.put(id, seconds);
    }

    void tickMineCooldowns() {
        Iterator<Map.Entry<UUID, Integer>> it = mineCooldowns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            int next = entry.getValue() - 1;
            if (next <= 0) {
                it.remove();
            } else {
                entry.setValue(next);
            }
        }
    }

}
