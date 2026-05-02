package top.ourisland.creepersiarena.core.component.metadata;

import top.ourisland.creepersiarena.core.component.annotation.CiaModeDef;
import top.ourisland.creepersiarena.game.mode.GameModeType;

public record ModeMetadata(
        GameModeType id,
        boolean enabledByDefault
) {

    public static ModeMetadata of(Class<?> type) {
        var ann = type.getAnnotation(CiaModeDef.class);
        if (ann == null) {
            throw new IllegalStateException("Missing @CiaModeDef on " + type.getName());
        }
        return new ModeMetadata(GameModeType.of(ann.id()), ann.enabledByDefault());
    }

}
