package top.ourisland.creepersiarena.api.game.death;

import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.skill.SkillId;

import java.util.UUID;

public record DeathAttribution(
        @lombok.NonNull DeathCauseId causeId,
        @Nullable UUID attackerId,
        @Nullable UUID victimId,
        boolean selfInflicted,
        boolean friendlyFire,
        @Nullable SkillId sourceSkillId,
        @lombok.NonNull EntityDamageEvent.DamageCause bukkitCause,
        long tick
) {

}
