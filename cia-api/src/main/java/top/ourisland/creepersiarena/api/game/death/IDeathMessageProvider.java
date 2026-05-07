package top.ourisland.creepersiarena.api.game.death;

import net.kyori.adventure.text.Component;

import java.util.Optional;

public interface IDeathMessageProvider {

    Optional<Component> buildMessage(DeathResult result);

}
