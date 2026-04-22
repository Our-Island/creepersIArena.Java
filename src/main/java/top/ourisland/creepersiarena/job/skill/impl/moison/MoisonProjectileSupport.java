package top.ourisland.creepersiarena.job.skill.impl.moison;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import top.ourisland.creepersiarena.job.skill.runtime.SkillStateStore;
import top.ourisland.creepersiarena.job.utils.BuiltinKeys;

final class MoisonProjectileSupport {

    private MoisonProjectileSupport() {
    }

    static boolean consumeSpectralReserve(
            SkillStateStore store,
            Player player,
            long nowTick
    ) {
        if (store.isCoolingDown(player.getUniqueId(), Skill1.PASSIVE_ID, nowTick)) {
            return false;
        }
        store.cooldownEndsAtTick(player.getUniqueId(), Skill1.PASSIVE_ID, nowTick + 12L * 20L);
        return true;
    }

    static void shoot(
            Player player,
            String sourceId,
            boolean spectral,
            Vector direction,
            double speed,
            double damage
    ) {
        var location = player.getEyeLocation().add(direction.clone().multiply(0.6));
        if (spectral) {
            var arrow = player.getWorld().spawn(location, SpectralArrow.class, spawned -> {
                spawned.setShooter(player);
                spawned.setVelocity(direction.clone().multiply(speed));
                spawned.setPierceLevel(0);
                spawned.setCritical(false);
                spawned.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                spawned.getPersistentDataContainer().set(
                        BuiltinKeys.key("moison_owner"),
                        PersistentDataType.STRING,
                        player.getUniqueId().toString()
                );
                spawned.getPersistentDataContainer().set(
                        BuiltinKeys.key("moison_source"),
                        PersistentDataType.STRING,
                        sourceId
                );
                spawned.getPersistentDataContainer().set(
                        BuiltinKeys.key("moison_spectral"),
                        PersistentDataType.BYTE,
                        (byte) 1
                );
            });
            arrow.setDamage(damage);
            return;
        }

        var arrow = player.getWorld().spawn(location, Arrow.class, spawned -> {
            spawned.setShooter(player);
            spawned.setVelocity(direction.clone().multiply(speed));
            spawned.setPierceLevel(0);
            spawned.setCritical(false);
            spawned.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            spawned.getPersistentDataContainer().set(
                    BuiltinKeys.key("moison_owner"),
                    PersistentDataType.STRING,
                    player.getUniqueId().toString()
            );
            spawned.getPersistentDataContainer().set(
                    BuiltinKeys.key("moison_source"),
                    PersistentDataType.STRING,
                    sourceId
            );
        });
        arrow.setDamage(damage);
    }

}
