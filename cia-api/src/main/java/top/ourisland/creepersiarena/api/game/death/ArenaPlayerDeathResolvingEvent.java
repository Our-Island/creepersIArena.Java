package top.ourisland.creepersiarena.api.game.death;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Fired after CIA resolves the semantic death result but before cleanup, stats, messages and mode flow run.
 * <p>
 * Use this event when a feature needs pre-cleanup inventory/effect/player-state visibility. Use
 * {@link ArenaPlayerDeathResolvedEvent} for post-pipeline notifications.
 */
public final class ArenaPlayerDeathResolvingEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter private final Player victim;
    @Getter private final @Nullable Player killer;
    @Getter private final DeathResult result;

    public ArenaPlayerDeathResolvingEvent(
            @lombok.NonNull Player victim,
            @Nullable Player killer,
            @lombok.NonNull DeathResult result
    ) {
        this.victim = victim;
        this.killer = killer;
        this.result = result;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
