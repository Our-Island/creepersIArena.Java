package top.ourisland.creepersiarena.job.skill;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.ourisland.creepersiarena.util.I18n;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SkillItemFactory {
    private final SkillItemCodec codec;

    public SkillItemFactory(SkillItemCodec codec) {
        this.codec = codec;
    }

    public ItemStack create(Skill skill, Object... loreArgs) {
        ItemStack item = new ItemStack(skill.itemType());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(I18n.langNP(SkillLangKeys.name(skill)));

        List<Component> lore = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> SkillLangKeys.lore(skill, i))
                .takeWhile(I18n::has)
                .map(key -> (loreArgs == null || loreArgs.length == 0)
                        ? I18n.langNP(key)
                        : I18n.langNP(key, loreArgs)
                )
                .collect(Collectors.toList());

        if (!lore.isEmpty()) meta.lore(lore);

        item.setItemMeta(meta);
        codec.mark(item, skill);
        return item;
    }

    public ItemStack createCooldownSeconds(Skill skill, int secondsLeft) {
        int amount = clampAmount(secondsLeft);
        ItemStack item = new ItemStack(Material.STONE_BUTTON, amount);
        decorateCooldownName(skill, item);
        codec.mark(item, skill);
        return item;
    }

    private static int clampAmount(int n) {
        if (n < 1) return 1;
        return Math.min(64, n);
    }

    private static void decorateCooldownName(Skill skill, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Component name = Component.text("[冷却中] ")
                .append(I18n.langNP(SkillLangKeys.name(skill)));
        meta.displayName(name);

        item.setItemMeta(meta);
    }

    public ItemStack createCooldownLastSecondTicks(Skill skill, int ticksLeft) {
        int amount = clampAmount(ticksLeft);
        ItemStack item = new ItemStack(Material.BIRCH_BUTTON, amount);
        decorateCooldownName(skill, item);
        codec.mark(item, skill);
        return item;
    }

    public static final class SkillLangKeys {
        private SkillLangKeys() {
        }

        public static String name(Skill skill) {
            return base(skill) + ".name";
        }

        public static String base(Skill skill) {
            String id = skill.id();
            int dot = id.indexOf('.');
            if (dot <= 0 || dot == id.length() - 1) {
                throw new IllegalArgumentException("Invalid skill id (expected job.skill): " + id);
            }
            String job = id.substring(0, dot);
            String sk = id.substring(dot + 1);
            return "cia.job." + job + ".skill." + sk;
        }

        public static String lore(Skill skill, int line) {
            return base(skill) + ".lore." + line;
        }
    }
}
