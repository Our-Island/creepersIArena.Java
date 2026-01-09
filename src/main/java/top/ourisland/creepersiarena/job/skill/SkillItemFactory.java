package top.ourisland.creepersiarena.job.skill;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.ourisland.creepersiarena.util.I18n;

import java.util.ArrayList;
import java.util.List;

public final class SkillItemFactory {
    private final SkillItemCodec codec;

    public SkillItemFactory(SkillItemCodec codec) {
        this.codec = codec;
    }

    public ItemStack create(Skill skill, Object... loreArgs) {
        ItemStack item = new ItemStack(skill.itemType());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(I18n.langNP(SkillLangKeys.name(skill)));

        List<Component> lore = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String key = SkillLangKeys.lore(skill, i);
            if (!I18n.has(key)) break;

            lore.add((loreArgs == null || loreArgs.length == 0)
                    ? I18n.langNP(key)
                    : I18n.langNP(key, loreArgs));
        }

        if (!lore.isEmpty()) meta.lore(lore);

        item.setItemMeta(meta);
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
