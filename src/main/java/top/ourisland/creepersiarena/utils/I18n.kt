package top.ourisland.creepersiarena.util;

import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.ConfigManager;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class I18n {

    private static ResourceBundle bundle;
    private static Logger logger;
    private static ConfigManager configManager;

    private I18n() {
    }

    public static void init(ConfigManager configManager, Logger logger) {
        I18n.configManager = configManager;
        I18n.logger = logger;
        reload();
    }

    public static void reload() {
        String lang = configManager.globalConfig().lang();
        loadBundle(lang);
    }

    private static void loadBundle(String lang) {
        try {
            bundle = ResourceBundle.getBundle("lang/" + lang);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle("lang/en_us");
            if (logger != null) logger.warn("[I18n] Failed to load language '{}', fallback to en_us", lang);
        }
    }

    public static Component lang(String key) {
        return Component.text(langStr(key));
    }

    public static String langStr(String key) {
        return prefixStr() + pattern(key);
    }

    private static String prefixStr() {
        if (bundle == null) return "";
        return bundle.getString("cia.prefix");
    }

    private static String pattern(String key) {
        if (bundle == null) return key;
        return bundle.getString(key);
    }

    public static Component lang(String key, Object... args) {
        return Component.text(langStr(key, args));
    }

    public static String langStr(String key, Object... args) {
        return prefixStr() + format(key, args);
    }

    private static String format(String key, Object... args) {
        String p = pattern(key);
        return (args == null || args.length == 0) ? p : MessageFormat.format(p, args);
    }

    public static Component langOrNullNP(String key, Object... args) {
        if (!has(key)) return null;
        return (args == null || args.length == 0) ? langNP(key) : langNP(key, args);
    }

    public static boolean has(String key) {
        return bundle != null && bundle.containsKey(key);
    }

    public static Component langNP(String key) {
        return Component.text(langStrNP(key));
    }

    public static Component langNP(String key, Object... args) {
        return Component.text(langStrNP(key, args));
    }

    public static String langStrNP(String key) {
        return pattern(key);
    }

    public static String langStrNP(String key, Object... args) {
        return format(key, args);
    }
}
