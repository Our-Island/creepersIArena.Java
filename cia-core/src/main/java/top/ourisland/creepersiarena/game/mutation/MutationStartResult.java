package top.ourisland.creepersiarena.game.mutation;

public record MutationStartResult(
        int durationTicks
) {

    public MutationStartResult {
        durationTicks = Math.max(1, durationTicks);
    }

}
