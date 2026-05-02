package top.ourisland.creepersiarena.api.config;

/**
 * Stable read-only view of skill configuration exposed to skill extensions.
 * <p>
 * Implementations decide how configuration is loaded and namespaced. Extension code should depend on this generic view
 * instead of core-owned configuration model classes.
 */
public interface ISkillConfigView {

    int cooldownSeconds(String skillId, int defaultValue);

    int getInt(String skillId, String key, int defaultValue);

    long getLong(String skillId, String key, long defaultValue);

    double getDouble(String skillId, String key, double defaultValue);

    boolean getBoolean(String skillId, String key, boolean defaultValue);

}
