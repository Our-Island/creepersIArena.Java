package top.ourisland.creepersiarena.core.command.handler;

import lombok.Getter;
import top.ourisland.creepersiarena.core.command.handler.admin.*;

/**
 * Lightweight holder for admin command handlers. It intentionally contains no business logic; each command domain owns
 * its own handler class.
 */
@Getter
public final class AdminHandlers {

    private final AdminSystemHandlers system;
    private final GameAdminHandlers game;
    private final AbilityAdminHandlers ability;
    private final EconomyAdminHandlers economy;
    private final StoreAdminHandlers store;
    private final ExtensionAdminHandlers extension;
    private final ConfigAdminHandlers config;
    private final DatabaseAdminHandlers database;

    public AdminHandlers(CommandHandlerContext context) {
        this.system = new AdminSystemHandlers(context);
        this.game = new GameAdminHandlers(context);
        this.ability = new AbilityAdminHandlers(context);
        this.economy = new EconomyAdminHandlers(context);
        this.store = new StoreAdminHandlers(context);
        this.extension = new ExtensionAdminHandlers(context);
        this.config = new ConfigAdminHandlers(context);
        this.database = new DatabaseAdminHandlers(context);
    }

}
