package top.ourisland.creepersiarena.api.job;

import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.metadata.JobMetadata;

/**
 * Declarative entry point for a playable job.
 * <p>
 * A job is the player-facing archetype selected in the lobby and materialised when the player enters combat. It defines
 * the base equipment package the player receives and serves as the ownership root for the skill set registered under
 * the same job id.
 *
 * <h2>Metadata vs content</h2>
 * Similar to modes and skills, an {@code IJob} combines:
 * <ul>
 *     <li><strong>Static registration metadata</strong> supplied by {@code @CiaJobDef} and resolved via
 *     {@link JobMetadata}</li>
 *     <li><strong>Concrete game content</strong> supplied by the implementation itself: lobby icon, armor template and
 *     base hotbar items</li>
 * </ul>
 *
 * <h2>What a job should contain</h2>
 * A job definition should describe the durable, reusable equipment identity of the archetype. Temporary or highly
 * stateful behaviour such as cooldowns, per-player counters, temporary buffs and active skill execution belongs in the
 * skill runtime or other combat services, not in mutable fields on the shared job definition instance.
 *
 * <h2>Equipment layering</h2>
 * The built-in combat equipment pipeline typically layers content in this order:
 * <ol>
 *     <li>job-provided armor via {@link #armorTemplate(PlayerSession)} or {@link #armorTemplate()}</li>
 *     <li>job-provided base hotbar items via {@link #hotbarTemplate(PlayerSession)}</li>
 *     <li>runtime skill icons placed by the skill UI/renderer</li>
 * </ol>
 * This means jobs should return the base combat kit, while activatable skill items are usually supplied separately by
 * the skill system.
 *
 * <h2>Addon compatibility</h2>
 * Built-in jobs use the {@code cia} namespace. Addon jobs should use their own namespace so ids remain globally unique.
 *
 * @see top.ourisland.creepersiarena.api.annotation.CiaJobDef
 * @see top.ourisland.creepersiarena.api.skill.ISkillDefinition
 *
 */
public interface IJob {

    /**
     * Returns the stable namespaced id of the job.
     * <p>
     * The id is resolved from the attached job annotation and acts as the public registry identity for selection,
     * configuration, skill ownership and language-key derivation.
     *
     * @return registry id resolved from job metadata
     */
    default JobId id() {
        return JobMetadata.of(getClass()).id();
    }

    /**
     * Returns whether the job should be enabled by default before configuration overrides are applied.
     *
     * @return annotation-declared default enabled state
     */
    default boolean enabled() {
        return JobMetadata.of(getClass()).enabledByDefault();
    }

    /**
     * Builds the item shown while selecting the job in lobby UI.
     * <p>
     * This item is a preview/selection icon rather than the actual combat kit. It should communicate the identity of
     * the job clearly through material, display name and lore.
     *
     * @return preview item representing the job in selection interfaces
     */
    ItemStack display();

    /**
     * Builds the armor template for a specific player session.
     * <p>
     * This overload exists for jobs whose armor varies with contextual data such as team colour, active mode or other
     * session-scoped information. Implementations that do not need context may simply rely on
     * {@link #armorTemplate()}.
     *
     * @param session player session the armor is being generated for
     *
     * @return armor contents for the player, using Bukkit's armor ordering
     */
    default ItemStack[] armorTemplate(PlayerSession session) {
        return armorTemplate();
    }

    /**
     * Builds the default armor template for the job.
     * <p>
     * The returned array should describe the persistent/base armor of the archetype. Runtime-only overlays such as
     * temporary buffs are better applied by combat logic at equip time.
     *
     * @return base armor contents for the job
     */
    ItemStack[] armorTemplate();

    /**
     * Builds the base hotbar template for a specific player session.
     * <p>
     * Jobs can place weapons, consumables and other non-skill base items here. The returned array covers the nine
     * hotbar slots only; skill icons may later overwrite or fill some of those slots as part of the combat UI layer.
     * {@code null} entries mean the slot should be left empty at the job layer.
     *
     * @param session player session the hotbar is being generated for
     *
     * @return nine-slot hotbar template with optional empty entries
     */
    default ItemStack[] hotbarTemplate(PlayerSession session) {
        return new ItemStack[9];
    }

}
