package top.ourisland.creepersiarena.core.game.mutation;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.game.mutation.MutationId;

public record MutationTriggerResult(
        MutationTriggerResult.Kind kind,
        MutationId type,
        String message
) {

    public static @NonNull MutationTriggerResult started(MutationId type) {
        return new MutationTriggerResult(
                Kind.STARTED,
                type,
                "Started mutation: " + type
        );
    }

    public static @NonNull MutationTriggerResult cancelled(MutationId type) {
        return new MutationTriggerResult(
                Kind.CANCELLED,
                type,
                "Cancelled mutation: " + type
        );
    }

    public static @NonNull MutationTriggerResult disabled() {
        return new MutationTriggerResult(
                Kind.DISABLED,
                MutationId.NONE,
                "Mutation is disabled."
        );
    }

    public static @NonNull MutationTriggerResult noEligibleGame() {
        return new MutationTriggerResult(
                Kind.NO_ELIGIBLE_GAME,
                MutationId.NONE,
                "No eligible active game for mutation."
        );
    }

    public static @NonNull MutationTriggerResult noEffect() {
        return new MutationTriggerResult(
                Kind.NO_EFFECT,
                MutationId.NONE,
                "No enabled mutation effect is available."
        );
    }

    public static @NonNull MutationTriggerResult failed(MutationId type, String message) {
        return new MutationTriggerResult(
                Kind.FAILED,
                type == null
                        ? MutationId.NONE
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
