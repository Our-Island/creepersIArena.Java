package top.ourisland.creepersiarena.defaultcontent.cosmetic.particle;

import org.bukkit.Color;
import org.bukkit.Particle;
import top.ourisland.creepersiarena.api.cosmetic.ParticleCosmeticContext;

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
        if (raw == null || raw.isBlank()) return null;
        String name = raw.trim().toUpperCase(Locale.ROOT)
                .replace("MINECRAFT:", "")
                .replace('-', '_')
                .replace(' ', '_');
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException _) {
            return null;
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
