package top.ourisland.creepersiarena.game.death;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.death.DeathResult;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.Optional;

public final class DeathMessageService {

    private final Logger log;
    private final DeathCauseRegistry registry;
    private final GameManager gameManager;

    public DeathMessageService(
            @lombok.NonNull Logger log,
            @lombok.NonNull DeathCauseRegistry registry,
            @lombok.NonNull GameManager gameManager
    ) {
        this.log = log;
        this.registry = registry;
        this.gameManager = gameManager;
    }

    public void broadcast(@lombok.NonNull DeathResult result) {
        Optional<Component> message = buildMessage(result);
        if (message.isEmpty()) return;

        GameSession game = gameManager.active();
        if (game == null) return;

        for (var playerId : game.players()) {
            var player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                Msg.send(player, message.get());
            }
        }
    }

    private Optional<Component> buildMessage(DeathResult result) {
        for (var registered : registry.messageProviders()) {
            try {
                Optional<Component> message = registered.value().buildMessage(result);
                if (message.isPresent()) return message;
            } catch (Throwable throwable) {
                log.warn(
                        "[Death] message provider failed: owner={} err={}",
                        registered.ownerId(),
                        throwable.getMessage(),
                        throwable
                );
            }
        }
        return Optional.empty();
    }

}
