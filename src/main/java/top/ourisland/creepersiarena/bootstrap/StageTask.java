package top.ourisland.creepersiarena.bootstrap;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Accessors(fluent = true)
public final class StageTask {
    private final String beginMessage;
    private final Runnable action;
    private final String endMessage;

    private StageTask(Runnable action, String beginMessage, String endMessage) {
        this.action = action;
        this.beginMessage = beginMessage;
        this.endMessage = endMessage;
    }

    public static StageTask of(@NotNull Runnable action, @Nullable String beginMessage, @Nullable String endMessage) {
        return new StageTask(action, beginMessage, endMessage);
    }
}
