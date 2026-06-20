package top.ourisland.creepersiarena.defaultcontent.game.death;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.game.death.IDeathCleanupParticipant;
import top.ourisland.creepersiarena.api.skill.SkillId;
import top.ourisland.creepersiarena.api.skill.runtime.ISkillStateStore;
import top.ourisland.creepersiarena.core.utils.AttributeUtils;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;
import top.ourisland.creepersiarena.defaultcontent.DefaultSkillIds;
import top.ourisland.creepersiarena.defaultcontent.job.utils.BuiltinKeys;

import java.util.List;
import java.util.UUID;

public final class BuiltinDeathCleanupParticipant implements IDeathCleanupParticipant {

    private static final String
            CREEPER_EXPLOSION_TAG = "cia_skill_creeper_boom",
            CREEPER_FIREWORK_TAG = "cia_skill3_fw",
            CREEPER_FIREWORK_OWNER_TAG_PREFIX = "cia_skill3_owner:";

    private static final List<SkillId> BUILTIN_SKILL_IDS = List.of(
            DefaultSkillIds.AVENGER_BLOOD_BLINK,
            DefaultSkillIds.AVENGER_REVENGE_GRASP,
            DefaultSkillIds.BLOODLINE_BLOOD_ORB,
            DefaultSkillIds.BLOODLINE_LEAP,
            DefaultSkillIds.BLOODLINE_SPRINT,
            DefaultSkillIds.CREEPER_CREEPER,
            DefaultSkillIds.CREEPER_CROSSBOW,
            DefaultSkillIds.CREEPER_FIREWORKS,
            DefaultSkillIds.GOLEM_STONEFORM,
            DefaultSkillIds.GOLEM_RIFT_FANGS,
            DefaultSkillIds.MOISON_BLOWGUN,
            DefaultSkillIds.MOISON_VOLLEY,
            DefaultSkillIds.MOISON_SHADOWSTEP,
            DefaultSkillIds.MOISON_SPECTRAL_RESERVE,
            DefaultSkillIds.WOLONG_FAN_DASH,
            DefaultSkillIds.WOLONG_SKY_LANTERN,
            DefaultSkillIds.WOLONG_REPEATING_CROSSBOW,
            DefaultSkillIds.WOLONG_EMPTY_FORT,
            DefaultSkillIds.YSAHAN_PUMPKIN_TRICK,
            DefaultSkillIds.YSAHAN_WHALE,
            DefaultSkillIds.YSAHAN_IT_WAS_ME
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
        AttributeUtils.setBaseValue(player, 1.0D, Attribute.SCALE);
    }

    private void clearBuiltinSkillCooldowns(UUID playerId) {
        if (skillStateStore == null || playerId == null) return;

        BUILTIN_SKILL_IDS.forEach(skillId ->
                skillStateStore.cooldownEndsAtTick(playerId, skillId, 0L)
        );
    }

    private void cleanupOwnedSkillEntities(Player player) {
        var playerId = player.getUniqueId();
        var playerIdRaw = playerId.toString();
        player.getWorld().getEntities().stream()
                .filter(entity -> !(entity instanceof Player))
                .filter(entity -> isOwnedBuiltinSkillEntity(entity, playerIdRaw))
                .forEach(entity -> {
                    clearEntityState(entity, playerIdRaw);
                    entity.remove();
                });
    }

    private static NamespacedKey key(String value) {
        return BuiltinKeys.key(value);
    }

    private boolean isOwnedBuiltinSkillEntity(Entity entity, String playerIdRaw) {
        var container = entity.getPersistentDataContainer();
        var deathOwner = container.get(key("death_source_owner"), PersistentDataType.STRING);
        if (playerIdRaw.equals(deathOwner)) return true;

        return entity.getScoreboardTags().stream()
                .anyMatch(tag -> tag.equals(CREEPER_FIREWORK_OWNER_TAG_PREFIX + playerIdRaw));
    }

    private void clearEntityState(Entity entity, String playerIdRaw) {
        var container = entity.getPersistentDataContainer();
        container.remove(key("death_next_damage"));
        container.remove(key("death_source_owner"));
        container.remove(key("death_cause_id"));
        container.remove(key("death_source_skill"));
        container.remove(key("moison_spectral"));

        entity.removeScoreboardTag(CREEPER_EXPLOSION_TAG);
        entity.removeScoreboardTag(CREEPER_FIREWORK_TAG);
        entity.removeScoreboardTag(CREEPER_FIREWORK_OWNER_TAG_PREFIX + playerIdRaw);
    }

}
