package top.ourisland.creepersiarena.api.metadata;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;

public record ModeMetadata(
        GameModeId id,
        boolean enabledByDefault
) {

    public static @NonNull ModeMetadata of(@NonNull Class<?> type) {
        var ann = type.getAnnotation(CiaModeDef.class);
        if (ann == null) {
            throw new IllegalStateException("Missing @CiaModeDef on " + type.getName());
        }
        return new ModeMetadata(GameModeId.parse(ann.id()), ann.enabledByDefault());
    }

}
