package top.ourisland.creepersiarena.game.economy;

import top.ourisland.creepersiarena.api.economy.ICurrency;

public record RegisteredCurrency(
        String ownerId,
        ICurrency value
) {

}
