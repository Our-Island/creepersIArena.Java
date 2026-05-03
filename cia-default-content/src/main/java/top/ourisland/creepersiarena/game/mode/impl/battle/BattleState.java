package top.ourisland.creepersiarena.game.mode.impl.battle;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.game.mode.impl.battle.config.BattleModeConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BattleState {

    static final GameModeType TYPE = GameModeType.of("battle");
    static final String MODE_DATA_PREFIX = "battle:";
    static final String PARTICIPANT_KEY = MODE_DATA_PREFIX + "participant";

    private final GameRuntime runtime;
    private final GameSession session;
    private final BattleModeConfig config;
    private final Map<Integer, Integer> teamKills = new LinkedHashMap<>();

    private int mapProgress;
    private boolean rotationPending;

    public BattleState(GameRuntime runtime, GameSession session, BattleModeConfig config) {
        this.runtime = runtime;
        this.session = session;
        this.config = config;
    }

    static boolean markedFighter(PlayerSession player) {
        return player != null && player.modeBoolean(PARTICIPANT_KEY, false);
    }

    public GameSession session() {
        return session;
    }

    public BattleModeConfig config() {
        return config;
    }

    public int mapProgress() {
        return mapProgress;
    }

    public float progressRatio() {
        return Math.clamp((float) mapProgress / config.mapProgressTarget(), 0.0F, 1.0F);
    }

    public boolean rotationPending() {
        return rotationPending;
    }

    public void rotationPending(boolean rotationPending) {
        this.rotationPending = rotationPending;
    }

    public void markFighter(PlayerSession player) {
        if (player == null) return;
        player.modeData(PARTICIPANT_KEY, true);
    }

    public void clearFighter(PlayerSession player) {
        if (player == null) return;
        player.clearModeData(MODE_DATA_PREFIX);
    }

    public Set<UUID> players() {
        return session.players();
    }

    public boolean recordKill(Player killer, Player victim) {
        if (killer == null || victim == null || killer.equals(victim)) return false;
        if (!isFighter(killer) || !isFighter(victim)) return false;

        PlayerSession killerSession = runtime.sessionStore().get(killer);
        PlayerSession victimSession = runtime.sessionStore().get(victim);
        if (sameTeam(killerSession, victimSession)) return false;

        int team = teamOf(killerSession);
        teamKills.merge(team, 1, Integer::sum);

        int delta = config.killProgressForPopulation(onlineFighterCount());
        mapProgress = Math.min(config.mapProgressTarget(), mapProgress + delta);
        return true;
    }

    public boolean isFighter(Player player) {
        if (player == null || !player.isOnline()) return false;
        if (!session.players().contains(player.getUniqueId())) return false;
        PlayerSession playerSession = runtime.sessionStore().get(player);
        return playerSession != null && playerSession.state() == PlayerState.IN_GAME;
    }

    private boolean sameTeam(PlayerSession left, PlayerSession right) {
        if (left == null || right == null) return false;
        Integer leftTeam = left.selectedTeam();
        Integer rightTeam = right.selectedTeam();
        return leftTeam != null && leftTeam.equals(rightTeam);
    }

    private int teamOf(PlayerSession session) {
        if (session == null || session.selectedTeam() == null) return 0;
        return Math.clamp(session.selectedTeam(), 1, config.maxTeam());
    }

    public int onlineFighterCount() {
        int count = 0;
        for (UUID uuid : session.players()) {
            Player player = Bukkit.getPlayer(uuid);
            if (isFighter(player)) count++;
        }
        return count;
    }

    public boolean reachedMapTarget() {
        return mapProgress >= config.mapProgressTarget();
    }

    public Component mapFinishedMessage() {
        return Component.text("Battle map complete: ", NamedTextColor.GOLD)
                .append(Component.text(session.arena().id(), NamedTextColor.YELLOW))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(scoreSummaryComponent());
    }

    public Component scoreSummaryComponent() {
        if (teamKills.isEmpty()) return Component.text("no kills recorded", NamedTextColor.GRAY);

        Component out = Component.empty();
        boolean first = true;
        for (int team = 1; team <= config.maxTeam(); team++) {
            int kills = teamKills.getOrDefault(team, 0);
            if (kills <= 0) continue;
            if (!first) out = out.append(Component.text(" / ", NamedTextColor.GRAY));
            out = out.append(Component.text("Team " + team + " ", NamedTextColor.GRAY))
                    .append(Component.text(kills, NamedTextColor.WHITE));
            first = false;
        }
        return first ? Component.text("no kills recorded", NamedTextColor.GRAY) : out;
    }

    public String scoreSummary() {
        if (teamKills.isEmpty()) return "no kills recorded";

        StringBuilder out = new StringBuilder();
        boolean first = true;
        for (int team = 1; team <= config.maxTeam(); team++) {
            int kills = teamKills.getOrDefault(team, 0);
            if (kills <= 0) continue;
            if (!first) out.append(" / ");
            out.append("Team ").append(team).append(" ").append(kills);
            first = false;
        }
        return first ? "no kills recorded" : out.toString();
    }

}
