package top.ourisland.creepersiarena.game.mode.impl.steal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

        return List.of(new GameAction.Broadcast(Component.text("STEAL：人数达标，倒计时 " + st.remaining + "s", NamedTextColor.GOLD)));
    }

    private List<GameAction> tickCountdown(GlobalConfig cfg) {
        int ready = countReadyOnline();
        if (ready < cfg.game().steal().minPlayerToStart()) {
            st.phase = StealPhase.LOBBY;
            st.remaining = 0;
            return List.of(new GameAction.Broadcast(Component.text("STEAL：人数不足，倒计时取消", NamedTextColor.RED)));
        }

        st.remaining--;
        if (st.remaining > 0) {
            if (st.remaining == 10 || st.remaining == 5 || st.remaining <= 3) {
                return List.of(new GameAction.Broadcast(Component.text("STEAL：倒计时 " + st.remaining + "s", NamedTextColor.YELLOW)));
            }
            return List.of();
        }

        lockParticipants();
        assignTeamsBalanced();

        st.phase = StealPhase.PRE_SPECTATE;
        st.remaining = PRE_SPECTATE_SECONDS;

        return List.of(
                new GameAction.Broadcast(Component.text("§7STEAL：地图展示…", NamedTextColor.DARK_AQUA)),
                new GameAction.ToSpectate(session.players(), session.arena().anchor().clone().add(0, 8, 0))
        );
    }

    private List<GameAction> tickPreSpectate() {
        if (--st.remaining > 0) return List.of();

        st.phase = StealPhase.PICK_BASE;
        st.remaining = PICK_BASE_SECONDS;

        // 这里不直接 ToBattle：因为 STEAL 的“基地/选职业”阶段你可能需要不同 kit；
        // 当前最小实现：仍回 hub，让玩家选职业（后续你再加“基地 kit”）
        return List.of(new GameAction.Broadcast(Component.text("§bSTEAL：基地准备阶段 " + st.remaining + "s（可选职业）", NamedTextColor.AQUA)));
    }

    private List<GameAction> tickPickBase(GlobalConfig cfg) {
        if (--st.remaining > 0) return List.of();

        st.phase = StealPhase.ROUND_PLAYING;
        st.remaining = Math.max(1, cfg.game().steal().timePerRoundSeconds());

        return List.of(new GameAction.Broadcast(Component.text("§cSTEAL：开局！回合时长 " + st.remaining + "s", NamedTextColor.RED)));
    }

    private List<GameAction> tickRoundPlaying(GlobalConfig cfg) {
        if (--st.remaining > 0) return List.of();

        st.phase = StealPhase.ROUND_END;
        st.remaining = ROUND_END_SECONDS;

        return List.of(new GameAction.Broadcast(Component.text("§fSTEAL：回合结束，结算中…", NamedTextColor.WHITE)));
    }

    private List<GameAction> tickRoundEnd(GlobalConfig cfg) {
        if (--st.remaining > 0) return List.of();

        st.roundIndex++;
        int total = Math.max(1, cfg.game().steal().totalRound());

        if (st.roundIndex >= total) {
            st.phase = StealPhase.GAME_END;
            st.remaining = GAME_END_SECONDS;
            return List.of(new GameAction.Broadcast(Component.text("STEAL：整局结束！", NamedTextColor.GOLD)));
        }

        reviveParticipantsForNextRound();

        st.phase = StealPhase.PRE_SPECTATE;
        st.remaining = PRE_SPECTATE_SECONDS;

        return List.of(
                new GameAction.Broadcast(Component.text("STEAL：准备下一回合（" + (st.roundIndex + 1) + "/" + total + "）…", NamedTextColor.GRAY)),
                new GameAction.ToSpectate(session.players(), session.arena().anchor().clone().add(0, 8, 0))
        );
    }

    private List<GameAction> tickGameEnd() {
        if (--st.remaining > 0) return List.of();

        st.roundIndex = 0;
        st.phase = StealPhase.LOBBY;
        st.remaining = 0;

        return List.of(
                new GameAction.Broadcast(Component.text("STEAL：已回到大厅，可再次准备", NamedTextColor.GREEN)),
                new GameAction.ToHub(session.players())
        );
    }

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
