package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.api.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModeRules;
import top.ourisland.creepersiarena.api.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.api.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.api.game.mode.context.RespawnContext;
import top.ourisland.creepersiarena.utils.Msg;

final class StealRules implements IModeRules {

    private final GameRuntime runtime;
    private final GameSession game;
    private final StealState state;

    StealRules(GameRuntime runtime, GameSession game, StealState state) {
        this.runtime = runtime;
        this.game = game;
        this.state = state;
    }

    @Override
    public GameModeType type() {
        return GameModeType.of("steal");
    }

    @Override
    public JoinDecision onJoin(JoinContext ctx) {
        return switch (state.phase) {
            case LOBBY, START_COUNTDOWN -> joinAsReady(ctx);
            case SPECTATOR_TOUR, CHOOSE_JOB, ROUND_PLAYING, ROUND_CELEBRATION, GAME_END_CELEBRATION -> {
                Location view = game.arena().anchor().clone().add(0, 8, 0);
                yield new JoinDecision.ToSpectate(view);
            }
        };
    }

    private JoinDecision joinAsReady(JoinContext ctx) {
        if (!ctx.fromHubRequest()) {
            return new JoinDecision.ToHub();
        }

        StealPlayerState.ready(ctx.session(), true);
        StealPlayerState.participant(ctx.session(), false);
        StealPlayerState.alive(ctx.session(), false);

        ctx.player().playSound(ctx.player(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 2.0f);
        Msg.actionBar(ctx.player(), Component.text("✪ 你已准备加入偷窃模式", NamedTextColor.GREEN));
        return new JoinDecision.AttachToHub();
    }

    @Override
    public void onLeave(LeaveContext ctx) {
        StealPlayerState.clear(ctx.session());
        state.removeParticipant(ctx.player().getUniqueId());
    }

    @Override
    public RespawnDecision onRespawn(RespawnContext ctx) {
        Location view = game.arena().anchor().clone().add(0, 8, 0);
        return new RespawnDecision.Spectate(view);
    }

}
