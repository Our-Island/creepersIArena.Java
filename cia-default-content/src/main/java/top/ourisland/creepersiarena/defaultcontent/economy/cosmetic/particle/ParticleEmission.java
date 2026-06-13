package top.ourisland.creepersiarena.defaultcontent.economy.cosmetic.particle;

import org.bukkit.Color;
import org.bukkit.Particle;
import top.ourisland.creepersiarena.api.economy.cosmetic.ParticleCosmeticContext;

import java.util.Locale;

public record ParticleEmission(
        Particle particle,
        double relativeX,
        double relativeY,
        double relativeZ,
        double offsetX,
        double offsetY,
        double offsetZ,
        double speed,
        int count,
        boolean force,
        boolean randomColor,
        Color dustColor,
        float dustScale
) {

    public static Particle particle(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Particle id must be a non-blank minecraft namespaced id");
        }
        if (!raw.startsWith("minecraft:")) {
            throw new IllegalArgumentException("Particle id must use the minecraft namespace: " + raw);
        }

        var path = raw.substring("minecraft:".length());
        if (!path.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid particle id: " + raw);
        }
        try {
            return Particle.valueOf(path.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown particle id: " + raw, exception);
        }
    }

    public void spawn(ParticleCosmeticContext context) {
        if (particle == null || context == null || context.origin() == null) return;
        var location = context.origin().clone().add(relativeX, relativeY, relativeZ);
        Object data = data(context);
        context.viewers().stream()
                .filter(viewer -> viewer != null && viewer.isOnline())
                .forEach(viewer -> viewer.spawnParticle(
                        particle,
                        location,
                        count,
                        offsetX,
                        offsetY,
                        offsetZ,
                        speed,
                        data,
                        force));
    }

    private Object data(ParticleCosmeticContext context) {
        if (particle == Particle.DUST) {
            return new Particle.DustOptions(dustColor == null ? Color.BLACK : dustColor, dustScale <= 0.0F
                    ? 1.0F
                    : dustScale);
        }
        if (randomColor && particle == Particle.ENTITY_EFFECT) {
            int red = 1 + context.random().nextInt(255);
            int green = 1 + context.random().nextInt(255);
            int blue = 1 + context.random().nextInt(255);
            return Color.fromRGB(red, green, blue);
        }
        return null;
    }

}
