package top.ourisland.creepersiarena.core.game.death;

import org.bukkit.event.entity.EntityDamageEvent;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.api.game.death.StandardDeathCauses;

public final class CoreDeathCauseMapper {

    private CoreDeathCauseMapper() {
    }

    public static DeathCauseId fromDamageCause(EntityDamageEvent.DamageCause damageCause) {
        if (damageCause == null) return StandardDeathCauses.GENERIC;

        return switch (damageCause) {
            case CONTACT -> StandardDeathCauses.CONTACT;
            case VOID -> StandardDeathCauses.VOID;
            case FIRE,
                 FIRE_TICK,
                 LAVA,
                 HOT_FLOOR -> StandardDeathCauses.FIRE;
            case FALL -> StandardDeathCauses.FALL;
            case DROWNING -> StandardDeathCauses.DROWNING;
            case ENTITY_ATTACK,
                 ENTITY_SWEEP_ATTACK,
                 PROJECTILE,
                 ENTITY_EXPLOSION,
                 BLOCK_EXPLOSION,
                 SONIC_BOOM,
                 MAGIC,
                 THORNS -> StandardDeathCauses.DIRECT_HIT;
            default -> StandardDeathCauses.GENERIC;
        };
    }

    public static boolean isExplicitEnvironmentalDamage(EntityDamageEvent.DamageCause damageCause) {
        if (damageCause == null) return true;

        return switch (damageCause) {
            case ENTITY_ATTACK,
                 ENTITY_SWEEP_ATTACK,
                 PROJECTILE,
                 ENTITY_EXPLOSION,
                 BLOCK_EXPLOSION,
                 MAGIC,
                 SONIC_BOOM,
                 THORNS -> false;
            default -> true;
        };
    }

}
