package top.ourisland.creepersiarena.api.economy.cosmetic;

public record ParticleSchedule(
        int intervalTicks
) {

    public ParticleSchedule {
        intervalTicks = Math.max(1, intervalTicks);
    }

}
