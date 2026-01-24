package top.ourisland.creepersiarena.bootstrap;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Describes a unit of work for a single bootstrap stage (install/start/stop/reload).
 *
 * <p>The bootstrapper is responsible for:
 * <ul>
 *   <li>Printing the beginning message (if present).</li>
 *   <li>Executing {@link #action()}.</li>
 *   <li>Printing the end message (if present).</li>
 * </ul>
 *
 * <p>A {@code null} {@link StageTask} returned by a {@link Module} stage method indicates that
 * the stage is skipped and no stage logs should be printed for that module.</p>
 */
@Getter
public final class StageTask {
    /**
     * Optional begin message printed before executing {@link #action}.
     */
    private final String beginMessage;

    /**
     * The actual business action to run for this stage.
     */
    private final Runnable action;

    /**
     * Optional end message printed after executing {@link #action}.
     */
    private final String endMessage;

    private StageTask(
            @NonNull Runnable action,
            @Nullable String beginMessage,
            @Nullable String endMessage
    ) {
        this.action = action;
        this.beginMessage = beginMessage;
        this.endMessage = endMessage;
    }

    /**
     * Creates a new stage task.
     *
     * @param action       the action to execute (required)
     * @param beginMessage optional begin message (nullable/blank allowed)
     * @param endMessage   optional end message (nullable/blank allowed)
     * @return a new {@link StageTask}
     */
    public static StageTask of(
            @NotNull Runnable action,
            @Nullable String beginMessage,
            @Nullable String endMessage
    ) {
        return new StageTask(action, beginMessage, endMessage);
    }
}
