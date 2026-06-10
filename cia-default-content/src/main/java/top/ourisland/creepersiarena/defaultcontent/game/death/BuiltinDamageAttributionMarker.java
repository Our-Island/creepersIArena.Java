package top.ourisland.creepersiarena.defaultcontent.game.death;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.defaultcontent.job.utils.BuiltinKeys;

import java.util.Optional;
import java.util.UUID;

public final class BuiltinDamageAttributionMarker {

    private BuiltinDamageAttributionMarker() {
    }

    public static void markEntitySource(
            Entity entity,
            Player owner,
            DeathCauseId causeId
    ) {
        markEntitySource(entity, owner, causeId, null);
    }

    public static void markEntitySource(
            Entity entity,
            Player owner,
            DeathCauseId causeId,
            String sourceSkillId
    ) {
        if (entity == null || owner == null || causeId == null) return;
        write(entity.getPersistentDataContainer(), owner.getUniqueId(), causeId, sourceSkillId);
    }

    private static void write(
            PersistentDataContainer container,
            UUID ownerId,
            DeathCauseId causeId,
            String sourceSkillId
    ) {
        container.set(
                BuiltinKeys.key("death_source_owner"),
                PersistentDataType.STRING,
                ownerId.toString()
        );
        container.set(
                BuiltinKeys.key("death_cause_id"),
                PersistentDataType.STRING,
                causeId.toString()
        );
        if (sourceSkillId == null || sourceSkillId.isBlank()) {
            container.remove(BuiltinKeys.key("death_source_skill"));
        } else {
            container.set(
                    BuiltinKeys.key("death_source_skill"),
                    PersistentDataType.STRING,
                    sourceSkillId
            );
        }
    }

    public static void markNextDamage(
            Player victim,
            Player attacker,
            DeathCauseId causeId
    ) {
        markNextDamage(victim, attacker, causeId, null);
    }

    public static void markNextDamage(
            Player victim,
            Player attacker,
            DeathCauseId causeId,
            String sourceSkillId
    ) {
        if (victim == null || attacker == null || causeId == null) return;
        write(victim.getPersistentDataContainer(), attacker.getUniqueId(), causeId, sourceSkillId);
        victim.getPersistentDataContainer().set(
                BuiltinKeys.key("death_next_damage"),
                PersistentDataType.BYTE,
                (byte) 1
        );
    }

    public static Optional<MarkedDamageSource> readEntitySource(Entity entity) {
        if (entity == null) return Optional.empty();

        return read(entity.getPersistentDataContainer());
    }

    private static Optional<MarkedDamageSource> read(PersistentDataContainer container) {
        String ownerRaw = container.get(BuiltinKeys.key("death_source_owner"), PersistentDataType.STRING);
        String causeRaw = container.get(BuiltinKeys.key("death_cause_id"), PersistentDataType.STRING);
        if (ownerRaw == null || causeRaw == null) return Optional.empty();

        try {
            var ownerId = UUID.fromString(ownerRaw);
            var causeId = DeathCauseId.parse(causeRaw);
            var skillId = container.get(BuiltinKeys.key("death_source_skill"), PersistentDataType.STRING);
            return Optional.of(new MarkedDamageSource(ownerId, causeId, skillId));
        } catch (IllegalArgumentException _) {
            return Optional.empty();
        }
    }

    public static Optional<MarkedDamageSource> consumeNextDamage(Player victim) {
        if (victim == null) return Optional.empty();

        var container = victim.getPersistentDataContainer();
        if (!container.has(BuiltinKeys.key("death_next_damage"), PersistentDataType.BYTE)) {
            return Optional.empty();
        }

        Optional<MarkedDamageSource> marked = read(container);
        clearNextDamage(victim);
        return marked;
    }

    public static void clearNextDamage(Player player) {
        if (player == null) return;
        var container = player.getPersistentDataContainer();
        container.remove(BuiltinKeys.key("death_next_damage"));
        container.remove(BuiltinKeys.key("death_source_owner"));
        container.remove(BuiltinKeys.key("death_cause_id"));
        container.remove(BuiltinKeys.key("death_source_skill"));
    }

    public record MarkedDamageSource(
            UUID ownerId,
            DeathCauseId causeId,
            String sourceSkillId
    ) {

    }

}
