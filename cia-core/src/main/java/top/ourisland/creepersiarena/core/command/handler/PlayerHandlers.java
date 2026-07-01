package top.ourisland.creepersiarena.core.command.handler;

import lombok.Getter;
import top.ourisland.creepersiarena.core.command.handler.player.*;

/**
 * Lightweight holder for player command handlers. It intentionally contains no business logic; each player command
 * domain owns its own handler class.
 */
@Getter
public final class PlayerHandlers {

    private final PlayerHelpHandlers help;
    private final PlayerGameHandlers game;
    private final PlayerPreferenceHandlers preference;
    private final PlayerEconomyHandlers economy;
    private final PlayerStoreHandlers store;
    private final PlayerCosmeticHandlers cosmetic;

    public PlayerHandlers(CommandHandlerContext context) {
        this.help = new PlayerHelpHandlers(context);
        this.game = new PlayerGameHandlers(context);
        this.preference = new PlayerPreferenceHandlers(context);
        this.economy = new PlayerEconomyHandlers(context);
        this.store = new PlayerStoreHandlers(context);
        this.cosmetic = new PlayerCosmeticHandlers(context);
    }

}
