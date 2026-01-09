package top.ourisland.creepersiarena.game.mode.impl.steal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.flow.action.GameAction;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.ModeTimeline;
import top.ourisland.creepersiarena.game.mode.context.TickContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class StealTimeline implements ModeTimeline {

    private static final int PRE_SPECTATE_SECONDS = 5;
    private static final int PICK_BASE_SECONDS = 10;
    private static final int ROUND_END_SECONDS = 5;
    private static final int GAME_END_SECONDS = 8;

    private final GameRuntime runtime;
    private final GameSession session;
    private final StealState st;

    public StealTimeline(GameRuntime runtime, GameSession session, StealState state) {
        this.runtime = runtime;
        this.session = session;
        this.st = state;
    }

    @Override
    public GameModeType type() {
        return GameModeType.STEAL;
    }

    @Override
    public List<GameAction> tick(TickContext ctx) {
        GlobalConfig cfg = runtime.cfg();

        return switch (st.phase) {
            case LOBBY -> tickLobby(cfg);
            case COUNTDOWN -> tickCountdown(cfg);
            case PRE_SPECTATE -> tickPreSpectate();
            case PICK_BASE -> tickPickBase(cfg);
            case ROUND_PLAYING -> tickRoundPlaying(cfg);
            case ROUND_END -> tickRoundEnd(cfg);
            case GAME_END -> tickGameEnd();
        };
    }

    private List<GameAction> tickLobby(GlobalConfig cfg) {
        int ready = countReadyOnline();
        if (ready < cfg.game().steal().minPlayerToStart()) return List.of();

        st.phase = StealPhase.COUNTDOWN;
        st.remaining = Math.max(1, cfg.game().steal().prepareTimeSeconds());

        return List.of(new GameAction.Broadcast("§eSTEAL：人数达标，倒计时 " + st.remaining + "s"));
    }

    private List<GameAction> tickCountdown(GlobalConfig cfg) {
        int ready = countReadyOnline();
        if (ready < cfg.game().steal().minPlayerToStart()) {
            st.phase = StealPhase.LOBBY;
            st.remaining = 0;
            return List.of(new GameAction.Broadcast("§cSTEAL：人数不足，倒计时取消"));
        }

        st.remaining--;
        if (st.remaining > 0) {
            if (st.remaining == 10 || st.remaining == 5 || st.remaining <= 3) {
                return List.of(new GameAction.Broadcast("§eSTEAL：倒计时 " + st.remaining + "s"));
            }
            return List.of();
        }

        // 锁定参赛者、分队（仍然写在 timeline 里；后续可再拆 service）
        lockParticipants();
        assignTeamsBalanced();

        st.phase = StealPhase.PRE_SPECTATE;
        st.remaining = PRE_SPECTATE_SECONDS;

        return List.of(
                new GameAction.Broadcast("§7STEAL：地图展示…"),
                new GameAction.ToSpectate(session.players(), session.arena().anchor().clone().add(0, 8, 0))
        );
    }

    private List<GameAction> tickPreSpectate() {
        if (--st.remaining > 0) return List.of();

        st.phase = StealPhase.PICK_BASE;
        st.remaining = PICK_BASE_SECONDS;

        // 这里不直接 ToBattle：因为 STEAL 的“基地/选职业”阶段你可能需要不同 kit；
        // 当前最小实现：仍回 hub，让玩家选职业（后续你再加“基地 kit”）
        return List.of(new GameAction.Broadcast("§bSTEAL：基地准备阶段 " + st.remaining + "s（可选职业）"));
    }

    private List<GameAction> tickPickBase(GlobalConfig cfg) {
        if (--st.remaining > 0) return List.of();

        st.phase = StealPhase.ROUND_PLAYING;
        st.remaining = Math.max(1, cfg.game().steal().timePerRoundSeconds());

        return List.of(new GameAction.Broadcast("§cSTEAL：开局！回合时长 " + st.remaining + "s"));
    }

    private List<GameAction> tickRoundPlaying(GlobalConfig cfg) {
        if (--st.remaining > 0) return List.of();

        st.phase = StealPhase.ROUND_END;
        st.remaining = ROUND_END_SECONDS;

        return List.of(new GameAction.Broadcast("§fSTEAL：回合结束，结算中…"));
    }

    private List<GameAction> tickRoundEnd(GlobalConfig cfg) {
        if (--st.remaining > 0) return List.of();

        st.roundIndex++;
        int total = Math.max(1, cfg.game().steal().totalRound());

        if (st.roundIndex >= total) {
            st.phase = StealPhase.GAME_END;
            st.remaining = GAME_END_SECONDS;
            return List.of(new GameAction.Broadcast("§6STEAL：整局结束！"));
        }

        reviveParticipantsForNextRound();

        st.phase = StealPhase.PRE_SPECTATE;
        st.remaining = PRE_SPECTATE_SECONDS;

        return List.of(
                new GameAction.Broadcast("§7STEAL：准备下一回合（" + (st.roundIndex + 1) + "/" + total + "）…"),
                new GameAction.ToSpectate(session.players(), session.arena().anchor().clone().add(0, 8, 0))
        );
    }

    private List<GameAction> tickGameEnd() {
        if (--st.remaining > 0) return List.of();

        st.roundIndex = 0;
        st.phase = StealPhase.LOBBY;
        st.remaining = 0;

        // 最小实现：回 hub 可再次准备
        return List.of(
                new GameAction.Broadcast("§aSTEAL：已回到大厅，可再次准备"),
                new GameAction.ToHub(session.players())
        );
    }

    // ---------- helpers：这里依赖 PlayerSessionStore（通过 runtime.sessionStore） ----------
    private int countReadyOnline() {
        int c = 0;
        for (UUID id : session.players()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline()) continue;
            var s = runtime.sessionStore().get(p);
            if (s != null && s.stealReady()) c++;
        }
        return c;
    }

    private void lockParticipants() {
        for (UUID id : session.players()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline()) continue;

            var s = runtime.sessionStore().getOrCreate(p);
            boolean participant = s.stealReady();

            s.stealParticipant(participant);
            s.stealAlive(true);

            // 非参赛者此处不强制改状态：由规则层 join/respawn 做，Flow 迁移
        }
    }

    private void assignTeamsBalanced() {
        List<Player> ps = new ArrayList<>();
        for (UUID id : session.players()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline()) continue;
            var s = runtime.sessionStore().get(p);
            if (s != null && s.stealParticipant()) ps.add(p);
        }

        ps.sort(Comparator.comparing(Player::getUniqueId));
        for (int i = 0; i < ps.size(); i++) {
            runtime.sessionStore().get(ps.get(i)).selectedTeamKey((i % 2 == 0) ? "red" : "blue");
        }
    }

    private void reviveParticipantsForNextRound() {
        for (UUID id : session.players()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline()) continue;
            var s = runtime.sessionStore().get(p);
            if (s == null || !s.stealParticipant()) continue;
            s.stealAlive(true);
        }
    }
}
