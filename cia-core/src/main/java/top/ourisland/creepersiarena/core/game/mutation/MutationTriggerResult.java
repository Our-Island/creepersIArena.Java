package top.ourisland.creepersiarena.core.game.mutation;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.game.mutation.MutationType;

public record MutationTriggerResult(
        MutationTriggerResult.Kind kind,
        MutationType type,
        String message
) {

    public static @NonNull MutationTriggerResult started(MutationType type) {
        return new MutationTriggerResult(
                Kind.STARTED,
                type,
                "Started mutation: " + type
        );
    }

    public static @NonNull MutationTriggerResult cancelled(MutationType type) {
        return new MutationTriggerResult(
                Kind.CANCELLED,
                type,
                "Cancelled mutation: " + type
        );
    }

    public static @NonNull MutationTriggerResult disabled() {
        return new MutationTriggerResult(
                Kind.DISABLED,
                MutationType.NONE,
                "Mutation is disabled."
        );
    }

    public static @NonNull MutationTriggerResult noEligibleGame() {
        return new MutationTriggerResult(
                Kind.NO_ELIGIBLE_GAME,
                MutationType.NONE,
                "No eligible active game for mutation."
        );
    }

    public static @NonNull MutationTriggerResult noEffect() {
        return new MutationTriggerResult(
                Kind.NO_EFFECT,
                MutationType.NONE,
                "No enabled mutation effect is available."
        );
    }

    public static @NonNull MutationTriggerResult failed(MutationType type, String message) {
        return new MutationTriggerResult(
                Kind.FAILED,
                type == null
                        ? MutationType.NONE
                        : type,
                message
        );
    }

    public enum Kind {

        STARTED,
        CANCELLED,
        DISABLED,
        NO_ELIGIBLE_GAME,
        NO_EFFECT,
        FAILED

    }

}
