package top.ourisland.creepersiarena.game.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.death.DeathResolutionService;
import top.ourisland.creepersiarena.game.flow.GameFlow;

public final class ArenaDeathListener implements Listener {

    private final PlayerSessionStore store;
    private final DeathResolutionService deathResolutionService;
    private final GameFlow flow;

    public ArenaDeathListener(
            @lombok.NonNull PlayerSessionStore store,
            @lombok.NonNull DeathResolutionService deathResolutionService,
            @lombok.NonNull GameFlow flow
    ) {
        this.store = store;
        this.deathResolutionService = deathResolutionService;
        this.flow = flow;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        deathResolutionService.handleDeath(event);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        var session = store.get(player);
        if (session == null) return;

        flow.onPlayerRespawnEvent(player, event::setRespawnLocation);
    }

}
