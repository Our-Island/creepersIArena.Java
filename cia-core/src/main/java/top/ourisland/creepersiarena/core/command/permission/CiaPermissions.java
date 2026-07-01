package top.ourisland.creepersiarena.core.command.permission;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Canonical permission nodes for the current /cia and /ciaa command trees.
 *
 * <p>The node lists are intentionally public read-only metadata: command-tree tests can verify
 * that the registered permission hierarchy stays in sync without booting Bukkit.</p>
 */
public final class CiaPermissions {

    // ====== player command root ======
    public static final String
            COMMAND = "creepersiarena.command",
            COMMAND_JOIN = "creepersiarena.command.join",
            COMMAND_LEAVE = "creepersiarena.command.leave",
            COMMAND_JOB = "creepersiarena.command.job",
            COMMAND_TEAM = "creepersiarena.command.team",
            COMMAND_LANGUAGE = "creepersiarena.command.language",
            COMMAND_PREFERENCE = "creepersiarena.command.preference",
            COMMAND_BALANCE = "creepersiarena.command.balance",
            COMMAND_STORE = "creepersiarena.command.store",
            COMMAND_PARTICLES = "creepersiarena.command.particles";

    // ====== admin command root ======
    public static final String
            ADMIN = "creepersiarena.command.admin",
            ADMIN_GAME = "creepersiarena.command.admin.game",
            ADMIN_MODE = "creepersiarena.command.admin.mode",
            ADMIN_ARENA = "creepersiarena.command.admin.arena",
            ADMIN_SKIP = "creepersiarena.command.admin.skip",
            ADMIN_COOLDOWN = "creepersiarena.command.admin.cooldown",
            ADMIN_REGENERATION = "creepersiarena.command.admin.regeneration",
            ADMIN_MUTATION = "creepersiarena.command.admin.mutation",
            ADMIN_ABILITY = "creepersiarena.command.admin.ability",
            ADMIN_DATABASE = "creepersiarena.command.admin.database",
            ADMIN_ECONOMY = "creepersiarena.command.admin.economy",
            ADMIN_STORE = "creepersiarena.command.admin.store",
            ADMIN_ENTRANCE = "creepersiarena.command.admin.entrance",
            ADMIN_LANGUAGE = "creepersiarena.command.admin.language",
            ADMIN_RELOAD = "creepersiarena.command.admin.reload",
            ADMIN_EXTENSION = "creepersiarena.command.admin.extension",
            ADMIN_CONFIG = "creepersiarena.command.admin.config";

    private static final List<String> PLAYER_CHILD_NODES = List.of(
            COMMAND_JOIN,
            COMMAND_LEAVE,
            COMMAND_JOB,
            COMMAND_TEAM,
            COMMAND_LANGUAGE,
            COMMAND_PREFERENCE,
            COMMAND_BALANCE,
            COMMAND_STORE,
            COMMAND_PARTICLES
    );

    private static final List<String> ADMIN_CHILD_NODES = List.of(
            ADMIN_GAME,
            ADMIN_MODE,
            ADMIN_ARENA,
            ADMIN_SKIP,
            ADMIN_COOLDOWN,
            ADMIN_REGENERATION,
            ADMIN_MUTATION,
            ADMIN_ABILITY,
            ADMIN_DATABASE,
            ADMIN_ECONOMY,
            ADMIN_STORE,
            ADMIN_ENTRANCE,
            ADMIN_LANGUAGE,
            ADMIN_RELOAD,
            ADMIN_EXTENSION,
            ADMIN_CONFIG
    );

    private CiaPermissions() {
    }

    /**
     * Direct children granted by the public player-command root.
     */
    public static List<String> playerChildNodes() {
        return PLAYER_CHILD_NODES;
    }

    /**
     * Direct children granted by the admin-command root.
     */
    public static List<String> adminChildNodes() {
        return ADMIN_CHILD_NODES;
    }

    /**
     * Complete permission declaration used by {@link #registerAll(Plugin)}.
     */
    public static List<String> allNodes() {
        var nodes = new java.util.ArrayList<String>(2 + PLAYER_CHILD_NODES.size() + ADMIN_CHILD_NODES.size());
        nodes.add(COMMAND);
        nodes.addAll(PLAYER_CHILD_NODES);
        nodes.add(ADMIN);
        nodes.addAll(ADMIN_CHILD_NODES);
        return List.copyOf(nodes);
    }

    public static void registerAll(Plugin plugin) {
        var playerRoot = ensure(COMMAND, PermissionDefault.TRUE);
        PLAYER_CHILD_NODES.forEach(node -> addChild(playerRoot, ensure(node, PermissionDefault.TRUE)));

        var adminRoot = ensure(ADMIN, PermissionDefault.OP);
        ADMIN_CHILD_NODES.forEach(node -> addChild(adminRoot, ensure(node, PermissionDefault.OP)));

        plugin.getLogger().info("Registered permissions (programmatic): command tree, admin tree.");
    }

    private static Permission ensure(String node, PermissionDefault def) {
        var existing = Bukkit.getPluginManager().getPermission(node);
        if (existing != null) {
            existing.setDefault(def);
            return existing;
        }
        var created = new Permission(node, def);
        Bukkit.getPluginManager().addPermission(created);
        return created;
    }

    private static void addChild(Permission parent, Permission child) {
        var children = parent.getChildren();
        children.put(child.getName(), true);
        parent.recalculatePermissibles();
    }

}
