package top.ourisland.creepersiarena.game.mode;

import top.ourisland.creepersiarena.core.component.metadata.ModeMetadata;
import top.ourisland.creepersiarena.game.GameSession;

public interface IGameMode {

    default GameModeType mode() {
        return ModeMetadata.of(getClass()).id();
    }

    default boolean enabled() {
        return ModeMetadata.of(getClass()).enabledByDefault();
    }

    ModeLogic createLogic(GameSession session, GameRuntime runtime);

}
