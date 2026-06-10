package top.ourisland.creepersiarena.defaultcontent.death;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.game.death.IDeathCleanupParticipant;
import top.ourisland.creepersiarena.api.skill.runtime.ISkillStateStore;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;
import top.ourisland.creepersiarena.job.utils.BuiltinKeys;
import top.ourisland.creepersiarena.utils.AttributeUtils;

import java.util.List;
import java.util.UUID;

public final class BuiltinDeathCleanupParticipant implements IDeathCleanupParticipant {

    private static final String
            CREEPER_EXPLOSION_TAG = "cia_skill_creeper_boom",
            CREEPER_FIREWORK_TAG = "cia_skill3_fw",
            CREEPER_FIREWORK_OWNER_TAG_PREFIX = "cia_skill3_owner:",
            LEGACY_NO_PARTICLE_TAG = "no_particle";

    private static final List<String> BUILTIN_SKILL_IDS = List.of(
            "cia:avenger.blood_blink",
            "cia:avenger.revenge_grasp",
            "cia:bloodline.blood_orb",
            "cia:bloodline.leap",
            "cia:bloodline.sprint",
            "cia:creeper.creeper",
            "cia:creeper.crossbow",
            "cia:creeper.fireworks",
            "cia:golem.stoneform",
            "cia:golem.rift_fangs",
            "cia:moison.blowgun",
            "cia:moison.volley",
            "cia:moison.shadowstep",
            "cia:moison.spectral_reserve",
            "cia:wolong.fan_dash",
            "cia:wolong.sky_lantern",
            "cia:wolong.repeating_crossbow",
            "cia:wolong.empty_fort",
            "cia:ysahan.pumpkin_trick",
            "cia:ysahan.whale",
            "cia:ysahan.it_was_me"
    );

    private final ISkillStateStore skillStateStore;

    public BuiltinDeathCleanupParticipant(ISkillStateStore skillStateStore) {
        this.skillStateStore = skillStateStore;
    }

    @Override
    public AbilityId abilityId() {
        return DefaultContentAbilities.BUILTIN_DEATH_CLEANUP;
    }

    @Override
    public void cleanupAfterDeath(Player player) {
        BuiltinDamageAttributionMarker.clearNextDamage(player);
        if (player == null) return;

        clearPlayerState(player);
        clearBuiltinSkillCooldowns(player.getUniqueId());
        cleanupOwnedSkillEntities(player);
    }

    private void clearPlayerState(Player player) {
        var container = player.getPersistentDataContainer();
        container.remove(key("avenger_armor_until"));
        container.remove(key("golem_last_target"));
        container.remove(key("ysahan_whale_until"));
        container.remove(key("ysahan_whale_task"));
        AttributeUtils.setBaseValue(
                player,
                1.0D,
                "generic.scale"
        );
        player.removeScoreboardTag(LEGACY_NO_PARTICLE_TAG);
    }

    private void clearBuiltinSkillCooldowns(UUID playerId) {
        if (skillStateStore == null || playerId == null) return;

        for (String skillId : BUILTIN_SKILL_IDS) {
            skillStateStore.cooldownEndsAtTick(playerId, skillId, 0L);
        }
    }

    private void cleanupOwnedSkillEntities(Player player) {
        var playerId = player.getUniqueId();
        var playerIdRaw = playerId.toString();
        for (var entity : player.getWorld().getEntities()) {
            if (entity instanceof Player) continue;
            if (!isOwnedBuiltinSkillEntity(entity, playerIdRaw)) continue;

            clearEntityState(entity, playerIdRaw);
            entity.remove();
        }
    }

    private static NamespacedKey key(String value) {
        return BuiltinKeys.key(value);
    }

    private boolean isOwnedBuiltinSkillEntity(Entity entity, String playerIdRaw) {
        var container = entity.getPersistentDataContainer();
        String deathOwner = container.get(key("death_source_owner"), PersistentDataType.STRING);
        String moisonOwner = container.get(key("moison_owner"), PersistentDataType.STRING);
        if (playerIdRaw.equals(deathOwner) || playerIdRaw.equals(moisonOwner)) return true;

        return entity.getScoreboardTags().stream()
                .anyMatch(tag -> tag.equals(CREEPER_FIREWORK_OWNER_TAG_PREFIX + playerIdRaw));
    }

    private void clearEntityState(Entity entity, String playerIdRaw) {
        var container = entity.getPersistentDataContainer();
        container.remove(key("death_next_damage"));
        container.remove(key("death_source_owner"));
        container.remove(key("death_cause_id"));
        container.remove(key("death_source_skill"));
        container.remove(key("moison_owner"));
        container.remove(key("moison_source"));
        container.remove(key("moison_spectral"));

        entity.removeScoreboardTag(CREEPER_EXPLOSION_TAG);
        entity.removeScoreboardTag(CREEPER_FIREWORK_TAG);
        entity.removeScoreboardTag(CREEPER_FIREWORK_OWNER_TAG_PREFIX + playerIdRaw);
    }

}
