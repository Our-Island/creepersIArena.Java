package top.ourisland.creepersiarena.api.game.death;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ArenaPlayerDeathResolvedEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player victim;
    private final @Nullable Player killer;
    private final DeathResult result;

    public ArenaPlayerDeathResolvedEvent(
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

    public Player victim() {
        return victim;
    }

    public @Nullable Player killer() {
        return killer;
    }

    public DeathResult result() {
        return result;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
