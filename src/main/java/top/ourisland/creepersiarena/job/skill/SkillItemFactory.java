package top.ourisland.creepersiarena.job.skill;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.ourisland.creepersiarena.util.I18n;
import top.ourisland.creepersiarena.util.LangKeyResolver;

import java.util.List;

public final class SkillItemFactory {
    private final SkillItemCodec codec;

    public SkillItemFactory(SkillItemCodec codec) {
        this.codec = codec;
    }

    private static void decorateCooldownName(Skill skill, ItemStack item, String text) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Component name = Component.text(text).appendSpace()
                .append(I18n.langNP(LangKeyResolver.skillName(skill)));
        meta.displayName(name);

        item.setItemMeta(meta);
    }

    public ItemStack createCooldownSeconds(Skill skill, int secondsLeft) {
        int amount = clampAmount(secondsLeft);
        ItemStack item = new ItemStack(Material.STONE_BUTTON, amount);
        decorateCooldownName(skill, item, "[技能冷却中]");
        codec.mark(item, skill);
        return item;
    }

    private static int clampAmount(int n) {
        if (n < 1) return 1;
        return Math.min(64, n);
    }

    public ItemStack create(Skill skill, Object... loreArgs) {
        ItemStack item = new ItemStack(skill.itemType());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(I18n.langNP(LangKeyResolver.skillName(skill)));

        List<Component> lore = LangKeyResolver.resolveSkillLore(skill, loreArgs);
        if (!lore.isEmpty()) meta.lore(lore);

        item.setItemMeta(meta);
        codec.mark(item, skill);
        return item;
    }

    public ItemStack createCooldownLastSecondTicks(Skill skill, int ticksLeft) {
        int amount = clampAmount(ticksLeft);
        ItemStack item = new ItemStack(Material.BIRCH_BUTTON, amount);
        decorateCooldownName(skill, item, "[即将冷却完毕]");
        codec.mark(item, skill);
        return item;
    }
}
