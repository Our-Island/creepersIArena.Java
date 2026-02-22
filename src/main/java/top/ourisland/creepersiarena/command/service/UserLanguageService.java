package top.ourisland.creepersiarena.command.service;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class UserLanguageService {

    private final NamespacedKey userLangKey;

    public UserLanguageService(Plugin plugin) {
        this.userLangKey = new NamespacedKey(plugin, "cia_user_lang");
    }

    public String getOrNull(Player p) {
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        String v = pdc.get(userLangKey, PersistentDataType.STRING);
        if (v == null || v.isBlank()) return null;
        return v.trim();
    }

    public void set(Player p, String langOrNull) {
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        if (langOrNull == null || langOrNull.isBlank()) {
            pdc.remove(userLangKey);
            return;
        }
        pdc.set(userLangKey, PersistentDataType.STRING, langOrNull.trim());
    }
}
