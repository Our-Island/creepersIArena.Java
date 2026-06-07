package top.ourisland.creepersiarena.game.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.ourisland.creepersiarena.api.game.event.ArenaPlayerDeathResolvedEvent;
import top.ourisland.creepersiarena.game.mutation.MutationService;

public final class MutationListener implements Listener {

    private final MutationService mutation;

    public MutationListener(MutationService mutation) {
        this.mutation = mutation;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeathResolved(ArenaPlayerDeathResolvedEvent event) {
        mutation.nudgeFromDeath();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        mutation.clearPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent event) {
        mutation.clearPlayer(event.getPlayer());
    }

}
