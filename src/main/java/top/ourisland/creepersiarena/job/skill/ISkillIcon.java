package top.ourisland.creepersiarena.job.skill;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Builds the visible item representation of a skill.
 * <p>
 * Skill icons are used by the built-in hotbar/UI layer to present activatable skills, passives and cooldown states to
 * players. The icon is generated per viewer instead of once globally, which allows an implementation to adapt the
 * resulting {@link ItemStack} to player-specific context such as translated lore, active cooldowns, temporary mode
 * changes or team-dependent styling.
 *
 * <h2>Design constraints</h2>
 * Implementations should return a fresh or safely reusable item stack that the caller can place into inventories
 * without leaking mutable metadata between players. They should also avoid depending on client-only behaviour: the icon
 * is purely a server-side representation of the skill in the current runtime.
 *
 * @see ISkillDefinition
 * @see top.ourisland.creepersiarena.job.skill.ui.SkillHotbarRenderer
 */
@FunctionalInterface
public interface ISkillIcon {

    /**
     * Builds the item shown to the supplied player for this skill.
     * <p>
     * The returned item may encode translated name/lore, cooldown information, passive status and any custom item meta
     * required by the skill UI.
     *
     * @param player player who will receive and see the icon
     * @return item stack representing the skill for that player
     */
    ItemStack buildIcon(Player player);

}
