package top.ourisland.creepersiarena.permission;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * LuckPerms integration entry.
 *
 * <p>When LuckPerms is installed, it will take over Bukkit/Paper's permission system automatically.
 * This service mainly provides access to LuckPerms API for deeper integrations (meta, groups, offline lookups).</p>
 */
public final class LuckPermsService {

    private final LuckPerms api;

    private LuckPermsService(@NonNull LuckPerms api) {
        this.api = api;
    }

    public @NonNull LuckPerms api() {
        return api;
    }

    /**
     * Attempts to resolve LuckPerms API.
     *
     * @return service instance if LuckPerms is present; otherwise null
     */
    public static @Nullable LuckPermsService tryLoad(@NonNull Plugin plugin, @NonNull Logger log) {
        // If LuckPerms is not installed, LuckPermsProvider#get throws IllegalStateException.
        if (!plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            return null;
        }
        try {
            LuckPerms api = LuckPermsProvider.get();
            return new LuckPermsService(api);
        } catch (IllegalStateException e) {
            log.warn("[LuckPerms] Plugin present but API not available yet: {}", e.getMessage());
            return null;
        }
    }
}
