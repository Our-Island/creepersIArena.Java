package top.ourisland.creepersiarena.core.game.mutation;

public record MutationSelectionConfig(
        int onlineRollMin,
        int onlineRollMax,
        int offlineRollMin,
        int offlineRollMax,
        int startMinInclusive
) {

    public MutationSelectionConfig {
        if (onlineRollMax < onlineRollMin) {
            throw new IllegalArgumentException("online-roll-max must be >= online-roll-min");
        }
        if (offlineRollMax < offlineRollMin) {
            throw new IllegalArgumentException("offline-roll-max must be >= offline-roll-min");
        }
        if (startMinInclusive < 0) {
            throw new IllegalArgumentException("start-min-inclusive must be >= 0");
        }
    }

    public static MutationSelectionConfig defaults() {
        return new MutationSelectionConfig(
                -5,
                10,
                -5,
                0,
                1
        );
    }

}
