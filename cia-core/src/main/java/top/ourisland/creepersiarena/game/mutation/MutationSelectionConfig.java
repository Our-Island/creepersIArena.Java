package top.ourisland.creepersiarena.game.mutation;

public record MutationSelectionConfig(
        int onlineRollMin,
        int onlineRollMax,
        int offlineRollMin,
        int offlineRollMax,
        int startMinInclusive
) {

    public MutationSelectionConfig {
        if (onlineRollMax < onlineRollMin) {
            int tmp = onlineRollMin;
            onlineRollMin = onlineRollMax;
            onlineRollMax = tmp;
        }
        if (offlineRollMax < offlineRollMin) {
            int tmp = offlineRollMin;
            offlineRollMin = offlineRollMax;
            offlineRollMax = tmp;
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
