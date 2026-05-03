package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.flow.action.GameAction;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModeTimeline;
import top.ourisland.creepersiarena.api.game.mode.context.TickContext;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.mode.impl.steal.config.StealArenaConfig;
import top.ourisland.creepersiarena.game.mode.impl.steal.config.StealModeConfig;
import top.ourisland.creepersiarena.game.mode.impl.steal.model.StealTeam;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.*;

final class StealTimeline implements IModeTimeline {

    private final GameRuntime runtime;
    private final GameSession session;
    private final StealState state;
    private final Random random = new Random();

    StealTimeline(
            GameRuntime runtime,
            GameSession session,
            StealState state
    ) {
        this.runtime = runtime;
        this.session = session;
        this.state = state;
    }

    @Override
    public GameModeType type() {
        return GameModeType.of("steal");
    }

    @Override
    public List<GameAction> tick(TickContext ctx) {
        var cfg = StealModeConfig.from(runtime.cfg());
        var arenaCfg = StealArenaConfig.from(session.arena());

        return switch (state.phase) {
            case LOBBY -> tickLobby(cfg);
            case START_COUNTDOWN -> tickCountdown(cfg, arenaCfg);
            case SPECTATOR_TOUR -> tickSpectatorTour(cfg, arenaCfg);
            case CHOOSE_JOB -> tickChooseJob(cfg, arenaCfg);
            case ROUND_PLAYING -> tickRoundPlaying(cfg);
            case ROUND_CELEBRATION -> tickRoundCelebration(cfg, arenaCfg);
            case GAME_END_CELEBRATION -> tickGameEndCelebration(cfg, arenaCfg);
        };
    }

    GameRuntime runtime() {
        return runtime;
    }

    StealState state() {
        return state;
    }

    private List<GameAction> tickLobby(StealModeConfig cfg) {
        var players = onlineSessionPlayers();
        int population = players.size();
        int ready = countReadyOnline();
        int required = cfg.requiredReadyPlayers(population);

        state.bossBars.showWaiting(players, ready, required, Math.max(1, population), 0, cfg.startCountdownSeconds());
        if (population == 0 || ready < required) return List.of();

        state.phase = StealPhase.START_COUNTDOWN;
        state.remainingSeconds = cfg.startCountdownSeconds();
        state.closing = false;

        for (var p : players) {
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            var ps = runtime.sessionStore().get(p);
            if (StealPlayerState.ready(ps)) {
                Msg.actionBar(p, Component.text("✪ 游戏将在" + state.remainingSeconds + "秒内开始，你将参与游戏", NamedTextColor.GREEN));
            } else {
                Msg.actionBar(p, Component.text("✪ 游戏将在" + state.remainingSeconds + "秒内开始，你还未准备", NamedTextColor.RED));
            }
        }
        return List.of();
    }

    private List<GameAction> tickCountdown(StealModeConfig cfg, StealArenaConfig arenaCfg) {
        var players = onlineSessionPlayers();
        int population = players.size();
        int ready = countReadyOnline();
        int required = cfg.requiredReadyPlayers(population);
        state.bossBars.showWaiting(players, ready, required, Math.max(1, population), state.remainingSeconds, cfg.startCountdownSeconds());

        if (population == 0 || ready < required) {
            state.phase = StealPhase.LOBBY;
            state.remainingSeconds = 0;
            for (var p : players) {
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.PLAYERS, 1.0f, 1.0f);
                Msg.actionBar(p, Component.text("✪ 已准备人数不足，需要更多玩家准备", NamedTextColor.RED));
            }
            return List.of();
        }

        for (var p : players) {
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        state.remainingSeconds--;
        if (state.remainingSeconds > 0) return List.of();

        return startSpectatorTour(cfg, arenaCfg);
    }

    private List<GameAction> startSpectatorTour(StealModeConfig cfg, StealArenaConfig arenaCfg) {
        lockParticipants();
        assignTeamsBalanced();
        state.redWins = 0;
        state.blueWins = 0;
        state.roundIndex = 0;
        state.minedBlocks = 0;
        state.phase = StealPhase.SPECTATOR_TOUR;
        state.remainingSeconds = cfg.spectatorTourSeconds();
        state.tourStep = -1;
        state.closing = false;
        arenaCfg.resetRedstoneTargets();
        arenaCfg.setSelectionBarriers(false);

        for (var p : onlineSessionPlayers()) {
            p.getInventory().clear();
            p.setGameMode(GameMode.SPECTATOR);
            p.showTitle(Title.title(
                    Component.text("观察地图", NamedTextColor.GRAY),
                    Component.text("偷窃对抗", NamedTextColor.DARK_AQUA)
            ));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        return List.of(new GameAction.ToSpectate(session.players(), session.arena().anchor().clone().add(0, 8, 0)));
    }

    private List<GameAction> tickSpectatorTour(StealModeConfig cfg, StealArenaConfig arenaCfg) {
        var players = onlineSessionPlayers();
        state.bossBars.showSpectator(players, state.remainingSeconds, cfg.spectatorTourSeconds());
        showTourPointIfNeeded(arenaCfg, cfg.spectatorTourSeconds());

        state.remainingSeconds--;
        if (state.remainingSeconds > 0) return List.of();

        state.targetMineCount = effectiveTargetMineCount(cfg, arenaCfg);
        announceRoundObjective();
        return startChooseJob(cfg, arenaCfg);
    }

    private int effectiveTargetMineCount(StealModeConfig cfg, StealArenaConfig arenaCfg) {
        int physicalTargets = arenaCfg.redstoneTargetCount();
        if (physicalTargets <= 0) return Math.max(1, cfg.targetMineCount());
        return Math.clamp(cfg.targetMineCount(), 1, physicalTargets);
    }

    private void showTourPointIfNeeded(StealArenaConfig arenaCfg, int totalSeconds) {
        var points = arenaCfg.tourPoints();
        if (points.isEmpty()) return;

        int elapsed = totalSeconds - state.remainingSeconds;
        int nextStep = state.tourStep + 1;
        if (nextStep >= points.size()) return;

        int trigger = tourTriggerTick(nextStep, points.size(), totalSeconds);
        if (elapsed < trigger) return;

        state.tourStep = nextStep;
        var point = points.get(nextStep);
        for (var p : onlineSessionPlayers()) {
            p.teleportAsync(point.location());
            Msg.send(p, point.message());
            p.playSound(p, Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 0.0f);
        }
    }

    private int tourTriggerTick(
            int index,
            int count,
            int totalSeconds
    ) {
        if (count <= 1) return 0;
        return Math.round(index * (float) totalSeconds / (float) count);
    }

    private void announceRoundObjective() {
        for (var p : onlineSessionPlayers()) {
            var ps = runtime.sessionStore().get(p);
            var team = state.team(p.getUniqueId());
            if (!StealPlayerState.participant(ps) || team == null) continue;

            Msg.send(p, Component.empty());
            if (team == StealTeam.BLUE) {
                Msg.send(p, Component.text("你作为蓝队一员，合作拆除" + state.targetMineCount + "块红石矿即可获得胜利", NamedTextColor.GRAY));
                Msg.send(p, Component.text("如果时间内没有拆除足够红石矿则失败", NamedTextColor.GRAY));
            } else {
                Msg.send(p, Component.text("你作为红队一员，合作守护红石矿即可获得胜利", NamedTextColor.GRAY));
                Msg.send(p, Component.text("如果被拆除了" + state.targetMineCount + "块红石矿则失败", NamedTextColor.GRAY));
            }
        }
    }

    private List<GameAction> startChooseJob(StealModeConfig cfg, StealArenaConfig arenaCfg) {
        if (state.participants.isEmpty())
            return startFinalCelebration(cfg, arenaCfg, StealTeam.RED, StealRoundReason.NO_PARTICIPANTS);

        state.phase = StealPhase.CHOOSE_JOB;
        state.remainingSeconds = cfg.chooseJobSeconds();
        state.roundIndex++;
        state.resetRoundCounters(effectiveTargetMineCount(cfg, arenaCfg));
        arenaCfg.resetRedstoneTargets();
        arenaCfg.setSelectionBarriers(true);

        for (var p : onlineParticipants()) {
            p.getInventory().clear();
            for (var effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.RESISTANCE,
                    (cfg.chooseJobSeconds() + 2) * 20,
                    5,
                    true,
                    false,
                    false
            ));
            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.INSTANT_HEALTH,
                    20,
                    10,
                    true,
                    false,
                    false
            ));
            p.setGameMode(GameMode.ADVENTURE);
            p.playSound(p, Sound.ENTITY_ENDER_EYE_DEATH, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }

        broadcast(Component.text("第", NamedTextColor.WHITE)
                .append(Component.text(state.roundIndex, NamedTextColor.YELLOW))
                .append(Component.text(" / ", NamedTextColor.WHITE))
                .append(Component.text(cfg.totalRound(), NamedTextColor.AQUA))
                .append(Component.text(" 轮", NamedTextColor.WHITE)));
        broadcast(Component.text("STEAL：准备阶段 " + state.remainingSeconds + "s", NamedTextColor.AQUA));

        return List.of(new GameAction.EnterGame(new LinkedHashSet<>(state.participants)));
    }

    private List<GameAction> tickChooseJob(StealModeConfig cfg, StealArenaConfig arenaCfg) {
        var players = onlineParticipants();
        state.bossBars.showChooseJob(players, state.remainingSeconds, cfg.chooseJobSeconds());

        state.remainingSeconds--;
        if (state.remainingSeconds > 0) return List.of();

        arenaCfg.setSelectionBarriers(false);
        state.phase = StealPhase.ROUND_PLAYING;
        state.remainingSeconds = cfg.timePerRoundSeconds();
        state.minedBlocks = 0;
        state.mineCooldowns.clear();

        for (var p : players) {
            p.setGameMode(GameMode.SURVIVAL);
            p.playSound(p.getLocation(), "minecraft:item.goat_horn.sound.0", SoundCategory.PLAYERS, 1.0f, 0.9f);
        }
        broadcast(Component.text("STEAL：开局！回合时长 " + state.remainingSeconds + "s", NamedTextColor.RED));
        return List.of();
    }

    private List<GameAction> tickRoundPlaying(StealModeConfig cfg) {
        var players = onlineSessionPlayers();
        state.bossBars.showRound(players, state.remainingSeconds, cfg.timePerRoundSeconds(), state.minedBlocks, state.targetMineCount);
        state.tickMineCooldowns();

        if (state.minedBlocks >= state.targetMineCount)
            return endRound(cfg, StealTeam.BLUE, StealRoundReason.BLUE_MINED);
        StealRoundReason elimination = evaluateEliminationReason();
        if (elimination != null) return endRoundFromReason(cfg, elimination);

        state.remainingSeconds--;
        if (state.remainingSeconds == cfg.timePerRoundSeconds() - 30 || state.remainingSeconds == Math.max(1, cfg.timePerRoundSeconds() * 5 / 12)) {
            warnTime(state.remainingSeconds, NamedTextColor.YELLOW, Sound.BLOCK_NOTE_BLOCK_CHIME, 2.0f);
        }
        if (state.remainingSeconds <= 0) return endRound(cfg, StealTeam.RED, StealRoundReason.RED_TIMEOUT);
        return List.of();
    }

    private List<GameAction> tickRoundCelebration(StealModeConfig cfg, StealArenaConfig arenaCfg) {
        var players = onlineSessionPlayers();
        state.bossBars.showCelebration(players, state.remainingSeconds, cfg.roundCelebrationSeconds(), false);
        launchCelebrationFireworks(3);

        state.remainingSeconds--;
        if (state.remainingSeconds > 0) return List.of();

        state.bossBars.hideAll(players);
        arenaCfg.setSelectionBarriers(false);

        if (state.participantsOn(StealTeam.RED) == 0 && state.participantsOn(StealTeam.BLUE) == 0) {
            return startFinalCelebration(cfg, arenaCfg, StealTeam.RED, StealRoundReason.NO_PARTICIPANTS);
        }
        if (state.participantsOn(StealTeam.RED) == 0) {
            return startFinalCelebration(cfg, arenaCfg, StealTeam.BLUE, StealRoundReason.RED_LEFT);
        }
        if (state.participantsOn(StealTeam.BLUE) == 0) {
            return startFinalCelebration(cfg, arenaCfg, StealTeam.RED, StealRoundReason.BLUE_LEFT);
        }

        return startChooseJob(cfg, arenaCfg);
    }

    private List<GameAction> tickGameEndCelebration(StealModeConfig cfg, StealArenaConfig arenaCfg) {
        var players = onlineSessionPlayers();
        state.bossBars.showCelebration(players, state.remainingSeconds, cfg.gameEndCelebrationSeconds(), true);
        launchCelebrationFireworks(3);

        state.remainingSeconds--;
        if (state.remainingSeconds > 0) return List.of();

        arenaCfg.setSelectionBarriers(false);
        cleanupWholeGame();
        return List.of(new GameAction.EndGameAndBackToHub("steal finished"));
    }

    boolean onMinedRedstone(Player player, StealModeConfig cfg) {
        if (player == null || state.phase != StealPhase.ROUND_PLAYING) return false;
        var playerId = player.getUniqueId();
        if (!state.isParticipant(playerId) || !state.isAlive(playerId)) return false;

        var team = state.team(playerId);
        if (team != StealTeam.BLUE) {
            Msg.actionBar(player, Component.text("红队需要守护红石矿", NamedTextColor.RED));
            return false;
        }

        int cooldown = state.mineCooldown(playerId);
        if (cooldown > 0) {
            Msg.actionBar(player, Component.text("铁镐冷却中：" + cooldown + "s", NamedTextColor.YELLOW));
            return false;
        }

        state.minedBlocks++;
        state.startMineCooldown(playerId, cfg.mineCooldownSeconds());
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5 * 20, 0, true, false, false));
        player.playSound(player, Sound.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.2f, 0.0f);
        broadcast(Component.text("☸ ", NamedTextColor.BLUE)
                .append(player.displayName())
                .append(Component.text(" 拆除了一块红石矿 ( ", NamedTextColor.BLUE))
                .append(Component.text(state.minedBlocks, NamedTextColor.BLUE))
                .append(Component.text(" / " + state.targetMineCount + " )", NamedTextColor.BLUE)));
        return true;
    }

    private void broadcast(Component message) {
        for (var p : onlineSessionPlayers()) {
            Msg.send(p, message);
        }
    }

    private List<Player> onlineSessionPlayers() {
        var out = new ArrayList<Player>();
        for (var uuid : session.players()) {
            var p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) out.add(p);
        }
        return out;
    }

    void onPlayerDeath(Player player) {
        if (player == null || state.phase != StealPhase.ROUND_PLAYING) return;
        if (!state.isParticipant(player.getUniqueId())) return;
        state.markDead(player.getUniqueId());
        PlayerSession ps = runtime.sessionStore().get(player);
        StealPlayerState.alive(ps, false);
    }

    private List<GameAction> endRoundFromReason(StealModeConfig cfg, StealRoundReason reason) {
        return switch (reason) {
            case BLUE_MINED, RED_ELIMINATED, RED_LEFT -> endRound(cfg, StealTeam.BLUE, reason);
            case RED_TIMEOUT, BLUE_ELIMINATED, BOTH_ELIMINATED, BLUE_LEFT, NO_PARTICIPANTS ->
                    endRound(cfg, StealTeam.RED, reason);
        };
    }

    private List<GameAction> endRound(
            StealModeConfig cfg,
            StealTeam winner,
            StealRoundReason reason
    ) {
        if (state.phase != StealPhase.ROUND_PLAYING) return List.of();
        state.addWin(winner);

        for (var p : onlineParticipants()) {
            p.setGameMode(GameMode.ADVENTURE);
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 12 * 20, 5, true, false, false));
        }

        broadcastReason(reason, winner);
        broadcast(Component.text("♨ 当前比分 ", NamedTextColor.WHITE)
                .append(Component.text(state.redWins, NamedTextColor.RED))
                .append(Component.text(" : ", NamedTextColor.WHITE))
                .append(Component.text(state.blueWins, NamedTextColor.BLUE)));

        boolean finalGame = state.wins(winner) >= cfg.scoreToWin() || state.roundIndex >= cfg.totalRound();
        if (finalGame) {
            return startFinalCelebration(cfg, StealArenaConfig.from(session.arena()), winner, reason);
        }

        state.phase = StealPhase.ROUND_CELEBRATION;
        state.remainingSeconds = cfg.roundCelebrationSeconds();
        for (var p : onlineSessionPlayers()) {
            p.playSound(p, Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        return List.of();
    }

    private List<GameAction> startFinalCelebration(
            StealModeConfig cfg,
            StealArenaConfig arenaCfg,
            StealTeam winner,
            StealRoundReason reason
    ) {
        arenaCfg.setSelectionBarriers(false);
        state.phase = StealPhase.GAME_END_CELEBRATION;
        state.remainingSeconds = cfg.gameEndCelebrationSeconds();
        state.closing = true;

        var title = Component.text(winner.displayNameZh() + "获得了胜利！", winner.color())
                .decorate(TextDecoration.BOLD);
        for (var p : onlineSessionPlayers()) {
            p.setGameMode(GameMode.ADVENTURE);
            p.showTitle(Title.title(title, Component.empty()));
            p.playSound(p, Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.PLAYERS, 0.7f, 1.0f);
        }
        return List.of();
    }

    private void broadcastReason(StealRoundReason reason, StealTeam winner) {
        broadcast(Component.empty());
        switch (reason) {
            case BLUE_MINED -> {
                broadcast(Component.text("♕ 蓝队拆除了足够的红石矿", NamedTextColor.WHITE));
                broadcast(Component.text("♕ 本轮蓝队获得了胜利", NamedTextColor.BLUE));
            }
            case RED_TIMEOUT -> {
                broadcast(Component.text("♕ 时间到，蓝队未能拆除足够的红石矿", NamedTextColor.WHITE));
                broadcast(Component.text("♕ 本轮红队获得了胜利", NamedTextColor.RED));
            }
            case RED_ELIMINATED -> {
                broadcast(Component.text("♕ 红队全部被击杀", NamedTextColor.WHITE));
                broadcast(Component.text("♕ 本轮蓝队获得了胜利！", NamedTextColor.BLUE));
            }
            case BLUE_ELIMINATED -> {
                broadcast(Component.text("♕ 蓝队全部被击杀", NamedTextColor.WHITE));
                broadcast(Component.text("♕ 本轮红队获得了胜利！", NamedTextColor.RED));
            }
            case BOTH_ELIMINATED -> {
                broadcast(Component.text("♕ 双方同时全部死亡！由于蓝队没有拆除足够的红石矿……", NamedTextColor.WHITE));
                broadcast(Component.text("♕ 本轮红队获得了胜利！", NamedTextColor.RED));
            }
            case RED_LEFT, BLUE_LEFT, NO_PARTICIPANTS ->
                    broadcast(Component.text("♕ " + winner.displayNameZh() + "因对方离场获得了胜利", winner.color()));
        }
    }

    private StealRoundReason evaluateEliminationReason() {
        if (state.participants.isEmpty()) return StealRoundReason.NO_PARTICIPANTS;

        int redParticipants = state.participantsOn(StealTeam.RED);
        int blueParticipants = state.participantsOn(StealTeam.BLUE);
        if (redParticipants == 0 && blueParticipants == 0) return StealRoundReason.NO_PARTICIPANTS;
        if (redParticipants == 0) return StealRoundReason.RED_LEFT;
        if (blueParticipants == 0) return StealRoundReason.BLUE_LEFT;

        int red = state.livingOn(StealTeam.RED);
        int blue = state.livingOn(StealTeam.BLUE);
        if (red == 0 && blue == 0) return StealRoundReason.BOTH_ELIMINATED;
        if (red == 0) return StealRoundReason.RED_ELIMINATED;
        if (blue == 0) return StealRoundReason.BLUE_ELIMINATED;
        return null;
    }

    private void lockParticipants() {
        state.participants.clear();
        state.alive.clear();
        state.teams.clear();
        state.mineCooldowns.clear();

        for (var uuid : session.players()) {
            var p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) continue;

            var ps = runtime.sessionStore().getOrCreate(p);
            boolean participant = StealPlayerState.ready(ps);
            StealPlayerState.participant(ps, participant);
            StealPlayerState.alive(ps, participant);
            if (participant) {
                state.participants.add(uuid);
                state.alive.add(uuid);
            }
        }
    }

    private void assignTeamsBalanced() {
        List<Player> red = new ArrayList<>();
        List<Player> blue = new ArrayList<>();
        List<Player> randoms = new ArrayList<>();

        for (var p : onlineParticipants()) {
            var ps = runtime.sessionStore().get(p);
            var selected = StealPlayerState.team(ps);
            if (selected == StealTeam.RED) red.add(p);
            else if (selected == StealTeam.BLUE) blue.add(p);
            else randoms.add(p);
        }

        randoms.sort(Comparator.comparing(Player::getUniqueId));
        for (var p : randoms) {
            if (red.size() < blue.size()) red.add(p);
            else if (blue.size() < red.size()) blue.add(p);
            else if (random.nextBoolean()) red.add(p);
            else blue.add(p);
        }

        rebalance(red, blue);
        red.forEach(p -> applyTeam(p, StealTeam.RED));
        blue.forEach(p -> applyTeam(p, StealTeam.BLUE));
    }

    private void rebalance(List<Player> red, List<Player> blue) {
        while (Math.abs(red.size() - blue.size()) > 1) {
            if (red.size() > blue.size()) {
                blue.add(red.removeLast());
            } else {
                red.add(blue.removeLast());
            }
        }
    }

    private void applyTeam(Player p, StealTeam team) {
        var ps = runtime.sessionStore().getOrCreate(p);
        StealPlayerState.team(ps, team);
        state.setTeam(p.getUniqueId(), team);
        Msg.send(p, Component.text("你加入了", NamedTextColor.WHITE)
                .append(Component.text(team.displayNameZh(), team.color())));
    }

    private int countReadyOnline() {
        int c = 0;
        for (var uuid : session.players()) {
            var p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) continue;
            var s = runtime.sessionStore().get(p);
            if (StealPlayerState.ready(s)) c++;
        }
        return c;
    }

    private List<Player> onlineParticipants() {
        var out = new ArrayList<Player>();
        for (var uuid : state.participants) {
            var p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) out.add(p);
        }
        return out;
    }

    private void warnTime(
            int remaining,
            NamedTextColor color,
            Sound sound,
            float pitch
    ) {
        for (var p : onlineSessionPlayers()) {
            p.playSound(p, sound, SoundCategory.PLAYERS, 1.0f, pitch);
        }
        broadcast(Component.text("还剩" + formatTime(remaining), color));
    }

    private String formatTime(int seconds) {
        int min = Math.max(0, seconds) / 60;
        int sec = Math.max(0, seconds) % 60;
        return min + "分" + (sec == 0 ? "" : "-" + sec + "秒");
    }

    private void launchCelebrationFireworks(int limit) {
        int launched = 0;
        for (var p : onlineParticipants()) {
            if (launched >= limit) return;
            if (p.getGameMode() != GameMode.ADVENTURE && p.getGameMode() != GameMode.SURVIVAL) continue;
            spawnFirework(p.getLocation().clone().add(0, 1.5, 0));
            launched++;
        }
    }

    private void spawnFirework(Location location) {
        var world = location.getWorld();
        if (world == null) return;
        var firework = world.spawn(location, Firework.class, fw -> {
            var meta = fw.getFireworkMeta();
            meta.setPower(1);
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.CREEPER)
                    .withColor(Color.fromRGB(random.nextInt(0x1000000)))
                    .withColor(Color.fromRGB(random.nextInt(0x1000000)))
                    .withFade(Color.fromRGB(random.nextInt(0x1000000)))
                    .flicker(true)
                    .trail(true)
                    .build());
            fw.setFireworkMeta(meta);
        });
        firework.setTicksToDetonate(10);
    }

    private void cleanupWholeGame() {
        for (var p : onlineSessionPlayers()) {
            var ps = runtime.sessionStore().get(p);
            StealPlayerState.clear(ps);
            p.getInventory().clear();
            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.WEAKNESS,
                    3 * 20,
                    255,
                    true,
                    false,
                    false
            ));
            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.INSTANT_HEALTH,
                    20,
                    14,
                    true,
                    false,
                    false
            ));
        }
        state.bossBars.hideAllTracked();
        state.resetWholeGame();
    }

}
