package top.ourisland.creepersiarena.game.death;

import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageEvent;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;

public final class CoreDeathCauseIds {

    public static final DeathCauseId GENERIC = key("accident/generic");
    public static final DeathCauseId DIRECT_HIT = key("accident/direct_hit");
    public static final DeathCauseId VOID = key("accident/void");
    public static final DeathCauseId FIRE = key("accident/fire");
    public static final DeathCauseId FALL = key("accident/fall");
    public static final DeathCauseId DROWNING = key("accident/drowning");

    private CoreDeathCauseIds() {
    }

    public static DeathCauseId fromDamageCause(EntityDamageEvent.DamageCause damageCause) {
        if (damageCause == null) return GENERIC;

        return switch (damageCause) {
            case VOID -> VOID;
            case FIRE,
                 FIRE_TICK,
                 LAVA,
                 HOT_FLOOR -> FIRE;
            case FALL -> FALL;
            case DROWNING -> DROWNING;
            case ENTITY_ATTACK,
                 ENTITY_SWEEP_ATTACK,
                 PROJECTILE,
                 ENTITY_EXPLOSION,
                 SONIC_BOOM,
                 MAGIC,
                 THORNS -> DIRECT_HIT;
            default -> GENERIC;
        };
    }

    private static DeathCauseId key(String path) {
        return new DeathCauseId(new NamespacedKey("cia", path));
    }

}
