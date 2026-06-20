package top.ourisland.creepersiarena.core.game.death;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.death.DeathResult;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.utils.Msg;

import java.util.Optional;

public final class DeathMessageService {

    private final Logger log;
    private final DeathResolutionRegistry registry;
    private final GameManager gameManager;
    private final IAbilityGate abilities;

    public DeathMessageService(
            @lombok.NonNull Logger log,
            @lombok.NonNull DeathResolutionRegistry registry,
            @lombok.NonNull GameManager gameManager,
            @lombok.NonNull IAbilityGate abilities
    ) {
        this.log = log;
        this.registry = registry;
        this.gameManager = gameManager;
        this.abilities = abilities;
    }

    public void broadcast(@lombok.NonNull DeathResult result) {
        if (!abilities.isEnabled(CoreAbilities.DEATH_MESSAGES, result.victim(), "death_message")) return;

        Optional<Component> message = buildMessage(result);
        if (message.isEmpty()) return;

        var game = gameManager.active();
        if (game == null) return;

        game.players().stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .forEach(player -> Msg.send(player, message.get()));
    }

    private Optional<Component> buildMessage(DeathResult result) {
        for (var registered : registry.messageProviders()) {
            try {
                Optional<Component> message = registered.value().buildMessage(result);
                if (message.isPresent()) return message;
            } catch (Throwable throwable) {
                log.warn(
                        "[Death] message provider failed: owner={} err={}",
                        registered.owner(),
                        throwable.getMessage(),
                        throwable
                );
            }
        }
        return Optional.empty();
    }

}
